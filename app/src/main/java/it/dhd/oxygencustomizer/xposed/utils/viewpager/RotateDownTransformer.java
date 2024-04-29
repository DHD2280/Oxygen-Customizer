package it.dhd.oxygencustomizer.xposed.utils.viewpager;

import android.view.View;

public class RotateDownTransformer extends ABaseTransformer {
    private static final float ROT_MOD = -15.0f;

    protected void onTransform(View view, float position) {
        float width = (float) view.getWidth();
        float height = (float) view.getHeight();
        float rotation = ROT_MOD * position * -1.25f;
        view.setPivotX(0.5f * width);
        view.setPivotY(height);
        view.setRotation(rotation);
    }

    protected boolean isPagingEnabled() {
        return true;
    }
}
