package it.dhd.oxygencustomizer.xposed.utils.launcher;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.hooks.launcher.FakeResources.getFakeIdIcon;
import static it.dhd.oxygencustomizer.xposed.hooks.launcher.FakeResources.getFakeIdString;

import android.content.Context;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.popup.SystemShortcut;
import com.android.quickstep.views.TaskView;

public class KillShortcut extends SystemShortcut<BaseDraggingActivity> {

    private final BaseDraggingActivity activity;
    private final TaskView.TaskIdAttributeContainer mTaskContainer;


    public KillShortcut(BaseDraggingActivity activity, TaskView.TaskIdAttributeContainer taskContainer) {
        super(getFakeIdIcon(), getFakeIdString(), activity, taskContainer.getItemInfo(), taskContainer.getTaskView());
        this.mTaskContainer = taskContainer;
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        try {
            Toast.makeText(activity, "App Killed", Toast.LENGTH_SHORT).show();

            callMethod(activity.getSystemService(Context.ACTIVITY_SERVICE),

                    "forceStopPackageAsUser",
                    mTaskContainer.getItemInfo().getTargetComponent().getPackageName(),
                    callMethod(Process.myUserHandle(), "getIdentifier"));
            TaskView taskView = mTaskContainer.getTaskView();
            callMethod(taskView, "performAccessibilityAction", activity.getResources().getIdentifier("accessibility_close", "string", LAUNCHER), null);
        } catch (Throwable t) {
            Log.e("[ Oxygen Customizer - Launcher - Kill Shortcut ]", "Error while killing app " + Log.getStackTraceString(t));
        }
    }
}
