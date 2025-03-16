package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.getColor;
import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.saveColor;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ViewListDarkShadowColorsBinding;
import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.ui.models.DarkShadowItem;
import it.dhd.oxygencustomizer.utils.overlay.FabricatedUtil;

public class DarkShadowColorsAdapter extends RecyclerView.Adapter<DarkShadowColorsAdapter.ViewHolder> {

    private final MainActivity mActivity;
    private final List<DarkShadowItem> mDarkShadowColors;
    private final OnUserAction mListener;

    public interface OnUserAction {
        void onColorChanged(DarkShadowItem darkShadowItem);
        void onEnabledClicked(DarkShadowItem darkShadowItem);
        void onDisabledClicked(DarkShadowItem darkShadowItem);
    }

    public DarkShadowColorsAdapter(MainActivity activity, List<DarkShadowItem> colors, OnUserAction listener) {
        mActivity = activity;
        mDarkShadowColors = colors;
        mListener = listener;
    }

    @NonNull
    @Override
    public DarkShadowColorsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewListDarkShadowColorsBinding binding = ViewListDarkShadowColorsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DarkShadowItem model = mDarkShadowColors.get(holder.getBindingAdapterPosition());
        boolean shouldDrawDivider = getItemCount() > 1 && position < getItemCount() - 1;
        holder.setDrawDivider(shouldDrawDivider);
        holder.binding.colorPicker.forcePosition("middle");
        if (position == 0) {
            holder.binding.colorPicker.forcePosition(mDarkShadowColors.size() == 1 ? "full" : "top");
        } else if (position == mDarkShadowColors.size() - 1) {
            holder.binding.colorPicker.forcePosition("bottom");
        } else {
            holder.binding.colorPicker.forcePosition("middle");
        }
        holder.binding.colorPicker.setTitle(model.getTitle());
        if (FabricatedUtil.isOverlayEnabled(model.getOverlayName() + "_0")) {
            holder.binding.colorPicker.setDisableButtonEnabled(true);
        }
        holder.binding.colorPicker.setOnEnableClickListener(v -> mListener.onEnabledClicked(model));
        holder.binding.colorPicker.setOnDisableClickListener(v -> mListener.onDisabledClicked(model));
        holder.binding.colorPicker.setColorPickerListener(
                mActivity,
                getColor(model),
                true,
                true,
                true
        );
        holder.binding.colorPicker.setPreviewColor(getColor(model));
        holder.binding.colorPicker.setOnColorSelectedListener(color -> {
            Log.w("DarkShadowColorsAdapter", "onColorSelected: " + model.getTitle() + " " + color);
            model.setColor(color);
            mListener.onColorChanged(model);
            holder.binding.colorPicker.setDisableButtonEnabled(false);
            holder.binding.colorPicker.setEnableButtonEnabled(true);
        });
    }

    @Override
    public int getItemCount() {
        return mDarkShadowColors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements OplusRecyclerView.IOplusDividerDecorationInterface {

        private final ViewListDarkShadowColorsBinding binding;
        private boolean drawDivider;
        private final int mDividerDefaultHorizontalPadding = getAppContext().getResources().getDimensionPixelSize(R.dimen.preference_divider_default_horizontal_padding);

        public ViewHolder(@NonNull ViewListDarkShadowColorsBinding binding) {
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
            return this.binding.colorPicker.getTitleView();
        }

        @Override
        public int getDividerStartInset() {
            return this.mDividerDefaultHorizontalPadding;
        }

    }

}
