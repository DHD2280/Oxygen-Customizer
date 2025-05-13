package it.dhd.oxygencustomizer.xposed.views.statusbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.widget.ImageView;

import com.android.systemui.plugins.DarkIconDispatcher;

import java.util.ArrayList;
import java.util.Collection;

import com.android.systemui.Dependency;

@SuppressLint("AppCompatCustomView")
public class LogoView extends ImageView implements DarkIconDispatcher.DarkReceiver {

    boolean mForceTint = true;

    public LogoView(Context context) {
        super(context);
    }

    @Override
    public void onDarkChanged(ArrayList<Rect> arrayList, float f2, int i2) {
        if (!mForceTint) {
            clearColorFilter();
            return;
        }
        int mTint;
        if (Build.VERSION.SDK_INT >= 35) {
            mTint = DarkIconDispatcher.getTint(((Collection<Rect>) arrayList), this, i2);
        } else {
            mTint = DarkIconDispatcher.getTint(arrayList, this, i2);
        }
        clearColorFilter();
        setColorFilter(mTint);
    }

    public void setForceTint(boolean forceTint) {
        mForceTint = forceTint;
        if (!mForceTint) clearColorFilter();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        DarkIconDispatcher darkIconDispatcher;
        try {
            darkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        } catch (Throwable ignored) {
            darkIconDispatcher = (DarkIconDispatcher) Dependency.sDependency.getDependencyInner(DarkIconDispatcher.class);
        }
        darkIconDispatcher.addDarkReceiver(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        DarkIconDispatcher darkIconDispatcher;
        try {
            darkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        } catch (Throwable ignored) {
            darkIconDispatcher = (DarkIconDispatcher) Dependency.sDependency.getDependencyInner(DarkIconDispatcher.class);
        }
        darkIconDispatcher.removeDarkReceiver(this);
    }

}
