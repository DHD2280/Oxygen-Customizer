package it.dhd.oxygencustomizer.ui.base;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.view.Window;

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

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.LocaleHelper;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDarkTheme();
        DynamicColors.applyToActivityIfAvailable(this);
        setupEdgeToEdge();
    }

    private void setupEdgeToEdge() {
        try {
            ((AppBarLayout) findViewById(R.id.appbarLayout)).setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(getApplicationContext()));
        } catch (Exception ignored) {
        }

        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup viewGroup = getWindow().getDecorView().findViewById(android.R.id.content);
            ViewCompat.setOnApplyWindowInsetsListener(viewGroup, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                v.setPadding(
                        params.leftMargin + insets.left,
                        0,
                        params.rightMargin + insets.right,
                        0
                );
                params.bottomMargin = 0;
                v.setLayoutParams(params);

                return windowInsets;
            });
        }
    }

    private void setDarkTheme() {
        if (isNightMode()) {
            int darkStyle = Settings.System.getInt(getContentResolver(), "DarkMode_style_key", 2);
            switch (darkStyle) {
                case 0:
                    setTheme(R.style.Theme_OxygenCustomizer_DarkHard);
                    break;
                case 1:
                    setTheme(R.style.Theme_OxygenCustomizer_DarkMedium);
                    break;
                case 2:
                    setTheme(R.style.Theme_OxygenCustomizer_DarkSoft);
                    break;
            }
        }
    }

    private boolean isNightMode() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void setHeader(Context context, int title) {
        Toolbar toolbar = ((AppCompatActivity) context).findViewById(R.id.toolbar);
        ((AppCompatActivity) context).setSupportActionBar(toolbar);
        toolbar.setTitle(title);
    }

    public static void setHeader(Context context, CharSequence title) {
        Toolbar toolbar = ((AppCompatActivity) context).findViewById(R.id.toolbar);
        ((AppCompatActivity) context).setSupportActionBar(toolbar);
        toolbar.setTitle(title);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

}
