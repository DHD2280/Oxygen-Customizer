package it.dhd.oxygencustomizer.ui.views;

import static it.dhd.oxygencustomizer.utils.Dynamic.skippedInstallation;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.TRANSITION_DELAY;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.XPOSED_ONLY_MODE;
import static it.dhd.oxygencustomizer.utils.helper.Logger.writeLog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.RenderMode;
import com.airbnb.lottie.SimpleColorFilter;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ViewOnboardingPageBinding;
import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.ui.adapters.OnboardingAdapter;
import it.dhd.oxygencustomizer.ui.core.Transform;
import it.dhd.oxygencustomizer.ui.dialogs.ErrorDialog;
import it.dhd.oxygencustomizer.ui.dialogs.InstallationDialog;
import it.dhd.oxygencustomizer.ui.entity.OnboardingPage;
import it.dhd.oxygencustomizer.utils.Animatoo;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.FileUtil;
import it.dhd.oxygencustomizer.utils.ModuleConstants;
import it.dhd.oxygencustomizer.utils.ModuleUtil;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.RootUtil;
import it.dhd.oxygencustomizer.utils.extension.TaskExecutor;
import it.dhd.oxygencustomizer.utils.helper.BackupRestore;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;
import it.dhd.oxygencustomizer.utils.overlay.compiler.OnboardingCompiler;

@SuppressWarnings("unused")
public class OnboardingView extends FrameLayout {

    private static ViewOnboardingPageBinding binding;
    private static final String TAG = OnboardingView.class.getSimpleName();
    private int numberOfPages;
    private static boolean isClickable = true;
    private static boolean clickedContinue = false;
    private static boolean hasErroredOut = false;
    private static StartInstallationProcess installModule = null;
    private InstallationDialog progressDialog;
    private String logger = null, prev_log = null;

    public OnboardingView(@NonNull Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public OnboardingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public OnboardingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    public OnboardingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = ViewOnboardingPageBinding.inflate(inflater, this, true);

        numberOfPages = OnboardingPage.values().length;

        setUpSlider();
        initButtonsClickListeners();
        setLottieColorFilter(getButtonTextColor());

        progressDialog = new InstallationDialog(getContext());

        binding.startBtn.getViewTreeObserver().addOnDrawListener(() -> {
            if (binding.startBtn.getAlpha() <= 0.1f) {
                binding.startBtn.setVisibility(GONE);
            } else {
                binding.startBtn.setVisibility(VISIBLE);
            }
        });
    }

    private void setUpSlider() {
        binding.slider.setAdapter(new OnboardingAdapter());

        binding.slider.setPageTransformer(Transform::setParallaxTransformation);

        binding.slider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (numberOfPages > 1) {
                    float newProgress = (position + positionOffset) / (numberOfPages - 1);
                    binding.onboardingRoot.setProgress(newProgress);
                }
            }
        });

        binding.pageIndicator.attachTo(binding.slider);
    }

    private void initButtonsClickListeners() {
        binding.nextBtn.setOnClickListener(view -> navigateToNextSlide());
        binding.skipBtn.setOnClickListener(view -> navigateToLastSlide());
        binding.startBtn.setOnClickListener(view -> startOnClickActions());
        binding.startBtn.setOnLongClickListener(view -> startOnLongClickActions());
    }

    private void navigateToNextSlide() {
        int nextSlidePos = binding.slider.getCurrentItem() + 1;
        binding.slider.setCurrentItem(nextSlidePos, true);
    }

    public static void navigateToPrevSlide() {
        int prevSlidePos = binding.slider.getCurrentItem() - 1;
        if (prevSlidePos < 0) {
            throw new IndexOutOfBoundsException("Can't navigate to previous slide");
        }
        binding.slider.setCurrentItem(prevSlidePos, true);
    }

    private void navigateToLastSlide() {
        int lastSlidePos = numberOfPages - 1;
        binding.slider.setCurrentItem(lastSlidePos, true);
    }

    private void startOnClickActions() {
        if (!isClickable) return;

        isClickable = false;
        skippedInstallation = false;
        hasErroredOut = false;

        Shell.getShell(shell -> {
            if (!RootUtil.isDeviceRooted()) {
                ErrorDialog errorDialog = new ErrorDialog(getContext());
                errorDialog.show(R.string.root_not_found_title, R.string.root_not_found_desc);
                return;
            }

            if (!RootUtil.deviceProperlyRooted()) {
                ErrorDialog errorDialog = new ErrorDialog(getContext());
                errorDialog.show(R.string.compatible_root_not_found_title, R.string.compatible_root_not_found_desc);
                return;
            }

            if (!AppUtils.hasStoragePermission()) {
                Toast.makeText(getContext(), R.string.need_storage_perm_title, Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    clickedContinue = true;
                    AppUtils.requestStoragePermission(getContext());
                }, clickedContinue ? 10 : 1000);
            } else {
                boolean moduleExists = ModuleUtil.moduleExists();
                boolean overlayExists = OverlayUtil.overlayExists();

                if (!ModuleUtil.checkModuleVersion(OxygenCustomizer.getAppContext()) || !moduleExists || !overlayExists) {

                    handleInstallation();
                } else {
                    Prefs.putBoolean(XPOSED_ONLY_MODE, false);
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                    Animatoo.animateSlideLeft(getContext());
                }
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> isClickable = true, TRANSITION_DELAY + 50);
    }

    private boolean startOnLongClickActions() {
        skippedInstallation = true;
        hasErroredOut = false;

        Shell.getShell(shell -> {
            if (!RootUtil.isDeviceRooted()) {
                ErrorDialog errorDialog = new ErrorDialog(getContext());
                errorDialog.show(R.string.root_not_found_title, R.string.root_not_found_desc);
                return;
            }

            if (!RootUtil.deviceProperlyRooted()) {
                ErrorDialog errorDialog = new ErrorDialog(getContext());
                errorDialog.show(R.string.compatible_root_not_found_title, R.string.compatible_root_not_found_desc);
                return;
            }

            if (!AppUtils.hasStoragePermission()) {
                Toast.makeText(getContext(), R.string.need_storage_perm_title, Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    clickedContinue = true;
                    AppUtils.requestStoragePermission(getContext());
                }, clickedContinue ? 10 : 1000);
            } else {
                if (!ModuleUtil.moduleExists()) {
                    handleInstallation();
                } else {
                    Prefs.putBoolean(XPOSED_ONLY_MODE, true);
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                    Animatoo.animateSlideLeft(getContext());
                    Toast.makeText(getContext(), R.string.toast_skipped_installation, Toast.LENGTH_LONG).show();
                }
            }
        });
        return true;
    }

    private void handleInstallation() {
        LottieCompositionFactory.fromRawRes(getContext(), R.raw.loading_anim).addListener(result -> {
            binding.loadingAnim.setMaxWidth(binding.startBtn.getHeight());
            binding.loadingAnim.setMaxHeight(binding.startBtn.getHeight());
            binding.loadingAnim.setAnimation(R.raw.loading_anim);
            binding.loadingAnim.setRenderMode(RenderMode.HARDWARE);
            binding.loadingAnim.setVisibility(LottieAnimationView.VISIBLE);
            binding.startBtn.setTextColor(Color.TRANSPARENT);

            installModule = new StartInstallationProcess();
            installModule.execute();
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class StartInstallationProcess extends TaskExecutor<Void, Integer, Integer> {
        @SuppressLint("SetTextI18n")
        @Override
        protected void onPreExecute() {
            binding.startBtn.setText(R.string.btn_lets_go);

            progressDialog.show(getResources().getString(R.string.installing), getResources().getString(R.string.init_module_installation));
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            for (Integer value : values) {

                String title = getResources().getString(R.string.step) + ' ' + value + "/6";
                getResources().getString(R.string.loading_dialog_wait);
                String desc = switch (value) {
                    case 1 -> getResources().getString(R.string.module_installation_step1);
                    case 2 -> getResources().getString(R.string.module_installation_step2);
                    case 3 -> getResources().getString(R.string.module_installation_step3);
                    case 4 -> getResources().getString(R.string.module_installation_step4);
                    case 5 -> getResources().getString(R.string.module_installation_step5);
                    case 6 -> getResources().getString(R.string.module_installation_step6);
                    default -> getResources().getString(R.string.loading_dialog_wait);
                };

                progressDialog.setMessage(title, desc);

                if (logger != null && !Objects.equals(prev_log, logger)) {
                    progressDialog.setLogs(logger);
                    prev_log = logger;
                }
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int step = 0;

            logger = "I: Creating blank module template";
            publishProgress(++step);
            ModuleUtil.handleModule();

            logger = null;
            publishProgress(++step);
            try {
                logger = "I: Cleaning oxygen customizer data directory";
                publishProgress(step);
                // Clean data directory
                Shell.cmd("rm -rf " + ModuleConstants.DATA_DIR + "/Overlays").exec();

                logger = "I: Extracting overlays from assets";
                publishProgress(step);
                if (skippedInstallation) {
                    waiter(100);
                    logger = "I: Skipped...";
                    publishProgress(step);
                    waiter(100);
                } else {
                    // Extract overlays from assets
                    FileUtil.copyAssets("Overlays");
                }

                logger = "I: Creating temporary directories";
                publishProgress(step);
                // Create temp directory
                Shell.cmd("mkdir -p " + ModuleConstants.TEMP_OVERLAY_DIR).exec();
                Shell.cmd("mkdir -p " + ModuleConstants.UNSIGNED_UNALIGNED_DIR).exec();
                Shell.cmd("mkdir -p " + ModuleConstants.UNSIGNED_DIR).exec();
                Shell.cmd("mkdir -p " + ModuleConstants.SIGNED_DIR).exec();
            } catch (IOException e) {
                hasErroredOut = true;
                Log.e(TAG, e.toString());
            }

            logger = null;
            publishProgress(++step);
            File dir;
            if (skippedInstallation) {
                logger = "I: Skipping overlay builder...";
                publishProgress(step);
                waiter(100);
            } else {
                // Create AndroidManifest.xml and build Overlay using AAPT
                dir = new File(ModuleConstants.DATA_DIR + "/Overlays");
                if (dir.listFiles() == null) hasErroredOut = true;

                if (!hasErroredOut) {
                    for (File pkg : Objects.requireNonNull(dir.listFiles())) {
                        if (pkg.isDirectory()) {
                            for (File overlay : Objects.requireNonNull(pkg.listFiles())) {
                                if (overlay.isDirectory()) {
                                    String overlay_name = overlay.toString().replace(pkg.toString() + '/', "");

                                    if (OnboardingCompiler.createManifest(overlay_name, pkg.toString().replace(ModuleConstants.DATA_DIR + "/Overlays/", ""), overlay.getAbsolutePath())) {
                                        hasErroredOut = true;
                                    }

                                    logger = "I: Building Overlay for " + overlay_name;
                                    publishProgress(step);

                                    if (!hasErroredOut && OnboardingCompiler.runAapt(overlay.getAbsolutePath(), overlay_name)) {
                                        hasErroredOut = true;
                                    }
                                }
                                if (hasErroredOut) break;
                            }
                        }
                        if (hasErroredOut) break;
                    }
                }
            }

            logger = null;
            publishProgress(++step);
            if (skippedInstallation) {
                logger = "I: Skipping zipalign process...";
                publishProgress(step);
                waiter(100);
            } else {
                // ZipAlign the Overlay
                dir = new File(ModuleConstants.UNSIGNED_UNALIGNED_DIR);
                if (dir.listFiles() == null) hasErroredOut = true;

                if (!hasErroredOut) {
                    for (File overlay : Objects.requireNonNull(dir.listFiles())) {
                        if (!overlay.isDirectory()) {
                            String overlay_name = overlay.toString().replace(ModuleConstants.UNSIGNED_UNALIGNED_DIR + '/', "").replace("-unaligned", "");

                            logger = "I: Zip aligning Overlay " + overlay_name.replace("-unsigned.apk", "");
                            publishProgress(step);

                            if (OnboardingCompiler.zipAlign(overlay.getAbsolutePath(), overlay_name)) {
                                hasErroredOut = true;
                            }
                        }
                        if (hasErroredOut) break;
                    }
                }
            }

            logger = null;
            publishProgress(++step);
            if (skippedInstallation) {
                logger = "W: Skipping signing process...";
                publishProgress(step);
                waiter(100);
            } else {
                // Sign the Overlay
                dir = new File(ModuleConstants.UNSIGNED_DIR);
                if (dir.listFiles() == null) hasErroredOut = true;

                if (!hasErroredOut) {
                    for (File overlay : Objects.requireNonNull(dir.listFiles())) {
                        if (!overlay.isDirectory()) {
                            String overlay_name = overlay.toString().replace(ModuleConstants.UNSIGNED_DIR + '/', "").replace("-unsigned", "");

                            logger = "I: Signing Overlay " + overlay_name.replace(".apk", "");
                            publishProgress(step);

                            int attempt = 3;
                            while (attempt-- != 0) {
                                hasErroredOut = OnboardingCompiler.apkSigner(overlay.getAbsolutePath(), overlay_name);

                                if (!hasErroredOut) break;
                                else waiter(1000);
                            }
                        }
                        if (hasErroredOut) break;
                    }
                }
            }

            logger = "I: Moving overlays to system directory";
            publishProgress(++step);
            if (skippedInstallation) {
                waiter(100);
                logger = "Skipping...";
                publishProgress(step);
            }
            // Move all generated overlays to system dir and flash as module
            if (!hasErroredOut) {
                Shell.cmd("cp -a " + ModuleConstants.SIGNED_DIR + "/. " + ModuleConstants.TEMP_MODULE_OVERLAY_DIR).exec();
                BackupRestore.restoreFiles();

                try {
                    hasErroredOut = ModuleUtil.flashModule(ModuleUtil.createModule(ModuleConstants.TEMP_MODULE_DIR, ModuleConstants.TEMP_DIR + "/OxygenCustomizer.zip"));
                } catch (Exception e) {
                    hasErroredOut = true;
                    writeLog(TAG, "Failed to create/flash module zip", e);
                    Log.e(TAG, "Failed to create/flash module zip\n" + e);
                }
            }

            logger = "I: Cleaning temporary directories";
            publishProgress(step);
            // Clean temp directory
            Shell.cmd("rm -rf " + ModuleConstants.TEMP_DIR).exec();
            Shell.cmd("rm -rf " + ModuleConstants.DATA_DIR + "/Overlays").exec();

            if (!hasErroredOut) {
                logger = "I: Installation process finished";
                publishProgress(step);
                waiter(100);

                logger = "You should reboot your device";
            } else {
                logger = "E: Installation process failed";
            }

            publishProgress(step);
            waiter(500);

            logger = "Closing in...";
            publishProgress(step);
            waiter(1000);

            logger = "3...";
            publishProgress(step);
            waiter(1000);

            logger = "2..";
            publishProgress(step);
            waiter(1000);

            logger = "1.";
            publishProgress(step);
            waiter(1000);

            return null;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(Integer integer) {
            progressDialog.hide();

            if (!hasErroredOut) {
                if (!skippedInstallation) {

                    Prefs.putBoolean(XPOSED_ONLY_MODE, false);

                    if (OverlayUtil.overlayExists()) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            getContext().startActivity(intent);
                            Animatoo.animateSlideLeft(getContext());
                        }, 10);
                    } else {
                        Toast.makeText(getContext(), R.string.need_reboot_title, Toast.LENGTH_SHORT).show();
                        binding.startBtn.setText(R.string.btn_reboot);
                        binding.startBtn.setTextColor(getButtonTextColor());
                        binding.startBtn.setOnClickListener(view -> AppUtils.restartDevice());
                        binding.startBtn.setOnLongClickListener(null);
                    }
                } else {
                    Prefs.putBoolean(XPOSED_ONLY_MODE, true);
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                    Animatoo.animateSlideLeft(getContext());
                    Toast.makeText(getContext(), R.string.one_time_reboot_needed, Toast.LENGTH_LONG).show();
                }
            } else {
                cancelledInstallation();
                ErrorDialog errorDialog = new ErrorDialog(getContext());
                errorDialog.show(R.string.installation_failed_title, R.string.installation_failed_desc);
                binding.startBtn.setText(R.string.btn_lets_go);
            }

            binding.loadingAnim.setVisibility(LottieAnimationView.GONE);
            binding.startBtn.setTextColor(getButtonTextColor());
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            cancelledInstallation();
        }
    }

    private @ColorInt int getButtonTextColor() {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        return typedValue.data;
    }

    private void setLottieColorFilter(@ColorInt int color) {
        LottieValueCallback<ColorFilter> callback = new LottieValueCallback<>(new SimpleColorFilter(color));
        binding.loadingAnim.addValueCallback(new KeyPath("**"), LottieProperty.COLOR_FILTER, callback);
    }

    private void cancelledInstallation() {
        Prefs.clearPref(XPOSED_ONLY_MODE);
        Shell.cmd("rm -rf " + ModuleConstants.DATA_DIR).exec();
        Shell.cmd("rm -rf " + ModuleConstants.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + ModuleConstants.BACKUP_DIR).exec();
        Shell.cmd("rm -rf " + ModuleConstants.MODULE_DIR).exec();
    }

    private void waiter(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception ignored) {
        }
    }
}
