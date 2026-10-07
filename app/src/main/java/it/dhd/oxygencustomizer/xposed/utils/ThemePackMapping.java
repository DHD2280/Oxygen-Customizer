package it.dhd.oxygencustomizer.xposed.utils;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.HashMap;

public class ThemePackMapping {
    public static final String DRAWABLE_MAPPING_KEY = "drawableMapping";
    public static final int TYPE_DRAWABLE = 0;

    public static class OverlayID {
        public int resID;
        public String packageName;

        public OverlayID(String packageName, int resID) {
            this.resID = resID;
            this.packageName = packageName;
        }
    }

    public static class OverlayIDName {
        public String resName;
        public String packageName;

        public OverlayIDName(String packageName, String resName) {
            this.resName = resName;
            this.packageName = packageName;
        }

        @NonNull
        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }

    public static class IDMapping extends HashMap<Integer, OverlayID> {
    }

    public static class Mapping extends HashMap<String, OverlayIDName> {
        /**
         * @noinspection NullableProblems
         */
        @Override
        public String toString() {
            return new Gson().toJson(this);
        }

        public static Mapping loadMapping(SharedPreferences prefs, int type) {
            //noinspection SwitchStatementWithTooFewBranches
            return switch (type) {
                case TYPE_DRAWABLE -> fromJSONString(prefs.getString(DRAWABLE_MAPPING_KEY, ""));
                default -> new Mapping();
            };
        }

        public static void saveMapping(SharedPreferences prefs, int type, Mapping mapping) {
            switch (type) {
                case TYPE_DRAWABLE ->
                        prefs.edit().putString(DRAWABLE_MAPPING_KEY, mapping.toString()).apply();
            }
        }

        public void add(String resName, String packageName, String replaceResName) {
            if (!containsKey(resName)) {
                put(resName, new OverlayIDName(packageName, replaceResName));
            } else {
                remove(resName);
                add(resName, packageName, replaceResName);
            }
        }

        public void replace(String resName, String packageName, String replaceResName) {
            if (containsKey(resName)) {
                remove(resName);
                put(resName, new OverlayIDName(packageName, replaceResName));
            }
        }

        public boolean isEnabled(String resName, String packageName, String replaceResName) {
            if (!containsKey(resName)) return false;

            OverlayIDName idName = get(resName);

            return idName.packageName.equals(packageName) && idName.resName.equals(replaceResName);
        }

        @NonNull
        private static Mapping fromJSONString(String jsonString) {
            Mapping mapping = new Gson().fromJson(jsonString, Mapping.class);
            return (mapping == null) ? new Mapping() : mapping;
        }
    }
}

