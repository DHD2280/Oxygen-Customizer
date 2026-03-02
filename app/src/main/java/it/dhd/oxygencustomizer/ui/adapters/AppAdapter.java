package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.dhd.oneplusui.appcompat.seekbar.OplusSeekBar;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.AppItemBinding;
import it.dhd.oxygencustomizer.databinding.AppItemMenuBinding;
import it.dhd.oxygencustomizer.ui.models.AppModel;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private final List<AppModel> itemList;
    private final List<AppModel> filteredApps;
    private static OnSwitchChange switchChangeListener;
    private static OnSliderChange sliderChangeListener;
    private static OnMenuChange menuChangeListener;
    private String filterText = "";
    private boolean showSystem = false;
    private boolean hasSlider = false;
    private boolean hasSwitch = true;
    private boolean hasMenu = false;
    private boolean tracking = false;

    public interface OnSwitchChange {
        void onSwitchChange(AppModel model, boolean isChecked);
    }

    public interface OnSliderChange {
        void onSliderChange(AppModel model, int progress);
    }

    public interface OnMenuChange {
        void onMenuChange(AppModel model, CharSequence value);
    }

    public AppAdapter(List<AppModel> items, OnSwitchChange changeListener, OnSliderChange sliderListener) {
        items.sort(Comparator.comparing(AppModel::getAppName));
        this.itemList = items;
        this.filteredApps = new ArrayList<>(items);
        checkChange();
        switchChangeListener = changeListener;
        sliderChangeListener = sliderListener;
        menuChangeListener = null;
        hasSlider = true;
        hasSwitch = true;
        hasMenu = false;
    }

    public AppAdapter(List<AppModel> items, OnSwitchChange changeListener) {
        items.sort(Comparator.comparing(AppModel::getAppName));
        this.itemList = items;
        this.filteredApps = new ArrayList<>(items);
        checkChange();
        switchChangeListener = changeListener;
        sliderChangeListener = null;
        menuChangeListener = null;
        hasSlider = false;
        hasSwitch = true;
        hasMenu = false;
    }

    public AppAdapter(List<AppModel> items, OnMenuChange changeListener) {
        items.sort(Comparator.comparing(AppModel::getAppName));
        this.itemList = items;
        this.filteredApps = new ArrayList<>(items);
        checkChange();
        menuChangeListener = changeListener;
        switchChangeListener = null;
        sliderChangeListener = null;
        hasSlider = false;
        hasSwitch = false;
        hasMenu = true;
    }

    @NonNull
    @Override
    public AppAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppItemBinding binding = AppItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        AppItemMenuBinding menuBinding = AppItemMenuBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        if (hasMenu)
            return new ViewHolder(menuBinding, menuBinding.appMenu.getTitleView(), menuBinding.appMenu.getTitleView());
        else return new ViewHolder(binding,
                binding.appSwitch.getIconView(),
                binding.appSwitch.getSwitchView());
    }

    @Override
    public void onBindViewHolder(@NonNull AppAdapter.ViewHolder holder, int position) {
        AppModel model = filteredApps.get(holder.getBindingAdapterPosition());
        boolean shouldDrawDivider = getItemCount() > 1 && position < getItemCount() - 1;
        holder.setDrawDivider(shouldDrawDivider);

        if (hasMenu) {
            if (position == 0) {
                holder.menuBinding.appMenu.forcePosition(filteredApps.size() == 1 ? "full" : "top");
            } else if (position == filteredApps.size() - 1) {
                holder.menuBinding.appMenu.forcePosition("bottom");
            } else {
                holder.menuBinding.appMenu.forcePosition("middle");
            }
            //
            holder.menuBinding.appMenu.setTitle(model.getAppName());
            holder.menuBinding.appMenu.setSummary(model.getPackageName());
            holder.menuBinding.appMenu.setIcon(model.getAppIcon());
            //
            holder.menuBinding.appMenu.setEntries(model.getEntries());
            holder.menuBinding.appMenu.setEntryValues(model.getEntryValues());
            holder.menuBinding.appMenu.setSelectedValue(model.getSelectedValue());
            holder.menuBinding.appMenu.setOnSelectedListener(
                    (entry, entryValue) -> {
                        model.setSelectedValue((String) entryValue);
                        menuChangeListener.onMenuChange(model, entryValue);
                    });
            return;
        }
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
        holder.binding.appSlider.setSliderValue(model.getDarkModeValue());
        holder.binding.appSlider.setOnSliderTouchListener(new OplusSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(OplusSeekBar oplusSeekbar, int progress, boolean fromUser) {
                Log.d("AppAdapter", "onProgressChanged: " + progress + " fromUser: " + fromUser + " tracking: " + tracking + " model: " + model.toString());
                if (!fromUser) return;
                model.setDarkModeValue(progress);
                if (sliderChangeListener != null) {
                    sliderChangeListener.onSliderChange(model, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(OplusSeekBar oplusSeekbar) {
                tracking = true;
            }

            @Override
            public void onStopTrackingTouch(OplusSeekBar oplusSeekbar) {
                tracking = false;
                Log.d("AppAdapter", "onStopTrackingTouch: " + " tracking: " + tracking + " model: " + model.toString());
                model.setDarkModeValue(oplusSeekbar.getProgress());
                if (sliderChangeListener != null) {
                    sliderChangeListener.onSliderChange(model, oplusSeekbar.getProgress());
                }
            }
        });

        holder.binding.appSlider.setTitle(getAppContext().getString(R.string.dark_mode_intensity));

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
        private final AppItemMenuBinding menuBinding;
        private final View mDividerStart, mDividerEnd;
        private boolean drawDivider;
        private final int mDividerDefaultHorizontalPadding = getAppContext().getResources().getDimensionPixelSize(R.dimen.preference_divider_default_horizontal_padding);

        public ViewHolder(@NonNull AppItemBinding binding, View dividerStart, View dividerEnd) {
            super(binding.getRoot());
            this.binding = binding;
            this.menuBinding = null;
            this.mDividerStart = dividerStart;
            this.mDividerEnd = dividerEnd;
        }

        public ViewHolder(@NonNull AppItemMenuBinding menuBinding, View dividerStart, View dividerEnd) {
            super(menuBinding.getRoot());
            this.binding = null;
            this.menuBinding = menuBinding;
            this.mDividerStart = dividerStart;
            this.mDividerEnd = dividerEnd;
        }

        public void setDrawDivider(boolean drawDivider) {
            this.drawDivider = drawDivider;
        }

        @Override
        public boolean drawDivider() {
            return drawDivider;
        }

        @Override
        public int getDividerEndInset() {
            return this.mDividerDefaultHorizontalPadding;
        }

        @Override
        public View getDividerStartAlignView() {
            return mDividerStart;
        }

        @Override
        public View getDividerEndAlignView() {
            return mDividerEnd;
        }

        @Override
        public int getDividerStartInset() {
            return this.mDividerDefaultHorizontalPadding;
        }

    }

}
