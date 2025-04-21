package it.dhd.oxygencustomizer.xposed.hooks.launcher;

import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.getModulePath;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.resparams;
import static it.dhd.oxygencustomizer.xposed.hooks.launcher.CustomShortcut.DIVIDER;
import static it.dhd.oxygencustomizer.xposed.hooks.launcher.CustomShortcut.KILL_SHORTCUT;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.util.Log;

import com.android.launcher3.popup.SystemShortcut;
import com.android.quickstep.TaskShortcutFactory;
import com.oplus.quickstep.shortcuts.OplusGroupDividerShortcut;

import java.util.ArrayList;
import java.util.Arrays;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.launcher.KillShortcut;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class RecentsMenu extends XposedMods {

    private static final String listenPackage = LAUNCHER;

    @Override
    public void initResources() {
        XResources xRes = resparams.get(LAUNCHER).res;
        XModuleResources moddedRes = XModuleResources.createInstance(getModulePath(), xRes);
        FakeResources.setFakeIdIcon(xRes.addResource(moddedRes, R.drawable.ic_kill));
        FakeResources.setFakeIdString(xRes.addResource(moddedRes, R.string.gesture_override_back_hold_command_kill_app));
        FakeResources.setIdDivider(xRes.getIdentifier("ic_oplus_task_shortcut_lock", "drawable", LAUNCHER));
    }

    public RecentsMenu(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        Log.d("OXYGEN_CUSTOMIZER", "SystemShortcut " + SystemShortcut.class);
        Log.d("OXYGEN_CUSTOMIZER", "SystemShortcut's classloader " + SystemShortcut.class.getClassLoader());
        Log.d("OXYGEN_CUSTOMIZER", "KillShortcut " + KillShortcut.class);
        Log.d("OXYGEN_CUSTOMIZER", "KillShortcut's classloader " + KillShortcut.class.getClassLoader());
        Log.d("OXYGEN_CUSTOMIZER", "moduleClassLoader " + XPLauncher.class.getClassLoader());


        ReflectedClass OplusTaskOverlayFactoryKt = ReflectedClass.of("com.oplus.quickstep.shortcuts.OplusTaskOverlayFactoryKt");
        Object[] MENU_OPTIONS = (Object[]) getStaticObjectField(OplusTaskOverlayFactoryKt.getClazz(), "MENU_OPTIONS");

        ArrayList<Object> taskShortcutFactories = new ArrayList<>(Arrays.asList(MENU_OPTIONS));
        taskShortcutFactories.add(DIVIDER);
        taskShortcutFactories.add(KILL_SHORTCUT);
        setStaticObjectField(OplusTaskOverlayFactoryKt.getClazz(), "MENU_OPTIONS", taskShortcutFactories.toArray(new TaskShortcutFactory[0]));
    }


    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
