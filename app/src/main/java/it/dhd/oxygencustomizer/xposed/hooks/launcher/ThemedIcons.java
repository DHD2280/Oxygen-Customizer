package it.dhd.oxygencustomizer.xposed.hooks.launcher;

import static org.lsposed.hiddenapibypass.HiddenApiBypass.newInstance;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticIntField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.SystemUtils.PackageManager;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageItemInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.GoogleMonochromeIconFactory;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class ThemedIcons extends XposedMods {

    private final static String listenPackage = LAUNCHER;
    private final static String TAG = "Oxygen Customizer - Launcher - ThemedIcons: ";
    private Set<String> mAllowedMonochrome = new HashSet<>();
    Set<String> defaultValues = new HashSet<>(Arrays.asList("0", "1", "2", "3", "4"));
    private boolean mDrawerMonochrome = false;
    private boolean mWorkspaceMonochrome = false;
    private boolean mFolderMonochrome = false;
    private boolean mSearchMonochrome = false;
    private boolean mTaskbarMonochrome = false;

    private static final int DISPLAY_ALL_APPS = 1;
    private static final int DISPLAY_FOLDER = 2;
    private static final int DISPLAY_SEARCH_RESULT = 6;
    private static final int DISPLAY_SEARCH_RESULT_SMALL = 7;
    private static final int DISPLAY_TASKBAR = 5;
    private static final int DISPLAY_WORKSPACE = 0;

    private Object mIconProvider = null;
    private Object mIconObserver = null;

    private static boolean ForceThemedLauncherIcons = false;
    private static boolean mAlternative = false;
    private int mIconBitmapSize;

    public ThemedIcons(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        ForceThemedLauncherIcons = Xprefs.getBoolean("force_themed_launcher_icons", false);
        mAlternative = Xprefs.getBoolean("alternative_monochrome", false);
        mAllowedMonochrome = Xprefs.getStringSet("themed_icons_where", defaultValues);
        mDrawerMonochrome = mAllowedMonochrome.contains("1");
        mWorkspaceMonochrome = mAllowedMonochrome.contains("0");
        mFolderMonochrome = mAllowedMonochrome.contains("2");
        mSearchMonochrome = mAllowedMonochrome.contains("3");
        mTaskbarMonochrome = mAllowedMonochrome.contains("4");

        if (Key.length > 0 &&
                (Key[0].equals("force_themed_launcher_icons") ||
                        Key[0].equals("themed_icons_where") ||
                        Key[0].equals("alternative_monochrome"))) {
            updateIcons();
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            Class<?> BaseIconFactoryClass = findClass("com.android.launcher3.icons.BaseIconFactory", lpparam.classLoader);

            hookAllConstructors(BaseIconFactoryClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mIconBitmapSize = getIntField(param.thisObject, "mIconBitmapSize");
                }
            });


            ReflectedClass UxIconLoaderHelper = ReflectedClass.of("com.oplus.uxicon.ui.util.UxIconLoaderHelper");
            UxIconLoaderHelper
                    .after("getIconThemeDrawable")
                    .run(param -> {
                        if (!ForceThemedLauncherIcons) return;
                        PackageItemInfo info = (PackageItemInfo) param.args[1];
                        if (mAlternative && param.getResult() == null) {
                            if (BuildConfig.DEBUG) XposedBridge.log("ThemedIcons: getIconThemeDrawable is null for " + info.packageName);
                            Drawable icon = info.loadIcon(PackageManager());
                            GoogleMonochromeIconFactory monochrome = new GoogleMonochromeIconFactory(icon, mIconBitmapSize);
                            param.setResult(monochrome);
                        }
                    });

            hookAllMethods(AdaptiveIconDrawable.class, "getMonochrome", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if (param.getResult() == null && ForceThemedLauncherIcons && !mAlternative) {

                            if (new Throwable().getStackTrace()[4].getMethodName().toLowerCase().contains("override")) //It's from com.android.launcher3.icons.IconProvider.getIconWithOverrides. Monochrome is included
                            {
                                return;
                            }

                            GoogleMonochromeIconFactory mono = (GoogleMonochromeIconFactory) getAdditionalInstanceField(param.thisObject, "mMonoFactoryOC");
                            if (mono == null) {
                                mono = new GoogleMonochromeIconFactory((AdaptiveIconDrawable) param.thisObject, mIconBitmapSize);
                                setAdditionalInstanceField(param.thisObject, "mMonoFactoryOC", mono);
                            }
                            param.setResult(mono);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable t) {
            log(t.getMessage());
        }

        ReflectedClass LauncherAppState = ReflectedClass.of("com.android.launcher3.LauncherAppState");
        LauncherAppState
                .afterConstruction()
                .run(param -> {
                    mIconProvider = getObjectField(param.thisObject, "mIconProvider");
                });
        ReflectedClass LauncherIconObserver = ReflectedClass.of("com.android.launcher3.LauncherAppState$IconObserver");
        LauncherIconObserver
                .afterConstruction()
                .run(param -> mIconObserver = param.thisObject);

        ReflectedClass OplusBubbleTextView = ReflectedClass.of("com.android.launcher3.OplusBubbleTextView");
        OplusBubbleTextView
                .after("applyIconAndLabel")
                .run(param -> {
                    Object itemInfoWithIcon = param.args[0];
                    if (itemInfoWithIcon == null) return;
                    int mDisplay = getIntField(param.thisObject, "mDisplay");
                    boolean isIconThemed = getBooleanField(itemInfoWithIcon, "mIsIconEdited");
                    if (isIconThemed) return;
                    // ((mContext,
                    // (Themes.isThemedIconEnabled(getContext())
                    // && (i9 == 0 || i9 == 2 || i9 == 5 || i9 == 8 || i9 == 1 || i9 == 9)
                    // && !itemInfoWithIcon.mIsIconEdited) ? 1 : 0
                    Object newIcon = callMethod(itemInfoWithIcon, "newIcon", mContext, shouldUseTheme(mDisplay) ? 1 : 0);
                    callMethod(param.thisObject, "setIcon", newIcon);
                });

        // Folder Previews
        ReflectedClass PreviewItemManager = ReflectedClass.of("com.android.launcher3.folder.PreviewItemManager");
        PreviewItemManager
                .before("getNewIcon")
                .run(param -> {
                    int theme = 1;
                    if (!mWorkspaceMonochrome) {
                        theme = 0;
                    }
                    param.args[1] = theme;
                });
    }

    private boolean shouldUseTheme(int display) {
        return switch (display) {
            case DISPLAY_ALL_APPS -> mDrawerMonochrome;
            case DISPLAY_FOLDER -> mFolderMonochrome;
            case DISPLAY_SEARCH_RESULT, DISPLAY_SEARCH_RESULT_SMALL -> mSearchMonochrome;
            case DISPLAY_TASKBAR -> mTaskbarMonochrome;
            default -> mWorkspaceMonochrome;
        };
    }

    private void updateIcons() {
        try {
            String systemIconState = (String) callMethod(mIconProvider, "getSystemIconState");
            callMethod(mIconObserver, "onSystemIconStateChanged", systemIconState);
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
