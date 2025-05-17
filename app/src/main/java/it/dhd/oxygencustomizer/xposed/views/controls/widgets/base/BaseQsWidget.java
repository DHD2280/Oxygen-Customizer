package it.dhd.oxygencustomizer.xposed.views.controls.widgets.base;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedFAB;
import it.dhd.oxygencustomizer.xposed.utils.SeparateQsWidgetsFactory;

/**
 * Base class for all QS widgets.
 * This MUST be used only in app UI,
 */
public abstract class BaseQsWidget extends LinearLayout implements BaseLaunchWidget {

    protected ImageView mImageView;
    protected ExtendedFAB mFab;
    protected Context mContext;
    protected ActivityLauncherUtils mActivityLauncherUtils;
    protected boolean mSettingsInterface;

    public BaseQsWidget(Context context, boolean settingsInterface) {
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
}

