package it.dhd.oxygencustomizer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.dhd.oxygencustomizer.databinding.ViewListOptionsButtonsBinding;

public class ButtonsAdapter extends RecyclerView.Adapter<ButtonsAdapter.ViewHolder> {

    private OnButtonClick onButtonClick;
    private ViewListOptionsButtonsBinding binding;

    private boolean showEnable = true, showDisable = false;

    public ButtonsAdapter(OnButtonClick onButtonClick) {
        this.onButtonClick = onButtonClick;
    }

    @NonNull
    @Override
    public ButtonsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ViewListOptionsButtonsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ButtonsAdapter.ViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public void setButtons(boolean showEnable, boolean showDisable) {
        this.showEnable = showEnable;
        this.showDisable = showDisable;
        notifyItemChanged(0);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ViewListOptionsButtonsBinding binding;

        public ViewHolder(ViewListOptionsButtonsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind() {
            if (showEnable) {
                binding.enableSettingsIcons.setVisibility(View.VISIBLE);
                binding.enableSettingsIcons.setOnClickListener(v -> onButtonClick.onApplyClick());
            } else {
                binding.enableSettingsIcons.setVisibility(View.GONE);
            }

            if (showDisable) {
                binding.disableSettingsIcons.setVisibility(View.VISIBLE);
                binding.disableSettingsIcons.setOnClickListener(v -> onButtonClick.onDisableClick());
            } else {
                binding.disableSettingsIcons.setVisibility(View.GONE);
            }
        }
    }

    public interface OnButtonClick {
        void onApplyClick();
        void onDisableClick();
    }

}
