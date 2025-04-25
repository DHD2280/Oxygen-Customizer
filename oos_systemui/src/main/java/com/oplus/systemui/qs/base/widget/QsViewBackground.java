package com.oplus.systemui.qs.base.widget;

import com.android.systemui.plugins.qs.QSTile;

public interface QsViewBackground {

    default void handleStateChanged(QSTile.State state) {}

    void onBackgroundAttach();

    default void onBackgroundDetach() {}

    default void onClick() {}

    void refreshViewBackground();
}