package it.dhd.oxygencustomizer.xposed.views.peek;

import static de.robv.android.xposed.XposedHelpers.callMethod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.SystemNotificationListener;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ThemeEnabler;

public class PeekDisplayViewController {

    @SuppressLint("StaticFieldLeak")
    private static PeekDisplayViewController instance = null;
    private PeekDisplayView mPeekView;
    private Context mContext;

    private Object mNotificationListener = null;

    private boolean mDozing = false;
    private boolean mPeekDisplayEnabled = true;

    public PeekDisplayViewController() {
        instance = this;
        SystemNotificationListener.addNotificationCallback(mNotificationCallback);
        SystemNotificationListener.addDeviceUnlockListener(mUnlockCallback);
    }

    public static PeekDisplayViewController getInstance() {
        if (instance == null) {
            return new PeekDisplayViewController();
        }
        return instance;
    }

    public void setNotificationListener(Object notificationListener) {
        mNotificationListener = notificationListener;
        if (mPeekView == null) return;
        mPeekView.setNotificationListener(notificationListener);
    }

    private final ControllersProvider.OnDozingChanged onDozingChanged = dozing -> {
        XposedBridge.log("PeekDisplayViewController onDozingChanged: " + dozing);
        if (mDozing == dozing) {
            return;
        }
        mDozing = dozing;
        if (mDozing) {
            resetShelves();
        }
    };

    private final SystemNotificationListener.NotificationCallback mNotificationCallback = new SystemNotificationListener.NotificationCallback() {
        @Override
        public void onNotificationPosted(StatusBarNotification notification, NotificationListenerService.RankingMap rankingMap) {
            if (mPeekView == null) return;
            mPeekView.currentRankingMap = rankingMap;
            StatusBarNotification[] activeNotifications = (StatusBarNotification[]) callMethod(mNotificationListener, "getActiveNotifications");
            mPeekView.updateNotificationShelf(List.of(activeNotifications));
        }

        @Override
        public void onNotificationRemoved(StatusBarNotification notification, NotificationListenerService.RankingMap rankingMap, int reason) {
            if (mPeekView == null) return;
            mPeekView.currentRankingMap = rankingMap;
            StatusBarNotification[] activeNotifications = (StatusBarNotification[]) callMethod(mNotificationListener, "getActiveNotifications");
            mPeekView.updateNotificationShelf(List.of(activeNotifications));
        }

        @Override
        public void onNotificationRemoved(StatusBarNotification notification, NotificationListenerService.RankingMap rankingMap) {
            if (mPeekView == null) return;
            mPeekView.currentRankingMap = rankingMap;
            StatusBarNotification[] activeNotifications = (StatusBarNotification[]) callMethod(mNotificationListener, "getActiveNotifications");
            mPeekView.updateNotificationShelf(List.of(activeNotifications));
        }

        @Override
        public void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
            if (mPeekView == null) return;
            mPeekView.currentRankingMap = rankingMap;
        }
    };

    private final SystemNotificationListener.DeviceUnlockListener mUnlockCallback = new SystemNotificationListener.DeviceUnlockListener() {
        @Override
        public void onDeviceUnlock(boolean unlocked) {
            if (mPeekView == null) return;
            mPeekView.setUnlocked(unlocked);
        }
    };

    public void setPeekDisplayView(PeekDisplayView view) {
        registerCallbacks();
        mPeekView = view;
        mPeekView.setNotificationListener(mNotificationListener);
        XposedBridge.log("PeekDisplayViewController addPeekDisplayView: " + view.toString());
    }

    public void registerCallbacks() {
        ControllersProvider.registerDozingCallback(onDozingChanged);
        ThemeEnabler.registerThemeChangedListener(this::updatePeekDisplayState);
        updatePeekDisplayState();
    }

    private void updatePeekDisplayState() {
        mPeekDisplayEnabled = true;
        updateVisibility();
    }

    private void unregisterCallbacks() {
        ControllersProvider.unRegisterDozingCallback(onDozingChanged);
    }

    private void removeCurrentNotification(StatusBarNotification sbn) {
        String sbnKey = sbn.getKey();
        callMethod(mNotificationListener, "cancelNotification", sbnKey);
        if (mPeekView == null) return;
        mPeekView.hideNotificationCard();
        mPeekView.updateNotificationShelf(Arrays.asList((StatusBarNotification[]) callMethod(mNotificationListener, "getActiveNotifications")));
    }

    private void clearAllNotifications() {
        try {
            callMethod(mNotificationListener, "cancelAllNotifications");
        } catch (Throwable ignored) {}
    }

    private void resetShelves() {
        if (mPeekView == null) return;
        mPeekView.hideNotificationCard();
        mPeekView.resetNotificationShelf();
    }

    private void hidePeekDisplayView() {
        if (mPeekView == null) return;
        mPeekView.setVisibility(View.GONE);
        mPeekView.hideNotificationCard();
        mPeekView.resetNotificationShelf();
    }

    private void showPeekDisplayView() {
        if (mPeekView == null) return;
        if (mPeekDisplayEnabled) {
            mPeekView.setVisibility(View.VISIBLE);
        }
    }

    private void setAlpha(float alpha) {
        if (mPeekView == null) return;
        mPeekView.setAlpha(alpha);
    }

    private void updateVisibility() {
        if (mPeekView == null) return;
        mPeekView.setVisibility(mPeekDisplayEnabled ? View.VISIBLE : View.GONE);
    }

    public PeekDisplayView getPeekView() {
        return mPeekView;
    }

}
