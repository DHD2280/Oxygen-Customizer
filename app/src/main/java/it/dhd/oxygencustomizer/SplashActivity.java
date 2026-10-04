package it.dhd.oxygencustomizer;

import static it.dhd.oxygencustomizer.utils.ModuleConstants.XPOSED_ONLY_MODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.color.DynamicColors;
import com.topjohnwu.superuser.Shell;

import it.dhd.oneplusui.appcompat.app.OplusActivity;
import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.ui.activity.OnboardingActivity;
import it.dhd.oxygencustomizer.utils.ModuleUtil;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.utils.RootUtil;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends OplusActivity {

    public static final boolean SKIP_INSTALLATION = false;
    private String intentKey = "";

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        if (Shell.getCachedShell() == null) {
            Shell.setDefaultBuilder(Shell.Builder.create()
                    .setFlags(Shell.FLAG_MOUNT_MASTER)
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(20)
            );
        }
    }

    private boolean keepShowing = true;
    private final Runnable runner = () -> Shell.getShell(shell -> {
        Intent intent;

        boolean isRooted = RootUtil.deviceProperlyRooted();
        boolean isModuleInstalled = ModuleUtil.moduleExists();
        boolean isOverlayInstalled = OverlayUtil.overlayExists();
        boolean isXposedOnlyMode = OCPreferences.getBoolean(XPOSED_ONLY_MODE, false);
        boolean isVersionCodeCorrect = ModuleUtil.checkModuleVersion(OxygenCustomizer.getAppContext());

        boolean isSetupComplete = isXposedOnlyMode || (isModuleInstalled && isOverlayInstalled);

        boolean isVersionValid = isXposedOnlyMode || isVersionCodeCorrect;

        Log.d("SplashActivity", "isRooted: " + isRooted + ",\n" +
                " isModuleInstalled: " + isModuleInstalled + ",\n" +
                " isOverlayInstalled: " + isOverlayInstalled + ",\n" +
                " isXposedOnlyMode: " + isXposedOnlyMode + ",\n" +
                " isVersionCodeCorrect: " + isVersionCodeCorrect + ",\n" +
                " isSetupComplete: " + isSetupComplete + ",\n" +
                " isVersionValid: " + isVersionValid);

        if (SKIP_INSTALLATION || (isRooted && isSetupComplete && isVersionValid)) {
            keepShowing = false;
            intent = new Intent(SplashActivity.this, MainActivity.class);
            Log.i("SplashActivity", "Starting MainActivity with intentKey: " + intentKey);
            if (!intentKey.isEmpty()) {
                intent.putExtra("launch", intentKey);
            }
        } else {
            keepShowing = false;
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        }

        startActivity(intent);
        finish();
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            if (getIntent().hasExtra("launch")) {
                intentKey = getIntent().getStringExtra("launch");
            }
        }
        splashScreen.setKeepOnScreenCondition(() -> keepShowing);
        DynamicColors.applyToActivityIfAvailable(this);
        DynamicColors.applyToActivitiesIfAvailable(getApplication());

        new Thread(runner).start();
    }
}
