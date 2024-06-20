package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
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
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_LABELS_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_LABELS_CUSTOM_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_UPDATE_PREFS;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getArt;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;

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
import it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider;
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
    private Object mPersonalityManager = null;
    private int qsInactiveColor, qsActiveColor, qsDisabledColor;
    private boolean qsInactiveColorEnabled, qsActiveColorEnabled, qsDisabledColorEnabled;
    private boolean qsLabelsHide, qsLabelsColorEnabled;
    private int qsLabelsColor;
    private boolean qsBrightnessSliderCustomize, qsBrightnessBackgroundCustomize;
    private int qsBrightnessSliderColorMode, qsBrightnessSliderColor, qsBrightnessBackgroundColor;
    private View mOplusQsMediaView = null;
    private Drawable mOplusQsMediaDefaultBackground = null;
    private Drawable mOplusQsMediaDrawable = null;
    private ViewGroup mLabelContainer = null;
    private TextView mTitle = null, mSubtitle = null;
    private ImageView mExpandIndicator = null;
    private boolean advancedCustom = true;
    private int mAnimStyle = 0;
    private int mInterpolatorType = 0;
    private int mAnimDuration = 0;
    private boolean mTrasformationsEnabled = false;
    private int mTrasformations = 1;
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

        // Media QS
        showMediaArtMediaQs = Xprefs.getBoolean(QS_MEDIA_SHOW_ALBUM_ART, false);
        mMediaQsArtFilter = Integer.parseInt(Xprefs.getString(QS_MEDIA_ART_FILTER, "0"));
        mMediaQsArtBlurAmount = (Xprefs.getSliderInt(QS_MEDIA_ART_BLUR_AMOUNT, 30)/100f) * 25f;
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
            for(String k : QS_UPDATE_PREFS) {
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

        Class<?> PersonalityManager = findClass("com.oplus.systemui.qs.personality.PersonalityManager", lpparam.classLoader);
        hookAllConstructors(PersonalityManager, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // Do something
                mPersonalityManager = param.thisObject;
            }
        });

        Class<?> OplusQSTileBaseView;
        try {
            OplusQSTileBaseView = findClass("com.oplus.systemui.qs.qstileimpl.OplusQSTileBaseView", lpparam.classLoader);
        } catch (Throwable ignored) {
            OplusQSTileBaseView = findClass("com.oplusos.systemui.qs.qstileimpl.OplusQSTileBaseView", lpparam.classLoader);
        }

        /*
        TESTING CUSTOMIZATIONS
        hookAllMethods(OplusQSTileBaseView, "updateBgResource", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ImageView mBg = (ImageView) getObjectField(param.thisObject, "mBg");
                int state = getIntField(param.thisObject, "mTileIconState");
                if (state == 2) {
                    GradientDrawable gb = new GradientDrawable();
                    gb.setShape(GradientDrawable.RECTANGLE);
                    gb.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                    gb.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                    gb.setCornerRadii(new float[]{
                            dp2px(mContext, 28), dp2px(mContext, 28),
                            dp2px(mContext, 28), dp2px(mContext, 28),
                            dp2px(mContext, 28), dp2px(mContext, 28),
                            dp2px(mContext, 28), dp2px(mContext, 28)
                    });
                    gb.setStroke(28, getPrimaryColor(mContext));
                    mBg.setImageDrawable(gb);
                }
            }
        });

        hookAllMethods(OplusQSTileBaseView, "obtainDrawable", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                int state = (int) param.args[0];
                if (state == 2) {
                    GradientDrawable gb = new GradientDrawable();
                    gb.setShape(GradientDrawable.RECTANGLE);
                    gb.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                    gb.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                    gb.setCornerRadii(new float[]{
                            dp2px(mContext, 28), dp2px(mContext, 28),
                            dp2px(mContext, 28), dp2px(mContext, 28),
                            dp2px(mContext, 28), dp2px(mContext, 28),
                            dp2px(mContext, 28), dp2px(mContext, 28)
                    });
                    gb.setStroke(28, getPrimaryColor(mContext));
                    param.setResult(gb);
                }
            }
        });

        hookAllMethods(OplusQSTileBaseView, "setDrawableForDefault", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ImageView mBg = (ImageView) getObjectField(param.thisObject, "mBg");
                Object St = param.args[0];
                int state = (int) getIntField(St, "state");
                if (state == 2) {
                    GradientDrawable gb = new GradientDrawable();
                    gb.setShape(GradientDrawable.RECTANGLE);
                    gb.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                    gb.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                    gb.setCornerRadii(new float[]{
                            dp2px(mContext, 28), dp2px(mContext, 28),
                            dp2px(mContext, 28), dp2px(mContext, 28),
                            dp2px(mContext, 28), dp2px(mContext, 28),
                            dp2px(mContext, 28), dp2px(mContext, 28)
                    });
                    gb.setStroke(28, getPrimaryColor(mContext));
                    mBg.setImageDrawable(gb);
                }
            }
        });

        hookAllMethods(OplusQSTileBaseView, "handleStateChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ImageView mBg = (ImageView) getObjectField(param.thisObject, "mBg");
                Object St = param.args[0];
                int state = (int) getIntField(St, "state");
                if (state == 2) {
                    GradientDrawable gb = new GradientDrawable();
                    gb.setShape(GradientDrawable.RECTANGLE);
                    gb.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                    gb.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                    gb.setCornerRadii(new float[]{
                            dp2px(mContext, 28), dp2px(mContext, 28),
                            dp2px(mContext, 28), dp2px(mContext, 28),
                            dp2px(mContext, 28), dp2px(mContext, 28),
                            dp2px(mContext, 28), dp2px(mContext, 28)
                    });
                    gb.setStroke(28, getPrimaryColor(mContext));
                    mBg.setImageDrawable(gb);
                }
            }
        });*/


        final XC_MethodHook colorHook = new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int state = (int) param.args[0];
                ShapeDrawable mPersonalityDrawable = (ShapeDrawable) param.getResult();
                if (state == 1 && qsInactiveColorEnabled) // Inactive State
                {
                    mPersonalityDrawable.getPaint().setColor(qsInactiveColor);
                } else if (state == 2 && qsActiveColorEnabled) // Active State
                {
                    mPersonalityDrawable.getPaint().setColor(qsActiveColor);
                } else if (qsDisabledColorEnabled && state!=1 && state!=2) // Disabled State
                {
                    mPersonalityDrawable.getPaint().setColor(qsDisabledColor);
                }
                if (qsInactiveColorEnabled || qsActiveColorEnabled || qsDisabledColorEnabled || advancedCustom) mPersonalityDrawable.invalidateSelf();
            }
        };

        final XC_MethodHook animationHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View qsTile = (View) param.thisObject;
                qsTile.post(()->getTileAnimation(qsTile));
            }
        };

        hookAllMethods(OplusQSTileBaseView, "generateDrawable", colorHook);

        hookAllMethods(OplusQSTileBaseView, "performClick", animationHook);

        Class<?> OplusQSHighlightTileView;
        try {
            OplusQSHighlightTileView = findClass("com.oplus.systemui.qs.qstileimpl.OplusQSHighlightTileView", lpparam.classLoader);
        } catch (Throwable ignored) {
            OplusQSHighlightTileView = findClass("com.oplusos.systemui.qs.qstileimpl.OplusQSHighlightTileView", lpparam.classLoader);
        }
        hookAllMethods(OplusQSHighlightTileView, "generateDrawable", colorHook);
        hookAllMethods(OplusQSHighlightTileView, "performClick", animationHook);


        Class<?> OplusQsMediaPanelView = findClass("com.oplus.systemui.qs.media.OplusQsMediaPanelView", lpparam.classLoader);
        hookAllMethods(OplusQsMediaPanelView, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // Do something
                mOplusQsMediaView = (View) param.thisObject;
                mOplusQsMediaDefaultBackground = mOplusQsMediaView.getBackground();
                mOplusQsMediaDrawable = mOplusQsMediaView.getBackground();
                if (qsInactiveColorEnabled) {
                    mOplusQsMediaDrawable.setTint(qsInactiveColor);
                    mOplusQsMediaDrawable.invalidateSelf();
                    mOplusQsMediaView.setBackground(mOplusQsMediaDrawable);
                } else
                    mOplusQsMediaView.setBackground(mOplusQsMediaDefaultBackground);
            }
        });

        hookAllMethods(OplusQsMediaPanelView, "bindTitleAndText", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object mediaData = param.args[0];
                if (mediaData == null && showMediaArtMediaQs) {
                    Drawable[] layers = new Drawable[]{new BitmapDrawable(mContext.getResources(), mArt), qsInactiveColorEnabled ? mOplusQsMediaDrawable : mOplusQsMediaDefaultBackground};
                    TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
                    mOplusQsMediaView.setBackground(transitionDrawable);
                    transitionDrawable.startTransition(250);
                    return;
                }
                if (showMediaArtMediaQs) {
                    updateMediaQsBackground();
                }
            }
        });

        Class<?> OplusQSTileView;
        try {
            OplusQSTileView = findClass("com.oplus.systemui.qs.qstileimpl.OplusQSTileView", lpparam.classLoader);
        } catch (Throwable ignored) {
            OplusQSTileView = findClass("com.oplusos.systemui.qs.qstileimpl.OplusQSTileView", lpparam.classLoader); // OOS 13
        }
        hookAllMethods(OplusQSTileView, "createLabel", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                // Do something
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
                // Do something
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
                            callMethod(getObjectField(param.thisObject, "mSlider"), "setProgressColor", ColorStateList.valueOf(getPrimaryColor(mContext)));
                        } else if (qsBrightnessSliderColorMode == 2) {
                            callMethod(getObjectField(param.thisObject, "mSlider"), "setProgressColor", ColorStateList.valueOf(qsBrightnessSliderColor));
                        }

                        if (qsBrightnessBackgroundCustomize) {
                            callMethod(getObjectField(param.thisObject, "mSlider"), "setSeekBarBackgroundColor", ColorStateList.valueOf(qsBrightnessBackgroundColor));
                        } else {
                            int color = ResourcesCompat.getColor(mContext.getResources(), mContext.getResources().getIdentifier("status_bar_qs_brightness_slider_bg_color", "color", lpparam.packageName), mContext.getTheme());
                            callMethod(getObjectField(param.thisObject, "mSlider"), "setSeekBarBackgroundColor", ColorStateList.valueOf(color));
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

                callMethod(getObjectField(param.thisObject, "mSlider"), "setProgressColor", ColorStateList.valueOf(colorToApply));
                if (getBooleanField(param.thisObject, "mIsMirror")) {
                    callMethod(getObjectField(param.thisObject, "mSlider"), "setThumbColor", ColorStateList.valueOf(colorToApply));
                }

                if (qsBrightnessBackgroundCustomize) {
                    callMethod(getObjectField(param.thisObject, "mSlider"), "setSeekBarBackgroundColor", ColorStateList.valueOf(qsBrightnessBackgroundColor));
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

        } catch (Throwable ignored) {}

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
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    if (!mTrasformationsEnabled) return;
                                    final int childCount = (int) callMethod(vPager, "getChildCount");
                                    for (int i = 0; i < childCount; i++) {
                                        final View child = (View) callMethod(vPager, "getChildAt", i);
                                        final Object lp = callMethod(child, "getLayoutParams");
                                        if (getBooleanField(lp, "isDecor")) continue;
                                        final float transformPos = (float) (child.getLeft() - (int)callMethod(vPager, "getScrollX")) / child.getWidth();
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

    private void updateMediaQsBackground() {
        if (!showMediaArtMediaQs || mOplusQsMediaView == null) return;
        Bitmap oldArt = mArt;
        mArt = getFilteredArt(getArt());
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

    private Bitmap getFilteredArt(Bitmap art) {
        Bitmap finalArt;
        switch (mMediaQsArtFilter) {
            default -> finalArt = art;
            case 1 -> finalArt = DrawableConverter.toGrayscale(art);
            case 2 -> finalArt = DrawableConverter.getColoredBitmap(new BitmapDrawable(mContext.getResources(), art),
                    getPrimaryColor(mContext));
            case 3 -> finalArt = DrawableConverter.getBlurredImage(mContext, art, mMediaQsArtBlurAmount);
            case 4 -> finalArt = DrawableConverter.getGrayscaleBlurredImage(mContext, art, mMediaQsArtBlurAmount);
            case 5 -> finalArt = DrawableConverter.getColoredBitmap(new BitmapDrawable(mContext.getResources(), art),
                    mMediaQsTintColor, mMediaQsTintAmount);
        };
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
        if (showMediaArtMediaQs) return;
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
            if (mLabelContainer.getVisibility() != View.GONE) mLabelContainer.setVisibility(View.GONE);
            return;
        }

        if (mLabelContainer.getVisibility() != View.VISIBLE) mLabelContainer.setVisibility(View.VISIBLE);

        if (qsLabelsColorEnabled) {
            mTitle.setTextColor(qsLabelsColor);
            mSubtitle.setTextColor(qsLabelsColor);
            mExpandIndicator.setImageTintList(ColorStateList.valueOf(qsLabelsColor));
        }

    }

}
