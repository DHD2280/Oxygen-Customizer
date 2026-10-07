package it.dhd.oxygencustomizer.ui.adapters.iconpack;

import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import it.dhd.oxygencustomizer.databinding.ViewItemIconBinding;
import it.dhd.oxygencustomizer.xposed.utils.IconPackUtil;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.ViewHolder> {

    private final IconPackUtil mIconPackUtil;
    private final List<Pair<String, Drawable>> mIcons = new ArrayList<>();

    public IconAdapter(IconPackUtil iconPackUtil, IconPackUtil.IconPack iconPack) {
        mIconPackUtil = iconPackUtil;
        mIcons.addAll(getDrawablesFromIconPack(iconPack));
    }

    public List<Pair<String, Drawable>> getDrawablesFromIconPack(IconPackUtil.IconPack iconPack) {
        List<Pair<String, Drawable>> drawables = new ArrayList<>();

        HashMap<String, ArrayList<IconPackUtil.ReplacementIcon>> resourcesMap = mIconPackUtil.mIconPackMapping.get(iconPack);

        if (resourcesMap != null) {
            for (ArrayList<IconPackUtil.ReplacementIcon> replacementIcons : resourcesMap.values()) {
                for (IconPackUtil.ReplacementIcon icon : replacementIcons) {
                    Drawable drawable = icon.getDrawable();
                    if (drawable != null) {
                        Pair<String, Drawable> pair = new Pair<>(icon.mReplacementRes, drawable);
                        drawables.add(pair);
                    }
                }
            }
        }

        drawables.sort(Comparator.comparing(p -> p.first));

        return drawables;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewItemIconBinding binding = ViewItemIconBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.text.setText(mIcons.get(position).first);
        holder.binding.text.setVisibility(View.GONE); // only for debugging purposes
        holder.binding.icon.setImageDrawable(mIcons.get(position).second);
    }

    @Override
    public int getItemCount() {
        return mIcons.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ViewItemIconBinding binding;

        public ViewHolder(ViewItemIconBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
