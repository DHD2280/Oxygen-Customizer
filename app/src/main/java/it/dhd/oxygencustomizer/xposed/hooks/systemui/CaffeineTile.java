package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.service.quicksettings.Tile.STATE_ACTIVE;
import static android.service.quicksettings.Tile.STATE_INACTIVE;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_TILE_REMOVED;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class CaffeineTile extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private static final String TARGET_SPEC = "custom(" + BuildConfig.APPLICATION_ID + "/.services.tiles.CaffeineTileService)";
    private static final String TAG = "Oxygen Customizer - Caffeine Tile: ";

    private final PowerManager.WakeLock mWakeLock;
    private int mSecondsRemaining;
    private int mDuration;
    private static final int[] DURATIONS = new int[]{
            5 * 60,   // 5 min
            10 * 60,  // 10 min
            30 * 60,  // 30 min
            -1,       // infinity
    };

    private static final int INFINITE_DURATION_INDEX = DURATIONS.length - 1;
    private CountDownTimer mCountdownTimer = null;
    public long mLastClickTime = -1;
    private boolean mRegistered = false;
    private View mTileView;
    private Object mTile;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, android.content.Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_SCREEN_OFF) || intent.getAction().equals(ACTION_TILE_REMOVED)) {
                    stopCountDown(true);
                    if (mWakeLock.isHeld())
                        mWakeLock.release();
                    updateTileView((LinearLayout) mTileView, STATE_INACTIVE);
                }
            }
        }
    };

    public CaffeineTile(Context context) {
        super(context);
        mWakeLock = mContext.getSystemService(PowerManager.class).newWakeLock(
                PowerManager.FULL_WAKE_LOCK, "OC:CaffeineTile");
    }

    @Override
    public void updatePrefs(String... Key) {}

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!mRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_SCREEN_OFF);
            filter.addAction(ACTION_TILE_REMOVED);
            mContext.registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
            mRegistered = true;
        }

        Class<?> QSPanelControllerBaseClass = findClass("com.android.systemui.qs.QSPanelControllerBase", lpparam.classLoader);
        hookAllMethods(QSPanelControllerBaseClass, "setTiles", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ((ArrayList<?>) getObjectField(param.thisObject, "mRecords")).forEach(record ->
                {
                    Object tile = getObjectField(record, "tile");

                    if (TARGET_SPEC.equals(getObjectField(tile, "mTileSpec"))) {
                        mTileView = (View) getObjectField(record, "tileView");
                        setupTile(tile, mTileView);
                    }
                });
            }
        });

        Class<?> QSTileViewImplClass;
        try {
            QSTileViewImplClass = findClass("com.oplus.systemui.qs.qstileimpl.OplusQSTileBaseView", lpparam.classLoader);
        } catch (Throwable t) {
            QSTileViewImplClass = findClass("com.oplusos.systemui.qs.qstileimpl.OplusQSTileBaseView", lpparam.classLoader); // OOS 13
        }
        hookAllMethods(QSTileViewImplClass, "handleStateChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    if (getAdditionalInstanceField(param.thisObject, "mParentTile") != null) {
                        mTile = param.args[0];
                        log(TAG + "handleStateChanged");
                        updateTileView((LinearLayout) param.thisObject, (int) getObjectField(param.args[0] /* QSTile.State */, "state"));
                    }
                } catch (Throwable ignored) {
                }
            }
        });

        Class<?> QSTileImpl = findClass("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.classLoader);
        hookAllMethods(QSTileImpl, "handleLongClick", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                try {
                    View v = (View) param.args[0];
                    if (v != null) {
                        if (getAdditionalInstanceField(v, "mTileTag") != null &&
                                getAdditionalInstanceField(v, "mTileTag").equals("caffeine")) {
                            handleLongClick();
                            param.setResult(null);
                        }
                    }
                } catch (Throwable t) {
                    log(TAG + "Error handling long click: " + t.getMessage());
                }
            }
        });

    }

    private void setupTile(Object tile, View tileView) {

        setAdditionalInstanceField(tileView, "mParentTile", tile);
        setAdditionalInstanceField(tileView, "mTileTag", "caffeine");
        setOnClickListener((LinearLayout) tileView);

    }

    private void updateTileView(LinearLayout tileView, int state) {
        try { //don't crash systemui if failed

            TextView label = (TextView) getObjectField(tileView, "mLabel");
            String newLabel = formatValueWithRemainingTime();
            label.post(() -> label.setText(state == STATE_ACTIVE ? newLabel : modRes.getString(R.string.caffeine)));

        } catch (Throwable ignored) {}
    }

    @SuppressLint("DefaultLocale")
    private String formatValueWithRemainingTime() {

        if (mSecondsRemaining == -1) {
            return "\u221E"; // infinity
        }
        return String.format("%02d:%02d",
                mSecondsRemaining / 60 % 60, mSecondsRemaining % 60);
    }

    private void setOnClickListener(LinearLayout tileView) {
        final ClickListener clickListener = new ClickListener();
        tileView.setOnClickListener(clickListener);
    }

    class ClickListener implements View.OnClickListener {

        public ClickListener() {
        }

        @Override
        public void onClick(View v) {
            if (mWakeLock.isHeld() && (mLastClickTime != -1) &&
                    (SystemClock.elapsedRealtime() - mLastClickTime < 5000)) {
                // cycle duration
                mDuration++;
                if (mDuration >= DURATIONS.length) {
                    // all durations cycled, turn if off
                    mDuration = -1;
                    stopCountDown(true);
                    if (mWakeLock.isHeld()) {
                        mWakeLock.release();
                    }
                } else {
                    // change duration
                    startCountDown(DURATIONS[mDuration]);
                    if (!mWakeLock.isHeld()) {
                        mWakeLock.acquire();
                    }
                }
            } else {
                // toggle
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                    stopCountDown(true);
                } else {
                    mWakeLock.acquire();
                    mDuration = 0;
                    startCountDown(DURATIONS[mDuration]);
                }
            }
            mLastClickTime = SystemClock.elapsedRealtime();
        }
    }

    private void handleLongClick() {
        if (mWakeLock.isHeld()) {
            if (mDuration == INFINITE_DURATION_INDEX) {
                return;
            }
        } else {
            mWakeLock.acquire();
        }
        mDuration = INFINITE_DURATION_INDEX;
        startCountDown(DURATIONS[INFINITE_DURATION_INDEX]);
    }

    private void stopCountDown(boolean callChange) {
        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
            mCountdownTimer = null;
        }
        if (callChange) handleStateChange(mTileView, STATE_INACTIVE, false);
    }

    private void startCountDown(long duration) {
        stopCountDown(false);
        mSecondsRemaining = (int) duration;
        if (duration == -1) {
            // infinity timing, no need to start timer
            handleStateChange(mTileView, STATE_ACTIVE, true);
            return;
        }
        handleStateChange(mTileView, STATE_ACTIVE, false);
        mCountdownTimer = new CountDownTimer(duration * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mSecondsRemaining = (int) (millisUntilFinished / 1000);
                updateTileView((LinearLayout) mTileView, STATE_ACTIVE); // sets time
            }

            @Override
            public void onFinish() {
                if (mWakeLock.isHeld())
                    mWakeLock.release();
                handleStateChange(mTileView, STATE_INACTIVE, true);
            }

        }.start();
    }

    private void handleStateChange(View thisView, int newState, boolean force) {
        if (thisView == null) return;
        new Thread(() -> {
            Object parentTile = getAdditionalInstanceField(thisView, "mParentTile");

            Object mTile = getObjectField(parentTile, "mTile");

            int currentState = (int) getObjectField(mTile, "mState");

            if (force || currentState != newState) {
                setObjectField(mTile, "mState", newState);
                callMethod(parentTile, "refreshState");
            }
            thisView.post(() -> updateTileView((LinearLayout) thisView, newState));
        }).start();
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
