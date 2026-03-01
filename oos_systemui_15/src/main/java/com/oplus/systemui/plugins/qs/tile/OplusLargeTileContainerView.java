package com.oplus.systemui.plugins.qs.tile;

import android.content.Context;
import android.view.View;

public class OplusLargeTileContainerView extends OplusTileContainerView {

    public OplusLargeTileContainerView(Context context) {
        super(context);
    }

    public class ViewCellInfo {
        public int spanX;
        public int spanY;

        public View view;

        public int x;

        public int y;

        public ViewCellInfo(View view, int x, int y, int spanx, int spany) {
            this.view = view;
            this.x = x;
            this.y = y;
            this.spanX = spanx;
            this.spanY = spany;
        }
    }


}
