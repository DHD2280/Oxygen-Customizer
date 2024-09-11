package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_BLUR_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_FILTER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_TINT_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_TINT_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_SHOW_ALBUM_ART;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_PHOTO_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_WIDGETS_LIST;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_WIDGETS_SWITCH;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.views.QsControlsView;

public class QsWidgets extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private static final String TAG = "QsWidgets: ";

    private ViewGroup mOplusQsMediaView = null;

    private boolean mQsWidgetsEnabled = false;
    private String mQsWidgetsList = "media";

    // Photo Showcase
    private int mQsPhotoRadius = 22;

    // Qs Media Tile colors
    private Drawable mDefaultMediaBg = null;
    private boolean qsInactiveColorEnabled = false;
    private int qsInactiveColor = Color.WHITE;

    // Qs Media Tile Album Art
    private boolean showMediaArtMediaQs = false;
    private int mMediaQsArtFilter = 0, mMediaQsTintColor = Color.WHITE, mMediaQsTintAmount = 20;
    private float mMediaQsArtBlurAmount = 7.5f;

    public static Object mActivityStarter = null;

    private LinearLayout mWidgetsContainer;

    public QsWidgets(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        mQsWidgetsEnabled = Xprefs.getBoolean(QS_WIDGETS_SWITCH, false);
        mQsWidgetsList = Xprefs.getString(QS_WIDGETS_LIST, "media");
        mQsPhotoRadius = Xprefs.getSliderInt(QS_PHOTO_RADIUS, 22);
        // Media QS
        qsInactiveColorEnabled = Xprefs.getBoolean(QS_TILE_INACTIVE_COLOR_ENABLED, false);
        qsInactiveColor = Xprefs.getInt(QS_TILE_INACTIVE_COLOR, Color.WHITE);
        showMediaArtMediaQs = Xprefs.getBoolean(QS_MEDIA_SHOW_ALBUM_ART, false);
        mMediaQsArtFilter = Integer.parseInt(Xprefs.getString(QS_MEDIA_ART_FILTER, "0"));
        mMediaQsArtBlurAmount = (Xprefs.getSliderInt(QS_MEDIA_ART_BLUR_AMOUNT, 30)/100f) * 25f;
        mMediaQsTintColor = Xprefs.getInt(QS_MEDIA_ART_TINT_COLOR, Color.WHITE);
        mMediaQsTintAmount = Xprefs.getSliderInt(QS_MEDIA_ART_TINT_AMOUNT, 20);

        if (Key.length > 0) {
            if (Key[0].equals(QS_WIDGETS_LIST)) {
                updateWidgets();
            }
            if (Key[0].equals(QS_PHOTO_RADIUS)) {
                updatePhotoRadius();
            }
            if (Key[0].equals(QS_MEDIA_SHOW_ALBUM_ART) ||
                    Key[0].equals(QS_MEDIA_ART_FILTER) ||
                    Key[0].equals(QS_MEDIA_ART_BLUR_AMOUNT) ||
                    Key[0].equals(QS_MEDIA_ART_TINT_COLOR) ||
                    Key[0].equals(QS_MEDIA_ART_TINT_AMOUNT)) {
                updateMediaPlayerPrefs();
            }
            if (Key[0].equals(QS_TILE_INACTIVE_COLOR) ||
                    Key[0].equals(QS_TILE_INACTIVE_COLOR_ENABLED)) {
                updateControlsBg();
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        log(TAG + "Hooking OplusQSTileMediaContainer");

        Class<?> OplusQSTileMediaContainer;
        try {
            OplusQSTileMediaContainer = findClass("com.oplus.systemui.qs.OplusQSTileMediaContainer", lpparam.classLoader);
        } catch (Throwable t) {
            OplusQSTileMediaContainer = findClass("com.oplusos.systemui.qs.OplusQSContainerImpl", lpparam.classLoader); //OOS 13
        }
        hookAllConstructors(OplusQSTileMediaContainer, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!mQsWidgetsEnabled) return;
                setBooleanField(param.thisObject, "mIsMediaMode", true);
            }
        });
        hookAllMethods(OplusQSTileMediaContainer, "setMediaMode", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!mQsWidgetsEnabled) return;
                param.args[0] = true;
            }
        });
        if (Build.VERSION.SDK_INT == 33) {
            hookAllMethods(OplusQSTileMediaContainer, "setQsMediaPanelShown", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!mQsWidgetsEnabled) return;
                    param.args[0] = true;
                }
            });
        }

        Class<?> QSSecurityFooterUtilsClass;
        try {
            QSSecurityFooterUtilsClass = findClass("com.android.systemui.qs.QSSecurityFooterUtils", lpparam.classLoader);
        } catch (Throwable t) {
            QSSecurityFooterUtilsClass = findClass("com.android.systemui.qs.QSSecurityFooter", lpparam.classLoader);
        }
        hookAllConstructors(QSSecurityFooterUtilsClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mActivityStarter = getObjectField(param.thisObject, "mActivityStarter");
            }
        });

        Class<?> OplusQsMediaPanelView = findClass("com.oplus.systemui.qs.media.OplusQsMediaPanelView", lpparam.classLoader);
        hookAllMethods(OplusQsMediaPanelView, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!mQsWidgetsEnabled) return;

                mOplusQsMediaView = (ViewGroup) param.thisObject;
                mDefaultMediaBg = mOplusQsMediaView.getBackground();
                mOplusQsMediaView.setBackground(null);
                mOplusQsMediaView.removeAllViews();

                placeWidgets();
            }
        });


    }

    private void placeWidgets() {
        try {
            QsControlsView qsControlsView = QsControlsView.getInstance(mContext);
            try {
                ((ViewGroup) qsControlsView.getParent()).removeView(qsControlsView);
            } catch (Throwable ignored) {
            }
            mOplusQsMediaView.addView(qsControlsView, 0);
            qsControlsView.bringToFront();
            qsControlsView.requestLayout();
            updateWidgets();
            updateMediaPlayerPrefs();
            updateControlsBg();
            updatePhotoRadius();
        } catch (Throwable t) {
            log(TAG + "Error: " + t.getMessage());
        }
    }

    private void updateWidgets() {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        if (qsControlsView != null) {
            qsControlsView.updateWidgets(mQsWidgetsList);
        }
    }

    private void updateMediaPlayerPrefs() {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        if (qsControlsView != null) {
            qsControlsView.updateMediaPlayerPrefs(showMediaArtMediaQs, mMediaQsArtFilter, mMediaQsTintColor, mMediaQsTintAmount, mMediaQsArtBlurAmount);
        }
    }

    private void updateControlsBg() {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        if (qsControlsView != null) {
            qsControlsView.updateControlsBg(mDefaultMediaBg, qsInactiveColorEnabled, qsInactiveColor);
        }
    }

    private void updatePhotoRadius() {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        log(TAG + "updatePhotoRadius: " + mQsPhotoRadius);
        if (qsControlsView != null) {
            qsControlsView.updatePhotoRadius(mQsPhotoRadius);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
