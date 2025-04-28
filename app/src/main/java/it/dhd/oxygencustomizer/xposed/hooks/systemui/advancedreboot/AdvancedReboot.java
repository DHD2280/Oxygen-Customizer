package it.dhd.oxygencustomizer.xposed.hooks.systemui.advancedreboot;

import static android.content.Context.RECEIVER_EXPORTED;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.BuildConfig.APPLICATION_ID;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.getModulePath;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.resparams;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.biometrics.BiometricManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.android.systemui.plugins.qs.DetailAdapter;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.list.COUIListView;
import com.oplus.systemui.common.dialog.OplusQSDetailDialogProvider;
import com.oplus.systemui.qs.base.util.QsColorUtil;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
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
    private OplusRebootDetailAdapter oplusScreenshotDetailAdapter;
    private OplusRebootDetailAdapter14 oplusScreenshotDetailAdapter14;
    public OplusRebootDetailAdapter detailAdapter;
    public OplusRebootDetailAdapter14 detailAdapter14;
    public OplusQSDetailDialogProvider mQsDialog;
    private OplusQSDetailDialogProvider mQsDialog14;
    public static final List<Integer> itemTitles = new ArrayList<>();
    public static final List<Integer> itemIcons = new ArrayList<>();

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
    private boolean broadcastRegistered = false;


    public AdvancedReboot(Context context) {
        super(context);
        mAdvancedRebootDrawable = ResourcesCompat.getDrawable(mContext.getResources(), mContext.getResources().getIdentifier("oplus_reboot", "drawable", listenPackage), mContext.getTheme());
        if (Build.VERSION.SDK_INT >= 35) {
            this.detailAdapter = new OplusRebootDetailAdapter(this.mContext);
        } else {
            this.detailAdapter14 = new OplusRebootDetailAdapter14(this.mContext);
        }
    }

    @Override
    public void initResources() {
        XResources xRes = resparams.get(SYSTEM_UI).res;
        // Titles
        XModuleResources moddedRes = XModuleResources.createInstance(getModulePath(), xRes);
        AdvancedRebootResources.setFakeIdDialogTitle(xRes.addResource(moddedRes, R.string.advanced_reboot_title));
        AdvancedRebootResources.setFakeIdTitleRecovery(xRes.addResource(moddedRes, R.string.advanced_reboot_recovery));
        AdvancedRebootResources.setFakeIdTitleBootloader(xRes.addResource(moddedRes, R.string.advanced_reboot_bootloader));
        AdvancedRebootResources.setFakeIdTitleSafeMode(xRes.addResource(moddedRes, R.string.advanced_reboot_safe_mode));
        AdvancedRebootResources.setFakeIdTitleFastReboot(xRes.addResource(moddedRes, R.string.advanced_reboot_fast_reboot));
        AdvancedRebootResources.setFakeIdTitleSystemUi(xRes.addResource(moddedRes, R.string.advanced_reboot_systemui));
        itemTitles.add(AdvancedRebootResources.getFakeIdTitleRecovery());
        itemTitles.add(AdvancedRebootResources.getFakeIdTitleBootloader());
        itemTitles.add(AdvancedRebootResources.getFakeIdTitleSafeMode());
        itemTitles.add(AdvancedRebootResources.getFakeIdTitleFastReboot());
        itemTitles.add(AdvancedRebootResources.getFakeIdTitleSystemUi());
        // Icons
        AdvancedRebootResources.setFakeIdIconRecovery(xRes.addResource(moddedRes, R.drawable.ic_ar_recovery));
        AdvancedRebootResources.setFakeIdIconBootloader(xRes.addResource(moddedRes, R.drawable.ic_ar_bootloader));
        AdvancedRebootResources.setFakeIdIconSafeMode(xRes.addResource(moddedRes, R.drawable.ic_ar_fastboot));
        itemIcons.add(AdvancedRebootResources.getFakeIdIconRecovery());
        itemIcons.add(AdvancedRebootResources.getFakeIdIconBootloader());
        itemIcons.add(AdvancedRebootResources.getFakeIdIconSafeMode());
        int resId = mContext.getResources().getIdentifier("oplus_reboot", "drawable", listenPackage);
        itemIcons.add(resId);
        itemIcons.add(resId);
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

        try {
            ReflectedClass ShutdownUi = ReflectedClass.of("com.android.systemui.globalactions.ShutdownUi");
            ShutdownUi
                    .afterConstruction()
                    .run(param -> {
                        try {
                            mNearbyManager = getObjectField(param.thisObject, "mNearbyManager");
                            int powerOffFindingMode = (int) callMethod(mNearbyManager, "getPoweredOffFindingMode");
                            isFinderActive = powerOffFindingMode == 2;
                            XposedBridge.log("mNearbyManager != null? " + (mNearbyManager != null) + " powerOffFindingMode = " + powerOffFindingMode +  " isFinderActive: " + isFinderActive);
                        } catch (Throwable t) {
                            log(t);
                        }
                    });
        } catch (Throwable ignored) {}

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
                        });

        ShutdownView
                .before("isShowEmergency")
                        .run(param -> {
                            if (hideSosPowerMenu)
                                param.setResult(false);
                        });

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
            centerY += dp2px(mContext, 75);
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

    public final class OplusRebootDetailAdapter implements DetailAdapter, AdapterView.OnItemClickListener {
        public final Context mainContext;

        @Override
        public int getMetricsCategory() {
            return 119;
        }

        @Override
        public Boolean getToggleState() {
            return null;
        }

        @Override
        public void setToggleState(boolean z) {
        }

        public OplusRebootDetailAdapter(Context context) {
            this.mainContext = context;
        }

        @Override
        public CharSequence getTitle() {
            return this.mainContext.getString(AdvancedRebootResources.getFakeIdDialogTitle());
        }

        @Override
        public Intent getSettingsIntent() {
            return null;
        }

        @Override
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            View inflate;
//            COUIThemeOverlay.getInstance().applyThemeOverlays(this.mainContext);
            inflate = LayoutInflater.from(this.mainContext).inflate(mContext.getResources().getIdentifier("oplus_qs_screenshot_detail", "layout", SYSTEM_UI), viewGroup, false);
            COUIListView cOUIListView = inflate != null ? (COUIListView) inflate.findViewById(mContext.getResources().getIdentifier("screen_shot_option_list", "id", SYSTEM_UI)) : null;
            ItemAdapter itemAdapter = new ItemAdapter(this.mainContext);
            itemAdapter.setOnItemClickListener(i2 -> OplusRebootDetailAdapter.this.onItemClick(null, null, i2, 0L));
            if (cOUIListView != null) {
                cOUIListView.setAdapter(itemAdapter);
            }
            return inflate;
        }

        @Override
        public void onItemClick(AdapterView adapterView, View view, int i2, long j2) {
            int i3 = itemTitles.get(i2);
            String cmd;
            if (i3 == itemTitles.get(0)) {
                cmd = "reboot recovery";
            } else if (i3 == itemTitles.get(1)) {
                cmd = "reboot bootloader";
            } else if (i3 == itemTitles.get(2)) {
                cmd = "reboot safemode";
            } else if (i3 == itemTitles.get(3)) {
                cmd = "killall zygote; killall zygote64";
            } else if (i3 == itemTitles.get(4)) {
                cmd = "killall " + SYSTEM_UI;
            } else {
                cmd = "";
            }
            String finalCmd = cmd;
            XPLauncher.enqueueProxyCommand(proxy -> {
                try {
                    proxy.runCommand(finalCmd);
                } catch (Exception e) {
                    XposedBridge.log("Error executing command: " + e.getMessage());
                }
            });
        }
    }

    public final class OplusRebootDetailAdapter14 implements com.oplus.systemui.qs.detail.DetailAdapter, AdapterView.OnItemClickListener {
        public final Context mainContext;

        @Override
        public int getMetricsCategory() {
            return 119;
        }

        @Override
        public Boolean getToggleState() {
            return null;
        }

        @Override
        public void setToggleState(boolean z) {
        }

        public OplusRebootDetailAdapter14(Context context) {
            this.mainContext = context;
        }

        @Override
        public CharSequence getTitle() {
            return this.mainContext.getString(AdvancedRebootResources.getFakeIdDialogTitle());
        }

        @Override
        public Intent getSettingsIntent() {
            return null;
        }

        @Override
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            View inflate;
//            COUIThemeOverlay.getInstance().applyThemeOverlays(this.mainContext);
            inflate = LayoutInflater.from(this.mainContext).inflate(mContext.getResources().getIdentifier("oplus_qs_screenshot_detail", "layout", SYSTEM_UI), viewGroup, false);
            COUIListView cOUIListView = inflate != null ? (COUIListView) inflate.findViewById(mContext.getResources().getIdentifier("screen_shot_option_list", "id", SYSTEM_UI)) : null;
            ItemAdapter itemAdapter = new ItemAdapter(this.mainContext);
            itemAdapter.setOnItemClickListener(i2 -> OplusRebootDetailAdapter14.this.onItemClick(null, null, i2, 0L));
            if (cOUIListView != null) {
                cOUIListView.setAdapter(itemAdapter);
            }
            return inflate;
        }

        @Override
        public void onItemClick(AdapterView adapterView, View view, int i2, long j2) {
            int i3 = itemTitles.get(i2);
            String cmd;
            if (i3 == itemTitles.get(0)) {
                cmd = "reboot recovery";
            } else if (i3 == itemTitles.get(1)) {
                cmd = "reboot bootloader";
            } else if (i3 == itemTitles.get(2)) {
                cmd = "reboot safemode";
            } else if (i3 == itemTitles.get(3)) {
                cmd = "killall zygote; killall zygote64";
            } else if (i3 == itemTitles.get(4)) {
                cmd = "killall " + SYSTEM_UI;
            } else {
                cmd = "";
            }
            String finalCmd = cmd;
            XPLauncher.enqueueProxyCommand(proxy -> {
                try {
                    proxy.runCommand(finalCmd);
                } catch (Exception e) {
                    XposedBridge.log("Error executing command: " + e.getMessage());
                }
            });
        }
    }

    public final class ItemAdapter extends BaseAdapter {
        public final Context context;
        public OnItemClickListener mItemClickListener;

        @Override
        public Object getItem(int i2) {
            return null;
        }

        @Override
        public long getItemId(int i2) {
            return i2;
        }

        public ItemAdapter(Context context) {
            this.context = context;
        }

        public final void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.mItemClickListener = onItemClickListener;
        }

        @Override
        public int getCount() {
            return itemTitles.size();
        }

        @Override
        public View getView(final int i2, View view, ViewGroup viewGroup) {
            View view2;
            ViewHolder viewHolder;
            boolean isNightSpInLight = isNightSpInLight();
            if (view == null) {
                viewHolder = new ViewHolder();
                view2 = LayoutInflater.from(QsColorUtil.createDarkColorContext(this.context, isNightSpInLight)).inflate(mContext.getResources().getIdentifier("oplus_qs_screenshot_detail_item", "layout", SYSTEM_UI), viewGroup, false);
                viewHolder.setItemIcon(view2 != null ? view2.findViewById(mContext.getResources().getIdentifier("screenshot_option_icon_image", "id", SYSTEM_UI)) : null);
                viewHolder.setItemTitle(view2 != null ? view2.findViewById(mContext.getResources().getIdentifier("screenshot_option_description", "id", SYSTEM_UI)) : null);
                viewHolder.setDividerLine(view2 != null ? view2.findViewById(mContext.getResources().getIdentifier("divider_line", "id", SYSTEM_UI)) : null);
                if (view2 != null) {
                    view2.setTag(viewHolder);
                }
            } else {
                view2 = view;
                viewHolder = (ViewHolder) view.getTag();
            }
            if (i2 == getCount() - 1) {
                View dividerLine = viewHolder.getDividerLine();
                dividerLine.setVisibility(View.GONE);
            } else {
                View dividerLine2 = viewHolder.getDividerLine();
                dividerLine2.setVisibility(View.VISIBLE);
            }
            if (view2 != null) {
                view2.setOnClickListener(view3 -> {
                    OnItemClickListener onItemClickListener;
                    onItemClickListener = ItemAdapter.this.mItemClickListener;
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(i2);
                    }
                });
            }
            if (isNightSpInLight) {
                ImageView itemIcon = viewHolder.getItemIcon();
                if (itemIcon != null) {
                    itemIcon.setImageResource(itemIcons.get(i2));
                }
            } else {
                ImageView itemIcon2 = viewHolder.getItemIcon();
                itemIcon2.setVisibility(View.VISIBLE);
                if (itemIcon2 != null) {
                    itemIcon2.setImageResource(itemIcons.get(i2));
                }
            }
            TextView itemTitle = viewHolder.getItemTitle();
            if (itemTitle != null) {
                itemTitle.setText(itemTitles.get(i2));
            }
            COUICardListHelper.setItemCardBackground(view2, COUICardListHelper.getPositionInGroup(getCount(), i2));
            int dimensionPixelSize = this.context.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("qs_detail_item_list_padding_horizontal", "dimen", SYSTEM_UI));
            int paddingTop = view2 != null ? view2.getPaddingTop() : 0;
            int paddingBottom = view2 != null ? view2.getPaddingBottom() : 0;
            if (view2 != null) {
                view2.setPadding(dimensionPixelSize, paddingTop, dimensionPixelSize, paddingBottom);
            }
            return view2;
        }

        public final boolean isNightSpInLight() {
            return QsColorUtil.isNeedSeparateDarkThemeColor(this.context);
        }
    }

    public final class ViewHolder {
        public View dividerLine;
        public ImageView itemIcon;
        public TextView itemTitle;

        public final TextView getItemTitle() {
            return this.itemTitle;
        }

        public final void setItemTitle(TextView textView) {
            this.itemTitle = textView;
        }

        public final ImageView getItemIcon() {
            return this.itemIcon;
        }

        public final void setItemIcon(ImageView imageView) {
            this.itemIcon = imageView;
        }

        public final View getDividerLine() {
            return this.dividerLine;
        }

        public final void setDividerLine(View view) {
            this.dividerLine = view;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int i2);
    }

    private void showDialog() {
        if (Build.VERSION.SDK_INT >= 35) {
            oplusScreenshotDetailAdapter = detailAdapter;
            mQsDialog = new OplusQSDetailDialogProvider(mContext, oplusScreenshotDetailAdapter);
            mQsDialog.showDetail(null, true, null, OplusQSDetailDialogProvider.DialogHeight.CHANGE);
        } else { // OOS14
            oplusScreenshotDetailAdapter14 = detailAdapter14;
            mQsDialog14 = new OplusQSDetailDialogProvider(mContext, oplusScreenshotDetailAdapter14);
            mQsDialog14.showDetail(null, true, null);
        }
    }

    private void showAuth() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(APPLICATION_ID, APPLICATION_ID + ".ui.activity.AuthActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

}
