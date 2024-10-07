package it.dhd.oxygencustomizer.ui.fragments.mods.misc.memc;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.io.InputStream;

import br.tiagohm.markdownview.css.InternalStyleSheet;
import br.tiagohm.markdownview.css.styles.Github;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentMemcInfoBinding;
import it.dhd.oxygencustomizer.utils.ThemeUtils;

public class MemcInfoFragment extends BottomSheetDialogFragment {

    private FragmentMemcInfoBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new BottomSheetDialog(requireContext(), getTheme());
        dialog.setOnShowListener(it -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) it;
            View parentLayout = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (parentLayout != null) {
                BottomSheetBehavior.from(parentLayout).setState(BottomSheetBehavior.STATE_EXPANDED);
                DisplayMetrics displaymetrics = new DisplayMetrics();
                requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int screenHeight = displaymetrics.heightPixels;
                BottomSheetBehavior.from(parentLayout).setPeekHeight(screenHeight, true);
                setupFullHeight(parentLayout);
            }
        });
        return dialog;
    }

    private void setupFullHeight(View bottomSheet) {
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        bottomSheet.setLayoutParams(layoutParams);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentMemcInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.memcInfoToolbar.setTitle(R.string.memc_help);

        InternalStyleSheet css = new Github();
        css.addRule("body, kbd", "background-color: " + intToHex(ThemeUtils.getAttrColor(requireContext(), R.attr.preferenceBackgroundColor)));
        css.addRule("body, p, h1, h2, h3, h4, h5, h6, span, div", "color: " + intToHex(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)));
        css.addRule("kbd", "border-color: " + intToHex(ThemeUtils.getAttrColor(requireContext(), R.attr.preferenceBackgroundColor)));
        css.addRule("kbd", "color: " + intToHex(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)));
        css.addRule("a", "color: " + intToHex(getColorFromAttribute(requireContext(), R.attr.colorPrimary)));
        binding.memcInfo.addStyleSheet(css);
        binding.memcInfo.loadMarkdown(LoadData("IrisConfig.md"));

    }

    public String intToHex(int colorValue) {
        return String.format("#%06X", (0xFFFFFF & colorValue));
    }

    public @ColorInt int getColorFromAttribute(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public String LoadData(String inFile) {
        String tContents = "";

        try {
            InputStream stream = getContext().getAssets().open(inFile);

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException e) {
            // Handle exceptions here
        }

        return tContents;

    }

}
