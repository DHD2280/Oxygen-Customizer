package it.dhd.oxygencustomizer.xposed.views.controls.customapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedFAB;
import it.dhd.oxygencustomizer.xposed.utils.SeparateQsWidgetsFactory;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

public class CustomAppBinder {

    private final Context mContext;
    private final String mPackageName;
    private final boolean mSettingsInterface;
    private final ImageView mImageView;
    private final ExtendedFAB mFab;

    public CustomAppBinder(Context context, String pkgName, boolean settingsInterface) {
        mContext = context;
        mPackageName = pkgName;
        mSettingsInterface = settingsInterface;
        mImageView = SeparateQsWidgetsFactory.createImageView(mContext, settingsInterface);
        mFab = SeparateQsWidgetsFactory.createFAB(mContext, settingsInterface);
        mFab.setTextColor(isNightMode() ? Color.WHITE : Color.BLACK);
        try {
            setupImage();
        } catch (Throwable t) {
            Log.e("CustomAppBinder", "Error setting up image", t);
        }
    }

    public void bindView(ViewGroup parent) {
        parent.addView(mImageView);
        parent.addView(mFab);
        mFab.setVisibility(View.GONE);
        if (!mSettingsInterface) {
            parent.setOnClickListener(v -> openApp());
        }
    }

    public void onSizeChanged(int newWidth) {
        mImageView.setVisibility(newWidth >= 2 ? View.GONE : View.VISIBLE);
        mFab.setVisibility(newWidth >= 2 ? View.VISIBLE : View.GONE);
    }

    private void setupImage() throws PackageManager.NameNotFoundException {
        Drawable appIcon =
                mSettingsInterface ?
                        AppUtils.getAppIcon(mContext, mPackageName) :
                        SystemUtils.PackageManager().getApplicationIcon(mPackageName);
        mImageView.setImageDrawable(appIcon);
        mFab.setIcon(
                DrawableConverter.scaleDrawable(mContext, appIcon, 0.3f));
        mFab.setText(mSettingsInterface ?
                AppUtils.getAppName(mContext, mPackageName) :
                SystemUtils.PackageManager().getApplicationLabel(SystemUtils.PackageManager().getApplicationInfo(mPackageName, PackageManager.GET_META_DATA)).toString());
    }

    private void openApp() {
        ActivityLauncherUtils mActivityLauncherUtils = new ActivityLauncherUtils(mContext, ControllersProvider.getActivityStarterExternal());
        mActivityLauncherUtils.launchApp(mPackageName, true);
    }

    private boolean isNightMode() {
        final Configuration config = mContext.getResources().getConfiguration();
        return (config.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    public void onConfigurationChanged() {
        mFab.setTextColor(isNightMode() ? Color.WHITE : Color.BLACK);
    }

}
