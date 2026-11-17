package com.android.launcher3.uioverrides.states.blurdrawable;

import com.android.launcher3.model.data.ItemInfo;

/**
 * COS17
 */
public abstract class IBlurBaseController {

    public abstract void recycle();

    public abstract IBlurBaseController setBlurCornerRadius(float radius, boolean isFolder, ItemInfo itemInfo, boolean z);

    public abstract IBlurBaseController setBlurParams(int blurRadius, int colorMode, int blendColor, int mixColor);

    public final void setIgnorePageScrollBlurAnim(boolean ignore) {
        throw new UnsupportedOperationException("Stub!");
    }
}
