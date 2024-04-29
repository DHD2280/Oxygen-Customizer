package it.dhd.oxygencustomizer.xposed.utils.viewpager;

import android.view.View;

public class ForegroundToBackgroundTransformer extends ABaseTransformer {
    protected void onTransform(View view, float position) {
        float f = 1.0f;
        float height = (float) view.getHeight();
        float width = (float) view.getWidth();
        if (position <= 0.0f) {
            f = Math.abs(1.0f + position);
        }
        float scale = min(f, 0.5f);
        view.setScaleX(scale);
        view.setScaleY(scale);
        view.setPivotX(width * 0.5f);
        view.setPivotY(height * 0.5f);
        view.setTranslationX(position > 0.0f ? width * position : (-width) * position * 0.25f);
    }
}
