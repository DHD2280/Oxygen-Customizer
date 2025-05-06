package it.dhd.oxygencustomizer.ui.base;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.shape.MaterialShapeDrawable;

import it.dhd.oneplusui.appcompat.app.OplusActivity;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.LocaleHelper;
import it.dhd.oxygencustomizer.utils.TextUtil;

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
