package it.dhd.oxygencustomizer;

import static it.dhd.oxygencustomizer.utils.ModuleConstants.XPOSED_ONLY_MODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.color.DynamicColors;
import com.topjohnwu.superuser.Shell;

import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.ui.activity.OnboardingActivity;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.ModuleUtil;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.RootUtil;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    public static final boolean SKIP_INSTALLATION = false;

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
        boolean isXposedOnlyMode = Prefs.getBoolean(XPOSED_ONLY_MODE, false);
        boolean isVersionCodeCorrect = ModuleUtil.checkModuleVersion(OxygenCustomizer.getAppContext());

        if (isRooted) {
            if (isOverlayInstalled) {
                Prefs.putBoolean(XPOSED_ONLY_MODE, false);
            } else if (isModuleInstalled) {
                Prefs.putBoolean(XPOSED_ONLY_MODE, true);
                isXposedOnlyMode = true;
            }
        }

        boolean isModuleProperlyInstalled = isModuleInstalled && (isOverlayInstalled || isXposedOnlyMode);

        if (SKIP_INSTALLATION || (isRooted && isModuleProperlyInstalled && isVersionCodeCorrect)) {
            keepShowing = false;
            intent = new Intent(SplashActivity.this, MainActivity.class);
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
        splashScreen.setKeepOnScreenCondition(() -> keepShowing);
        setDarkTheme();
        DynamicColors.applyToActivityIfAvailable(this);
        DynamicColors.applyToActivitiesIfAvailable(getApplication());

        new Thread(runner).start();
    }

    private void setDarkTheme() {
        if (isNightMode()) {
            int darkStyle = Settings.System.getInt(getContentResolver(), "DarkMode_style_key", Constants.DEFAULT_DARK_MODE_STYLE);
            switch (darkStyle) {
                case 0:
                    setTheme(R.style.Theme_OxygenCustomizer_DarkHard);
                    break;
                case 1:
                    setTheme(R.style.Theme_OxygenCustomizer_DarkMedium);
                    break;
                case 2:
                    setTheme(R.style.Theme_OxygenCustomizer_DarkSoft);
                    break;
            }
        }
    }

    private boolean isNightMode() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }
}
