package it.dhd.oxygencustomizer.xposed.utils;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.PersonalityManager;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.PersonalityManagerClass;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.PersonalityManagerEx;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callStaticMethod;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.Shape;
import android.util.Log;

import com.android.systemui.DependencyEx;
import com.android.systemui.qs.OplusQuickSettingsEx;

import it.dhd.oxygencustomizer.xposed.utils.toolkit.Logger;


public class QsTileHelper {
    public static final int TYPE_QS_QUICK_TILE_SIZE = 1;
    public static final int TYPE_QS_QUICK_TILE_SIZE_SIMPLE_MODE = 4;
    public static final int TYPE_QS_TILE_ICON_SIZE = 2;
    public static final int TYPE_QS_TILE_ICON_SIZE_SIMPLE_MODE = 5;
    public static final int TYPE_QS_TILE_SIZE = 0;
    public static final int TYPE_QS_TILE_SIZE_SIMPLE_MODE = 3;
    public static final int TYPE_QS_TILE_SIZE_SMALLSPACE = 6;

    public static int getQsTileSize(Context context, int i) {
        Resources resources = context.getResources();
        return switch (i) {
            case TYPE_QS_TILE_SIZE -> resources.getDimensionPixelSize(
                    context.getResources().getIdentifier(
                            "qs_quick_tile_size",
                            "dimen",
                            SYSTEM_UI));
            case TYPE_QS_QUICK_TILE_SIZE -> resources.getDimensionPixelSize(
                    context.getResources().getIdentifier(
                            "qs_quick_qs_tile_size",
                            "dimen",
                            SYSTEM_UI));
            case TYPE_QS_TILE_ICON_SIZE -> resources.getDimensionPixelSize(
                    context.getResources().getIdentifier(
                            "qs_tile_icon_size",
                            "dimen",
                            SYSTEM_UI));
            case TYPE_QS_TILE_SIZE_SIMPLE_MODE -> resources.getDimensionPixelSize(
                    context.getResources().getIdentifier(
                            "qs_quick_tile_size_simple_mode",
                            "dimen",
                            SYSTEM_UI));
            case TYPE_QS_QUICK_TILE_SIZE_SIMPLE_MODE -> resources.getDimensionPixelSize(
                    context.getResources().getIdentifier(
                            "qs_quick_qs_tile_size_simple_mode",
                            "dimen",
                            SYSTEM_UI));
            case TYPE_QS_TILE_ICON_SIZE_SIMPLE_MODE -> resources.getDimensionPixelSize(
                    context.getResources().getIdentifier(
                            "qs_tile_icon_size_simple_mode",
                            "dimen",
                            SYSTEM_UI));
            case TYPE_QS_TILE_SIZE_SMALLSPACE -> resources.getDimensionPixelSize(
                    context.getResources().getIdentifier(
                            "qs_tile_size_smallspace",
                            "dimen",
                            SYSTEM_UI));
            default -> 0;
        };
    }

    public static float getShapeRadius(Context context, int i) {
        int resId = switch (i) {
            case 0, 5 ->
                    context.getResources().getIdentifier("qs_hl_tile_corner_radius_square", "dimen", SYSTEM_UI);
            case 4 ->
                    context.getResources().getIdentifier("qs_hl_tile_corner_radius_semicircle", "dimen", SYSTEM_UI);
            case 1 ->
                    context.getResources().getIdentifier("qs_hl_tile_corner_radius_round_rect", "dimen", SYSTEM_UI);
            default ->
                    context.getResources().getIdentifier("qs_hl_tile_corner_radius_circle", "dimen", SYSTEM_UI);
        };
        return context.getResources().getDimension(resId);
    }

    public static Shape getShapeForHighlightTile(Context c) {
        // When PersonalityManagerEx is not available, fallback to PersonalityManagerClass, mainly for OOS 13
        if (PersonalityManagerEx == null) {
            if (PersonalityManagerClass != null) {
                Object PersonalityManager = callStaticMethod(PersonalityManagerClass, "getInstance");
                if (PersonalityManager != null) {
                    return (Shape) callMethod(PersonalityManager, "getShapeForHighlightTile", c);
                }
            }
        }
        // Method should be available
        return (Shape) callMethod(PersonalityManagerEx, "getShapeForHighlightTile", c);
    }

    public static float getHighlightRadius(Context c) {
        try {
            return (float) callMethod(resolvePersonalityManager(), "getHighlightRadius", c);
        } catch (Throwable t) {
            Logger.log("QsTileHelper, get highlight radius error: \n" + Log.getStackTraceString(t));
            return getShapeRadius(c, -1);
        }
    }

    private static Object resolvePersonalityManager() {
        if (PersonalityManager != null) return PersonalityManager;
        if (PersonalityManagerEx != null) return PersonalityManagerEx;
        try {
            return ((OplusQuickSettingsEx) DependencyEx.get(OplusQuickSettingsEx.class)).getPersonalityManagerEx();
        } catch (Throwable ignored) {
            return ((OplusQuickSettingsEx) DependencyEx.sDependency.getDependency(OplusQuickSettingsEx.class)).getPersonalityManagerEx();
        }
    }

    public static PathShape getLastShape(Context c) {
        try {
            return (PathShape) callMethod(PersonalityManagerEx, "getLastShape", c);
        } catch (Throwable t) {
            Logger.log("QsTileHelper, get last shape error: \n" + Log.getStackTraceString(t));
            if (PersonalityManagerClass != null) {
                Object PersonalityManager = callStaticMethod(PersonalityManagerClass, "getInstance");
                if (PersonalityManager != null) {
                    return (PathShape) callMethod(PersonalityManager, "getLastShape", c);
                }
            }
            return null;
        }
    }

    public static float getMediaPanelRadius(Context c) {
        try {
            return (float) callMethod(PersonalityManager, "getMediaPanelRadius", c);
        } catch (Throwable t) {
            Logger.log("Error getting media panel radius: " + Log.getStackTraceString(t));
            return getShapeRadius(c, -1);
        }
    }

}
