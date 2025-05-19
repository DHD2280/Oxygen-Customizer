package com.android.launcher3.uioverrides.states.blurdrawable;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import androidx.annotation.NonNull;

public final class LayerBlurDrawable extends LayerDrawable {
    /**
     * Creates a new layer drawable with the list of specified layers.
     *
     * @param layers a list of drawables to use as layers in this new drawable,
     *               must be non-null
     */
    public LayerBlurDrawable(@NonNull Drawable[] layers) {
        super(layers);
    }
}
