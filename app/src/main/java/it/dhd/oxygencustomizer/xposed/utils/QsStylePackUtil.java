package it.dhd.oxygencustomizer.xposed.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Quick Settings style packs.
 * <p>
 * OxygenOS 17 builds its tile backgrounds in code, so a pack cannot ship drawables for
 * them the way a notification style pack does. A style is a set of values instead, read
 * from a string-array named after the activity that declares the pack:
 * <pre>
 * &lt;string-array name="ControlCenter"&gt;
 *     &lt;item&gt;border.width=1dp&lt;/item&gt;
 *     &lt;item&gt;border.color.dark=#59FFFFFF&lt;/item&gt;
 *     &lt;item&gt;border.color.light=#1A000000&lt;/item&gt;
 * &lt;/string-array&gt;
 * </pre>
 */
public class QsStylePackUtil {

    public static final String ACTION = "it.dhd.oxygencustomizer.qsstyle";
    private static final String PREVIEW_KEY = "preview";
    private static final String AUTHOR_KEY = "packauthor";
    private static final Pattern DIGITS = Pattern.compile("\\d{1,9}");

    public static final float DEFAULT_WIDTH_DP = 1f;
    public static final int DEFAULT_COLOR_DARK = 0x59FFFFFF;
    public static final int DEFAULT_COLOR_LIGHT = 0x1A000000;

    /** Everything a style can say about the tile edge. */
    public static final class Values {
        public float borderWidthDp = DEFAULT_WIDTH_DP;
        public int borderColorDark = DEFAULT_COLOR_DARK;
        public int borderColorLight = DEFAULT_COLOR_LIGHT;
        /** Second stop of a top to bottom gradient, or 0 for a flat edge. */
        public int borderColorDarkBottom = 0;
        public int borderColorLightBottom = 0;
        public float borderInsetDp = 0f;
        /** Control Center leaves its active toggles borderless, so this is off by default. */
        public boolean borderActive = false;

        public int color(boolean night) {
            return night ? borderColorDark : borderColorLight;
        }

        public int colorBottom(boolean night) {
            return night ? borderColorDarkBottom : borderColorLightBottom;
        }

        public boolean hasEdge() {
            return borderWidthDp > 0f && Color.alpha(borderColorDark | borderColorLight) != 0;
        }

        public void apply(@NonNull Map<String, String> entries) {
            borderWidthDp = dimen(entries.get("border.width"), borderWidthDp);
            borderColorDark = color(entries.get("border.color.dark"), borderColorDark);
            borderColorLight = color(entries.get("border.color.light"), borderColorLight);
            borderColorDarkBottom = color(entries.get("border.color.dark.bottom"), borderColorDarkBottom);
            borderColorLightBottom = color(entries.get("border.color.light.bottom"), borderColorLightBottom);
            borderInsetDp = dimen(entries.get("border.inset"), borderInsetDp);
            borderActive = bool(entries.get("border.active"), borderActive);
        }

        private static float dimen(String raw, float fallback) {
            if (TextUtils.isEmpty(raw)) return fallback;
            String value = raw.trim().toLowerCase(Locale.ROOT);
            if (value.endsWith("dp")) value = value.substring(0, value.length() - 2);
            else if (value.endsWith("dip")) value = value.substring(0, value.length() - 3);
            try {
                return Float.parseFloat(value.trim());
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }

        private static int color(String raw, int fallback) {
            if (TextUtils.isEmpty(raw)) return fallback;
            try {
                return Color.parseColor(raw.trim());
            } catch (IllegalArgumentException ignored) {
                return fallback;
            }
        }

        private static boolean bool(String raw, boolean fallback) {
            if (TextUtils.isEmpty(raw)) return fallback;
            String value = raw.trim();
            if (value.equalsIgnoreCase("true") || value.equals("1")) return true;
            if (value.equalsIgnoreCase("false") || value.equals("0")) return false;
            return fallback;
        }
    }

    public static final class Style {
        public final String packageName;
        public final String id;
        public final String label;
        public final String packLabel;
        public final String author;
        public final String preview;
        public final Values values;

        Style(String packageName, String id, String label, String packLabel, String author, String preview, Values values) {
            this.packageName = packageName;
            this.id = id;
            this.label = label;
            this.packLabel = packLabel;
            this.author = author;
            this.preview = preview;
            this.values = values;
        }

        public String getKey() {
            return packageName + "/" + id;
        }
    }

    @NonNull
    public static List<Style> queryStyles(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        List<Style> styles = new ArrayList<>();
        for (ResolveInfo info : pm.queryIntentActivities(new Intent(ACTION), PackageManager.GET_META_DATA)) {
            Style style = parse(pm, info.activityInfo);
            if (style != null) styles.add(style);
        }
        styles.sort(Comparator.comparing((Style style) -> style.packLabel).thenComparing(style -> naturalKey(style.id)));
        return styles;
    }

    @Nullable
    public static Style loadStyle(@NonNull Context context, @Nullable String key) {
        if (TextUtils.isEmpty(key) || !key.contains("/")) return null;
        String packageName = key.substring(0, key.indexOf('/'));
        String id = key.substring(key.indexOf('/') + 1);
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(ACTION).setPackage(packageName);
        for (ResolveInfo info : pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)) {
            if (id.equals(styleId(info.activityInfo))) return parse(pm, info.activityInfo);
        }
        return null;
    }

    @Nullable
    public static Drawable getPreview(@NonNull Context context, @NonNull Style style) {
        if (TextUtils.isEmpty(style.preview)) return null;
        try {
            Resources res = context.createPackageContext(style.packageName, 0).getResources();
            @SuppressLint("DiscouragedApi")
            int id = res.getIdentifier(style.preview, "drawable", style.packageName);
            if (id == 0) return null;
            return res.getDrawable(id, context.getTheme());
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String naturalKey(String id) {
        Matcher matcher = DIGITS.matcher(id);
        StringBuilder key = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(key, String.format(Locale.ROOT, "%010d", Long.parseLong(matcher.group())));
        }
        matcher.appendTail(key);
        return key.toString();
    }

    private static String styleId(ActivityInfo info) {
        String prefix = info.packageName + ".";
        return info.name.startsWith(prefix) ? info.name.substring(prefix.length()) : info.name;
    }

    @Nullable
    private static Style parse(PackageManager pm, ActivityInfo info) {
        try {
            String packageName = info.packageName;
            String id = styleId(info);
            Resources res = pm.getResourcesForApplication(packageName);
            @SuppressLint("DiscouragedApi")
            int arrayId = res.getIdentifier(id, "array", packageName);
            if (arrayId == 0) return null;

            Map<String, String> entries = new HashMap<>();
            for (String item : res.getStringArray(arrayId)) {
                int split = item.indexOf('=');
                if (split <= 0) continue;
                entries.put(item.substring(0, split).trim().toLowerCase(Locale.ROOT), item.substring(split + 1).trim());
            }
            if (entries.isEmpty()) return null;

            Values values = new Values();
            values.apply(entries);

            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            String author = appInfo.metaData != null ? appInfo.metaData.getString(AUTHOR_KEY) : null;
            return new Style(packageName, id,
                    String.valueOf(info.loadLabel(pm)),
                    String.valueOf(appInfo.loadLabel(pm)),
                    TextUtils.isEmpty(author) ? "Unknown" : author,
                    entries.get(PREVIEW_KEY),
                    values);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
