package it.dhd.oxygencustomizer.xposed.hooks.launcher;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.hooks.launcher.CustomShortcut.DIVIDER;
import static it.dhd.oxygencustomizer.xposed.hooks.launcher.CustomShortcut.KILL_SHORTCUT;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findClassIfExists;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getStaticObjectField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.setStaticFinalObjectField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.setStaticObjectField;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.popup.SystemShortcut;
import com.android.quickstep.TaskShortcutFactory;
import com.android.quickstep.views.TaskView;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.libxposed.api.XposedModuleInterface;
import io.github.neonorbit.dexplore.DexFactory;
import io.github.neonorbit.dexplore.Dexplore;
import io.github.neonorbit.dexplore.filter.ClassFilter;
import io.github.neonorbit.dexplore.filter.DexFilter;
import io.github.neonorbit.dexplore.result.ClassData;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ModuleResourceInjector;
import it.dhd.oxygencustomizer.xposed.utils.launcher.KillShortcut;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class RecentsMenu extends XposedMods {

    private static final String listenPackage = LAUNCHER;

    @Override
    public void initResources() {
        FakeResources.setFakeIdIcon(ModuleResourceInjector.addDrawable(R.drawable.ic_kill));
        FakeResources.setFakeIdString(ModuleResourceInjector.addString(R.string.gesture_override_back_hold_command_kill_app));
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

        ReflectedClass OplusTaskOverlayFactoryKt = ReflectedClass.ofIfPossible("com.oplus.quickstep.shortcuts.OplusTaskOverlayFactoryKt");
        if (OplusTaskOverlayFactoryKt.getClazz() == null) {
            // COS17+: shortcuts package is obfuscated
            try {
                addKillShortcutObfuscated(PRParam);
            } catch (Throwable t) {
                log(t);
            }
            return;
        }
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


    // COS17 has obfuscated classes, we have to make a trick
    private void addKillShortcutObfuscated(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        ClassLoader classLoader = PRParam.getClassLoader();

        Class<?> taskOverlayFactory = findClassIfExists("com.android.quickstep.TaskOverlayFactory", classLoader);
        if (taskOverlayFactory == null) {
            log("TaskOverlayFactory not found");
            return;
        }
        Class<?> factoryInterface = null;
        for (Field field : taskOverlayFactory.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().isArray()
                    && field.getType().getComponentType().isInterface()) {
                factoryInterface = field.getType().getComponentType();
                break;
            }
        }
        if (factoryInterface == null) {
            log("TaskShortcutFactory interface not found");
            return;
        }

        Dexplore dexplore = DexFactory.load(PRParam.getApplicationInfo().sourceDir);
        ClassFilter holderFilter = ClassFilter.builder()
                .setSourceNames("OplusTaskOverlayFactory.kt")
                .build();
        List<ClassData> candidates = dexplore.findClasses(DexFilter.MATCH_ALL, holderFilter, -1);

        Object killFactory = createKillFactory(factoryInterface, classLoader);
        int patched = 0;
        for (ClassData candidate : candidates) {
            Class<?> holder = candidate.loadClass(classLoader);
            for (Field field : holder.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || field.getType().getComponentType() != factoryInterface)
                    continue;
                field.setAccessible(true);
                Object[] options = (Object[]) field.get(null);
                if (options == null) continue;
                Object[] newOptions = (Object[]) Array.newInstance(factoryInterface, options.length + 1);
                System.arraycopy(options, 0, newOptions, 0, options.length);
                newOptions[options.length] = killFactory;
                setStaticFinalObjectField(field, newOptions);
                patched++;
            }
        }
        log("Kill shortcut added to " + patched + " menu option arrays");
    }

    private Object createKillFactory(Class<?> factoryInterface, ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader, new Class<?>[]{factoryInterface}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "OxygenCustomizerKillShortcutFactory";
            }
            if (SystemShortcut.class.isAssignableFrom(method.getReturnType()) && args != null && args.length == 2) {
                return createKillShortcut((BaseDraggingActivity) args[0], args[1]);
            }
            // other factory capabilities (e.g. "supports split tasks"): not supported
            return method.getReturnType() == boolean.class ? false : null;
        });
    }

    private KillShortcut createKillShortcut(BaseDraggingActivity activity, Object taskContainer) throws Throwable {
        ItemInfo itemInfo = null;
        for (Method m : taskContainer.getClass().getDeclaredMethods()) {
            if (m.getParameterCount() == 0 && ItemInfo.class.isAssignableFrom(m.getReturnType())) {
                m.setAccessible(true);
                itemInfo = (ItemInfo) m.invoke(taskContainer);
                break;
            }
        }
        View taskView = null;
        Object task = null;
        for (Field f : taskContainer.getClass().getDeclaredFields()) {
            if (taskView == null && TaskView.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                taskView = (View) f.get(taskContainer);
            } else if (task == null && f.getType().getName().equals("com.android.systemui.shared.recents.model.Task")) {
                f.setAccessible(true);
                task = f.get(taskContainer);
            }
        }
        if (itemInfo == null || taskView == null || task == null) {
            log("Kill shortcut: unexpected task container " + taskContainer.getClass().getName());
            return null;
        }
        String packageName = (String) callMethod(task, "getPackageName");
        return new KillShortcut(activity, itemInfo, taskView, packageName);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
