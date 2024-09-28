package it.dhd.oxygencustomizer.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.models.ThemeModel;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ViewHolder> {

    Context context;
    ArrayList<ThemeModel> itemList;
    LinearLayoutManager linearLayoutManager;
    LoadingDialog loadingDialog;
    int selectedItem = -1;
    int enabledItem = -1;

    public ThemeAdapter(Context context, ArrayList<ThemeModel> itemList, LoadingDialog loadingDialog) {
        this.context = context;
        this.itemList = itemList;
        this.loadingDialog = loadingDialog;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_list_option_iconpack, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.style_name.setText(itemList.get(position).getThemeName());
        holder.desc.setVisibility(View.GONE);
        holder.icon_preview.setVisibility(View.GONE);

        refreshButton(holder);

        enableOnClickListener(holder);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        itemSelected(holder.container, itemList.get(holder.getBindingAdapterPosition()).isEnabled());
        refreshButton(holder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    // Function for onClick events
    private void enableOnClickListener(ViewHolder holder) {
        // Set onClick operation for each item
        holder.container.setOnClickListener(v -> {
            selectedItem = selectedItem == holder.getBindingAdapterPosition() ? -1 : holder.getBindingAdapterPosition();
            refreshLayout(holder);

            if (!itemList.get(holder.getBindingAdapterPosition()).isEnabled()) {
                holder.btn_disable.setVisibility(View.GONE);
                if (holder.btn_enable.getVisibility() == View.VISIBLE)
                    holder.btn_enable.setVisibility(View.GONE);
                else holder.btn_enable.setVisibility(View.VISIBLE);
            } else {
                holder.btn_enable.setVisibility(View.GONE);
                if (holder.btn_disable.getVisibility() == View.VISIBLE)
                    holder.btn_disable.setVisibility(View.GONE);
                else holder.btn_disable.setVisibility(View.VISIBLE);
            }
        });

        // Set onClick operation for Enable button
        holder.btn_enable.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(context.getResources().getString(R.string.loading_dialog_wait));

            @SuppressLint("SetTextI18n") Runnable runnable = () -> {
                for (int i = 0; i <= itemList.size()-1; i++) {
                    if (itemList.get(i).isEnabled()) enabledItem = i;
                    itemList.get(i).setEnabled(i == holder.getBindingAdapterPosition());
                    OverlayUtil.disableOverlay(itemList.get(i).getPkgName());
                }
                OverlayUtil.enableOverlay(itemList.get(holder.getBindingAdapterPosition()).getPkgName());
                if (OverlayUtil.overlayExist("COMMONSUITH")) {
                    OverlayUtil.enableOverlay("OxygenCustomizerComponentCOMMONSUITH.overlay");
                }

                ((Activity) context).runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Hide loading dialog
                    loadingDialog.hide();

                    // Change button visibility
                    holder.btn_enable.setVisibility(View.GONE);
                    holder.btn_disable.setVisibility(View.VISIBLE);
                    refreshBackground(holder);
                    notifyItemChanged(enabledItem);
                    enabledItem = -1;

                    Toast.makeText(OxygenCustomizer.getAppContext(), context.getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                }, 3000));
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Set onClick operation for Disable button
        holder.btn_disable.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(context.getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                OverlayUtil.disableOverlay(itemList.get(holder.getBindingAdapterPosition()).getPkgName());
                if (OverlayUtil.overlayExist("COMMONSUITH")) {
                    OverlayUtil.disableOverlay("OxygenCustomizerComponentCOMMONSUITH.overlay");
                }

                ((Activity) context).runOnUiThread(() -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change button visibility
                        holder.btn_disable.setVisibility(View.GONE);
                        holder.btn_enable.setVisibility(View.VISIBLE);
                        refreshBackground(holder);

                        Toast.makeText(OxygenCustomizer.getAppContext(), context.getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                    }, 3000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });
    }

    // Function to check for layout changes
    private void refreshLayout(ViewHolder holder) {
        int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

        for (int i = firstVisible; i <= lastVisible; i++) {
            View view = linearLayoutManager.findViewByPosition(i);

            if (view != null) {
                LinearLayout child = view.findViewById(R.id.icon_pack_child);

                if (!(view == holder.container) && child != null) {
                    child.findViewById(R.id.enable_iconpack).setVisibility(View.GONE);
                    child.findViewById(R.id.disable_iconpack).setVisibility(View.GONE);
                }
            }
        }
    }

    // Function to check for applied options
    @SuppressLint("SetTextI18n")
    private void refreshBackground(ViewHolder holder) {
        int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

        for (int i = firstVisible; i <= lastVisible; i++) {
            View view = linearLayoutManager.findViewByPosition(i);

            if (view != null) {
                LinearLayout child = view.findViewById(R.id.icon_pack_child);

                if (child != null) {
                    itemSelected(child, i == holder.getAbsoluteAdapterPosition() && (itemList.get(i - (holder.getAbsoluteAdapterPosition() - holder.getBindingAdapterPosition())).isEnabled()));
                }
            }
        }
    }

    private void refreshButton(ViewHolder holder) {
        if (holder.getBindingAdapterPosition() != selectedItem) {
            holder.btn_enable.setVisibility(View.GONE);
            holder.btn_disable.setVisibility(View.GONE);
        } else {
            if (itemList.get(holder.getBindingAdapterPosition()).isEnabled()) {
                holder.btn_enable.setVisibility(View.GONE);
                holder.btn_disable.setVisibility(View.VISIBLE);
            } else {
                holder.btn_enable.setVisibility(View.VISIBLE);
                holder.btn_disable.setVisibility(View.GONE);
            }
        }
    }

    private void itemSelected(View parent, boolean state) {
        if (state) {
            ((TextView) parent.findViewById(R.id.iconpack_title)).setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            ((TextView) parent.findViewById(R.id.iconpack_desc)).setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            parent.findViewById(R.id.icon_selected).setVisibility(View.VISIBLE);
            parent.findViewById(R.id.iconpack_desc).setAlpha(0.8f);
        } else {
            ((TextView) parent.findViewById(R.id.iconpack_title)).setTextColor(ContextCompat.getColor(context, R.color.text_color_primary));
            ((TextView) parent.findViewById(R.id.iconpack_desc)).setTextColor(ContextCompat.getColor(context, R.color.text_color_secondary));
            parent.findViewById(R.id.icon_selected).setVisibility(View.INVISIBLE);
            parent.findViewById(R.id.iconpack_desc).setAlpha(1f);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout container;
        TextView style_name, desc;
        LinearLayout icon_preview;
        Button btn_enable, btn_disable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.icon_pack_child);
            desc = itemView.findViewById(R.id.iconpack_desc);
            style_name = itemView.findViewById(R.id.iconpack_title);
            icon_preview = itemView.findViewById(R.id.icon_preview);
            btn_enable = itemView.findViewById(R.id.enable_iconpack);
            btn_disable = itemView.findViewById(R.id.disable_iconpack);
        }
    }
}
