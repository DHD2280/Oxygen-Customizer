package android.view;

import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ViewRootImpl implements ViewParent,
        ThreadedRenderer.DrawCallbacks,
        AttachedSurfaceControl {

    @Nullable
    @Override
    public SurfaceControl.Transaction buildReparentTransaction(@NonNull SurfaceControl child) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean applyTransactionOnDraw(@NonNull SurfaceControl.Transaction t) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void onPreDraw(RecordingCanvas canvas) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void onPostDraw(RecordingCanvas canvas) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void requestLayout() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean isLayoutRequested() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void requestTransparentRegion(View child) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void invalidateChild(View child, Rect r) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect r) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public ViewParent getParent() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void recomputeViewAttributes(View child) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void clearChildFocus(View child) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean getChildVisibleRect(View child, Rect r, Point offset) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public View focusSearch(View v, int direction) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public View keyboardNavigationClusterSearch(View currentCluster, int direction) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void bringChildToFront(View child) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void focusableViewAvailable(View v) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean showContextMenuForChild(View originalView, float x, float y) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void createContextMenu(ContextMenu menu) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void childDrawableStateChanged(@NonNull View child) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean requestChildRectangleOnScreen(@NonNull View child, Rect rectangle, boolean immediate) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean requestSendAccessibilityEvent(@NonNull View child, AccessibilityEvent event) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void childHasTransientStateChanged(@NonNull View child, boolean hasTransientState) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void requestFitSystemWindows() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public ViewParent getParentForAccessibility() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void notifySubtreeAccessibilityStateChanged(@NonNull View child, @NonNull View source, int changeType) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean canResolveLayoutDirection() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean isLayoutDirectionResolved() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public int getLayoutDirection() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean canResolveTextDirection() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean isTextDirectionResolved() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public int getTextDirection() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean canResolveTextAlignment() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean isTextAlignmentResolved() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public int getTextAlignment() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void onStopNestedScroll(@NonNull View target) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public boolean onNestedPrePerformAccessibilityAction(@NonNull View target, int action, @Nullable Bundle arguments) {
        throw new UnsupportedOperationException("Stub!");
    }

    public interface ConfigChangedCallback {

        /** Notifies about global config change. */
        void onConfigurationChanged(Configuration globalConfig);
    }
}
