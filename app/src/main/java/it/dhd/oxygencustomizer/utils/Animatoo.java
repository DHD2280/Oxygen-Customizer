package it.dhd.oxygencustomizer.utils;

import android.app.Activity;
import android.content.Context;

import it.dhd.oxygencustomizer.R;

public class Animatoo {

    public static void animateSlideLeft(Context context) {
        ((Activity) context).overridePendingTransition(
                R.anim.animate_slide_left_enter,
                R.anim.animate_slide_left_exit
        );
    }
}
