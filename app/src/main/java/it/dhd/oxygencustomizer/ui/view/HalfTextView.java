package it.dhd.oxygencustomizer.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextClock;

public class HalfTextView extends TextClock {
    /* renamed from: A */
    public final float f19753A;

    /* renamed from: x */
    public final Paint f19754x;

    /* renamed from: y */
    public final Rect f19755y;

    /* renamed from: z */
    public final boolean f19756z;

    public HalfTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, 0);
        this.f19754x = new Paint();
        this.f19755y = new Rect();
        //TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, C0190y.f469O);
        this.f19756z = false;//obtainStyledAttributes.getBoolean(1, false);
        this.f19753A = 0.0f;//obtainStyledAttributes.getDimension(0, 0.0f);
        //obtainStyledAttributes.recycle();
    }

    @Override
    public final void onDraw(Canvas canvas) {
        String m5689v = m5689v();
        Rect rect = this.f19755y;
        int i = rect.left;
        int i2 = rect.bottom;
        boolean z = this.f19756z;
        float f = this.f19753A;
        float height = z ? (rect.height() / 2.0f) + f : -f;
        rect.offset(-rect.left, -rect.top);
        Paint paint = this.f19754x;
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(getCurrentTextColor());
        paint.setTypeface(getTypeface());
        canvas.drawText(m5689v, -i, (rect.bottom - i2) - height, paint);
    }

    @Override
    public final void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        m5689v();
        Rect rect = this.f19755y;
        setMeasuredDimension(rect.width(), (int) (rect.height() / 2.0f));
    }

    /* renamed from: v */
    public final String m5689v() {
        String charSequence = getText().toString();
        int length = charSequence.length();
        Paint paint = this.f19754x;
        paint.setTextSize(getTextSize());
        paint.setTypeface(getTypeface());
        Rect rect = this.f19755y;
        paint.getTextBounds(charSequence, 0, length, rect);
        if (length == 0) {
            rect.right = rect.left;
        }
        return charSequence;
    }

}
