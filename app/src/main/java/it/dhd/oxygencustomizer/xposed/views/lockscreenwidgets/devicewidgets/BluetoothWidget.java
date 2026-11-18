package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.utils.SystemUtils.BluetoothManager;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.ArcProgressWidget;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.utils.WidgetUtils;

@SuppressLint({"MissingPermission", "ViewConstructor"})
public class BluetoothWidget extends BaseDeviceWidget {

    private static final int MAX_BIG_ROWS = 2;

    private ImageView mBatteryProgress;
    private final String mRowLayout = "view_icon_label_row";
    private final String DEVICE_ICON = "status_bar_qs_bt_cellphone_ic";
    private int mBatteryLevel = -1;
    private final List<Bitmap> mProgresses = new ArrayList<>();
    private int currentIndex = 0;
    private boolean mReceiversRegistered = false;
    private boolean mCallbackRegistered = false;

    private final BroadcastReceiver bluetoothBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    };

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            readBatteryLevel(intent);
            refresh();
        }
    };

    private final ControllersProvider.OnBluetoothChanged mBluetoothCallback = this::updateBatteries;

    public BluetoothWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mBatteryProgress = new ImageView(mContext);
        mBatteryProgress.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mBatteryProgress.setOnClickListener(v -> {
            if (mSettingsInterface && mAdded) {
                getPopupMenu().show();
                return;
            }
            animateImageChange();
        });
        setOrientation(VERTICAL);
        readBatteryLevel(mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)));
        onWidgetModeChanged();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mReceiversRegistered) {
            mContext.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED), Context.RECEIVER_EXPORTED);
            mContext.registerReceiver(bluetoothBatteryReceiver, new IntentFilter("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"), Context.RECEIVER_EXPORTED);
            mReceiversRegistered = true;
        }
        if (!mSettingsInterface && !mCallbackRegistered) {
            try {
                ControllersProvider.registerBluetoothCallback(mBluetoothCallback);
                mCallbackRegistered = true;
            } catch (Throwable ignored) {
            }
        }
        refresh();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mReceiversRegistered) {
            try {
                mContext.unregisterReceiver(batteryReceiver);
                mContext.unregisterReceiver(bluetoothBatteryReceiver);
            } catch (Throwable ignored) {
            }
            mReceiversRegistered = false;
        }
        if (mCallbackRegistered) {
            try {
                ControllersProvider.unRegisterBluetoothCallback(mBluetoothCallback);
            } catch (Throwable ignored) {
            }
            mCallbackRegistered = false;
        }
    }

    private void readBatteryLevel(Intent intent) {
        if (intent == null) return;
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        if (level >= 0) mBatteryLevel = Math.min(level, 100);
    }

    private BluetoothAdapter adapter() {
        try {
            BluetoothManager manager = BluetoothManager();
            if (manager == null) manager = mContext.getSystemService(BluetoothManager.class);
            return manager == null ? null : manager.getAdapter();
        } catch (Throwable t) {
            return null;
        }
    }

    private boolean isBluetoothEnabled() {
        try {
            BluetoothAdapter adapter = adapter();
            return adapter != null && adapter.isEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    private void refresh() {
        updateBatteries(mSettingsInterface || isBluetoothEnabled());
    }

    private String percentage(int level) {
        return level < 0 ? "..." : level + "%";
    }

    private Bitmap arc(int level, Drawable icon) {
        return ArcProgressWidget.generateBitmap(
                mContext,
                Math.max(level, 0),
                percentage(level),
                40,
                icon,
                36,
                null,
                mProgressColor,
                mTextColor
        );
    }

    private void updateBatteries(boolean enabled) {
        removeAllViews();
        if (mBatteryProgress.getParent() instanceof ViewGroup parent)
            parent.removeView(mBatteryProgress);
        Drawable deviceIcon = ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.ic_device, appContext.getTheme());
        String phoneLabel = mDeviceName + " " + percentage(mBatteryLevel);
        List<View> rows = new ArrayList<>();
        mProgresses.clear();

        if (mSettingsInterface) {
            addFakeDevices(rows);
        } else if (enabled) {
            for (BluetoothDevice device : getConnectedDevices()) {
                int batteryLevel = getBatteryLevel(device);
                Drawable icon = WidgetUtils.getDrawable(mContext, getBluetoothIcon(device), SYSTEM_UI);
                if (batteryLevel != -1) mProgresses.add(arc(batteryLevel, icon));
                String name = deviceName(device);
                rows.add(getPercentageRow(icon, batteryLevel == -1 ? name : name + " " + batteryLevel + "%"));
            }
        }

        mProgresses.add(arc(mBatteryLevel, deviceIcon));
        rows.add(getPercentageRow(deviceIcon, phoneLabel));
        currentIndex = Math.min(currentIndex, mProgresses.size() - 1);
        mBatteryProgress.setImageBitmap(mProgresses.get(Math.max(currentIndex, 0)));

        if (getMode() == WidgetMode.BIG) {
            for (int i = 0; i < Math.min(rows.size(), MAX_BIG_ROWS); i++) addView(rows.get(i));
        } else {
            addView(mBatteryProgress);
        }
    }

    private String deviceName(BluetoothDevice device) {
        try {
            String alias = device.getAlias();
            if (!TextUtils.isEmpty(alias)) return alias;
            return device.getName();
        } catch (Throwable t) {
            return "";
        }
    }

    private void addFakeDevices(List<View> rows) {
        String[] icons = new String[]{
                "status_bar_qs_bt_headphones_a2dp_ic",
                "status_bar_qs_bt_watch_ic"
        };
        String[] names = new String[]{
                "Headphones",
                "Smartwatch"
        };
        int[] levels = new int[]{80, 45};
        for (int i = 0; i < icons.length; i++) {
            Drawable icon = getDrawable(icons[i], SYSTEM_UI);
            mProgresses.add(arc(levels[i], icon));
            rows.add(getPercentageRow(icon, names[i] + " " + levels[i] + "%"));
        }
    }

    private Drawable getDrawable(String resName, String pkg) {
        if (!mSettingsInterface) {
            return WidgetUtils.getDrawable(mContext, resName, pkg);
        }
        try {
            Context pkgContext = mContext.createPackageContext(pkg, Context.CONTEXT_IGNORE_SECURITY);
            Resources pkgRes = pkgContext.getResources();
            int resourceId = pkgRes.getIdentifier(resName, "drawable", pkg);
            if (resourceId != 0x0) {
                return ResourcesCompat.getDrawable(pkgRes, resourceId, pkgContext.getTheme());
            }
        } catch (Throwable ignored) {
        }
        return ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.ic_device, appContext.getTheme());
    }

    public List<BluetoothDevice> getConnectedDevices() {
        List<BluetoothDevice> devices = new ArrayList<>();
        BluetoothAdapter bluetoothAdapter = adapter();
        if (bluetoothAdapter == null) return devices;
        try {
            if (!bluetoothAdapter.isEnabled()) return devices;
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getBondState() == BluetoothDevice.BOND_BONDED && getBatteryLevel(device) != -1) {
                    devices.add(device);
                }
            }
        } catch (Throwable t) {
            Log.e("BluetoothBattery", "Bonded devices not available", t);
        }
        return devices;
    }

    private String getBluetoothIcon(BluetoothDevice cachedDevice) {
        BluetoothClass btClass = cachedDevice.getBluetoothClass();
        if (btClass == null) return "status_bar_qs_bt_normal_ic";

        int majorClass = btClass.getMajorDeviceClass();
        switch (majorClass) {
            case BluetoothClass.Device.Major.COMPUTER:
                return "status_bar_qs_bt_computer_ic";
            case BluetoothClass.Device.Major.PHONE:
                return DEVICE_ICON;
            case BluetoothClass.Device.Major.WEARABLE:
                return "status_bar_qs_bt_watch_ic";
            case BluetoothClass.Device.Major.IMAGING:
                return "status_bar_qs_bt_imaging_ic";
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return "status_bar_qs_bt_headphones_a2dp_ic";
            case BluetoothClass.Device.Major.PERIPHERAL:
                return "ic_bluetooth_hid";
            default:
                break;
        }

        if (btClass.doesClassMatch(BluetoothClass.PROFILE_A2DP)) {
            return "status_bar_qs_bt_headphones_a2dp_ic";
        }
        if (btClass.doesClassMatch(BluetoothClass.PROFILE_HEADSET)) {
            return "status_bar_qs_bt_headphones_a2dp_ic";
        }

        return "status_bar_qs_bt_normal_ic";
    }

    private static int getBatteryLevel(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("getBatteryLevel");
            Object batteryLevel = method.invoke(device);
            if (batteryLevel instanceof Integer) {
                return (int) batteryLevel;
            }
        } catch (Exception e) {
            Log.e("BluetoothBattery", "Battery level not available", e);
        }
        return -1;
    }

    private void animateImageChange() {
        if (mProgresses.size() < 2) return;
        currentIndex = (currentIndex + 1) % mProgresses.size();
        Bitmap newProgress = mProgresses.get(currentIndex);

        float translationHeight = mBatteryProgress.getHeight() / 2f;

        ObjectAnimator moveUp = ObjectAnimator.ofFloat(mBatteryProgress, "translationY", 0, -translationHeight);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mBatteryProgress, "alpha", 1f, 0f);
        moveUp.setDuration(300);
        fadeOut.setDuration(300);

        AnimatorSet hideAnim = new AnimatorSet();
        hideAnim.playTogether(moveUp, fadeOut);
        hideAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                mBatteryProgress.setImageBitmap(newProgress);

                ObjectAnimator moveDown = ObjectAnimator.ofFloat(mBatteryProgress, "translationY", translationHeight, 0);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mBatteryProgress, "alpha", 0f, 1f);
                moveDown.setDuration(300);
                fadeIn.setDuration(300);
                moveDown.setInterpolator(new AccelerateDecelerateInterpolator());

                AnimatorSet showAnim = new AnimatorSet();
                showAnim.playTogether(moveDown, fadeIn);
                showAnim.start();
            }

            @Override
            public void onAnimationStart(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
            }
        });

        hideAnim.start();
    }

    private View getPercentageRow(Drawable icon, String label) {
        LayoutInflater inflater = LayoutInflater.from(appContext);
        View view = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                mRowLayout,
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );
        ImageView imageView = (ImageView) ViewHelper.findViewWithTag(view, "icon");
        TextView textView = (TextView) ViewHelper.findViewWithTag(view, "label");
        imageView.setImageDrawable(icon);
        textView.setText(label);
        textView.setTextColor(mTextColor);
        return view;
    }

    @Override
    public void onWidgetModeChanged() {
        super.onWidgetModeChanged();
        if (mBatteryProgress == null) return;
        refresh();
    }

    @Override
    public String getWidgetReference() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.bt);
    }

    @Override
    public boolean hasBigMode() {
        return true;
    }

    @Override
    public boolean hasSmallMode() {
        return true;
    }

    @Override
    public View getBigPreview() {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(VERTICAL);
        List<View> rows = new ArrayList<>();
        List<Bitmap> progresses = new ArrayList<>(mProgresses);
        addFakeDevices(rows);
        mProgresses.clear();
        mProgresses.addAll(progresses);
        for (int i = 0; i < Math.min(rows.size(), MAX_BIG_ROWS); i++) layout.addView(rows.get(i));
        return layout;
    }

    @Override
    public View getSmallPreview() {
        ImageView preview = new ImageView(mContext);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setLayoutParams(new LayoutParams(dp2px(mContext, 60), dp2px(mContext, 60)));
        if (!mProgresses.isEmpty()) preview.setImageBitmap(mProgresses.get(0));
        return preview;
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        return Collections.emptyList();
    }

    @Override
    public void applySettings(Map<String, Object> settings) {
    }

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        refresh();
    }

    @Override
    public void onSetDeviceName(String deviceName) {
        refresh();
    }

}
