package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class MediaPlayerObserver extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;

    @SuppressLint("StaticFieldLeak")
    private static MediaPlayerObserver instance = null;

    private final ArrayList<OnBindMediaData> mMediaDataListeners = new ArrayList<>();

    public MediaPlayerObserver(Context context) {
        super(context);
        instance = this;
    }

    public static void registerMediaData(OnBindMediaData callback) {
        instance.mMediaDataListeners.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterregisterMediaData(OnBindMediaData callback) {
        instance.mMediaDataListeners.remove(callback);
    }

    @Override
    public void updatePrefs(String... Key) {
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        try {
            Class<?> OplusQsMediaPanelView = findClass("com.oplus.systemui.qs.media.OplusQsMediaPanelView", lpparam.classLoader);
            hookAllMethods(OplusQsMediaPanelView, "bindTitleAndText", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    bindMediaData(param.args[0]);
                }
            });
            hookAllMethods(OplusQsMediaPanelView, "unBindMediaData", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    unBindMediaData();
                }
            });
        } catch (Throwable t) {
            log("MediaPlayerObserver error: " + t.getMessage());
        }
    }

    private void bindMediaData(Object mediaData) {
        for (OnBindMediaData listener : mMediaDataListeners) {
            listener.onBindMediaData(mediaData);
        }
    }

    private void unBindMediaData() {
        for (OnBindMediaData listener : mMediaDataListeners) {
            listener.onUnBindMediaData();
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    public interface OnBindMediaData {
        void onBindMediaData(Object mediaData);

        void onUnBindMediaData();
    }
}
