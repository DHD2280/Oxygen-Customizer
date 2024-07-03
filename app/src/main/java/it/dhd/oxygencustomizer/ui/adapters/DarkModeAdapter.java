package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;

import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.dhd.oxygencustomizer.databinding.DarkModeItemBinding;
import it.dhd.oxygencustomizer.ui.models.AppModel;

public class DarkModeAdapter extends RecyclerView.Adapter<DarkModeAdapter.ViewHolder> {

    private final List<AppModel> itemList;
    private final List<AppModel> filteredApps;
    private static OnSwitchChange switchChangeListener;
    private static OnSliderChange sliderChangeListener;

    public interface OnSwitchChange {
        void onSwitchChange(AppModel model, boolean isChecked);
    }

    public interface OnSliderChange {
        void onSliderChange(AppModel model, int progress);
    }

    public DarkModeAdapter(List<AppModel> items, OnSwitchChange changeListener, OnSliderChange sliderListener) {
        items.sort(Comparator.comparing(AppModel::getAppName));
        this.itemList = items;
        this.filteredApps = new ArrayList<>(items);
        checkChange();
        switchChangeListener = changeListener;
        sliderChangeListener = sliderListener;
    }

    @NonNull
    @Override
    public DarkModeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DarkModeItemBinding binding = DarkModeItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DarkModeAdapter.ViewHolder holder, int position) {
        AppModel model = filteredApps.get(holder.getBindingAdapterPosition());
        holder.binding.darkModeSwitch.setTitle(model.getAppName());
        holder.binding.darkModeSwitch.setSummary(model.getPackageName());
        holder.binding.darkModeSwitch.setImageDimensions(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getAppContext().getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getAppContext().getResources().getDisplayMetrics()));
        holder.binding.darkModeSwitch.setImageMargin(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9, getAppContext().getResources().getDisplayMetrics())
        );
        holder.binding.darkModeSwitch.setIcon(model.getAppIcon());
        holder.binding.darkModeSwitch.setSwitchChangeListener((buttonView, isChecked) -> {
            Log.d("DarkModeAdapter", "setSwitchChangeListener: App " + model.getPackageName() + " " + isChecked);
            holder.binding.darkIntensitySlider.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (switchChangeListener != null && model.isEnabled() != isChecked) {
                switchChangeListener.onSwitchChange(model, isChecked);
                checkChange();
            }
            model.setEnabled(isChecked);
        });
        holder.binding.darkModeSwitch.setSwitchChecked(model.isEnabled());
        holder.binding.darkIntensitySlider.setOnSliderChangeListener((slider, progress, fromUser) -> {
            Log.d("DarkModeAdapter", "setOnSliderChangeListener: App " + model.getPackageName() + " Int: " + progress + " " + fromUser);
            if (!fromUser) return;
            model.setDarkModeValue((int) progress);
            if (sliderChangeListener != null) {
                sliderChangeListener.onSliderChange(model, (int) progress);
            }
        });
        holder.binding.darkIntensitySlider.setSliderValue(model.getDarkModeValue());
        holder.binding.darkIntensitySlider.setVisibility(model.isEnabled() ? View.VISIBLE : View.GONE);
    }

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
        if (TextUtils.isEmpty(text)) {
            filteredApps.addAll(itemList);
        } else {
            text = text.toLowerCase();
            for (AppModel item : itemList) {
                if (item.getAppName().toLowerCase().contains(text) ||
                    item.getPackageName().contains(text)) {
                    filteredApps.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private DarkModeItemBinding binding;

        public ViewHolder(@NonNull DarkModeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
