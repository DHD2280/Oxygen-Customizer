package it.dhd.oxygencustomizer.ui.fragments;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.utils.BootLoopProtector.PACKAGE_STRIKE_KEY_KEY;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ipc.RootService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import it.dhd.oxygencustomizer.IRootProviderService;
import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentHooksBinding;
import it.dhd.oxygencustomizer.services.RootProvider;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;

public class Hooks extends BaseFragment {

    private FragmentHooksBinding binding;
    private final String TAG = getClass().getSimpleName();
    IntentFilter intentFilterHookedPackages = new IntentFilter();
    private final List<String> hookedPackageList = new ArrayList<>();
    private List<String> monitorPackageList;
    private int dotCount = 0;
    private ServiceConnection mCoreRootServiceConnection;
    private IRootProviderService mRootServiceIPC = null;
    private final String reboot_key = "reboot_pending";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHooksBinding.inflate(inflater, container, false);

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
        requireContext().registerReceiver(receiverHookedPackages, intentFilterHookedPackages, Context.RECEIVER_EXPORTED);
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
                            desc.setTextColor(requireContext().getColor(android.R.color.system_accent1_400));
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
        new Thread(() -> requireContext().sendBroadcast(new Intent().setAction(Constants.ACTION_CHECK_XPOSED_ENABLED))).start();
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
                desc.setTextColor(requireContext().getColor(com.google.android.material.R.color.design_default_color_error));
            }

            ImageView preview = list.findViewById(R.id.icon);
            preview.setImageDrawable(getAppIcon(pack.get(i)));

            int finalI = i;

            list.setOnClickListener(view -> {
                // show ripple effect and do nothing
            });

            PopupMenu popupMenu = new PopupMenu(getContext(), list, Gravity.END);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.hooks_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.launch_app) {
                    Intent intent = requireContext()
                            .getPackageManager()
                            .getLaunchIntentForPackage(pack.get(finalI));
                    if (intent != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(
                                requireContext(),
                                OxygenCustomizer.getAppContextLocale().getString(R.string.package_not_launchable),
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
            Intent intent = requireContext()
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
                desc.setTextColor(requireContext().getColor(android.R.color.system_accent1_400));
            } else {
                desc.setTextColor(requireContext().getColor(R.color.error));

                desc.setText(getText(
                        isAppInstalled(pkgName)
                                ? isBootLooped(pkgName)
                                ? R.string.package_hook_bootlooped
                                : R.string.package_hook_no_response
                                : R.string.package_not_found));
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
            return requireContext().getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException ignored) {
            return ContextCompat.getDrawable(requireContext(), R.drawable.ic_android);
        }
    }

    private boolean isBootLooped(String pkgName) {
        if (PreferenceHelper.getModulePrefs() != null) {
            SharedPreferences prefs = PreferenceHelper.getModulePrefs();
            String strikeKey = String.format("%s%s", PACKAGE_STRIKE_KEY_KEY, pkgName);
            int strikeCount = prefs.getInt(strikeKey, 0);

            return strikeCount >= 3;
        }
        return false;
    }

    private int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
            MenuHost menuHost = requireActivity();
            // Add menu items without using the Fragment Menu APIs
            // Note how we can tie the MenuProvider to the viewLifecycleOwner
            // and an optional Lifecycle.State (here, RESUMED) to indicate when
            // the menu should be visible
            menuHost.addMenuProvider(new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    // Add menu items here
                    menu.add(0, 1, 0, R.string.info_hooks)
                            .setIcon(R.drawable.settingslib_ic_info_outline_24)
                            .setIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)))
                            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    // Handle the menu selection
                    if (menuItem.getItemId() == 1) {
                        showInfoDialog();
                        return true;
                    }
                    return true;
                }
            }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showInfoDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.info_hooks);
        builder.setMessage(getSpannedDescription());
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private SpannableStringBuilder getSpannedDescription() {
        String description = getString(R.string.info_hooks_desc);
        int startSpan = 0, endSpan = 0;
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(description);
        List<Object[]> targets = new ArrayList<>();
        targets.add(new Object[]{getString(R.string.package_hooked_successful), requireContext().getColor(android.R.color.system_accent1_400)});
        targets.add(new Object[]{getString(R.string.package_hook_no_response), requireContext().getColor(R.color.error)});
        targets.add(new Object[]{getString(R.string.package_hook_bootlooped), requireContext().getColor(R.color.error)});
        for (Object[] target : targets) {
            String targetText = (String) target[0];
            int color = (int) target[1];
            Log.i(TAG, "getSpannedDescription: " + targetText);
            int startIndex = description.indexOf(targetText);
            int endIndex = startIndex + targetText.length();

            if (startIndex != -1) {
                spannableStringBuilder.setSpan(new ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableStringBuilder;
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
            requireContext().unregisterReceiver(receiverHookedPackages);
        } catch (Exception ignored) {
        }
        countDownTimer.cancel();
    }

    @Override
    public String getTitle() {
        return getString(R.string.hooks_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }
}
