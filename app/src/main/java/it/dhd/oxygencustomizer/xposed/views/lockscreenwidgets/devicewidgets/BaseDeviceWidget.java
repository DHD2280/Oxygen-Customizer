package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.CallSuper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.res.ResourcesCompat;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.utils.json.WidgetConfig;
import it.dhd.oxygencustomizer.xposed.utils.ArcProgressWidget;
import it.dhd.oxygencustomizer.xposed.utils.QsTileTouchAnim;

public abstract class BaseDeviceWidget extends LinearLayout {

    protected boolean mAdded = false;
    protected PopupMenu popup;
    protected Context mContext;
    protected Context appContext;
    protected boolean mSettingsInterface;
    private OnWidgetClick mWidgetClickListener;
    private final QsTileTouchAnim mTouchAnim = new QsTileTouchAnim();

    protected WidgetMode mCurrentMode = WidgetMode.BIG;

    protected int mProgressColor, mTextColor = Color.WHITE;
    protected String mDeviceName = Build.MODEL;

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
            else if (!onWidgetTap() && mWidgetClickListener != null)
                mWidgetClickListener.onWidgetClick(this);
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
        popup.getMenu().add(0, -1, 0, appContext.getString(R.string.remove_widget));
        if (!getCustomSettings().isEmpty()) {
            popup.getMenu().add(0, 0, 1, appContext.getString(R.string.settings_label));
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
                getCustomSettings()
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

    public void setCustomDeviceName(String deviceName) {
        mDeviceName = deviceName;
        onSetDeviceName(mDeviceName);
    }

    abstract public void onSetCustomColors(int progressColor, int textColor);

    abstract public void onSetDeviceName(String deviceName);

    protected boolean onWidgetTap() {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mTouchAnim.doAction(event.getActionMasked(), this);
        return super.dispatchTouchEvent(event);
    }

    protected Drawable tintedIcon(int res) {
        Drawable drawable = ResourcesCompat.getDrawable(appContext.getResources(), res, appContext.getTheme());
        if (drawable == null) return null;
        drawable = drawable.mutate();
        drawable.setTint(mTextColor);
        return drawable;
    }

    protected Bitmap arcBitmap(int progress, int max, String text, Drawable icon) {
        int size = text.length() > 5 ? 28 : text.length() > 4 ? 32 : 40;
        return ArcProgressWidget.generateBitmap(mContext, Math.max(0, Math.min(progress, max)), Math.max(1, max), text, size,
                icon, false, 36, null, mProgressColor, mTextColor);
    }

    protected ImageView createArcView() {
        ImageView view = new ImageView(mContext);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setLayoutParams(new LayoutParams(dp2px(mContext, 60), dp2px(mContext, 60)));
        return view;
    }

}
