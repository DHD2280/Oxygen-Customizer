package it.dhd.oxygencustomizer.xposed.utils;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.RenderMode;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;

public class ViewHelper {

    public static void setMarginsNoConvert(Object viewGroup, Context context, int left, int top, int right, int bottom) {
        if (viewGroup instanceof View) {
            if (((View) viewGroup).getLayoutParams() instanceof LinearLayout.LayoutParams)
                ((LinearLayout.LayoutParams) ((View) viewGroup).getLayoutParams()).setMargins(left, top, right, bottom);
            else if (((View) viewGroup).getLayoutParams() instanceof FrameLayout.LayoutParams)
                ((FrameLayout.LayoutParams) ((View) viewGroup).getLayoutParams()).setMargins(left, top, right, bottom);
        } else if (viewGroup instanceof ViewGroup.MarginLayoutParams)
            ((ViewGroup.MarginLayoutParams) viewGroup).setMargins(left, top, right, bottom);
        else
            throw new IllegalArgumentException("The viewGroup object has to be either a View or a ViewGroup.MarginLayoutParams. Found " + viewGroup.getClass().getSimpleName() + " instead.");
    }

    public static void setMargins(Object viewGroup, Context context, int left, int top, int right, int bottom) {
        if (viewGroup instanceof View) {
            if (((View) viewGroup).getLayoutParams() instanceof LinearLayout.LayoutParams)
                ((LinearLayout.LayoutParams) ((View) viewGroup).getLayoutParams()).setMargins(dp2px(context, left), dp2px(context, top), dp2px(context, right), dp2px(context, bottom));
            else if (((View) viewGroup).getLayoutParams() instanceof FrameLayout.LayoutParams)
                ((FrameLayout.LayoutParams) ((View) viewGroup).getLayoutParams()).setMargins(dp2px(context, left), dp2px(context, top), dp2px(context, right), dp2px(context, bottom));
        } else if (viewGroup instanceof ViewGroup.MarginLayoutParams)
            ((ViewGroup.MarginLayoutParams) viewGroup).setMargins(dp2px(context, left), dp2px(context, top), dp2px(context, right), dp2px(context, bottom));
        else
            throw new IllegalArgumentException("The viewGroup object has to be either a View or a ViewGroup.MarginLayoutParams. Found " + viewGroup.getClass().getSimpleName() + " instead.");
    }

    public static void setPaddings(ViewGroup viewGroup, Context context, int left, int top, int right, int bottom) {
        viewGroup.setPadding(dp2px(context, left), dp2px(context, top), dp2px(context, right), dp2px(context, bottom));
    }

    public static int dp2px(Context context, float dp) {
        return dp2px(context, (int) dp);
    }

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int dp2px(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static ViewGroup getParent(View view) {
        return (ViewGroup)view.getParent();
    }

    public static void removeView(View view) {
        ViewGroup parent = getParent(view);
        if(parent != null) {
            parent.removeView(view);
        }
    }

    public static void findViewWithTagAndChangeColor(View view, String tagContains, int color) {
        if (view == null) {
            return;
        }

        if (view instanceof ViewGroup viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);

                checkTagAndChangeColor(child, tagContains, color);

                if (child instanceof ViewGroup) {
                    findViewWithTagAndChangeColor((ViewGroup) child, tagContains, color);
                }
            }
        } else {
            checkTagAndChangeColor(view, tagContains, color);
        }
    }

    public static void recursivelyChangeViewColor(View v, int color) {
        if (v instanceof ViewGroup vg) {
            for (int i = 0; i < vg.getChildCount(); i++) {
                recursivelyChangeViewColor(vg.getChildAt(i), color);
            }
        } else {
            changeViewColor(v, color);
        }
    }

    private static void checkTagAndChangeColor(View view, String tagContains, int color) {
        Object tagObject = view.getTag();
        if (tagObject != null && tagObject.toString().toLowerCase().contains(tagContains)) {
            changeViewColor(view, color);
        }
    }


    public static void findViewWithTagAndChangeBackgroundColor(ViewGroup parent, String tagContains, int color, int radius) {
        if (parent == null || parent.getChildCount() == 0) {
            return;
        }

        Object tagParent = parent.getTag();
        if (tagParent != null && tagParent.toString().contains(tagContains)) {
            changeBackgroundColor(parent, color, radius);
        }

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            Object tagObject = child.getTag();
            if (tagObject != null && tagObject.toString().contains(tagContains)) {
                changeBackgroundColor(child, color, radius);
            }

            if (child instanceof ViewGroup) {
                findViewWithTagAndChangeBackgroundColor((ViewGroup) child, tagContains, color, radius);
            }
        }
    }

    private static void changeViewColor(View view, int color) {
        if (view instanceof TextView textView) {
            textView.setTextColor(color);

            Drawable[] drawablesRelative = textView.getCompoundDrawablesRelative();
            for (Drawable drawable : drawablesRelative) {
                if (drawable != null) {
                    drawable.mutate();
                    drawable.setTint(color);
                    drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                }
            }

            Drawable[] drawables = textView.getCompoundDrawables();
            for (Drawable drawable : drawables) {
                if (drawable != null) {
                    drawable.mutate();
                    drawable.setTint(color);
                    drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                }
            }
        } else if (view instanceof ImageView imageView) {
            imageView.setColorFilter(color);
        } else if (view instanceof ViewGroup viewGroup) {
            viewGroup.setBackgroundTintList(ColorStateList.valueOf(color));
        } else if (view instanceof ProgressBar progressBar) {
            progressBar.setProgressTintList(ColorStateList.valueOf(color));
            progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            view.getBackground().mutate().setTint(color);
        }
    }

    private static void changeBackgroundColor(View view, int color, int radius) {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{color, color});
        gradientDrawable.setCornerRadius(radius);
        if (view instanceof TextView textView) {
            textView.setBackground(gradientDrawable);
        } else if (view instanceof ImageView imageView) {
            imageView.setBackground(gradientDrawable);
        } else if (view instanceof ViewGroup viewGroup) {
            viewGroup.setBackground(gradientDrawable);
        } else if (view instanceof ProgressBar progressBar) {
            progressBar.setBackground(gradientDrawable);
        } else {
            view.getBackground().mutate().setTint(color);
        }
    }

    public static void applyFontRecursively(ViewGroup viewGroup, Typeface typeface) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                applyFontRecursively((ViewGroup) child, typeface);
            } else if (child instanceof TextView textView) {
                textView.setTypeface(typeface);
            }
        }
    }

    public static void applyTextMarginRecursively(ViewGroup viewGroup, int topMargin) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                applyTextMarginRecursively((ViewGroup) child, topMargin);
            } else if (child instanceof TextView) {
                ViewGroup.LayoutParams params = child.getLayoutParams();
                if (params instanceof LinearLayout.LayoutParams linearParams) {
                    linearParams.topMargin += topMargin;
                    child.setLayoutParams(linearParams);
                } else if (params instanceof FrameLayout.LayoutParams frameParams) {
                    frameParams.topMargin += topMargin;
                    child.setLayoutParams(frameParams);
                } else if (params instanceof RelativeLayout.LayoutParams relativeParams) {
                    relativeParams.topMargin += topMargin;
                    child.setLayoutParams(relativeParams);
                } else {
                    log("Invalid params: " + params);
                }
            }
        }
    }

    public static void applyTextScalingRecursively(ViewGroup viewGroup, float scaleFactor) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                applyTextScalingRecursively((ViewGroup) child, scaleFactor);
            } else if (child instanceof TextView textView) {
                float originalSize = textView.getTextSize();
                float newSize = originalSize * scaleFactor;
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
            }
        }
    }

    public static void applyTextSizeRecursively(ViewGroup viewGroup, int textSize) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                applyTextSizeRecursively((ViewGroup) child, textSize);
            } else if (child instanceof TextView textView) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            }
        }
    }

    public static void setTextRecursively(ViewGroup viewGroup, String text) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                setTextRecursively((ViewGroup) child, text);
            } else if (child instanceof TextView textView) {
                textView.setText(text);
            }
        }
    }


    public static int getColorResCompat(Context context, @AttrRes int id) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(id, typedValue, false);
        TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{id});
        @ColorInt int color = arr.getColor(0, -1);
        arr.recycle();
        return color;
    }

    public static void loadLottieAnimationView(
            Context appContext,
            Class<?> lottieAnimationViewClass,
            View parent,
            Integer styleIndex) {
        if (!(parent instanceof ViewGroup) ||
                parent.findViewWithTag("lottie") == null ||
                (lottieAnimationViewClass == null && styleIndex == null)) {
            return;
        }

        boolean isXposedMode = true;
        try {
            Xprefs.getInt(LOCKSCREEN_CLOCK_STYLE, 0);
        } catch (Throwable ignored) {
            if (styleIndex == null) {
                throw new IllegalStateException("Parameter \"styleIndex\" cannot be null");
            }
            isXposedMode = false;
        }
        String rawResName = "lottie_lockscreen_clock_" + styleIndex;

        Log.d("Oxygen Customizer Loading Lottie", "Loading Lottie Animation: " + rawResName + " (Style: " + styleIndex + ")");

        Object lottieAnimView;
        if (isXposedMode) {
            if (lottieAnimationViewClass == null) {
                throw new IllegalStateException("Parameter \"lottieAnimationViewClass\" cannot be null");
            }
            try {
                lottieAnimView = lottieAnimationViewClass.getConstructor(Context.class).newInstance(appContext);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            lottieAnimView = new LottieAnimationView(appContext);
        }

        LinearLayout.LayoutParams animationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        animationParams.gravity = Gravity.CENTER;

        int anim = appContext.getResources().getIdentifier(
                rawResName,
                "raw",
                BuildConfig.APPLICATION_ID
        );

        if (anim == 0x0) {
            if (isXposedMode) {
                log("Oxygen Customizer - " + ViewHelper.class.getSimpleName() + ": " + rawResName + " not found");
            } else {
                Log.w(ViewHelper.class.getSimpleName(), rawResName + " not found");
            }
            return;
        }

        InputStream rawRes = appContext.getResources().openRawResource(anim);

        if (isXposedMode) {
            try {
                callMethod(lottieAnimView, "setLayoutParams", animationParams);
                callMethod(lottieAnimView, "setAnimation", rawRes, "cacheKey_" + styleIndex);
                callMethod(lottieAnimView, "setRepeatCount", LottieDrawable.INFINITE);
                callMethod(lottieAnimView, "setScaleType", ImageView.ScaleType.FIT_CENTER);
                callMethod(lottieAnimView, "setAdjustViewBounds", true);
                callMethod(lottieAnimView, "enableMergePathsForKitKatAndAbove", true);
                callMethod(lottieAnimView, "playAnimation");
            } catch (Throwable ignored) {}
        } else {
            LottieAnimationView lottieAnimationView = (LottieAnimationView) lottieAnimView;
            lottieAnimationView.setLayoutParams(animationParams);
            lottieAnimationView.setAnimation(rawRes, "cacheKey_" + styleIndex);
            lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
            lottieAnimationView.setRenderMode(RenderMode.HARDWARE);
            lottieAnimationView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            lottieAnimationView.setAdjustViewBounds(true);
            lottieAnimationView.enableMergePathsForKitKatAndAbove(true);
            lottieAnimationView.playAnimation();
        }

        LinearLayout lottieContainer = parent.findViewWithTag("lottie");
        lottieContainer.setGravity(Gravity.CENTER);

        if (isXposedMode) {
            try {
                callMethod(lottieContainer, "addView", lottieAnimView);
            } catch (Throwable ignored) {}
        } else {
            lottieContainer.addView((LottieAnimationView) lottieAnimView);
        }

    }

    public static View findViewWithTag(View view, String tag) {
        if (view == null) {
            return null;
        }

        if (view instanceof ViewGroup viewGroup) {
            if (viewGroup.getTag() != null && viewGroup.getTag().toString().contains(tag)) {
                return viewGroup;
            }
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);

                View result = findViewWithTag(child, tag);
                if (result != null) {
                    return result;
                }
            }
        } else {
            if (view.getTag() != null && view.getTag().toString().contains(tag)) {
                return view;
            }
        }
        return null;
    }

    public static LayerDrawable getChip(int gradientOrientation, int[] colors, int strokeWidth, int strokeColor, float[] cornerRadii) {
        GradientDrawable gradient = new GradientDrawable();
        gradient.setShape(GradientDrawable.RECTANGLE);
        gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        GradientDrawable.Orientation orientation = switch (gradientOrientation) {
            case 1 -> GradientDrawable.Orientation.TOP_BOTTOM;
            case 2 -> GradientDrawable.Orientation.TL_BR;
            case 3 -> GradientDrawable.Orientation.TR_BL;
            default -> GradientDrawable.Orientation.LEFT_RIGHT;
        };
        gradient.setOrientation(orientation);
        gradient.setColors(colors);
        gradient.setStroke(strokeWidth, strokeColor);
        if (cornerRadii != null) {
            gradient.setCornerRadii(cornerRadii);
        } else {
            gradient.setCornerRadius(0);
        }

        return new LayerDrawable(new Drawable[]{gradient});
    }

    public static Bitmap getHumidityProgress(
            Context context,
            Context appContext,
            int percentage,
            @Nullable String text,
            int textSizePx,
            @Nullable String tf,
            @ColorInt int progressColor,
            @ColorInt int textColor
    ) {

        Drawable baseDrawable = ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.ic_humidity_percentage, context.getTheme()).mutate();
        Drawable backgroundDrawable = baseDrawable.getConstantState().newDrawable().mutate();
        backgroundDrawable.setTint(0xFFB0B0B0);
        Drawable foregroundDrawable = baseDrawable.getConstantState().newDrawable().mutate();
        foregroundDrawable.setTint(progressColor);
        ClipDrawable clipDrawable = new ClipDrawable(foregroundDrawable, android.view.Gravity.BOTTOM, ClipDrawable.VERTICAL);
        Drawable[] layers = new Drawable[]{backgroundDrawable, clipDrawable};
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        int level = (percentage * 100);
        clipDrawable.setLevel(level);

        int width = 400;
        int height = 400;
        int padding = dp2px(10, context);
        TextPaint mTextPaint = new TextPaint();
        mTextPaint.setTextSize(dp2px(context, textSizePx));
        mTextPaint.setColor(textColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (tf != null) {
            mTextPaint.setTypeface(Typeface.create(tf, Typeface.BOLD));
        } else {
            mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }
        int size = (int) (bitmap.getWidth() * 0.6);
        int left = (bitmap.getWidth() - size) / 2;
        int right = left + size;
        int bottom = padding + size;
        layerDrawable.setBounds(left, padding, right, bottom);
        layerDrawable.draw(canvas);

        mTextPaint.setTextSize(dp2px(context, textSizePx));
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float textY = padding + size + (Math.abs(fontMetrics.ascent) / 2);
        canvas.drawText(text, (float) bitmap.getWidth() / 2, textY, mTextPaint);
        return bitmap;
    }

}
