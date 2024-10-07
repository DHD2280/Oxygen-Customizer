package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ViewListOptionMemcItemBinding;
import it.dhd.oxygencustomizer.ui.models.MemcAppModel;

public class MemcAppAdapter extends RecyclerView.Adapter<MemcAppAdapter.ViewHolder> {

    private final List<MemcAppModel> itemList;
    private final List<MemcAppModel> filteredApps;
    private String filterText = "";
    private final OnItemClick mOnItemClick;

    public MemcAppAdapter(List<MemcAppModel> items, OnItemClick onItemClick) {
        items.sort(Comparator.comparing(MemcAppModel::getAppName));
        this.itemList = items;
        this.filteredApps = new ArrayList<>(items);
        this.mOnItemClick = onItemClick;
        checkChange();
    }

    public MemcAppAdapter(List<MemcAppModel> items, OnItemClick onItemClick, boolean activity) {
        items.sort(Comparator.comparing(MemcAppModel::getAppName));
        this.itemList = items;
        this.filteredApps = new ArrayList<>(items);
        this.mOnItemClick = onItemClick;
        checkChange();
    }

    public void addItem(MemcAppModel item) {
        itemList.add(item);
        notifyDataSetChanged();
        filter(filterText);
    }

    public void removeItem(MemcAppModel item) {
        itemList.remove(item);
        notifyDataSetChanged();
        filter(filterText);
    }

    @NonNull
    @Override
    public MemcAppAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewListOptionMemcItemBinding binding = ViewListOptionMemcItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MemcAppAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemcAppModel model = filteredApps.get(holder.getBindingAdapterPosition());
        holder.binding.appName.setText(model.getAppName());
        holder.binding.appPackage.setText(model.isActivity() ? model.getPackageName() + "\n" + model.getActivityName() : model.getPackageName());
        if (!model.isActivity()) {
            holder.binding.refreshRate.setVisibility(ViewGroup.VISIBLE);
            holder.binding.refreshRate.setText(String.format(getAppContext().getString(R.string.memc_refresh_rate), String.valueOf(model.getRefreshRate())));
        } else {
            holder.binding.refreshRate.setVisibility(ViewGroup.GONE);
        }
        holder.binding.memcConfig.setText(String.format(getAppContext().getString((R.string.memc_params)), model.getMemcConfig()));
        holder.binding.appIcon.setImageDrawable(model.getAppIcon());
        holder.binding.memcItem.setOnClickListener(v -> mOnItemClick.onItemClick(model));

    }

    @SuppressLint("NotifyDataSetChanged")
    private void checkChange() {
        filteredApps.sort((app1, app2) -> {
            if (app1.isEnabled() == app2.isEnabled()) {
                return app1.getAppName().compareTo(app2.getAppName());
            }
            return app1.isEnabled() ? -1 : 1;
        });
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredApps.size();
    }

    public void filter(String text) {
        filteredApps.clear();
        filterText = text != null ? text : "";
        for (MemcAppModel app : itemList) {
            boolean matchesText = TextUtils.isEmpty(filterText) || app.getAppName().toLowerCase().contains(filterText.toLowerCase()) || app.getPackageName().toLowerCase().contains(filterText.toLowerCase());

            if ((matchesText) || app.isEnabled()) {
                filteredApps.add(app);
            }
        }
        checkChange();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ViewListOptionMemcItemBinding binding;

        public ViewHolder(@NonNull ViewListOptionMemcItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClick {
        void onItemClick(MemcAppModel model);
    }

}