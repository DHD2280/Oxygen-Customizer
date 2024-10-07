package it.dhd.oxygencustomizer.provider;

import com.crossbowffs.remotepreferences.RemotePreferenceFile;
import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

import it.dhd.oxygencustomizer.BuildConfig;

public class RemotePrefProvider extends RemotePreferenceProvider {
    public RemotePrefProvider() {
        super(BuildConfig.APPLICATION_ID, new RemotePreferenceFile[]{new RemotePreferenceFile(BuildConfig.APPLICATION_ID + "_preferences", true)});
    }
}
