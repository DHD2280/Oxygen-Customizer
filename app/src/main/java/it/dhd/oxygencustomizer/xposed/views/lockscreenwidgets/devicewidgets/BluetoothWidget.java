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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
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

    private ImageView mBatteryProgress;
    private final String mRowLayout = "view_icon_label_row";
    private final String DEVICE_ICON = "status_bar_qs_bt_cellphone_ic";
    private int mBatteryLevel = -1;
    private final List<Bitmap> mProgresses = new ArrayList<>();
    private int currentIndex = 0;

    private final BroadcastReceiver bluetoothBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteries(BluetoothManager().getAdapter().isEnabled());
        }
    };

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mSettingsInterface) return;
            mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            mBatteryLevel = Math.max(0, Math.min(mBatteryLevel, 100));
            updateBatteries(BluetoothManager().getAdapter().isEnabled());
        }
    };

    private ControllersProvider.OnBluetoothChanged mBluetoothCallback = this::updateBatteries;

    private void updateBatteries(boolean enabled) {
        removeAllViews();
        Drawable deviceIcon = ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.ic_device, appContext.getTheme());
        if (mSettingsInterface) {
            if (mCurrentMode == WidgetMode.BIG) {
                addView(getFakeView());
                addView(getPercentageRow(deviceIcon, mDeviceName + " " + mBatteryLevel + "%"));
            } else {
                addView(getSmallView(mBatteryLevel, deviceIcon));
            }
            return;
        }
        if (!enabled) {
            if (mCurrentMode == WidgetMode.BIG) {
                addView(getPercentageRow(deviceIcon, mDeviceName + " " + mBatteryLevel + "%"));
            } else {
                addView(getSmallView(mBatteryLevel, deviceIcon));
            }
            return;
        }

        List<BluetoothDevice> mDevices = getConnectedDevices();
        mProgresses.clear();
        if (!mDevices.isEmpty()) {
            for (BluetoothDevice device : mDevices) {
                int batteryLevel = getBatteryLevel(device);
                Drawable icon = WidgetUtils.getDrawable(mContext, getBluetoothIcon(device), SYSTEM_UI);
                Bitmap widgetBitmap = ArcProgressWidget.generateBitmap(
                        mContext,
                        batteryLevel == -1 ? 0 : batteryLevel,
                        batteryLevel + "%",
                        40,
                        icon,
                        36,
                        null,
                        mProgressColor,
                        mTextColor
                );
                if (batteryLevel != -1) mProgresses.add(widgetBitmap);
                if (getMode() == WidgetMode.BIG) {
                    if (batteryLevel == -1) {
                        addView(getPercentageRow(icon, device.getName()));
                    } else {
                        addView(getPercentageRow(icon, device.getAlias() + " " + batteryLevel + "%"));
                    }
                }
            }
        }

        Bitmap widgetBitmap = ArcProgressWidget.generateBitmap(
                mContext,
                mBatteryLevel == -1 ? 0 : mBatteryLevel,
                mBatteryLevel + "%",
                40,
                deviceIcon,
                36,
                null,
                mProgressColor,
                mTextColor
        );
        mProgresses.add(widgetBitmap);
        currentIndex = 0;
        mBatteryProgress.setImageBitmap(mProgresses.get(currentIndex));
        if (getMode() == WidgetMode.BIG) {
            addView(getPercentageRow(deviceIcon, mDeviceName + " " + mBatteryLevel + "%"));
        } else {
            addView(mBatteryProgress);
        }
    }

    private View getFakeView() {
        LinearLayout fakeView = new LinearLayout(mContext);
        fakeView.setOrientation(VERTICAL);
        fakeView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        String[] icons = new String[]{
                "status_bar_qs_bt_headphones_a2dp_ic",
                "status_bar_qs_bt_watch_ic"
        };
        String[] names = new String[]{
                "Headphones",
                "Smartwatch"
        };
        mProgresses.clear();
        for (int i = 0; i < 2; i++) {
            int batteryLevel = (int) (Math.random() * 100);
            Drawable icon = getDrawable(icons[i], SYSTEM_UI);
            Bitmap widgetBitmap = ArcProgressWidget.generateBitmap(
                    mContext,
                    batteryLevel == -1 ? 0 : batteryLevel,
                    batteryLevel + "%",
                    40,
                    icon,
                    36,
                    null,
                    mProgressColor,
                    mTextColor
            );
            currentIndex = 0;
            mProgresses.add(widgetBitmap);
            fakeView.addView(getPercentageRow(icon, names[i] + " " + batteryLevel + "%"));
        }
        mBatteryProgress.setImageBitmap(mProgresses.get(currentIndex));
        return fakeView;
    }

    private Drawable getDrawable(String resName, String pkg) {
        if (!mSettingsInterface) {
            return WidgetUtils.getDrawable(mContext, resName, pkg);
        }
        Context pkgContext = null;
        try {
            pkgContext = mContext.createPackageContext(pkg, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {
        }
        Resources pkgRes = pkgContext.getResources();
        int resourceId = pkgRes.getIdentifier(resName, "drawable", pkg);
        if (resourceId != 0x0) {
            return ResourcesCompat.getDrawable(pkgRes, resourceId, pkgContext.getTheme());
        } else {
            return ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.ic_launcher_foreground, appContext.getTheme());
        }
    }

    public List<BluetoothDevice> getConnectedDevices() {
        List<BluetoothDevice> devices = new ArrayList<>();
        BluetoothAdapter bluetoothAdapter = BluetoothManager().getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e("BluetoothBattery", "Bluetooth is disabled");
            return devices;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                int batteryLevel = getBatteryLevel(device);
                if (batteryLevel != -1) {
                    devices.add(device);
                }
            }
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
            Log.e("BluetoothBattery", "Battery level not available for " + device.getName(), e);
        }
        return -1;
    }

    public BluetoothWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        if (!mSettingsInterface) {
            ControllersProvider.registerBluetoothCallback(mBluetoothCallback);
        }
        mContext.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        mContext.registerReceiver(bluetoothBatteryReceiver, new IntentFilter("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"));

        setupLayout();
        onWidgetModeChanged();
    }

    private void setupLayout() {
        setOrientation(VERTICAL);
        mBatteryProgress = new ImageView(mContext);
        mBatteryProgress.setOnClickListener(v -> {
            if (mSettingsInterface && mAdded) {
                getPopupMenu().show();
                return;
            }
            animateImageChange();
        });
        updateBatteries(mSettingsInterface ? true : BluetoothManager().getAdapter().isEnabled());
    }

    private void animateImageChange() {
        if (mProgresses.isEmpty()) return;
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

    private View getPercentageRow(Drawable icon, String percentage) {
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
        textView.setText(percentage);
        textView.setTextColor(mTextColor);
        return view;
    }


    @Override
    public void onWidgetModeChanged() {
        super.onWidgetModeChanged();
        if (mCurrentMode == WidgetMode.SMALL) {
            removeAllViews();
            try {
                ((ViewGroup) mBatteryProgress.getParent()).removeView(mBatteryProgress);
            } catch (Throwable ignored) {
            }
            addView(mBatteryProgress);
            return;
        }
        updateBatteries(mSettingsInterface ? true : BluetoothManager().getAdapter().isEnabled());
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
        LinearLayout layout = (LinearLayout) getFakeView();
        Drawable deviceIcon = getDrawable(DEVICE_ICON, SYSTEM_UI);
        layout.addView(getPercentageRow(deviceIcon, mDeviceName + " " + mBatteryLevel + "%"));
        return layout;
    }

    @Override
    public View getSmallPreview() {
        ImageView preview = new ImageView(mContext);
        preview.setLayoutParams(new LinearLayout.LayoutParams(dp2px(mContext, 60), dp2px(mContext, 60)));
        preview.setImageBitmap(mProgresses.get(0));
        return preview;
    }

    private View getSmallView(int percentage, Drawable icon) {
        ImageView preview = new ImageView(mContext);
        preview.setLayoutParams(new LinearLayout.LayoutParams(dp2px(mContext, 60), dp2px(mContext, 60)));
        preview.setImageBitmap(
                ArcProgressWidget.generateBitmap(
                        mContext,
                        percentage,
                        100,
                        percentage + "%",
                        40,
                        icon,
                        true,
                        36,
                        null,
                        mProgressColor,
                        mTextColor
                )
        );
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
        updateBatteries(mSettingsInterface ? true : BluetoothManager().getAdapter().isEnabled());
    }

    @Override
    public void onSetDeviceName(String deviceName) {
        updateBatteries(mSettingsInterface ? true : BluetoothManager().getAdapter().isEnabled());
    }

}
