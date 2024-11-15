package it.dhd.oxygencustomizer.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.CreditsHeaderViewBinding;
import it.dhd.oxygencustomizer.databinding.CreditsItemViewBinding;
import it.dhd.oxygencustomizer.ui.models.CreditsModel;

public class CreditsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_ITEM = 1;

    private final List<CreditsModel> items;

    public CreditsAdapter(List<CreditsModel> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CreditsItemViewBinding bindingItem = CreditsItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        CreditsHeaderViewBinding bindingHeader = CreditsHeaderViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return switch (viewType) {
            case VIEW_TYPE_HEADER -> new HeaderViewHolder(bindingHeader);
            case VIEW_TYPE_ITEM -> new ItemViewHolder(bindingItem);
            default -> throw new IllegalArgumentException("Invalid view type");
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            CreditsModel model = items.get(position);
            ((ItemViewHolder) holder).bind(model);
            boolean shouldDrawDivider = position < items.size() - 1
                    && getItemViewType(position + 1) != VIEW_TYPE_HEADER;
            ((ItemViewHolder) holder).setDrawDivider(shouldDrawDivider);

            int backgroundResId;
            boolean isLastItem = position == items.size() - 1;
            if (position > 0 && getItemViewType(position - 1) == VIEW_TYPE_HEADER) {
                backgroundResId = R.drawable.preference_background_top;
            }
            else if (position > 0
                    && !isLastItem
                    && getItemViewType(position - 1) == VIEW_TYPE_HEADER
                    && getItemViewType(position + 1) == VIEW_TYPE_HEADER) {
                backgroundResId = R.drawable.preference_background_center;
            }
            else if (position > 1 && getItemViewType(position - 2) == VIEW_TYPE_HEADER) {
                if (!isLastItem && getItemViewType(position + 1) == VIEW_TYPE_ITEM) {
                    backgroundResId = R.drawable.preference_background_middle;
                } else {
                    backgroundResId = R.drawable.preference_background_bottom;
                }
            }
            else if (!isLastItem && getItemViewType(position + 1) == VIEW_TYPE_HEADER) {
                backgroundResId = R.drawable.preference_background_bottom;
            }
            else if (isLastItem) {
                backgroundResId = R.drawable.preference_background_bottom;
            }
            else {
                backgroundResId = R.drawable.preference_background_middle;
            }

            holder.itemView.setBackgroundResource(backgroundResId);
        } else if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            CreditsModel model = items.get(position);
            ((HeaderViewHolder) holder).bind(model);
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements OplusRecyclerView.IOplusDividerDecorationInterface {

        private final CreditsItemViewBinding binding;
        private boolean drawDivider = true;
        private int mDefaultDividerPadding;

        public ItemViewHolder(@NonNull CreditsItemViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CreditsModel model) {
            mDefaultDividerPadding = binding.getRoot().getContext().getResources().getDimensionPixelSize(R.dimen.preference_divider_default_horizontal_padding);
            binding.title.setText(model.getTitle());
            if (!TextUtils.isEmpty(model.getSummary())) {
                binding.desc.setVisibility(View.VISIBLE);
                binding.desc.setText(model.getSummary());
            } else
                binding.desc.setVisibility(View.GONE);
            if (model.getIcon() != 0) {
                binding.icon.setImageResource(model.getIcon());
            } else {
                Glide.with(binding.icon.getContext())
                        .load(model.getDrawable() != null ? model.getDrawable() : model.getOnlineIcon().replace("http://", "https://"))
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.ic_default_person)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.icon);
            }
            if (!TextUtils.isEmpty(model.getUrl())) {
                binding.listInfoItem.setOnClickListener(v -> v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(model.getUrl()))));
            } else {
                binding.listInfoItem.setOnClickListener(null);
            }
        }

        public void setDrawDivider(boolean drawDivider) {
            if (this.drawDivider != drawDivider) {
                this.drawDivider = drawDivider;
                binding.getRoot().invalidate();
            }
        }

        @Override
        public boolean drawDivider() {
            return drawDivider;
        }

        @Override
        public View getDividerStartAlignView() {
            return binding.title;
        }

        @Override
        public int getDividerEndInset() {
            return mDefaultDividerPadding;
        }

    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder implements OplusRecyclerView.IOplusDividerDecorationInterface {

        private final CreditsHeaderViewBinding binding;

        HeaderViewHolder(@NonNull CreditsHeaderViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CreditsModel creditsModel) {
            binding.headerText.setText(creditsModel.getTitle());
        }

        @Override
        public boolean drawDivider() {
            return false;
        }

    }
}
