package com.coui.appcompat.list;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.coui.appcompat.scrollbar.COUIScrollBar;

public class COUIListView extends ListView implements COUIScrollBar.COUIScrollable {

    public COUIListView(Context context) {
        super(context);
    }

    @Override
    public COUIScrollBar getCOUIScrollDelegate() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public View getCOUIScrollableView() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void setNewCOUIScrollDelegate(COUIScrollBar cOUIScrollBar) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public int superComputeVerticalScrollExtent() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public int superComputeVerticalScrollOffset() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public int superComputeVerticalScrollRange() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void superOnTouchEvent(MotionEvent motionEvent) {
        throw new UnsupportedOperationException("Stub!");
    }
}
