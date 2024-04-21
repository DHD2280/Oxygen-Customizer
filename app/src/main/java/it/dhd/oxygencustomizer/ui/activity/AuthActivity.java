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

import java.util.concurrent.Executor;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;

public class AuthActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private int shown = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(AuthActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                Log.d("BiometricPrompt", "onAuthenticationError: " + errString + " (" + errorCode + ")");
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
                Intent broadcast = new Intent(Constants.ACTION_AUTH_SUCCESS_SHOW_ADVANCED_REBOOT);

                broadcast.putExtra("packageName", SYSTEM_UI);

                broadcast.setPackage(SYSTEM_UI);

                AuthActivity.this.sendBroadcast(broadcast);
                finishAndRemoveTask();
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



}
