package it.dhd.oxygencustomizer.ui.fragments.uistyle;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SELECTED_TOAST_FRAME;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentToastFrameBinding;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;
import it.dhd.oxygencustomizer.utils.overlay.compiler.OnDemandCompiler;

public class ToastFrame extends BaseFragment {

    private FragmentToastFrameBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToastFrameBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // Toast Frame style
        ArrayList<Object[]> toast_frame_style = new ArrayList<>();

        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_1, R.string.style_0});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_1, R.string.style_1});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_2, R.string.style_2});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_3, R.string.style_3});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_4, R.string.style_4});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_5, R.string.style_5});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_6, R.string.style_6});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_7, R.string.style_7});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_8, R.string.style_8});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_9, R.string.style_9});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_10, R.string.style_10});
        toast_frame_style.add(new Object[]{R.drawable.toast_frame_style_11, R.string.style_11});

        addItem(toast_frame_style);

        refreshBackground();

        return view;
    }

    // Function to add new item in list
    @SuppressLint("UseCompatLoadingForDrawables")
    private void addItem(ArrayList<Object[]> pack) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(requireContext()).inflate(R.layout.view_toast_frame, binding.toastFrameContainer, false);

            LinearLayout toast_container = list.findViewById(R.id.toast_container);
            toast_container.setBackground(ContextCompat.getDrawable(OxygenCustomizer.getAppContext(), (int) pack.get(i)[0]));

            TextView style_name = list.findViewById(R.id.style_name);
            style_name.setText(OxygenCustomizer.getAppContext().getString((int) pack.get(i)[1]));

            int finalI = i;
            list.setOnClickListener(v -> {
                if (finalI == 0) {
                    Prefs.putInt(SELECTED_TOAST_FRAME, -1);
                    OverlayUtil.disableOverlay("OxygenCustomizerComponentTSTFRM.overlay");
                    Toast.makeText(OxygenCustomizer.getAppContext(), OxygenCustomizer.getAppContext().getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!AppUtils.hasStoragePermission()) {
                    AppUtils.requestStoragePermission(requireContext());
                } else {
                    // Show loading dialog
                    loadingDialog.show(OxygenCustomizer.getAppContext().getResources().getString(R.string.loading_dialog_wait));

                    new Thread(() -> {
                        AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                        try {
                            hasErroredOut.set(OnDemandCompiler.buildOverlay("TSTFRM", finalI, SYSTEM_UI, true));
                        } catch (IOException e) {
                            hasErroredOut.set(true);
                            Log.e("ToastFrame", e.toString());
                        }

                        if (!hasErroredOut.get()) {
                            Prefs.putInt(SELECTED_TOAST_FRAME, finalI);
                            refreshBackground();
                        }

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            // Hide loading dialog
                            loadingDialog.hide();

                            if (!hasErroredOut.get()) {
                                Toast.makeText(OxygenCustomizer.getAppContext(), OxygenCustomizer.getAppContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(OxygenCustomizer.getAppContext(), OxygenCustomizer.getAppContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            }
                        }, 3000);
                    }).start();
                }
            });

            binding.toastFrameContainer.addView(list);
        }
    }

    // Function to check for bg drawable changes
    private void refreshBackground() {
        boolean selected = false;

        for (int i = 0; i < binding.toastFrameContainer.getChildCount(); i++) {
            LinearLayout child = binding.toastFrameContainer.getChildAt(i).findViewById(R.id.list_item_toast);
            TextView title = child.findViewById(R.id.style_name);
            if (i == Prefs.getInt(SELECTED_TOAST_FRAME, -1)) {
                selected = true;
                title.setTextColor(OxygenCustomizer.getAppContext().getResources().getColor(R.color.colorAccent, OxygenCustomizer.getAppContext().getTheme()));
            } else {
                title.setTextColor(ContextCompat.getColor(OxygenCustomizer.getAppContext(), R.color.text_color_secondary));
            }
        }

        if (!selected) {
            LinearLayout child = binding.toastFrameContainer.getChildAt(0).findViewById(R.id.list_item_toast);
            TextView title = child.findViewById(R.id.style_name);
            title.setTextColor(OxygenCustomizer.getAppContext().getResources().getColor(R.color.colorAccent, OxygenCustomizer.getAppContext().getTheme()));
        }
    }

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
