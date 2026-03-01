package com.oplus.systemui.statusbar.notification.row.menupanel.popupmenu;

import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public abstract class NotificationPopupMenuOperation {

    public abstract void doOperation(Operation operation, ExpandableNotificationRow expandableNotificationRow);


    public static final class Operation {

        int id;

        public Operation(String str, int i2) {
            this.id = i2;
        }

        public int getId() {
            return id;
        }

    }


}
