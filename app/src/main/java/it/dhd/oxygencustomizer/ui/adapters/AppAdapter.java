package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.AppItemBinding;
import it.dhd.oxygencustomizer.ui.models.AppModel;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private final List<AppModel> itemList;
    private final List<AppModel> filteredApps;
    private static OnSwitchChange switchChangeListener;
    private static OnSliderChange sliderChangeListener;
    private String filterText = "";
    private boolean showSystem = false;
    private boolean hasSlider = false;

    public interface OnSwitchChange {
        void onSwitchChange(AppModel model, boolean isChecked);
    }

    public interface OnSliderChange {
        void onSliderChange(AppModel model, int progress);
    }

    public AppAdapter(List<AppModel> items, OnSwitchChange changeListener, OnSliderChange sliderListener) {
        items.sort(Comparator.comparing(AppModel::getAppName));
        this.itemList = items;
        this.filteredApps = new ArrayList<>(items);
        checkChange();
        switchChangeListener = changeListener;
        sliderChangeListener = sliderListener;
        hasSlider = true;
    }

    public AppAdapter(List<AppModel> items, OnSwitchChange changeListener) {
        items.sort(Comparator.comparing(AppModel::getAppName));
        this.itemList = items;
        this.filteredApps = new ArrayList<>(items);
        checkChange();
        switchChangeListener = changeListener;
        sliderChangeListener = null;
        hasSlider = false;
    }

    @NonNull
    @Override
    public AppAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppItemBinding binding = AppItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppAdapter.ViewHolder holder, int position) {
        AppModel model = filteredApps.get(holder.getBindingAdapterPosition());
        boolean shouldDrawDivider = getItemCount() > 1 && position < getItemCount() - 1;
        holder.setDrawDivider(shouldDrawDivider);
        holder.binding.appSlider.setVisibility(hasSlider && model.isEnabled() ? View.VISIBLE : View.GONE);
        holder.binding.appSlider.forcePosition("middle");
        if (position == 0) {
            holder.binding.appSwitch.forcePosition(filteredApps.size() == 1 ? "full" : "top");
        } else if (position == filteredApps.size() - 1) {
            holder.binding.appSwitch.forcePosition(hasSlider && model.isEnabled() ?
                    "middle" : "bottom");
            holder.binding.appSlider.forcePosition("bottom");
        } else {
            holder.binding.appSwitch.forcePosition("middle");
        }
        holder.binding.appSwitch.setTitle(model.getAppName());
        holder.binding.appSwitch.setSummary(model.getPackageName());
        holder.binding.appSwitch.setIcon(model.getAppIcon());

        holder.binding.appSwitch.setSwitchChangeListener((buttonView, isChecked) -> {
            if (hasSlider) {
                holder.binding.appSlider.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
            if (switchChangeListener != null && model.isEnabled() != isChecked) {
                switchChangeListener.onSwitchChange(model, isChecked);
                checkChange();
            }
            model.setEnabled(isChecked);
        });
        holder.binding.appSwitch.setSwitchChecked(model.isEnabled());
        holder.binding.appSlider.setOnSliderChangeListener((slider, progress, fromUser) -> {
            if (!fromUser) return;
            model.setDarkModeValue((int) progress);
            if (sliderChangeListener != null) {
                sliderChangeListener.onSliderChange(model, (int) progress);
            }
        });

        holder.binding.appSlider.setTitle(getAppContext().getString(R.string.dark_mode_intensity));
        holder.binding.appSlider.setSliderValue(model.getDarkModeValue());
    }

    public void showSystem(boolean show) {
        showSystem = show;
        filter(filterText);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void checkChange() {
        filteredApps.sort((app1, app2) -> {
            if (app1.isEnabled() == app2.isEnabled()) {
                return app1.getAppName().toLowerCase().compareTo(app2.getAppName().toLowerCase());
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
        for (AppModel app : itemList) {
            boolean matchesText = TextUtils.isEmpty(filterText) || app.getAppName().toLowerCase().contains(filterText.toLowerCase()) || app.getPackageName().toLowerCase().contains(filterText.toLowerCase());
            boolean matchesSystem = showSystem || !app.isSystemApp();

            if ((matchesText && matchesSystem) || app.isEnabled()) {
                filteredApps.add(app);
            }
        }
        checkChange();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements OplusRecyclerView.IOplusDividerDecorationInterface {

        private final AppItemBinding binding;
        private boolean drawDivider;
        private final int mDividerDefaultHorizontalPadding = getAppContext().getResources().getDimensionPixelSize(R.dimen.preference_divider_default_horizontal_padding);

        public ViewHolder(@NonNull AppItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setDrawDivider(boolean drawDivider) {
            this.drawDivider = drawDivider;
        }

        @Override
        public boolean drawDivider() {
            return drawDivider;
        }

        @Override
        public View getDividerEndAlignView() {
            return null;
        }

        @Override
        public int getDividerEndInset() {
            return this.mDividerDefaultHorizontalPadding;
        }

        @Override
        public View getDividerStartAlignView() {
            return this.binding.appSwitch.getTitleView();
        }

        @Override
        public int getDividerStartInset() {
            return this.mDividerDefaultHorizontalPadding;
        }

    }

}
