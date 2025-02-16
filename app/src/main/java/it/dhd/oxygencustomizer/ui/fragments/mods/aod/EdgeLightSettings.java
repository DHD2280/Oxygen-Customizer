package it.dhd.oxygencustomizer.ui.fragments.mods.aod;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_STYLE;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import it.dhd.oneplusui.preference.OplusJumpPreference;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;

public class EdgeLightSettings extends ControlledPreferenceFragmentCompat {
    @Override
    public String getTitle() {
        return getString(R.string.edge_light_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.edge_light_settings;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{SYSTEM_UI};
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        if (key == null) setJumps();
    }

    private void setJumps() {
        OplusJumpPreference mEdgeStyle = findPreference("edge_light_style_jump");

        if (mEdgeStyle != null) {
            mEdgeStyle.setSummary(getJumpText());
        }

    }

    private SpannableStringBuilder getJumpText() {
        String[] mEntires = getResources().getStringArray(R.array.edge_light_style_entries);
        String text = mEntires[Integer.parseInt(mPreferences.getString(EDGE_LIGHT_STYLE, "0"))];
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        int color = ContextCompat.getColor(requireContext(), android.R.color.system_accent1_400);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;
    }

}
