package it.dhd.oxygencustomizer.ui.fragments.mods;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

import com.topjohnwu.superuser.Shell;

import org.json.JSONException;
import org.json.JSONObject;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.preferences.OplusColorPreference;
import it.dhd.oxygencustomizer.ui.preferences.OplusPreference;
import it.dhd.oxygencustomizer.utils.ColorUtils;

public class ColorsFragment extends ControlledPreferenceFragmentCompat {

    private OplusColorPreference mPrimaryColor;
    private OplusPreference mApplyColors;
    private String mSystemPalette, mAccentColor;
    private int mColorToApply = 0;
    private LoadingDialog mLoadingDialog;

    @Override
    public String getTitle() {
        return getString(R.string.colors);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.colors_prefs;
    }

    @Override
    public boolean hasMenu() {
        return false;
    }

    @Override
    public String[] getScopes() {
        return new String[0];
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLoadingDialog = new LoadingDialog(requireContext());

        MenuHost menuHost = requireActivity();
        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Add menu items here
                menu.add(0, 1, 0, R.string.menu_launch_app)
                        .setIcon(R.drawable.ic_launch)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle the menu selection
                if (menuItem.getItemId() == 1) {
                    Shell.cmd("am start -n com.oplus.uxdesign/com.oplus.uxdesign.personal.PersonalActivity").exec();
                    return true;
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        mPrimaryColor = findPreference("primary_color");
        mApplyColors = findPreference("apply_colors");

        mApplyColors.setOnPreferenceClickListener(preference -> {
            applyColors();
            return true;
        });

    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);
        getDefaultColor();
        if (key == null) return;

        if (key.equals("primary_color")) {
            mColorToApply = mPreferences.getInt("primary_color", 0);
        }
    }

    private void getDefaultColor() {
        String secureTheme = Shell.cmd("settings get secure theme_customization_overlay_packages")
                .exec()
                .getOut().get(0);
        try {
            JSONObject jsonObject = new JSONObject(secureTheme);

            mSystemPalette = jsonObject.getString("android.theme.customization.system_palette");
            mAccentColor = jsonObject.getString("android.theme.customization.accent_color");
        } catch (JSONException e) {
            Log.e("ColorsFragment", "Error parsing JSON", e);
        }
        if (TextUtils.isEmpty(mAccentColor)) {
            Log.e("ColorsFragment", "Accent color is empty");
            return;
        }
        if (!mAccentColor.contains("#")) {
            mAccentColor = "#" + mAccentColor;
        }
        if (mPreferences.getInt("primary_color", 0) == 0) {
            mPrimaryColor.setPreviewColor(Color.parseColor(mAccentColor), true);
        } else {
            int color = mPreferences.getInt("primary_color", 0);
            if (color != Color.parseColor(mAccentColor)) {
                mPrimaryColor.setPreviewColor(color, true);
            }
        }
    }

    private void applyColors() {
        if (mColorToApply == 0) return;

        mLoadingDialog.show(getString(R.string.loading_dialog_wait));
        Log.d("ColorsFragment", "Applying color: " + String.format("#%08X", (0xFFFFFFFF & mColorToApply)));
        generateColorFiles(String.format("#%08X", (0xFFFFFFFF & mColorToApply)));
        saveSecureSettings();

    }

    private void generateColorFiles(String colorHex) {
        int baseColor = Color.parseColor(colorHex);

        int pressedColor = ColorUtils.adjustColorForPressed(baseColor, 0.3f);
        int lightNormalColor = ColorUtils.adjustAlpha(baseColor, 0.3f);
        int lightPressedColor = ColorUtils.adjustAlpha(pressedColor, 0.3f);
        int textHighLightColor = ColorUtils.adjustAlpha(baseColor, 0.15f);
        int barDisabledColor = textHighLightColor;

        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>\n" +
                "<resources>\n" +
                "<color name=\"couiSingleFirstNormal\">" + colorHex + "</color>\n" +
                "<color name=\"couiSingleFirstPressed\">" + String.format("#%08X", (0xFFFFFFFF & pressedColor)) + "</color>\n" +
                "<color name=\"couiSingleFirstLightNormal\">" + String.format("#%08X", (0xFFFFFFFF & lightNormalColor)) + "</color>\n" +
                "<color name=\"couiSingleFirstLightPressed\">" + String.format("#%08X", (0xFFFFFFFF & lightPressedColor)) + "</color>\n" +
                "<color name=\"couiSingleFirstTextHighLight\">" + String.format("#%08X", (0xFFFFFFFF & textHighLightColor)) + "</color>\n" +
                "<color name=\"couiSingleFirstBarDisabledColor\">" + String.format("#%08X", (0xFFFFFFFF & barDisabledColor)) + "</color>\n" +
                "<color name=\"NXcolorSingleFirstNormal\">" + colorHex + "</color>\n" +
                "<color name=\"NXcolorSingleFirstPressed\">" + String.format("#%08X", (0xFFFFFFFF & pressedColor)) + "</color>\n" +
                "<color name=\"NXcolorSingleFirstLightNormal\">" + String.format("#%08X", (0xFFFFFFFF & lightNormalColor)) + "</color>\n" +
                "<color name=\"NXcolorSingleFirstLightPressed\">" + String.format("#%08X", (0xFFFFFFFF & lightPressedColor)) + "</color>\n" +
                "<color name=\"NXcolorSingleFirstTextHighLight\">" + String.format("#%08X", (0xFFFFFFFF & textHighLightColor)) + "</color>\n" +
                "<color name=\"NXcolorSingleFirstBarDisabledColor\">" + String.format("#%08X", (0xFFFFFFFF & barDisabledColor)) + "</color>\n" +
                "</resources>";

        writeToFile(content, "ux_custom_color.xml");

        int nightPressedColor = ColorUtils.adjustColorForPressed(baseColor, 0.7f);
        int nightLightNormalColor = ColorUtils.adjustAlpha(baseColor, 0.4f);
        int nightLightPressedColor = ColorUtils.adjustAlpha(nightPressedColor, 0.3f);

        String nightContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>\n" +
                "<resources>\n" +
                "<color name=\"couiSingleFirstNormal\">" + colorHex + "</color>\n" +
                "<color name=\"couiSingleFirstPressed\">" + String.format("#%08X", (0xFFFFFFFF & nightPressedColor)) + "</color>\n" +
                "<color name=\"couiSingleFirstLightNormal\">" + String.format("#%08X", (0xFFFFFFFF & nightLightNormalColor)) + "</color>\n" +
                "<color name=\"couiSingleFirstLightPressed\">" + String.format("#%08X", (0xFFFFFFFF & nightLightPressedColor)) + "</color>\n" +
                "<color name=\"couiSingleFirstTextHighLight\">" + String.format("#%08X", (0xFFFFFFFF & textHighLightColor)) + "</color>\n" +
                "<color name=\"couiSingleFirstBarDisabledColor\">" + String.format("#%08X", (0xFFFFFFFF & textHighLightColor)) + "</color>\n" +
                "<color name=\"NXcolorSingleFirstNormal\">" + colorHex + "</color>\n" +
                "<color name=\"NXcolorSingleFirstPressed\">" + String.format("#%08X", (0xFFFFFFFF & nightPressedColor)) + "</color>\n" +
                "<color name=\"NXcolorSingleFirstLightNormal\">" + String.format("#%08X", (0xFFFFFFFF & nightLightNormalColor)) + "</color>\n" +
                "<color name=\"NXcolorSingleFirstLightPressed\">" + String.format("#%08X", (0xFFFFFFFF & nightLightPressedColor)) + "</color>\n" +
                "<color name=\"NXcolorSingleFirstTextHighLight\">" + String.format("#%08X", (0xFFFFFFFF & textHighLightColor)) + "</color>\n" +
                "<color name=\"NXcolorSingleFirstBarDisabledColor\">" + String.format("#%08X", (0xFFFFFFFF & textHighLightColor)) + "</color>\n" +
                "</resources>";

        writeToFile(nightContent, "ux_custom_color_night.xml");
    }

    private void writeToFile(String content, String fileName) {
        Log.d("ColorsFragment", "Writing\n" + content + "\nto file: " + fileName);
        try {
            Shell.cmd("printf '" + content + "' > /data/oplus/uxres/uxcolor/" + fileName).exec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveSecureSettings() {
        long timestamp = System.currentTimeMillis();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("_applied_timestamp", timestamp);
            jsonObject.put("android.theme.customization.theme_style", "TONAL_SPOT");
            jsonObject.put("android.theme.customization.color_source", "home_wallpaper");
            jsonObject.put("material_you_overlay_enable", 1);
            jsonObject.put("android.theme.customization.color_index", 0);
            jsonObject.put("android.theme.customization.system_palette", String.format("%08X", (0xFFFFFFFF & mColorToApply)));
            jsonObject.put("android.theme.customization.accent_color", String.format("%08X", (0xFFFFFFFF & mColorToApply)));

            /*
            {
                "_applied_timestamp": 1727720712371,
                "android.theme.customization.theme_style": "VIBRANT",
                "android.theme.customization.color_source": "home_wallpaper",
                "material_you_overlay_enable": 1,
                "android.theme.customization.color_index": 0,
                "android.theme.customization.system_palette": "FF38D6D4",
                "android.theme.customization.accent_color": "FF38D6D4"
            }
             */


            String secureString = jsonObject.toString();
            Log.d("ColorsFragment", "Applying secure settings: " + secureString);
            Shell.cmd(
                    "settings put secure theme_customization_overlay_packages '" + secureString + "'"
            ).exec();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mLoadingDialog.hide();
    }

}
