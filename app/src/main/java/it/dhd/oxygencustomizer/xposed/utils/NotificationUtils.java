package it.dhd.oxygencustomizer.xposed.utils;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.SystemUtils.PackageManager;

import android.app.Notification;
import android.app.Notification.MessagingStyle;
import android.app.Person;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.service.notification.StatusBarNotification;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationUtils {

    private static final String TAG = "NotificationHelper";

    public static Pair<String, String> resolveNotificationContent(StatusBarNotification sbn) {
        if (sbn.getNotification() == null) {
            return new Pair<>("", "");
        }
        CharSequence titleText = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE);
        if (titleText == null) {
            titleText = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE);
        }
        if (titleText == null) {
            titleText = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE_BIG);
        }
        if (titleText == null) {
            titleText = "";
        }

        CharSequence contentText = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT);
        if (contentText == null) {
            contentText = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        }
        if (contentText == null) {
            contentText = "";
        }

        return new Pair<>(titleText.toString(), contentText.toString());
    }

    @SuppressWarnings("deprecation")
    public static Drawable resolveNotificationIcon(StatusBarNotification sbn, Context context) {
        try {
            Bundle extras = sbn.getNotification().extras;

            Icon avatarIcon = getAvatarIcon(sbn);
            if (avatarIcon != null) {
                return avatarIcon.loadDrawable(context);
            }

            Object iconObject = extras.get(Notification.EXTRA_VERIFICATION_ICON);
            if (iconObject == null) {
                iconObject = extras.get("android.conversationIcon");
            }
            if (iconObject == null) {
                iconObject = extras.get(Notification.EXTRA_LARGE_ICON_BIG);
            }
            if (iconObject == null) {
                iconObject = extras.get(Notification.EXTRA_PICTURE);
            }
            if (iconObject == null) {
                iconObject = extras.get(Notification.EXTRA_LARGE_ICON);
            }
            if (iconObject == null) {
                iconObject = extras.get(Notification.EXTRA_SMALL_ICON);
            }

            if (iconObject instanceof Bitmap bitmap) {
                return new BitmapDrawable(context.getResources(), bitmap);
            } else if (iconObject instanceof Icon icon) {
                return icon.loadDrawable(context);
            } else if (iconObject instanceof Drawable drawable) {
                return drawable;
            } else {
                return resolveAppIcon(sbn);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resolving notification icon, using application icon instead", e);
            return resolveAppIcon(sbn);
        }
    }

    private static Icon getAvatarIcon(StatusBarNotification sbn) {
        try {
            Bundle extras = sbn.getNotification().extras;
            MessagingStyle.Message[] messages = MessagingStyle.Message.getMessagesFromBundleArray(
                    extras.getParcelableArray(Notification.EXTRA_MESSAGES)).toArray(new MessagingStyle.Message[0]);
            Person user = extras.getParcelable(Notification.EXTRA_MESSAGING_PERSON);
            for (int i = messages.length - 1; i >= 0; i--) {
                MessagingStyle.Message message = messages[i];
                Person sender = message.getSenderPerson();
                if (sender != null && !Objects.equals(sender, user)) {
                    return sender.getIcon();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Drawable resolveSmallIcon(StatusBarNotification sbn, Context context) {
        try {
            Context pkgContext = context.createPackageContext(
                    sbn.getPackageName(),
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE
            );
            Icon icon = sbn.getNotification().getSmallIcon();
            if (icon != null) {
                try {
                    return (Drawable) callMethod(icon, "loadDrawableAsUser", pkgContext, (int) callMethod(sbn.getUser(), "getIdentifier"));
                } catch (Throwable ignored) {
                    return icon.loadDrawable(pkgContext);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resolving small icon", e);
            return null;
        }
        return null;
    }

    public static int interpolateColors(int i2, int i3, float f2) {
        return Color.argb((int) interpolate(Color.alpha(i2), Color.alpha(i3), f2), (int) interpolate(Color.red(i2), Color.red(i3), f2), (int) interpolate(Color.green(i2), Color.green(i3), f2), (int) interpolate(Color.blue(i2), Color.blue(i3), f2));
    }

    public static float interpolate(float f2, float f3, float f4) {
        return (f2 * (1.0f - f4)) + (f3 * f4);
    }


    public static Drawable resolveAppIcon(StatusBarNotification sbn) {
        try {
            return PackageManager().getApplicationIcon(getApplicationInfo(sbn));
        } catch (Exception e) {
            return null;
        }
    }

    public static ApplicationInfo getApplicationInfo(StatusBarNotification sbn) {
        try {
        return PackageManager().getApplicationInfo(sbn.getPackageName(), 0);
        } catch (Exception e) {
            return null;
        }
    }

    public static StatusBarNotification getFirstUsefulNotification(List<StatusBarNotification> notificationList, NotificationListenerService.RankingMap currentRankingMap) {
        List<StatusBarNotification> sortedNotifications = new ArrayList<>(notificationList);
        sortedNotifications.sort((o1, o2) -> Long.compare(o2.getPostTime(), o1.getPostTime()));
        StatusBarNotification usefulNotification = null;
        long postTime = 0L;
        for (StatusBarNotification sbn : sortedNotifications) {
            NotificationListenerService.Ranking ranking = currentRankingMap != null ? (NotificationListenerService.Ranking) callMethod(currentRankingMap, "getRawRankingObject", sbn.getKey()) : null;
            boolean hasSensitiveContent = ranking != null && (boolean) callMethod(ranking, "hasSensitiveContent");
            Pair<String, String> notificationContent = NotificationUtils.resolveNotificationContent(sbn);
            String title = notificationContent.first;
            String content = notificationContent.second;
            Notification notification = sbn.getNotification();
            boolean isFgsOrUij = (boolean) callMethod(notification, "isFgsOrUij");
            boolean isMediaNotification = (boolean) callMethod(notification, "isMediaNotification");
            boolean isGroupSummary = (sbn.getNotification().flags & Notification.FLAG_GROUP_SUMMARY) != 0;
            if (sbn.getPostTime() > postTime &&
                    !sbn.isOngoing() &&
                    !isFgsOrUij &&
                    !isMediaNotification &&
                    !isGroupSummary &&
                    (sbn.getNotification().flags & Notification.FLAG_FOREGROUND_SERVICE) == 0 &&
                    (!title.isEmpty() || !content.isEmpty())) {
                usefulNotification = sbn;
                postTime = sbn.getPostTime();
            }
        }
        return usefulNotification;
    }

    public static List<StatusBarNotification> filterNotifications(List<StatusBarNotification> notificationList, NotificationListenerService.RankingMap currentRankingMap, boolean allowSecureNotifications) {
        List<StatusBarNotification> sortedNotifications = new ArrayList<>(notificationList);
        sortedNotifications.sort((o1, o2) -> Long.compare(o2.getPostTime(), o1.getPostTime()));

        List<StatusBarNotification> filteredNotifications = new ArrayList<>();
        for (StatusBarNotification sbn : sortedNotifications) {
            NotificationListenerService.Ranking ranking = currentRankingMap != null ? (NotificationListenerService.Ranking) callMethod(currentRankingMap, "getRawRankingObject", sbn.getKey()) : null;
            boolean hasSensitiveContent = ranking != null && (boolean) callMethod(ranking, "hasSensitiveContent");
            boolean shouldFilterSensitiveNotifications = !allowSecureNotifications && (ranking != null && hasSensitiveContent);
            Pair<String, String> notificationContent = NotificationUtils.resolveNotificationContent(sbn);
            String title = notificationContent.first;
            String content = notificationContent.second;
            Notification notification = sbn.getNotification();
            boolean isFgsOrUij = (boolean) callMethod(notification, "isFgsOrUij");
            boolean isMediaNotification = (boolean) callMethod(notification, "isMediaNotification");
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
        return filteredNotifications;
    }
}
