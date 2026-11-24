package it.dhd.oxygencustomizer.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ViewQsStylePackBinding;
import it.dhd.oxygencustomizer.xposed.utils.QsStylePackUtil;

public class QsStylePackAdapter extends RecyclerView.Adapter<QsStylePackAdapter.ViewHolder> {

    public interface OnStyleClickListener {
        void onStyleClick(QsStylePackUtil.Style style);
    }

    private final List<QsStylePackUtil.Style> mStyles;
    private final OnStyleClickListener mListener;
    private String mSelectedKey;

    public QsStylePackAdapter(List<QsStylePackUtil.Style> styles, String selectedKey, OnStyleClickListener listener) {
        mStyles = styles;
        mSelectedKey = selectedKey;
        mListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedKey(String key) {
        mSelectedKey = key;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ViewQsStylePackBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QsStylePackUtil.Style style = mStyles.get(position);
        Context context = holder.itemView.getContext();
        boolean selected = style.getKey().equals(mSelectedKey);

        holder.binding.styleTitle.setText(style.label);
        holder.binding.styleTitle.setTextColor(ContextCompat.getColor(context, selected ? R.color.colorAccent : R.color.text_color_primary));
        holder.binding.styleDesc.setText(context.getString(R.string.qs_style_pack_desc, style.packLabel, style.author));
        holder.binding.styleSelected.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);

        Drawable preview = QsStylePackUtil.getPreview(context, style);
        holder.binding.stylePreviewImage.setVisibility(preview != null ? View.VISIBLE : View.GONE);
        holder.binding.stylePreview.setVisibility(preview != null ? View.GONE : View.VISIBLE);
        if (preview != null) holder.binding.stylePreviewImage.setImageDrawable(preview);
        else holder.binding.stylePreview.setValues(style.values);

        holder.binding.styleContainer.setOnClickListener(v -> mListener.onStyleClick(style));
        holder.binding.styleContainer.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.binding.styleContainer, Gravity.END);
            popupMenu.getMenuInflater().inflate(R.menu.notification_style_item_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() != R.id.app_info) return false;
                context.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", style.packageName, null)));
                return true;
            });
            popupMenu.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mStyles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ViewQsStylePackBinding binding;

        ViewHolder(ViewQsStylePackBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
