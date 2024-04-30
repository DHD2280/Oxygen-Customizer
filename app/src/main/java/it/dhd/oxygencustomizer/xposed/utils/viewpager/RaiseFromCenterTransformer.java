package it.dhd.oxygencustomizer.xposed.utils.viewpager;

import android.view.View;

import androidx.annotation.NonNull;

public class RaiseFromCenterTransformer extends ABaseTransformer {

    protected void onTransform(@NonNull View page, float position) {
        int width = page.getWidth(), height = page.getHeight();

        if (position < -1) {
            page.setTranslationX(0);
        } else if (position < 0) {

            float scale = 1 + position;
            page.setPivotX(width/2);
            page.setPivotY(height/2);
            page.setScaleY(scale);
            page.setScaleX(scale);
//            Counteract the default motion
            page.setTranslationX(-position*width);
        } else if (position < 1) {
            float scale = 1 - position;
            page.setPivotX(width/2);
            page.setPivotY(height/2);
            page.setScaleY(scale);
            page.setScaleX(scale);
//            Counteract the default motion
            page.setTranslationX(-width * position);

        } else {
            page.setTranslationX(0);
        }
    }
}
