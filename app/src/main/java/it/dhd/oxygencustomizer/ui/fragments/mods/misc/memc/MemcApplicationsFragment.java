package it.dhd.oxygencustomizer.ui.fragments.mods.misc.memc;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.PreferenceHelper.getModulePrefs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentAppListBinding;
import it.dhd.oxygencustomizer.ui.adapters.MemcAppAdapter;
import it.dhd.oxygencustomizer.ui.adapters.PackageListAdapter;
import it.dhd.oxygencustomizer.ui.dialogs.MemcDialog;
import it.dhd.oxygencustomizer.ui.models.MemcAppModel;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.hooks.framework.MemcEnhancer;

public class MemcApplicationsFragment extends Fragment {

    private Map<String, String> mEnabledApps;
    private Set<String> mAlreadyAddedApps = new HashSet<>();
    public SharedPreferences mPreferences;
    public FragmentAppListBinding binding;
    private PackageListAdapter mPackageAdapter;
    private PackageManager mPackageManager;
    private MemcDialog mMemcDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            mPackageManager = requireActivity().getPackageManager();
            mPackageAdapter = new PackageListAdapter(requireActivity());
        }).start();

        mMemcDialog = new MemcDialog(requireActivity());
        mMemcDialog.setTitle("MEMC Configuration");

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentAppListBinding.inflate(inflater, container, false);
        binding.appFunctionSwitch.setVisibility(View.GONE);

        mPreferences = getModulePrefs();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Set<String> enabledApps = mPreferences.getStringSet("custom_memc_applications", new ArraySet<>());
        Log.w("MemcApplicationsFragment", "onViewCreated enabledApps: " + enabledApps);
        mEnabledApps = new ArrayMap<>();
        for (String item : enabledApps) {
            if (item.contains("|")) {
                List<String> arr = new ArrayList<>(Arrays.asList(item.split("\\|")));
                if (arr.size() < 2 || arr.get(1).isBlank()) {
                    arr.set(1, "120$267-3-1");
                }
                mEnabledApps.put(arr.get(0), arr.get(1));
                mAlreadyAddedApps.add(arr.get(0));
            } else {
                mEnabledApps.put(item, "120$267-3-1");
                mAlreadyAddedApps.add(item);
            }
        }

        mPackageAdapter.setExcludedPackages((HashSet<String>) mAlreadyAddedApps);

        new LoadAppsTask(
                mPackageManager,
                mEnabledApps, () -> {
            binding.searchViewLayout.setEnabled(false);
            binding.progress.setVisibility(View.VISIBLE);
        }, appList -> {
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getAppContext()));
            binding.recyclerView.setAdapter(new MemcAppAdapter(appList,
                    this::onItemClick));
            binding.recyclerView.setHasFixedSize(true);
            binding.searchViewLayout.setEnabled(true);
            binding.progress.setVisibility(View.GONE);
            binding.searchView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ((MemcAppAdapter) binding.recyclerView.getAdapter()).filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }).execute();

    }

    @SuppressLint("NotifyDataSetChanged")
    public void addItem() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setAdapter(mPackageAdapter, (dialog, which) -> {
            PackageListAdapter.PackageItem info = mPackageAdapter.getItem(which);
            MemcAppModel newApp = new MemcAppModel(
                    (String) info.title,
                    info.packageName,
                    info.icon);
            mMemcDialog.show(
                    newApp,
                    (newModel) -> {
                        mEnabledApps.put(info.packageName, newModel.getRefreshRate() + "$" + newModel.getMemcConfig());
                        ((MemcAppAdapter) binding.recyclerView.getAdapter()).addItem(
                                newModel);
                        binding.recyclerView.getAdapter().notifyDataSetChanged();
                        mAlreadyAddedApps.add(info.packageName);
                        mPackageAdapter.setExcludedPackages((HashSet<String>) mAlreadyAddedApps);
                        savePrefs();
                    },
                    null
            );

        });
        builder.setTitle(getString(R.string.add_app));
        builder.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onItemClick(MemcAppModel model) {
        mMemcDialog.show(model,
                (newModel) -> {
                    mEnabledApps.remove(model.getPackageName());
                    mEnabledApps.put(model.getPackageName(), newModel.getRefreshRate() + "$" + newModel.getMemcConfig());
                    binding.recyclerView.getAdapter().notifyDataSetChanged();
                    savePrefs();
                },
                () -> {
                    mEnabledApps.remove(model.getPackageName());
                    mAlreadyAddedApps.remove(model.getPackageName());
                    ((MemcAppAdapter) binding.recyclerView.getAdapter()).removeItem(model);
                    binding.recyclerView.getAdapter().notifyDataSetChanged();
                    mPackageAdapter.setExcludedPackages((HashSet<String>) mAlreadyAddedApps);
                    savePrefs();
                }
        );
    }

    private void savePrefs() {
        Set<String> enabledApps = new HashSet<>();
        for (Map.Entry<String, String> entry : mEnabledApps.entrySet()) {
            enabledApps.add(entry.getKey() + "|" + entry.getValue());
        }
        mPreferences.edit().putStringSet("custom_memc_applications", enabledApps).apply();

        Intent broadcast = new Intent(Constants.ACTION_SETTINGS_CHANGED);

        broadcast.putExtra("packageName", FRAMEWORK);
        broadcast.putExtra("class", MemcEnhancer.class.getSimpleName());

        if (getContext() != null)
            getContext().sendBroadcast(broadcast);
    }

    public static class LoadAppsTask {
        private Map<String, String> mEnabledApps;
        private final PackageManager mPackageManager;
        private final LoadAppsTask.OnTaskCompleted taskCompletedListener;
        private final LoadAppsTask.OnPreTaskExecution preExecutionListener;

        public interface OnTaskCompleted {
            void onTaskCompleted(List<MemcAppModel> appList);
        }

        public interface OnPreTaskExecution {
            void onPreTaskExecution();
        }

        public LoadAppsTask(PackageManager packageManager,
                            Map<String, String> enabledApps,
                            LoadAppsTask.OnPreTaskExecution preExecutionListener,
                            LoadAppsTask.OnTaskCompleted taskCompleted) {
            this.mPackageManager = packageManager;
            this.mEnabledApps = enabledApps;
            this.preExecutionListener = preExecutionListener;
            this.taskCompletedListener = taskCompleted;
        }

        public void execute() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            if (preExecutionListener != null) {
                handler.post(preExecutionListener::onPreTaskExecution);
            }

            Callable<List<MemcAppModel>> callable = () -> {
                List<MemcAppModel> appList = new ArrayList<>();
                for(Map.Entry<String, String> entry : mEnabledApps.entrySet()) {
                    String pkg = entry.getKey();
                    String values = entry.getValue();
                    int refreshRate = 120;
                    String memcConfig = "267-3-1";
                    if (values.contains("$")) {
                        String[] arr = values.split("\\$");
                        refreshRate = Integer.parseInt(arr[0]);
                        memcConfig = arr[1];
                    }
                    try {
                        ApplicationInfo appInfo = mPackageManager.getApplicationInfo(pkg, 0);
                        String appName = (String) mPackageManager.getApplicationLabel(appInfo);
                        Drawable appIcon = mPackageManager.getApplicationIcon(appInfo);

                        appList.add(
                                new MemcAppModel(
                                        appName,
                                        pkg,
                                        "",
                                        appIcon,
                                        false,
                                        refreshRate,
                                        memcConfig));

                    } catch (PackageManager.NameNotFoundException e) {
                        mEnabledApps.remove(pkg);
                    }
                }

                return appList;
            };

            Future<List<MemcAppModel>> future = executor.submit(callable);

            executor.execute(() -> {
                try {
                    final List<MemcAppModel> appList = future.get();
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
