package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.provider.StepsProvider;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.xposed.utils.ArcProgressWidget;

@SuppressLint("ViewConstructor")
public class StepsWidget extends BaseDeviceWidget {

    private static final long REFRESH_MS = 60_000L;
    private static final int[] GOALS = {5000, 8000, 10000, 12000, 15000};
    private static final String KEY_GOAL = "goal";

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ImageView mProgress;
    private long mSteps = 0;
    private int mGoal = 10000;
    private boolean mRefreshing = false;

    private final Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            if (!isAttachedToWindow()) return;
            if (isShown() && isInteractive()) query();
            mHandler.postDelayed(this, REFRESH_MS);
        }
    };

    public StepsWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mProgress = new ImageView(mContext);
        mProgress.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mProgress.setLayoutParams(new LayoutParams(dp2px(mContext, 60), dp2px(mContext, 60)));
        addView(mProgress);
        if (mSettingsInterface) mSteps = 6420;
        render();
    }

    private boolean isInteractive() {
        PowerManager power = mContext.getSystemService(PowerManager.class);
        return power == null || power.isInteractive();
    }

    private void query() {
        if (mSettingsInterface || mRefreshing) return;
        mRefreshing = true;
        Context context = appContext != null ? appContext : mContext;
        EXECUTOR.execute(() -> {
            long steps = mSteps;
            try (Cursor cursor = context.getContentResolver().query(StepsProvider.CONTENT_URI, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) steps = cursor.getLong(0);
            } catch (Throwable ignored) {
            }
            long result = steps;
            mHandler.post(() -> {
                mRefreshing = false;
                mSteps = result;
                render();
            });
        });
    }

    private void render() {
        Drawable icon = ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.ic_steps, appContext.getTheme());
        if (icon != null) {
            icon = icon.mutate();
            icon.setTint(mTextColor);
        }
        String text;
        int progress = 0;
        if (mSteps == StepsProvider.NO_PERMISSION || mSteps == StepsProvider.NO_SENSOR) {
            text = "--";
        } else {
            text = mSteps >= 100_000 ? (mSteps / 1000) + "k" : String.valueOf(mSteps);
            progress = (int) Math.min(mSteps, mGoal);
        }
        Bitmap bitmap = ArcProgressWidget.generateBitmap(mContext, progress, mGoal, text, text.length() > 4 ? 32 : 40,
                icon, false, 36, null, mProgressColor, mTextColor);
        mProgress.setImageBitmap(bitmap);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler.removeCallbacks(mRefresh);
        if (!mSettingsInterface) mHandler.post(mRefresh);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mRefresh);
    }

    @Override
    public String getWidgetReference() {
        return getClass().getSimpleName();
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.steps_widget);
    }

    @Override
    public boolean hasBigMode() {
        return false;
    }

    @Override
    public boolean hasSmallMode() {
        return true;
    }

    @Override
    public View getBigPreview() {
        return null;
    }

    @Override
    public View getSmallPreview() {
        StepsWidget preview = new StepsWidget(mContext, true);
        preview.setMode(WidgetMode.SMALL);
        return preview;
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        List<SettingItem> settings = new ArrayList<>();
        List<String> entries = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (int goal : GOALS) {
            entries.add(String.valueOf(goal));
            values.add(String.valueOf(goal));
        }
        settings.add(new SettingItem(appContext.getString(R.string.steps_goal), KEY_GOAL, SettingItem.Type.LIST, String.valueOf(mGoal), entries, values));
        return settings;
    }

    @Override
    public void applySettings(Map<String, Object> settings) {
        Object goal = settings.get(KEY_GOAL);
        if (goal == null) return;
        try {
            mGoal = Math.max(1, goal instanceof Number number ? number.intValue() : Math.round(Float.parseFloat(String.valueOf(goal))));
        } catch (NumberFormatException ignored) {
        }
        if (mProgress != null) render();
    }

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        render();
    }

    @Override
    public void onSetDeviceName(String deviceName) {
    }
}
