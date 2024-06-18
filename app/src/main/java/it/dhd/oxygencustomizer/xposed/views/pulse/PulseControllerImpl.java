package it.dhd.oxygencustomizer.xposed.views.pulse;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.util.concurrent.Executor;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.views.PulseView;
import it.dhd.oxygencustomizer.xposed.views.VisualizerView;

public class PulseControllerImpl {

    public static final boolean DEBUG = BuildConfig.DEBUG;
    @SuppressLint("StaticFieldLeak")
    private static PulseControllerImpl instance = null;
    private final String STREAM_MUTE_CHANGED_ACTION = "android.media.STREAM_MUTE_CHANGED_ACTION";
    private final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";

    private static final String TAG = PulseControllerImpl.class.getSimpleName();
    private static final int RENDER_STYLE_LEGACY = 0;
    private static final int RENDER_STYLE_CM = 1;
    private static final int RENDER_STYLE_LINE = 2;
    private static final int RENDER_STYLE_CIRCLE = 3;
    private static final int RENDER_STYLE_CIRCLE_BAR = 4;

    private final Context mContext;
    private static AudioManager mAudioManager;
    private static Renderer mRenderer;
    private static VisualizerStreamHandler mStreamHandler;
    private static ColorController mColorController;
    private static PulseView mPulseView;
    private int mPulseStyle;
    private final PowerManager mPowerManager;

    // Pulse state
    private boolean mLinked;
    private boolean mPowerSaveModeEnabled;
    private boolean mScreenOn = true; // MUST initialize as true
    private boolean mMusicStreamMuted;
    private boolean mLeftInLandscape = false;
    private boolean mScreenPinningEnabled;
    private boolean mIsMediaPlaying;
    private boolean mAttached;

    private boolean mNavPulseEnabled;
    private boolean mLsPulseEnabled;
    private boolean mAmbPulseEnabled;
    private boolean mPulseEnabled;

    private boolean mKeyguardShowing;
    private boolean mDozing;
    private boolean mKeyguardGoingAway;
    private FrameLayout mNavBar = null;
    private FrameLayout mAodRootLayout = null;
    private Executor mBgHandler = command -> new Handler(Looper.getMainLooper()).post(command);

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            log("onReceive() " + action);
            if (!mPulseEnabled) return;
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mScreenOn = false;
                doLinkage();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mScreenOn = true;
                doLinkage();
            } else if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(intent.getAction())) {
                mPowerSaveModeEnabled = mPowerManager.isPowerSaveMode();
                doLinkage();
            } else if (STREAM_MUTE_CHANGED_ACTION.equals(intent.getAction())
                    || (VOLUME_CHANGED_ACTION.equals(intent.getAction()))) {
                log("STREAM_MUTE_CHANGED_ACTION or VOLUME_CHANGED_ACTION" + intent.getAction() + " streamType: " + intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1));
                int streamType = intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1);
                if (streamType == AudioManager.STREAM_MUSIC) {
                    boolean muted = isMusicMuted(streamType);
                    if (mMusicStreamMuted != muted) {
                        mMusicStreamMuted = muted;
                        doLinkage();
                    }
                }
            }
        }
    };

    private final VisualizerStreamHandler.Listener mStreamListener = new VisualizerStreamHandler.Listener() {
        @Override
        public void onStreamAnalyzed(boolean isValid) {
            if (mRenderer != null) {
                mRenderer.onStreamAnalyzed(isValid);
            }
            if (isValid) {
                turnOnPulse();
            } else {
                doSilentUnlinkVisualizer();
            }
        }

        @Override
        public void onFFTUpdate(byte[] bytes) {
            if (mRenderer != null && bytes != null) {
                mRenderer.onFFTUpdate(bytes);
            }
        }

        @Override
        public void onWaveFormUpdate(byte[] bytes) {
            if (mRenderer != null && bytes != null) {
                mRenderer.onWaveFormUpdate(bytes);
            }
        }
    };

    public static PulseControllerImpl getInstance(Context context) {
        if (instance != null) return instance;
        return new PulseControllerImpl(context);
    }

    public static PulseControllerImpl getInstance() {
        return instance;
    }

    public void setPulseEnabled(boolean enabled) {
        mPulseEnabled = enabled;
        updatePulseVisibility();
    }

    public void setNavbarPulseEnabled(boolean enabled) {
        mNavPulseEnabled = enabled;
        updatePulseVisibility();
    }

    public void setLockscreenPulseEnabled(boolean enabled) {
        mLsPulseEnabled = enabled;
        updatePulseVisibility();
    }

    public void setAmbientPulseEnabled(boolean enabled) {
        mAmbPulseEnabled = enabled;
        updatePulseVisibility();
    }

    public void setPulseRenderStyle(int style) {
        log("setPulseRenderStyle() " + style);
        mPulseStyle = style;
        loadRenderer();
    }

    public void notifyKeyguardGoingAway() {
        if (mLsPulseEnabled) {
            mKeyguardGoingAway = true;
            updatePulseVisibility();
            mKeyguardGoingAway = false;
        }
    }

    private void updatePulseVisibility() {
        log("updatePulseVisibility() " + mKeyguardShowing + " " + mDozing + " " + mPulseEnabled);

        VisualizerView vv = VisualizerView.getInstance();
        boolean allowAmbPulse = vv != null && vv.isAttached()
                && mAmbPulseEnabled && mKeyguardShowing && mDozing;
        boolean allowLsPulse = vv != null && vv.isAttached()
                && mLsPulseEnabled && mKeyguardShowing && !mDozing;
        boolean allowNavPulse = vv != null && vv.isAttached()
                && mNavPulseEnabled && !mKeyguardShowing && !mDozing;

        if (mKeyguardGoingAway) {
            detachPulseFrom(vv, allowNavPulse /*keep linked*/);
        } else if (allowNavPulse) {
            detachPulseFrom(vv, allowNavPulse /*keep linked*/);
            attachPulseTo(mNavBar);
        } else if (allowLsPulse) {
            detachPulseFrom(mNavBar, allowNavPulse || allowLsPulse || allowAmbPulse /*keep linked*/);
            if (mAmbPulseEnabled) detachPulseFrom(mAodRootLayout, allowNavPulse || allowAmbPulse /*keep linked*/);
            attachPulseTo(vv);
        } else if (allowAmbPulse) {
            detachPulseFrom(mNavBar, allowLsPulse || allowNavPulse /*keep linked*/);
            if (mLsPulseEnabled) detachPulseFrom(vv, allowLsPulse || allowNavPulse /*keep linked*/);
            attachPulseTo(mAodRootLayout);
        } else {
            detachPulseFrom(mNavBar, false /*keep linked*/);
            detachPulseFrom(mAodRootLayout, false /*keep linked*/);
            detachPulseFrom(vv, false /*keep linked*/);
        }
    }

    public void setDozing(boolean dozing) {
        if (mDozing != dozing) {
            mDozing = dozing;
            if (mPulseEnabled)
                updatePulseVisibility();
        }
    }

    public void setKeyguardShowing(boolean showing) {
        log("setKeyguardShowing() " + showing + " " + mKeyguardShowing);
        if (showing != mKeyguardShowing) {
            mKeyguardShowing = showing;
            if (mRenderer != null) {
                mRenderer.setKeyguardShowing(showing);
            }
            if (mPulseEnabled)
                updatePulseVisibility();
        }
    }

    private final Handler mHandler = new Handler();

    public PulseControllerImpl(
            Context context) {
        instance = this;
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMusicStreamMuted = isMusicMuted(AudioManager.STREAM_MUSIC);
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mPowerSaveModeEnabled = mPowerManager.isPowerSaveMode();
        mStreamHandler = new VisualizerStreamHandler(mContext, this, mStreamListener, mBgHandler);
        mPulseView = new PulseView(context, instance);
        mColorController = new ColorController(mContext);
        loadRenderer();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        filter.addAction(STREAM_MUTE_CHANGED_ACTION);
        filter.addAction(VOLUME_CHANGED_ACTION);
        context.registerReceiver(mBroadcastReceiver, filter, Context.RECEIVER_EXPORTED);
        log("PulseControllerImpl() done");
    }

    public void setNavbar(FrameLayout navBar) {
        mNavBar = navBar;
    }

    public void setAodRootLayout(FrameLayout aodRootLayout) {
        mAodRootLayout = aodRootLayout;
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    private void attachPulseTo(FrameLayout parent) {
        log("attachPulseTo() " + parent + " " + mAttached);
        if (parent == null) return;
        View v = parent.findViewWithTag(PulseView.TAG);
        if (v == null) {
            parent.addView(mPulseView);
            mAttached = true;
            doLinkage();
        }
    }

    private void detachPulseFrom(FrameLayout parent, boolean keepLinked) {
        if (parent == null) return;
        View v = parent.findViewWithTag(PulseView.TAG);
        if (v != null) {
            parent.removeView(mPulseView);
            mAttached = keepLinked;
            log("detachPulseFrom() ");
            doLinkage();
        }
    }

    private void loadRenderer() {
        log("loadRenderer() " + mPulseStyle);
        final boolean isRendering = shouldDrawPulse();
        if (isRendering) {
            mStreamHandler.pause();
        }
        if (mRenderer != null) {
            mRenderer.destroy();
            mRenderer = null;
        }
        mRenderer = getRenderer();
        mColorController.setRenderer(mRenderer);
        mRenderer.setLeftInLandscape(mLeftInLandscape);
        if (isRendering) {
            mRenderer.onStreamAnalyzed(true);
            mStreamHandler.resume();
        }
    }

    /*@Override
    public void screenPinningStateChanged(boolean enabled) {
        mScreenPinningEnabled = enabled;
        doLinkage();
    }

    @Override
    public void leftInLandscapeChanged(boolean isLeft) {
        if (mLeftInLandscape != isLeft) {
            mLeftInLandscape = isLeft;
            if (mRenderer != null) {
                mRenderer.setLeftInLandscape(isLeft);
            }
        }
    }*/

    /**
     * Current rendering state: There is a visualizer link and the fft stream is validated
     *
     * @return true if bar elements should be hidden, false if not
     */
    public boolean shouldDrawPulse() {
        return mLinked && mStreamHandler.isValidStream() && mRenderer != null;
    }

    public void onDraw(Canvas canvas) {
        if (shouldDrawPulse()) {
            mRenderer.draw(canvas);
        }
    }

    private void turnOnPulse() {
        if (shouldDrawPulse()) {
            mStreamHandler.resume(); // let bytes hit visualizer
        }
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mRenderer != null) {
            mRenderer.onSizeChanged(w, h, oldw, oldh);
        }
    }

    private Renderer getRenderer() {
        return switch (mPulseStyle) {
            case RENDER_STYLE_CM ->
                    new SolidLineRenderer(mContext, mHandler, mPulseView, instance, mColorController);
            case RENDER_STYLE_LINE ->
                    new LineRenderer(mContext, mHandler, mPulseView, instance, mColorController);
            default ->
                    new FadingBlockRenderer(mContext, mHandler, mPulseView, instance, mColorController);
        };
    }

    private boolean isMusicMuted(int streamType) {
        return streamType == AudioManager.STREAM_MUSIC &&
                (mAudioManager.isStreamMute(streamType) ||
                        mAudioManager.getStreamVolume(streamType) == 0);
    }

    private static void setVisualizerLocked(boolean doLock) {
        /*try {
            IBinder b = ServiceManager.getService(Context.AUDIO_SERVICE);
            IAudioService audioService = IAudioService.Stub.asInterface(b);
            audioService.setVisualizerLocked(doLock);
        } catch (RemoteException e) {
            Log.e(TAG, "Error setting visualizer lock");
        }*/
    }

    /**
     * if any of these conditions are met, we unlink regardless of any other states
     *
     * @return true if unlink is required, false if unlinking is not mandatory
     */
    private boolean isUnlinkRequired() {
        return (!mScreenOn && !mAmbPulseEnabled)
                || mPowerSaveModeEnabled
                || mMusicStreamMuted
                || mScreenPinningEnabled
                || !mAttached;
    }

    /**
     * All of these conditions must be met to allow a visualizer link
     *
     * @return true if all conditions are met to allow link, false if and conditions are not met
     */
    private boolean isAbleToLink() {
        return (mScreenOn || mAmbPulseEnabled)
                && mIsMediaPlaying
                && !mPowerSaveModeEnabled
                && !mMusicStreamMuted
                && !mScreenPinningEnabled
                && mAttached;
    }

    private boolean canActuallyLink() {
        return (mLsPulseEnabled) ||
                (mNavPulseEnabled && mNavBar != null) ||
                (mAmbPulseEnabled && mAodRootLayout != null);
    }

    private void doUnlinkVisualizer() {
        if (mStreamHandler != null) {
            if (mLinked) {
                mStreamHandler.unlink();
                setVisualizerLocked(false);
                mLinked = false;
                if (mRenderer != null) {
                    mRenderer.onVisualizerLinkChanged(false);
                }
                mPulseView.postInvalidate();
            }
        }
    }

    /**
     * Incoming event in which we need to
     * toggle our link state. Use runnable to
     * handle multiple events at same time.
     */
    private void doLinkage() {
        log("doLinkage() ");
        if (isUnlinkRequired()) {
            if (mLinked) {
                // explicitly unlink
                doUnlinkVisualizer();
            }
        } else {
            if (isAbleToLink() && canActuallyLink()) {
                doLinkVisualizer();
            } else if (mLinked) {
                doUnlinkVisualizer();
            }
        }
    }

    /**
     * Invalid media event not providing
     * a data stream to visualizer. Unlink
     * without calling into view. Like it
     * never happened
     */
    private void doSilentUnlinkVisualizer() {
        if (mStreamHandler != null) {
            if (mLinked) {
                mStreamHandler.unlink();
                setVisualizerLocked(false);
                mLinked = false;
            }
        }
    }

    /**
     * Link to visualizer after conditions
     * are confirmed
     */
    private void doLinkVisualizer() {
        log("doLinkVisualizer() " + mLinked);
        if (mStreamHandler != null) {
            if (!mLinked) {
                setVisualizerLocked(true);
                mStreamHandler.link();
                mLinked = true;
                if (mRenderer != null) {
                    mRenderer.onVisualizerLinkChanged(true);
                }
            }
        }
        log("doLinkVisualizer() done " + mLinked);
    }

    @Override
    public String toString() {
        return TAG + " " + getState();
    }

    private String getState() {
        return "canActuallyLink() = " + canActuallyLink() + " "
                + "isAbleToLink() = " + isAbleToLink() + " "
                + "shouldDrawPulse() = " + shouldDrawPulse() + " "
                + "mScreenOn = " + mScreenOn + " "
                + "mIsMediaPlaying = " + mIsMediaPlaying + " "
                + "mLinked = " + mLinked + " "
                + "mPowerSaveModeEnabled = " + mPowerSaveModeEnabled + " "
                + "mMusicStreamMuted = " + mMusicStreamMuted + " "
                + "mScreenPinningEnabled = " + mScreenPinningEnabled + " "
                + "mAttached = " + mAttached + " "
                + "mStreamHandler.isValidStream() = " + mStreamHandler.isValidStream() + " "
                + "mKeyguardShowing = " + mKeyguardShowing + " "
                + "mDozing = " + mDozing + " ";
    }

    private void log(String msg) {
        if (DEBUG) {
            XposedBridge.log(TAG + " " + msg + " " + getState());
            Log.i(TAG, msg + " " + getState());
        }
    }

    public void setScreenPinning(boolean arg) {
        log("setScreenPinning() " + arg);
        mScreenPinningEnabled = arg;
        doLinkage();
    }

    public void onPrimaryMetadataOrStateChanged(int state) {
        boolean isMusicActive = false;
        if (SystemUtils.AudioManager() != null) {
            isMusicActive = SystemUtils.AudioManager().isMusicActive();
        }
        boolean isPlaying = (isMusicActive || state == PlaybackState.STATE_PLAYING);
        if (mIsMediaPlaying != isPlaying) {
            mIsMediaPlaying = isPlaying;
            doLinkage();
        }
    }

    public void setColors(int mPulseColorMode, int mPulseColor, int mPulseLavaSpeed) {
        mColorController.setColorType(mPulseColorMode);
        mColorController.setCustomColor(mPulseColor);
        mColorController.setLavaLampSpeed(mPulseLavaSpeed);
    }
}
