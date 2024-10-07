package it.dhd.oxygencustomizer.ui.fragments.mods.misc;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContextLocale;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_MEMC_FEATURE_GET;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_MEMC_FEATURE_RECEIVED;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.Constants.SETTINGS_SECURE_OSIE_MOTION_FLUENCY_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.SETTINGS_SECURE_OSIE_MOTION_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.SETTINGS_SECURE_OSIE_VIDEO_SWITCH;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.topjohnwu.superuser.Shell;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.preferences.OplusPreference;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.ModuleUtil;
import it.dhd.oxygencustomizer.utils.ThemeUtils;
import it.dhd.oxygencustomizer.xposed.hooks.framework.MemcEnhancer;

public class MemcFragment extends ControlledPreferenceFragmentCompat {

    private LoadingDialog mLoadingDialog;

    private boolean mIsFeatureOn = false;
    private boolean mIsPwX7Enable = false;
    private boolean mMemcEnable = false;
    private boolean mSdr2hdrEnable = false;
    private boolean mVideoOsieSupport = false;

    private boolean mReceiverRegistered = false;

    private OplusPreference mFeatureOn, mPwX7Enable, mMemcEnabled, mSdr2hdrEnabled, mVideoOsieSupported, mMemcMode;

    @Override
    public String getTitle() {
        return getString(R.string.memc_page_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.memc_overview;
    }

    @Override
    public boolean hasMenu() {
        return false;
    }

    @Override
    public String[] getScopes() {
        return new String[0];
    }

    final BroadcastReceiver mFeatureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) return;
            if (intent.getAction().equals(ACTIONS_MEMC_FEATURE_RECEIVED)) {
                mIsFeatureOn = intent.hasExtra("feature_on") ? intent.getBooleanExtra("feature_on", false) : false;
                mIsPwX7Enable = intent.hasExtra("pw_x7_enable") ? intent.getBooleanExtra("pw_x7_enable", false) : false;
                mMemcEnable = intent.hasExtra("memc_enable") ? intent.getBooleanExtra("memc_enable", false) : getMemcEnableFromSettings();
                mSdr2hdrEnable = intent.hasExtra("sdr2hdr_enable") ? intent.getBooleanExtra("sdr2hdr_enable", false) : getSdr2hdrEnableFromSettings();
                mVideoOsieSupport = intent.hasExtra("video_osie_support") ? intent.getBooleanExtra("video_osie_support", false) : false;
                setPrefs();
            }
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        mLoadingDialog = new LoadingDialog(requireContext());

        if (!mReceiverRegistered) {
            mReceiverRegistered = true;
            requireContext().registerReceiver(mFeatureReceiver, new IntentFilter(ACTIONS_MEMC_FEATURE_RECEIVED), Context.RECEIVER_EXPORTED);
        }

        mFeatureOn = findPreference("memc_feature_on");
        mPwX7Enable = findPreference("pwx7_enabled");
        mMemcEnabled = findPreference("memc_enabled");
        mSdr2hdrEnabled = findPreference("sdr2hdr_enabled");
        mVideoOsieSupported = findPreference("video_osie_support");
        mMemcMode = findPreference("memc_mode");

        new Intent(ACTIONS_MEMC_FEATURE_GET);
        requireContext().sendBroadcast(new Intent(ACTIONS_MEMC_FEATURE_GET));

    }

    private void setPrefs() {
        if (getContext() == null) {
            return;
        }

        if (mFeatureOn != null) {
            mFeatureOn.setSummary(getSpannedSummary(mIsFeatureOn));
        }

        if (mPwX7Enable != null) {
            mPwX7Enable.setSummary(getSpannedSummary(mIsPwX7Enable));
        }

        if (mMemcEnabled != null) {
            mMemcEnabled.setSummary(getSpannedSummary(getMemcEnableFromSettings()));
        }

        if (mSdr2hdrEnabled != null) {
            mSdr2hdrEnabled.setSummary(getSpannedSummary(getSdr2hdrEnableFromSettings()));
        }

        if (mVideoOsieSupported != null) {
            mVideoOsieSupported.setSummary(getSpannedSummary(mVideoOsieSupport));
        }

        if (mMemcMode != null) {
            mMemcMode.setSummary(getMemcModeFromSettings());
        }

    }

    private SpannableStringBuilder getSpannedSummary(boolean enabled) {
        String text = enabled ? getString(R.string.general_enabled) : getString(R.string.general_disabled);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        int color = enabled ?
                ContextCompat.getColor(requireContext(), android.R.color.system_accent1_400) :
                ThemeUtils.getAttrColor(requireContext(), R.attr.colorError);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;
    }

    public boolean getMemcEnableFromSettings() {
        String ret = Shell.cmd("settings get secure " + SETTINGS_SECURE_OSIE_MOTION_FLUENCY_SWITCH).exec().getOut().get(0);
        return ret.equals("1");
    }

    public String getMemcModeFromSettings() {
        String ret = Shell.cmd("settings get secure " + SETTINGS_SECURE_OSIE_MOTION_VALUE).exec().getOut().get(0);
        return ret;
    }

    public boolean getSdr2hdrEnableFromSettings() {
        String ret = Shell.cmd("settings get secure " + SETTINGS_SECURE_OSIE_VIDEO_SWITCH).exec().getOut().get(0);
        if (!this.mVideoOsieSupport) {
            return ret.equals("1");
        }
        Log.d("MemcFragment", "mVideoOsieSupport on, not support pw sdr2hdr");
        return false;
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        if (key == null) return;

        switch (key) {
            case "force_memc_enabled":
                //enableMemc();
                break;
        }

        Intent broadcast = new Intent(Constants.ACTION_SETTINGS_CHANGED);

        broadcast.putExtra("packageName", FRAMEWORK);
        broadcast.putExtra("class", MemcEnhancer.class.getSimpleName());

        if (getContext() != null)
            getContext().sendBroadcast(broadcast);
    }

    private void enableMemc() {
        if (!ModuleUtil.moduleExists()) return;

        mLoadingDialog.show(getAppContextLocale().getResources().getString(R.string.loading_dialog_wait));

        Runnable runnable = () -> {
            // wait 500 millis
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ModuleUtil.enableMemcFeature(mPreferences.getBoolean("force_memc_enabled", false));
                // hide dialog
                ((Activity) requireContext()).runOnUiThread(() -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        mLoadingDialog.hide();
                        Toast.makeText(OxygenCustomizer.getAppContext(), getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 1000);
                });

            }, 500);
        };

        Thread thread = new Thread(runnable);
        thread.start();

        Intent broadcast = new Intent(Constants.ACTION_SETTINGS_CHANGED);

        broadcast.putExtra("packageName", FRAMEWORK);
        broadcast.putExtra("class", MemcEnhancer.class.getSimpleName());

        if (getContext() != null)
            getContext().sendBroadcast(broadcast);
    }


}
