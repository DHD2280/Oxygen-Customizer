package it.dhd.oxygencustomizer.xposed.hooks.pscanvas;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.PSCANVAS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.PanoramaUnlock.PANORAMA_THREE_SPLIT;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class BoundlessView extends XposedMods {

    private static final String FEATURE_TABLET = "oplus.hardware.type.tablet";
    private static final String FEATURE_PANORAMA = "oplus.software.wms.panorama_work_station";
    private static final String FEATURE_FOLD = "oplus.hardware.type.fold";
    private static final String FEATURE_FLIP = "oplus.software.fold_remap_display_disabled";
    private static final String FLEXIBLE_WINDOW_UTIL = "com.oplus.pscanvas.canvasmode.canvas.util.FlexibleWindowUtil";
    private static final String CANVAS_LAYOUT_RECT = "com.oplus.flexiblewindow.CanvasCalculateLayoutRect";
    private static final String SMALL_WINDOW_GEOMETRY = "f2.x";
    private static final String VISUAL_BOUNDS = "androidx.flexible.VisualBounds";
    private static final String LAUNCH_BOUNDS = "androidx.flexible.LaunchBounds";
    private static final String[] DEVICE_TYPE_HELPERS = {"i2.s", "j2.s", "h2.s", "k2.s", "l2.s", "m2.s", "n2.s"};
    private static final String[] PANORAMA_GATES = {"M0", "O0", "N0", "P0", "L0", "Q0"};
    private static final int THREE_PANES = 3;
    private static final int LAYOUT_ONE_TOP_TWO_BOTTOM = 4;

    private final ReflectedClass.ReflectionConsumer RETURN_TRUE = param -> param.setResult(true);
    private final ReflectedClass.ReflectionConsumer RETURN_FALSE = param -> param.setResult(false);

    private boolean mThreeSplit = true;
    private ClassLoader mClassLoader;

    public BoundlessView(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        mThreeSplit = Xprefs.getBoolean(PANORAMA_THREE_SPLIT, false);
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        mClassLoader = PRParam.getClassLoader();
        ReflectedClass windowUtil = ReflectedClass.ofIfPossible(FLEXIBLE_WINDOW_UTIL);
        ReflectedClass deviceType = findDeviceTypeHelper();

        hookFeatureManager();
        hookPanoramaGate(windowUtil);
        hookDeviceType(deviceType);
        hookLayoutPolicy();
        hookUseMax(windowUtil);
        hookZoomWindow(windowUtil);
        hookMediumScreen(deviceType);
        hookSmallWindowGeometry();
    }

    private void hookFeatureManager() {
        ReflectedClass featureConfig = ReflectedClass.of("com.oplus.content.OplusFeatureConfigManager", "com.oplus.content.OplusFeatureConfigManagerInternal");
        int n = after(featureConfig, "hasFeature", m -> m.getReturnType() == boolean.class
                && m.getParameterCount() == 1
                && m.getParameterTypes()[0] == String.class, param -> {
            if (!(param.args[0] instanceof String key)) return;
            if (FEATURE_TABLET.equals(key) || FEATURE_PANORAMA.equals(key)) {
                param.setResult(true);
            } else if (FEATURE_FOLD.equals(key) || FEATURE_FLIP.equals(key)) {
                param.setResult(false);
            }
        });
        log("BoundlessView: feature manager hooks=" + n);
    }

    private void hookPanoramaGate(ReflectedClass windowUtil) {
        for (String name : PANORAMA_GATES) {
            int n = after(windowUtil, name, firstArg(Context.class, 1), RETURN_TRUE);
            if (n > 0) {
                log("BoundlessView: panorama gate " + name + " hooks=" + n);
                return;
            }
        }
        log("BoundlessView: panorama gate not found");
    }

    private void hookDeviceType(ReflectedClass deviceType) {
        int n = after(deviceType, "I", noArgs(), RETURN_TRUE);
        n += after(deviceType, "q", noArgs(), RETURN_TRUE);
        log("BoundlessView: device type hooks=" + n);
    }

    private void hookLayoutPolicy() {
        ReflectedClass layoutRect = ReflectedClass.ofIfPossible(CANVAS_LAYOUT_RECT);
        int n = after(layoutRect, "isTabletDevice", noArgs(), RETURN_TRUE);
        n += after(layoutRect, "isPhoneDisplay", firstArg(Context.class, -1), RETURN_FALSE);
        ReflectedClass policy = ReflectedClass.of("com.oplus.flexiblewindow.TabletCanvasCalculateLayoutRectPolicy",
                "com.oplus.flexiblewindow.DefaultCanvasCalculateLayoutRectPolicy");
        n += after(policy, "computeLayoutOrientation", m -> m.getReturnType() == int.class && m.getParameterCount() == 4, param -> {
            if (!(param.args[0] instanceof List<?> panes) || !(param.args[3] instanceof Context context))
                return;
            if (panes.size() != THREE_PANES) return;
            Configuration configuration = context.getResources().getConfiguration();
            if (configuration.screenHeightDp > configuration.screenWidthDp) {
                param.setResult(LAYOUT_ONE_TOP_TWO_BOTTOM);
            }
        });
        log("BoundlessView: layout policy hooks=" + n);
    }

    private void hookUseMax(ReflectedClass windowUtil) {
        int n = after(windowUtil, "R0", m -> m.getReturnType() == boolean[].class && m.getParameterCount() == 2, param -> {
            if (param.args[0] instanceof List<?> panes) param.setResult(new boolean[panes.size()]);
        });
        log("BoundlessView: use max hooks=" + n);
    }

    private void hookZoomWindow(ReflectedClass windowUtil) {
        log("BoundlessView: zoom window hooks=" + after(windowUtil, "e1", firstArg(Context.class, -1), RETURN_TRUE));
    }

    private void hookMediumScreen(ReflectedClass deviceType) {
        log("BoundlessView: medium screen hooks=" + after(deviceType, "x", firstArg(Context.class, -1), RETURN_FALSE));
    }

    private void hookSmallWindowGeometry() {
        ReflectedClass geometry = ReflectedClass.ofIfPossible(SMALL_WINDOW_GEOMETRY);
        int n = before(geometry, "S", m -> m.getParameterCount() == 1 && List.class.isAssignableFrom(m.getParameterTypes()[0]), param -> {
            if (!(param.args[0] instanceof List<?> panes)) return;
            for (int i = 0; i < panes.size(); i++) {
                if (panes.get(i) instanceof Bundle bundle) {
                    shift(bundle, VISUAL_BOUNDS, i);
                    shift(bundle, LAUNCH_BOUNDS, i);
                }
            }
        });
        log("BoundlessView: small window geometry hooks=" + n);
    }

    private ReflectedClass findDeviceTypeHelper() {
        for (String name : DEVICE_TYPE_HELPERS) {
            ReflectedClass helper = ReflectedClass.ofIfPossible(name);
            if (isDeviceTypeHelper(helper)) return helper;
        }
        for (char c = 'a'; c <= 'z'; c++) {
            for (int i = 0; i <= 3; i++) {
                ReflectedClass helper = ReflectedClass.ofIfPossible(c + String.valueOf(i) + ".s", mClassLoader);
                if (isDeviceTypeHelper(helper)) {
                    log("BoundlessView: device type helper discovered via scan: " + helper.getClazz().getName());
                    return helper;
                }
            }
        }
        log("BoundlessView: device type helper not found");
        return new ReflectedClass(null);
    }

    private boolean isDeviceTypeHelper(ReflectedClass helper) {
        return !methods(helper, "I", noArgs()).isEmpty() && !methods(helper, "x", firstArg(Context.class, 1)).isEmpty();
    }

    private void shift(Bundle bundle, String key, int index) {
        Rect source = bundle.getParcelable(key, Rect.class);
        if (source == null) return;
        Rect rect = new Rect(source);
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * 0.46f);
        int height = (int) (width * ((float) rect.height() / Math.max(1, rect.width())));
        int margin = (int) (16 * metrics.density);
        int top = (int) (metrics.heightPixels * 0.12f);
        int bottomTop = metrics.heightPixels - height - (int) (metrics.heightPixels * 0.12f);
        switch (index % 3) {
            case 0 -> rect.set(margin, top, margin + width, top + height);
            case 1 ->
                    rect.set(metrics.widthPixels - width - margin, top, metrics.widthPixels - margin, top + height);
            default -> rect.set(margin, bottomTop, margin + width, bottomTop + height);
        }
        bundle.putParcelable(key, rect);
    }

    private static Predicate<Method> noArgs() {
        return m -> m.getReturnType() == boolean.class && m.getParameterCount() == 0;
    }

    private static Predicate<Method> firstArg(Class<?> type, int count) {
        return m -> m.getReturnType() == boolean.class
                && m.getParameterCount() >= 1
                && (count < 0 || m.getParameterCount() == count)
                && type.isAssignableFrom(m.getParameterTypes()[0]);
    }

    private ReflectedClass.ReflectionConsumer split(ReflectedClass.ReflectionConsumer body) {
        return param -> {
            if (mThreeSplit) body.run(param);
        };
    }

    private List<Method> methods(ReflectedClass reflected, String name, Predicate<Method> filter) {
        List<Method> out = new ArrayList<>();
        Class<?> clazz = reflected.getClazz();
        if (clazz == null) return out;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name) && !Modifier.isAbstract(method.getModifiers()) && filter.test(method)) {
                out.add(method);
            }
        }
        return out;
    }

    private int before(ReflectedClass reflected, String name, Predicate<Method> filter, ReflectedClass.ReflectionConsumer body) {
        int count = 0;
        for (Method method : methods(reflected, name, filter)) {
            try {
                count += reflected.before(method).run(split(body)).size();
            } catch (Throwable t) {
                log("BoundlessView: hook " + name + " failed " + t.getMessage());
            }
        }
        return count;
    }

    private int after(ReflectedClass reflected, String name, Predicate<Method> filter, ReflectedClass.ReflectionConsumer body) {
        int count = 0;
        for (Method method : methods(reflected, name, filter)) {
            try {
                count += reflected.after(method).run(split(body)).size();
            } catch (Throwable t) {
                log("BoundlessView: hook " + name + " failed " + t.getMessage());
            }
        }
        return count;
    }

    @Override
    public boolean listensTo(String packageName) {
        return PSCANVAS.equals(packageName);
    }
}
