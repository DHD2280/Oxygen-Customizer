package it.dhd.oxygencustomizer.ui.adapters.iconpack;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.ui.adapters.iconpack.IconPackAdapter.VIEW_TYPE_BOTTOM;
import static it.dhd.oxygencustomizer.ui.adapters.iconpack.IconPackAdapter.VIEW_TYPE_MIDDLE;
import static it.dhd.oxygencustomizer.ui.adapters.iconpack.IconPackAdapter.VIEW_TYPE_SINGLE;
import static it.dhd.oxygencustomizer.ui.adapters.iconpack.IconPackAdapter.VIEW_TYPE_TOP;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import it.dhd.oneplusui.appcompat.cardlist.CardListSelectedItemLayout;
import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ViewItemIconPackCustomizationBinding;
import it.dhd.oxygencustomizer.xposed.utils.IconPackUtil;

public class IconPackCustomizationAdapter extends RecyclerView.Adapter<IconPackCustomizationAdapter.ViewHolder> {

    private final IconPackUtil.ResourceMapping mResourceMapping;
    private final List<String> mIconResNames;
    private final List<String> mFilteredIconResNames;
    private final IconPackUtil mPackUtils;
    private final ItemChangedListener mItemChangedListener;

    public IconPackCustomizationAdapter(IconPackUtil packUtil, IconPackUtil.ResourceMapping mapping, ItemChangedListener itemChangedListener) {
        mPackUtils = packUtil;
        mResourceMapping = mapping;
        mIconResNames = mResourceMapping.getOriginalResList();
        Collections.sort(mIconResNames);
        mFilteredIconResNames = new ArrayList<>(mIconResNames);
        mItemChangedListener = itemChangedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewItemIconPackCustomizationBinding binding = ViewItemIconPackCustomizationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, binding.title, binding.reset);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String resName = mFilteredIconResNames.get(position);
        boolean shouldDrawDivider = getItemCount() > 1 && position < getItemCount() - 1;
        holder.setDrawDivider(shouldDrawDivider);
        Drawable drawable;
        List<IconPackUtil.ReplacementIcon> replacementIcons = mResourceMapping.getReplacementIcons(resName);
        IconPackUtil.ReplacementIcon replacementIcon = mPackUtils.getEnabled(resName);
        if (replacementIcon == null) {
            drawable = ResourcesCompat.getDrawable(
                    holder.itemView.getResources(),
                    R.drawable.ic_icon_disabled,
                    holder.itemView.getContext().getTheme()
            );
        } else {
            drawable = replacementIcon.getDrawable();
        }

        setLayoutBackground(holder.itemView.findViewById(R.id.container), position);
        holder.bind(resName, replacementIcon, replacementIcons, drawable);
    }

    private void setLayoutBackground(CardListSelectedItemLayout view, int position) {
        Context context = view.getContext();

        LayerDrawable cardBackground = switch (getItemViewType(position)) {
            case VIEW_TYPE_TOP ->
                    (LayerDrawable) ResourcesCompat.getDrawable(view.getResources(), R.drawable.preference_background_top, context.getTheme());
            case VIEW_TYPE_MIDDLE ->
                    (LayerDrawable) ResourcesCompat.getDrawable(view.getResources(), R.drawable.preference_background_middle, context.getTheme());
            case VIEW_TYPE_BOTTOM ->
                    (LayerDrawable) ResourcesCompat.getDrawable(view.getResources(), R.drawable.preference_background_bottom, context.getTheme());
            default ->
                    (LayerDrawable) ResourcesCompat.getDrawable(view.getResources(), R.drawable.preference_background_center, context.getTheme());
        };

        view.setBackground(cardBackground);
    }

    @Override
    public int getItemCount() {
        return mFilteredIconResNames.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mFilteredIconResNames.size() == 1) {
            return VIEW_TYPE_SINGLE;
        } else if (position == 0) {
            return VIEW_TYPE_TOP;
        } else if (position == mFilteredIconResNames.size() - 1) {
            return VIEW_TYPE_BOTTOM;
        } else {
            return VIEW_TYPE_MIDDLE;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String text) {
        mFilteredIconResNames.clear();
        String filterText = text != null
                ? text
                : "";

        for (String resName : mIconResNames) {
            if (resName.toLowerCase().contains(filterText.toLowerCase())) {
                mFilteredIconResNames.add(resName);
            }
        }
        Collections.sort(mFilteredIconResNames);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements OplusRecyclerView.IOplusDividerDecorationInterface {

        private final ViewItemIconPackCustomizationBinding binding;
        private boolean drawDivider;
        private final View mDividerStart, mDividerEnd;
        private final int mDividerDefaultHorizontalPadding = getAppContext().getResources().getDimensionPixelSize(R.dimen.preference_divider_default_horizontal_padding);

        public ViewHolder(ViewItemIconPackCustomizationBinding itemView, View dividerStart, View dividerEnd) {
            super(itemView.getRoot());
            binding = itemView;
            mDividerStart = dividerStart;
            mDividerEnd = dividerEnd;
        }

        public void bind(String resName, IconPackUtil.ReplacementIcon replacementIcon, List<IconPackUtil.ReplacementIcon> replacementIcons, Drawable drawable) {

            binding.icon.setImageDrawable(drawable);
            binding.title.setText(resName.contains(":") ? resName.split(":")[1] : resName);
            if (replacementIcon != null && replacementIcon.isEnabled()) {
                binding.icon.setAlpha(1f);
                binding.title.setAlpha(1f);
                binding.desc.setAlpha(.7f);
                binding.reset.setVisibility(View.VISIBLE);
            } else {
                binding.icon.setAlpha(.5f);
                binding.title.setAlpha(.5f);
                binding.desc.setAlpha(.4f);
                binding.reset.setVisibility(View.INVISIBLE);
            }
            binding.desc.setText(replacementIcon != null && replacementIcon.isEnabled() ? replacementIcon.mIconPack.mName : OxygenCustomizer.get().getString(R.string.icon_pack_icon_disabled));

            List<IconDialogAdapter.ReplacementIcon> mReplacementIcons = new LinkedList<>();
            for (IconPackUtil.ReplacementIcon replacementIconFromPack : replacementIcons) {
                mReplacementIcons.add(
                        new IconDialogAdapter.ReplacementIcon(replacementIconFromPack.mIconPack.mPackageName, replacementIconFromPack.mIconPack.mName, resName, replacementIconFromPack.mReplacementRes, replacementIconFromPack.getDrawable()));
            }

            binding.container.setOnClickListener(v -> {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(v.getContext());
                builder.setTitle(R.string.select_replacement_icon);
                builder.setAdapter(new IconDialogAdapter(v.getContext(), mReplacementIcons), (dialog, which) -> {
                    /* On item click we enable the selected icon */
                    IconDialogAdapter.ReplacementIcon replacementIconChoice = mReplacementIcons.get(which);
                    mPackUtils.disable(resName);
                    IconPackUtil.ReplacementIcon newReplacement = mResourceMapping.getReplacementIcons(resName).stream()
                            .filter(icon -> icon.mReplacementRes.equals(replacementIconChoice.replacementName))
                            .findFirst()
                            .orElse(null);
                    mPackUtils.setEnabled(resName, newReplacement);
                    notifyItemChanged(getBindingAdapterPosition());
                    if (mItemChangedListener != null) {
                        mItemChangedListener.onItemChanged();
                    }
                });
                builder.show();
            });

            binding.reset.setOnClickListener(v -> {
                mPackUtils.disable(resName);
                notifyItemChanged(getBindingAdapterPosition());
                if (mItemChangedListener != null) {
                    mItemChangedListener.onItemChanged();
                }
            });
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
