package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static android.content.Context.RECEIVER_EXPORTED;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static it.dhd.oxygencustomizer.BuildConfig.APPLICATION_ID;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.biometrics.BiometricManager;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.core.content.res.ResourcesCompat;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class AdvancedReboot extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private boolean hideSosPowerMenu, showAdvancedReboot, useAuthForAdvancedReboot;
    private Drawable mAdvancedRebootDrawable;
    private Paint buttonPaint;
    private Paint textPaint;
    private int centerX;
    private int centerY;
    private int radius;
    private Class<?> SystemUIDialogClass;
    private boolean broadcastRegistered = false;

    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action.equals(Constants.ACTION_AUTH_SUCCESS_SHOW_ADVANCED_REBOOT)) {
                    log("Oxygen Customizer - Advanced Reboot Auth Success");
                    showDialog();
                }
            } catch (Throwable t) {
                log("Oxygen Customizer - Advanced Reboot Error: " + t.getMessage());
            }
        }
    };

    public AdvancedReboot(Context context) {
        super(context);
        mAdvancedRebootDrawable = ResourcesCompat.getDrawable(mContext.getResources(), mContext.getResources().getIdentifier("oplus_reboot", "drawable", listenPackage), mContext.getTheme());
    }

    @Override
    public void updatePrefs(String... Key) {
        hideSosPowerMenu = Xprefs.getBoolean("power_menu_hide_sos", false);
        showAdvancedReboot = Xprefs.getBoolean("show_advanced_reboot", false);
        useAuthForAdvancedReboot = Xprefs.getBoolean("advanced_reboot_auth", false);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listenPackage.equals(lpparam.packageName)) return;

        if (!broadcastRegistered) {
            broadcastRegistered = true;

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.ACTION_AUTH_SUCCESS_SHOW_ADVANCED_REBOOT);
            mContext.registerReceiver(broadcastReceiver, intentFilter, RECEIVER_EXPORTED); //for Android 14, receiver flag is mandatory
        }

        SystemUIDialogClass = findClass("com.android.systemui.statusbar.phone.SystemUIDialog", lpparam.classLoader);

        Class<?> ShutdownView;
        try {
            ShutdownView = findClass("com.oplus.systemui.shutdown.OplusShutdownView", lpparam.classLoader);
        } catch (Throwable t) {
            ShutdownView = findClass("com.oplusos.systemui.controls.OplusShutdownView", lpparam.classLoader); // OOS 13
        }

        findAndHookMethod(ShutdownView, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (showAdvancedReboot) {
                    drawAdvancedReboot((Canvas) param.args[0], param.thisObject);
                }
            }
        });
        findAndHookMethod(ShutdownView, "onTouchEvent", "android.view.MotionEvent", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!showAdvancedReboot) return;
                MotionEvent event = (MotionEvent) param.args[0];
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    float distanceX = event.getX() - centerX;
                    float distanceY = event.getY() - centerY;
                    double distanceFromCenter = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

                    if (distanceFromCenter <= radius) {

                        if (useAuthForAdvancedReboot && ((BiometricManager) mContext.getSystemService(BiometricManager.class)).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                            showAuth();
                        } else {
                            try {
                                showDialog();
                            } catch (Throwable t) {
                                log("Oxygen Customizer - Advanced Reboot Error: " + t.getMessage());
                            }
                        }
                    }
                }
            }
        });

        hookAllMethods(ShutdownView, "isShowEmergency", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (hideSosPowerMenu)
                    param.setResult(false);
            }
        });
    }

    private void showAuth() {

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(APPLICATION_ID, "it.dhd.oxygencustomizer.ui.activity.AuthActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }

    private void showDialog() throws Exception {
        log("Oxygen Customizer - Advanced Reboot Dialog");
        final AlertDialog dialog = (AlertDialog) SystemUIDialogClass.getConstructor(Context.class).newInstance(mContext);
        dialog.setTitle(modRes.getString(R.string.advanced_reboot_title));
        ListView listView = new ListView(mContext);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);
        adapter.add(modRes.getString(R.string.advanced_reboot_recovery));
        adapter.add(modRes.getString(R.string.advanced_reboot_bootloader));
        adapter.add(modRes.getString(R.string.advanced_reboot_safe_mode));
        adapter.add(modRes.getString(R.string.advanced_reboot_fast_reboot));
        adapter.add(modRes.getString(R.string.advanced_reboot_systemui));
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        listView.setDivider(null);
        listView.setScrollContainer(false);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0 -> XPLauncher.enqueueProxyCommand(proxy -> proxy.runCommand("reboot recovery"));
                case 1 -> XPLauncher.enqueueProxyCommand(proxy -> proxy.runCommand("reboot bootloader"));
                case 2 -> XPLauncher.enqueueProxyCommand(proxy -> proxy.runCommand("reboot safemode"));
                case 3 -> XPLauncher.enqueueProxyCommand(proxy -> proxy.runCommand("killall zygote"));
                case 4 -> XPLauncher.enqueueProxyCommand(proxy -> proxy.runCommand("killall " + SYSTEM_UI));
            }
        });

        dialog.setView(listView);

        dialog.show();
    }

    private void drawAdvancedReboot(Canvas canvas, Object param) {
        buttonPaint = new Paint();
        buttonPaint.setColor(mContext.getColor(mContext.getResources().getIdentifier("oplus_road_color", "color", listenPackage)));
        buttonPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);

        int viewWidth = (int) callMethod(param, "getWidth");

        radius = mContext.getResources().getDimensionPixelSize(
                mContext.getResources().getIdentifier("oplus_default_bar_radius", "dimen", listenPackage));

        centerX = viewWidth / 2;
        centerY = radius + 100;

        canvas.drawCircle(centerX, centerY, radius, buttonPaint);

        if (mAdvancedRebootDrawable != null) {
            int iconWidth = mAdvancedRebootDrawable.getIntrinsicWidth();
            int iconHeight = mAdvancedRebootDrawable.getIntrinsicHeight();
            Rect iconBounds = new Rect(centerX - iconWidth / 2, centerY - iconHeight / 2, centerX + iconWidth / 2, centerY + iconHeight / 2);
            mAdvancedRebootDrawable.setBounds(iconBounds);
            mAdvancedRebootDrawable.draw(canvas);
        }

        float textX = (float) viewWidth / 2;
        float textY = centerY + radius + 75;
        String buttonText = modRes.getString(R.string.advanced_reboot_title);
        canvas.drawText(buttonText, textX, textY, textPaint);
    }


    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
