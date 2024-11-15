package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_BRIGHTNESS_SLIDER_BACKGROUND_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_BRIGHTNESS_SLIDER_BACKGROUND_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_BRIGHTNESS_SLIDER_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_BRIGHTNESS_SLIDER_COLOR_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_BRIGHTNESS_SLIDER_CUSTOMIZE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_BLUR_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_FILTER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_TINT_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_TINT_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_SHOW_ALBUM_ART;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_DURATION;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_INTERPOLATOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_TRANSFORMATIONS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_TRANSFORMATIONS_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_DISABLED_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_DISABLED_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIDE_LABELS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_LABELS_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_LABELS_CUSTOM_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_BOTTOM_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_BOTTOM_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_TOP_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_TOP_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_UPDATE_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_WIDGETS_SWITCH;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getArt;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ReflectionTools.findClassInArray;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.ViewPager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.AccordionTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.BackgroundToForegroundTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.CubeInTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.CubeOutTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.DepthPageTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.FadeTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.ForegroundToBackgroundTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.RaiseFromCenterTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.RotateAboutBottomTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.RotateDownTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.RotateUpTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.StackTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.TabletTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.TranslationYTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.ZoomInTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.ZoomOutSlideTransformer;
import it.dhd.oxygencustomizer.xposed.utils.viewpager.ZoomOutTransformer;

public class QsTileCustomization extends XposedMods {

    private static final String listenerPackage = Constants.Packages.SYSTEM_UI;
    private final int STATE_ACTIVE = 2;
    private final int STATE_INACTIVE = 1;
    private Object mPersonalityManager = null;

    // Qs Tile Colors
    private int qsInactiveColor, qsActiveColor, qsDisabledColor;
    private boolean qsInactiveColorEnabled = false, qsActiveColorEnabled = false, qsDisabledColorEnabled = false;

    // Qs Tile Radius
    private boolean customHighlightTileRadius = false, customTileRadius = false;
    private int highlightTSRadius, highlightTDRadius, highlightBSRadius, highlightBDRadius;
    private int tileTSRadius, tileTDRadius, tileBSRadius, tileBDRadius;

    // Qs Tile Label Utils
    private boolean qsLabelsHide, qsLabelsColorEnabled;
    private int qsLabelsColor;

    // Brightness Slider
    private boolean qsBrightnessSliderCustomize, qsBrightnessBackgroundCustomize;
    private int qsBrightnessSliderColorMode, qsBrightnessSliderColor, qsBrightnessBackgroundColor;

    // QS Media Tile
    private ImageView mCoverImg = null;
    private View mOplusQsMediaView = null;
    private Drawable mOplusQsMediaDefaultBackground = null;
    private Drawable mOplusQsMediaDrawable = null;
    private ViewGroup mLabelContainer = null;
    private TextView mTitle = null, mSubtitle = null;
    private ImageView mExpandIndicator = null;

    // Qs Tile Animation
    private int mAnimStyle = 0;
    private int mInterpolatorType = 0;
    private int mAnimDuration = 0;
    private boolean mTrasformationsEnabled = false;
    private int mTrasformations = 1;

    // Qs Media Tile Album Art
    private boolean mQsWidgetsEnabled = false;
    private boolean showMediaArtMediaQs = false;
    private int mMediaQsArtFilter = 0, mMediaQsTintColor = Color.WHITE, mMediaQsTintAmount = 20;
    private float mMediaQsArtBlurAmount = 7.5f;
    private Bitmap mArt = null;
    private int mColorOnAlbum = Color.WHITE;

    private Class<?> QsColorUtil = null;

    public QsTileCustomization(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        // Qs Colors
        qsActiveColorEnabled = Xprefs.getBoolean(QS_TILE_ACTIVE_COLOR_ENABLED, false);
        qsActiveColor = Xprefs.getInt(QS_TILE_ACTIVE_COLOR, Color.RED);
        qsInactiveColorEnabled = Xprefs.getBoolean(QS_TILE_INACTIVE_COLOR_ENABLED, false);
        qsInactiveColor = Xprefs.getInt(QS_TILE_INACTIVE_COLOR, Color.GRAY);
        qsDisabledColorEnabled = Xprefs.getBoolean(QS_TILE_DISABLED_COLOR_ENABLED, false);
        qsDisabledColor = Xprefs.getInt(QS_TILE_DISABLED_COLOR, Color.DKGRAY);

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

        // Media QS
        mQsWidgetsEnabled = Xprefs.getBoolean(QS_WIDGETS_SWITCH, false);
        showMediaArtMediaQs = Xprefs.getBoolean(QS_MEDIA_SHOW_ALBUM_ART, false);
        mMediaQsArtFilter = Integer.parseInt(Xprefs.getString(QS_MEDIA_ART_FILTER, "0"));
        mMediaQsArtBlurAmount = (Xprefs.getSliderInt(QS_MEDIA_ART_BLUR_AMOUNT, 30) / 100f) * 25f;
        mMediaQsTintColor = Xprefs.getInt(QS_MEDIA_ART_TINT_COLOR, Color.WHITE);
        mMediaQsTintAmount = Xprefs.getSliderInt(QS_MEDIA_ART_TINT_AMOUNT, 20);

        // Brightness Slider
        qsBrightnessSliderCustomize = Xprefs.getBoolean(QS_BRIGHTNESS_SLIDER_CUSTOMIZE, false);
        qsBrightnessSliderColorMode = Integer.parseInt(Xprefs.getString(QS_BRIGHTNESS_SLIDER_COLOR_MODE, "0"));
        qsBrightnessSliderColor = Xprefs.getInt(QS_BRIGHTNESS_SLIDER_COLOR, getPrimaryColor(mContext));
        qsBrightnessBackgroundCustomize = Xprefs.getBoolean(QS_BRIGHTNESS_SLIDER_BACKGROUND_ENABLED, false);
        qsBrightnessBackgroundColor = Xprefs.getInt(QS_BRIGHTNESS_SLIDER_BACKGROUND_COLOR, Color.TRANSPARENT);

        // Labels
        qsLabelsHide = Xprefs.getBoolean(QS_TILE_HIDE_LABELS, false);
        qsLabelsColorEnabled = Xprefs.getBoolean(QS_TILE_LABELS_CUSTOM_COLOR_ENABLED, false);
        qsLabelsColor = Xprefs.getInt(QS_TILE_LABELS_CUSTOM_COLOR, Color.WHITE);

        // Qs Animations
        mAnimStyle = Integer.parseInt(Xprefs.getString(QS_TILE_ANIMATION_STYLE, "0"));
        mInterpolatorType = Integer.parseInt(Xprefs.getString(QS_TILE_ANIMATION_INTERPOLATOR, "0"));
        mAnimDuration = Xprefs.getSliderInt(QS_TILE_ANIMATION_DURATION, 1);
        mTrasformationsEnabled = Xprefs.getBoolean(QS_TILE_ANIMATION_TRANSFORMATIONS_SWITCH, false);
        mTrasformations = Integer.parseInt(Xprefs.getString(QS_TILE_ANIMATION_TRANSFORMATIONS, "1"));

        if (Key.length > 0) {
            for (String k : QS_UPDATE_PREFS) {
                if (Key[0].equals(k)) {
                    if (Key[0].equals(QS_TILE_INACTIVE_COLOR_ENABLED) || Key[0].equals(QS_TILE_INACTIVE_COLOR)) {
                        updateMediaQs();
                    }
                    notifyQsUpdate();
                }
            }
            if (Key[0].equals(QS_MEDIA_SHOW_ALBUM_ART) ||
                    Key[0].equals(QS_MEDIA_ART_FILTER) ||
                    Key[0].equals(QS_MEDIA_ART_BLUR_AMOUNT) ||
                    Key[0].equals(QS_MEDIA_ART_TINT_COLOR) ||
                    Key[0].equals(QS_MEDIA_ART_TINT_AMOUNT)) {
                if (showMediaArtMediaQs) updateMediaQsBackground();
                else updateMediaQs();
            }
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenerPackage)) return;

        try {
            Class<?> PersonalityManager;
            try {
                PersonalityManager = findClass("com.oplus.systemui.qs.personality.PersonalityManager", lpparam.classLoader);
            } catch (Throwable ignored) {
                PersonalityManager = findClass("com.oplusos.systemui.qs.personality.PersonalityManager", lpparam.classLoader);
            }
            hookAllConstructors(PersonalityManager, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mPersonalityManager = param.thisObject;
                }
            });
        } catch (Throwable t) {
            log("PersonalityManager error: " + t.getMessage());
        }

        try {
            QsColorUtil = findClassIfExists("com.oplus.systemui.qs.util.QsColorUtil", lpparam.classLoader);
        } catch (Throwable ignored) {
        }

        Class<?> OplusQSTileBaseView = findClassInArray(lpparam,
                "com.oplus.systemui.qs.base.tile.OplusQSTileBaseView" /* OOS15 */,
                "com.oplus.systemui.qs.qstileimpl.OplusQSTileBaseView" /* OOS14 */,
                "com.oplusos.systemui.qs.qstileimpl.OplusQSTileBaseView" /* OOS13 */);
        if (OplusQSTileBaseView == null) {
            log(new Throwable("OplusQSTileBaseView not found"));
        }

        /*if (QsColorUtil != null) {
            setStaticIntField(QsColorUtil, "BRIGHTNESS_ICON_BG_LIGHT_COLOR", Color.WHITE);
            setStaticIntField(QsColorUtil, "BRIGHTNESS_ICON_BG_DARK_COLOR", Color.WHITE);
        }*/
        /*hookAllMethods(QsColorUtil, "isLightColor", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                setStaticIntField(QsColorUtil, "BRIGHTNESS_ICON_BG_LIGHT_COLOR", Color.WHITE);
                setStaticIntField(QsColorUtil, "BRIGHTNESS_ICON_BG_DARK_COLOR", Color.WHITE);

            }
        });*/

        final XC_MethodHook colorHook = new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int state = (int) param.args[0];
                ShapeDrawable mPersonalityDrawable = (ShapeDrawable) param.getResult();

                Shape mPersonalityDrawable2 = new RoundRectShape(
                        new float[]{
                                dp2px(mContext, 10), dp2px(mContext, 10),
                                dp2px(mContext, 28), dp2px(mContext, 28),
                                dp2px(mContext, 10), dp2px(mContext, 10),
                                dp2px(mContext, 28), dp2px(mContext, 28)}, null, null);
                mPersonalityDrawable.setShape(mPersonalityDrawable2);
                if (state == STATE_INACTIVE && qsInactiveColorEnabled) // Inactive State
                {
                    mPersonalityDrawable.getPaint().setColor(qsInactiveColor);
                } else if (state == STATE_ACTIVE && qsActiveColorEnabled) // Active State
                {
                    mPersonalityDrawable.getPaint().setColor(qsActiveColor);
                } else if (qsDisabledColorEnabled && state != 1 && state != 2) // Disabled State
                {
                    mPersonalityDrawable.getPaint().setColor(qsDisabledColor);
                }
                if (qsInactiveColorEnabled || qsActiveColorEnabled || qsDisabledColorEnabled)
                    mPersonalityDrawable.invalidateSelf();
            }
        };

        final XC_MethodHook animationHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View qsTile = (View) param.thisObject;
                qsTile.post(() -> getTileAnimation(qsTile));
            }
        };

        hookAllMethods(OplusQSTileBaseView, "generateDrawable", getColorHook(false));

        hookAllMethods(OplusQSTileBaseView, "performClick", animationHook);

        Class<?> OplusQSHighlightTileView = findClassInArray(lpparam,
                "com.oplus.systemui.qs.base.tile.OplusQSHighlightTileView" /* OOS15 */,
                "com.oplus.systemui.qs.qstileimpl.OplusQSHighlightTileView" /* OOS14 */,
                "com.oplusos.systemui.qs.qstileimpl.OplusQSHighlightTileView" /* OOS13 */);
        if (OplusQSHighlightTileView == null) {
            log(new Throwable("OplusQSHighlightTileView not found"));
        }
        hookAllMethods(OplusQSHighlightTileView, "generateDrawable", getColorHook(true));
        hookAllMethods(OplusQSHighlightTileView, "performClick", animationHook);


        Class<?> OplusQsMediaPanelView = findClass("com.oplus.systemui.qs.media.OplusQsMediaPanelView", lpparam.classLoader);
        hookAllMethods(OplusQsMediaPanelView, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mOplusQsMediaView = (View) param.thisObject;
                if (mQsWidgetsEnabled) return;
                mOplusQsMediaDefaultBackground = mOplusQsMediaView.getBackground();
                if (mOplusQsMediaDefaultBackground != null) {
                    mOplusQsMediaDrawable = mOplusQsMediaDefaultBackground.getConstantState().newDrawable().mutate();
                }
                if (qsInactiveColorEnabled) {
                    mOplusQsMediaDrawable.setTint(qsInactiveColor);
                    mOplusQsMediaDrawable.invalidateSelf();
                    mOplusQsMediaView.setBackground(mOplusQsMediaDrawable);
                } else mOplusQsMediaView.setBackground(mOplusQsMediaDefaultBackground);

                // Get OOS15 cover
                if (Build.VERSION.SDK_INT >= 35) {
                    mCoverImg = (ImageView) getObjectField(param.thisObject, "mCoverImg");
                }

                // Listen for default tip change
                View mDefaultTip = (View) getObjectField(param.thisObject, "mDefaultTip");
                mDefaultTip.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    if (v.getVisibility() == View.VISIBLE) {
                        hideMediaQsBackground();
                    }
                });
            }
        });

        hookAllMethods(OplusQsMediaPanelView, "bindTitleAndText", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                updateMediaQsBackground();
            }
        });

        Class<?> OplusQSTileView = findClassInArray(lpparam,
                "com.oplus.systemui.plugins.qs.tile.OplusQSTileView" /* OOS15 */,
                "com.oplus.systemui.qs.qstileimpl.OplusQSTileView" /* OOS14 */,
                "com.oplusos.systemui.qs.qstileimpl.OplusQSTileView" /* OOS13 */);
        if (OplusQSTileView == null) {
            log(new Throwable("OplusQSTileView not found"));
        }
        hookAllMethods(OplusQSTileView, "createLabel", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                mLabelContainer = (ViewGroup) getObjectField(param.thisObject, "mLabelContainer");
                mTitle = (TextView) getObjectField(param.thisObject, "mLabel");
                mSubtitle = (TextView) getObjectField(param.thisObject, "mSecondLine");
                mExpandIndicator = (ImageView) getObjectField(param.thisObject, "mExpandIndicator");
                setupLabels();
            }
        });

        hookAllMethods(OplusQSTileView, "updateTextColor", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mLabelContainer = (ViewGroup) getObjectField(param.thisObject, "mLabelContainer");
                mTitle = (TextView) getObjectField(param.thisObject, "mLabel");
                mSubtitle = (TextView) getObjectField(param.thisObject, "mSecondLine");
                mExpandIndicator = (ImageView) getObjectField(param.thisObject, "mExpandIndicator");
                setupLabels();
            }
        });

        hookAllMethods(OplusQSTileView, "handleStateChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mLabelContainer = (ViewGroup) getObjectField(param.thisObject, "mLabelContainer");
                mTitle = (TextView) getObjectField(param.thisObject, "mLabel");
                mSubtitle = (TextView) getObjectField(param.thisObject, "mSecondLine");
                mExpandIndicator = (ImageView) getObjectField(param.thisObject, "mExpandIndicator");
                setupLabels();
            }
        });

        Class<?> OplusToggleSliderView;
        try {
            OplusToggleSliderView = findClass("com.oplus.systemui.qs.widget.OplusToggleSliderView", lpparam.classLoader);
        } catch (Throwable ignored) {
            OplusToggleSliderView = findClass("com.oplusos.systemui.qs.widget.OplusToggleSliderView", lpparam.classLoader);
        }

        findAndHookMethod(OplusToggleSliderView, "onShapeChanged",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        if (!qsBrightnessSliderCustomize) return;

                        if (qsBrightnessSliderColorMode == 1) {
                            setSliderProgressColor(getObjectField(param.thisObject, "mSlider"), ColorStateList.valueOf(getPrimaryColor(mContext)));
                        } else if (qsBrightnessSliderColorMode == 2) {
                            setSliderProgressColor(getObjectField(param.thisObject, "mSlider"), ColorStateList.valueOf(qsBrightnessSliderColor));
                        }

                        if (qsBrightnessBackgroundCustomize) {
                            setSliderBackgroundColor(getObjectField(param.thisObject, "mSlider"), ColorStateList.valueOf(qsBrightnessBackgroundColor));
                        } else {
                            int color = ResourcesCompat.getColor(mContext.getResources(), mContext.getResources().getIdentifier("status_bar_qs_brightness_slider_bg_color", "color", lpparam.packageName), mContext.getTheme());
                            if (color != 0x0) {
                                setSliderBackgroundColor(getObjectField(param.thisObject, "mSlider"), ColorStateList.valueOf(color));
                            }
                        }
                    }
                });

        hookAllMethods(OplusToggleSliderView, "setupSliderProgressDrawable", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!qsBrightnessSliderCustomize) return;

                int colorToApply = getPrimaryColor(mContext);
                if (qsBrightnessSliderColorMode == 2) {
                    colorToApply = qsBrightnessSliderColor;
                }

                setSliderProgressColor(getObjectField(param.thisObject, "mSlider"), ColorStateList.valueOf(colorToApply));
                if (getBooleanField(param.thisObject, "mIsMirror")) {
                    try {
                        callMethod(getObjectField(param.thisObject, "mSlider"), "setThumbColor", ColorStateList.valueOf(colorToApply));
                    } catch (Throwable ignored) {
                        callMethod(getObjectField(param.thisObject, "mSlider"), "setThumbTintList", ColorStateList.valueOf(colorToApply));
                    }
                }
                if (qsBrightnessBackgroundCustomize) {
                    setSliderBackgroundColor(getObjectField(param.thisObject, "mSlider"), ColorStateList.valueOf(qsBrightnessBackgroundColor));
                } else {
                    int color = ResourcesCompat.getColor(mContext.getResources(), mContext.getResources().getIdentifier("status_bar_qs_brightness_slider_bg_color", "color", lpparam.packageName), mContext.getTheme());
                    if (color != 0x0) {
                        setSliderBackgroundColor(getObjectField(param.thisObject, "mSlider"), ColorStateList.valueOf(color));
                    }
                }
            }
        });

        final XC_MethodHook newUiHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!qsBrightnessSliderCustomize) return;

                Object slider = getObjectField(param.thisObject, "slider");

                int colorToApply = getPrimaryColor(mContext);
                if (qsBrightnessSliderColorMode == 2) {
                    colorToApply = qsBrightnessSliderColor;
                }
                callMethod(slider, "setProgressColor", ColorStateList.valueOf(colorToApply));

                if (qsBrightnessBackgroundCustomize) {
                    callMethod(slider, "setSeekBarBackgroundColor", ColorStateList.valueOf(qsBrightnessBackgroundColor));
                } else {
                    int color = ResourcesCompat.getColor(mContext.getResources(), mContext.getResources().getIdentifier("status_bar_qs_brightness_slider_bg_color", "color", lpparam.packageName), mContext.getTheme());
                    callMethod(slider, "setSeekBarBackgroundColor", ColorStateList.valueOf(color));
                }
            }
        };

        try {
            Class<?> OplusQsToggleSliderLayout = findClass("com.oplus.systemui.qs.widget.OplusQsToggleSliderLayout", lpparam.classLoader);
            hookAllConstructors(OplusQsToggleSliderLayout, newUiHook);
            findAndHookMethod(OplusQsToggleSliderLayout,
                    "onShapeChanged", int.class,
                    newUiHook
            );

        } catch (Throwable ignored) {
        }


        try {
            Class<?> PagedTileLayout = findClass("com.android.systemui.qs.PagedTileLayout", lpparam.classLoader);
            hookAllConstructors(PagedTileLayout, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    Object VPagerListener = getObjectField(param.thisObject, "mOnPageChangeListener");
                    Object vPager = param.thisObject;
                    hookAllMethods(VPagerListener.getClass(),
                            "onPageScrolled", new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    if (!mTrasformationsEnabled) return;
                                    final int childCount = (int) callMethod(vPager, "getChildCount");
                                    for (int i = 0; i < childCount; i++) {
                                        final View child = (View) callMethod(vPager, "getChildAt", i);
                                        final Object lp = callMethod(child, "getLayoutParams");
                                        if (getBooleanField(lp, "isDecor")) continue;
                                        final float transformPos = (float) (child.getLeft() - (int) callMethod(vPager, "getScrollX")) / child.getWidth();
                                        getCustomTransitions().transformPage(child, transformPos);
                                    }
                                }
                            });
                }
            });
        } catch (Throwable t) {
            log(this.getClass().getSimpleName() + " error: " + t.getMessage());
        }

    }

    private void setSliderProgressColor(Object mSlider, ColorStateList colorStateList) {
        try {
            callMethod(mSlider, "setProgressColor", colorStateList);
        } catch (Throwable ignored) {
            callMethod(mSlider, "setProgressTintList", colorStateList);
        }
    }

    private void setSliderBackgroundColor(Object mSlider, ColorStateList colorStateList) {
        try {
            callMethod(mSlider, "setSeekBarBackgroundColor", colorStateList);
        } catch (Throwable ignored) {
            callMethod(mSlider, "setProgressBackgroundTintList", colorStateList);
        }
    }

    private void updateMediaQsBackground() {
        if (!showMediaArtMediaQs || mOplusQsMediaView == null) return;
        if (mQsWidgetsEnabled) return;
        mCoverImg.setVisibility(View.GONE);
        Bitmap oldArt = mArt;
        Bitmap tempArt = getArt();
        if (tempArt == null) {
            hideMediaQsBackground();
            return;
        }
        mArt = getFilteredArt(tempArt);
        float radius = 0f;
        try {
            GradientDrawable defBg = (GradientDrawable) mOplusQsMediaDefaultBackground;
            radius = defBg.getCornerRadius();
        } catch (Throwable t) {
            log("Oxygen Customizer - QsTileCustomization error: " + t.getMessage());
        }
        Bitmap artRounded = DrawableConverter.getRoundedCornerBitmap(mArt, radius);
        Bitmap oldArtRounded = DrawableConverter.getRoundedCornerBitmap(oldArt, radius);
        Palette.Builder builder = new Palette.Builder(artRounded);
        builder.generate(palette -> {
            int dominantColor = palette.getDominantColor(Color.WHITE);
            mColorOnAlbum =
                    isColorDark(dominantColor) ?
                            DrawableConverter.findContrastColorAgainstDark(Color.WHITE, dominantColor, true, 2) :
                            DrawableConverter.findContrastColor(Color.BLACK, dominantColor, true, 2);
            mOplusQsMediaView.post(() -> {
                setupOtherViews(mOplusQsMediaView, mColorOnAlbum);
            });
        });

        mOplusQsMediaView.post(() -> {
            Drawable[] layers = new Drawable[]{new BitmapDrawable(mContext.getResources(), oldArtRounded), new BitmapDrawable(mContext.getResources(), artRounded)};
            TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
            mOplusQsMediaView.setBackground(transitionDrawable);
            transitionDrawable.startTransition(250);
        });
    }

    private void hideMediaQsBackground() {
        if (mOplusQsMediaView == null) return;
        if (mQsWidgetsEnabled) return;
        mOplusQsMediaView.setBackground(qsInactiveColorEnabled ? mOplusQsMediaDrawable : mOplusQsMediaDefaultBackground);
    }

    private Bitmap getFilteredArt(Bitmap art) {
        Bitmap finalArt;
        switch (mMediaQsArtFilter) {
            case 1 -> finalArt = DrawableConverter.toGrayscale(art);
            case 2 ->
                    finalArt = DrawableConverter.getColoredBitmap(new BitmapDrawable(mContext.getResources(), art),
                            getPrimaryColor(mContext));
            case 3 ->
                    finalArt = DrawableConverter.getBlurredImage(mContext, art, mMediaQsArtBlurAmount);
            case 4 ->
                    finalArt = DrawableConverter.getGrayscaleBlurredImage(mContext, art, mMediaQsArtBlurAmount);
            case 5 ->
                    finalArt = DrawableConverter.getColoredBitmap(new BitmapDrawable(mContext.getResources(), art),
                            mMediaQsTintColor, mMediaQsTintAmount);
            default -> finalArt = art;
        }
        return finalArt;
    }

    private void setupOtherViews(View parent, int color) {
        if (parent == null) return;

        if (parent instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) parent).getChildCount(); i++) {
                if (((ViewGroup) parent).getChildAt(i) instanceof ViewGroup) {
                    setupOtherViews(((ViewGroup) parent).getChildAt(i), color);
                } else {
                    View v = ((ViewGroup) parent).getChildAt(i);
                    if (v instanceof ImageButton imageButton) {
                        imageButton.setImageTintList(ColorStateList.valueOf(color));
                    } else if (v instanceof TextView text) {
                        text.setTextColor(color);
                    }
                }
            }
        }
    }

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    private ViewPager.PageTransformer getCustomTransitions() {
        return switch (mTrasformations) {
            case 1 -> new CubeInTransformer();
            case 2 -> new CubeOutTransformer();
            case 3 -> new AccordionTransformer();
            case 4 -> new BackgroundToForegroundTransformer();
            case 5 -> new DepthPageTransformer();
            case 6 -> new FadeTransformer();
            case 7 -> new ForegroundToBackgroundTransformer();
            case 8 -> new RotateDownTransformer();
            case 9 -> new RotateUpTransformer();
            case 10 -> new StackTransformer();
            case 11 -> new TabletTransformer();
            case 12 -> new ZoomInTransformer();
            case 13 -> new ZoomOutTransformer();
            case 14 -> new ZoomOutSlideTransformer();
            case 15 -> new RaiseFromCenterTransformer();
            case 16 -> new RotateAboutBottomTransformer();
            case 17 -> new TranslationYTransformer(TranslationYTransformer.TOP_TO_BOTTOM);
            case 18 -> new TranslationYTransformer(TranslationYTransformer.BOTTOM_TO_TOP);
            default -> null;
        };

    }

    private void getTileAnimation(View v) {
        ObjectAnimator animTile = null;

        switch (mAnimStyle) {
            case 1:
                animTile = ObjectAnimator.ofFloat(v, "rotationY", 0f, 360f);
                break;
            case 2:
                animTile = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f);
                break;
            default:
                return;
        }

        switch (mInterpolatorType) {
            case 0:
                animTile.setInterpolator(new LinearInterpolator());
                break;
            case 1:
                animTile.setInterpolator(new AccelerateInterpolator());
                break;
            case 2:
                animTile.setInterpolator(new DecelerateInterpolator());
                break;
            case 3:
                animTile.setInterpolator(new AccelerateDecelerateInterpolator());
                break;
            case 4:
                animTile.setInterpolator(new BounceInterpolator());
                break;
            case 5:
                animTile.setInterpolator(new OvershootInterpolator());
                break;
            case 6:
                animTile.setInterpolator(new AnticipateInterpolator());
                break;
            case 7:
                animTile.setInterpolator(new AnticipateOvershootInterpolator());
                break;
            default:
                break;
        }
        animTile.setDuration(mAnimDuration * 1000L);
        animTile.start();
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenerPackage.equals(packageName);
    }

    private void notifyQsUpdate() {
        if (mPersonalityManager == null) return;

        int currentShape = 0;
        try {
            currentShape = (int) callMethod(mPersonalityManager, "getLastShapeType");
        } catch (Throwable t) {
            log("Oxygen Customizer - QsTileCustomization error: " + t.getMessage());
        }
        callMethod(mPersonalityManager, "notifyListener", currentShape);
    }

    private void updateMediaQs() {
        if (!mQsWidgetsEnabled && showMediaArtMediaQs) return;
        if (mQsWidgetsEnabled) return;
        if (qsInactiveColorEnabled) {
            if (mOplusQsMediaView != null && mOplusQsMediaDrawable != null) {
                mOplusQsMediaDrawable.setTint(qsInactiveColor);
                mOplusQsMediaDrawable.invalidateSelf();
                mOplusQsMediaView.setBackground(mOplusQsMediaDrawable);
            }
        } else {
            if (mOplusQsMediaView != null && mOplusQsMediaDefaultBackground != null) {
                mOplusQsMediaView.setBackground(mOplusQsMediaDefaultBackground);
            }
        }
    }

    private void setupLabels() {
        if (mLabelContainer == null) return;

        if (qsLabelsHide) {
            if (mLabelContainer.getVisibility() != View.GONE)
                mLabelContainer.setVisibility(View.GONE);
            return;
        }

        if (mLabelContainer.getVisibility() != View.VISIBLE)
            mLabelContainer.setVisibility(View.VISIBLE);

        if (qsLabelsColorEnabled) {
            mTitle.setTextColor(qsLabelsColor);
            mSubtitle.setTextColor(qsLabelsColor);
            mExpandIndicator.setImageTintList(ColorStateList.valueOf(qsLabelsColor));
        }

    }

    private XC_MethodHook getColorHook(boolean isHighlight) {
        return new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int state = (int) param.args[0];
                Shape mCustomShape = null;
                if (customHighlightTileRadius && isHighlight) {
                    mCustomShape = getTileShape(true);
                } else if (customTileRadius && !isHighlight) {
                    mCustomShape = getTileShape(false);
                }
                ShapeDrawable mPersonalityDrawable = (ShapeDrawable) param.getResult();
                if (mCustomShape != null)
                    mPersonalityDrawable.setShape(mCustomShape);
                if (state == STATE_INACTIVE && qsInactiveColorEnabled) // Inactive State
                {
                    mPersonalityDrawable.getPaint().setColor(qsInactiveColor);
                } else if (state == STATE_ACTIVE && qsActiveColorEnabled) // Active State
                {
                    mPersonalityDrawable.getPaint().setColor(qsActiveColor);
                } else if (qsDisabledColorEnabled && state != STATE_INACTIVE && state != STATE_ACTIVE) // Disabled State
                {
                    mPersonalityDrawable.getPaint().setColor(qsDisabledColor);
                }
                if (qsInactiveColorEnabled || qsActiveColorEnabled || qsDisabledColorEnabled || customHighlightTileRadius || customTileRadius)
                    mPersonalityDrawable.invalidateSelf();
            }
        };
    }

    private Shape getTileShape(boolean isHighlight) {
        return new RoundRectShape(
                new float[]{
                        dp2px(mContext, isHighlight ? highlightTSRadius : tileTSRadius),
                        dp2px(mContext, isHighlight ? highlightTSRadius : tileTSRadius),
                        dp2px(mContext, isHighlight ? highlightTDRadius : tileTDRadius),
                        dp2px(mContext, isHighlight ? highlightTDRadius : tileTDRadius),
                        dp2px(mContext, isHighlight ? highlightBDRadius : tileBDRadius),
                        dp2px(mContext, isHighlight ? highlightBDRadius : tileBDRadius),
                        dp2px(mContext, isHighlight ? highlightBSRadius : tileBSRadius),
                        dp2px(mContext, isHighlight ? highlightBSRadius : tileBSRadius)},
                null, null);
    }

}
