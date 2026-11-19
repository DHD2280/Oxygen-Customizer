package it.dhd.oxygencustomizer.xposed.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationStylePackUtil {

    public static final String ACTION = "it.dhd.oxygencustomizer.notificationstyle";
    public static final String DEFAULT_TARGET = "com.android.systemui";
    public static final String BACKGROUND_NAME = "notification_material_bg";
    private static final String PREVIEW_KEY = "preview";
    private static final String AUTHOR_KEY = "packauthor";
    private static final Pattern DIGITS = Pattern.compile("\\d{1,9}");

    public static final class Entry {
        public final String targetPackage;
        public final String type;
        public final String name;
        public final String replacement;

        Entry(String targetPackage, String type, String name, String replacement) {
            this.targetPackage = targetPackage;
            this.type = type;
            this.name = name;
            this.replacement = replacement;
        }
    }

    public static final class Style {
        public final String packageName;
        public final String id;
        public final String label;
        public final String packLabel;
        public final String author;
        public final String preview;
        public final List<Entry> entries;

        Style(String packageName, String id, String label, String packLabel, String author, String preview, List<Entry> entries) {
            this.packageName = packageName;
            this.id = id;
            this.label = label;
            this.packLabel = packLabel;
            this.author = author;
            this.preview = preview;
            this.entries = Collections.unmodifiableList(entries);
        }

        public String getKey() {
            return packageName + "/" + id;
        }

        @Nullable
        public String getPreviewName() {
            if (!TextUtils.isEmpty(preview)) return preview;
            for (Entry entry : entries) {
                if (entry.type.equals("drawable") && entry.name.equals(BACKGROUND_NAME) && entry.targetPackage.equals(DEFAULT_TARGET)) {
                    return entry.replacement;
                }
            }
            return null;
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

    private static String naturalKey(String id) {
        Matcher matcher = DIGITS.matcher(id);
        StringBuilder key = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(key, String.format(Locale.ROOT, "%010d", Long.parseLong(matcher.group())));
        }
        matcher.appendTail(key);
        return key.toString();
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
        String name = style.getPreviewName();
        if (name == null) return null;
        try {
            Resources res = context.createPackageContext(style.packageName, 0).getResources();
            @SuppressLint("DiscouragedApi")
            int id = res.getIdentifier(name, "drawable", style.packageName);
            if (id == 0) return null;
            return res.getDrawable(id, context.getTheme());
        } catch (Throwable ignored) {
            return null;
        }
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

            List<Entry> entries = new ArrayList<>();
            String preview = null;
            for (String item : res.getStringArray(arrayId)) {
                int split = item.indexOf('=');
                if (split <= 0) continue;
                String target = item.substring(0, split).trim();
                String replacement = item.substring(split + 1).trim();
                if (target.equals(PREVIEW_KEY)) {
                    preview = replacement;
                    continue;
                }
                String targetPackage = DEFAULT_TARGET;
                int colon = target.indexOf(':');
                if (colon > 0) {
                    targetPackage = target.substring(0, colon);
                    target = target.substring(colon + 1);
                }
                int slash = target.indexOf('/');
                if (slash <= 0 || replacement.isEmpty()) continue;
                entries.add(new Entry(targetPackage, target.substring(0, slash), target.substring(slash + 1), replacement));
            }
            if (entries.isEmpty()) return null;

            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            String author = appInfo.metaData != null ? appInfo.metaData.getString(AUTHOR_KEY) : null;
            String label = String.valueOf(info.loadLabel(pm));
            String packLabel = String.valueOf(appInfo.loadLabel(pm));
            return new Style(packageName, id, label, packLabel, TextUtils.isEmpty(author) ? "Unknown" : author, preview, entries);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
