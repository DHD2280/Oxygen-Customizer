package com.oplus.systemui.qs.base.dialog;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.systemui.plugins.qs.QSTile;

import java.util.List;

/**
 * OplusQSDetailItems
 * <p>
 * This class is used on OOS15 850+
 */
public class OplusQSDetailItems extends FrameLayout {

    public OplusQSDetailItems(Context context, AttributeSet attributeSet) throws Resources.NotFoundException {
        super(context, attributeSet);
        throw new UnsupportedOperationException("Stub!");
    }

    public static OplusQSDetailItems convertOrInflate(Context context, ViewGroup viewGroup, boolean z) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void setListItems(int i2, List list, boolean z) {
        throw new UnsupportedOperationException("Stub!");
    }

    public void setCallback(Callback callback) {
        throw new UnsupportedOperationException("Stub!");
    }


    public final static class Item {
        public CharSequence action;
        public boolean active;
        public CharSequence frequency;
        public QSTile.Icon icon;
        public int iconResId;
        public CharSequence line1;
        public CharSequence line2;
        public CharSequence line3;
        public Object tag;
        public boolean tint;
        public String tintText;
        public int viewType = 0;
        public boolean activeLabel = false;
        public boolean isSaved = false;
        public boolean isCategoryFirstItem = false;
        public boolean isCategoryLastItem = false;
        public boolean select = false;
    }

    public interface Callback {
        void onDetailItemClick(Item item);

        void onDetailItemDisconnect(Item item);

        default void onRefresh() {
        }
    }


}
