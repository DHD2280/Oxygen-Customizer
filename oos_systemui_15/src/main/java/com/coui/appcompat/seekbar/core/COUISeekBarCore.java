package com.coui.appcompat.seekbar.core;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsSeekBar;

// COS17
public class COUISeekBarCore extends AbsSeekBar {

    public COUISeekBarCore(Context context) {
        super(context);
        throw new UnsupportedOperationException("Stub!");
    }

    public COUISeekBarCore(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        throw new UnsupportedOperationException("Stub!");
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        throw new UnsupportedOperationException("Stub!");
    }

    public void setMoveType(int moveType) {
        throw new UnsupportedOperationException("Stub!");
    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(COUISeekBarCore seekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(COUISeekBarCore seekBar);

        void onStopTrackingTouch(COUISeekBarCore seekBar);
    }
}
