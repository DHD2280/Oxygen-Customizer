package it.dhd.oxygencustomizer.xposed.views.nowbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.PowerManager;
import android.view.View;
import android.widget.AbsSeekBar;

import androidx.core.graphics.ColorUtils;

import com.coui.appcompat.seekbar.COUIIntentSeekBar;
import com.coui.appcompat.seekbar.COUISeekBar;
import com.coui.appcompat.seekbar.PlaybackState;
import com.coui.appcompat.seekbar.core.COUISeekBarCore;

final class NowBarSeekBar {

    interface Listener {
        void onProgressChanged(int progress, boolean fromUser);

        void onStartTrackingTouch();

        void onStopTrackingTouch();
    }

    private static final int TRACK_ALPHA = 0x33;

    private final AbsSeekBar mView;
    private final COUIIntentSeekBar mIntentSeekBar;
    private final boolean mLight;
    private Boolean mPlaying;

    private NowBarSeekBar(AbsSeekBar view, COUIIntentSeekBar intentSeekBar, boolean light) {
        mView = view;
        mIntentSeekBar = intentSeekBar;
        mLight = light;
    }

    static NowBarSeekBar create(Context context, Listener listener) {
        if (Build.VERSION.SDK_INT >= 37) {
            try {
                return createIntent(context, listener);
            } catch (Throwable ignored) {
            }
        }
        COUISeekBar seekBar = new COUISeekBar(context);
        seekBar.setOnSeekBarChangeListener(new COUISeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(COUISeekBar bar, int progress, boolean fromUser) {
                listener.onProgressChanged(progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(COUISeekBar bar) {
                listener.onStartTrackingTouch();
            }

            @Override
            public void onStopTrackingTouch(COUISeekBar bar) {
                listener.onStopTrackingTouch();
            }
        });
        return new NowBarSeekBar(seekBar, null, false);
    }

    private static NowBarSeekBar createIntent(Context context, Listener listener) {
        COUIIntentSeekBar seekBar = new COUIIntentSeekBar(context);
        seekBar.setOnSeekBarChangeListener(new COUISeekBarCore.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(COUISeekBarCore bar, int progress, boolean fromUser) {
                listener.onProgressChanged(progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(COUISeekBarCore bar) {
                listener.onStartTrackingTouch();
            }

            @Override
            public void onStopTrackingTouch(COUISeekBarCore bar) {
                listener.onStopTrackingTouch();
            }
        });
        seekBar.setThumbOffset(0);
        seekBar.setMoveType(1);
        seekBar.setFollowSystemTheme(false);
        seekBar.setEnableVibrator(false);
        PowerManager power = context.getSystemService(PowerManager.class);
        boolean light = power == null || !power.isPowerSaveMode();
        seekBar.setUseMaterialEffect(light ? 1 : 0);
        return new NowBarSeekBar(seekBar, seekBar, light);
    }

    View getView() {
        return mView;
    }

    void setRange(int min, int max) {
        mView.setMin(min);
        mView.setMax(max);
    }

    void setProgress(int progress) {
        mView.setProgress(progress);
    }

    void setColor(int color) {
        ColorStateList tint = ColorStateList.valueOf(color);
        if (mIntentSeekBar == null) {
            COUISeekBar seekBar = (COUISeekBar) mView;
            seekBar.setProgressColor(tint);
            seekBar.setThumbColor(tint);
            return;
        }
        mIntentSeekBar.setConicLightColor(color);
        ColorStateList track = ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, TRACK_ALPHA));
        mIntentSeekBar.setSeekBarColors(track, mLight ? null : tint, null);
    }

    void setPlaying(boolean playing) {
        if (mIntentSeekBar == null || Boolean.valueOf(playing).equals(mPlaying)) return;
        mPlaying = playing;
        mIntentSeekBar.setPlaybackState(playing ? PlaybackState.PLAYING : PlaybackState.PAUSED);
    }
}
