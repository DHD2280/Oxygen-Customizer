package it.dhd.oxygencustomizer.xposed.hooks;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class HookTester extends XposedMods {
    public HookTester(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                new Thread(() -> {
                    Intent broadcast = new Intent(Constants.ACTION_XPOSED_CONFIRMED);

                    broadcast.putExtra("packageName", PRParam.getPackageName());

                    broadcast.setPackage(BuildConfig.APPLICATION_ID);

                    mContext.sendBroadcast(broadcast);
                }).start();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_CHECK_XPOSED_ENABLED);

        mContext.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    @Override
    public boolean listensTo(String packageName) {
        return true;
    }
}
