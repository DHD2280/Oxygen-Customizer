package com.coui.appcompat.scrollbar;

import android.view.MotionEvent;
import android.view.View;

public class COUIScrollBar {

    public interface COUIScrollable {
        COUIScrollBar getCOUIScrollDelegate();

        View getCOUIScrollableView();

        void setNewCOUIScrollDelegate(COUIScrollBar cOUIScrollBar);

        int superComputeVerticalScrollExtent();

        int superComputeVerticalScrollOffset();

        int superComputeVerticalScrollRange();

        void superOnTouchEvent(MotionEvent motionEvent);
    }


}
