package com.oplus.basecommon.util;

import android.view.View;

public class ViewPool<T extends View & ViewPool.Reusable> {

    public interface Reusable {
        void onRecycle();
    }


}
