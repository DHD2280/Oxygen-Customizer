package it.dhd.oxygencustomizer.xposed.utils.viewpager;


import android.view.View;

public class RotateUpTransformer extends ABaseTransformer {
    private static final float ROT_MOD = -15.0f;

    protected void onTransform(View view, float position) {
        float rotation = ROT_MOD * position;
        view.setPivotX(0.5f * ((float) view.getWidth()));
        view.setPivotY(0.0f);
        view.setTranslationX(0.0f);
        view.setRotation(rotation);
    }

    protected boolean isPagingEnabled() {
        return true;
    }
}
