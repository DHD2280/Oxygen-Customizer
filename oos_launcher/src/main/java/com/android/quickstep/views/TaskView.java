package com.android.quickstep.views;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.android.launcher3.model.data.WorkspaceItemInfo;
import com.oplus.basecommon.util.ViewPool;

public class TaskView extends FrameLayout implements ViewPool.Reusable {


    public TaskView(@NonNull Context context) {
        super(context);
    }

    @Override
    public void onRecycle() {
        throw new UnsupportedOperationException("STUB");
    }

    public class TaskIdAttributeContainer {


        public WorkspaceItemInfo getItemInfo() {
            throw new UnsupportedOperationException("STUB");
        }

        public TaskView getTaskView() {
            return TaskView.this;
        }

    }

}
