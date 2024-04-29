package it.dhd.oxygencustomizer.xposed.utils.viewpager;


import android.view.View;

public class FadeTransformer extends ABaseTransformer {
    public void onTransform(View view, float position) {
        float f = 0.0f;
        if (position < 0.0f) {
            f = (float) view.getWidth();
        }
        view.setPivotX(f);
        view.setPivotY(((float) view.getHeight()) * 0.5f);
        view.setRotationY(20.0f * position);
        float normalizedPosition = Math.abs(Math.abs(position) - 1.0f);
        view.setScaleX((float) (((double) (normalizedPosition / 2.0f)) + 0.5d));
        view.setScaleY((float) (((double) (normalizedPosition / 2.0f)) + 0.5d));
    }

    public boolean isPagingEnabled() {
        return true;
    }
}
