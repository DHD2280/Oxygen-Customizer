package it.dhd.oxygencustomizer.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.DynamicColors;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ActivityOnboardingBinding;
import it.dhd.oxygencustomizer.ui.views.OnboardingView;
import it.dhd.oxygencustomizer.utils.Constants;

public class OnboardingActivity extends AppCompatActivity {

    ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDarkTheme();
        DynamicColors.applyToActivityIfAvailable(this);
        DynamicColors.applyToActivitiesIfAvailable(getApplication());

        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                try {
                    OnboardingView.navigateToPrevSlide();
                } catch (Exception ignored) {
                    OnboardingActivity.this.finish();
                    System.exit(0);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
