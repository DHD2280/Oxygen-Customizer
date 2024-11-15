package it.dhd.oxygencustomizer.ui.fragments.uistyle;

import static it.dhd.oxygencustomizer.utils.Constants.Preferences.NOTIFICATION_CORNER_RADIUS;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentNotifRoundnessBinding;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;
import it.dhd.oxygencustomizer.utils.overlay.manager.RoundnessManager;

public class NotificationRoundness extends BaseFragment {

    private FragmentNotifRoundnessBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public String getTitle() {
        return getString(R.string.notification_corner_radius_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotifRoundnessBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Show loading dialog
        loadingDialog = new LoadingDialog(requireContext());

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(Prefs.getInt(NOTIFICATION_CORNER_RADIUS, 28));
        gradientDrawable.setColor(ContextCompat.getColor(OxygenCustomizer.getAppContext(), R.color.offStateColor));
        binding.notifRoundPreview.notificationChild.setBackground(gradientDrawable);
        binding.notifRoundPreview.notifDesc.setText(getString(R.string.notif_roundness_preview_desc));
        binding.disableRadius.setVisibility(OverlayUtil.isOverlayEnabled("CRN1") ? View.VISIBLE : View.GONE);

        final int[] finalUiCornerRadius = {Prefs.getInt(NOTIFICATION_CORNER_RADIUS, 28)};

        if (finalUiCornerRadius[0] == 28) {
            binding.cornerRadiusOutput.setText(getResources().getString(R.string.default_value));
        } else {
            binding.cornerRadiusOutput.setText(finalUiCornerRadius[0] + " dp");
        }
        gradientDrawable.setCornerRadius(finalUiCornerRadius[0] * OxygenCustomizer.getAppContext().getResources().getDisplayMetrics().density);
        binding.cornerRadiusSeekbar.setValue(finalUiCornerRadius[0]);

        binding.cornerRadiusSeekbar.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalUiCornerRadius[0] = (int) slider.getValue();
                if (finalUiCornerRadius[0] == 28)
                    binding.cornerRadiusOutput.setText(getResources().getString(R.string.default_value));
                else
                    binding.cornerRadiusOutput.setText(finalUiCornerRadius[0] + " dp");
            }
        });

        binding.cornerRadiusSeekbar.addOnChangeListener((slider, value, fromUser) -> {
            gradientDrawable.setCornerRadius(value * OxygenCustomizer.getAppContext().getResources().getDisplayMetrics().density);
            gradientDrawable.invalidateSelf();
        });

        binding.applyRadius.setOnClickListener(v -> {
            if (!AppUtils.hasStoragePermission()) {
                AppUtils.requestStoragePermission(requireContext());
            } else {
                // Show loading dialog
                loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                new Thread(() -> {
                    try {
                        hasErroredOut.set(RoundnessManager.buildOverlay(finalUiCornerRadius[0], true));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                        Log.e("NotificationRoundness", e.toString());
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!hasErroredOut.get()) {
                            Prefs.putInt(NOTIFICATION_CORNER_RADIUS, finalUiCornerRadius[0]);
                        }

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            // Hide loading dialog
                            loadingDialog.hide();

                            if (hasErroredOut.get())
                                Toast.makeText(OxygenCustomizer.getAppContext(), OxygenCustomizer.getAppContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            else {
                                Toast.makeText(OxygenCustomizer.getAppContext(), OxygenCustomizer.getAppContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                                binding.disableRadius.setVisibility(View.VISIBLE);
                            }
                        }, 2000);
                    });
                }).start();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}
