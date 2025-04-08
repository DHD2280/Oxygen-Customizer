package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.CallSuper;
import androidx.appcompat.widget.PopupMenu;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.utils.json.WidgetConfig;

public abstract class BaseDeviceWidget extends LinearLayout {

    protected boolean mAdded = false;
    protected PopupMenu popup;
    protected Context mContext;
    protected Context appContext;
    protected boolean mSettingsInterface;
    private OnWidgetClick mWidgetClickListener;

    protected WidgetMode mCurrentMode = WidgetMode.BIG;

    protected int mProgressColor, mTextColor = Color.WHITE;

    public interface OnWidgetClick {
        void onWidgetClick(BaseDeviceWidget widget);
        void onWidgetRemove(BaseDeviceWidget widget);
        void onSettingsClicked(BaseDeviceWidget widget);
    }

    public BaseDeviceWidget(Context context, boolean settingsInterface) {
        super(context);
        this.mSettingsInterface = settingsInterface;
        this.mContext = context;
        try {
            appContext = context.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {}
        setupMenuInternal();
        setOnClickListener(v -> {
            if (mSettingsInterface && mAdded) popup.show();
            else if (mWidgetClickListener != null) mWidgetClickListener.onWidgetClick(this);
        });
        mProgressColor = getPrimaryColor(mContext);
    }

    public void setSettingsInterface(boolean settingsInterface) {
        mSettingsInterface = settingsInterface;
    }

    public void setMode(WidgetMode mode) {
        mCurrentMode = mode;
        onWidgetModeChanged();
    }

    public WidgetMode getMode() {
        return mCurrentMode;
    }

    private void setupMenuInternal() {
        popup = new PopupMenu(getContext(), this, Gravity.END);
        popup.getMenu().add(0, -1, 0, "Remove");
        if (!getCustomSettings().isEmpty()) {
            popup.getMenu().add(0, 0, 1, "Settings");
        }

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == -1) { // Remove
                if (mWidgetClickListener != null) {
                    mWidgetClickListener.onWidgetRemove(this);
                }
                return true;
            } else if (item.getItemId() == 0) { // Settings
                if (mWidgetClickListener != null) {
                    mWidgetClickListener.onSettingsClicked(this);
                }
            }
            return false;
        });
    }

    @CallSuper
    public void onWidgetModeChanged() {
        setupMenuInternal();
    }

    abstract public String getWidgetReference();
    abstract public String getWidgetName();

    abstract public boolean hasBigMode();
    abstract public boolean hasSmallMode();

    abstract public View getBigPreview();
    abstract public View getSmallPreview();

    public String toJson() {
        Gson gson = new Gson();
        WidgetConfig config = new WidgetConfig(
                getWidgetReference(), // Widget Name internal
                this.getMode(), // Widget Mode
                getCustomSettings() // Metodo che ogni widget dovrà implementare
        );
        return gson.toJson(config);
    }

    public abstract List<SettingItem> getCustomSettings();
    public abstract void applySettings(Map<String, Object> settings);

    public PopupMenu getPopupMenu() {
        return popup;
    }

    public void setAdded(boolean added) {
        this.mAdded = added;
        setupMenuInternal();
    }

    public boolean isAdded() {
        return this.mAdded;
    }

    public void setWidgetClickListener(OnWidgetClick listener) {
        mWidgetClickListener = listener;
    }

    public void setCustomColors(int progressColor, int textColor) {
        mProgressColor = progressColor;
        mTextColor = textColor;
        onSetCustomColors(mProgressColor, mTextColor);
    }

    abstract public void onSetCustomColors(int progressColor, int textColor);

}
