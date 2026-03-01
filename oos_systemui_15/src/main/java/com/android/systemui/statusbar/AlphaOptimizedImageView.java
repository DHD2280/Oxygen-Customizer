package com.android.systemui.statusbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class AlphaOptimizedImageView extends ImageView {

    public AlphaOptimizedImageView(Context context) {
        this(context, null);
    }

    @Override
    public final boolean hasOverlappingRendering() {
        throw new UnsupportedOperationException("Stub!");
    }

    public AlphaOptimizedImageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AlphaOptimizedImageView(Context context, AttributeSet attributeSet, int i2) {
        this(context, attributeSet, i2, 0);
    }

    public AlphaOptimizedImageView(Context context, AttributeSet attributeSet, int i2, int i3) {
        super(context, attributeSet, i2, i3);
    }
}