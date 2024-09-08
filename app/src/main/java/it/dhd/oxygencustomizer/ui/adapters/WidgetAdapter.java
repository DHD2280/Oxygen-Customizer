package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.utils.PreferenceHelper.getModulePrefs;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ApplistPreferenceIconBinding;
import it.dhd.oxygencustomizer.databinding.WidgetItemBinding;
import it.dhd.oxygencustomizer.utils.Prefs;

public class WidgetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_WIDGETS = 1;

    private Context context;
    private Map<String, List<AppWidgetProviderInfo>> widgetsByApp;
    private AppWidgetHost appWidgetHost;
    private AppWidgetManager appWidgetManager;
    private FrameLayout widgetContainer;

    public WidgetAdapter(Context context, Map<String, List<AppWidgetProviderInfo>> widgetsByApp,
                         AppWidgetHost appWidgetHost, AppWidgetManager appWidgetManager, FrameLayout widgetContainer) {
        this.context = context;
        this.widgetsByApp = widgetsByApp;
        this.appWidgetHost = appWidgetHost;
        this.appWidgetManager = appWidgetManager;
        this.widgetContainer = widgetContainer;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2 == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_WIDGETS;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ApplistPreferenceIconBinding appHeaderBinding = ApplistPreferenceIconBinding.inflate(inflater, parent, false);
        WidgetItemBinding widgetBinding = WidgetItemBinding.inflate(inflater, parent, false);
        if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(appHeaderBinding);
        } else {
            return new WidgetsViewHolder(widgetBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int appPosition = position / 2;
        String packageName = (String) widgetsByApp.keySet().toArray()[appPosition];
        if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            ((HeaderViewHolder) holder).bind(packageName);
        } else {
            ((WidgetsViewHolder) holder).bind(widgetsByApp.get(packageName));
        }
    }

    @Override
    public int getItemCount() {
        return widgetsByApp.size() * 2;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        private ApplistPreferenceIconBinding binding;

        HeaderViewHolder(@NonNull ApplistPreferenceIconBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String packageName) {
            try {
                PackageManager pm = context.getPackageManager();
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                Drawable icon = pm.getApplicationIcon(appInfo);
                String name = pm.getApplicationLabel(appInfo).toString();

                binding.icon.setImageDrawable(icon);
                binding.title.setText(name);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private class WidgetsViewHolder extends RecyclerView.ViewHolder {
        private WidgetItemBinding binding;

        WidgetsViewHolder(@NonNull WidgetItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(List<AppWidgetProviderInfo> widgetList) {
            widgetContainer.removeAllViews();
            for (AppWidgetProviderInfo widgetInfo : widgetList) {

                ImageView widgetPreview = new ImageView(context);
                widgetPreview.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                PackageManager pm = context.getPackageManager();
                Drawable previewDrawable = null;
                if (widgetInfo.previewImage != 0) {
                    previewDrawable = pm.getDrawable(widgetInfo.provider.getPackageName(), widgetInfo.previewImage, null);
                } else if (widgetInfo.icon != 0) {
                    previewDrawable = pm.getDrawable(widgetInfo.provider.getPackageName(), widgetInfo.icon, null);
                }
                if (previewDrawable != null) {
                    widgetPreview.setImageDrawable(previewDrawable);
                }

                widgetPreview.setOnClickListener(v -> addWidgetToView(widgetInfo));
                widgetContainer.addView(widgetPreview);
            }
        }
    }

    private void addWidgetToView(AppWidgetProviderInfo widgetInfo) {
        int appWidgetId = appWidgetHost.allocateAppWidgetId();
        if (appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, widgetInfo.provider)) {
            AppWidgetHostView hostView = appWidgetHost.createView(context, appWidgetId, widgetInfo);
            hostView.setAppWidget(appWidgetId, widgetInfo);
            //widgetContainer.removeAllViews();
            //widgetContainer.addView(hostView);
        }
        saveWidgetInfo(appWidgetId, widgetInfo);
    }

    private void saveWidgetInfo(int appWidgetId, AppWidgetProviderInfo widgetInfo) {
        getModulePrefs().edit().putInt("widgetId", appWidgetId).apply();
        getModulePrefs().edit().putString("widgetProvider", widgetInfo.provider.flattenToString()).apply();
    }
}
