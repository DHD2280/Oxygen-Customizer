package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.*;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.app.Notification;
import android.os.Bundle;
import android.os.Parcel;
import android.service.notification.StatusBarNotification;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentPeekNotificationsBinding;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.interfaces.PreferenceListener;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.xposed.views.peek.PeekDisplayView;

public class LockscreenPeekNotificationsFragment extends BaseFragment {

    private FragmentPeekNotificationsBinding binding;
    private PeekDisplayView mPeekView;
    private PreferenceListener mListener;

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_peek_notifications_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPeekNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mListener = this::updatePrefs;
        mPeekView = new PeekDisplayView(requireContext(), "TOP", true);
        List<StatusBarNotification> notifications = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            notifications.add(createFakeSbn(BuildConfig.APPLICATION_ID, i, BuildConfig.APPLICATION_ID + " #" + i, "Test message " + i));
        }
        mPeekView.updateNotificationShelf(
                notifications
        );
        binding.peekContainer.addView(mPeekView);
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.getId(), new LockscreenPeekNotificationsPrefs(mListener))
                .commit();

        updatePrefs();
    }

    private StatusBarNotification createFakeSbn(String pkg, int id, String title, String text) {
        Parcel parcel = Parcel.obtain();

        try {
            long postTime = System.currentTimeMillis() - (id * 5L * DateUtils.MINUTE_IN_MILLIS);
            parcel.writeString(pkg); // pkg
            parcel.writeString(pkg); // opPkg
            parcel.writeInt(id); // id
            parcel.writeInt(1); // has tag (1 = true, 0 = false)
            parcel.writeString("fake_tag"); // tag
            parcel.writeInt(1000); // uid
            parcel.writeInt(0); // initialPid

            Notification.Builder builder = new Notification.Builder(requireContext())
                    .setContentTitle(title)
                    .setContentText(text)
                    .setWhen(postTime)
                    .setSmallIcon(R.drawable.ic_notification_foreground);

            Notification notification = builder.build();
            notification.writeToParcel(parcel, 0);

            parcel.writeLong(postTime); // postTime
            parcel.writeInt(0);
            parcel.writeInt(0);

            parcel.setDataPosition(0);

            return new StatusBarNotification(parcel);
        } finally {
            parcel.recycle();
        }
    }

    private void updatePrefs() {
        if (mPeekView == null) return;
        mPeekView.updatePeekDisplayView(
                Integer.parseInt(OCPreferences.getString(LOCKSCREEN_PEEK_NOTIFICATIONS_LOCATION, "0"))
        );
        float[] radius = new float[]{
                dp2px(requireContext(),OCPreferences.getSliderFloat(LOCKSCREEN_PEEK_CARD_TSX, 26f)), dp2px(requireContext(),OCPreferences.getSliderFloat(LOCKSCREEN_PEEK_CARD_TSX, 26f)),
                dp2px(requireContext(),OCPreferences.getSliderFloat(LOCKSCREEN_PEEK_CARD_TDX, 26f)), dp2px(requireContext(),OCPreferences.getSliderFloat(LOCKSCREEN_PEEK_CARD_TDX, 26f)),
                dp2px(requireContext(),OCPreferences.getSliderFloat(LOCKSCREEN_PEEK_CARD_BDX, 26f)), dp2px(requireContext(),OCPreferences.getSliderFloat(LOCKSCREEN_PEEK_CARD_BDX, 26f)),
                dp2px(requireContext(),OCPreferences.getSliderFloat(LOCKSCREEN_PEEK_CARD_BSX, 26f)), dp2px(requireContext(),OCPreferences.getSliderFloat(LOCKSCREEN_PEEK_CARD_BSX, 26f))
        };
        mPeekView.updatePeekStyle(
                Integer.parseInt(OCPreferences.getString(LOCKSCREEN_PEEK_NOTIFICATIONS_STYLE, "0")),
                Integer.parseInt(OCPreferences.getString(LOCKSCREEN_PEEK_ICON_STYLE, "0")),
                OCPreferences.getInt(LOCKSCREEN_PEEK_ICON_BG_COLOR, mPeekView.getSurfaceColor()),
                OCPreferences.getInt(LOCKSCREEN_PEEK_ICON_SIZE, requireContext().getResources().getDimensionPixelSize(R.dimen.peek_display_notification_icon_size)),
                OCPreferences.getInt(LOCKSCREEN_PEEK_ICON_MARGIN, requireContext().getResources().getDimensionPixelSize(R.dimen.peek_display_notification_icon_margin_end)),
                OCPreferences.getInt(LOCKSCREEN_PEEK_ICON_PADDING, requireContext().getResources().getDimensionPixelSize(R.dimen.peek_notification_icon_padding)),
                OCPreferences.getInt(LOCKSCREEN_PEEK_CARD_TITLE_COLOR, mPeekView.getPrimaryColor()),
                OCPreferences.getInt(LOCKSCREEN_PEEK_CARD_SUMMARY_COLOR, mPeekView.getSecondaryColor()),
                OCPreferences.getInt(LOCKSCREEN_PEEK_CARD_BG_COLOR, mPeekView.getSurfaceColor()),
                radius,
                OCPreferences.getInt(LOCKSCREEN_PEEK_CARD_BUTTONS_COLOR, mPeekView.getPrimaryColor()),
                false
        );
    }

    public static class LockscreenPeekNotificationsPrefs extends ControlledPreferenceFragmentCompat {

        private PreferenceListener listener;

        public LockscreenPeekNotificationsPrefs() {}

        public LockscreenPeekNotificationsPrefs(PreferenceListener listener) {
            this.listener = listener;
        }

        @Override
        public String getTitle() {
            return getString(R.string.lockscreen_peek_notifications_title);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public int getLayoutResource() {
            return R.xml.lockscreen_peek_notifications_prefs;
        }

        @Override
        public boolean hasMenu() {
            return true;
        }

        @Override
        public String[] getScopes() {
            return new String[]{SYSTEM_UI};
        }

        @Override
        public void updateScreen(String key) {
            super.updateScreen(key);

            if (key != null) listener.onPreferenceChanged();

        }

    }



}
