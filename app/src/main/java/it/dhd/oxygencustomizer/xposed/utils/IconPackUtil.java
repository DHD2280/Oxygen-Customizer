package it.dhd.oxygencustomizer.xposed.utils;

import static it.dhd.oxygencustomizer.xposed.utils.ThemePackMapping.TYPE_DRAWABLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.OCPreferences;

/**
 * @noinspection DataFlowIssue
 */
public class IconPackUtil implements Serializable {

    /**
     * @noinspection unused
     */
    private static final String TAG = "IconPackUtil";

    public static IconPackUtil instance = null;

    /**
     * Constants for the enabled state of an icon pack
     */
    public static final int ENABLED_FULL = 0;
    public static final int ENABLED_PARTIAL = 1;
    public static final int DISABLED = 2;

    private final PackageManager mPackageManager;
    public ResourceMapping mResourceMapping;
    public IconPackMapping mIconPackMapping;
    private final Set<IconPackQueryListener> listeners = ConcurrentHashMap.newKeySet();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private boolean hasLoaded = false;
    private boolean isQuerying = false;

    public interface IconPackQueryListener {
        default void onIconPacksLoaded(ResourceMapping resourceMapping, IconPackMapping iconPackMapping) {
        }
    }

    public void addListener(IconPackQueryListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IconPackQueryListener listener) {
        listeners.remove(listener);
    }

    public IconPackUtil(@NonNull Context context) {
        mPackageManager = context.getPackageManager();
    }

    public boolean isAnythingEnabled() {
        if (isQuerying || mResourceMapping == null) return false;

        return mResourceMapping.values().stream()
                .flatMap(List::stream)
                .anyMatch(ReplacementIcon::isEnabled);
    }

    public static synchronized IconPackUtil getInstance(Context context) {
        if (instance == null) {
            instance = new IconPackUtil(context);
        }
        return instance;
    }

    public void queryIconPacks(boolean force) {
        if (force || !hasLoaded) {
            executorService.submit(this::queryMappingInternal);
            return;
        }
        listeners.forEach(listener -> listener.onIconPacksLoaded(mResourceMapping, mIconPackMapping));
    }

    private void queryMappingInternal() {
        isQuerying = true;
        hasLoaded = false;
        instance.mResourceMapping = new ResourceMapping();
        instance.mIconPackMapping = new IconPackMapping();
        ThemePackMapping.Mapping prefMapping = ThemePackMapping.Mapping.loadMapping(OCPreferences.getPrefs(), TYPE_DRAWABLE);

        List<ResolveInfo> activities = mPackageManager.queryIntentActivities(new Intent("it.dhd.oxygencustomizer.iconpack"), 0);

        for (ResolveInfo activity : activities) {
            try {
                String packageName = activity.activityInfo.packageName;
                String packName = activity.activityInfo.name.replaceAll(String.format("^%s\\.", packageName), "");
                String packLabel = String.valueOf(activity.activityInfo.loadLabel(mPackageManager));

                PackageInfo packageInfo = mPackageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
                String author;
                try {
                    author = packageInfo.applicationInfo.metaData.getString("packauthor");
                } catch (Exception e) {
                    author = "Unknown";
                }

                Resources packResources = mPackageManager.getResourcesForApplication(packageName);

                @SuppressLint("DiscouragedApi")
                String[] packData = packResources.getStringArray(packResources.getIdentifier(packName, "array", packageName));

                int resNameArrayID = getStringArrayID(packData[0], packResources, packageName);
                int replacementNameArrayID = getStringArrayID(packData[1], packResources, packageName);

                String[] resNames = packResources.getStringArray(resNameArrayID);
                String[] replacements = packResources.getStringArray(replacementNameArrayID);

                IconPack iconPack = new IconPack(packageName, packLabel, author);

                for (int i = 0; i < resNames.length; i++) {
                    ReplacementIcon rep = new ReplacementIcon(iconPack, replacements[i]);
                    rep.mEnabled = prefMapping.isEnabled(resNames[i], iconPack.mPackageName, replacements[i]);

                    mResourceMapping.add(resNames[i], rep);
                    mIconPackMapping.add(iconPack, resNames[i], rep);
                }
            } catch (Exception ignored) {
            }
        }

        listeners.forEach(listener -> listener.onIconPacksLoaded(mResourceMapping, mIconPackMapping));
        hasLoaded = true;
        isQuerying = false;
    }

    public void enable(IconPack iconPack) {
        setEnabled(iconPack, true);
    }

    private void setEnabled(IconPack iconPack, boolean enabled) {
        HashMap<String, ArrayList<ReplacementIcon>> thisPack = mIconPackMapping.get(iconPack);
        thisPack.forEach((resName, replacementIcons) ->
                mResourceMapping.setEnabled(resName, replacementIcons.get(0), enabled));
        savePrefs();
    }

    public void disable(IconPack iconPack) {
        setEnabled(iconPack, false);
    }

    private void savePrefs() {
        ThemePackMapping.Mapping mapping = new ThemePackMapping.Mapping();
        mResourceMapping.forEach((resName, replacementIcons) -> replacementIcons.stream()
                .filter(ReplacementIcon::isEnabled)
                .findFirst()
                .ifPresent(rep ->
                        mapping.add(resName, rep.mIconPack.mPackageName, rep.mReplacementRes)));

        ThemePackMapping.Mapping.saveMapping(OCPreferences.getPrefs(), TYPE_DRAWABLE, mapping);
    }

    public void disable(String resName) {
        mResourceMapping.reset(resName);
        savePrefs();
    }

    public void setEnabled(String resName, ReplacementIcon replacementIcon) {
        mResourceMapping.setEnabled(resName, replacementIcon, true);
        savePrefs();
    }

    public int getEnabledState(IconPack iconPack) {
        HashMap<String, ArrayList<ReplacementIcon>> thisPack = mIconPackMapping.get(iconPack);
        AtomicBoolean fullyEnabled = new AtomicBoolean(true);
        AtomicBoolean partialEnabled = new AtomicBoolean(false);

        thisPack.forEach((resName, replacementIcons) -> {
            ReplacementIcon replacementIcon = getEnabled(resName);
            if (replacementIcon != null && replacementIcon.mIconPack.equals(iconPack)) {
                partialEnabled.set(true);
            } else {
                fullyEnabled.set(false);
            }
        });

        if (fullyEnabled.get()) return ENABLED_FULL;
        if (partialEnabled.get()) return ENABLED_PARTIAL;
        return DISABLED;
    }

    @Nullable
    public ReplacementIcon getEnabled(String resName) {
        return mResourceMapping.getReplacementIcons(resName).stream()
                .filter(ReplacementIcon::isEnabled)
                .findFirst()
                .orElse(null);
    }

    @SuppressLint("DiscouragedApi")
    private int getStringArrayID(String resName, Resources packResources, String packageName) {
        return packResources.getIdentifier(resName, "array", packageName);
    }

    public static class IconPack implements Serializable {
        public String mName;
        public String mAuthor;
        public String mPackageName;
        List<Drawable> packDrawables = new ArrayList<>();

        public IconPack(String packageName, String name, String author) {
            mName = name;
            mAuthor = author;
            mPackageName = packageName;
        }

        public List<Drawable> getPackDrawables(Context context) {
            HashMap<String, ArrayList<ReplacementIcon>> packMapping = instance.mIconPackMapping.get(this);
            if (packDrawables.isEmpty()) {
                String[] previewResNames = context.getResources().getStringArray(R.array.iconpack_preview_icons);


                for (int i = 0; i < previewResNames.length; i++) {
                    int finalI = i;
                    String resName = packMapping.keySet().stream().filter(resName1 -> resName1.contains(previewResNames[finalI])).findFirst().orElse(null);
                    if (resName == null) {
                        Set<String> keySet = packMapping.keySet();
                        resName = (String) keySet.toArray()[ThreadLocalRandom.current().nextInt(0, keySet.size() - 1)];
                    }
                    packDrawables.add(packMapping.get(resName).get(0).getDrawable());
                }
            }
            return packDrawables;
        }

        @NonNull
        @Override
        public String toString() {
            return "IconPack{" +
                    "mName='" + mName + '\'' +
                    ", mAuthor='" + mAuthor + '\'' +
                    ", mPackageName='" + mPackageName + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof IconPack) {
                return mName.equals(((IconPack) other).mName) && mPackageName.equals(((IconPack) other).mPackageName);
            }
            return false;
        }
    }

    public class ReplacementIcon {
        public IconPack mIconPack;
        public String mReplacementRes;
        private boolean mEnabled = false;

        public ReplacementIcon(IconPack iconPack, String replacementRes) {
            mIconPack = iconPack;
            mReplacementRes = replacementRes;
        }

        @Nullable
        public Drawable getDrawable() {
            try {
                Resources packResources = mPackageManager.getResourcesForApplication(mIconPack.mPackageName);

                @SuppressLint("DiscouragedApi")
                int resID = packResources.getIdentifier(mReplacementRes, "drawable", mIconPack.mPackageName);

                if (resID == 0) //apparently not present
                {
                    return null;
                }

                XmlPullParser parser = packResources.getXml(resID);

                return Drawable.createFromXml(packResources, parser);
            } catch (Throwable ignored) {
            }
            return null;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof ReplacementIcon) {
                return ((ReplacementIcon) other).mIconPack.equals(mIconPack) && ((ReplacementIcon) other).mReplacementRes.equals(mReplacementRes);
            }
            return false;
        }

        public boolean isEnabled() {
            return mEnabled;
        }

        @NonNull
        @Override
        public String toString() {
            return "ReplacementIcon{" +
                    "mIconPack=" + mIconPack +
                    ", mReplacementRes='" + mReplacementRes + '\'' +
                    ", mEnabled=" + mEnabled +
                    '}';
        }

    }

    /**
     * @noinspection DataFlowIssue
     */
    public class ResourceMapping extends HashMap<String, ArrayList<ReplacementIcon>> {
        public void add(String originalRes, ReplacementIcon replacementIcon) {
            if (!containsKey(originalRes)) {
                put(originalRes, new ArrayList<>());
            }
            get(originalRes).add(replacementIcon);
        }

        public List<String> getOriginalResList() {
            return new ArrayList<>(keySet());
        }

        public List<ReplacementIcon> getReplacementIcons(String originalRes) {
            return get(originalRes);
        }

        public void reset(String resName) {
            if (!containsKey(resName)) return;

            this.get(resName).forEach(replacementIcon -> replacementIcon.mEnabled = false);
        }

        public void setEnabled(String resName, ReplacementIcon replacementIcon, boolean enabled) {
            if (!containsKey(resName)) return;

            reset(resName);
            get(resName).forEach(rep -> {
                if (rep.equals(replacementIcon)) rep.mEnabled = enabled;
            });
        }
    }

    /**
     * @noinspection DataFlowIssue
     */
    public class IconPackMapping extends HashMap<IconPack, HashMap<String, ArrayList<ReplacementIcon>>> {

        public void add(IconPack iconPack, String resName, ReplacementIcon replacementIcon) {
            if (!containsKey(iconPack)) {
                put(iconPack, new HashMap<>());
            }
            HashMap<String, ArrayList<ReplacementIcon>> thisPack = get(iconPack);

            if (!thisPack.containsKey(resName)) {
                thisPack.put(resName, new ArrayList<>());
            }
            thisPack.get(resName).add(replacementIcon);
        }

        public List<IconPack> getIconPacks() {
            return new ArrayList<>(keySet());
        }

        /**
         * @noinspection unused
         */
        public List<String> getResNames(IconPack iconPack) {
            return new ArrayList<>(get(iconPack).keySet());
        }

        /**
         * @noinspection unused
         */
        public List<ReplacementIcon> getReplacementIcons(IconPack iconPack, String resName) {
            return get(iconPack).get(resName);
        }
    }
}