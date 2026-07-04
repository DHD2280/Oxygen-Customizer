package it.dhd.oxygencustomizer.xposed.hooks.launcher;

import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.hooks.launcher.CustomShortcut.DIVIDER;
import static it.dhd.oxygencustomizer.xposed.hooks.launcher.CustomShortcut.KILL_SHORTCUT;

import android.content.Context;
import android.util.Log;

import com.android.quickstep.TaskShortcutFactory;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class RecentsMenu extends XposedMods {

    private static final String listenPackage = LAUNCHER;

    @Override
    public void initResources() {
//        XResources xRes = resparams.get(LAUNCHER).res;
//        XModuleResources moddedRes = XModuleResources.createInstance(getModulePath(), xRes);
        FakeResources.setFakeIdIcon(mContext.getResources().getIdentifier("flexible_close_icon", "drawable", LAUNCHER));
        FakeResources.setFakeIdString(mContext.getResources().getIdentifier("bubble_close", "string", LAUNCHER));
        FakeResources.setIdDivider(mContext.getResources().getIdentifier("ic_oplus_task_shortcut_lock", "drawable", LAUNCHER));
    }

    public RecentsMenu(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {

        ReflectedClass OplusTaskOverlayFactoryKt = ReflectedClass.of("com.oplus.quickstep.shortcuts.OplusTaskOverlayFactoryKt");
        Object[] MENU_OPTIONS = (Object[]) getStaticObjectField(OplusTaskOverlayFactoryKt.getClazz(), "MENU_OPTIONS");

        ArrayList<Object> taskShortcutFactories = new ArrayList<>(Arrays.asList(MENU_OPTIONS));
        ReflectedClass OplusGroupDividerShortcutClz = ReflectedClass.of("com.oplus.quickstep.shortcuts.OplusGroupDividerShortcut");
        try {
            if (OplusGroupDividerShortcutClz.getClazz() != null) {
                taskShortcutFactories.add(DIVIDER);
            }
        } catch (Throwable t) {
            log("Failed to add divider shortcut: " + Log.getStackTraceString(t));
        }
        try {
            taskShortcutFactories.add(KILL_SHORTCUT);
        } catch (Throwable t) {
            log("Failed to add kill shortcut: " + Log.getStackTraceString(t));
        }
        setStaticObjectField(OplusTaskOverlayFactoryKt.getClazz(), "MENU_OPTIONS", taskShortcutFactories.toArray(new TaskShortcutFactory[0]));
    }


    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
