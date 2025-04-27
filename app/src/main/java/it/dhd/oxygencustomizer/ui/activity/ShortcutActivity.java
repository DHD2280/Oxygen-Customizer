package it.dhd.oxygencustomizer.ui.activity;

import static android.content.Intent.ACTION_VIEW;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.topjohnwu.superuser.Shell;

import it.dhd.oxygencustomizer.utils.RootUtil;

public class ShortcutActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        handleShortcutIntent(getIntent());
    }

    @Override
    public void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        handleShortcutIntent(intent);
    }

    private void handleShortcutIntent(Intent intent) {
        if (intent == null) finishAffinity();
        String shortcutId = intent.getStringExtra("shortcutId");
        Log.d("ShortcutActivity", "Shortcut ID: " + shortcutId);
        if (shortcutId.equals("shortcut_1")) {
            Shell.cmd("am broadcast -a android.telephony.action.SECRET_CODE -d android_secret_code://5776733 android").exec();
            finishAffinity();
        }
    }

}
