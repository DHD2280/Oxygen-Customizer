package it.dhd.oxygencustomizer.xposed.utils.viewpager;

import android.view.View;

public class ZoomInTransformer extends ABaseTransformer {
    protected void onTransform(View view, float position) {
        float f = 0.0f;
        float scale = position < 0.0f ? position + 1.0f : Math.abs(1.0f - position);
        view.setScaleX(scale);
        view.setScaleY(scale);
        view.setPivotX(((float) view.getWidth()) * 0.5f);
        view.setPivotY(((float) view.getHeight()) * 0.5f);
        if (position >= -1.0f && position <= 1.0f) {
            f = 1.0f - (scale - 1.0f);
        }
        view.setAlpha(f);
    }
}
