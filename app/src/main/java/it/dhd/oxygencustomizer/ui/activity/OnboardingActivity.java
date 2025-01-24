package it.dhd.oxygencustomizer.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.color.DynamicColors;

import it.dhd.oneplusui.appcompat.app.OplusActivity;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ActivityOnboardingBinding;
import it.dhd.oxygencustomizer.ui.views.OnboardingView;
import it.dhd.oxygencustomizer.utils.Constants;

public class OnboardingActivity extends OplusActivity {

    ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarContrastEnforced(false);
        DynamicColors.applyToActivityIfAvailable(this);
        DynamicColors.applyToActivitiesIfAvailable(getApplication());

        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply the insets as a margin to the view. This solution sets only the
            // bottom, left, and right dimensions, but you can apply whichever insets are
            // appropriate to your layout. You can also update the view padding if that's
            // more appropriate.
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.leftMargin = insets.left;
            mlp.bottomMargin = insets.bottom;
            mlp.rightMargin = insets.right;
            v.setLayoutParams(mlp);

            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            return WindowInsetsCompat.CONSUMED;
        });

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
