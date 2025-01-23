package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.BuildConfig.APPLICATION_ID;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.biometrics.BiometricManager;
import android.view.MotionEvent;

import androidx.core.content.res.ResourcesCompat;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class AdvancedReboot extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private boolean hideSosPowerMenu, showAdvancedReboot, useAuthForAdvancedReboot;
    private final Drawable mAdvancedRebootDrawable;
    private Paint buttonPaint;
    private Paint textPaint;
    private int centerX;
    private int centerY;
    private int radius;
    private Object mNearbyManager = null;
    private boolean isFinderActive = false;

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

        ReflectedClass ShutdownUi = ReflectedClass.of("com.android.systemui.globalactions.ShutdownUi");
        ShutdownUi
                .after("getShutdownDialogContent")
                .run(param -> {
                    try {
                        mNearbyManager = getObjectField(param.thisObject, "mNearbyManager");
                        int powerOffFindingMode = (int) callMethod(mNearbyManager, "getPoweredOffFindingMode");
                        isFinderActive = !(boolean) param.args[1] && powerOffFindingMode == 2;
                    } catch (Throwable ignored) {}
                });

        ReflectedClass ShutdownView = ReflectedClass.of(
                "com.oplus.systemui.shutdown.OplusShutdownView" /* OOS14-15 */,
                "com.oplusos.systemui.controls.OplusShutdownView" /* OOS13 */);

        ShutdownView
                .after("onDraw")
                        .run(param -> {
                            if (showAdvancedReboot) {
                                drawAdvancedReboot((Canvas) param.args[0], param.thisObject);
                            }
                        });

        ShutdownView
                .after("onTouchEvent")
                        .run(param -> {
                            if (!showAdvancedReboot) return;

                            MotionEvent event = (MotionEvent) param.args[0];
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                float distanceX = event.getX() - centerX;
                                float distanceY = event.getY() - centerY;
                                double distanceFromCenter = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

                                if (distanceFromCenter <= radius) {

                                    if (useAuthForAdvancedReboot && mContext.getSystemService(BiometricManager.class).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                                        startActivity(true);
                                    } else {
                                        startActivity(false);
                                    }
                                }
                            }
                        });

        ShutdownView
                .before("isShowEmergency")
                        .run(param -> {
                            if (hideSosPowerMenu)
                                param.setResult(false);
                        });

    }

    private void startActivity(boolean showAuth) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(APPLICATION_ID, APPLICATION_ID + ".ui.activity.AuthActivity"));
        intent.putExtra("showAuth", showAuth);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mContext.startActivity(intent);
    }

    private void drawAdvancedReboot(Canvas canvas, Object param) {
        buttonPaint = new Paint();
        buttonPaint.setColor(mContext.getColor(mContext.getResources().getIdentifier("oplus_road_color", "color", listenPackage)));
        buttonPaint.setStyle(Paint.Style.FILL);

        int density = Resources.getSystem().getConfiguration().densityDpi;

        textPaint = new Paint();
        textPaint.setColor(Color.GRAY);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize((float) density / 13);

        int viewWidth = (int) callMethod(param, "getWidth");

        radius = (int) (mContext.getResources().getDimensionPixelSize(
                mContext.getResources().getIdentifier("oplus_default_bar_radius", "dimen", listenPackage)) / 2.0f);

        centerX = viewWidth / 2;
        centerY = radius + dp2px(mContext, 50);
        if (isFinderActive) {
            centerY += dp2px(mContext, 50);
        }

        canvas.drawCircle(centerX, centerY, radius, buttonPaint);

        if (mAdvancedRebootDrawable != null) {

            Rect iconBounds = new Rect(
                    centerX - radius / 2,
                    centerY - radius / 2,
                    centerX + radius / 2,
                    centerY + radius / 2);
            mAdvancedRebootDrawable.setBounds(iconBounds);
            mAdvancedRebootDrawable.draw(canvas);
        }

        float textX = (float) viewWidth / 2;
        float textY = centerY + radius + dp2px(mContext, 20);
        String buttonText = modRes.getString(R.string.advanced_reboot_title);
        canvas.drawText(buttonText, textX, textY, textPaint);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
