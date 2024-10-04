package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.utils.Constants.SELECTED_SETTINGS_ICONS_BG_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.SELECTED_SETTINGS_ICONS_BG_SHAPE;
import static it.dhd.oxygencustomizer.utils.Constants.SELECTED_SETTINGS_ICONS_BG_SOLID;
import static it.dhd.oxygencustomizer.utils.Constants.SELECTED_SETTINGS_ICONS_COLOR;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.dhd.oxygencustomizer.databinding.ViewListOptionSettingsiconsBinding;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.ThemeUtils;

public class SettingsIconOptions extends RecyclerView.Adapter<SettingsIconOptions.ViewHolder> {

    private final Context mContext;
    private final OnSettingsIconChange onSettingsIconChange;
    private ViewListOptionSettingsiconsBinding binding;
    private int bgColor, iconColor;
    private int bgShape;
    private boolean solidBg;
    private boolean hasBgColor = false, hasBgShape = false, hasBgSolid = false, hasIconColor = false;

    public SettingsIconOptions(Context context, OnSettingsIconChange onSettingsIconChange) {
        this.mContext = context;
        bgColor = ThemeUtils.getPrimaryColor(context);
        iconColor = ThemeUtils.getPrimaryColor(context);
        this.onSettingsIconChange = onSettingsIconChange;
        bgColor = Prefs.getInt(SELECTED_SETTINGS_ICONS_BG_COLOR, 0);
        iconColor = Prefs.getInt(SELECTED_SETTINGS_ICONS_COLOR, 0);
        bgShape = Prefs.getInt(SELECTED_SETTINGS_ICONS_BG_SHAPE, 0);
        solidBg = Prefs.getBoolean(SELECTED_SETTINGS_ICONS_BG_SOLID, false);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ViewListOptionSettingsiconsBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind();
    }

    /**
     * Set the options for the settings icon
     * @param hasBgColor The background color option
     * @param hasBgShape The background shape option
     * @param hasBgSolid The background solid option
     * @param hasIconColor The icon color option
     */
    public void setOptions(boolean hasBgColor, boolean hasBgShape, boolean hasBgSolid, boolean hasIconColor) {
        this.hasBgColor = hasBgColor;
        this.hasBgShape = hasBgShape;
        this.hasBgSolid = hasBgSolid;
        this.hasIconColor = hasIconColor;
        notifyItemChanged(0);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewListOptionSettingsiconsBinding binding;

        public ViewHolder(@NonNull ViewListOptionSettingsiconsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind() {

            if (hasBgColor) {
                binding.settingsBgColor.setVisibility(View.VISIBLE);
                binding.settingsBgColor.setSelectedValue(String.valueOf(bgColor));
                binding.settingsBgColor.setOnSelectedListener((entry, entryValue) -> {
                    bgColor = Integer.parseInt((String) entryValue);
                    Prefs.putInt(SELECTED_SETTINGS_ICONS_BG_COLOR, bgColor);
                    binding.settingsBgColor.setSelectedValue(String.valueOf(bgColor));
                    onSettingsIconChange.onBgColorChanged(bgColor);
                });
            } else {
                binding.settingsBgColor.setVisibility(View.GONE);
            }

            if (hasBgSolid) {
                binding.settingsBgSolid.setVisibility(View.VISIBLE);
                binding.settingsBgSolid.setSelectedValue(solidBg ? "1" : "0");
                binding.settingsBgSolid.setOnSelectedListener((entry, entryValue) -> {
                    solidBg = entryValue.equals("1");
                    Prefs.putBoolean(SELECTED_SETTINGS_ICONS_BG_SOLID, solidBg);
                    binding.settingsBgSolid.setSelectedValue(solidBg ? "1" : "0");
                    onSettingsIconChange.onSolidBgChanged(solidBg);
                });
            } else {
                binding.settingsBgSolid.setVisibility(View.GONE);
            }

            if (!hasBgSolid && !hasBgColor) {
                binding.settingsBgColor.setVisibility(View.GONE);
            } else {
                binding.settingsBgColor.setVisibility(View.VISIBLE);
            }

            if (hasBgShape) {
                binding.settingsBgShape.setVisibility(View.VISIBLE);
                binding.settingsBgShape.setSelectedValue(String.valueOf(bgShape));
                binding.settingsBgShape.setOnSelectedListener((entry, entryValue) -> {
                    bgShape = Integer.parseInt((String) entryValue);
                    Prefs.putInt(SELECTED_SETTINGS_ICONS_BG_SHAPE, bgShape);
                    binding.settingsBgShape.setSelectedValue(String.valueOf(bgShape));
                    onSettingsIconChange.onBgShapeChanged(bgShape);
                });
            } else {
                binding.settingsBgShape.setVisibility(View.GONE);
            }

            if (hasIconColor) {
                binding.settingsIconColor.setVisibility(View.VISIBLE);
                binding.settingsIconColor.setSelectedValue(String.valueOf(iconColor));
                binding.settingsIconColor.setOnSelectedListener((entry, entryValue) -> {
                    iconColor = Integer.parseInt((String) entryValue);
                    Prefs.putInt(SELECTED_SETTINGS_ICONS_COLOR, iconColor);
                    binding.settingsIconColor.setSelectedValue(String.valueOf(iconColor));
                    onSettingsIconChange.onIconColorChanged(iconColor);
                });
            } else {
                binding.settingsIconColor.setVisibility(View.GONE);
            }
        }
    }

    public interface OnSettingsIconChange {
        void onBgColorChanged(int color);
        void onBgShapeChanged(int shape);
        void onSolidBgChanged(boolean solidBg);
        void onIconColorChanged(int color);
    }

}
