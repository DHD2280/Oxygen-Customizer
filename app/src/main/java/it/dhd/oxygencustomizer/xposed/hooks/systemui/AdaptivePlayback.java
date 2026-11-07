package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findClass;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getIntField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getObjectField;

import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class AdaptivePlayback extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private static final Object ADAPTIVE_PLAYBACK_TOKEN = new Object();
    private final int STREAM_MUSIC = 3;
    private boolean mAdaptivePlaybackEnabled;
    private int mAdaptivePlaybackTimeout;
    private boolean mAdaptivePlaybackResumable;
    final Runnable mAdaptivePlaybackRunnable = () -> mAdaptivePlaybackResumable = false;
    private Class<?> mVolumeState = null;
    private Object mAudio = null;
    private Handler mWorker = null;

    public AdaptivePlayback(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        if (Xprefs == null) return;

        mAdaptivePlaybackEnabled = Xprefs.getBoolean(Constants.Preferences.ADAPTIVE_PLAYBACK_ENABLED, false);
        mAdaptivePlaybackTimeout = Xprefs.getInt(Constants.Preferences.ADAPTIVE_PLAYBACK_TIMEOUT, 30000);

        if (Key.length > 0 && Key[0].equals(Constants.Preferences.ADAPTIVE_PLAYBACK_ENABLED)) {
            if (!mAdaptivePlaybackEnabled && mWorker != null) {
                mWorker.removeCallbacksAndMessages(ADAPTIVE_PLAYBACK_TOKEN);
                mAdaptivePlaybackResumable = false;
            }
        }

    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        if (!PRParam.getPackageName().equals(listenPackage)) return;

        mVolumeState = findClass("com.android.systemui.plugins.VolumeDialogController$StreamState", PRParam.getClassLoader());

        Class<?> VolumeDialogControllerImpl = findClass("com.android.systemui.volume.VolumeDialogControllerImpl", PRParam.getClassLoader());
        ReflectedClass.of(VolumeDialogControllerImpl)
                .afterConstruction()
                .run(param -> {
                    mAudio = getObjectField(param.thisObject, "mAudio");
                    mWorker = (Handler) getObjectField(param.thisObject, "mWorker");
                });
        ReflectedClass.of(VolumeDialogControllerImpl)
                .after("updateStreamLevelW")
                .run(param -> {
                    if (!mAdaptivePlaybackEnabled) return;
                    Object volState = callMethod(param.thisObject, "streamStateW", param.args[0]);
                    int level = getIntField(volState, "level");
                    int stream = (int) param.args[0];
                    boolean isMusicActive = (boolean) callMethod(mAudio, "isMusicActive");
                    if (mAdaptivePlaybackEnabled && stream == STREAM_MUSIC && level == 0
                            && isMusicActive) {
                        callMethod(mAudio, "dispatchMediaKeyEvent", new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
                        callMethod(mAudio, "dispatchMediaKeyEvent", new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE));
                        mAdaptivePlaybackResumable = true;

                        mWorker.removeCallbacksAndMessages(ADAPTIVE_PLAYBACK_TOKEN);
                        mWorker.postDelayed(mAdaptivePlaybackRunnable, ADAPTIVE_PLAYBACK_TOKEN,
                                mAdaptivePlaybackTimeout);
                    }
                    if (stream == STREAM_MUSIC && level > 0 && mAdaptivePlaybackResumable) {
                        mWorker.removeCallbacksAndMessages(ADAPTIVE_PLAYBACK_TOKEN);
                        callMethod(mAudio, "dispatchMediaKeyEvent", new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
                        callMethod(mAudio, "dispatchMediaKeyEvent", new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
                        mAdaptivePlaybackResumable = false;
                    }
                });
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
