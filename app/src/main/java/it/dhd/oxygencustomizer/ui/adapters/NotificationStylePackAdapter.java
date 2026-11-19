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
import it.dhd.oxygencustomizer.databinding.ViewNotificationStylePackBinding;
import it.dhd.oxygencustomizer.xposed.utils.NotificationStylePackUtil;

public class NotificationStylePackAdapter extends RecyclerView.Adapter<NotificationStylePackAdapter.ViewHolder> {

    public interface OnStyleClickListener {
        void onStyleClick(NotificationStylePackUtil.Style style);
    }

    private final List<NotificationStylePackUtil.Style> mStyles;
    private final OnStyleClickListener mListener;
    private String mSelectedKey;

    public NotificationStylePackAdapter(List<NotificationStylePackUtil.Style> styles, String selectedKey, OnStyleClickListener listener) {
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
        return new ViewHolder(ViewNotificationStylePackBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationStylePackUtil.Style style = mStyles.get(position);
        Context context = holder.itemView.getContext();
        boolean selected = style.getKey().equals(mSelectedKey);

        holder.binding.styleTitle.setText(style.label);
        holder.binding.styleTitle.setTextColor(ContextCompat.getColor(context, selected ? R.color.colorAccent : R.color.text_color_primary));
        holder.binding.styleDesc.setText(context.getString(R.string.notification_style_pack_desc, style.packLabel, style.author));
        holder.binding.styleSelected.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);

        Drawable preview = NotificationStylePackUtil.getPreview(context, style);
        holder.binding.stylePreview.setBackground(preview != null ? preview : ContextCompat.getDrawable(context, R.drawable.notif_default));

        holder.binding.styleContainer.setOnClickListener(v -> mListener.onStyleClick(style));
        holder.binding.styleContainer.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.binding.styleContainer, Gravity.END);
            popupMenu.getMenuInflater().inflate(R.menu.notification_style_item_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() != R.id.app_info) return false;
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", style.packageName, null));
                context.startActivity(intent);
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
        final ViewNotificationStylePackBinding binding;

        ViewHolder(ViewNotificationStylePackBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
