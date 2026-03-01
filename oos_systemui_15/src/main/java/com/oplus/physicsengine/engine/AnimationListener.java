package com.oplus.physicsengine.engine;

public interface AnimationListener {
    default void onAnimationCancel(BaseBehavior baseBehavior) {}

    void onAnimationEnd(BaseBehavior baseBehavior);

    default void onAnimationStart(BaseBehavior baseBehavior) {}
}
