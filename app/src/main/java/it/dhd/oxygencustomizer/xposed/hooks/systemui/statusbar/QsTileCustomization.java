package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setIntField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTiles.QS_TILES_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.*;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsSeekBar;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.slider.Slider;

import java.lang.reflect.Method;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XPrefs;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ShellUtils;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

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
    private static final ArrayList<Object> qsViews = new ArrayList<>();

    public QsTileCustomization(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        qsActiveColorEnabled = Xprefs.getBoolean(QS_TILE_ACTIVE_COLOR_ENABLED, false);
        qsActiveColor = Xprefs.getInt(QS_TILE_ACTIVE_COLOR, Color.RED);
        qsInactiveColorEnabled = Xprefs.getBoolean(QS_TILE_INACTIVE_COLOR_ENABLED, false);
        qsInactiveColor = Xprefs.getInt(QS_TILE_INACTIVE_COLOR, Color.GRAY);
        qsDisabledColorEnabled = Xprefs.getBoolean(QS_TILE_DISABLED_COLOR_ENABLED, false);
        qsDisabledColor = Xprefs.getInt(QS_TILE_DISABLED_COLOR, Color.DKGRAY);
        qsBrightnessSliderCustomize = Xprefs.getBoolean(QS_BRIGHTNESS_SLIDER_CUSTOMIZE, false);
        qsBrightnessSliderColorMode = Integer.parseInt(Xprefs.getString(QS_BRIGHTNESS_SLIDER_COLOR_MODE, "0"));
        qsBrightnessSliderColor = Xprefs.getInt(QS_BRIGHTNESS_SLIDER_COLOR, getPrimaryColor(mContext));
        qsBrightnessBackgroundCustomize = Xprefs.getBoolean(QS_BRIGHTNESS_SLIDER_BACKGROUND_ENABLED, false);
        qsBrightnessBackgroundColor = Xprefs.getInt(QS_BRIGHTNESS_SLIDER_BACKGROUND_COLOR, Color.TRANSPARENT);
        qsLabelsHide = Xprefs.getBoolean(QS_TILE_HIDE_LABELS, false);
        qsLabelsColorEnabled = Xprefs.getBoolean(QS_TILE_LABELS_CUSTOM_COLOR_ENABLED, false);
        qsLabelsColor = Xprefs.getInt(QS_TILE_LABELS_CUSTOM_COLOR, Color.WHITE);

        if (Key.length > 0) {
            for(String k : QS_UPDATE_PREFS) {
                if (Key[0].equals(k)) {
                    if (Key[0].equals(QS_TILE_INACTIVE_COLOR_ENABLED) || Key[0].equals(QS_TILE_INACTIVE_COLOR)) {
                        updateMediaQs();
                    }
                    notifyQsUpdate();
                }
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

        Class<?> OplusQSTileBaseView = findClass("com.oplus.systemui.qs.qstileimpl.OplusQSTileBaseView", lpparam.classLoader);

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

        hookAllMethods(OplusQSTileBaseView, "generateDrawable", colorHook);

        Class<?> OplusQSHighlightTileView = findClass("com.oplus.systemui.qs.qstileimpl.OplusQSHighlightTileView", lpparam.classLoader);
        hookAllMethods(OplusQSHighlightTileView, "generateDrawable", colorHook);


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

        Class<?> OplusQSTileView = findClass("com.oplus.systemui.qs.qstileimpl.OplusQSTileView", lpparam.classLoader);
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

        Class<?> OplusToggleSliderView = findClass("com.oplus.systemui.qs.widget.OplusToggleSliderView", lpparam.classLoader);

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

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenerPackage.equals(packageName);
    }

    private void notifyQsUpdate() {
        if (mPersonalityManager == null) return;

        int currentShape = 0;
        try {
            currentShape = Integer.parseInt(ShellUtils.execCommand("settings get system qs_personality_shape_type", true).successMsg);
        } catch (Throwable ignored) {}
        int finalCurrentShape = currentShape;
        /*new Thread(() -> {
            try {
                while (updating) {
                    Thread.currentThread().wait(500);
                }
                updating = true;

                ShellUtils.execCommand("settings set system qs_personality_shape_type 0", false);
                Thread.sleep(500);
                ShellUtils.execCommand("settings get system qs_personality_shape_type " + finalCurrentShape, false);

                Thread.sleep(500);
                updating = false;
            } catch (Exception ignored) {
            }
        }).start();*/
        callMethod(mPersonalityManager, "notifyListener", currentShape);
    }

    private void updateMediaQs() {
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
