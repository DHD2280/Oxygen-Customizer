package it.dhd.oxygencustomizer.xposed.utils;

import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class ArcProgressWidget {

    public static Bitmap generateBitmap(Context context, int percentage, String textInside, int textInsideSizePx, @Nullable String textBottom, int textBottomSizePx, @Nullable String tf) {
        return generateBitmap(context, percentage, 100, textInside, textInsideSizePx, null, 28, textBottom, textBottomSizePx, tf, Color.WHITE, Color.WHITE);
    }

    /**
     * Generate a bitmap with a circular progress bar.
     * @param context The context to use for resources.
     * @param percentage The percentage of the progress (0-100).
     * @param maxPercentage The maximum percentage (default is 100).
     * @param textInside The text to display inside the circle.
     * @param textInsideSizePx The text size in pixels.
     * @param iconDrawable The drawable icon to display inside the circle (optional).
     * @param iconSizePx The icon size in pixels.
     * @param tf The typeface to use for the text (optional).
     * @param progressColor The color of the progress arc.
     * @param textColor The color of the text.
     * @return A bitmap with the circular progress bar.
     */
    public static Bitmap generateBitmap(Context context, int percentage, int maxPercentage, String textInside, int textInsideSizePx, @Nullable Drawable iconDrawable, int iconSizePx, @Nullable String tf, @ColorInt int progressColor, @ColorInt int textColor) {
        return generateBitmap(context, percentage, maxPercentage, textInside, textInsideSizePx, iconDrawable, iconSizePx, "Usage", 28, tf, progressColor, textColor);
    }

    public static Bitmap generateBitmap(Context context, int percentage, String textInside, int textInsideSizePx, @Nullable Drawable iconDrawable, int iconSizePx, @Nullable String tf, @ColorInt int progressColor, @ColorInt int textColor) {
        return generateBitmap(context, percentage, 100, textInside, textInsideSizePx, iconDrawable, iconSizePx, "Usage", 28, tf, progressColor, textColor);
    }

    public static Bitmap generateBitmap(Context context,
                                        int percentage,
                                        int maxPercentage,
                                        String textInside,
                                        int textInsideSizePx,
                                        @Nullable Drawable iconDrawable,
                                        int iconSizePx,
                                        @Nullable String textBottom,
                                        int textBottomSizePx,
                                        @Nullable String tf,
                                        @ColorInt int progressColor,
                                        @ColorInt int textColor) {
        int width = 400;
        int height = 400;
        int stroke = 40;
        int padding = 5;
        int minAngle = 135;
        int maxAngle = 275;
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(stroke);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(dp2px(context, textInsideSizePx));
        mTextPaint.setColor(textColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.density = context.getResources().getDisplayMetrics().density;
        final RectF arc = new RectF();
        arc.set(((float) stroke / 2) + padding, ((float) stroke / 2) + padding, width - padding - ((float) stroke / 2), height - padding - ((float) stroke / 2));
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        paint.setColor(Color.argb(75, 255, 255, 255));
        canvas.drawArc(arc, minAngle, maxAngle, false, paint);
        paint.setColor(progressColor);
        canvas.drawArc(arc, minAngle, ((float) maxAngle / maxPercentage) * percentage, false, paint);
        if (tf != null) {
            mTextPaint.setTypeface(Typeface.create(tf, Typeface.BOLD));
        } else {
            mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }
        canvas.drawText(textInside, (float) bitmap.getWidth() / 2, (bitmap.getHeight() - mTextPaint.ascent() * 0.7f) / 2, mTextPaint);
        if (iconDrawable != null) {
            int size = dp2px(context, iconSizePx);
            int left = (bitmap.getWidth() - size) / 2;
            int top = bitmap.getHeight() - (int) (size / 1.3) - (stroke + padding) - dp2px(context, 4);
            int right = left + size;
            int bottom = top + size;
            iconDrawable.setBounds(left, top, right, bottom);
            iconDrawable.setColorFilter(new BlendModeColorFilter(Color.WHITE, BlendMode.SRC_IN));
            iconDrawable.draw(canvas);
        } else if (textBottom != null) {
            mTextPaint.setTextSize(dp2px(context, textBottomSizePx));
            canvas.drawText(textBottom, (float) bitmap.getWidth() / 2, bitmap.getHeight() - (stroke + padding), mTextPaint);
        }
        return bitmap;
    }

}
