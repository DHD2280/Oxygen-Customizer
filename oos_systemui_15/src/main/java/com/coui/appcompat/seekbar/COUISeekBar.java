package com.coui.appcompat.seekbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.AbsSeekBar;

import com.oplus.physicsengine.engine.AnimationListener;
import com.oplus.physicsengine.engine.AnimationUpdateListener;
import com.oplus.physicsengine.engine.BaseBehavior;

public class COUISeekBar extends AbsSeekBar implements AnimationListener, AnimationUpdateListener {

    public COUISeekBar(Context context) {
        this(context, null);
        throw new UnsupportedOperationException("Stub!");
    }

    public COUISeekBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
        throw new UnsupportedOperationException("Stub!");
    }

    public COUISeekBar(Context context, AttributeSet attributeSet, int i2) {
        this(context, attributeSet, i2, 0);
    }

    public COUISeekBar(Context context, AttributeSet attributeSet, int i2, int i3) {
        super(context, attributeSet, i2, i3);
        throw new UnsupportedOperationException("Stub!");
    }

        @Override
    public void onAnimationEnd(BaseBehavior baseBehavior) {

    }

    @Override
    public void onAnimationUpdate(BaseBehavior baseBehavior) {

    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        throw new UnsupportedOperationException("Stub!");
    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(COUISeekBar cOUISeekBar, int i2, boolean z);

        void onStartTrackingTouch(COUISeekBar cOUISeekBar);

        void onStopTrackingTouch(COUISeekBar cOUISeekBar);
    }

    public void setThumbColor(ColorStateList colorStateList) {
        throw new UnsupportedOperationException("Stub!");
    }

    public void setProgressColor(ColorStateList colorStateList) {
        throw new UnsupportedOperationException("Stub!");
    }

    public void setSeekBarBackgroundColor(ColorStateList colorStateList) {
        throw new UnsupportedOperationException("Stub!");
    }


}
