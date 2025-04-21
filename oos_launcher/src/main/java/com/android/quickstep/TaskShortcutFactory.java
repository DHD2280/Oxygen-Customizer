package com.android.quickstep;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.popup.SystemShortcut;
import com.android.quickstep.views.TaskView;

public interface TaskShortcutFactory {

    SystemShortcut getShortcut(BaseDraggingActivity baseDraggingActivity, TaskView.TaskIdAttributeContainer taskIdAttributeContainer);

}
