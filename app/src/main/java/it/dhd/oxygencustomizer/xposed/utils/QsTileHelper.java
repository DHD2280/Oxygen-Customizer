package it.dhd.oxygencustomizer.xposed.utils;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.PersonalityManagerClass;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.PersonalityManagerEx;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.Shape;

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

    public static PathShape getLastShape(Context c) {
        if (PersonalityManagerEx == null) {
            if (PersonalityManagerClass != null) {
                Object PersonalityManager = callStaticMethod(PersonalityManagerClass, "getInstance");
                if (PersonalityManager != null) {
                    return (PathShape) callMethod(PersonalityManager, "getLastShape", c);
                }
            }
        }
        return (PathShape) callMethod(PersonalityManagerEx, "getLastShape", c);
    }

}
