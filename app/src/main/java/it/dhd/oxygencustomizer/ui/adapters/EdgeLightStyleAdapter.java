package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_STYLE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.databinding.ViewListOptionsEdgeStyleBinding;
import it.dhd.oxygencustomizer.ui.fragments.mods.aod.edgelight.EdgeLightStyle;
import it.dhd.oxygencustomizer.ui.models.EdgeLightStyleModel;
import it.dhd.oxygencustomizer.utils.OCPreferences;

public class EdgeLightStyleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<EdgeLightStyleModel> mItems = new ArrayList<>();
    private final EdgeLightStyle.OnSettingsChangedListener mListener;
    private String mSelectedStyle;

    public EdgeLightStyleAdapter(List<EdgeLightStyleModel> items, EdgeLightStyle.OnSettingsChangedListener listener) {
        mItems.addAll(items);
        mListener = listener;
        mSelectedStyle = OCPreferences.getString(EDGE_LIGHT_STYLE, "0");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ViewListOptionsEdgeStyleBinding binding = ViewListOptionsEdgeStyleBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ViewHolder holder = (ViewHolder) viewHolder;
        EdgeLightStyleModel item = mItems.get(i);
        holder.binding.typeTitle.setText(item.getTitle());
        holder.binding.edgePreview.setBackgroundResource(item.getPreview());
        holder.binding.checkMark.setVisibility(mSelectedStyle.equals(item.getPrefValue()) ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> {
            OCPreferences.putString(EDGE_LIGHT_STYLE, item.getPrefValue());
            if (mListener != null) mListener.onSettingsChanged(Integer.parseInt(item.getPrefValue()));
            String oldStyle = mSelectedStyle;
            mSelectedStyle = item.getPrefValue();
            notifyItemChanged(findIndexByPrefValue(oldStyle));
            notifyItemChanged(findIndexByPrefValue(mSelectedStyle));
        });
    }

    private int findIndexByPrefValue(String prefValue) {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).getPrefValue().equals(prefValue)) {
                return i;
            }
        }
        return -1; // Ritorna -1 se non trovato
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ViewListOptionsEdgeStyleBinding binding;

        ViewHolder(@NonNull ViewListOptionsEdgeStyleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}
