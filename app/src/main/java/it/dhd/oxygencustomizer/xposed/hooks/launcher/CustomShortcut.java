package it.dhd.oxygencustomizer.xposed.hooks.launcher;

import static it.dhd.oxygencustomizer.xposed.hooks.launcher.FakeResources.getFakeIdString;
import static it.dhd.oxygencustomizer.xposed.hooks.launcher.FakeResources.getIdDivider;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.popup.SystemShortcut;
import com.android.quickstep.TaskShortcutFactory;
import com.android.quickstep.views.TaskView;
import com.oplus.quickstep.shortcuts.OplusGroupDividerShortcut;

import it.dhd.oxygencustomizer.xposed.utils.launcher.KillShortcut;

public final class CustomShortcut {


    public static final TaskShortcutFactory KILL_SHORTCUT = new TaskShortcutFactory() {
        @Override
        public SystemShortcut getShortcut(BaseDraggingActivity baseDraggingActivity, TaskView.TaskIdAttributeContainer taskIdAttributeContainer) {
            return new KillShortcut(baseDraggingActivity, taskIdAttributeContainer);
        }
    };

    public static final TaskShortcutFactory DIVIDER = new TaskShortcutFactory() {
        @Override
        public SystemShortcut getShortcut(BaseDraggingActivity baseDraggingActivity, TaskView.TaskIdAttributeContainer taskIdAttributeContainer) {
            return new OplusGroupDividerShortcut(getIdDivider(), getFakeIdString(), baseDraggingActivity, taskIdAttributeContainer.getItemInfo(), taskIdAttributeContainer.getTaskView());
        }
    };

}
