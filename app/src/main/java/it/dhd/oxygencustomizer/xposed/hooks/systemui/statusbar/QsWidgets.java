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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.WidgetUtils;
import it.dhd.oxygencustomizer.xposed.views.BatteryBarView;
import it.dhd.oxygencustomizer.xposed.views.QsControlsView;

public class QsWidgets extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private static final String TAG = "QsWidgets: ";

    private ViewGroup mOplusQsMediaView = null;
    private View mMediaPlayer;
    private ViewGroup mQsMediaPanelContainer;

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
            OplusQSTileMediaContainer = findClass("com.oplusos.systemui.qs.OplusQSContainerImpl", lpparam.classLoader);
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

//        hookAllMethods(OplusQSTileMediaContainer, "onFinishInflate", new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                log(TAG + "onFinishInflate");
//
//                mQsMediaPanelContainer = (ViewGroup) param.thisObject;
//                mQsMediaPanelContainer.addView(mWidgetsContainer);
//
//                //(ViewGroup) getObjectField(param.thisObject, "mQsMediaPanelContainer");
//                //mOplusQsMediaView.removeAllViews();
//                /*
//                <LinearLayout
//                    android:id="@+id/media_container"
//                    android:layout_width="0dp"
//                    android:layout_height="@dimen/oplus_qs_media_panel_height"
//                    android:layout_marginBottom="@dimen/qs_footer_hl_tile_two_container_margin_top"
//                    android:layout_marginEnd="@dimen/qs_footer_hl_tile_side_margin"
//                    app:layout_constraintEnd_toStartOf="@+id/guide_line"
//                    app:layout_constraintStart_toStartOf="0"
//                    app:layout_constraintTop_toTopOf="0">
//                    <include
//                        android:layout_width="match_parent"
//                        android:layout_height="match_parent"
//                        layout="@layout/oplus_qs_media_panel"/>
//                </LinearLayout>
//
//                 */
//                placeWidgets();
//            }
//        });
//
//        hookAllMethods(OplusQSTileMediaContainer, "updateViewState", new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                log(TAG + "setMediaPanelVisibility");
//                if (mWidgetsContainer == null) return;
//
//                float f = (float) param.args[0];
//                float f2 = (float) param.args[1];
//
//                mWidgetsContainer.setPivotX(mWidgetsContainer != null ? mWidgetsContainer.getWidth() : 0.0f);
//                mWidgetsContainer.setPivotY(mWidgetsContainer != null ? mWidgetsContainer.getHeight() / 2 : 0.0f);
//                mWidgetsContainer.setScaleX(f2);
//                mWidgetsContainer.setScaleY(f2);
//                mWidgetsContainer.setTransitionAlpha(f);
//
//            }
//        });
//
//        hookAllMethods(OplusQSTileMediaContainer, "updateQsSeekbarLayout", new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                log(TAG + "updateQsSeekbarLayout");
//                if (mWidgetsContainer == null) return;
//
//                int i = (int) param.args[0];
//                boolean z = (boolean) param.args[1];
//
//
//                Object mQsSeekBarContainer = getObjectField(param.thisObject, "mQsSeekBarContainer");
//                Object mTmpConstraintSet = getObjectField(param.thisObject, "mTmpConstraintSet");
//                Object mSecondTileContainer = getObjectField(param.thisObject, "mSecondTileContainer");
//                Object mFirstTileContainer = getObjectField(param.thisObject, "mFirstTileContainer");
//
//                    callMethod(mTmpConstraintSet, "connect",
//                        callMethod(mQsSeekBarContainer, "getId"), 3,
//                        mWidgetsContainer.getId(), 3, 0);
//                callMethod(mTmpConstraintSet, "connect",
//                        callMethod(mQsSeekBarContainer, "getId"), 4,
//                        mWidgetsContainer.getId(), 4, 0);
//                callMethod(mTmpConstraintSet, "connect",
//                        callMethod(mQsSeekBarContainer, "getId"), 6,
//                        mWidgetsContainer.getId(), 7, 0);
//
//                callMethod(mTmpConstraintSet, "connect",
//                        callMethod(mQsSeekBarContainer, "getId"), 3,
//                        callMethod(mFirstTileContainer, "getId"), 3, 0);
//                callMethod(mTmpConstraintSet, "connect",
//                        callMethod(mQsSeekBarContainer, "getId"), 4,
//                        callMethod(mSecondTileContainer, "getId"), 4, 0);
//                callMethod(mTmpConstraintSet, "connect",
//                        callMethod(mQsSeekBarContainer, "getId"), 6,
//                        mContext.getResources().getIdentifier("guide_line", "id", listenPackage), 7, i);
//                callMethod(mTmpConstraintSet, "connect",
//                        callMethod(mQsSeekBarContainer, "getId"), 7,
//                        0, 7, 0);
//            }
//        });

//        Class<?> OplusQsMediaControllerExImpl = findClass("com.oplus.systemui.qs.media.OplusQsMediaControllerExImpl", lpparam.classLoader);
//        hookAllMethods(OplusQsMediaControllerExImpl, "initMediaPanelViewController", new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                log(TAG + "initMediaPanelViewController");
//                mOplusQsMediaView = (ViewGroup) param.args[0];
//                try {
//                    mMediaPlayer = mOplusQsMediaView.findViewById(
//                            mContext.getResources().getIdentifier(
//                                    "oplus_qs_media_panel",
//                                    "id",
//                                    listenPackage
//                            )
//                    );
//                    log(TAG + "MediaPlayer: " + (mMediaPlayer != null));
//                    placeWidgets();
//                } catch (Throwable t) {
//                    log(TAG + "Error: " + t.getMessage());
//                }
//            }
//        });

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
        log(TAG + "updateWidgets: " + mQsWidgetsList);
        if (qsControlsView != null) {
            qsControlsView.updateWidgets(mQsWidgetsList);
        }
    }

    private void updateMediaPlayerPrefs() {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        log(TAG + "updateMediaPlayerPrefs: " + mMediaPlayer);
        if (qsControlsView != null) {
            qsControlsView.updateMediaPlayerPrefs(showMediaArtMediaQs, mMediaQsArtFilter, mMediaQsTintColor, mMediaQsTintAmount, mMediaQsArtBlurAmount);
        }
    }

    private void updateControlsBg() {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        log(TAG + "updateMediaColors: " + mMediaPlayer);
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
