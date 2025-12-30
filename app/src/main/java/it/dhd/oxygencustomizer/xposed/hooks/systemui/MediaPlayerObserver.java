package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.ArrayList;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

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
            ReflectedClass OplusQsMediaPanelView = ReflectedClass.of(
                    "com.oplus.systemui.qs.media.OplusQsBaseMediaPanelView", // OOS16 - They added OplusQsNormalMediaPanelView and OplusQsLargeMediaPanelView
                    "com.oplus.systemui.qs.media.OplusQsMediaPanelView");
            OplusQsMediaPanelView
                    .after("bindTitleAndText")
                    .run(param -> bindMediaData(param.args[0]));
            OplusQsMediaPanelView
                    .after("bindMediaData")
                    .run(param -> {
                        Object mediaData = param.args[0];
                        if (mediaData == null) {
                            unBindMediaData();
                            return;
                        }
                        bindMediaData(param.args[0]);
                    });

            OplusQsMediaPanelView
                    .after("unBindMediaData")
                    .run(param -> unBindMediaData());
            // OOS15.0.1 method & OOS16
            OplusQsMediaPanelView
                    .after("resetButtonEnableState")
                    .run(param -> unBindMediaData());
            OplusQsMediaPanelView
                    .after("setMediaData")
                    .run(param -> {
                        if (param.args[0] == null) {
                            unBindMediaData();
                        }
                    });
        } catch (Throwable t) {
            log(t);
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
