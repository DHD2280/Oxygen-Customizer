package it.dhd.oxygencustomizer.xposed.utils;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_PHOTO_RADIUS;
import static it.dhd.oxygencustomizer.xposed.utils.QsTileHelper.getShapeForHighlightTile;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.xposed.views.controls.customapp.QsCustomAppWidget;
import it.dhd.oxygencustomizer.xposed.views.controls.customapp.QsCustomAppWidgetView;
import it.dhd.oxygencustomizer.xposed.views.controls.devicewidget.DeviceWidgetHolder;
import it.dhd.oxygencustomizer.xposed.views.controls.devicewidget.DeviceWidgetHolderView;
import it.dhd.oxygencustomizer.xposed.views.controls.devicewidget.DeviceWidgetInterface;
import it.dhd.oxygencustomizer.xposed.views.controls.photo.QsPhotoShowcaseContainer;
import it.dhd.oxygencustomizer.xposed.views.controls.photo.QsPhotoShowcaseContainerView;
import it.dhd.oxygencustomizer.xposed.views.controls.weather.QsMiniWeather;
import it.dhd.oxygencustomizer.xposed.views.controls.weather.QsMiniWeatherView;
import it.dhd.oxygencustomizer.xposed.views.controls.weather.QsViewWeather;
import it.dhd.oxygencustomizer.xposed.views.controls.weather.QsWeatherWidget;
import it.dhd.oxygencustomizer.xposed.views.controls.widgets.CalculatorWidget;
import it.dhd.oxygencustomizer.xposed.views.controls.widgets.CalculatorWidgetView;
import it.dhd.oxygencustomizer.xposed.views.controls.widgets.HomeControlsWidget;
import it.dhd.oxygencustomizer.xposed.views.controls.widgets.HomeControlsWidgetView;
import it.dhd.oxygencustomizer.xposed.views.controls.widgets.WalletWidget;
import it.dhd.oxygencustomizer.xposed.views.controls.widgets.WalletWidgetView;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WidgetMode;

public class SeparateQsWidgetsFactory {

    public static View getWidget(String widgetId, Context context, boolean settingsInterface) {
        View v;
        Log.d("SeparateQsWidgetsFactory", "getWidget: " + widgetId + " settingsInterface: " + settingsInterface);
        if (widgetId.contains("customapp")) {
            String pkgName = widgetId.replace("customapp:", "");
            v = settingsInterface ? new QsCustomAppWidget(context, pkgName, settingsInterface) : new QsCustomAppWidgetView(context, pkgName, settingsInterface);
            v.setTag(widgetId);
            if (settingsInterface) {
                v.setBackgroundResource(R.drawable.qs_tile_background);
            }
            return v;
        }
        v = switch (widgetId) {
            case "photo" -> settingsInterface ? new QsPhotoShowcaseContainer(context, settingsInterface) : new QsPhotoShowcaseContainerView(context);
            case "weather" -> settingsInterface ? new QsWeatherWidget(context, settingsInterface) : new QsViewWeather(context, settingsInterface);
            case "w:weather" -> settingsInterface ? new QsMiniWeather(context, settingsInterface) : new QsMiniWeatherView(context, settingsInterface);
            case "w:wallet" -> settingsInterface ? new WalletWidget(context, settingsInterface) : new WalletWidgetView(context, settingsInterface);
            case "w:homecontrols" -> settingsInterface ? new HomeControlsWidget(context, settingsInterface) : new HomeControlsWidgetView(context, settingsInterface);
            case "w:calculator" -> settingsInterface? new CalculatorWidget(context, settingsInterface) : new CalculatorWidgetView(context, settingsInterface);
            default -> {
                Log.d("SeparateQsWidgetsFactory", "getWidget: default");
                if (widgetId.contains("device")) {
                    yield settingsInterface ? new DeviceWidgetHolder(context, settingsInterface) : new DeviceWidgetHolderView(context);
                }
                yield null;
            }
        };
        if (v instanceof QsPhotoShowcaseContainer showcase) {
            showcase.setRadius(OCPreferences.getSliderInt(QS_PHOTO_RADIUS, 22));
        }
        if (v instanceof DeviceWidgetInterface deviceWidget) {
            Log.d("SeparateQsWidgetsFactory", "getWidget: DeviceWidgetInterface");
            String[] wSplit = widgetId.split(":");
            String widgetType = wSplit[1];
            WidgetMode wMode = WidgetMode.SMALL; // default
            if (wSplit.length > 2) {
                try {
                    wMode = WidgetMode.valueOf(wSplit[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    Log.e("SeparateQsWidgetsFactory", "getWidget: Invalid widget mode: " + wSplit[2], e);
                }
            }
            deviceWidget.setWidget(WidgetFactory.getNewWidget(context, widgetType, wMode));
        }
        if (v != null) {
            v.setTag(widgetId);
            Log.d("SeparateQsWidgetsFactory", "setTag " + widgetId);
            if (settingsInterface) {
                v.setBackgroundResource(R.drawable.qs_tile_background);
            }
        }
        return v;
    }

    public static ExtendedFAB createFAB(Context mContext, boolean mSettingsInterface) {
        ExtendedFAB fab = new ExtendedFAB(mContext);
        int resIdH = mContext.getResources().getIdentifier("qs_footer_hl_tile_height_with_media_with_volume", "dimen", SYSTEM_UI);
        if (resIdH == 0) {
            resIdH = mContext.getResources().getIdentifier("qs_footer_hl_tile_collapse_height", "dimen", SYSTEM_UI);
        }
        int h = mSettingsInterface ? ViewGroup.LayoutParams.MATCH_PARENT : mContext.getResources().getDimensionPixelSize(resIdH);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0,
                h,
                1);
        layoutParams.gravity = Gravity.CENTER;
        fab.setId(View.generateViewId());
        fab.setLayoutParams(layoutParams);
        fab.setPadding(
                dp2px(mContext, 6),
                0,
                dp2px(mContext, 6),
                0);
        fab.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        fab.setSingleLine(true);
        fab.setMarqueeRepeatLimit(-1);
        fab.setClickable(false);
        fab.setFocusable(false);
        fab.setFocusableInTouchMode(false);
        fab.setEnabled(false);
        fab.setHorizontallyScrolling(true);
        fab.setTypeface(null, Typeface.BOLD);
        fab.setGravity(Gravity.CENTER_VERTICAL);
        fab.setIconSize(dp2px(mContext, Build.VERSION.SDK_INT >= 35 ? 35 : 24)); //OOS15 has bigger icon
        fab.setTextColor(Color.WHITE);
        fab.setIconTint(null);
        fab.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int resIdH2 = mContext.getResources().getIdentifier("qs_footer_hl_tile_height_with_media_with_volume", "dimen", SYSTEM_UI);
            if (resIdH2 == 0) {
                resIdH2 = mContext.getResources().getIdentifier("qs_footer_hl_tile_collapse_height", "dimen", SYSTEM_UI);
            }
            int h2 = mSettingsInterface ? ViewGroup.LayoutParams.MATCH_PARENT : mContext.getResources().getDimensionPixelSize(resIdH2);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                    0, h2, 1);
            layoutParams2.gravity = Gravity.CENTER;
            ShapeDrawable fabBackground = new ShapeDrawable();
            if (Build.VERSION.SDK_INT < 35) {
                fabBackground.setShape(getShapeForHighlightTile(mContext));
                fab.setBackground(fabBackground);
            } else {
                fab.setBackgroundColor(Color.TRANSPARENT);
            }
            fab.setLayoutParams(layoutParams2);
            fab.requestLayout();
        });
        fab.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                fab.setSelected(true);
            }
        });

        return fab;
    }

    public static ImageView createImageView(Context mContext, boolean mSettingsInterface) {

        ImageView imageView = new ImageView(mContext);

        imageView.setId(View.generateViewId());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(
                mSettingsInterface ? dp2px(mContext, 6) : 0,
                mSettingsInterface ? dp2px(mContext, 6) : 0,
                mSettingsInterface ? dp2px(mContext, 6) : 0,
                mSettingsInterface ? dp2px(mContext, 6) : 0);
        imageView.setLayoutParams(params);
        imageView.setPadding(
                dp2px(mContext, 10),
                dp2px(mContext, 10),
                dp2px(mContext, 10),
                dp2px(mContext, 10));
        imageView.setFocusable(true);

        return imageView;

    }

}
