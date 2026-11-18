package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.BatteryManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.adapters.DeviceWidgetsAdapter;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ThemeEnabler;
import it.dhd.oxygencustomizer.xposed.utils.LiquidGlass;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.Logger;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BaseDeviceWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WidgetMode;

public class DeviceWidgetView extends FrameLayout {

    private final Context mContext;
    private Context appContext;

    public int DEVICE_WIDGET_CLASSIC = 0;
    public int DEVICE_WIDGET_CIRCULAR = 1;
    public int DEVICE_WIDGET_CUSTOM = 2;

    private int mDeviceWidgetStyle = DEVICE_WIDGET_CLASSIC;
    private boolean mLiquidGlass = false;
    private LinearLayout mBatteryArcContainer;
    private LinearLayout mBatteryTempContainer;
    private final Map<View, int[]> mOriginalMargins = new HashMap<>();
    private static final int GRID_COLUMNS = 4;
    private static final int GLASS_GAP_DP = 12;
    private final Map<View, int[]> mOriginalBatteryLayout = new HashMap<>();
    private static final int GLASS_BAR_HEIGHT_DP = 6;
    private static final float GLASS_ARC_FILL = 0.78f;
    private float mWidgetScaling = 1f;
    private TextView mBatteryLevelView;
    private ProgressBar mBatteryProgress;
    private int mBatteryPercentage = 1;
    private LinearLayout mClassicRow, mArcRow, mCustomRow;
    private TextView mAddWidgetText;
    private LinearLayout mVolumeLevelContainer, mRamUsageContainer, mVolumeLevelContainerArc, mRamUsageContainerArc;
    private ProgressImageView mBatteryPercentArc, mBatteryTempArc, mVolumeLevelArcProgress, mRamUsageArcProgress;

    private boolean mCustomColor = false;
    private int mProgressColor = 0;
    private int mLinearProgressColor = 0;
    private int mTextColor = 0;
    private DeviceWidgetListener mSettingsWidgetListener = null;
    private String mDeviceName = Build.MODEL;

    private boolean mSettingsInterface = false;

    private final List<BaseDeviceWidget> mWidgets = new ArrayList<>();
    private final boolean[] slots = new boolean[4];
    private DeviceWidgetsAdapter.WidgetSettingsListener mSettingsListener;

    public interface DeviceWidgetListener {
        void onAddWidgetClicked();
        void onWidgetRemoved(BaseDeviceWidget widget);
    }

    private boolean batteryRegistered = false;
    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                mBatteryPercentage = (level * 100) / scale;
                initBatteryStatus();
            }
        }
    };

    public DeviceWidgetView(Context context, boolean settignsInterface) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        mSettingsInterface = settignsInterface;
        mContext = context;
        try {
            appContext = context.createPackageContext(
                    BuildConfig.APPLICATION_ID,
                    Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (!mSettingsInterface) {
            ThemeEnabler.registerThemeChangedListener(this::reloadColors);
        }

        inflateView();
    }

    public DeviceWidgetView(Context context) {
        this(context, false);
    }

    public void setAddWidgetListener(DeviceWidgetListener listener) {
        mSettingsWidgetListener = listener;
    }

    private void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(appContext);
        View view = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                "device_widget",
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );
        view.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        addView(view);
        setupViews();
    }

    private void setupViews() {

        mClassicRow = (LinearLayout) ViewHelper.findViewWithTag(this, "device_widget_classic");
        mArcRow = (LinearLayout) ViewHelper.findViewWithTag(this, "device_widget_arc");
        mCustomRow = (LinearLayout) ViewHelper.findViewWithTag(this, "device_widget_custom");

        // First row
        mBatteryLevelView = (TextView) ViewHelper.findViewWithTag(this, "battery_percentage");
        mBatteryProgress = (ProgressBar) ViewHelper.findViewWithTag(this, "battery_progressbar");
        mVolumeLevelContainer = (LinearLayout) ViewHelper.findViewWithTag(this, "volume_progress");
        mRamUsageContainer = (LinearLayout) ViewHelper.findViewWithTag(this, "ram_usage_info");
        // Volume progress
        if (mVolumeLevelArcProgress == null) {
            mVolumeLevelArcProgress = new ProgressImageView(mContext);
            mVolumeLevelArcProgress.setProgressType(ProgressImageView.ProgressType.VOLUME);
        }
        mVolumeLevelContainer.addView(mVolumeLevelArcProgress);
        // Ram usage progress
        if (mRamUsageArcProgress == null) {
            mRamUsageArcProgress = new ProgressImageView(mContext);
            mRamUsageArcProgress.setProgressType(ProgressImageView.ProgressType.MEMORY);
        }
        mRamUsageContainer.addView(mRamUsageArcProgress);

        mBatteryProgress.setProgressTintList(ColorStateList.valueOf(
                mCustomColor ?
                        mLinearProgressColor == 0 ?
                                getPrimaryColor(mContext) :
                                mLinearProgressColor :
                        getPrimaryColor(mContext)
        ));

        ((TextView) ViewHelper.findViewWithTag(this, "device_name")).setText(Build.MODEL);

        // Second Row
        mVolumeLevelContainerArc = (LinearLayout) ViewHelper.findViewWithTag(this, "volume_progress_2");
        mRamUsageContainerArc = (LinearLayout) ViewHelper.findViewWithTag(this, "memory_progress");

        LinearLayout batteryArc = (LinearLayout) ViewHelper.findViewWithTag(this, "battery_progress_arc");
        LinearLayout batteryTemp = (LinearLayout) ViewHelper.findViewWithTag(this, "temperature_progress");
        mBatteryArcContainer = batteryArc;
        mBatteryTempContainer = batteryTemp;

        if (mBatteryPercentArc == null) {
            mBatteryPercentArc = new ProgressImageView(mContext);
            mBatteryPercentArc.setProgressType(ProgressImageView.ProgressType.BATTERY);
        }
        batteryArc.addView(mBatteryPercentArc);

        if (mBatteryTempArc == null) {
            mBatteryTempArc = new ProgressImageView(mContext);
            mBatteryTempArc.setProgressType(ProgressImageView.ProgressType.TEMPERATURE);
        }
        batteryTemp.addView(mBatteryTempArc);

        // Custom row
        mAddWidgetText = (TextView) ViewHelper.findViewWithTag(this, "add_widget_text");
        mAddWidgetText.setOnClickListener(v -> {
            if (mSettingsWidgetListener != null) {
                mSettingsWidgetListener.onAddWidgetClicked();
            }
        });
        mCustomRow.setOnClickListener(v -> {
            if (mSettingsWidgetListener != null) {
                mSettingsWidgetListener.onAddWidgetClicked();
            }
        });
        if (mSettingsInterface) {
            int textColorPrimary = ResourcesCompat.getColor(appContext.getResources(), R.color.text_color_primary, appContext.getTheme());
            mAddWidgetText.setTextColor(textColorPrimary);
        }
        setupRows();
    }

    private void setupRows() {
        if (mDeviceWidgetStyle == DEVICE_WIDGET_CUSTOM) {
            mClassicRow.setVisibility(View.GONE);
            mArcRow.setVisibility(View.GONE);
            mCustomRow.setVisibility(View.VISIBLE);
            return;
        }
        LinearLayout volumeContainer = mDeviceWidgetStyle == DEVICE_WIDGET_CLASSIC ?
                mVolumeLevelContainer : mVolumeLevelContainerArc;
        LinearLayout ramContainer = mDeviceWidgetStyle == DEVICE_WIDGET_CLASSIC ?
                mRamUsageContainer : mRamUsageContainerArc;

        try {
            ((ViewGroup) mVolumeLevelArcProgress.getParent()).removeView(mVolumeLevelArcProgress);
            ((ViewGroup) mRamUsageArcProgress.getParent()).removeView(mRamUsageArcProgress);
        } catch (Throwable ignored) {}

        volumeContainer.addView(mVolumeLevelArcProgress);
        ramContainer.addView(mRamUsageArcProgress);

        mClassicRow.setVisibility(mDeviceWidgetStyle == DEVICE_WIDGET_CLASSIC ? View.VISIBLE : View.GONE);
        mArcRow.setVisibility(mDeviceWidgetStyle == DEVICE_WIDGET_CIRCULAR ? View.VISIBLE : View.GONE);
        mCustomRow.setVisibility(View.GONE);
    }

    public void setLiquidGlass(boolean enabled) {
        if (mLiquidGlass == enabled) return;
        mLiquidGlass = enabled;
        updateScaling();
        updateWidgetMargin();
    }

    private void applyLiquidGlass() {
        setGlass((View) mBatteryProgress.getParent());
        setGlass(mVolumeLevelContainer);
        setGlass(mRamUsageContainer);
        setGlass(mVolumeLevelContainerArc);
        setGlass(mRamUsageContainerArc);
        setGlass(mBatteryArcContainer);
        setGlass(mBatteryTempContainer);
        for (int i = 0; i < mCustomRow.getChildCount(); i++) {
            if (mCustomRow.getChildAt(i) instanceof BaseDeviceWidget widget) {
                setGlass(widget);
            }
        }
        int rowPadding = mLiquidGlass ? dp2px(mContext, 6) : 0;
        for (LinearLayout row : List.of(mClassicRow, mArcRow, mCustomRow)) {
            row.setPadding(rowPadding, 0, rowPadding, 0);
        }
        setPadding(0, 0, 0, mLiquidGlass ? dp2px(mContext, 12) : 0);
    }

    private void setGlass(View view) {
        if (view == null) return;
        if (mLiquidGlass) {
            LiquidGlass.apply(view, dp2px(mContext, 18), LiquidGlass.DEFAULT_COLOR);
        } else {
            view.setBackground(null);
        }
    }

    private void setHorizontalMargins(View view, LinearLayout.LayoutParams params, int glassMargin) {
        int[] original = mOriginalMargins.computeIfAbsent(view, v -> new int[]{params.leftMargin, params.rightMargin});
        params.leftMargin = mLiquidGlass ? glassMargin : original[0];
        params.rightMargin = mLiquidGlass ? glassMargin : original[1];
    }

    public void setDeviceWidgetStyle(int newStyle) {
        mDeviceWidgetStyle = newStyle;
        setupRows();
    }

    public void setDeviceWidgetScaling(float newScaling) {
        mWidgetScaling = newScaling;
        updateScaling();
    }

    private void updateScaling() {
        int artContainerSize = (int) (dp2px(mContext, 60) * mWidgetScaling);
        int batteryProgressContainerSize = dp2px(mContext, 120);
        int glassMargin = dp2px(mContext, GLASS_GAP_DP / 2);

        List<View> views = List.of(mVolumeLevelArcProgress, mRamUsageArcProgress, mBatteryPercentArc, mBatteryTempArc);
        for (View v : views) {
            ViewGroup parent = (ViewGroup) v.getParent();
            if (parent == null) continue;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) parent.getLayoutParams();
            params.width = artContainerSize;
            params.height = artContainerSize;
            setHorizontalMargins(parent, params, glassMargin);
            parent.setLayoutParams(params);
            parent.setPadding(0, 0, 0, 0);
        }

        LinearLayout batteryProgressParent = (LinearLayout) mBatteryProgress.getParent();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) batteryProgressParent.getLayoutParams();
        params.width = (int) (batteryProgressContainerSize * mWidgetScaling);
        params.height = WRAP_CONTENT;
        params.weight = 0f;
        setHorizontalMargins(batteryProgressParent, params, glassMargin);
        batteryProgressParent.setLayoutParams(params);
        batteryProgressParent.setGravity(mLiquidGlass ? Gravity.CENTER_VERTICAL : Gravity.NO_GRAVITY);
        int batteryPadding = mLiquidGlass ? dp2px(mContext, 14) : 0;
        batteryProgressParent.setPadding(batteryPadding, 0, batteryPadding, 0);
        compactBatteryCard();

        applyLiquidGlass();
        post(this::applyGrid);
    }

    private void compactBatteryCard() {
        View percentage = mBatteryLevelView;
        View name = ViewHelper.findViewWithTag(this, "device_name");
        for (View view : new View[]{percentage, name, mBatteryProgress}) {
            if (view == null || !(view.getLayoutParams() instanceof LinearLayout.LayoutParams params))
                continue;
            int[] original = mOriginalBatteryLayout.computeIfAbsent(view, v -> new int[]{
                    params.topMargin, params.height, v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom()});
            params.topMargin = mLiquidGlass && view != mBatteryProgress ? 0 : original[0];
            params.height = mLiquidGlass && view == mBatteryProgress ? dp2px(mContext, GLASS_BAR_HEIGHT_DP) : original[1];
            view.setLayoutParams(params);
            if (view == mBatteryProgress) continue;
            if (mLiquidGlass) {
                view.setPadding(0, 0, 0, 0);
            } else {
                view.setPadding(original[2], original[3], original[4], original[5]);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) post(this::applyGrid);
    }

    private void applyGrid() {
        if (!mLiquidGlass) return;
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        if (width <= 0) return;
        int rowPadding = dp2px(mContext, GLASS_GAP_DP / 2);
        int column = (width - 2 * rowPadding) / GRID_COLUMNS;
        int height = column - dp2px(mContext, GLASS_GAP_DP);

        View batteryCard = (View) mBatteryProgress.getParent();
        int horizontalPadding = dp2px(mContext, 14);
        batteryCard.setPadding(horizontalPadding, 0, horizontalPadding, 0);
        int batteryWidth = column * 2 - dp2px(mContext, GLASS_GAP_DP);
        batteryCard.measure(MeasureSpec.makeMeasureSpec(Math.max(batteryWidth, 0), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int verticalPadding = Math.max(0, (height - batteryCard.getMeasuredHeight()) / 2);
        batteryCard.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        int arcSize = Math.round(height * GLASS_ARC_FILL);
        int arcHorizontal = Math.max(0, (height - arcSize) / 2);
        int arcVertical = arcHorizontal;

        setGridSize(batteryCard, column, 2, height);
        for (View arcContainer : List.of(mVolumeLevelContainer, mRamUsageContainer,
                mVolumeLevelContainerArc, mRamUsageContainerArc, mBatteryArcContainer, mBatteryTempContainer)) {
            setGridSize(arcContainer, column, 1, height);
            arcContainer.setPadding(arcHorizontal, arcVertical, arcHorizontal, arcVertical);
        }
        for (int i = 0; i < mCustomRow.getChildCount(); i++) {
            if (mCustomRow.getChildAt(i) instanceof BaseDeviceWidget widget) {
                setGridSize(widget, column, widget.getMode() == WidgetMode.SMALL ? 1 : 2, height);
            }
        }
    }

    private void setGridSize(View view, int column, int span, int height) {
        if (view == null || !(view.getLayoutParams() instanceof LinearLayout.LayoutParams params))
            return;
        int margin = dp2px(mContext, GLASS_GAP_DP / 2);
        int width = column * span - 2 * margin;
        if (params.width == width && params.height == height && params.weight == 0f
                && params.leftMargin == margin && params.rightMargin == margin) return;
        params.width = width;
        params.height = height;
        params.weight = 0f;
        params.leftMargin = margin;
        params.rightMargin = margin;
        view.setLayoutParams(params);
    }

    private void initBatteryStatus() {
        if (getVisibility() == View.GONE) return;
        if (mBatteryProgress != null) {
            post(() -> {
                mBatteryProgress.setProgress(mBatteryPercentage);
                mBatteryProgress.setProgressTintList(ColorStateList.valueOf(
                        mCustomColor ?
                                mLinearProgressColor == 0 ?
                                        getPrimaryColor(mContext) :
                                        mLinearProgressColor :
                                getPrimaryColor(mContext)
                ));
            });
        }
        if (mBatteryLevelView != null) {
            post(() -> mBatteryLevelView.setText(appContext.getResources().getString(R.string.percentage_text, mBatteryPercentage)));
        }
    }

    public void setCustomColor(boolean customColor, int linearColor, int circularColor) {
        mCustomColor = customColor;
        mProgressColor = circularColor;
        mLinearProgressColor = linearColor;
        mVolumeLevelArcProgress.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mRamUsageArcProgress.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mBatteryPercentArc.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mBatteryTempArc.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mBatteryProgress.setProgressTintList(ColorStateList.valueOf(
                mCustomColor ?
                        mLinearProgressColor == 0 ?
                                getPrimaryColor(mContext) :
                                mLinearProgressColor :
                        getPrimaryColor(mContext)));
        for (BaseDeviceWidget widget : mWidgets) {
            widget.setCustomColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        }
    }

    public void setTextCustomColor(int color) {
        mTextColor = color;
        mVolumeLevelArcProgress.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mRamUsageArcProgress.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mBatteryPercentArc.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mBatteryTempArc.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        post(() -> ViewHelper.findViewWithTagAndChangeColor(this, "text1", mTextColor));
        for (BaseDeviceWidget widget : mWidgets) {
            widget.setCustomColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        }
    }

    public void setDeviceName(String devName) {
        if (!TextUtils.isEmpty(devName)) {
            mDeviceName = devName;
        } else {
            mDeviceName = Build.MODEL;
        }

        post(() -> ((TextView) ViewHelper.findViewWithTag(this, "device_name")).setText(mDeviceName));

    }

    private void reloadColors() {
        initBatteryStatus();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            if (!batteryRegistered)
                mContext.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            if (batteryRegistered) mContext.unregisterReceiver(mBatteryReceiver);
        } catch (Exception ignored) {
        }
    }

    public void setWidgetSettingsListener(DeviceWidgetsAdapter.WidgetSettingsListener listener) {
        mSettingsListener = listener;
    }

    public void setWidgets(List<BaseDeviceWidget> widgets) {
        mCustomRow.removeAllViews();
        mWidgets.clear();

        if (widgets.isEmpty()) {
            if (mAddWidgetText.getParent() == null) {
                mCustomRow.setGravity(Gravity.CENTER);
                if (mSettingsInterface) mCustomRow.addView(mAddWidgetText);
            }
            return;
        }
        mCustomRow.setGravity(Gravity.NO_GRAVITY);
        for (BaseDeviceWidget wid : widgets) {
            if (!mSettingsInterface)
                Logger.log("DeviceWidgetView - add: " + wid.toJson());
            else
                Log.w("DeviceWidgetView", "add: " + wid.toJson());
        }
        if (mAddWidgetText.getParent() != null) {
            mCustomRow.removeView(mAddWidgetText);
        }
        Arrays.fill(slots, false);
        for (BaseDeviceWidget widget : widgets) {
            widget.setCustomColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
            widget.setCustomDeviceName(mDeviceName);
            if (mSettingsInterface) {
                widget.setWidgetClickListener(new BaseDeviceWidget.OnWidgetClick() {
                    @Override
                    public void onWidgetClick(BaseDeviceWidget widget) {}

                    @Override
                    public void onWidgetRemove(BaseDeviceWidget widget) {
                        removeWidget(widget);
                    }

                    @Override
                    public void onSettingsClicked(BaseDeviceWidget widget) {
                        mSettingsListener.onWidgetSettingsClick(widget);
                    }
                });
            }
            addWidget(widget);
        }
    }

    public boolean addWidget(BaseDeviceWidget widget) {
        int size = widget.getMode() == WidgetMode.BIG ? 2 : 1;
        for (int i = 0; i <= 4 - size; i++) {
            boolean spaceFree = true;
            for (int j = 0; j < size; j++) {
                if (slots[i + j]) {
                    spaceFree = false;
                    break;
                }
            }
            if (spaceFree) {
                for (int j = 0; j < size; j++) slots[i + j] = true;
                if (mAddWidgetText.getParent() != null) {
                    mCustomRow.removeView(mAddWidgetText);
                }
                mCustomRow.addView(widget);
                mWidgets.add(widget);
                widget.setAdded(true);
                updateWidgetMargin();
                return true;
            }
        }
        return false;
    }

    public void removeWidget(BaseDeviceWidget widget) {
        int index = mCustomRow.indexOfChild(widget);
        if (index == -1) return;

        int size = (widget.getMode() == WidgetMode.SMALL) ? 1 : 2;
        for (int i = index; i < Math.min(index + size, 4); i++) {
            slots[i] = false;
        }

        mWidgets.remove(widget);
        mCustomRow.removeView(widget);
        widget.setAdded(false);
        if (mWidgets.isEmpty()) {
            Arrays.fill(slots, false);
            if (mAddWidgetText.getParent() == null) {
                mCustomRow.addView(mAddWidgetText);
            }
        } else {
            updateWidgetMargin();
        }

        if (mSettingsWidgetListener != null) {
            mSettingsWidgetListener.onWidgetRemoved(widget);
        }
    }

    private void updateWidgetMargin() {
        int childCount = mCustomRow.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (!(mCustomRow.getChildAt(i) instanceof BaseDeviceWidget widget)) continue;
            LinearLayout.LayoutParams params;
            if (widget.getMode() == WidgetMode.SMALL) {
                int size = dp2px(mContext, 60);
                params = new LinearLayout.LayoutParams(size, size);
            } else {
                params = new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f);
            }

            int margin = dp2px(mContext, mLiquidGlass ? GLASS_GAP_DP / 2 : 12);
            params.setMargins(margin, 0, margin, 0);
            widget.setLayoutParams(params);
            int padding = mLiquidGlass ? dp2px(mContext, 8) : 0;
            widget.setPadding(padding, padding, padding, padding);
        }
        applyLiquidGlass();
        post(this::applyGrid);
    }

    public List<BaseDeviceWidget> getWidgetsList() {
        return mWidgets;
    }

}
