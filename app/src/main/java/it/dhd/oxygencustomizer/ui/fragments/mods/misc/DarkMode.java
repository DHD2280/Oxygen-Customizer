package it.dhd.oxygencustomizer.ui.fragments.mods.misc;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.ui.base.BaseActivity.setHeader;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SETTINGS;
import static it.dhd.oxygencustomizer.utils.PreferenceHelper.getModulePrefs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentDarkModeBinding;
import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.ui.adapters.DarkModeAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.models.AppModel;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Constants;

public class DarkMode extends BaseFragment {

    private FragmentDarkModeBinding binding;
    private List<AppModel> mAppList;
    private Map<String, Integer> mEnabledApps;
    private SharedPreferences mPrefs;
    private boolean showSystem = false;

    @Override
    public String getTitle() {
        return getString(R.string.dark_mode);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentDarkModeBinding.inflate(inflater, container, false);
        mPrefs = getModulePrefs();

        Set<String> enabledApps = mPrefs.getStringSet("custom_dark_mode", new ArraySet<>());
        mEnabledApps = new ArrayMap<>();
        for (String item : enabledApps) {
            if (item.contains("|")) {
                List<String> arr = new ArrayList<>(Arrays.asList(item.split("\\|")));
                if (arr.size() < 2 || arr.get(1).isBlank()) {
                    arr.set(1, "0");
                }
                Log.d("DarkMode", "onCreateView: " + arr.get(0) + "|" + Integer.parseInt(arr.get(1)));
                mEnabledApps.put(arr.get(0), Integer.parseInt(arr.get(1)));
            } else {
                mEnabledApps.put(item, 0);
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupMenu();

        binding.darkModeSwitch.setSwitchChangeListener((buttonView, isChecked) -> {
            mPrefs.edit().putBoolean("custom_dark_mode_switch", isChecked).apply();
        });
        binding.darkModeSwitch.setSwitchChecked(mPrefs.getBoolean("custom_dark_mode_switch", false));

        new LoadAppsTask(getAppContext(), mEnabledApps, () -> {
            binding.searchViewLayout.setEnabled(false);
            binding.progress.setVisibility(View.VISIBLE);
        }, appList -> {
            mAppList = appList;
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getAppContext()));
            binding.recyclerView.setAdapter(new DarkModeAdapter(appList,
                    this::onSwitchChange,
                    this::onSliderChange));
            binding.recyclerView.setHasFixedSize(true);
            binding.searchViewLayout.setEnabled(true);
            binding.progress.setVisibility(View.GONE);
            ((DarkModeAdapter) binding.recyclerView.getAdapter()).showSystem(showSystem);
            binding.searchView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ((DarkModeAdapter) binding.recyclerView.getAdapter()).filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }).execute();

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
                menu.add(0, 1, 0, R.string.restart_scopes)
                        .setIcon(R.drawable.ic_restart)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                menu.add(0, 2, 0, R.string.menu_launch_app)
                        .setIcon(R.drawable.ic_launch)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                menu.add(0, 3, 0, R.string.menu_show_system_apps)
                        .setCheckable(true)
                        .setChecked(showSystem)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle the menu selection
                if (menuItem.getItemId() == 1) {
                    AppUtils.restartScopes(requireActivity(), new String[]{SETTINGS});
                    return true;
                } else if (menuItem.getItemId() == 2) {
                    Intent intent = new Intent("com.android.settings.DISPLAY_SETTINGS");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    getAppContext().startActivity(intent);
                    return true;
                } else if (menuItem.getItemId() == 3) {
                    showSystem = !menuItem.isChecked();
                    menuItem.setChecked(showSystem);
                    ((DarkModeAdapter) binding.recyclerView.getAdapter()).showSystem(showSystem);
                    return true;
                }

                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void onSwitchChange(AppModel model, boolean isChecked) {
        model.setEnabled(isChecked);
        if (mEnabledApps.containsKey(model.getPackageName())) {
            if (!isChecked) mEnabledApps.remove(model.getPackageName());
        } else {
            mEnabledApps.put(model.getPackageName(), model.getDarkModeValue());
        }
        savePrefs();
    }

    private void onSliderChange(AppModel model, int progress) {
        if (mEnabledApps.containsKey(model.getPackageName())) {
            mEnabledApps.put(model.getPackageName(), progress);
        }
        savePrefs();
    }

    private void savePrefs() {
        Log.d("DarkMode", "savePrefs: " + mEnabledApps.size());
        Set<String> enabledApps = new HashSet<>();
        for (Map.Entry<String, Integer> entry : mEnabledApps.entrySet()) {
            enabledApps.add(entry.getKey() + "|" + entry.getValue());
        }
        mPrefs.edit().putStringSet("custom_dark_mode", enabledApps).apply();

        Intent broadcast = new Intent(Constants.ACTION_SETTINGS_CHANGED);

        broadcast.putExtra("packageName", FRAMEWORK);
        broadcast.putExtra("class", it.dhd.oxygencustomizer.xposed.hooks.framework.DarkMode.class.getSimpleName());

        if (getContext() != null)
            getContext().sendBroadcast(broadcast);
    }

    public static class LoadAppsTask {
        private final Context mContext;
        private final Map<String, Integer> mEnabledApps;
        private final OnTaskCompleted taskCompletedListener;
        private final OnPreTaskExecution preExecutionListener;

        public interface OnTaskCompleted {
            void onTaskCompleted(List<AppModel> appList);
        }

        public interface OnPreTaskExecution {
            void onPreTaskExecution();
        }

        public LoadAppsTask(Context context,
                            Map<String, Integer> enabledApps,
                            OnPreTaskExecution preExecutionListener,
                            OnTaskCompleted tastCompleted) {
            this.mContext = context;
            this.mEnabledApps = enabledApps;
            this.preExecutionListener = preExecutionListener;
            this.taskCompletedListener = tastCompleted;
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
                    Integer value = mEnabledApps.getOrDefault(app.packageName, 0);
                    int enabledAppCount = (value != null) ? value : 0;
                    boolean isSystem = (app.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
                    Log.d("DarkMode", "execute: " + app.packageName + " " + isSystem);
                    appList.add(
                            new AppModel(
                                    app.loadLabel(packageManager).toString(),
                                    app.packageName,
                                    app.loadIcon(packageManager),
                                    isSystem,
                                    mEnabledApps.containsKey(app.packageName),
                                    enabledAppCount));
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
                    e.printStackTrace();
                }
            });
        }
    }


}
