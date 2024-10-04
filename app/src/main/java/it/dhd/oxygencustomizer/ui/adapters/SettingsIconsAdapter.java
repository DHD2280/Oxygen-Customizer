package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.utils.Constants.SELECTED_SETTINGS_ICONS_SET;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ViewListOptionIconpackBinding;
import it.dhd.oxygencustomizer.ui.models.SettingsIconModel;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

public class SettingsIconsAdapter extends RecyclerView.Adapter<SettingsIconsAdapter.ViewHolder> {

    private final ArrayList<SettingsIconModel> settingsIcons;
    private ViewListOptionIconpackBinding binding;
    private Context mContext;
    private OnSettingsIconClick onSettingsIconClick;
    private int selectedItem = -1;
    private int mEnabledPack;

    public SettingsIconsAdapter(Context context, ArrayList<SettingsIconModel> settingsIcons, OnSettingsIconClick onSettingsIconClick) {
        this.mContext = context;
        this.settingsIcons = settingsIcons;
        this.onSettingsIconClick = onSettingsIconClick;
        mEnabledPack = Prefs.getInt(SELECTED_SETTINGS_ICONS_SET, -1);
    }

    @NonNull
    @Override
    public SettingsIconsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ViewListOptionIconpackBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsIconsAdapter.ViewHolder holder, int position) {
        SettingsIconModel settingsIcon = settingsIcons.get(position);
        holder.bind(settingsIcon, position);
    }

    @Override
    public int getItemCount() {
        return settingsIcons.size();
    }

    public void setSelectedPack(int i) {
        int oldEnabled = mEnabledPack;
        mEnabledPack = i;
        if (oldEnabled != -1) {
            notifyItemChanged(oldEnabled);
        }
        notifyItemChanged(mEnabledPack);
    }

    public interface OnSettingsIconClick {
        void onSettingsIconClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewListOptionIconpackBinding binding;

        public ViewHolder(@NonNull ViewListOptionIconpackBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SettingsIconModel settingsIcon, int position) {
            binding.iconpackTitle.setText(settingsIcon.getName());

            binding.iconSelected.setVisibility(
                    (OverlayUtil.isOverlayEnabled("OxygenCustomizerComponentSIP1.overlay") &&
                            position == Prefs.getInt(SELECTED_SETTINGS_ICONS_SET, 0) ) ? View.VISIBLE : View.INVISIBLE
            );

            if (position == mEnabledPack) {
                binding.iconSelected.setVisibility(View.VISIBLE);
            } else {
                binding.iconSelected.setVisibility(View.INVISIBLE);
            }

            if (position == selectedItem) {
                binding.iconpackTitle.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                binding.iconpackDesc.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            } else {
                binding.iconpackTitle.setTextColor(ContextCompat.getColor(mContext, R.color.text_color_primary));
                binding.iconpackDesc.setTextColor(ContextCompat.getColor(mContext, R.color.text_color_secondary));
            }

            if (settingsIcon.getDesc() != 0) {
                binding.iconpackDesc.setVisibility(View.VISIBLE);
                binding.iconpackDesc.setText(settingsIcon.getDesc());
            } else {
                binding.iconpackDesc.setVisibility(View.GONE);
            }

            if (settingsIcon.getDrawableIcon1() != null) {
                binding.iconpackPreview1.setVisibility(View.VISIBLE);
                binding.iconpackPreview1.setImageDrawable(settingsIcon.getDrawableIcon1());
            } else {
                binding.iconpackPreview1.setVisibility(View.GONE);
            }

            if (settingsIcon.getDrawableIcon2() != null) {
                binding.iconpackPreview2.setVisibility(View.VISIBLE);
                binding.iconpackPreview2.setImageDrawable(settingsIcon.getDrawableIcon2());
            } else {
                binding.iconpackPreview2.setVisibility(View.GONE);
            }

            if (settingsIcon.getDrawableIcon3() != null) {
                binding.iconpackPreview3.setVisibility(View.VISIBLE);
                binding.iconpackPreview3.setImageDrawable(settingsIcon.getDrawableIcon3());
            } else {
                binding.iconpackPreview3.setVisibility(View.GONE);
            }

            if (settingsIcon.getDrawableIcon4() != null) {
                binding.iconpackPreview4.setVisibility(View.VISIBLE);
                binding.iconpackPreview4.setImageDrawable(settingsIcon.getDrawableIcon4());
            } else {
                binding.iconpackPreview4.setVisibility(View.GONE);
            }

            binding.iconPackChild.setOnClickListener(v -> {
                int oldSelected = selectedItem;
                selectedItem = position;
                notifyItemChanged(oldSelected);
                notifyItemChanged(selectedItem);
                if (onSettingsIconClick != null) {
                    onSettingsIconClick.onSettingsIconClick(position);
                }
            });
        }

    }

}
