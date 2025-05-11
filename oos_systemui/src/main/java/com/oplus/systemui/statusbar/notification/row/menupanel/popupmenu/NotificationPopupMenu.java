package com.oplus.systemui.statusbar.notification.row.menupanel.popupmenu;

import android.graphics.drawable.Drawable;

import com.coui.appcompat.poplist.PopupListItem;

import kotlin.jvm.internal.Intrinsics;

public class NotificationPopupMenu {

    public static final class MenuItem extends PopupListItem {
        public final boolean checkable;
        public final boolean checked;
        public boolean clickAfterDismissEnd;
        public final boolean enable;
        public final Drawable menuIcon;
        public final NotificationPopupMenuOperation.Operation menuOperation;
        public final String menuTitle;
        public final int redDotCount;


        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
        public String toString() {
            throw new UnsupportedOperationException("Stub!");
        }

        public final NotificationPopupMenuOperation.Operation getMenuOperation() {
            throw new UnsupportedOperationException("Stub!");
        }

        public final String getMenuTitle() {
            throw new UnsupportedOperationException("Stub!");
        }

        public final Drawable getMenuIcon() {
            throw new UnsupportedOperationException("Stub!");
        }

        public final boolean getCheckable() {
            throw new UnsupportedOperationException("Stub!");
        }

        public final boolean getChecked() {
            throw new UnsupportedOperationException("Stub!");
        }

        public final int getRedDotCount() {
            throw new UnsupportedOperationException("Stub!");
        }

        public final boolean getEnable() {
            throw new UnsupportedOperationException("Stub!");
        }

        public final boolean getClickAfterDismissEnd() {
            throw new UnsupportedOperationException("Stub!");
        }

        public final void setClickAfterDismissEnd(boolean z) {
            throw new UnsupportedOperationException("Stub!");
        }

        public MenuItem(NotificationPopupMenuOperation.Operation operation, String str, Drawable drawable, boolean z, boolean z2, int i2, boolean z3, boolean z4) {
            throw new UnsupportedOperationException("Stub!");
        }
    }



}
