package it.dhd.oxygencustomizer.ui.activity;


import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static androidx.biometric.BiometricPrompt.ERROR_CANCELED;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import java.util.concurrent.Executor;

import it.dhd.oneplusui.appcompat.dialog.adapter.SummaryAdapter;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.fragments.UpdateFragment;
import it.dhd.oxygencustomizer.utils.Constants;

public class AuthActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private int shown = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showAuth = false;

        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("showAuth", false)) {
                showAuth = true;
            }
        }

        if (showAuth) {
            showAuth();
        } else {
            showAdvancedReboot();
        }

    }

    private void showAuth() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(AuthActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                Log.e("BiometricPrompt", "onAuthenticationError: " + errString + " (" + errorCode + ")");
                if (errorCode == ERROR_CANCELED || errorCode == BiometricPrompt.ERROR_USER_CANCELED && shown < 2) {
                    biometricPrompt.cancelAuthentication();
                    runOnUiThread(() -> {
                        try {
                            biometricPrompt.authenticate(promptInfo);
                            shown++;
                        } catch (Throwable ignored) {
                        }
                    });
                } else {
                    super.onAuthenticationError(errorCode, errString);
                }
                finishAndRemoveTask();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                showAdvancedReboot();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_SHORT)
                        .show();
                finishAndRemoveTask();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.advanced_reboot_auth))
                .setSubtitle(getString(R.string.advanced_reboot_auth_summary))
                .setAllowedAuthenticators(BIOMETRIC_STRONG | BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
                .setConfirmationRequired(true)
                .build();

        new Handler(Looper.getMainLooper()).postDelayed(() -> biometricPrompt.authenticate(promptInfo), 300);
        shown++;
    }

    private void showAdvancedReboot() {
        CharSequence[] list = new String[]{
                getString(R.string.advanced_reboot_recovery),
                getString(R.string.advanced_reboot_bootloader),
                getString(R.string.advanced_reboot_safe_mode),
                getString(R.string.advanced_reboot_fast_reboot),
                getString(R.string.advanced_reboot_systemui)
        };
        SummaryAdapter mAdapter = new SummaryAdapter(this, list);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.advanced_reboot_title);
        builder.setAdapter(mAdapter, (dialog, which) -> {
            String cmd = switch (which) {
                case 0 -> "reboot recovery";
                case 1 -> "reboot bootloader";
                case 2 -> "reboot safemode";
                case 3 -> "killall zygote && killall zygote64";
                case 4 -> "killall " + SYSTEM_UI;
                default -> "";
            };
            try {
                Shell.cmd(cmd).exec();
            } catch (Exception ignored) {
            }
            finishAndRemoveTask();
        });
        builder.setOnCancelListener(dialog -> finishAndRemoveTask());
        builder.show();
    }


}
