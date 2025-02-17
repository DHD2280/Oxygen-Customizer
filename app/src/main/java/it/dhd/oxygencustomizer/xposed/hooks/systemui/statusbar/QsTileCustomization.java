package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
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
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_SLIDERS_BLEND_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_SLIDERS_REMOVE_BLUR;
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
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.isNeedSeparateDarkThemeColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.InvocationTargetException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
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
import it.dhd.oxygencustomizer.xposed.views.QsControlsView;

public class QsTileCustomization extends XposedMods {

    private static final String listenerPackage = Constants.Packages.SYSTEM_UI;
    private final int STATE_ACTIVE = 2;
    private final int STATE_INACTIVE = 1;
    private static Object mPersonalityManager = null;

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
    private Class<?> ForegroundBlurParam = null;
    private boolean qsBrightnessSliderCustomize, qsBrightnessBackgroundCustomize;
    private int qsBrightnessSliderColorMode, qsBrightnessSliderColor, qsBrightnessBackgroundColor;
    private final int SLIDER_PROGRESS = 0;
    private final int SLIDER_BACKGROUND = 1;
    private boolean sliderRemoveBlur = false;
    private int sliderBlendColor = -1;
    private final int BLEND_LUMINOSITY_COLOR_DODGE = 1;
    private final int BLEND_COLOR_DODGE_LUMINOSITY = 2;
    private final int BLEND_OVERLAY_LUMINOSITY = 3;
    private final int BLEND_LUMINOSITY_OVERLAY = 4;

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
        sliderRemoveBlur = Xprefs.getBoolean(QS_SLIDERS_REMOVE_BLUR, false);
        sliderBlendColor = Integer.parseInt(Xprefs.getString(QS_SLIDERS_BLEND_COLOR, "0"));

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
                    if (Build.VERSION.SDK_INT >= 35) {
                        if (Key[0].equals(QS_TILE_HIDE_LABELS) ||
                                Key[0].equals(QS_TILE_LABELS_CUSTOM_COLOR) ||
                                Key[0].equals(QS_TILE_LABELS_CUSTOM_COLOR_ENABLED)) {
                            SystemUtils.doubleToggleDarkMode();
                        }
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

        ReflectedClass PersonalityManager = ReflectedClass.of(
                "com.oplus.systemui.qs.personality.PersonalityManager" /* OOS14-15 */,
                "com.oplusos.systemui.qs.personality.PersonalityManager" /* OOS13 */);
        if (PersonalityManager != null) {
            PersonalityManager
                    .afterConstruction()
                            .run(param -> mPersonalityManager = param.thisObject);
        } else log("PersonalityManager not found");

        // Color Hooker
        hookQsColors();

        // Animation Hooker
        hookQsTileAnimation();
        // End Animation Hooker

        // Media Panel Album Art
        hookMediaPanel();

        // Qs Labels
        ReflectedClass OplusQSTileView = ReflectedClass.of(
                "com.oplus.systemui.plugins.qs.tile.OplusQSTileView" /* OOS15 */,
                "com.oplus.systemui.qs.qstileimpl.OplusQSTileView" /* OOS14 */,
                "com.oplusos.systemui.qs.qstileimpl.OplusQSTileView" /* OOS13 */);
        if (OplusQSTileView == null) {
            log(new Throwable("OplusQSTileView not found"));
        }

        ReflectedClass OplusQSTileViewPlugin = ReflectedClass.ofIfPossible("com.oplus.systemui.plugins.qs.tile.OplusQSTileView");

        ReflectedClass.ReflectionConsumer labelHook = param -> {
            mLabelContainer = (ViewGroup) getObjectField(param.thisObject, "mLabelContainer");
            mTitle = (TextView) getObjectField(param.thisObject, "mLabel");
            mSubtitle = (TextView) getObjectField(param.thisObject, "mSecondLine");
            mExpandIndicator = (ImageView) getObjectField(param.thisObject, "mExpandIndicator");
            setupLabels();
        };

        OplusQSTileView.after("createLabel").run(labelHook);
        if (OplusQSTileViewPlugin.getClazz() != null) OplusQSTileViewPlugin.after("createLabel").run(labelHook);
        OplusQSTileView.after("updateTextColor").run(labelHook);
        OplusQSTileView.after("handleStateChanged").run(labelHook);

        ReflectedClass OplusToggleSliderView = ReflectedClass.of(
                "com.oplus.systemui.qs.widget.OplusToggleSliderView",
                "com.oplusos.systemui.qs.widget.OplusToggleSliderView");

        OplusToggleSliderView
                .after("onShapeChanged")
                        .run(param -> {
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
                        });

        OplusToggleSliderView
                .after("setupSliderProgressDrawable")
                        .run(param -> {
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
                        });

        final ReflectedClass.ReflectionConsumer newUiHook = param -> {
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
        };

        ReflectedClass OplusQsBaseToggleSliderLayout;
        if (Build.VERSION.SDK_INT >= 35) {
            try {
                OplusQsBaseToggleSliderLayout = ReflectedClass.of("com.oplus.systemui.qs.base.seek.OplusQsBaseToggleSliderLayout");
                OplusQsBaseToggleSliderLayout.afterConstruction().run(newUiHook);
            } catch (Throwable t) {
                log(t);
            }
        }

        try {
            ReflectedClass OplusQsToggleSliderLayout = ReflectedClass.of("com.oplus.systemui.qs.widget.OplusQsToggleSliderLayout");
            OplusQsToggleSliderLayout.afterConstruction().run(newUiHook);
            OplusQsToggleSliderLayout.after("onShapeChanged").run(newUiHook);

        } catch (Throwable ignored) {
        }

        if (Build.VERSION.SDK_INT >= 35) {
            hookSliders();
        }

        try {
            ReflectedClass PagedTileLayout = ReflectedClass.of("com.android.systemui.qs.PagedTileLayout");
            PagedTileLayout
                    .afterConstruction()
                            .run(param -> {
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
                            });
        } catch (Throwable t) {
            log(t);
        }

    }


    public void hookQsColors() {
        ReflectedClass OplusQSTileBaseView = ReflectedClass.of(
                "com.oplus.systemui.qs.base.tile.OplusQSTileBaseView" /* OOS15 */,
                "com.oplus.systemui.qs.qstileimpl.OplusQSTileBaseView" /* OOS14 */,
                "com.oplusos.systemui.qs.qstileimpl.OplusQSTileBaseView" /* OOS13 */);
        OplusQSTileBaseView.after("generateDrawable").run(getColorHook(false));

        ReflectedClass OplusQSHighlightTileView = ReflectedClass.of(
                "com.oplus.systemui.qs.base.tile.OplusQSHighlightTileView" /* OOS15 */,
                "com.oplus.systemui.qs.qstileimpl.OplusQSHighlightTileView" /* OOS14 */,
                "com.oplusos.systemui.qs.qstileimpl.OplusQSHighlightTileView" /* OOS13 */);
        OplusQSHighlightTileView.after("generateDrawable").run(getColorHook(true));

    }

    public void hookQsTileAnimation() {
        ReflectedClass OplusQSTileBaseView = ReflectedClass.of(
                "com.oplus.systemui.qs.base.tile.OplusQSTileBaseView" /* OOS15 */,
                "com.oplus.systemui.qs.qstileimpl.OplusQSTileBaseView" /* OOS14 */,
                "com.oplusos.systemui.qs.qstileimpl.OplusQSTileBaseView" /* OOS13 */);
        if (OplusQSTileBaseView == null) {
            log(new Throwable("OplusQSTileBaseView not found"));
        }
        final ReflectedClass.ReflectionConsumer animationHook = param -> {
            View qsTile = (View) param.thisObject;
            qsTile.post(() -> getTileAnimation(qsTile));
        };

        OplusQSTileBaseView.after("performClick").run(animationHook);

        ReflectedClass OplusQSHighlightTileView = ReflectedClass.of(
                "com.oplus.systemui.qs.base.tile.OplusQSHighlightTileView" /* OOS15 */,
                "com.oplus.systemui.qs.qstileimpl.OplusQSHighlightTileView" /* OOS14 */,
                "com.oplusos.systemui.qs.qstileimpl.OplusQSHighlightTileView" /* OOS13 */);
        if (OplusQSHighlightTileView == null) {
            log(new Throwable("OplusQSHighlightTileView not found"));
        }
        OplusQSHighlightTileView.after("performClick").run(animationHook);
    }

    public void hookMediaPanel() {
        ReflectedClass OplusQsMediaPanelView = ReflectedClass.of("com.oplus.systemui.qs.media.OplusQsMediaPanelView");
        OplusQsMediaPanelView
                .after("onFinishInflate")
                        .run(param -> {
                            mOplusQsMediaView = (View) param.thisObject;
                            if (mQsWidgetsEnabled) return;
                            if (Build.VERSION.SDK_INT >= 35) { // OOS 15
                                Drawable bgg = getOOS15Background(param.thisObject);
                                XposedBridge.log("OplusQsMediaPanelView onFinishInflate background null? " + (bgg == null));
                                mOplusQsMediaDefaultBackground = getOOS15Background(param.thisObject);
                                mOplusQsMediaView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                                    @Override
                                    public void onViewAttachedToWindow(@NonNull View v) {
                                        mOplusQsMediaDefaultBackground = getOOS15Background(param.thisObject);
                                    }

                                    @Override
                                    public void onViewDetachedFromWindow(@NonNull View v) {

                                    }
                                });
                            } else { // OOS 13-14
                                mOplusQsMediaDefaultBackground = mOplusQsMediaView.getBackground();
                            }
                            if (mOplusQsMediaDefaultBackground != null) {
                                mOplusQsMediaDrawable = mOplusQsMediaDefaultBackground.getConstantState().newDrawable().mutate();
                            }
                            if (qsInactiveColorEnabled) {
                                mOplusQsMediaDrawable.setTint(qsInactiveColor);
                                mOplusQsMediaDrawable.invalidateSelf();
                                mOplusQsMediaView.setBackground(mOplusQsMediaDrawable);
                            } else {
                                if (Build.VERSION.SDK_INT < 35) mOplusQsMediaView.setBackground(mOplusQsMediaDefaultBackground);
                            }

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
                        });

        OplusQsMediaPanelView
                .after("onShapeChanged")
                .run(param -> {
                    if (mQsWidgetsEnabled) return;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        XposedBridge.log("QsWidgets: onShapeChanged");
                        Object backgroundProxy = getObjectField(param.thisObject, "backgroundProxy");
                        Object panelInfo = getObjectField(backgroundProxy, "panelInfo");
                        XposedBridge.log("onShapeChanged backgroundProxy null? " + (backgroundProxy == null));
                        XposedBridge.log("onShapeChanged panelInfo null? " + (panelInfo == null));
                        Drawable bg = (Drawable) callMethod(panelInfo, "getBackgroundDrawable");
                        XposedBridge.log("onShapeChanged getBackgroundDrawable null? " + (bg == null));
                        Object getBackgroundDrawable = callMethod(param.thisObject, "getBackgroundDrawable");
                        XposedBridge.log("onShapeChanged getBackgroundDrawable null? " + (getBackgroundDrawable == null));
                        mOplusQsMediaDefaultBackground = bg;
                        View view = (View) callMethod(panelInfo, "getBackgroundView");
                        view.postDelayed(() -> view.setBackground(null), 250L);
                    }, 250L);
                });

        OplusQsMediaPanelView
                .after("bindTitleAndText")
                        .run(param -> updateMediaQsBackground());

    }

    private Drawable getOOS15Background(Object mediaPanelView) {
        Drawable global = null;
        try {
            global = (Drawable) callMethod(mediaPanelView, "getGlobalThemeBackgroundDrawable");
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        try {
            Object backgroundProxy = getObjectField(mediaPanelView, "backgroundProxy");
            Object panelInfo = getObjectField(backgroundProxy, "panelInfo");
            XposedBridge.log("getOOS15Background backgroundProxy null? " + (backgroundProxy == null));
            XposedBridge.log("getOOS15Background panelInfo null? " + (panelInfo == null));
            Drawable bg = (Drawable) callMethod(panelInfo, "getBackgroundDrawable");
            XposedBridge.log("getOOS15Background getBackgroundDrawable null? " + (bg == null));
            if (bg != null) return bg;
            else return global;
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        
        return null;
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
        if (mCoverImg != null) mCoverImg.setVisibility(View.GONE);
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
            int dominantColor = palette != null ? palette.getDominantColor(Color.WHITE) : Color.WHITE;
            mColorOnAlbum =
                    isColorDark(dominantColor) ?
                            DrawableConverter.findContrastColorAgainstDark(Color.WHITE, dominantColor, true, 2) :
                            DrawableConverter.findContrastColor(Color.BLACK, dominantColor, true, 2);
            mOplusQsMediaView.post(() -> setupOtherViews(mOplusQsMediaView, mColorOnAlbum));
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
        setupOtherViews(mOplusQsMediaView, SystemUtils.isDarkMode() ? Color.WHITE : Color.BLACK);
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

    private void hookSliders() {

        ForegroundBlurParam = ReflectedClass.of("com.oplus.posteffect.ForegroundBlurParam").getClazz();

        ReflectedClass OplusQsVerticalSeekBar = ReflectedClass.of("com.oplus.systemui.qs.base.seek.OplusQsVerticalSeekBar");

        // Run on sysui start
        OplusQsVerticalSeekBar
                .before("createActiveTrackBlurParams")
                .run(param -> {
                    if (!qsBrightnessSliderCustomize) return;

                    Object ForegroundParams = getForegroundBlur(SLIDER_PROGRESS);

                    param.setResult(ForegroundParams);
                });

        OplusQsVerticalSeekBar
                .before("createInactiveTrackBlurParams")
                .run(param -> {
                    if (!qsBrightnessBackgroundCustomize) return;
                    Object ForegroundParams = getForegroundBlur(SLIDER_BACKGROUND);

                    param.setResult(ForegroundParams);
//                    param.setResult(
//                            callMethod(
//                                    param.thisObject,
//                                    "createForegroundBlurParams",
//                                    isNeedSeparateDarkThemeColor(mContext),
//                                    qsBrightnessBackgroundColor,
//                                    qsBrightnessBackgroundColor
//                            )
//                    );
                });

        // now hook when update colors
        // public final void drawForegroundBlur(Canvas canvas, Paint paint, ForegroundBlurParam foregroundBlurParam, Path path) {
        OplusQsVerticalSeekBar
                .before("drawForegroundBlur")
                .run(param -> {
                    Object foregroundBlurParam = param.args[2];
                    Object activeTrackParam = getObjectField(param.thisObject, "activeTrackParam");
                    Object inactiveTrackParam = getObjectField(param.thisObject, "inactiveTrackParam");

                    if (foregroundBlurParam == activeTrackParam) { // draw active color
                        // TODO: check if we need to draw active color
                        Object newForeground;
                        if (!qsBrightnessSliderCustomize || qsBrightnessSliderColorMode == 0) {
                            newForeground = callMethod(param.thisObject, "createActiveTrackBlurParams");
                            param.args[2] = newForeground;
                        } else {
                            newForeground = callMethod(param.thisObject, "createForegroundBlurParams",
                                    isNeedSeparateDarkThemeColor(mContext),
                                    qsBrightnessSliderColorMode == 2 ? qsBrightnessSliderColor : getPrimaryColor(mContext),
                                    qsBrightnessSliderColorMode == 2 ? qsBrightnessSliderColor : getPrimaryColor(mContext)
                            );
                        }
                        param.args[2] = newForeground;
                    } else { // only remains inactive color
                        Object newForeground;
                        if (qsBrightnessBackgroundCustomize) {
                            newForeground = callMethod(param.thisObject, "createForegroundBlurParams",
                                    isNeedSeparateDarkThemeColor(mContext),
                                    qsBrightnessBackgroundColor,
                                    qsBrightnessBackgroundColor
                            );
                        } else {
                            newForeground = callMethod(param.thisObject, "createInactiveTrackBlurParams");
                        }
                        param.args[2] = newForeground;
                    }

                });

    }

    private int getBlendMode() {
        if (sliderRemoveBlur) return 0;
        return switch (sliderBlendColor) {
            case 0 -> BLEND_LUMINOSITY_COLOR_DODGE;
            case 1 -> BLEND_COLOR_DODGE_LUMINOSITY;
            case 2 -> BLEND_OVERLAY_LUMINOSITY;
            case 3 -> BLEND_LUMINOSITY_OVERLAY;
            default -> SystemUtils.isDarkMode() ? BLEND_LUMINOSITY_OVERLAY : BLEND_LUMINOSITY_COLOR_DODGE;
        };
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
        ObjectAnimator animTile;

        switch (mAnimStyle) {
            case 1:
                animTile = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f); // Rotate
                break;
            case 2:
                animTile = ObjectAnimator.ofFloat(v, "rotationX", 0f, 360f); // Flip X
                break;
            case 3:
                animTile = ObjectAnimator.ofFloat(v, "rotationY", 0f, 360f); // Flip Y
                break;
            case 4:
                animTile = ObjectAnimator.ofFloat(v, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0); // Shake
                break;
            case 5:
                animTile = ObjectAnimator.ofFloat(v, "alpha", 0f, 1f); // Fade In
                break;
            case 6:
                animTile = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.2f, 0.8f, 1f); // Bounce Effect (Scale X)
                break;
            case 7:
                animTile = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.2f, 0.8f, 1f); // Bounce Effect (Scale Y)
                break;
            case 8:
                // Pulse Animation X
                animTile = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.1f, 1f);
                animTile.setRepeatCount(0);
                animTile.setDuration(1000);
                break;
            case 9:
                // Pulse Animation Y
                animTile = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.1f, 1f);
                animTile.setRepeatCount(0);
                animTile.setDuration(1000);
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

    public static void notifyQsUpdate() {
        if (mPersonalityManager == null) return;

        int currentShape = 0;
        try {
            currentShape = (int) callMethod(mPersonalityManager, "getLastShapeType");
        } catch (Throwable t) {
            XposedBridge.log("Oxygen Customizer - QsTileCustomization error: " + Log.getStackTraceString(t));
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
            if (mTitle != null && mTitle.getVisibility() != View.GONE)
                mTitle.setVisibility(View.GONE);
            if (mSubtitle != null && mSubtitle.getVisibility() != View.GONE)
                mSubtitle.setVisibility(View.GONE);
            if (mExpandIndicator != null && mExpandIndicator.getVisibility() != View.GONE)
                mExpandIndicator.setVisibility(View.GONE);
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

    private ReflectedClass.ReflectionConsumer getColorHook(boolean isHighlight) {
        return param -> {
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
        };
    }

    // TODO: Custom qs tile shape for OOS15
    private Drawable getCustomizedTile(Object tile, Object state, boolean isHighlight) {
        if (tile == null || state == null) return null;
        int intState = getIntField(state, "state");
        Shape mCustomShape = null;
        if (customHighlightTileRadius && isHighlight) {
            mCustomShape = getTileShape(true);
        } else if (customTileRadius && !isHighlight) {
            mCustomShape = getTileShape(false);
        }
        Drawable mPersonalityDrawable = (Drawable) callMethod(tile, "getBackgroundDrawable");
        if (intState == STATE_INACTIVE && qsInactiveColorEnabled) // Inactive State
        {
            mPersonalityDrawable.setColorFilter(new PorterDuffColorFilter(qsInactiveColor, PorterDuff.Mode.SRC_IN));
        } else if (intState == STATE_ACTIVE && qsActiveColorEnabled) // Active State
        {
            mPersonalityDrawable.setColorFilter(new PorterDuffColorFilter(qsActiveColor, PorterDuff.Mode.SRC_IN));
        } else if (qsDisabledColorEnabled && intState != STATE_INACTIVE && intState != STATE_ACTIVE) // Disabled State
        {
            mPersonalityDrawable.setColorFilter(new PorterDuffColorFilter(qsDisabledColor, PorterDuff.Mode.SRC_IN));
        }
        if (qsInactiveColorEnabled || qsActiveColorEnabled || qsDisabledColorEnabled || customHighlightTileRadius || customTileRadius)
            mPersonalityDrawable.invalidateSelf();
        return mPersonalityDrawable;
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

    private Object getForegroundBlur(int type) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {

        return switch (type) {
            case SLIDER_PROGRESS ->
                    ForegroundBlurParam.getConstructor(int.class, int.class, int.class)
                            .newInstance(getBlendMode(),
                                    qsBrightnessSliderColorMode == 2 ? qsBrightnessSliderColor : getPrimaryColor(mContext),
                                    qsBrightnessSliderColorMode == 2 ? qsBrightnessSliderColor : getPrimaryColor(mContext));
            case SLIDER_BACKGROUND ->
                    ForegroundBlurParam.getConstructor(int.class, int.class, int.class)
                            .newInstance(getBlendMode(), qsBrightnessBackgroundColor, qsBrightnessBackgroundColor);
            default -> null;
        };
    }

}
