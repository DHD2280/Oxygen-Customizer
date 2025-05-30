package it.dhd.oxygencustomizer.utils;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;
import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_PHOTO_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_PHOTO_SHOWCASE;
import static it.dhd.oxygencustomizer.xposed.utils.BootLoopProtector.LOAD_TIME_KEY_KEY;
import static it.dhd.oxygencustomizer.xposed.utils.BootLoopProtector.PACKAGE_STRIKE_KEY_KEY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.dhd.oneplusui.appcompat.dialog.adapter.ChoiceListAdapter;
import it.dhd.oneplusui.appcompat.dialog.adapter.SummaryAdapter;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.widgets.SliderWidget;
import it.dhd.oxygencustomizer.xposed.utils.BootLoopProtector;

public class AppUtils {

    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            // not installed
        }
        return false;
    }

    public static void restartScopes(Context context, String[] scopes) {
        CharSequence[] list = new String[]{
                context.getString(R.string.restart_module),
                context.getString(R.string.restart_page_scope)
        };
        SummaryAdapter mAdapter = new SummaryAdapter(context, list);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setAdapter(mAdapter, (dialog, which) -> {
            switch (which) {
                case 0:
                    restartAllScope(context);
                    break;
                case 1:
                    restartAllScope(scopes);
                    break;
            }
        });
        builder.show();
    }

    public static void restartAllScope(Context context) {
        String[] xposedScope = context.getResources().getStringArray(R.array.xposed_scope);
        ArrayList<String> commands = new ArrayList<>();
        for (String scope : xposedScope) {
            if ("android".equals(scope)) continue;
            if (scope.contains("systemui")) {
                commands.add("kill -9 `pgrep systemui`");
                continue;
            }
            commands.add("pkill -9 " + scope);
            commands.add("am force-stop " + scope);
        }
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setMessage(context.getString(R.string.restart_scope_message));
        builder.setPositiveButton(context.getString(android.R.string.ok), (dialog, which) -> new Thread(() -> {
            try {
                Shell.cmd(commands.toArray(new String[0])).exec();
            } catch (Exception ignored) {
            }
        }).start());
        builder.setNeutralButton(context.getString(android.R.string.cancel), null);
        builder.show();
    }

    public static void restartAllScope(String[] scopes) {
        List<String> commands = new ArrayList<>();
        for (String scope : scopes) {
            if ("android".equals(scope)) continue;
            resetCounter(scope);
            if (scope.contains("systemui")) {
                commands.add("kill -9 `pgrep systemui`");
                continue;
            }
            commands.add("killall " + scope);
            commands.add("am force-stop " + scope);
        }
        Shell.cmd(commands.toArray(new String[0])).exec();
    }

    public static void resetCounter(String packageName) {
        try {
            String loadTimeKey = String.format("%s%s", LOAD_TIME_KEY_KEY, packageName);
            String strikeKey = String.format("%s%s", PACKAGE_STRIKE_KEY_KEY, packageName);
            long currentTime = Calendar.getInstance().getTime().getTime();

            OCPreferences.getPrefs().edit()
                    .putLong(loadTimeKey, currentTime)
                    .putInt(strikeKey, 0)
                    .commit();
        } catch (Throwable ignored) {
        }
    }

    public static boolean hasStoragePermission() {
        return Environment.isExternalStorageManager() || Environment.isExternalStorageLegacy();
    }

    public static void requestStoragePermission(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
        ((Activity) context).startActivityForResult(intent, 0);

        ActivityCompat.requestPermissions((Activity) context, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
        }, 0);
    }

    public static boolean hasPermission(String permission) {
        return hasPermission(OxygenCustomizer.getAppContext(), permission);
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void restartScope(String what) {
        switch (what.toLowerCase()) {
            case "systemui":
                BootLoopProtector.resetCounter("com.android.systemui");
                Shell.cmd("killall com.android.systemui").exec();
                break;
            case "system":
                Shell.cmd("am start -a android.intent.action.REBOOT").exec();
                break;
            case "zygote":
            case "android":
                Shell.cmd("kill $(pidof zygote)").submit();
                Shell.cmd("kill $(pidof zygote64)").submit();
                break;
            default:
                Shell.cmd(String.format("killall %s", what)).exec();
        }
    }

    public static void showToast(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }

    public static void restartDevice() {
        Shell.cmd("am start -a android.intent.action.REBOOT").exec();
    }

    public static String[] getSplitLocations(String packageName) {
        try {
            String[] splitLocations = getAppContext().getPackageManager().getApplicationInfo(packageName, 0).splitSourceDirs;
            if (splitLocations == null) {
                splitLocations = new String[]{getAppContext().getPackageManager().getApplicationInfo(packageName, 0).sourceDir};
            }
            return splitLocations;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new String[0];
    }

    public static boolean doesClassExist(String packageName, String className) {
        try {
            Context c = getAppContext();
            Context otherAppContext = c.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            ClassLoader classLoader = otherAppContext.getClassLoader();
            Class<?> loadedClass = Class.forName(className, false, classLoader);
            return loadedClass != null;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("ClassChecker", "Package not found: " + packageName, e);
        } catch (ClassNotFoundException e) {
            Log.e("ClassChecker", "Class not found: " + className, e);
        } catch (Exception e) {
            Log.e("ClassChecker", "Exception occurred", e);
        }
        return false;
    }

    public static String getAppName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    public static Drawable getAppIcon(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static void restartApplication(Activity activity) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = activity.getIntent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.finish();
            activity.startActivity(intent);
        }, 600);
    }

    @SuppressLint("PrivateApi")
    public static void circleToSearch() {
        try {
            Bundle bundle = new Bundle();
            bundle.putLong("invocation_time_ms", SystemClock.elapsedRealtime());
            bundle.putInt("omni.entry_point", 1);
            bundle.putBoolean("micts_trigger", true);

            Class<?> iVimsClass = Class.forName("com.android.internal.app.IVoiceInteractionManagerService");
            Object vis = Class.forName("android.os.ServiceManager").getMethod("getService", String.class).invoke(null, "voiceinteraction");
            Object vims = Class.forName("com.android.internal.app.IVoiceInteractionManagerService$Stub").getMethod("asInterface", IBinder.class).invoke(null, vis);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                HiddenApiBypass.invoke(iVimsClass, vims, "showSessionFromSession", null, bundle, 7, "hyperOS_home");
            } else {
                HiddenApiBypass.invoke(iVimsClass, vims, "showSessionFromSession", null, bundle, 7);
            }
        } catch (Exception e) {
            String errMsg = "triggerCircleToSearch failed: " + e.getStackTrace();
            Log.e("MiCTS", errMsg);
        }
    }

    public static void showPhotoShowcaseRadiusDialog(Context context) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        SliderWidget sliderWidget = new SliderWidget(context);
        sliderWidget.setTitle(R.string.qs_widget_set_radius);
        sliderWidget.setSliderValue(OCPreferences.getInt(QS_PHOTO_RADIUS, 22));
        sliderWidget.setSliderValueFrom(0);
        sliderWidget.setSliderValueTo(50);
        builder.setView(sliderWidget);
        builder.setTitle(R.string.qs_widget_set_radius);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            int radius = sliderWidget.getSliderValue();
            OCPreferences.putInt(QS_PHOTO_RADIUS, radius);
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setCancelable(false);
        builder.show();
    }

    public static void showPhotoModeDialog(Context context) {
        CharSequence[] entries = context.getResources().getTextArray(R.array.photo_showcase_modes_entries);
        CharSequence[] entryValues = context.getResources().getTextArray(R.array.photo_showcase_modes_entry_values);
        CharSequence[] summaries = context.getResources().getTextArray(R.array.photo_showcase_modes_entries_summaries);
        boolean[] checkedValue;
        int item = findIndexOfValue(entryValues, OCPreferences.getString(QS_PHOTO_SHOWCASE, "0"));
        if (item >= 0 && item < entries.length) {
            boolean[] valueMap = new boolean[entries.length];
            valueMap[item] = true;
            checkedValue = valueMap;
        } else {
            checkedValue = null;
        }

        MaterialAlertDialogBuilder adapter = new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.qs_widget_set_photo_mode))
                .setNegativeButton(android.R.string.cancel, null)
                .setAdapter(new ChoiceListAdapter(context, R.layout.oplus_select_dialog_singlechoice, entries, summaries, checkedValue, false) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view3 = super.getView(position, convertView, parent);
                        View findViewById = view3.findViewById(R.id.item_divider);
                        int count = getCount();
                        if (findViewById != null) {
                            if (count != 1 && position != count - 1) {
                                findViewById.setVisibility(View.VISIBLE);
                            } else {
                                findViewById.setVisibility(View.GONE);
                            }
                        }
                        return view3;
                    }
                }, (dialogInterface, which) -> {
                    OCPreferences.putString("photoMode", entryValues[which].toString());
                    dialogInterface.dismiss();
                });
        AlertDialog dialog = adapter.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
        }
        dialog.show();
    }

    public interface OnDialogItemClickListener {
        void onItemClick(String value);
    }

    public static int findIndexOfValue(CharSequence[] entryValues, String value) {
        if (value != null && entryValues != null) {
            for (int i = entryValues.length - 1; i >= 0; i--) {
                if (TextUtils.equals(entryValues[i].toString(), value)) {
                    return i;
                }
            }
        }
        return -1;
    }

}
