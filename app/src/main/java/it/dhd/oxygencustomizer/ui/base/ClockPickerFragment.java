package it.dhd.oxygencustomizer.ui.base;

import static android.content.Context.BATTERY_SERVICE;
import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.BatteryManager;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.BatteryIconOptionsBinding;
import it.dhd.oxygencustomizer.databinding.FragmentClockPickerBinding;
import it.dhd.oxygencustomizer.ui.views.ClockCarouselItemViewModel;
import it.dhd.oxygencustomizer.ui.views.ClockCarouselView;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.utils.WallpaperLoaderTask;
import it.dhd.oxygencustomizer.xposed.batterystyles.BatteryDrawable;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryL;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

public abstract class ClockPickerFragment extends BaseFragment {

    public abstract String getCategoryTitle();

    public abstract String getSwitchPreferenceKey();

    public abstract String getSwitchTitle();

    public abstract String getPreferenceKey();

    public abstract String getLayoutName();

    public abstract ControlledPreferenceFragmentCompat getPreferenceFragment();

    public abstract PREVIEW_TYPE getPreviewType();

    public enum PREVIEW_TYPE {
        LOCKSCREEN,
        AOD,
        QS
    }

    private FragmentClockPickerBinding binding;
    private SharedPreferences mPreferences;
    private Bitmap wallpaperBitmap;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;
    private ClockCarouselView clockCarouselView;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentClockPickerBinding.inflate(inflater, container, false);

        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, getPreferenceFragment())
                .commit();

        mPreferences = PreferenceHelper.getModulePrefs();
        boolean lockscreenClockEnabled = mPreferences.getBoolean(getSwitchPreferenceKey(), false);

        setupPreferences(lockscreenClockEnabled);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.clockCarouselView.clockCarouselViewStub.setLayoutResource(R.layout.clock_carousel_view);
        clockCarouselView = (ClockCarouselView) binding.clockCarouselView.clockCarouselViewStub.inflate();

        loadCarouselInBackground();

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

    private Drawable getBatteryIcon() {
        BatteryManager bm = (BatteryManager) requireContext().getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        int batteryColor = getAppContext().getColor(R.color.textColorPrimary);
        Drawable batteryDrawable = new LandscapeBatteryL(requireContext(), batteryColor);
        ((BatteryDrawable)batteryDrawable).setBatteryLevel(batLevel);
        return batteryDrawable;
    }

    private void setupPreferences(boolean lockscreenClockEnabled) {
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
    }

    private void loadCarouselInBackground() {
        executor.execute(() -> {
            List<ClockCarouselItemViewModel> ls_clock = new ArrayList<>();
            int maxIndex = 0;

            while (getResources().getIdentifier(getLayoutName() + maxIndex, "layout", BuildConfig.APPLICATION_ID) != 0) {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                params.gravity = Gravity.CENTER_HORIZONTAL;
                if (getContext() == null) {
                    executor.shutdownNow();
                }
                View v = LayoutInflater.from(getAppContext()).inflate(
                        getResources().getIdentifier(getLayoutName() + maxIndex, "layout", BuildConfig.APPLICATION_ID),
                        null
                );
                v.setLayoutParams(params);
                ViewHelper.loadLottieAnimationView(getAppContext(), null, v, maxIndex);

                ls_clock.add(new ClockCarouselItemViewModel(
                        maxIndex == 0 ? getString(R.string.clock_none) : String.format(getString(R.string.clock_style_name), maxIndex),
                        maxIndex,
                        mPreferences.getInt(getPreferenceKey(), 0) == maxIndex,
                        "preview_lockscreen_clock_" + maxIndex,
                        v
                ));
                maxIndex++;
            }
            if (getContext() == null) {
                executor.shutdownNow();
            }
            mainHandler.postDelayed(() -> {
                setupClockCarousel(ls_clock);
                setupPreview();
            }, 50);
        });
    }

    private void setupClockCarousel(List<ClockCarouselItemViewModel> ls_clock) {

        clockCarouselView.setUpClockCarouselView(ls_clock, onClockSelected -> {
            int selectedClock = onClockSelected.getClockLayout();
            if (updateRunnable != null) {
                handler.removeCallbacks(updateRunnable);
            }
            updateRunnable = () -> setPref(selectedClock);
            handler.postDelayed(updateRunnable, 500);
        });

        binding.clockCarouselView.screenPreviewClickView.setOnSideClickedListener(isStart -> {
            if (isStart) clockCarouselView.scrollToPrevious();
            else clockCarouselView.scrollToNext();
            return null;
        });
    }

    private void setupPreview() {
        if (getPreviewType() == PREVIEW_TYPE.LOCKSCREEN) {
            new WallpaperLoaderTask(requireContext(), this::onWallpaperLoad).loadWallpaper();
        } else if (getPreviewType() == PREVIEW_TYPE.AOD) {
            binding.clockCarouselView.preview.wallpaperFadeinScrim.setVisibility(View.VISIBLE);
            binding.clockCarouselView.preview.wallpaperPreviewSpinner.setVisibility(View.GONE);
            binding.clockCarouselView.preview.wallpaperFadeinScrim.setBackgroundColor(Color.BLACK);
        } else if (getPreviewType() == PREVIEW_TYPE.QS) {
            binding.clockCarouselView.previewHost.setVisibility(View.GONE);
            binding.clockCarouselView.qsPreview.setVisibility(View.VISIBLE);
            binding.clockCarouselView.qsPreviewLayout.batteryIcon.setImageDrawable(getBatteryIcon());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wallpaperBitmap != null) {
            wallpaperBitmap.recycle();
            wallpaperBitmap = null;
        }
        if (!executor.isTerminated()) {
            executor.shutdownNow();
        }
    }

}
