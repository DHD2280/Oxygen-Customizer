package it.dhd.oxygencustomizer.xposed.utils.systemui;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.QsStyleObserver;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.Logger;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers;

public final class StockQsTileResources {

    public static final int STATE_UNAVAILABLE = 0;
    public static final int STATE_INACTIVE = 1;
    public static final int STATE_ACTIVE = 2;

    private static final String STD_RES_POOL = "com.oplus.systemui.qs.base.res.StdQSResPool";
    private static final String SEP_RES_POOL = "com.oplus.systemui.qs.base.res.SepQSResPool";
    private static final String TILE_DRAWABLE_WRAPPER = "com.oplus.systemui.qs.base.res.drawable.TileDrawableWrapper";
    private static final String TILE_VIEW_FLAG = "com.oplus.systemui.qs.base.res.model.TileViewFlag";
    private static final String COLORFUL_CONFIG_UTIL = "com.oplus.systemui.qs.base.util.QsColorfulConfigUtil";
    private static final String OVAL_OUTLINE_PROVIDER = "com.oplusos.systemui.common.outline.OvalOutlineProvider";
    private static final String CELL_CALCULATOR_MANAGER = "com.oplus.systemui.plugins.qs.CellCalculatorManager";
    private static final float CAPSULE_SIZE_DP = 62f;
    private static final int STATE_TOGGLEABLE_KEY = 100;

    private StockQsTileResources() {
    }

    public static boolean isSupported() {
        return Build.VERSION.SDK_INT >= 37;
    }

    public static Drawable createTileBackground(View host) {
        return createPoolBackground(host, "getTileViewDrawable", "getTileOutline", "tileViewDrawable", "tileOutline");
    }

    public static Drawable createHighlightBackground(View host) {
        return createPoolBackground(host, "getHighlightTileViewDrawable", "getHighLightTileOutline", "highlightTileViewDrawable", "highLightTileOutline");
    }

    public static Drawable createHighlightIconBackground(View host) {
        if (!isSupported() || !QsStyleObserver.isSeparateStyle()) return null;
        try {
            ClassLoader classLoader = StockQsTileResources.class.getClassLoader();
            Object sepResPool = ReflectionHelpers.getStaticObjectField(ReflectionHelpers.findClass(SEP_RES_POOL, classLoader), "INSTANCE");
            Object builder = stateFlowValue(ReflectionHelpers.callMethod(sepResPool, "getHighLightIconBg"));
            Object outline = ReflectionHelpers.getStaticObjectField(ReflectionHelpers.findClass(OVAL_OUTLINE_PROVIDER, classLoader), "INSTANCE");
            return wrap(host, builder, outline, false);
        } catch (Throwable t) {
            Logger.log(t);
            return null;
        }
    }

    public static int getCapsuleSize(View host) {
        try {
            return (int) ReflectionHelpers.callStaticMethod(
                    ReflectionHelpers.findClass(CELL_CALCULATOR_MANAGER, StockQsTileResources.class.getClassLoader()),
                    "dpToPxWithInitialDensity", new Class<?>[]{float.class, android.content.Context.class}, CAPSULE_SIZE_DP, host.getContext());
        } catch (Throwable t) {
            return (int) (CAPSULE_SIZE_DP * host.getResources().getDisplayMetrics().density);
        }
    }

    private static Drawable createPoolBackground(View host, String sepBuilderGetter, String sepOutlineGetter, String stdBuilderField, String stdOutlineField) {
        if (!isSupported()) return null;
        try {
            ClassLoader classLoader = StockQsTileResources.class.getClassLoader();
            Object builder;
            Object outline;
            if (QsStyleObserver.isSeparateStyle()) {
                Object sepResPool = ReflectionHelpers.getStaticObjectField(ReflectionHelpers.findClass(SEP_RES_POOL, classLoader), "INSTANCE");
                builder = stateFlowValue(ReflectionHelpers.callMethod(sepResPool, sepBuilderGetter));
                outline = stateFlowValue(ReflectionHelpers.callMethod(sepResPool, sepOutlineGetter));
            } else {
                Class<?> stdResPool = ReflectionHelpers.findClass(STD_RES_POOL, classLoader);
                builder = stateFlowValue(ReflectionHelpers.getStaticObjectField(stdResPool, stdBuilderField));
                outline = stateFlowValue(ReflectionHelpers.getStaticObjectField(stdResPool, stdOutlineField));
            }
            return wrap(host, builder, outline, true);
        } catch (Throwable t) {
            Logger.log(t);
            return null;
        }
    }

    private static Drawable wrap(View host, Object builder, Object outline, boolean useDefaultFlag) throws ReflectiveOperationException {
        Drawable drawable = (Drawable) ReflectionHelpers.callMethod(builder, "build",
                new Class<?>[]{View.class, boolean.class, Integer.class}, host, useDefaultFlag, null);
        Drawable wrapper = (Drawable) ReflectionHelpers.findClass(TILE_DRAWABLE_WRAPPER, StockQsTileResources.class.getClassLoader())
                .getConstructor(Drawable.class)
                .newInstance((Object) null);
        ReflectionHelpers.callMethod(wrapper, "setPathProvider", outline);
        ReflectionHelpers.callMethod(wrapper, "setDrawable", new Class<?>[]{Drawable.class}, drawable);
        return wrapper;
    }

    public static ColorStateList getIconColors(View host, Integer colorfulConfig) {
        if (!isSupported()) return null;
        try {
            ClassLoader classLoader = StockQsTileResources.class.getClassLoader();
            boolean separate = QsStyleObserver.isSeparateStyle();
            Class<?> pool = ReflectionHelpers.findClass(separate ? SEP_RES_POOL : STD_RES_POOL, classLoader);
            Object iconColorState = null;
            if (separate) {
                iconColorState = findStaticNoArgMethod(pool, "getTileIconThemeColorState").invoke(null);
            }
            if (iconColorState == null) {
                iconColorState = findStaticNoArgMethod(pool, "getTileIconColorState").invoke(null);
            }
            Integer effectiveColorfulConfig = OpUtils.isQsColorfulEnabled() ? colorfulConfig : null;
            return (ColorStateList) ReflectionHelpers.callMethod(iconColorState, "getColorStateList",
                    new Class<?>[]{Integer.class}, effectiveColorfulConfig);
        } catch (Throwable t) {
            Logger.log(t);
            return null;
        }
    }

    public static Integer getColorfulConfig(View host, String widgetType) {
        if (!isSupported() || widgetType == null) return null;
        String fieldName = switch (widgetType) {
            case "w:wifi", "w:bt" -> "QS_COLORFUL_ICON_ACTIVE_BLUE_COLOR";
            case "w:data", "w:hotspot" -> "QS_COLORFUL_ICON_ACTIVE_GREEN_COLOR";
            case "w:torch" -> "QS_COLORFUL_ICON_ACTIVE_VIOLET_COLOR";
            case "w:ringer" -> "QS_COLORFUL_ICON_ACTIVE_RED_COLOR";
            default -> null;
        };
        if (fieldName == null) return null;
        try {
            return ReflectionHelpers.getStaticIntField(
                    ReflectionHelpers.findClass(COLORFUL_CONFIG_UTIL, StockQsTileResources.class.getClassLoader()), fieldName);
        } catch (Throwable t) {
            Logger.log(t);
            return null;
        }
    }

    private static final Map<Integer, int[]> sStateFlags = new ConcurrentHashMap<>();

    public static int[] getStateFlag(View host, int state) {
        if (!isSupported()) return new int[0];
        int[] cached = sStateFlags.get(state);
        if (cached != null) return cached;
        int[] flag = lookupStateFlag(host, state);
        if (flag.length > 0) sStateFlags.put(state, flag);
        return flag;
    }

    public static int[] getToggleableStateFlag(View host, int state) {
        if (!isSupported()) return new int[0];
        int key = STATE_TOGGLEABLE_KEY + state;
        int[] cached = sStateFlags.get(key);
        if (cached != null) return cached;
        int[] stateFlag = getStateFlag(host, state);
        int[] toggleable = lookupFlag(host, "Toggleable");
        if (stateFlag.length == 0 || toggleable.length == 0) return stateFlag;
        int[] merged = new int[stateFlag.length + toggleable.length];
        System.arraycopy(toggleable, 0, merged, 0, toggleable.length);
        System.arraycopy(stateFlag, 0, merged, toggleable.length, stateFlag.length);
        sStateFlags.put(key, merged);
        return merged;
    }

    private static int[] lookupStateFlag(View host, int state) {
        String flagName = switch (state) {
            case STATE_ACTIVE -> "Active";
            case STATE_UNAVAILABLE -> "Unavailable";
            default -> "Inactive";
        };
        return lookupFlag(host, flagName);
    }

    private static int[] lookupFlag(View host, String flagName) {
        try {
            Object flag = ReflectionHelpers.getStaticObjectField(
                    ReflectionHelpers.findClass(TILE_VIEW_FLAG, StockQsTileResources.class.getClassLoader()), flagName);
            return (int[]) ReflectionHelpers.callMethod(flag, "getFlag");
        } catch (Throwable t) {
            Logger.log(t);
            return new int[0];
        }
    }

    private static Object stateFlowValue(Object stateFlow) {
        return ReflectionHelpers.callMethod(stateFlow, "getValue");
    }

    private static Method findStaticNoArgMethod(Class<?> clazz, String name) throws NoSuchMethodException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 0 && Modifier.isStatic(method.getModifiers())) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new NoSuchMethodException(clazz.getName() + "#" + name);
    }
}
