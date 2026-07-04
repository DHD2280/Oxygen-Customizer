package it.dhd.oxygencustomizer.xposed.views.peek;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPLauncher.moduleResources;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getActivityStarterExternal;
import static it.dhd.oxygencustomizer.xposed.utils.SystemUtils.PackageManager;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.ColorUtils;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.NotificationUtils;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

@SuppressLint("ViewConstructor")

public class PeekDisplayView extends LinearLayout {

    private final Context mContext;
    private Context appContext;

    private ColorMatrixColorFilter mColorMatrixColorFilter;

    private final int PEEK_DISPLAY_LOCATION_TOP = 0;
    private final int PEEK_DISPLAY_LOCATION_BOTTOM = 1;

    private String mTag;
    private RecyclerView notificationShelf = null;
    private LinearLayout notificationCard = null;
    private ImageView notificationIcon = null;
    private TextView notificationTitle = null;
    private TextView notificationSummary = null;
    private TextView notificationHeader = null;
    private TextView overflowText = null;
    private ImageButton clearAllButton = null;
    private ImageView minimizeButton = null;
    private ImageView dismissButton = null;
    private final Handler clearAllHandler = new Handler();
    private StatusBarNotification currentDisplayedNotification = null;
    public NotificationListenerService.RankingMap currentRankingMap = null;
    private List<StatusBarNotification> lastFilteredNotifications = new ArrayList<>();
    private int lastLayoutWidth = ViewGroup.LayoutParams.WRAP_CONTENT;

    private NotificationAdapter notificationAdapter = new NotificationAdapter();

    private Object mNotificationListener;
    private PeekDisplayViewController mController;

    private boolean mBound = false;
    private boolean allowPrivateNotifications = true;
    private boolean isMinimalStyleEnabled = false;
    public boolean isPeekDisplayEnabled = true;
    private boolean showOverflow = false;
    public int peekDisplayLocation = PEEK_DISPLAY_LOCATION_TOP;

    // Styles
    private PeekStyle PEEK_STYLE_DEFAULT, PEEK_STYLE_MINIMAL, PEEK_STYLE_CUSTOM;
    private PeekIconStyle PEEK_STYLE_ICON_DEFAULT, PEEK_STYLE_ICON_MINIMAL, PEEK_STYLE_ICON_CUSTOM;
    private PeekStyle mCurrentPeekStyle;
    private PeekIconStyle mCurrentIconStyle;

    // Private Content
    private boolean mUnlocked = false;
    private boolean mSystemAllowSecureNotifications = false;
    private boolean mIgnoreSecurity = false;

    private boolean mIsSettingsInterface = false;

    private static final int CLEAR_ALL_MODE_NONE = 0;
    private static final int CLEAR_ALL_MODE_COUNT = 1;
    private int mClearAllMode = CLEAR_ALL_MODE_COUNT;
    private int mClearAllCount = 4;

    public PeekDisplayView(Context context, String tag, boolean settingsInterface) {
        super(context);
        Log.d("PeekDisplayView", "PeekDisplayView: Constructor");
        mIsSettingsInterface = settingsInterface;
        if (!mIsSettingsInterface) {
            mController = PeekDisplayViewController.getInstance();
        }
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        mColorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
        mContext = context;
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Exception ignored) {
        }
        createIconStyles();
        createStyles();
        setTag(tag);
        mTag = tag;

        inflateView();
        if (!mIsSettingsInterface) {
            mController.setPeekDisplayView(this);
        }
        ContentResolver contentResolver = mContext.getContentResolver();
        contentResolver.registerContentObserver(
                Settings.Secure.getUriFor("keyguard_notification_visibility"),
                true,
                mSecureNotificationObserver
        );
        mSystemAllowSecureNotifications = Settings.Secure.getInt(
                mContext.getContentResolver(),
                "keyguard_notification_visibility",
                2) == 1;
    }

    private final ContentObserver mSecureNotificationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mSystemAllowSecureNotifications = Settings.Secure.getInt(
                    mContext.getContentResolver(),
                    "keyguard_notification_visibility",
                    2) == 1;
        }
    };

    private void createIconStyles() {
        PEEK_STYLE_ICON_DEFAULT = new PeekIconStyle(
                "default", /* Style Name */
                0, /* Style int according to pref */
                mIsSettingsInterface ?
                        getContext().getResources().getDimensionPixelSize(R.dimen.peek_display_notification_icon_size) :
                        moduleResources.getDimensionPixelSize(R.dimen.peek_display_notification_icon_size), /* Background Size */
                getSurfaceColor(), /* Background Color */
                mIsSettingsInterface ?
                        getContext().getResources().getDimensionPixelSize(R.dimen.peek_display_notification_icon_margin_end) :
                        moduleResources.getDimensionPixelSize(R.dimen.peek_display_notification_icon_margin_end), /* Icon Margin Horizontal */
                mIsSettingsInterface ?
                        getContext().getResources().getDimensionPixelSize(R.dimen.peek_notification_icon_padding) :
                        moduleResources.getDimensionPixelSize(R.dimen.peek_notification_icon_padding) /* Icon Padding */
        );
        PEEK_STYLE_ICON_MINIMAL = new PeekIconStyle(
                "minimal",
                1, /* Style int according to pref */
                mIsSettingsInterface ?
                        getContext().getResources().getDimensionPixelSize(R.dimen.peek_display_notification_icon_size_minimal) :
                        moduleResources.getDimensionPixelSize(R.dimen.peek_display_notification_icon_size_minimal), /* Background Size */
                Color.TRANSPARENT, /* Background Color */
                mIsSettingsInterface ?
                        getContext().getResources().getDimensionPixelSize(R.dimen.peek_display_notification_icon_margin_end) :
                        moduleResources.getDimensionPixelSize(R.dimen.peek_display_notification_icon_margin_end), /* Icon Margin Horizontal */
                0 /* Icon Padding */
        );
        PEEK_STYLE_ICON_CUSTOM = new PeekIconStyle(
                "user", /* Style Name */
                2, /* Style int according to pref */
                mIsSettingsInterface ?
                        getContext().getResources().getDimensionPixelSize(R.dimen.peek_display_notification_icon_size) :
                        moduleResources.getDimensionPixelSize(R.dimen.peek_display_notification_icon_size), /* Background Size */
                getSurfaceColor(), /* Background Color */
                mIsSettingsInterface ?
                        getContext().getResources().getDimensionPixelSize(R.dimen.peek_display_notification_icon_margin_end) :
                        moduleResources.getDimensionPixelSize(R.dimen.peek_display_notification_icon_margin_end), /* Icon Margin Horizontal */
                mIsSettingsInterface ?
                        getContext().getResources().getDimensionPixelSize(R.dimen.peek_notification_icon_padding) :
                        moduleResources.getDimensionPixelSize(R.dimen.peek_notification_icon_padding) /* Icon Padding */
        );
        mCurrentIconStyle = PEEK_STYLE_ICON_DEFAULT;
    }

    private void createStyles() {
        float[] corners = new float[]{26f, 26f, 26f, 26f, 26f, 26f, 26f, 26f};
        // Default Style
        PEEK_STYLE_DEFAULT =
                new PeekStyle(
                        "default", /* Style Name */
                        0, /* Style int according to pref */
                        getPrimaryColor(), /* Title Color */
                        getSecondaryColor(), /* Summary Color */
                        getSurfaceColor(), /* Card Background Color */
                        corners, /* Card Background Radius */
                        getPrimaryColor(), /* Buttons Color */
                        false, /* Use App Icons */
                        PEEK_STYLE_ICON_DEFAULT /* Icon Style */);
        // Minimal Style
        PEEK_STYLE_MINIMAL =
                new PeekStyle("minimal", /* Style Name */
                        1, /* Style int according to pref */
                        getPrimaryColor(), /* Title Color */
                        getSecondaryColor(), /* Summary Color */
                        getSurfaceColor(), /* Card Background Color */
                        corners, /* Card Background Radius */
                        getPrimaryColor(), /* Buttons Color */
                        false, /* Use App Icons */
                        PEEK_STYLE_ICON_MINIMAL /* Icon Style */);
        // User Style
        PEEK_STYLE_CUSTOM =
                new PeekStyle("custom", /* Style Name */
                        2, /* Style int according to pref */
                        getPrimaryColor(), /* Title Color */
                        getSecondaryColor(), /* Summary Color */
                        getSurfaceColor(), /* Card Background Color */
                        corners, /* Card Background Radius */
                        getPrimaryColor(), /* Buttons Color */
                        false, /* Use App Icons */
                        PEEK_STYLE_ICON_CUSTOM /* Icon Style */);
        mCurrentPeekStyle = PEEK_STYLE_DEFAULT;
    }

    public void setNotificationListener(Object notificationListener) {
        mNotificationListener = notificationListener;
    }

    public void setUnlocked(boolean unlocked) {
        mUnlocked = unlocked;
        if (currentDisplayedNotification != null && mUnlocked)
            notificationSummary.setText(NotificationUtils.resolveNotificationContent(currentDisplayedNotification).second);
    }

    @NonNull
    @Override
    public String toString() {
        return "PeekDisplayView{" +
                "mTag='" + mTag + '\'' +
                '}';
    }

    public void updatePeekDisplayView(int location) {
        if (peekDisplayLocation == location) return;
        mBound = false;
        removeAllViews();
        mTag = location == PEEK_DISPLAY_LOCATION_TOP ? "TOP" : "BOTTOM";
        inflateView();
    }

    /**
     * Sets if shot private content or not
     *
     * @param ignoreSecurity true if private content should be shown
     */
    public void setIgnoreSecurity(boolean ignoreSecurity) {
        mIgnoreSecurity = ignoreSecurity;
        notificationAdapter.notifyDataSetChanged();
    }

    public void updatePeekStyle(
            int newStyle,
            int newIconStyle,
            int backgroundColor, int iconSize, int iconMarginEnd, int iconPadding,
            int titleColor, int summaryColor, int cardBackgroundColor, float[] cardBackgroundRadius,
            int buttonsColor,
            boolean useAppIcons,
            int clearAllMode, int clearAllCount) {
        Log.d("PeekDisplayView", "updatePeekStyle: " + newStyle + " " + newIconStyle + "\n" +
                "backgroundColor: " + backgroundColor + "\n" +
                "iconSize: " + iconSize + "\n" +
                "iconMarginEnd: " + iconMarginEnd + "\n" +
                "iconPadding: " + iconPadding + "\n" +
                "titleColor: " + titleColor + "\n" +
                "summaryColor: " + summaryColor + "\n" +
                "cardBackgroundColor: " + cardBackgroundColor + "\n" +
                "cardBackgroundRadius: " + Arrays.toString(cardBackgroundRadius) + "\n" +
                "buttonsColor: " + buttonsColor + "\n" +
                "useAppIcons: " + useAppIcons);
        boolean requireUpdate = checkChange(newIconStyle, iconSize, backgroundColor, iconMarginEnd, iconPadding) ||
                newIconStyle != mCurrentPeekStyle.getPeekIconStyle().getStyle() ||
                useAppIcons != mCurrentPeekStyle.useAppIcons() ||
                clearAllCount != mClearAllCount ||
                clearAllMode != mClearAllMode;
        PEEK_STYLE_CUSTOM = new PeekStyle(
                "custom", 2,
                titleColor, summaryColor, cardBackgroundColor, cardBackgroundRadius,
                buttonsColor, useAppIcons, mCurrentIconStyle
        );
        PEEK_STYLE_DEFAULT.setUseAppIcons(useAppIcons);
        PEEK_STYLE_MINIMAL.setUseAppIcons(useAppIcons);
        switch (newStyle) {
            case 0:
                mCurrentPeekStyle = PEEK_STYLE_DEFAULT;
                break;
            case 1:
                mCurrentPeekStyle = PEEK_STYLE_MINIMAL;
                break;
            case 2:
                mCurrentPeekStyle = PEEK_STYLE_CUSTOM;
                break;
        }
        mClearAllMode = clearAllMode;
        mClearAllCount = clearAllCount;
        if (requireUpdate) {
            List<StatusBarNotification> notif = notificationAdapter.getNotifications();
            notificationAdapter = new NotificationAdapter();
            notificationShelf.setAdapter(notificationAdapter);
            notificationAdapter.submitList(notif);
        }
        updateViewColors();
    }

    private boolean checkChange(int newIconStyle, int iconSize, int backgroundColor, int iconMarginEnd, int iconPadding) {
        boolean requireUpdate = false;
        PeekIconStyle iconStyle = mCurrentPeekStyle.getPeekIconStyle();
        if (newIconStyle == 2) { // Custom icon selected
            boolean shouldUpdate = PEEK_STYLE_ICON_CUSTOM.getIconBackgroundColor() != backgroundColor ||
                    (PEEK_STYLE_ICON_CUSTOM.getIconSize() != iconSize) ||
                    (PEEK_STYLE_ICON_CUSTOM.getIconSpacing() != iconMarginEnd) ||
                    (PEEK_STYLE_ICON_CUSTOM.getIconPadding() != iconPadding);
            if (shouldUpdate) {
                PEEK_STYLE_ICON_CUSTOM = new PeekIconStyle(
                        PEEK_STYLE_ICON_CUSTOM.getName(),
                        PEEK_STYLE_ICON_CUSTOM.getStyle(),
                        dp2px(mContext, iconSize),
                        backgroundColor,
                        dp2px(mContext, iconMarginEnd),
                        dp2px(mContext, iconPadding)
                );
                mCurrentIconStyle = PEEK_STYLE_ICON_CUSTOM;
                return true;
            }
        }
        if (iconStyle.getStyle() != newIconStyle) {
            requireUpdate = true;
            if (newIconStyle == 0) {
                mCurrentIconStyle = PEEK_STYLE_ICON_DEFAULT;
            } else if (newIconStyle == 1) {
                mCurrentIconStyle = PEEK_STYLE_ICON_MINIMAL;
            }
        }
        return requireUpdate;
    }

    @SuppressLint("DiscouragedApi")
    private void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(appContext);
        View view = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                mTag.equals("TOP") ? "peek_display_top" : "peek_display_bottom",
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );
        LinearLayout recyclerContainer = (LinearLayout) ViewHelper.findViewWithTag(view, "recycler_container");
        notificationShelf = new RecyclerView(appContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        notificationShelf.setTag("RECYCLER");
        layoutParams.gravity = Gravity.CENTER;
        notificationShelf.setLayoutParams(layoutParams);
        notificationShelf.setScrollBarSize(0);
        recyclerContainer.addView(notificationShelf);
        notificationCard = (LinearLayout) ViewHelper.findViewWithTag(view, "notificationCard");
        notificationIcon = (ImageView) ViewHelper.findViewWithTag(view, "notificationIcon");
        notificationTitle = (TextView) ViewHelper.findViewWithTag(view, "notificationTitle");
        notificationSummary = (TextView) ViewHelper.findViewWithTag(view, "notificationSummary");
        notificationHeader = (TextView) ViewHelper.findViewWithTag(view, "notificationHeader");
        minimizeButton = (ImageView) ViewHelper.findViewWithTag(view, "minimizeButton");
        dismissButton = (ImageView) ViewHelper.findViewWithTag(view, "dismissButton");
        overflowText = (TextView) ViewHelper.findViewWithTag(view, "overflowText");
        clearAllButton = (ImageButton) ViewHelper.findViewWithTag(view, "clearAllButton");

        notificationShelf.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false));
        notificationShelf.setAdapter(notificationAdapter);
        notificationShelf.setOnClickListener(v -> {
            if (notificationCard.getVisibility() == View.VISIBLE) {
                hideNotificationCard();
            }
        });

        minimizeButton.setOnClickListener(v -> hideNotificationCard());
        dismissButton.setOnClickListener(v -> removeCurrentNotification());
        overflowText.setOnClickListener(v -> showClearAllButton());
        clearAllButton.setOnClickListener(v -> clearAllNotifications());
        notificationShelf.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mClearAllMode == CLEAR_ALL_MODE_COUNT) showClearAllButton();
            }
        });
        addView(view);
        peekDisplayLocation = mTag.equals("TOP") ? PEEK_DISPLAY_LOCATION_TOP : PEEK_DISPLAY_LOCATION_BOTTOM;
        mBound = true;
        updateView();
    }

    private void updateClearAllButtonSize() {
        clearAllButton.setLayoutParams(new LinearLayout.LayoutParams(getIconSize(), getIconSize()));
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ColorStateList.valueOf(mCurrentPeekStyle.getPeekIconStyle().getIconBackgroundColor()));
        RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), DrawableConverter.drawableToBitmap(drawable));
        roundedDrawable.setCornerRadius(getIconSize());
        clearAllButton.setBackground(roundedDrawable);
    }

    private void showClearAllButton() {
        overflowText.setVisibility(View.GONE);
        clearAllButton.setVisibility(View.VISIBLE);
        clearAllHandler.removeCallbacksAndMessages(null);
        clearAllHandler.postDelayed(() -> {
            if (clearAllButton.getVisibility() == View.VISIBLE) {
                hideClearAllButton();
            }
        }, 3000);
    }

    private void hideClearAllButton() {
        clearAllButton.setVisibility(View.GONE);
        overflowText.setVisibility(showOverflow ? View.VISIBLE : View.GONE);
    }

    private void clearAllNotifications() {
        try {
            callMethod(mNotificationListener, "cancelAllNotifications");
        } catch (Throwable ignored) {
        }
        List<StatusBarNotification> emptyList = new ArrayList<>();
        updateNotificationShelf(emptyList);
        overflowText.setVisibility(View.GONE);
        clearAllButton.setVisibility(View.GONE);
    }

    private void removeCurrentNotification() {
        if (currentDisplayedNotification != null) {
            if (mIsSettingsInterface) {
                List<StatusBarNotification> newNotif = new ArrayList<>(lastFilteredNotifications);
                newNotif.remove(currentDisplayedNotification);
                updateNotificationShelf(newNotif);
                hideNotificationCard();
                return;
            }
            StatusBarNotification sbn = currentDisplayedNotification;
            String sbnKey = sbn.getKey();
            if (mNotificationListener != null) {
                callMethod(mNotificationListener, "cancelNotification", sbnKey);
                updateNotificationShelf(Arrays.asList((StatusBarNotification[]) callMethod(mNotificationListener, "getActiveNotifications")));
                hideNotificationCard();
            }
        }
    }

    public void updateNotificationShelf(List<StatusBarNotification> notificationList) {
        if (!mBound) return;
        List<StatusBarNotification> sortedNotifications = new ArrayList<>(notificationList);
        sortedNotifications.sort((o1, o2) -> Long.compare(o2.getPostTime(), o1.getPostTime()));

        List<StatusBarNotification> filteredNotifications = new ArrayList<>();
        for (StatusBarNotification sbn : sortedNotifications) {
            NotificationListenerService.Ranking ranking = currentRankingMap != null ? (NotificationListenerService.Ranking) callMethod(currentRankingMap, "getRawRankingObject", sbn.getKey()) : null;
            boolean hasSensitiveContent = mIsSettingsInterface ? false : ranking != null && (boolean) callMethod(ranking, "hasSensitiveContent");
            boolean shouldFilterSensitiveNotifications = mIsSettingsInterface ? false : !allowPrivateNotifications && (ranking != null && hasSensitiveContent);
            Pair<String, String> notificationContent = NotificationUtils.resolveNotificationContent(sbn);
            String title = notificationContent.first;
            String content = notificationContent.second;
            Notification notification = sbn.getNotification();
            boolean isFgsOrUij = mIsSettingsInterface ? false : (boolean) callMethod(notification, "isFgsOrUij");
            boolean isMediaNotification = mIsSettingsInterface ? false : (boolean) callMethod(notification, "isMediaNotification");
            boolean isGroupSummary = (sbn.getNotification().flags & Notification.FLAG_GROUP_SUMMARY) != 0;
            if (!sbn.isOngoing() &&
                    !isFgsOrUij &&
                    !isMediaNotification &&
                    !isGroupSummary &&
                    (sbn.getNotification().flags & Notification.FLAG_FOREGROUND_SERVICE) == 0 &&
                    !shouldFilterSensitiveNotifications &&
                    (!title.isEmpty() || !content.isEmpty())) {
                filteredNotifications.add(sbn);
            }
        }

        if (filteredNotifications.equals(lastFilteredNotifications)) {
            return;
        }
        lastFilteredNotifications = filteredNotifications;
        notificationAdapter.clearSelection();
        if (!filteredNotifications.isEmpty()) {
            notificationAdapter.submitList(filteredNotifications);
            notificationShelf.setVisibility(View.VISIBLE);
        } else {
            notificationShelf.setVisibility(View.GONE);
        }

        int iconMarginEnd = mCurrentPeekStyle.getPeekIconStyle().getIconSpacing();
        int newWidth = filteredNotifications.size() <= 4 ? ViewGroup.LayoutParams.WRAP_CONTENT : (4 * getIconSize()) + (4 * iconMarginEnd);
        if (mClearAllMode == CLEAR_ALL_MODE_NONE) {
            newWidth = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        if (newWidth != lastLayoutWidth) {
            ViewGroup.LayoutParams layoutParams = notificationShelf.getLayoutParams();
            layoutParams.width = newWidth;
            notificationShelf.setLayoutParams(layoutParams);
            lastLayoutWidth = newWidth;
        }

        showOverflow = mClearAllMode == CLEAR_ALL_MODE_COUNT ?
                filteredNotifications.size() > mClearAllCount :
                false;
        overflowText.setVisibility(showOverflow ? View.VISIBLE : View.GONE);
        clearAllButton.setVisibility(View.GONE);
    }

    private int getIconSize() {
        return mCurrentPeekStyle.getPeekIconStyle().getIconSize();
    }

    private void toggleNotificationDetails(StatusBarNotification sbn) {
        Pair<String, String> notificationContent = NotificationUtils.resolveNotificationContent(sbn);
        if ((TextUtils.isEmpty(notificationContent.first) && TextUtils.isEmpty(notificationContent.second))
                || currentDisplayedNotification == sbn && notificationCard.getVisibility() == View.VISIBLE) {
            hideNotificationCard();
        } else {
            currentDisplayedNotification = sbn;
            CharSequence subtext = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE);
            String subText = TextUtils.isEmpty(subtext) ? "" : subtext.toString();
            String packageName = sbn.getPackageName();
            CharSequence timeSinceArrival = DateUtils.getRelativeTimeSpanString(
                    sbn.getPostTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
            );
            String appLabel;
            try {
                appLabel =
                        mIsSettingsInterface ?
                                AppUtils.getAppName(appContext, packageName) :
                                PackageManager().getApplicationLabel(NotificationUtils.getApplicationInfo(sbn)).toString();
            } catch (Throwable ignored) {
                appLabel = packageName;
            }

            int maxLength = 17;
            String subHeaderText = subText.length() > maxLength ?
                    subText.substring(0, maxLength) + "..." :
                    subText;
            String appLabelHeaderText = (appLabel.length() > maxLength) ?
                    appLabel.substring(0, maxLength) + "..." :
                    appLabel;

            String headerText;
            if (TextUtils.isEmpty(subHeaderText)) {
                headerText = subHeaderText + " • " + appLabelHeaderText + " • " + timeSinceArrival;
            } else {
                headerText = appLabelHeaderText + " • " + timeSinceArrival;
            }

            notificationHeader.setText(headerText);
            notificationTitle.setText(notificationContent.first);
            notificationSummary.setText(
                    (mIgnoreSecurity || mSystemAllowSecureNotifications) || mUnlocked ?
                            notificationContent.second :
                            mIsSettingsInterface ?
                                    notificationContent.second :
                                    moduleResources.getString(R.string.lockscreen_now_bar_new_notification_content));
            notificationIcon.setImageDrawable(
                    mIsSettingsInterface ?
                            AppUtils.getAppIcon(appContext, packageName) :
                            sbn.getPackageName().equals(SYSTEM_UI) ?
                                    NotificationUtils.resolveSmallIcon(sbn, mContext) :
                                    NotificationUtils.resolveNotificationIcon(sbn, mContext));
            final OnClickListener click = v -> launchNotificationIntent();
            setClickListener(click, notificationCard, notificationSummary, notificationIcon, notificationHeader);
            notificationCard.setVisibility(View.VISIBLE);
            notificationCard.setScaleX(0.8f);
            notificationCard.setScaleY(0.8f);
            notificationCard.setAlpha(0f);
            notificationCard.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(300L)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void launchNotificationIntent() {

        StatusBarNotification currentNotification = currentDisplayedNotification;
        if (currentNotification == null) return;
        String pkgName = currentNotification.getPackageName();
        if (TextUtils.isEmpty(pkgName)) return;
        PendingIntent pendingIntent = currentNotification.getNotification().contentIntent;
        try {
            callMethod(getActivityStarterExternal(), "postStartActivityDismissingKeyguard", pendingIntent);
        } catch (Throwable ignored) {
            launchAppFromPackageName(pkgName);
        }
        removeCurrentNotification();
    }

    private void launchAppFromPackageName(String pkgName) {
        Intent appIntent;
        try {
            appIntent = mContext.getPackageManager().getLaunchIntentForPackage(pkgName);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } catch (Throwable ignored) {
            return;
        }
        callMethod(getActivityStarterExternal(), "startActivity", appIntent, true);
    }

    void resetNotificationShelf() {
        notificationShelf.scrollToPosition(0);
    }

    public void hideNotificationCard() {
        notificationCard
                .animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .alpha(0f)
                .setDuration(200L)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    notificationCard.setVisibility(
                            mIsSettingsInterface ?
                                    View.INVISIBLE :
                                    peekDisplayLocation == PEEK_DISPLAY_LOCATION_TOP ?
                                            View.GONE :
                                            View.INVISIBLE);
                    currentDisplayedNotification = null;
                })
                .start();
        notificationAdapter.clearSelection();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mIsSettingsInterface) {
            mController.registerCallbacks();
        }
        updateView();
    }

    private void updateView() {
        setVisibility(isPeekDisplayEnabled ? View.VISIBLE : View.GONE);
        if (isPeekDisplayEnabled) {
            if (mNotificationListener != null) {
                updateNotificationShelf(Arrays.asList((StatusBarNotification[]) callMethod(mNotificationListener, "getActiveNotifications")));
            }
            updateViewColors();
        }
    }

    private void updatePeekDisplayState(boolean minimalStyle, boolean peekDisplayEnabled, int peekLocation) {
        allowPrivateNotifications = Settings.Secure.getInt(
                mContext.getContentResolver(),
                "lock_screen_allow_private_notifications",
                1) == 1;
        isMinimalStyleEnabled = minimalStyle;
        isPeekDisplayEnabled = peekDisplayEnabled;
        peekDisplayLocation = peekLocation;
        updateView();
    }

    private void updateViewColors() {
        int surfaceColor = mCurrentPeekStyle == PEEK_STYLE_MINIMAL ? Color.TRANSPARENT : getSurfaceColor();
        updateClearAllButtonSize();
        notificationCard.setBackground(getNotificationBackground(mCurrentPeekStyle.getCardBackgroundColor()));
        clearAllButton.setBackgroundTintList(ColorStateList.valueOf(mCurrentPeekStyle.getPeekIconStyle().getIconBackgroundColor()));
        notificationTitle.setTextColor(mCurrentPeekStyle.getTitleColor());
        notificationSummary.setTextColor(mCurrentPeekStyle.getSummaryColor());
        notificationHeader.setTextColor(mCurrentPeekStyle.getTitleColor());
        overflowText.setTextColor(mCurrentPeekStyle == PEEK_STYLE_MINIMAL ? Color.WHITE : surfaceColor);
        minimizeButton.setImageTintList(ColorStateList.valueOf(mCurrentPeekStyle.getButtonsColor()));
        dismissButton.setImageTintList(ColorStateList.valueOf(mCurrentPeekStyle.getButtonsColor()));
        clearAllButton.setImageTintList(ColorStateList.valueOf(mCurrentPeekStyle.getButtonsColor()));
        notificationAdapter.notifyItemRangeChanged(0, notificationAdapter.getItemCount());
        notificationShelf.invalidate();
    }

    private Drawable getNotificationBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setCornerRadii(mCurrentPeekStyle.getCardBackgroundRadius());
        return drawable;
    }

    private void setClickListener(OnClickListener listener, View... views) {
        for (View view : views) {
            view.setOnClickListener(listener);
        }
    }

    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

        private List<StatusBarNotification> notifications = new ArrayList<>();
        private int selectedPosition = -1;
        private boolean isHighlighted = false;

        private void submitList(List<StatusBarNotification> list) {
            notifications.clear();
            notifications.addAll(list);
            notifyDataSetChanged();
        }

        private List<StatusBarNotification> getNotifications() {
            return notifications;
        }

        private void clearSelection() {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = RecyclerView.NO_POSITION;
            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition);
            }
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            ImageView iconView = new ImageView(mContext);
            Log.d("PeekDisplayView", "onCreateViewHolder: icon size= " + mCurrentPeekStyle.getPeekIconStyle().getIconSize() + " icon padding= " + getIconPadding());
            RecyclerView.LayoutParams imageParams = new RecyclerView.LayoutParams(getIconSize(), getIconSize());
            iconView.setLayoutParams(imageParams);
            iconView.setBackground(createBackgroundDrawable(mCurrentPeekStyle.getPeekIconStyle().getIconBackgroundColor()));
            int padding = getIconPadding();
            iconView.setPadding(padding, padding, padding, padding);
            iconView.setClickable(true);
            return new NotificationViewHolder(iconView);
        }

        private int getIconPadding() {
            int resId = R.dimen.peek_notification_icon_padding;
            if (mCurrentPeekStyle.useAppIcons()) {
                resId = R.dimen.peek_notification_icon_padding_app_icons;
            }
            Log.d("PeekDisplayView", "getIconPadding: " + mCurrentPeekStyle.toString());
            if (mCurrentPeekStyle == PEEK_STYLE_CUSTOM) {
                return mCurrentPeekStyle.getPeekIconStyle().getIconPadding();
            }
            return mCurrentPeekStyle == PEEK_STYLE_MINIMAL ? 0 :
                    mIsSettingsInterface ?
                            appContext.getResources().getDimensionPixelSize(resId) :
                            moduleResources.getDimensionPixelSize(resId);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, @SuppressLint("RecyclerView") int position) {
            StatusBarNotification notification = notifications.get(position);
            RecyclerView.LayoutParams imageParams = (RecyclerView.LayoutParams) holder.iconView.getLayoutParams();
            int marginEnd = 0;
            if (position != notifications.size() - 1) {
                marginEnd = mCurrentPeekStyle.getPeekIconStyle().getIconSpacing();
            }
            if (getItemCount() > mClearAllCount && mClearAllMode == CLEAR_ALL_MODE_COUNT) {
                marginEnd = mCurrentPeekStyle.getPeekIconStyle().getIconSpacing();
            }
            imageParams.setMarginEnd(marginEnd);
            holder.iconView.setLayoutParams(imageParams);
            Drawable iconDrawable =
                    mCurrentPeekStyle.useAppIcons() ?
                            mIsSettingsInterface ?
                                    AppUtils.getAppIcon(appContext, notification.getPackageName()) :
                                    NotificationUtils.resolveAppIcon(notification) :
                            NotificationUtils.resolveSmallIcon(notification, mContext);
            holder.iconView.setImageDrawable(iconDrawable);
            float[] matrix = getTintMatrix(NotificationUtils.interpolateColors(isNightMode() ? Color.WHITE : Color.BLACK, -1, 1f), 1f * 0.67f);
            ColorMatrixColorFilter matrixFilter = new ColorMatrixColorFilter(matrix);
            holder.iconView.clearColorFilter();
            if (mCurrentPeekStyle.useAppIcons()) {
                if (mCurrentPeekStyle == PEEK_STYLE_DEFAULT)
                    holder.iconView.setColorFilter(mColorMatrixColorFilter);
            } else {
                if (DrawableConverter.isGrayscaleIcon(iconDrawable)) {
                    holder.iconView.setColorFilter(matrixFilter);
                }
            }
            boolean isSelected = selectedPosition == position;
            int color = mCurrentPeekStyle.getPeekIconStyle().getIconBackgroundColor();
            int newColor = isSelected ? ColorUtils.adjustColorForPressed(color, 0.7f) : color;
            holder.iconView.setBackground(createBackgroundDrawable(newColor));
            holder.iconView.setOnClickListener(v -> {
                if (mCurrentPeekStyle == PEEK_STYLE_MINIMAL) {
                    toggleNotificationDetails(notification);
                } else {
                    if (isSelected) {
                        selectedPosition = RecyclerView.NO_POSITION;
                        notifyItemChanged(position);
                        hideNotificationCard();
                    } else {
                        int prevSelectedPosition = selectedPosition;
                        selectedPosition = position;
                        notifyItemChanged(prevSelectedPosition);
                        notifyItemChanged(position);
                        toggleNotificationDetails(notification);
                    }
                }
            });

        }

        private float[] getTintMatrix(int i2, float f2) {
            float[] fArr = new float[20];
            Arrays.fill(fArr, 0.0f);
            fArr[4] = Color.red(i2);
            fArr[9] = Color.green(i2);
            fArr[14] = Color.blue(i2);
            fArr[18] = (Color.alpha(i2) / 255.0f) + f2;
            return fArr;
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        private GradientDrawable createBackgroundDrawable(int color) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            drawable.setCornerRadius(mIsSettingsInterface ?
                    getContext().getResources().getDimensionPixelSize(R.dimen.peek_notification_icon_corner_radius) :
                    moduleResources.getDimension(R.dimen.peek_notification_icon_corner_radius));
            return drawable;
        }

        static class NotificationViewHolder extends RecyclerView.ViewHolder {

            public ImageView iconView;

            public NotificationViewHolder(@NonNull ImageView iconView) {
                super(iconView);
                this.iconView = iconView;
            }
        }
    }

    public static int getSurfaceColor(Context context) {
        return SystemUtils.isDarkMode() ?
                ResourcesCompat.getColor(moduleResources, R.color.lockscreen_widget_background_color_dark, context.getTheme()) :
                ResourcesCompat.getColor(moduleResources, R.color.lockscreen_widget_background_color_light, context.getTheme());
    }

    public static int getPrimaryColor(Context context) {
        return SystemUtils.isDarkMode() ?
                ResourcesCompat.getColor(moduleResources, R.color.peek_title_text_color_dark, context.getTheme()) :
                ResourcesCompat.getColor(moduleResources, R.color.peek_title_text_color_light, context.getTheme());
    }

    public static int getSecondaryColor(Context context) {
        return SystemUtils.isDarkMode() ?
                ResourcesCompat.getColor(moduleResources, R.color.peek_summary_text_color_dark, context.getTheme()) :
                ResourcesCompat.getColor(moduleResources, R.color.peek_summary_text_color_light, context.getTheme());
    }

    public int getSurfaceColor() {
        return isNightMode() ?
                ResourcesCompat.getColor(appContext.getResources(), R.color.lockscreen_widget_background_color_dark, appContext.getTheme()) :
                ResourcesCompat.getColor(appContext.getResources(), R.color.lockscreen_widget_background_color_light, appContext.getTheme());
    }

    public int getPrimaryColor() {
        return isNightMode() ?
                ResourcesCompat.getColor(appContext.getResources(), R.color.peek_title_text_color_dark, appContext.getTheme()) :
                ResourcesCompat.getColor(appContext.getResources(), R.color.peek_title_text_color_light, appContext.getTheme());
    }

    public int getSecondaryColor() {
        return isNightMode() ?
                ResourcesCompat.getColor(appContext.getResources(), R.color.peek_summary_text_color_dark, appContext.getTheme()) :
                ResourcesCompat.getColor(appContext.getResources(), R.color.peek_summary_text_color_light, appContext.getTheme());
    }

    public boolean isNightMode() {
        return (mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public int getActiveColor() {
        return ResourcesCompat.getColor(appContext.getResources(), isNightMode() ? R.color.peek_title_text_color_light : R.color.peek_accent_color_dark, appContext.getTheme());
    }

}
