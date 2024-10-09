package it.dhd.oxygencustomizer.ui.base;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.ui.base.BaseActivity.setHeader;
import static it.dhd.oxygencustomizer.utils.PreferenceHelper.getModulePrefs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentAppListBinding;
import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.ui.models.AppModel;
import it.dhd.oxygencustomizer.utils.AppUtils;

public abstract class AppFragmentBase extends BaseFragment {

    public SharedPreferences mPreferences;
    public FragmentAppListBinding binding;
    public boolean showSystem = false;

    public abstract String getTitle();

    public abstract boolean backButtonEnabled();

    public boolean hasMainSwitch() { return true; }

    public abstract String getFunctionTitle();

    public abstract String getFunctionSummary();

    public abstract boolean hasQuickLaunch();

    public String getQuickLaunchIntent() {
        return "";
    }

    public abstract OnShowSystemChange getShowSystemChange();

    public abstract boolean hasRestartScopes();

    public String[] getScopes() {
        return null;
    }

    public interface OnShowSystemChange {
        void onShowSystemChange(boolean showSystem);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentAppListBinding.inflate(inflater, container, false);

        mPreferences = getModulePrefs();

        if (hasMainSwitch()) {
            binding.appFunctionSwitch.setVisibility(View.VISIBLE);
            binding.appFunctionSwitch.setTitle(getFunctionTitle());
            binding.appFunctionSwitch.setSummary(getFunctionSummary());
        } else {
            binding.appFunctionSwitch.setVisibility(View.GONE);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMenu();
    }

    private void setupMenu() {
        MenuHost menuHost = requireActivity();
        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Add menu items here
                if (hasRestartScopes() && getScopes() != null) {
                    menu.add(0, 1, 0, R.string.restart_scopes)
                            .setIcon(R.drawable.ic_restart)
                            .setIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)))
                            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                }
                if (hasQuickLaunch()) {
                    menu.add(0, 2, 0, R.string.menu_launch_app)
                            .setIcon(R.drawable.ic_launch)
                            .setIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)))
                            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                }
                menu.add(0, 3, 0, R.string.menu_show_system_apps)
                        .setCheckable(true)
                        .setChecked(showSystem)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle the menu selection
                if (menuItem.getItemId() == 1) {
                    AppUtils.restartScopes(requireActivity(), getScopes());
                    return true;
                } else if (menuItem.getItemId() == 2) {
                    Intent intent = new Intent(getQuickLaunchIntent());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    getAppContext().startActivity(intent);
                    return true;
                } else if (menuItem.getItemId() == 3) {
                    showSystem = !menuItem.isChecked();
                    menuItem.setChecked(showSystem);
                    getShowSystemChange().onShowSystemChange(showSystem);
                    return true;
                }

                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            setHeader(getContext(), getTitle());
            ((MainActivity) getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(backButtonEnabled());
        }
    }

    public static class LoadAppsTask {
        private final Context mContext;
        private Map<String, Integer> mEnabledApps;
        private Set<String> mEnabledSetApps;
        private final OnTaskCompleted taskCompletedListener;
        private final OnPreTaskExecution preExecutionListener;
        private final boolean hasSlider;

        public interface OnTaskCompleted {
            void onTaskCompleted(List<AppModel> appList);
        }

        public interface OnPreTaskExecution {
            void onPreTaskExecution();
        }

        public LoadAppsTask(Context context,
                            Map<String, Integer> enabledApps,
                            boolean hasSlider,
                            OnPreTaskExecution preExecutionListener,
                            OnTaskCompleted taskCompleted) {
            this.mContext = context;
            this.hasSlider = hasSlider;
            this.mEnabledApps = enabledApps;
            this.preExecutionListener = preExecutionListener;
            this.taskCompletedListener = taskCompleted;
        }

        public LoadAppsTask(Context context,
                            Set<String> enabledApps,
                            boolean hasSlider,
                            OnPreTaskExecution preExecutionListener,
                            OnTaskCompleted taskCompleted) {
            this.mContext = context;
            this.hasSlider = hasSlider;
            this.mEnabledSetApps = enabledApps;
            this.preExecutionListener = preExecutionListener;
            this.taskCompletedListener = taskCompleted;
        }

        public void execute() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            if (preExecutionListener != null) {
                handler.post(preExecutionListener::onPreTaskExecution);
            }

            Callable<List<AppModel>> callable = () -> {
                List<AppModel> appList = new ArrayList<>();
                PackageManager packageManager = mContext.getPackageManager();
                List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0));
                for(ApplicationInfo app : apps) {
                    Integer value;
                    int enabledAppCount = 0;
                    if (hasSlider) {
                        value = mEnabledApps.getOrDefault(app.packageName, 0);
                        enabledAppCount  = (value != null) ? value : 0;
                    }
                    boolean isSystem = (app.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
                    appList.add(
                            hasSlider ?
                            new AppModel(
                                    app.loadLabel(packageManager).toString(),
                                    app.packageName,
                                    app.loadIcon(packageManager),
                                    isSystem,
                                    mEnabledApps.containsKey(app.packageName),
                                    enabledAppCount) :
                                    new AppModel(
                                            app.loadLabel(packageManager).toString(),
                                            app.packageName,
                                            app.loadIcon(packageManager),
                                            isSystem,
                                            mEnabledSetApps.contains(app.packageName)));
                }

                return appList;
            };

            Future<List<AppModel>> future = executor.submit(callable);

            executor.execute(() -> {
                try {
                    final List<AppModel> appList = future.get();
                    handler.post(() -> {
                        if (taskCompletedListener != null) {
                            taskCompletedListener.onTaskCompleted(appList);
                        }
                    });
                } catch (Exception e) {
                    Log.e(this.getClass().getName(), "Executor error", e);
                }
            });
        }
    }

}
