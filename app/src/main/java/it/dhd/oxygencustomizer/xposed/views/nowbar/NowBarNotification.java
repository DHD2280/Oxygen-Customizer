package it.dhd.oxygencustomizer.xposed.views.nowbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getActivityStarterExternal;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.SystemNotificationListener.getNotificationListenerExternal;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.BuildConfig;
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

    private TextView mTitle;
    private TextView mMessage;
    private ImageView mIcon;
    private TextView mNotificationHeader;
    private Object mNotificationListener = null;
    private final OnUsefulNotificationListener mOnUsefulNotificationListener;
    private long mLastNotificationTime = 0L;

    public NowBarNotification(Context context, OnUsefulNotificationListener listener) {
        super(context);
        mContext = context;
        mOnUsefulNotificationListener = listener;
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {}
        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, getActivityStarterExternal());

        inflateViews();
        mNotificationListener = getNotificationListenerExternal();
        SystemNotificationListener.addNotificationCallback(mNotificationCallback);
        GradientDrawable fallBack = new GradientDrawable();
        fallBack.setCornerRadius(100f);
        fallBack.setColor(Color.BLACK);
        setBackground(fallBack);

        setOnClickListener(v -> launchNotificationIntent());

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
            mActivityLauncherUtils.launchApp(pkgName);
        }
        removeCurrentNotification();
    }

    private void removeCurrentNotification() {
        if (currentDisplayedNotification != null) {
            StatusBarNotification sbn = currentDisplayedNotification;
            String sbnKey = sbn.getKey();
            XposedBridge.log("removeCurrentNotification: " + sbnKey + " - mNotificationListener != null " + (mNotificationListener != null));
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
        mNotificationHeader = (TextView) ViewHelper.findViewWithTag(v, "notificationHeader");
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
        if (usefulNotification != null && usefulNotification != currentDisplayedNotification && mLastNotificationTime < usefulNotification.getPostTime()) {
            mLastNotificationTime = System.currentTimeMillis();
            currentDisplayedNotification = usefulNotification;
            mOnUsefulNotificationListener.onUsefulNotification();
        }
        if (currentDisplayedNotification == null) {
            mTitle.setText("No New Notification");
            mTitle.setSelected(true);
            mMessage.setText("");
            mIcon.setImageDrawable(null);
            return;
        }
        Pair<String, String> ntf = NotificationUtils.resolveNotificationContent(currentDisplayedNotification);
        mTitle.setText(ntf.first);
        mTitle.setSelected(true);
        mMessage.setText(ntf.second);
        mMessage.setSelected(true);
        mIcon.setImageDrawable(NotificationUtils.resolveSmallIcon(currentDisplayedNotification, mContext));
    }

    public interface OnUsefulNotificationListener {
        void onUsefulNotification();
    }

}
