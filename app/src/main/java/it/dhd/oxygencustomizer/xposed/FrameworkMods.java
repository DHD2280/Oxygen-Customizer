package it.dhd.oxygencustomizer.xposed;

import static it.dhd.oxygencustomizer.utils.Constants.ACTION_SETTINGS_CHANGED;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.FrameworkSettings.EXTRA_CLASS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import io.github.libxposed.api.XposedModuleInterface;

public abstract class FrameworkMods extends XposedMods {

    private final BroadcastReceiver mSettingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!ACTION_SETTINGS_CHANGED.equals(intent.getAction())) return;
                if (!FrameworkMods.this.getClass().getSimpleName().equals(intent.getStringExtra(EXTRA_CLASS)))
                    return;
                log("Intent received - will update preferences");
                loadSettings();
            } catch (Throwable t) {
                log(t);
            }
        }
    };
    private boolean mSettingsLoaded = false;
    private boolean mReceiverRegistered = false;
    private boolean mHooksInstalled = false;

    public FrameworkMods(Context context) {
        super(context);
    }

    protected abstract void loadSettings();

    protected abstract void onFrameworkLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable;

    final void attach(Context context) {
        mContext = context;
        registerSettingsReceiver();
    }

    @Override
    public final void onPreferenceUpdated(String... Key) {
        if (mSettingsLoaded) return;
        loadSettings();
        mSettingsLoaded = true;
    }

    @Override
    public final void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        registerSettingsReceiver();
        if (mHooksInstalled) return;
        mHooksInstalled = true;
        onFrameworkLoaded(PRParam);
    }

    private void registerSettingsReceiver() {
        if (mReceiverRegistered || mContext == null) return;
        mReceiverRegistered = true;
        mContext.registerReceiver(mSettingsReceiver, new IntentFilter(ACTION_SETTINGS_CHANGED), Context.RECEIVER_EXPORTED);
    }

    @Override
    public boolean listensTo(String packageName) {
        return FRAMEWORK.equals(packageName);
    }
}
