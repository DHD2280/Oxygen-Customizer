package it.dhd.oxygencustomizer.xposed.utils.viewpager;

import android.view.View;

import androidx.annotation.NonNull;

public class TranslationYTransformer extends ABaseTransformer {
    public static final int TOP_TO_BOTTOM = 1, BOTTOM_TO_TOP = 2;
    public int ANIMATE_TYPE = 2;

    public TranslationYTransformer(){

    }

    public TranslationYTransformer(int ANIMATE_TYPE) {
        this.ANIMATE_TYPE = ANIMATE_TYPE;
    }

    protected void onTransform(@NonNull View page, float position) {

        int width = page.getWidth(), height = page.getHeight();

        if (position <= 0) {
            // This page is way off-screen to the left.
            page.setTranslationX(0);
        } else if (position <= 1) {
//            Log.d("POSITION", position * height + "!!");
            if (ANIMATE_TYPE == TOP_TO_BOTTOM) {
                page.setTranslationY(-position * (height));
            } else if (ANIMATE_TYPE == BOTTOM_TO_TOP) {
                page.setTranslationY(position * (height));
            }
        } else {
            // This page is way off-screen to the right.
            page.setTranslationX(0);

        }
    }
}
