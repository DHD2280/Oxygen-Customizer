package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_SWITCH;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.models.ClockModel;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.utils.ViewBindingHelpers;
import it.dhd.oxygencustomizer.utils.WallpaperUtil;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

public class ClockPreviewAdapter extends RecyclerView.Adapter<ClockPreviewAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<ClockModel> itemList;
    private static String prefSwitch;
    private final String prefStyle;
    private static Bitmap wallpaperBitmap;
    private LinearLayoutManager linearLayoutManager;

    public ClockPreviewAdapter(Context context, ArrayList<ClockModel> itemList, String prefSwitch, String prefStyle) {
        this.context = context;
        this.itemList = itemList;
        ClockPreviewAdapter.prefSwitch = prefSwitch;
        this.prefStyle = prefStyle;

        new WallpaperLoaderTask(this).execute();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                Objects.equals(prefSwitch, LOCKSCREEN_CLOCK_SWITCH) ||
                        Objects.equals(prefSwitch, AOD_CLOCK_SWITCH) ?
                        R.layout.view_clock_preview_lockscreen :
                        R.layout.view_clock_preview_header,
                parent,
                false
        );
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClockModel model = itemList.get(position);

        holder.bind(model, position);

        if (wallpaperBitmap != null) {
            ViewBindingHelpers.setBitmap(holder.wallpaperView, wallpaperBitmap);
        }

        ViewHelper.loadLottieAnimationView(
                context,
                null,
                holder.clockContainer,
                position
        );
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout container;
        private final TextView title;
        private final LinearLayout clockContainer;
        private final ImageView checkIcon;
        private final MaterialButton button;
        private final ShapeableImageView wallpaperView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.clock_preview_child);
            title = itemView.findViewById(R.id.clock_title);
            clockContainer = itemView.findViewById(R.id.clock_view_container);
            checkIcon = itemView.findViewById(R.id.icon_selected);
            button = itemView.findViewById(R.id.btn_select_style);
            wallpaperView = itemView.findViewById(R.id.wallpaper_view);
        }

        public void bind(ClockModel model, int position) {
            title.setText(model.getTitle());

            button.setOnClickListener(v -> {
                PreferenceHelper.instance.mPreferences.putInt(prefStyle, position);
                refreshLayout(this);
            });

            int adapterPosition = getBindingAdapterPosition();
            if (PreferenceHelper.instance.mPreferences.getInt(prefStyle, 0) != adapterPosition) {
                checkIcon.setVisibility(View.GONE);
                button.setEnabled(true);
            } else {
                checkIcon.setVisibility(View.VISIBLE);
                button.setEnabled(false);
            }

            clockContainer.removeAllViews();
            ViewStub viewStub = new ViewStub(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER_VERTICAL;
            viewStub.setLayoutParams(params);
            viewStub.setLayoutResource(model.getLayout());
            clockContainer.addView(viewStub);

            if (viewStub.getParent() != null) {
                viewStub.inflate();
            }

            boolean isSelected = adapterPosition == getBindingAdapterPosition();
            button.setEnabled(!isSelected);
            checkIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }
    }

    private void refreshLayout(ViewHolder holder) {
        int firstVisible = linearLayoutManager.findFirstVisibleItemPosition() - 1;
        int lastVisible = linearLayoutManager.findLastVisibleItemPosition() + 1;

        for (int i = firstVisible; i <= lastVisible; i++) {
            View view = linearLayoutManager.findViewByPosition(i);
            if (view == null) continue;

            LinearLayout child = view.findViewById(R.id.clock_preview_child);
            if (child == null) continue;

            boolean isSelected = view == holder.container;
            child.findViewById(R.id.btn_select_style).setEnabled(!isSelected);
            child.findViewById(R.id.icon_selected).setVisibility(!isSelected ?
                    View.GONE :
                    View.VISIBLE
            );
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        if (PreferenceHelper.instance.mPreferences.getInt(prefStyle, 0) != holder.getBindingAdapterPosition()) {
            holder.checkIcon.setVisibility(View.GONE);
            holder.button.setEnabled(true);
        } else {
            holder.checkIcon.setVisibility(View.VISIBLE);
            holder.button.setEnabled(false);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    private static class WallpaperLoaderTask extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<ClockPreviewAdapter> adapterRef;

        WallpaperLoaderTask(ClockPreviewAdapter adapter) {
            adapterRef = new WeakReference<>(adapter);
            wallpaperBitmap = null;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Context context = adapterRef.get().context;
            if (context == null) return null;
            return WallpaperUtil.getCompressedWallpaper(
                    context,
                    80,
                    Objects.equals(prefSwitch, LOCKSCREEN_CLOCK_SWITCH) ?
                            WallpaperManager.FLAG_LOCK :
                            WallpaperManager.FLAG_SYSTEM
            );
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ClockPreviewAdapter adapter = adapterRef.get();
            if (adapter != null && bitmap != null && Objects.equals(prefSwitch, LOCKSCREEN_CLOCK_SWITCH)) {
                wallpaperBitmap = bitmap;
                adapter.notifyDataSetChanged();
            }
        }
    }
}
