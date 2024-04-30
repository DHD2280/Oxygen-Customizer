package it.dhd.oxygencustomizer.xposed.utils.viewpager;

import android.view.View;

public class RotateAboutBottomTransformer extends ABaseTransformer {
    protected void onTransform(View view, float position) {
        final float width = view.getWidth(), height = view.getHeight();
        view.setPivotX(width / 2);
        view.setPivotY(height);
        view.setRotation(position * 20);
    }
}
