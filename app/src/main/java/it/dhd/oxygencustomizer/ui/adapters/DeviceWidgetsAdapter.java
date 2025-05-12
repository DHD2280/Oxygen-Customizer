package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.DeviceWidgetPreviewsBinding;
import it.dhd.oxygencustomizer.xposed.utils.WidgetFactory;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.DeviceWidgetView;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BaseDeviceWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WidgetMode;

public class DeviceWidgetsAdapter extends RecyclerView.Adapter<DeviceWidgetsAdapter.ViewHolder> {

    public interface WidgetSettingsListener {
        void onWidgetSettingsClick(BaseDeviceWidget widget);
    }

    private final List<BaseDeviceWidget> mWidgets = new ArrayList<>();
    private final DeviceWidgetView mDeviceWidgetView;
    private final WidgetSettingsListener mListener;

    public DeviceWidgetsAdapter(List<BaseDeviceWidget> widgetsList, DeviceWidgetView deviceWidgetView, WidgetSettingsListener listener) {
        mWidgets.addAll(widgetsList);
        mDeviceWidgetView = deviceWidgetView;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DeviceWidgetPreviewsBinding container = DeviceWidgetPreviewsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(container, mDeviceWidgetView, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BaseDeviceWidget widget = mWidgets.get(position);
        boolean shouldDrawDivider = getItemCount() > 1 && position < getItemCount() - 1;
        holder.setDrawDivider(shouldDrawDivider);
        holder.bind(widget);
    }

    @Override
    public int getItemCount() {
        return mWidgets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements OplusRecyclerView.IOplusDividerDecorationInterface {

        private final DeviceWidgetPreviewsBinding binding;
        private final DeviceWidgetView deviceWidgetView;
        private final WidgetSettingsListener mListener;
        private final int mDividerDefaultHorizontalPadding = getAppContext().getResources().getDimensionPixelSize(R.dimen.preference_divider_default_horizontal_padding);
        private boolean drawDivider;

        public ViewHolder(@NonNull DeviceWidgetPreviewsBinding itemView, DeviceWidgetView dwv, WidgetSettingsListener listener) {
            super(itemView.getRoot());
            binding = itemView;
            deviceWidgetView = dwv;
            mListener = listener;
        }

        public void bind(BaseDeviceWidget widget) {
            binding.widgetTitle.setText(widget.getWidgetName());
            if (widget.hasBigMode()) {
                binding.previewBig.setVisibility(View.VISIBLE);
                binding.previewBig.addView(widget.getBigPreview());
                binding.previewBig.setOnClickListener(v -> addWidget(widget, WidgetMode.BIG));
            } else {
                binding.previewBig.setVisibility(View.GONE);
            }
            if (widget.hasSmallMode()) {
                binding.previewSmall.setVisibility(View.VISIBLE);
                binding.previewSmall.addView(widget.getSmallPreview());
                binding.previewSmall.setOnClickListener(v -> addWidget(widget, WidgetMode.SMALL));
            } else {
                binding.previewSmall.setVisibility(View.GONE);
            }
        }

        private void addWidget(BaseDeviceWidget widget, WidgetMode widgetMode) {
            BaseDeviceWidget dw = WidgetFactory.getNewWidget(binding.getRoot().getContext(), widget.getWidgetReference(), widgetMode, true);
            dw.setWidgetClickListener(new BaseDeviceWidget.OnWidgetClick() {
                @Override
                public void onWidgetClick(BaseDeviceWidget widget) {}

                @Override
                public void onWidgetRemove(BaseDeviceWidget widget) {
                    deviceWidgetView.removeWidget(widget);
                }

                @Override
                public void onSettingsClicked(BaseDeviceWidget widget) {
                    mListener.onWidgetSettingsClick(widget);
                }
            });

            if (!deviceWidgetView.addWidget(dw)) {
                Toast.makeText(binding.getRoot().getContext(), binding.getRoot().getContext().getString(R.string.remove_one_widget), Toast.LENGTH_LONG).show();
            }
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
            return this.binding.previewsContainer;
        }

        @Override
        public int getDividerEndInset() {
            return this.mDividerDefaultHorizontalPadding;
        }

        @Override
        public View getDividerStartAlignView() {
            return this.binding.previewsContainer;
        }

        @Override
        public int getDividerStartInset() {
            return this.mDividerDefaultHorizontalPadding;
        }

    }


}
