package it.dhd.oxygencustomizer.ui.base;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentClockPickerBinding;
import it.dhd.oxygencustomizer.ui.views.ClockCarouselItemViewModel;
import it.dhd.oxygencustomizer.ui.views.ClockCarouselView;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.utils.WallpaperLoaderTask;
import it.dhd.oxygencustomizer.utils.WallpaperUtil;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

public abstract class ClockPickerFragment extends BaseFragment {

    public boolean shouldLoadWallpaper() { return false; }

    public abstract String getCategoryTitle();

    public abstract String getSwitchPreferenceKey();

    public abstract String getSwitchTitle();

    public abstract String getPreferenceKey();

    public abstract String getLayoutName();

    public abstract ControlledPreferenceFragmentCompat getPreferenceFragment();

    private FragmentClockPickerBinding binding;
    private SharedPreferences mPreferences;
    private Bitmap wallpaperBitmap;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentClockPickerBinding.inflate(inflater, container, false);

        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.getId(), getPreferenceFragment())
                .commit();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPreferences = PreferenceHelper.getModulePrefs();
        boolean lockscreenClockEnabled = mPreferences.getBoolean(getSwitchPreferenceKey(), false);

        // Setup category
        binding.clockCategory.setTitle(getCategoryTitle());

        // Setup switch
        binding.clockSwitch.setTitle(getSwitchTitle());
        binding.clockSwitch.setSwitchChecked(lockscreenClockEnabled);
        binding.clockSwitch.setSummary(lockscreenClockEnabled ? R.string.general_on : R.string.general_off);
        binding.clockSwitch.setSwitchChangeListener((buttonView, isChecked) -> {
            mPreferences.edit().putBoolean(getSwitchPreferenceKey(), isChecked).apply();
            binding.clockSwitch.setSummary(isChecked ? R.string.general_on : R.string.general_off);
            binding.clockCarouselView.getRoot().setVisibility(isChecked ? View.VISIBLE : View.GONE);
            getPreferenceFragment().updateScreen(null);
        });

        // Setup carousel
        binding.clockCarouselView.getRoot().setVisibility(lockscreenClockEnabled ? View.VISIBLE : View.GONE);
        binding.clockCarouselView.clockCarouselViewStub.setLayoutResource(R.layout.clock_carousel_view);
        ClockCarouselView clockCarouselView = (ClockCarouselView) binding.clockCarouselView.clockCarouselViewStub.inflate();
        List<ClockCarouselItemViewModel> ls_clock = new ArrayList<>();

        int maxIndex = 0;
        while (getResources()
                .getIdentifier(
                        getLayoutName() + maxIndex,
                        "layout",
                        BuildConfig.APPLICATION_ID
                ) != 0) {
            maxIndex++;
        }

        for (int i = 0; i < maxIndex; i++) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER_HORIZONTAL;
            View v = LayoutInflater.from(requireContext()).inflate(
                    this
                            .getResources()
                            .getIdentifier(
                                    getLayoutName() + i,
                                    "layout",
                                    BuildConfig.APPLICATION_ID
                            ),
                    null
            );
            v.setLayoutParams(params);
            ViewHelper.loadLottieAnimationView(
                    requireContext(),
                    null,
                    v,
                    i
            );
            ls_clock.add(
                    new ClockCarouselItemViewModel(
                            i == 0 ?
                                    "No Clock" :
                                    "Clock Style " + i,
                            i,
                            mPreferences.getInt(getPreferenceKey(), 0) == i,
                            "preview_lockscreen_clock_" + i,
                            v
                    ));
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            clockCarouselView.setUpClockCarouselView(
                    ls_clock,
                    onClockSelected -> {
                        int selectedClock = onClockSelected.getClockLayout();
                        if (updateRunnable != null) {
                            handler.removeCallbacks(updateRunnable);
                        }
                        updateRunnable = () -> {
                            setPref(selectedClock);
                        };
                        handler.postDelayed(updateRunnable, 500);
                    }
            );
            binding.clockCarouselView.screenPreviewClickView.setOnSideClickedListener(
                    (isStart) -> {
                        if (isStart) clockCarouselView.scrollToPrevious();
                        else clockCarouselView.scrollToNext();
                        return null;
                    }
            );
        }, 50);

        if (shouldLoadWallpaper()) new WallpaperLoaderTask(requireContext(), this::onWallpaperLoad).loadWallpaper();
        else binding.clockCarouselView.preview.wallpaperPreviewSpinner.setVisibility(View.GONE);

    }

    private void onWallpaperLoad(Bitmap bitmap) {
        wallpaperBitmap = bitmap;
        if (wallpaperBitmap != null) {
            binding.clockCarouselView.preview.wallpaperDimmingScrim.setVisibility(View.VISIBLE);
            binding.clockCarouselView.preview.wallpaperFadeinScrim.setVisibility(View.VISIBLE);
            binding.clockCarouselView.preview.wallpaperPreviewSpinner.setVisibility(View.GONE);
            binding.clockCarouselView.preview.wallpaperFadeinScrim.setImageBitmap(bitmap);
        }
    }

    private void setPref(int selectedClock) {
        PreferenceHelper.getModulePrefs().edit().putInt(getPreferenceKey(), selectedClock).apply();
    }

    public void scrollToPreference() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.scrollView.scrollTo(0, binding.fragmentContainer.getTop());
        }, 150);
    }



}
