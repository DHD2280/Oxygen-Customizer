package it.dhd.oxygencustomizer.ui.fragments.mods.aod.edgelight;

import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_BLUR_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_BLUR_TYPE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_COLOR_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_DRAW_BLUR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_WIDTH;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentEdgeLightPreviewBinding;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightView;

public class EdgeLightPreviewFragment extends BaseFragment {

    private FragmentEdgeLightPreviewBinding binding;
    private EdgeLightView mEdgeLightView;

    private int mEdgeLightStyle = 0;
    private EdgeLightView.ColorMode mEdgeColorMode = EdgeLightView.ColorMode.ACCENT;
    private int mEdgeCustomColor = Color.RED;
    private float mEdgeWidth = 20f;
    private boolean drawBlur = false;
    private int blurType = 0, blurMode = 0;

    private final EdgeLightStyle.OnSettingsChangedListener mListener = new EdgeLightStyle.OnSettingsChangedListener() {
        @Override
        public void onSettingsChanged(int newStyle) {
            if (mEdgeLightView == null) return;
            mEdgeLightView.stopAnimation();
            mEdgeLightStyle = newStyle;
            mEdgeLightView.setOptions(mEdgeLightStyle, mEdgeWidth, mEdgeColorMode, mEdgeCustomColor, drawBlur, blurMode, blurType);
            mEdgeLightView.setPulsing(true, mEdgeLightView.PULSE_REASON_NOTIFICATION);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEdgeLightPreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        EdgeLightStyle mEdgeLightStyleFragment = new EdgeLightStyle(mListener);

        getChildFragmentManager().beginTransaction()
                .replace(binding.fragmentContainer.getId(), mEdgeLightStyleFragment)
                .commit();


        mEdgeColorMode = getColorMode(Integer.parseInt(OCPreferences.getString(EDGE_LIGHT_COLOR_MODE, "0")));
        mEdgeCustomColor = OCPreferences.getInt(EDGE_LIGHT_CUSTOM_COLOR, Color.RED);
        mEdgeWidth = OCPreferences.getSliderFloat(EDGE_LIGHT_WIDTH, 20f);
        drawBlur = OCPreferences.getBoolean(EDGE_LIGHT_DRAW_BLUR, false);
        blurType = Integer.parseInt(OCPreferences.getString(EDGE_LIGHT_BLUR_TYPE, "0"));
        blurMode = Integer.parseInt(OCPreferences.getString(EDGE_LIGHT_BLUR_MODE, "0"));
        mEdgeLightView = new EdgeLightView(requireContext(), true);
        mEdgeLightView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        binding.edgeLightParent.addView(mEdgeLightView);
        mEdgeLightView.setScreenRadius((float) dp2px(28, requireContext()));
        mEdgeLightView.setOptions(mEdgeLightStyle, mEdgeWidth, mEdgeColorMode, mEdgeCustomColor, drawBlur, blurMode, blurType);

    }

    private EdgeLightView.ColorMode getColorMode(int colorMode) {
        for (EdgeLightView.ColorMode mode : EdgeLightView.ColorMode.values()) {
            if (mode.ordinal() == colorMode) {
                return mode;
            }
        }
        return EdgeLightView.ColorMode.ACCENT;
    }

    @Override
    public String getTitle() {
        return getString(R.string.edge_light_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEdgeLightView != null) {
            mEdgeLightView.stopAnimation();
            mEdgeLightView.hide();
        }
    }

}
