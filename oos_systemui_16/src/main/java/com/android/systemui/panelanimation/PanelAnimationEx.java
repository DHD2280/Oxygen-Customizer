package com.android.systemui.panelanimation;

import com.android.systemui.statusbar.policy.CallbackController;

public class PanelAnimationEx {

    public interface ExpandedFraction extends CallbackController {
    }

    public interface ExpandedFractionChangeListener {
        void onFractionChanged(float fraction);

        default void onBlurFractionChange() {
        }

        default void onFractionEnd(float fraction) {
        }
    }

    public boolean getUseLayeredAnimation() {
        throw new UnsupportedOperationException("Stub!");
    }

    public ExpandedFraction getPanelExpandFraction() {
        throw new UnsupportedOperationException("Stub!");
    }


}
