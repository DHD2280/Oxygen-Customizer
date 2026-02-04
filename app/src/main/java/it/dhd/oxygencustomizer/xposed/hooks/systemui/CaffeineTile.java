package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.service.quicksettings.Tile.STATE_ACTIVE;
import static android.service.quicksettings.Tile.STATE_INACTIVE;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setIntField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_TILE_REMOVED;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class CaffeineTile extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private static final String TARGET_SPEC = "custom(" + BuildConfig.APPLICATION_ID + "/.services.tiles.CaffeineTileService)";
    private static final int[] DURATIONS = new int[]{
            5 * 60,   // 5 min
            10 * 60,  // 10 min
            30 * 60,  // 30 min
            -1,       // infinity
    };
    private static final int INFINITE_DURATION_INDEX = DURATIONS.length - 1;
    private final PowerManager.WakeLock mWakeLock;
    public long mLastClickTime = -1;
    private int mSecondsRemaining;
    private int mDuration;
    private CountDownTimer mCountdownTimer = null;
    private boolean mRegistered = false;
    private View mTileView;
    private Object mTileObject;
    private Object mTileState;
    private int mCurrentState = STATE_INACTIVE;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, android.content.Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_SCREEN_OFF) || intent.getAction().equals(ACTION_TILE_REMOVED)) {
                    stopCountDown(true);
                    if (mWakeLock.isHeld())
                        mWakeLock.release();
                    updateTileView((LinearLayout) mTileView, STATE_INACTIVE);
                    updateTileViewPlugin(mTileObject, STATE_INACTIVE);
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
    public void updatePrefs(String... Key) {
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!mRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_SCREEN_OFF);
            filter.addAction(ACTION_TILE_REMOVED);
            mContext.registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
            mRegistered = true;
        }

        if (Build.VERSION.SDK_INT >= 35) {
            ReflectedClass QSPanelControllerBaseClass = ReflectedClass.of("com.android.systemui.qs.QSPanelControllerBase");
            QSPanelControllerBaseClass
                    .after("setTiles")
                    .run(param -> {
                        ((ArrayList<?>) getObjectField(param.thisObject, "mRecords")).forEach(record ->
                        {
                            Object tile = getObjectField(record, "tile");

                            if (TARGET_SPEC.equals(getObjectField(tile, "mTileSpec"))) {
                                mTileView = (View) getObjectField(record, "tileView");
                                setupTile(tile, mTileView);
                            }
                        });
                    });
        }

        ReflectedClass OplusQSPageViewController = ReflectedClass.ofIfPossible("com.oplus.systemui.plugins.qs.page.OplusQSPageViewController");
        if (OplusQSPageViewController.getClazz() != null) {
            OplusQSPageViewController
                    .after("setTiles")
                    .run(param -> {
                        ((ArrayList<?>) getObjectField(param.thisObject, "records")).forEach(record ->
                        {
                            Object tile = getObjectField(record, "tile");

                            if (TARGET_SPEC.equals(getObjectField(tile, "mTileSpec"))) {
                                mTileView = (View) getObjectField(record, "tileView");
                                setupTile(tile, mTileView);
                            }
                        });
                    });
        }

        ReflectedClass OplusQSResizeableTileView = ReflectedClass.ofIfPossible("com.oplus.systemui.plugins.qs.customize.view.tile.OplusQSResizeableTileView");
        OplusQSResizeableTileView
                .before("init")
                .run(param -> {
                    Object qsTile = param.args[0];
                    String tileSpec = (String) callMethod(qsTile, "getTileSpec");
                    if (TextUtils.isEmpty(tileSpec)) return;

                    if (TARGET_SPEC.equals(tileSpec)) {
                        setupTile(qsTile, (View) param.thisObject);
                        mTileObject = param.thisObject;
                        setAdditionalInstanceField(param.thisObject, "mTag", "caffeine");
                    }
                });
        ReflectedClass OplusQsOneXOne = ReflectedClass.ofIfPossible("com.oplus.systemui.plugins.qs.customize.view.tile.OplusQSResizeableTileViewOneXOne");
        OplusQsOneXOne
                .before("bindClickListener")
                .run(param -> {
                    if (getAdditionalInstanceField(param.thisObject, "mTileTag") != null &&
                            getAdditionalInstanceField(param.thisObject, "mTileTag").equals("caffeine")) {
                        param.args[0] = new ClickListener();
                        param.args[1] = new ClickListener();
                        param.args[2] = (View.OnLongClickListener) view -> {
                            handleLongClick();
                            return true;
                        };
                    }
                });
        OplusQSResizeableTileView
                .before("access$handleStateChanged")
                .run(param -> {
                    try {
                        Object tileState = param.args[1];
                        String spec = (String) getObjectField(tileState, "spec");
                        if (!TextUtils.isEmpty(spec) && spec.equals(TARGET_SPEC) &&
                                mCurrentState == STATE_ACTIVE) {
                            mTileState = tileState;
                            setIntField(mTileState, "state", mCurrentState);
                            updateTileViewPlugin(param.thisObject, mCurrentState);
                        }
                    } catch (Throwable ignored) {
                    }
                });

        ReflectedClass QSTileViewImplClass = ReflectedClass.of(
                "com.oplus.systemui.qs.base.tile.OplusQSTileBaseView", /* OOS15 */
                "com.oplus.systemui.qs.qstileimpl.OplusQSTileBaseView", /* OOS14 */
                "com.oplusos.systemui.qs.qstileimpl.OplusQSTileBaseView" /* OOS13 */);
        QSTileViewImplClass
                .after("handleStateChanged")
                .run(param -> {
                    try {
                        if (getAdditionalInstanceField(param.thisObject, "mParentTile") != null) {
                            updateTileView((LinearLayout) param.thisObject, (int) getObjectField(param.args[0] /* QSTile.State */, "state"));
                        }
                    } catch (Throwable ignored) {
                    }
                });

        ReflectedClass QSTileImpl = ReflectedClass.of("com.android.systemui.qs.tileimpl.QSTileImpl");
        QSTileImpl
                .before("handleLongClick")
                .run(param -> {
                    try {
                        View v = (Build.VERSION.SDK_INT >= 35) ?
                                (View) callMethod(param.args[0], "getView") :
                                (View) param.args[0];
                        if (v != null) {
                            if (getAdditionalInstanceField(v, "mTileTag") != null &&
                                    getAdditionalInstanceField(v, "mTileTag").equals("caffeine")) {
                                handleLongClick();
                                param.setResult(null);
                            }
                        }
                    } catch (Throwable t) {
                        log("Error handling long click: " + t.getMessage());
                    }
                });

    }

    private void setupTile(Object tile, View tileView) {
        setAdditionalInstanceField(tileView, "mParentTile", tile);
        setAdditionalInstanceField(tileView, "mTileTag", "caffeine");
        setOnClickListener(tileView);

    }

    private void updateTileView(LinearLayout tileView, int state) {
        try { //don't crash systemui if failed

            TextView label = (TextView) getObjectField(tileView, "mLabel");
            String newLabel = formatValueWithRemainingTime();
            label.post(() -> label.setText(state == STATE_ACTIVE ? newLabel : modRes.getString(R.string.caffeine)));

        } catch (Throwable ignored) {
        }
    }

    private void updateTileViewPlugin(Object tileView, int state) {
        try { //don't crash systemui if failed
            mCurrentState = state;
            Object QsLabelView = getObjectField(tileView, "labelView");
            String newLabel = formatValueWithRemainingTime();
            callMethod(QsLabelView, "updateText", state == STATE_ACTIVE ? newLabel : modRes.getString(R.string.caffeine));
            if (mTileState == null) mTileState = getObjectField(tileView, "tileState");
            setIntField(mTileState, "state", state);
        } catch (Throwable ignored) {
        }
    }

    @SuppressLint("DefaultLocale")
    private String formatValueWithRemainingTime() {

        if (mSecondsRemaining == -1) {
            return "\u221E"; // infinity
        }
        return String.format("%02d:%02d",
                mSecondsRemaining / 60 % 60, mSecondsRemaining % 60);
    }

    private void setOnClickListener(View tileView) {
        final ClickListener clickListener = new ClickListener();
        tileView.setOnClickListener(clickListener);
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
        if (callChange) handleStateChange(mTileObject, mTileView, STATE_INACTIVE, false);
    }

    private void startCountDown(long duration) {
        stopCountDown(false);
        mSecondsRemaining = (int) duration;
        if (duration == -1) {
            // infinity timing, no need to start timer
            handleStateChange(mTileObject, mTileView, STATE_ACTIVE, true);
            return;
        }
        handleStateChange(mTileObject, mTileView, STATE_ACTIVE, false);
        mCountdownTimer = new CountDownTimer(duration * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mSecondsRemaining = (int) (millisUntilFinished / 1000);
                updateTileView((LinearLayout) mTileView, STATE_ACTIVE); // sets time
                updateTileViewPlugin(mTileObject, STATE_ACTIVE);
            }

            @Override
            public void onFinish() {
                if (mWakeLock.isHeld())
                    mWakeLock.release();
                handleStateChange(mTileObject, mTileView, STATE_INACTIVE, true);
            }

        }.start();
    }

    private void handleStateChange(Object tileViewSplit, View thisView, int newState, boolean force) {
        if (thisView != null) {
            new Thread(() -> {
                Object parentTile = getAdditionalInstanceField(thisView, "mParentTile");

                Object mTile = getObjectField(parentTile, "mTile");

                int currentState = (int) getObjectField(mTile, "mState");

                if (force || currentState != newState) {
                    mCurrentState = newState;
                    setObjectField(mTile, "mState", newState);
                    callMethod(parentTile, "refreshState");
                }
                thisView.post(() -> updateTileView((LinearLayout) thisView, newState));
            }).start();
        }
        if (tileViewSplit != null) {
            if (mTileState == null) mTileState = getObjectField(mTileObject, "tileState");
            setIntField(mTileState, "state", newState);
            mCurrentState = newState;
            callMethod(mTileObject, "onStateChanged", mTileState);
            callMethod(mTileObject, "setStateImmediately", mTileState);
            updateTileViewPlugin(mTileObject, newState);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
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
}
