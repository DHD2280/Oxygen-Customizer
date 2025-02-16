package it.dhd.oxygencustomizer.ui.fragments.mods.aod.edgelight;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentEdgeRecyclerBinding;
import it.dhd.oxygencustomizer.ui.adapters.EdgeLightStyleAdapter;
import it.dhd.oxygencustomizer.ui.models.EdgeLightStyleModel;

public class EdgeLightStyle extends Fragment {

    private final OnSettingsChangedListener mListener;
    private FragmentEdgeRecyclerBinding binding;
    private EdgeLightStyleAdapter mStyleAdapter;

    public interface OnSettingsChangedListener {
        void onSettingsChanged(int style);
    }

    public EdgeLightStyle(OnSettingsChangedListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEdgeRecyclerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        List<EdgeLightStyleModel> items = new ArrayList<>();

        String[] edgeLightStyles = getResources().getStringArray(R.array.edge_light_style_entries);
        String[] edgeLightStyleValues = getResources().getStringArray(R.array.edge_light_style_values);
        String[] edgeLightStylePreviews = getResources().getStringArray(R.array.edge_light_style_previews);

        for (int i = 0; i < edgeLightStyles.length; i++) {
            items.add(new EdgeLightStyleModel(edgeLightStyles[i], getResources().getIdentifier(edgeLightStylePreviews[i], "drawable", BuildConfig.APPLICATION_ID), edgeLightStyleValues[i]));
        }

        mStyleAdapter = new EdgeLightStyleAdapter(items, mListener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerViewFragment.setAdapter(mStyleAdapter);
        binding.recyclerViewFragment.setLayoutManager(layoutManager);

    }

}
