package it.dhd.oxygencustomizer.utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.activity.ShortcutActivity;

public class ShortcutUtils {

    private final Context mContext;

    /**
     * Constructor
     * @param context The context
     */
    public ShortcutUtils(@NonNull Context context) {
        this.mContext = context;
    }

    /**
     * Add starred dynamic shortcut
     */
    public void setupShortcut() {
        ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(mContext, "shortcut_1")
                .setShortLabel(mContext.getString(R.string.lsposed))
                .setLongLabel(mContext.getString(R.string.open_lsposed))
                .setIcon(
                        IconCompat.createWithResource(mContext, R.drawable.ic_lsposed))
                .setIntent(new Intent(mContext,
                        ShortcutActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setAction(Intent.ACTION_VIEW)
                        .putExtra("shortcutId", "shortcut_1"))
                .setRank(0)
                .build();

        ShortcutManagerCompat.pushDynamicShortcut(mContext, shortcut);
    }

}
