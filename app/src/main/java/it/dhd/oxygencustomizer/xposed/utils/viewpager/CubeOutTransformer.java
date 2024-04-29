package it.dhd.oxygencustomizer.xposed.utils.viewpager;

import android.view.View;

public class CubeOutTransformer extends ABaseTransformer {
    protected void onTransform(View view, float position) {
        float f = 0.0f;
        if (position < 0.0f) {
            f = (float) view.getWidth();
        }
        view.setPivotX(f);
        view.setPivotY(((float) view.getHeight()) * 0.5f);
        view.setRotationY(90.0f * position);
    }

    public boolean isPagingEnabled() {
        return true;
    }
}
