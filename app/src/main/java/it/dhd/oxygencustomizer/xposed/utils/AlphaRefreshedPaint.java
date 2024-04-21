package it.dhd.oxygencustomizer.xposed.utils;

import android.graphics.Paint;

public class AlphaRefreshedPaint extends Paint {
    public AlphaRefreshedPaint(int flag) {
        super(flag);
    }

    @Override
    public void setColor(int color)
    {
        int alpha = getAlpha();

        super.setColor(color);
        setAlpha(alpha);
    }
}