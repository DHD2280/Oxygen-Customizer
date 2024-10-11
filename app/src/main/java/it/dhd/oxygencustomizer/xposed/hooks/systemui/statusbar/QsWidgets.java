package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_BLUR_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_FILTER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_TINT_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_TINT_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_SHOW_ALBUM_ART;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_BOTTOM_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_BOTTOM_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_TOP_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_TOP_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_PHOTO_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_WIDGETS_LIST;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_WIDGETS_SWITCH;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

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
    public static Object mActivityStarter = null;
    private ViewGroup mOplusQsMediaView = null;
    private boolean mQsWidgetsEnabled = false;
    private String mQsWidgetsList = "media";
    // Photo Showcase
    private int mQsPhotoRadius = 22;
    // Qs Media Tile colors
    private Drawable mDefaultMediaBg = null;
    private boolean qsInactiveColorEnabled = false, qsActiveColorEnabled = false;
    private int qsInactiveColor = Color.GRAY, qsActiveColor = getPrimaryColor(mContext);
    // Qs Tile Radius
    private boolean customHighlightTileRadius = false, customTileRadius = false;
    private int highlightTSRadius, highlightTDRadius, highlightBSRadius, highlightBDRadius;
    private int tileTSRadius, tileTDRadius, tileBSRadius, tileBDRadius;
    // Qs Media Tile Album Art
    private boolean showMediaArtMediaQs = false;
    private int mMediaQsArtFilter = 0, mMediaQsTintColor = Color.WHITE, mMediaQsTintAmount = 20;
    private float mMediaQsArtBlurAmount = 7.5f;
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
        showMediaArtMediaQs = Xprefs.getBoolean(QS_MEDIA_SHOW_ALBUM_ART, false);
        mMediaQsArtFilter = Integer.parseInt(Xprefs.getString(QS_MEDIA_ART_FILTER, "0"));
        mMediaQsArtBlurAmount = (Xprefs.getSliderInt(QS_MEDIA_ART_BLUR_AMOUNT, 30) / 100f) * 25f;
        mMediaQsTintColor = Xprefs.getInt(QS_MEDIA_ART_TINT_COLOR, Color.WHITE);
        mMediaQsTintAmount = Xprefs.getSliderInt(QS_MEDIA_ART_TINT_AMOUNT, 20);

        // Tile Colors
        qsActiveColorEnabled = Xprefs.getBoolean(QS_TILE_ACTIVE_COLOR_ENABLED, false);
        qsActiveColor = Xprefs.getInt(QS_TILE_ACTIVE_COLOR, getPrimaryColor(mContext));
        qsInactiveColorEnabled = Xprefs.getBoolean(QS_TILE_INACTIVE_COLOR_ENABLED, false);
        qsInactiveColor = Xprefs.getInt(QS_TILE_INACTIVE_COLOR, Color.GRAY);

        // Qs Radius
        customHighlightTileRadius = Xprefs.getBoolean(QS_TILE_HIGHTLIGHT_RADIUS, false);
        highlightTSRadius = Xprefs.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT, 0);
        highlightTDRadius = Xprefs.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT, 0);
        highlightBSRadius = Xprefs.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT, 0);
        highlightBDRadius = Xprefs.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT, 0);
        customTileRadius = Xprefs.getBoolean(QS_TILE_RADIUS, false);
        tileTSRadius = Xprefs.getSliderInt(QS_TILE_RADIUS_TOP_LEFT, 0);
        tileTDRadius = Xprefs.getSliderInt(QS_TILE_RADIUS_TOP_RIGHT, 0);
        tileBSRadius = Xprefs.getSliderInt(QS_TILE_RADIUS_BOTTOM_LEFT, 0);
        tileBDRadius = Xprefs.getSliderInt(QS_TILE_RADIUS_BOTTOM_RIGHT, 0);

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
                    Key[0].equals(QS_TILE_INACTIVE_COLOR_ENABLED) ||
                    Key[0].equals(QS_TILE_ACTIVE_COLOR) ||
                    Key[0].equals(QS_TILE_ACTIVE_COLOR_ENABLED)) {
                updateTileColors(true);
            }
            if (Key[0].equals(QS_TILE_HIGHTLIGHT_RADIUS) ||
                    Key[0].equals(QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT) ||
                    Key[0].equals(QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT) ||
                    Key[0].equals(QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT) ||
                    Key[0].equals(QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT) ||
                    Key[0].equals(QS_TILE_RADIUS) ||
                    Key[0].equals(QS_TILE_RADIUS_TOP_LEFT) ||
                    Key[0].equals(QS_TILE_RADIUS_TOP_RIGHT) ||
                    Key[0].equals(QS_TILE_RADIUS_BOTTOM_LEFT) ||
                    Key[0].equals(QS_TILE_RADIUS_BOTTOM_RIGHT)) {
                updateTileShapes(true);
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        log("Hooking OplusQSTileMediaContainer");

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
            forceMediaPanelA13(lpparam);
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

    private void forceMediaPanelA13(XC_LoadPackage.LoadPackageParam lpparam) {
        // Classes
        Class<?> OplusQSFooterImpl = findClass("com.oplusos.systemui.qs.OplusQSFooterImpl", lpparam.classLoader); //1
        Class<?> OplusQSContainerImpl = findClass("com.oplusos.systemui.qs.OplusQSContainerImpl", lpparam.classLoader); //2
        Class<?> QuickStatusBarHeader = findClass("com.android.systemui.qs.QuickStatusBarHeader", lpparam.classLoader);

        // Hooks
        XC_MethodHook boolHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!mQsWidgetsEnabled) return;
                try {
                    setBooleanField(param.thisObject, "mIsMediaMode", true);
                } catch (Throwable t) {
                    log("No boolean field mIsMediaMode: " + t.getMessage());
                }
            }
        };

        XC_MethodHook methodHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!mQsWidgetsEnabled) return;
                param.args[0] = true;
            }
        };

        // Constructors
        hookAllConstructors(OplusQSFooterImpl, boolHook);
        hookAllConstructors(OplusQSContainerImpl, boolHook);
        hookAllConstructors(QuickStatusBarHeader, boolHook);

        // Methods
        hookAllMethods(OplusQSContainerImpl, "setQsMediaPanelShown", methodHook);
        hookAllMethods(OplusQSFooterImpl, "setQsMediaPanelShown", methodHook);
        hookAllMethods(OplusQSFooterImpl, "setMediaMode", methodHook);
        hookAllMethods(QuickStatusBarHeader, "setQsMediaPanelShown", methodHook);
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
            updateControlsBg();
            updateTileColors(false);
            updateTileShapes(false);
            updateWidgets();
            updateMediaPlayerPrefs();
            updatePhotoRadius();
        } catch (Throwable t) {
            log("Error: " + t.getMessage());
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
            qsControlsView.updateDefaultMediaBg(mDefaultMediaBg);
        }
    }

    private void updateTileColors(boolean force) {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        if (qsControlsView != null) {
            qsControlsView.updateQsTileColors(qsInactiveColorEnabled, qsInactiveColor, qsActiveColorEnabled, qsActiveColor, force);
        }
    }

    private void updateTileShapes(boolean force) {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        if (qsControlsView != null) {
            qsControlsView.updateTileShapes(customHighlightTileRadius,
                    new float[]{
                            dp2px(mContext, highlightTSRadius),
                            dp2px(mContext, highlightTSRadius),
                            dp2px(mContext, highlightTDRadius),
                            dp2px(mContext, highlightTDRadius),
                            dp2px(mContext, highlightBDRadius),
                            dp2px(mContext, highlightBDRadius),
                            dp2px(mContext, highlightBSRadius),
                            dp2px(mContext, highlightBSRadius)},
                    customTileRadius,
                    new float[]{
                            dp2px(mContext, tileTSRadius),
                            dp2px(mContext, tileTSRadius),
                            dp2px(mContext, tileTDRadius),
                            dp2px(mContext, tileTDRadius),
                            dp2px(mContext, tileBDRadius),
                            dp2px(mContext, tileBDRadius),
                            dp2px(mContext, tileBSRadius),
                            dp2px(mContext, tileBSRadius)},
                    force);
        }
    }

    private void updatePhotoRadius() {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        log("updatePhotoRadius: " + mQsPhotoRadius);
        if (qsControlsView != null) {
            qsControlsView.updatePhotoRadius(mQsPhotoRadius);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
