package it.dhd.oxygencustomizer.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.utils.QsStylePackUtil;

/**
 * Three tiles drawn with a style's own values, so a pack needs no artwork to show up
 * correctly in the list.
 */
public class QsStylePreviewView extends View {

    private static final float TILE_DP = 52f;
    private static final float GAP_DP = 10f;
    private static final float RADIUS_DP = 18f;

    private final Paint mFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mEdge = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mRect = new RectF();

    private QsStylePackUtil.Values mValues;

    public QsStylePreviewView(Context context) {
        this(context, null);
    }

    public QsStylePreviewView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mEdge.setStyle(Paint.Style.STROKE);
    }

    public void setValues(@Nullable QsStylePackUtil.Values values) {
        mValues = values;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = (int) (TILE_DP * density() + 0.5f);
        setMeasuredDimension(resolveSize((int) ((TILE_DP * 3 + GAP_DP * 2) * density() + 0.5f), widthMeasureSpec),
                resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (mValues == null) return;

        float density = density();
        float tile = TILE_DP * density;
        float gap = GAP_DP * density;
        float radius = RADIUS_DP * density;
        float total = tile * 3 + gap * 2;
        float left = (getWidth() - total) / 2f;
        float top = (getHeight() - tile) / 2f;

        boolean night = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int inactiveFill = night ? 0x1FFFFFFF : 0x14000000;
        int activeFill = getResources().getColor(R.color.colorAccent, getContext().getTheme());

        float width = Math.max(1f, mValues.borderWidthDp * density);
        float inset = mValues.borderInsetDp * density;
        mEdge.setStrokeWidth(width);

        for (int i = 0; i < 3; i++) {
            boolean active = i == 1;
            mRect.set(left + i * (tile + gap), top, left + i * (tile + gap) + tile, top + tile);

            mFill.setColor(active ? activeFill : inactiveFill);
            canvas.drawRoundRect(mRect, radius, radius, mFill);

            if (active && !mValues.borderActive) continue;

            int edgeTop = mValues.color(night);
            int edgeBottom = mValues.colorBottom(night);
            if (Color.alpha(edgeBottom) != 0) {
                mEdge.setShader(new LinearGradient(0, mRect.top, 0, mRect.bottom,
                        edgeTop, edgeBottom, Shader.TileMode.CLAMP));
                mEdge.setColor(Color.WHITE);
            } else {
                mEdge.setShader(null);
                mEdge.setColor(edgeTop);
            }

            mRect.inset(inset, inset);
            canvas.drawRoundRect(mRect, radius - inset, radius - inset, mEdge);
        }
    }

    private float density() {
        return getResources().getDisplayMetrics().density;
    }
}
