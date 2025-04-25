package com.android.systemui.plugins.qs;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public abstract class QSIconView extends ViewGroup {
    public static final int VERSION = 1;

    public abstract void disableAnimation();

    public abstract View getIconView();

    public abstract void setIcon(QSTile.State state, boolean z);

    public void setIconBackgroundColor(int i2) {}

    public abstract boolean shouldAnimate(ImageView imageView);

    public void tintBgColor(boolean z, int i2) {}

    public void updateIconBackground(boolean z, int i2) {}

    public void updateIconColorImmediately() {}

    public QSIconView(Context context) {
        super(context);
    }
}