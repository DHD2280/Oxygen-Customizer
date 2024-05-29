package it.dhd.oxygencustomizer.ui.activity;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import it.dhd.oxygencustomizer.databinding.ActivityOnboardingBinding;
import it.dhd.oxygencustomizer.ui.views.OnboardingView;

public class OnboardingActivity extends AppCompatActivity {

    ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
