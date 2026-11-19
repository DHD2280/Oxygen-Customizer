package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.NOTIFICATION_STYLE_PACK;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.NotificationStylePackUtil;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers;

public class NotificationStylePacks extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private static volatile boolean sBackgroundReplaced = false;

    private final Map<Integer, Replacement> mMapping = new ConcurrentHashMap<>();
    private final Set<String> mTypes = new HashSet<>();
    private final List<XposedInterface.HookHandle> mHooks = new ArrayList<>();
    private String mStyleKey = null;
    private Context mPackContext;
    private Resources mPackResources;
    private Resources.Theme mPackTheme;
    private int mPackUiMode = -1, mPackDensity = -1;
    private int mBackgroundId = 0;
    private boolean mPackageLoaded = false;

    private static final class Replacement {
        final String targetPackage;
        final int id;

        Replacement(String targetPackage, int id) {
            this.targetPackage = targetPackage;
            this.id = id;
        }
    }

    public NotificationStylePacks(Context context) {
        super(context);
    }

    public static boolean isBackgroundReplaced() {
        return sBackgroundReplaced;
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        String key = Xprefs.getString(NOTIFICATION_STYLE_PACK, "");
        if (key.equals(mStyleKey)) return;
        mStyleKey = key;
        loadStyle();
    }

    @SuppressLint("DiscouragedApi")
    private synchronized void loadStyle() {
        mMapping.clear();
        mTypes.clear();
        mPackContext = null;
        mPackResources = null;
        mPackTheme = null;
        mBackgroundId = 0;
        sBackgroundReplaced = false;

        NotificationStylePackUtil.Style style = TextUtils.isEmpty(mStyleKey) ? null : NotificationStylePackUtil.loadStyle(mContext, mStyleKey);
        if (style != null) {
            try {
                mPackContext = mContext.createPackageContext(style.packageName, 0);
                Resources packRes = mPackContext.getResources();
                Resources res = mContext.getResources();
                for (NotificationStylePackUtil.Entry entry : style.entries) {
                    int target = res.getIdentifier(entry.name, entry.type, entry.targetPackage);
                    int replacement = packRes.getIdentifier(entry.replacement, entry.type, style.packageName);
                    if (target == 0 || replacement == 0) continue;
                    mMapping.put(target, new Replacement(entry.targetPackage, replacement));
                    mTypes.add(entry.type);
                    if (entry.type.equals("drawable") && entry.name.equals(NotificationStylePackUtil.BACKGROUND_NAME) && entry.targetPackage.equals(SYSTEM_UI)) {
                        mBackgroundId = target;
                    }
                }
            } catch (Throwable t) {
                log(t);
                mMapping.clear();
                mTypes.clear();
            }
        }
        sBackgroundReplaced = mBackgroundId != 0;

        unhookAll();
        if (mPackageLoaded) hookResources();
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        mPackageLoaded = true;
        hookResources();
        hookBackground();
    }

    private synchronized void hookResources() {
        if (!mHooks.isEmpty() || mMapping.isEmpty()) return;
        if (mTypes.contains("drawable")) {
            hookResourceMethod("getDrawable", int.class, Resources.Theme.class);
            hookResourceMethod("getDrawableForDensity", int.class, int.class, Resources.Theme.class);
        }
        if (mTypes.contains("color")) {
            hookResourceMethod("getColor", int.class, Resources.Theme.class);
            hookResourceMethod("getColorStateList", int.class, Resources.Theme.class);
        }
        if (mTypes.contains("bool")) {
            hookResourceMethod("getBoolean", int.class);
        }
        if (mTypes.contains("integer")) {
            hookResourceMethod("getInteger", int.class);
        }
        if (mTypes.contains("dimen")) {
            hookResourceMethod("getDimension", int.class);
            hookResourceMethod("getDimensionPixelSize", int.class);
            hookResourceMethod("getDimensionPixelOffset", int.class);
            hookResourceMethod("getFloat", int.class);
        }
    }

    private void hookResourceMethod(String name, Class<?>... parameterTypes) {
        Method method = ReflectionHelpers.findMethodExact(Resources.class, name, (Object[]) parameterTypes);
        boolean themed = parameterTypes[parameterTypes.length - 1] == Resources.Theme.class;
        mHooks.addAll(ReflectedClass.of(Resources.class)
                .before(method)
                .run(param -> {
                    Resources res = (Resources) param.thisObject;
                    Replacement replacement = lookup(res, (int) param.args[0]);
                    if (replacement == null) return;
                    Resources packRes = packResources(res);
                    if (packRes == null) return;
                    Object[] args = param.args.clone();
                    args[0] = replacement.id;
                    if (themed) args[args.length - 1] = mPackTheme;
                    try {
                        param.setResult(method.invoke(packRes, args));
                    } catch (Throwable t) {
                        log(t);
                    }
                }));
    }

    private void unhookAll() {
        for (XposedInterface.HookHandle hook : mHooks) {
            hook.unhook();
        }
        mHooks.clear();
    }

    private Replacement lookup(Resources res, int id) {
        Replacement replacement = mMapping.get(id);
        if (replacement == null) return null;
        try {
            return replacement.targetPackage.equals(res.getResourcePackageName(id)) ? replacement : null;
        } catch (Resources.NotFoundException ignored) {
            return null;
        }
    }

    private synchronized Resources packResources(Resources caller) {
        if (mPackContext == null) return null;
        Configuration config = caller.getConfiguration();
        int uiMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (mPackResources == null || uiMode != mPackUiMode || config.densityDpi != mPackDensity) {
            mPackResources = mPackContext.createConfigurationContext(config).getResources();
            mPackTheme = mPackResources.newTheme();
            mPackTheme.applyStyle(android.R.style.Theme_DeviceDefault_DayNight, true);
            mPackUiMode = uiMode;
            mPackDensity = config.densityDpi;
        }
        return mPackResources;
    }

    private void hookBackground() {
        if (Build.VERSION.SDK_INT < 37) return;

        ReflectedClass.ofIfPossible("com.oplus.systemui.statusbar.notification.row.OplusExpandableViewExImpl")
                .before("getCustomBackground")
                .run(param -> {
                    if (!sBackgroundReplaced) return;
                    param.setResult(((Context) param.args[0]).getDrawable(mBackgroundId));
                });

        ReflectedClass.ofIfPossible("com.oplus.systemui.statusbar.notification.row.NotificationBackgroundViewExtImp")
                .before("draw")
                .run(param -> {
                    if (!sBackgroundReplaced || param.args.length != 2 || !(param.args[0] instanceof Canvas))
                        return;
                    if (param.args[1] == null) {
                        param.setResult(null);
                        return;
                    }
                    View bgView = (View) callMethod(param.thisObject, "getBgView");
                    callMethod(bgView, "draw", param.args[0], param.args[1]);
                    param.setResult(null);
                });

        ReflectedClass.ofIfPossible("com.android.systemui.statusbar.notification.row.NotificationBackgroundView")
                .before("setTint")
                .run(param -> {
                    if (sBackgroundReplaced) param.setResult(null);
                });
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
