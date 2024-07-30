package it.dhd.oxygencustomizer.ui.fragments.uistyle;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SELECTED_TOAST_FRAME;
import static it.dhd.oxygencustomizer.utils.overlay.compiler.OnDemandCompiler.buildOverlay;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentRecyclerBinding;
import it.dhd.oxygencustomizer.databinding.FragmentToastFrameBinding;
import it.dhd.oxygencustomizer.ui.adapters.ToastAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.models.ToastModel;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;
import it.dhd.oxygencustomizer.utils.overlay.compiler.OnDemandCompiler;

public class ToastFrame extends BaseFragment {

    private FragmentRecyclerBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // RecyclerView
        GridLayoutManager gridLayout = new GridLayoutManager(requireContext(), 2);
        gridLayout.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int totalItemCount = binding.recyclerViewFragment.getAdapter() != null ? binding.recyclerViewFragment.getAdapter().getItemCount() : 1;
                int spanCount = gridLayout.getSpanCount();

                // Calculate the number of items in the last row
                int itemsInLastRow = totalItemCount % spanCount;
                if (position >= totalItemCount - itemsInLastRow) {
                    // Adjust span size for the last row
                    if (itemsInLastRow == 1) {
                        return 1;
                    } else {
                        return 2;
                    }
                } else {
                    return 1; // Default span size (1 column each)
                }
            }
        });
        binding.recyclerViewFragment.setLayoutManager(gridLayout);
        binding.recyclerViewFragment.setAdapter(initToastStyles());
        binding.recyclerViewFragment.setHasFixedSize(true);

        return view;
    }

    private ToastAdapter initToastStyles() {
        ArrayList<ToastModel> toastFrameStyle = new ArrayList<>() {{
            add(new ToastModel(R.drawable.toast_frame_style_1, R.string.style_0));
            add(new ToastModel(R.drawable.toast_frame_style_1, R.string.style_1));
            add(new ToastModel(R.drawable.toast_frame_style_2, R.string.style_2));
            add(new ToastModel(R.drawable.toast_frame_style_3, R.string.style_3));
            add(new ToastModel(R.drawable.toast_frame_style_4, R.string.style_4));
            add(new ToastModel(R.drawable.toast_frame_style_5, R.string.style_5));
            add(new ToastModel(R.drawable.toast_frame_style_6, R.string.style_6));
            add(new ToastModel(R.drawable.toast_frame_style_7, R.string.style_7));
            add(new ToastModel(R.drawable.toast_frame_style_8, R.string.style_8));
            add(new ToastModel(R.drawable.toast_frame_style_9, R.string.style_9));
            add(new ToastModel(R.drawable.toast_frame_style_10, R.string.style_10));
            add(new ToastModel(R.drawable.toast_frame_style_11, R.string.style_11));
        }};

        return new ToastAdapter(
                getAppContext(),
                toastFrameStyle,
                onToastClick
        );

    }

    private final ToastAdapter.OnToastClick onToastClick = new ToastAdapter.OnToastClick() {
        @Override
        public void onToastClick(int position, @NonNull ToastModel item) {

            if (!AppUtils.hasStoragePermission()) {
                AppUtils.requestStoragePermission(requireContext());
                return;
            }

            if (position == 0) {
                Prefs.putInt(SELECTED_TOAST_FRAME, -1);
                OverlayUtil.disableOverlay("OxygenCustomizerComponentTSTFRM.overlay");
                Toast.makeText(getAppContext(), getAppContext().getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading dialog
            loadingDialog.show(getString(R.string.loading_dialog_wait));

            new Thread(() -> {
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                try {
                    hasErroredOut.set(
                            buildOverlay(
                                    "TSTFRM",
                                    position,
                                    SYSTEM_UI,
                                    true
                            )
                    );
                } catch (IOException e) {
                    hasErroredOut.set(true);
                    Log.e("ToastFrame", e.toString());
                }

                if (!hasErroredOut.get()) {
                    Prefs.putInt(SELECTED_TOAST_FRAME, position);
                    ToastAdapter ad = (ToastAdapter) binding.recyclerViewFragment.getAdapter();
                    ad.notifyChange();
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Hide loading dialog
                    loadingDialog.hide();

                    if (!hasErroredOut.get()) {
                        Toast.makeText(
                                getAppContext(),
                                getString(R.string.toast_applied),
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                getAppContext(),
                                getString(R.string.toast_error),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }, 3000);
            }).start();
        }
    };

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public String getTitle() {
        return getString(R.string.toast_styles);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }
}
