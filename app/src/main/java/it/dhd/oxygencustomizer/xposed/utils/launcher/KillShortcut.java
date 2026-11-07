package it.dhd.oxygencustomizer.xposed.utils.launcher;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.hooks.launcher.FakeResources.getFakeIdIcon;
import static it.dhd.oxygencustomizer.xposed.hooks.launcher.FakeResources.getFakeIdString;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;

import android.content.Context;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.popup.SystemShortcut;
import com.android.quickstep.views.TaskView;

public class KillShortcut extends SystemShortcut<BaseDraggingActivity> {

    private final BaseDraggingActivity activity;
    private final View mTaskView;
    private final String mPackageName;

    /**
     * Up to COS16: uses the (unobfuscated) task container.
     */
    public KillShortcut(BaseDraggingActivity activity, TaskView.TaskIdAttributeContainer taskContainer) {
        this(activity, taskContainer.getItemInfo(), taskContainer.getTaskView(),
                taskContainer.getItemInfo().getTargetComponent().getPackageName());
    }

    /**
     * Container-independent constructor (COS17+, where the task container class is obfuscated).
     */
    public KillShortcut(BaseDraggingActivity activity, ItemInfo itemInfo, View taskView, String packageName) {
        super(getFakeIdIcon(), getFakeIdString(), activity, itemInfo, taskView);
        this.activity = activity;
        this.mTaskView = taskView;
        this.mPackageName = packageName;
    }

    @Override
    public void onClick(View v) {
        try {
            callMethod(activity.getSystemService(Context.ACTIVITY_SERVICE),
                    "forceStopPackageAsUser",
                    mPackageName,
                    callMethod(Process.myUserHandle(), "getIdentifier"));
            callMethod(mTaskView, "performAccessibilityAction", activity.getResources().getIdentifier("accessibility_close", "string", LAUNCHER), null);
            Toast.makeText(activity, "App Killed", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Log.e("[ Oxygen Customizer - Launcher - Kill Shortcut ]", "Error while killing app " + Log.getStackTraceString(t));
        }
    }
}
