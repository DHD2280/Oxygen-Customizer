package it.dhd.oxygencustomizer.xposed.views.edgelight.animators;

import android.graphics.DashPathEffect;

import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightView;

public class EdgeDottedLineAnimator extends EdgeLineAnimator {

    public EdgeDottedLineAnimator(EdgeLightView edgeLightView) {
        super(edgeLightView);
        mEdgePaint.setPathEffect(new DashPathEffect(new float[]{40, 40}, 0));
        mEdgeBlurPaint.setPathEffect(new DashPathEffect(new float[]{40, 40}, 0));
    }

}
