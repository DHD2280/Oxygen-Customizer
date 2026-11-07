package it.dhd.oxygencustomizer.ui.base;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.color.DynamicColors;

import it.dhd.oneplusui.appcompat.app.OplusActivity;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.LocaleHelper;

public class BaseActivity extends OplusActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT < 35) EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarContrastEnforced(false);
        DynamicColors.applyToActivityIfAvailable(this);
    }

    public static void setHeader(Context context, int title) {
        Toolbar toolbar = ((AppCompatActivity) context).findViewById(R.id.toolbar);
        ((AppCompatActivity) context).setSupportActionBar(toolbar);
        toolbar.setTitle(title);
    }

    public static void setHeader(Context context, CharSequence title) {
        if (TextUtils.isEmpty(title)) return;
        Toolbar toolbar = ((AppCompatActivity) context).findViewById(R.id.toolbar);
        ((AppCompatActivity) context).setSupportActionBar(toolbar);
        toolbar.setTitle(title);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

}
