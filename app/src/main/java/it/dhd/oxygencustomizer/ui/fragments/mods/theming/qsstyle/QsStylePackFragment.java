package it.dhd.oxygencustomizer.ui.fragments.mods.theming.qsstyle;

import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_STYLE_PACK;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentQsStylePackBinding;
import it.dhd.oxygencustomizer.ui.adapters.QsStylePackAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.xposed.utils.QsStylePackUtil;

public class QsStylePackFragment extends BaseFragment {

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private FragmentQsStylePackBinding binding;
    private QsStylePackAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQsStylePackBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.add(0, 1, 0, R.string.icon_pack_disclaimer_title)
                        .setIcon(R.drawable.settingslib_ic_info_outline_24)
                        .setIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)))
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() != 1) return false;
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.icon_pack_disclaimer_title)
                        .setMessage(R.string.qs_style_pack_disclaimer_desc)
                        .setPositiveButton(R.string.depth_effect_ok_btn, (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.resetButton.setOnClickListener(v -> applyStyle(""));
        loadStyles();
    }

    private void loadStyles() {
        Context context = requireContext().getApplicationContext();
        binding.loadingIndicator.setVisibility(View.VISIBLE);
        mExecutor.execute(() -> {
            List<QsStylePackUtil.Style> styles = QsStylePackUtil.queryStyles(context);
            mHandler.post(() -> {
                if (binding == null) return;
                binding.loadingIndicator.setVisibility(View.GONE);
                binding.noPacksLayout.setVisibility(styles.isEmpty() ? View.VISIBLE : View.GONE);
                mAdapter = new QsStylePackAdapter(styles, getSelectedKey(), style ->
                        applyStyle(style.getKey().equals(getSelectedKey()) ? "" : style.getKey()));
                binding.recyclerView.setAdapter(mAdapter);
                updateResetButton();
            });
        });
    }

    private String getSelectedKey() {
        return OCPreferences.getString(QS_STYLE_PACK, "");
    }

    private void applyStyle(String key) {
        OCPreferences.putString(QS_STYLE_PACK, key);
        if (mAdapter != null) mAdapter.setSelectedKey(key);
        updateResetButton();
        Toast.makeText(requireContext(), key.isEmpty() ? R.string.toast_disabled : R.string.toast_applied, Toast.LENGTH_SHORT).show();
    }

    private void updateResetButton() {
        binding.resetButton.setVisibility(getSelectedKey().isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        mExecutor.shutdown();
        super.onDestroy();
    }

    @Override
    public String getTitle() {
        return getString(R.string.qs_style_packs_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }
}
