package it.dhd.oxygencustomizer.xposed.views.nowbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.XPLauncher.moduleResources;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getActivityStarterExternal;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.SystemNotificationListener.getNotificationListenerExternal;
import static it.dhd.oxygencustomizer.xposed.utils.NotificationUtils.filterNotifications;
import static it.dhd.oxygencustomizer.xposed.utils.SystemUtils.PackageManager;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.SystemNotificationListener;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.NotificationUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

@SuppressLint("ViewConstructor")
public class NowBarNotification extends RelativeLayout {

    private boolean mNotificationEnabled = false;

    private StatusBarNotification currentDisplayedNotification = null;
    private StatusBarNotification currentOpenedNotification = null;
    public NotificationListenerService.RankingMap currentRankingMap = null;

    private final Context mContext;
    private Context appContext;
    private final ActivityLauncherUtils mActivityLauncherUtils;

    private boolean mUnlocked = false;
    private boolean ignoreSecurity = false;
    private boolean mSystemAllowSecureNotifications = false;

    private String mNotificationTitleText;
    private String mNotificationContentText;

    // Containers
    private LinearLayout mLastContainer, mListContainer;

    private ColorMatrixColorFilter mColorMatrixColorFilter;

    // Notification Layout
    private TextView mTitle;
    private TextView mMessage;
    private ImageView mIcon;

    // Notification Num Layout
    private LinearLayout mIconsContainer;
    private TextView mNumText;
    private RecyclerView mIconsRecycler;
    private final NotificationAdapter mNotificationAdapter = new NotificationAdapter();
    private List<StatusBarNotification> lastFilteredNotifications = new ArrayList<>();

    // Vars
    private boolean mUseAppIcon = false,
                    mCustomColors = false;
    private int mBackgroundColor = Color.BLACK,
                mTextColor1 = Color.WHITE,
                mTextColor2 = Color.WHITE,
                mIconTintColor = Color.WHITE;

    private Object mNotificationListener = null;
    private final OnUsefulNotificationListener mOnUsefulNotificationListener;
    private long mLastNotificationTime = 0L;
    private Drawable mNotificationDrawable;

    public NowBarNotification(Context context, OnUsefulNotificationListener listener) throws Throwable {
        super(context);
        mContext = context;
        mOnUsefulNotificationListener = listener;
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {
        }
        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, getActivityStarterExternal());
        mNotificationDrawable = ResourcesCompat.getDrawable(moduleResources, R.drawable.notifications_24px, appContext.getTheme());

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        mColorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);

        inflateViews();
        mNotificationListener = getNotificationListenerExternal();
        SystemNotificationListener.addNotificationCallback(mNotificationCallback);
        SystemNotificationListener.addDeviceUnlockListener(mDeviceUnlockListener);
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

        GradientDrawable fallBack = new GradientDrawable();
        fallBack.setCornerRadius(100f);
        fallBack.setColor(Color.BLACK);
        setBackground(fallBack);

    }

    private final ContentObserver mSecureNotificationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mSystemAllowSecureNotifications = Settings.Secure.getInt(
                    mContext.getContentResolver(),
                    "keyguard_notification_visibility",
                    2) == 1;
            refreshText();
        }
    };

    public void setNotificationEnabled(boolean enabled) {
        mNotificationEnabled = enabled;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNotificationsOptions(
            boolean customColors, boolean useAppIcon,
            int backgroundColor, int textColor1, int textColor2, int iconTintColor
    ) {
        mCustomColors = customColors;
        if (mUseAppIcon != useAppIcon) {
            mUseAppIcon = useAppIcon;
            mNotificationAdapter.notifyDataSetChanged();
        }
        mBackgroundColor = backgroundColor;
        mTextColor1 = textColor1;
        mTextColor2 = textColor2;
        mIconTintColor = iconTintColor;
        setBarBackground(mCustomColors ? mBackgroundColor : Color.BLACK);
        mTitle.setTextColor(mCustomColors ? mTextColor1 : Color.WHITE);
        mMessage.setTextColor(mCustomColors ? mTextColor2 : Color.WHITE);
        mNumText.setTextColor(mCustomColors ? mTextColor2 : Color.WHITE);
        mIcon.setColorFilter(mColorMatrixColorFilter);
    }

    private void launchNotificationIntent() {
        StatusBarNotification currentNotification = mListContainer.getVisibility() == View.VISIBLE ?
                currentOpenedNotification :
                currentDisplayedNotification;
        if (currentNotification == null) return;
        String pkgName = currentNotification.getPackageName();
        if (TextUtils.isEmpty(pkgName)) return;
        PendingIntent pendingIntent = currentNotification.getNotification().contentIntent;
        try {
            callMethod(getActivityStarterExternal(), "postStartActivityDismissingKeyguard", pendingIntent);
        } catch (Throwable ignored) {
            mActivityLauncherUtils.launchApp(pkgName);
        }
        removeCurrentNotification();
    }

    private void removeCurrentNotification() {
        if (currentDisplayedNotification != null) {
            StatusBarNotification sbn = currentDisplayedNotification;
            String sbnKey = sbn.getKey();
            if (mNotificationListener != null) {
                callMethod(mNotificationListener, "cancelNotification", sbnKey);
            }
        }
    }

    public SystemNotificationListener.NotificationCallback mNotificationCallback = new SystemNotificationListener.NotificationCallback() {
        @Override
        public void onNotificationPosted(StatusBarNotification notification, NotificationListenerService.RankingMap rankingMap) {
            if (mNotificationListener == null) return;
            StatusBarNotification[] activeNotifications = (StatusBarNotification[]) callMethod(mNotificationListener, "getActiveNotifications");
            updateNotifications(List.of(activeNotifications));
        }

        @Override
        public void onNotificationRemoved(StatusBarNotification notification, NotificationListenerService.RankingMap rankingMap, int reason) {
            if (mNotificationListener == null) return;
            StatusBarNotification[] activeNotifications = (StatusBarNotification[]) callMethod(mNotificationListener, "getActiveNotifications");
            updateNotifications(List.of(activeNotifications));
        }

        @Override
        public void onNotificationRemoved(StatusBarNotification notification, NotificationListenerService.RankingMap rankingMap) {
            currentRankingMap = rankingMap;
            if (mNotificationListener == null) return;
            StatusBarNotification[] activeNotifications = (StatusBarNotification[]) callMethod(mNotificationListener, "getActiveNotifications");
            updateNotifications(List.of(activeNotifications));
        }

        @Override
        public void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
            currentRankingMap = rankingMap;
            StatusBarNotification[] activeNotifications = (StatusBarNotification[]) callMethod(mNotificationListener, "getActiveNotifications");
            updateNotifications(List.of(activeNotifications));
        }
    };

    private SystemNotificationListener.DeviceUnlockListener mDeviceUnlockListener = unlocked -> {
        if (mUnlocked == unlocked) return;
        mUnlocked = unlocked;
        toggleNotificationDetails(currentOpenedNotification);
        refreshText();
    };

    private void inflateViews() {
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(mContext, 72)));
        LayoutInflater inflater = LayoutInflater.from(appContext);
        @SuppressLint("DiscouragedApi") View v = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                "now_bar_notification",
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );

        // Containers
        mLastContainer = (LinearLayout) ViewHelper.findViewWithTag(v, "notification_last_view");
        mListContainer = (LinearLayout) ViewHelper.findViewWithTag(v, "notifications_list_view");

        // Notification Layout
        mTitle = (TextView) ViewHelper.findViewWithTag(v, "notificationTitle");
        mMessage = (TextView) ViewHelper.findViewWithTag(v, "notificationMessage");
        mIcon = (ImageView) ViewHelper.findViewWithTag(v, "notificationIcon");

        // Num Layout
        mNumText = (TextView) ViewHelper.findViewWithTag(v, "notification_num");
        mIconsContainer = (LinearLayout) ViewHelper.findViewWithTag(v, "icons_container");
        mIconsRecycler = new RecyclerView(appContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp2px(mContext, 72/2.2f));
        layoutParams.gravity = Gravity.CENTER;
        mIconsRecycler.setLayoutParams(layoutParams);
        mIconsRecycler.setScrollBarSize(0);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mIconsRecycler.setLayoutManager(layoutManager);
        mIconsRecycler.setAdapter(mNotificationAdapter);
        mIconsRecycler.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                int action = e.getAction();
                if (rv.canScrollHorizontally(RecyclerView.FOCUS_FORWARD)) {
                    if (action == MotionEvent.ACTION_MOVE) {
                        rv.getParent()
                                .requestDisallowInterceptTouchEvent(true);
                    }
                    return false;
                } else {
                    if (action == MotionEvent.ACTION_MOVE) {
                        rv.getParent()
                                .requestDisallowInterceptTouchEvent(false);
                    }
                    rv.removeOnItemTouchListener(this);
                    return true;
                }
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });
        mIconsContainer.addView(mIconsRecycler);
        mListContainer.setVisibility(View.GONE);

        final OnClickListener onClickListener = v1 -> switchView();
        final OnLongClickListener onLongClickListener = v1 -> {
            launchNotificationIntent();
            return true;
        };
        mLastContainer.setOnClickListener(onClickListener);
        mLastContainer.setOnLongClickListener(onLongClickListener);
        mListContainer.setOnClickListener(onClickListener);
        mListContainer.setOnLongClickListener(onLongClickListener);

        v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(mContext, 72)));
        addView(v);
        setBarBackground(Color.BLACK);
    }

    private void switchView() {
        mLastContainer.setVisibility(mLastContainer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        mListContainer.setVisibility(mListContainer.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void setBarBackground(int color) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(100f);
        setBackground(background);
    }

    public void updateNotifications(List<StatusBarNotification> notificationList) {
        if (!mNotificationEnabled) return;
        List<StatusBarNotification> filteredNotifications = filterNotifications(
                notificationList,
                currentRankingMap,
                true
        );

        if (filteredNotifications.equals(lastFilteredNotifications)) {
            return;
        }
        lastFilteredNotifications = filteredNotifications;
        mNotificationAdapter.clearSelection();
        mNotificationAdapter.submitList(filteredNotifications.isEmpty() ?
                new ArrayList<>() :
                filteredNotifications);
        if (mListContainer.getVisibility() == View.VISIBLE) {
            switchView();
        }
        mNumText.setText(moduleResources.getQuantityString(R.plurals.notification_summary, filteredNotifications.size(), filteredNotifications.size()));
        StatusBarNotification usefulNotification = filteredNotifications.isEmpty() ? null : filteredNotifications.get(0);
        boolean isAlertOnce = usefulNotification == null ? false : (usefulNotification.getNotification().flags & Notification.FLAG_FOREGROUND_SERVICE) == Notification.FLAG_FOREGROUND_SERVICE;
        if (usefulNotification != currentDisplayedNotification && usefulNotification != null && mLastNotificationTime < usefulNotification.getPostTime() && !isAlertOnce) {
            mLastNotificationTime = usefulNotification.getPostTime();
            currentDisplayedNotification = usefulNotification;
            mOnUsefulNotificationListener.onUsefulNotification();
        } else {
            currentDisplayedNotification = null;
        }
        if (isAlertOnce) return;
        if (currentDisplayedNotification == null) {
            mNotificationTitleText = moduleResources.getString(R.string.lockscreen_now_bar_no_notifications);
            mNotificationContentText = "";
            mIcon.setImageDrawable(mNotificationDrawable);
            refreshText();
            return;
        }
        Pair<String, String> ntf = NotificationUtils.resolveNotificationContent(currentDisplayedNotification);
        mNotificationTitleText = ntf.first;
        mNotificationContentText = ntf.second;
        refreshText();
        mIcon.setImageDrawable(mUseAppIcon ?
                NotificationUtils.resolveAppIcon(currentDisplayedNotification) :
                NotificationUtils.resolveSmallIcon(currentDisplayedNotification, mContext));
    }

    public interface OnUsefulNotificationListener {
        void onUsefulNotification();
    }

    public void setIgnoreSecurity(boolean ignoreSecurity) {
        this.ignoreSecurity = ignoreSecurity;
        refreshText();
    }

    private void refreshText() {
        mTitle.setText((ignoreSecurity || mSystemAllowSecureNotifications) || mUnlocked ?
                mNotificationTitleText :
                moduleResources.getString(R.string.lockscreen_now_bar_new_notification));
        mMessage.setText((ignoreSecurity || mSystemAllowSecureNotifications) || mUnlocked ?
                mNotificationContentText :
                moduleResources.getString(R.string.lockscreen_now_bar_new_notification_content));
        mTitle.setSelected(true);
        mMessage.setSelected(true);
    }

    private void toggleNotificationDetails(StatusBarNotification sbn) {
        if (sbn == null) return;
        Pair<String, String> notificationContent = NotificationUtils.resolveNotificationContent(sbn);
        if (TextUtils.isEmpty(notificationContent.first) && TextUtils.isEmpty(notificationContent.second)) {
            mNumText.setText(String.valueOf(lastFilteredNotifications.size()));
        } else {
            currentOpenedNotification = sbn;
            String appName = "";
            try {
                appName = PackageManager().getApplicationLabel(PackageManager().getApplicationInfo(sbn.getPackageName(), PackageManager.GET_META_DATA)).toString();
            } catch (PackageManager.NameNotFoundException e) {
                appName = sbn.getPackageName();
            }
            mNumText.setText(mUnlocked ?
                    notificationContent.first + ": " + notificationContent.second :
                    appName + ": " + moduleResources.getString(R.string.lockscreen_now_bar_new_notification_content));
            mNumText.setSelected(true);

        }
    }

    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

        private List<StatusBarNotification> notifications = new ArrayList<>();
        private int selectedPosition = -1;

        private void submitList(List<StatusBarNotification> list) {
            notifications.clear();
            notifications.addAll(list);
            mNumText.setText(moduleResources.getQuantityString(R.plurals.notification_summary, notifications.size(), notifications.size()));
            notifyDataSetChanged();
            updateRecyclerViewPadding();
        }

        private void updateRecyclerViewPadding() {
            if (mIconsRecycler == null || notifications.isEmpty()) return;

            int parentWidth = mIconsContainer.getWidth();
            int itemWidth = (int) (mIconsContainer.getHeight() * 0.7f) + (((int) (mIconsContainer.getHeight() * 0.2f)) * 2);
            int totalItemsWidth = (notifications.size() * itemWidth);

            if (totalItemsWidth < parentWidth) {
                int padding = (parentWidth - totalItemsWidth) / 2;
                mIconsRecycler.post(() -> mIconsRecycler.setPadding(padding, 0, padding, 0));
            } else {
                mIconsRecycler.post(() -> mIconsRecycler.setPadding(0, 0, 0, 0));
            }
        }

        private void clearSelection() {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = RecyclerView.NO_POSITION;
            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition);
            }
            mNumText.setText(moduleResources.getQuantityString(R.plurals.notification_summary, notifications.size(), notifications.size()));
        }

        @NonNull
        @Override
        public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            ImageView iconView = new ImageView(mContext);
            int height = (int) (mIconsContainer.getHeight() * 0.7f);
            int margin = (int) (mIconsContainer.getHeight() * 0.2f);
            RecyclerView.LayoutParams imageParams = new RecyclerView.LayoutParams(height, height);
            imageParams.setMargins(margin, 0, margin, 0);
            iconView.setLayoutParams(imageParams);
            iconView.setClickable(true);
            return new NotificationAdapter.NotificationViewHolder(iconView);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, @SuppressLint("RecyclerView") int position) {
            StatusBarNotification notification = notifications.get(position);
            Drawable iconDrawable = mUseAppIcon ?
                    NotificationUtils.resolveAppIcon(notification) :
                    NotificationUtils.resolveSmallIcon(notification, mContext);
            holder.iconView.setImageDrawable(iconDrawable);
            boolean isSelected = selectedPosition == position;
            if (isSelected) {
                int fadeFilter = ColorUtils.blendARGB(Color.TRANSPARENT, Color.GRAY, 75f / 100f);
                ColorFilter colorFilter = new PorterDuffColorFilter(fadeFilter, PorterDuff.Mode.SRC_ATOP);
                holder.iconView.setColorFilter(colorFilter);
            } else {
                holder.iconView.clearColorFilter();
            }
            holder.iconView.setOnClickListener(v -> {
                if (isSelected) {
                    selectedPosition = RecyclerView.NO_POSITION;
                    notifyItemChanged(position);
                    mNumText.setText(moduleResources.getQuantityString(R.plurals.notification_summary, notifications.size(), notifications.size()));
                } else {
                    int prevSelectedPosition = selectedPosition;
                    selectedPosition = position;
                    notifyItemChanged(prevSelectedPosition);
                    notifyItemChanged(position);
                    toggleNotificationDetails(notification);
                }
            });

        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        static class NotificationViewHolder extends RecyclerView.ViewHolder {

            public ImageView iconView;

            public NotificationViewHolder(@NonNull ImageView iconView) {
                super(iconView);
                this.iconView = iconView;
            }
        }
    }

}
