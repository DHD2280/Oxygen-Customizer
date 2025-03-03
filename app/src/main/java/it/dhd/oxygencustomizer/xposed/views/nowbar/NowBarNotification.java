package it.dhd.oxygencustomizer.xposed.views.nowbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getActivityStarterExternal;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.SystemNotificationListener.getNotificationListenerExternal;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.SystemNotificationListener;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.NotificationUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

@SuppressLint("ViewConstructor")
public class NowBarNotification extends RelativeLayout {

    private StatusBarNotification currentDisplayedNotification = null;
    public NotificationListenerService.RankingMap currentRankingMap = null;

    private final Context mContext;
    private Context appContext;
    private final ActivityLauncherUtils mActivityLauncherUtils;

    private boolean mUnlocked = false;
    private boolean ignoreSecurity = false;
    private boolean mSystemAllowSecureNotifications = false;

    private String mNotificationTitleText;
    private String mNotificationContentText;

    private TextView mTitle;
    private TextView mMessage;
    private ImageView mIcon;
    private Object mNotificationListener = null;
    private final OnUsefulNotificationListener mOnUsefulNotificationListener;
    private long mLastNotificationTime = 0L;
    private Drawable mNotificationDrawable;

    public NowBarNotification(Context context, OnUsefulNotificationListener listener) {
        super(context);
        mContext = context;
        mOnUsefulNotificationListener = listener;
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {
        }
        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, getActivityStarterExternal());
        mNotificationDrawable = ResourcesCompat.getDrawable(modRes, R.drawable.notifications_24px, appContext.getTheme());

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

        setOnClickListener(v -> launchNotificationIntent());

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

    private void launchNotificationIntent() {
        StatusBarNotification currentNotification = currentDisplayedNotification;
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
        mUnlocked = unlocked;
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
        mTitle = (TextView) ViewHelper.findViewWithTag(v, "notificationTitle");
        mMessage = (TextView) ViewHelper.findViewWithTag(v, "notificationMessage");
        mIcon = (ImageView) ViewHelper.findViewWithTag(v, "notificationIcon");
        v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(mContext, 72)));
        addView(v);
        setBarBackground();
    }

    private void setBarBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.BLACK);
        background.setCornerRadius(100f);
        setBackground(background);
    }

    public void updateNotifications(List<StatusBarNotification> notificationList) {
        StatusBarNotification usefulNotification = NotificationUtils.getFirstUsefulNotification(notificationList, currentRankingMap);
        if (usefulNotification != currentDisplayedNotification && mLastNotificationTime < usefulNotification.getPostTime()) {
            mLastNotificationTime = System.currentTimeMillis();
            currentDisplayedNotification = usefulNotification;
            mOnUsefulNotificationListener.onUsefulNotification();
        } else {
            currentDisplayedNotification = null;
        }
        if (currentDisplayedNotification == null) {
            mNotificationTitleText = modRes.getString(R.string.lockscreen_now_bar_no_notifications);
            mNotificationContentText = "";
            mIcon.setImageDrawable(mNotificationDrawable);
            refreshText();
            return;
        }
        Pair<String, String> ntf = NotificationUtils.resolveNotificationContent(currentDisplayedNotification);
        mNotificationTitleText = ntf.first;
        mNotificationContentText = ntf.second;
        refreshText();
        mIcon.setImageDrawable(NotificationUtils.resolveSmallIcon(currentDisplayedNotification, mContext));
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
                modRes.getString(R.string.lockscreen_now_bar_new_notification));
        mMessage.setText((ignoreSecurity || mSystemAllowSecureNotifications) || mUnlocked ?
                mNotificationContentText :
                modRes.getString(R.string.lockscreen_now_bar_new_notification_content));
        mTitle.setSelected(true);
        mMessage.setSelected(true);
    }

}
