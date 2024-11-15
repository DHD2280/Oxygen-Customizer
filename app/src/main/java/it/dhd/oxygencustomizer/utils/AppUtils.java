package it.dhd.oxygencustomizer.utils;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;
import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oneplusui.appcompat.dialog.adapter.SummaryAdapter;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
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
            BootLoopProtector.resetCounter(scope);
            if (scope.contains("systemui")) {
                commands.add("kill -9 `pgrep systemui`");
                continue;
            }
            commands.add("killall " + scope);
            commands.add("am force-stop " + scope);
        }
        Shell.cmd(commands.toArray(new String[0])).exec();
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
}
