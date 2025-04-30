package com.android.systemui.plugins;

import android.graphics.Rect;
import android.view.View;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public interface DarkIconDispatcher {

    public static final int DEFAULT_ICON_TINT = -855638017;
    public static final int DEFAULT_INVERSE_ICON_TINT = -16777216;
    public static final int VERSION = 2;
    public static final Rect sTmpRect = new Rect();
    public static final int[] sTmpInt2 = new int[2];

    public interface DarkReceiver {
        public static final int VERSION = 3;

        void onDarkChanged(ArrayList<Rect> arrayList, float f2, int i2);

        default void onDarkChangedWithContrast(ArrayList<Rect> arrayList, int i2, int i3) {
        }
    }

    void addDarkReceiver(DarkReceiver darkReceiver);

    void applyDark(DarkReceiver darkReceiver);

    void removeDarkReceiver(DarkReceiver darkReceiver);

    void setIconsDarkArea(ArrayList<Rect> arrayList);

    static int getTint(Collection<Rect> collection, View view, int i2) {
        throw new UnsupportedOperationException("Stub!");
    }

    static int getInverseTint(Collection<Rect> collection, View view, int i2) {
        throw new UnsupportedOperationException("Stub!");
    }

    static boolean isInAreas(Collection<Rect> collection, View view) {
        throw new UnsupportedOperationException("Stub!");
    }

    static boolean isInAreas(Collection<Rect> collection, Rect rect) {
        throw new UnsupportedOperationException("Stub!");
    }

    static boolean isInArea(Rect rect, Rect rect2) {
        throw new UnsupportedOperationException("Stub!");
    }

    static boolean isInArea(Rect rect, View view) {
        throw new UnsupportedOperationException("Stub!");
    }
}
