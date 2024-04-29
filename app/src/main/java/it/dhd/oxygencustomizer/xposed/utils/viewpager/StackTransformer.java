package it.dhd.oxygencustomizer.xposed.utils.viewpager;


import android.view.View;

public class StackTransformer extends ABaseTransformer {
    protected void onTransform(View view, float position) {
        view.setTranslationX(position >= 0f ? (((float) (-view.getWidth())) * position) : 0f);
    }
}
