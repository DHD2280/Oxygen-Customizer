package it.dhd.oxygencustomizer.xposed.views.controls.widgets.base;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedFAB;
import it.dhd.oxygencustomizer.xposed.utils.SeparateQsWidgetsFactory;
import it.dhd.oxygencustomizer.xposed.views.base.BaseQsStaticView;

/**
 * Base class for all QS widgets.
 * This MUST be used only in SystemUI. (OOS15 only)
 */
public abstract class BaseQsWidgetView extends BaseQsStaticView implements BaseLaunchWidget {
    
    protected ImageView mImageView;
    protected ExtendedFAB mFab;
    protected Context mContext;
    protected ActivityLauncherUtils mActivityLauncherUtils;
    protected boolean mSettingsInterface;

    public BaseQsWidgetView(Context context, boolean settingsInterface) {
        super(context);
        mContext = context;
        mSettingsInterface = settingsInterface;
        initBaseWidget();
    }

    private void initBaseWidget() {
        mImageView = SeparateQsWidgetsFactory.createImageView(mContext, mSettingsInterface);
        mFab = SeparateQsWidgetsFactory.createFAB(mContext, mSettingsInterface);

        configureViews();
        setupQsSpecifics();
    }

    private void configureViews() {
        mImageView.setImageDrawable(getWidgetImage());
        mFab.setIcon(getWidgetImage());
        mFab.setText(getWidgetName());

        if(!mSettingsInterface) {
            setOnClickListener(v -> onWidgetClick());
        }
    }

    private void setupQsSpecifics() {
        mActivityLauncherUtils = mSettingsInterface ? null : new ActivityLauncherUtils(
                mContext,
                ControllersProvider.getActivityStarterExternal()
        );
        addView(mImageView);
        addView(mFab);
        mFab.setVisibility(View.GONE);
    }

    @Override
    public void onSizeChanged(int newWidth) {
        mImageView.setVisibility(newWidth >= 2 ? View.GONE : View.VISIBLE);
        mFab.setVisibility(newWidth >= 2 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mImageView.setImageTintList(ColorStateList.valueOf(isNightMode() ? Color.WHITE : Color.BLACK));
        mFab.setIconTint(ColorStateList.valueOf(isNightMode() ? Color.WHITE : Color.BLACK));
        mFab.setTextColor(isNightMode() ? Color.WHITE : Color.BLACK);
    }

    private boolean isNightMode() {
        final Configuration config = mContext.getResources().getConfiguration();
        return (config.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

}

