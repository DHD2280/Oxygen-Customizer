package it.dhd.oxygencustomizer.ui.fragments;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.BootLoopProtector.LOAD_TIME_KEY_KEY;
import static it.dhd.oxygencustomizer.xposed.utils.BootLoopProtector.PACKAGE_STRIKE_KEY_KEY;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ipc.RootService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import it.dhd.oxygencustomizer.IRootProviderService;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentHooksBinding;
import it.dhd.oxygencustomizer.services.RootProvider;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.xposed.utils.BootLoopProtector;

public class Hooks extends Fragment {

    private FragmentHooksBinding binding;
    private final String TAG = getClass().getSimpleName();
    IntentFilter intentFilterHookedPackages = new IntentFilter();
    private final List<String> hookedPackageList = new ArrayList<>();
    private List<String> monitorPackageList;
    private final String LSPosedDB = "/data/adb/lspd/config/modules_config.db";
    private int dotCount = 0;
    private ServiceConnection mCoreRootServiceConnection;
    private IRootProviderService mRootServiceIPC = null;
    private boolean rebootPending = false;
    private final String reboot_key = "reboot_pending";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHooksBinding.inflate(inflater, container, false);

        if (savedInstanceState != null) {
            rebootPending = savedInstanceState.getBoolean(reboot_key);
        }

        //binding.rebootButton.setOnClickListener(view -> AppUtils.Restart("system"));

        if (!rebootPending) {
            binding.rebootButton.hide();
        }

        startRootService();

        return binding.getRoot();
    }

    private void startRootService() {
        // Start RootService connection
        Intent intent = new Intent(getContext(), RootProvider.class);
        mCoreRootServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binding.loadingIndicator.setVisibility(View.GONE);
                mRootServiceIPC = IRootProviderService.Stub.asInterface(service);
                onRootServiceStarted();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mRootServiceIPC = null;
            }
        };
        RootService.bind(intent, mCoreRootServiceConnection);
    }

    private void onRootServiceStarted() {
        if (getContext() == null) {
            return;
        }

        intentFilterHookedPackages.addAction(Constants.ACTION_XPOSED_CONFIRMED);
        getContext().registerReceiver(receiverHookedPackages, intentFilterHookedPackages, Context.RECEIVER_EXPORTED);
        monitorPackageList = Arrays.asList(getResources().getStringArray(R.array.xposed_scope));
        checkHookedPackages();

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            checkHookedPackages();
            binding.swipeRefreshLayout.setRefreshing(false);
        });
    }

    private final BroadcastReceiver receiverHookedPackages = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), Constants.ACTION_XPOSED_CONFIRMED)) {
                String broadcastPackageName = intent.getStringExtra("packageName");

                for (int i = 0; i < binding.content.getChildCount(); i++) {
                    View list = binding.content.getChildAt(i);
                    TextView desc = list.findViewById(R.id.desc);
                    String pkgName = ((TextView) list.findViewById(R.id.title)).getText().toString();

                    if (pkgName.equals(broadcastPackageName)) {
                        binding.content.post(() -> {
                            desc.setText(getText(R.string.package_hooked_successful));
                            desc.setTextColor(getContext().getColor(android.R.color.system_accent1_400));
                        });
                    }
                }

                if (!hookedPackageList.contains(broadcastPackageName)) {
                    hookedPackageList.add(broadcastPackageName);
                }
            }
        }
    };


    private final CountDownTimer countDownTimer = new CountDownTimer(5000, 500) {
        @Override
        public void onTick(long millisUntilFinished) {
            dotCount = (dotCount + 1) % 4;
            String dots = new String(new char[dotCount]).replace('\0', '.');

            for (int i = 0; i < binding.content.getChildCount(); i++) {
                View list = binding.content.getChildAt(i);
                TextView desc = list.findViewById(R.id.desc);

                if (((String) desc.getText()).contains(getString(R.string.package_checking, ""))) {
                    desc.setText(getString(R.string.package_checking, dots));
                }
            }
        }

        @Override
        public void onFinish() {
            dotCount = 0;
            refreshListItem();
        }
    };

    private void checkHookedPackages() {
        hookedPackageList.clear();

        initListItem(monitorPackageList);
        new Thread(() -> getContext().sendBroadcast(new Intent().setAction(Constants.ACTION_CHECK_XPOSED_ENABLED))).start();
        waitAndRefresh();
    }

    private void waitAndRefresh() {
        countDownTimer.start();
    }

    private void initListItem(List<String> pack) {
        dotCount = 0;
        countDownTimer.cancel();

        if (binding.content.getChildCount() > 0) {
            binding.content.removeAllViews();
        }

        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(getContext()).inflate(R.layout.view_hooked_package_list, binding.content, false);
            int margin = getResources().getDimensionPixelSize(R.dimen.ui_container_margin_side);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) list.getLayoutParams();
            if (i == 0) {
                params.setMargins(margin, dp2px(getContext(), 12), margin, dp2px(getContext(), 6));
            } else if (i == pack.size() - 1) {
                params.setMargins(margin, dp2px(getContext(), 6), margin, dp2px(getContext(), 12));
            }

            TextView title = list.findViewById(R.id.title);
            title.setText(pack.get(i));

            TextView desc = list.findViewById(R.id.desc);
            if (isAppInstalled(pack.get(i))) {
                desc.setText(getString(R.string.package_checking, ""));
            } else {
                desc.setText(getText(R.string.package_not_found));
                desc.setTextColor(getContext().getColor(com.google.android.material.R.color.design_default_color_error));
            }

            ImageView preview = list.findViewById(R.id.icon);
            preview.setImageDrawable(getAppIcon(pack.get(i)));

            int finalI = i;

            MaterialButton activateInLSPosed = list.findViewById(R.id.activate_in_lsposed);
            activateInLSPosed.setOnClickListener(view -> {
                activateInLSPosed.setEnabled(false);
                try {
                    if (mRootServiceIPC.activateInLSPosed(pack.get(finalI))) {
                        activateInLSPosed.animate().setDuration(300).withEndAction(() -> activateInLSPosed.setVisibility(View.GONE)).start();
                        Toast.makeText(getContext(), getText(R.string.package_activated), Toast.LENGTH_SHORT).show();
                        binding.rebootButton.show();
                        rebootPending = true;
                    } else {
                        Toast.makeText(getContext(), getText(R.string.package_activation_failed), Toast.LENGTH_SHORT).show();
                        activateInLSPosed.setEnabled(true);
                    }
                } catch (RemoteException e) {
                    Toast.makeText(getContext(), getText(R.string.package_activation_failed), Toast.LENGTH_SHORT).show();
                    activateInLSPosed.setEnabled(true);
                    e.printStackTrace();
                }
            });

            list.setOnClickListener(view -> {
                // show ripple effect and do nothing
            });

            PopupMenu popupMenu = new PopupMenu(getContext(), list, Gravity.END);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.hooks_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.launch_app) {
                    Intent intent = getContext()
                            .getPackageManager()
                            .getLaunchIntentForPackage(pack.get(finalI));
                    if (intent != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(
                                getContext(),
                                getContext().getString(R.string.package_not_launchable),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                } else if (itemId == R.id.restart_app) {
                    handleApplicationRestart(pack.get(finalI));
                }

                return true;
            });

            list.setOnLongClickListener(v -> {
                popupMenu.show();
                return true;
            });

            binding.content.addView(list);
        }
    }

    private void handleApplicationRestart(String packageName) {
        if (Constants.Packages.FRAMEWORK.equals(packageName)) {
            AppUtils.restartScopes(requireContext(), new String[]{FRAMEWORK});
        } else if (SYSTEM_UI.equals(packageName)) {
            AppUtils.restartScopes(requireContext(), new String[]{SYSTEM_UI});
        } else {
            Shell.cmd(
                    "killall " + packageName,
                    "am force-stop " + packageName
            ).exec();
            Intent intent = getContext()
                    .getPackageManager()
                    .getLaunchIntentForPackage(packageName);
            if (intent != null) {
                startActivity(intent);
            }
        }
    }

    private void refreshListItem() {
        for (int i = 0; i < binding.content.getChildCount(); i++) {
            View list = binding.content.getChildAt(i);
            TextView desc = list.findViewById(R.id.desc);
            String pkgName = ((TextView) list.findViewById(R.id.title)).getText().toString();

            if (hookedPackageList.contains(pkgName)) {
                desc.setText(getText(R.string.package_hooked_successful));
                desc.setTextColor(getContext().getColor(android.R.color.system_accent1_400));
            } else {
                desc.setTextColor(getContext().getColor(R.color.error));

                desc.setText(getText(
                        isAppInstalled(pkgName)
                                ? checkLSPosedDB(pkgName)
                                ? isBootLooped(pkgName)
                                ? R.string.package_hook_bootlooped
                                : R.string.package_hook_no_response
                                : R.string.package_not_hook_enabled
                                : R.string.package_not_found));
            }

            if (desc.getText() == getText(R.string.package_not_hook_enabled)) {
                MaterialButton activateInLSPosed = list.findViewById(R.id.activate_in_lsposed);
                activateInLSPosed.setVisibility(View.VISIBLE);
                activateInLSPosed.setEnabled(true);
            }
        }
    }

    private boolean isAppInstalled(String packageName) {
        try {
            return mRootServiceIPC.isPackageInstalled(packageName);
        } catch (RemoteException e) {
            return false;
        }
    }

    private Drawable getAppIcon(String packageName) {
        try {
            return getContext().getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException ignored) {
            return ContextCompat.getDrawable(getContext(), R.drawable.ic_android);
        }
    }

    private boolean checkLSPosedDB(String pkgName) {
        try {
            return mRootServiceIPC.checkLSPosedDB(pkgName);
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean isBootLooped(String pkgName) {
        if (PreferenceHelper.getModulePrefs() != null) {
            SharedPreferences prefs = PreferenceHelper.getModulePrefs();
            String loadTimeKey = String.format("%s%s", LOAD_TIME_KEY_KEY, pkgName);
            String strikeKey = String.format("%s%s", PACKAGE_STRIKE_KEY_KEY, pkgName);
            long currentTime = Calendar.getInstance().getTime().getTime();
            long lastLoadTime = prefs.getLong(loadTimeKey, 0);
            int strikeCount = prefs.getInt(strikeKey, 0);

            if (strikeCount >= 3) {
                return true;
            }
            return false;
        }
        return false;
    }

    private int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @SuppressWarnings("unused")
    public static class StringBooleanMap {
        private final HashMap<String, Boolean> map = new HashMap<>();

        public void put(String key, boolean value) {
            map.put(key, value);
        }

        public boolean get(String key) {
            Boolean value = map.get(key);
            return value != null ? value : false;
        }

        public boolean containsKey(String key) {
            return map.containsKey(key);
        }

        public void remove(String key) {
            map.remove(key);
        }

        public void clear() {
            map.clear();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(reboot_key, rebootPending);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            rebootPending = savedInstanceState.getBoolean(reboot_key);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        countDownTimer.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getContext().unregisterReceiver(receiverHookedPackages);
        } catch (Exception ignored) {
        }
        countDownTimer.cancel();
    }
}
