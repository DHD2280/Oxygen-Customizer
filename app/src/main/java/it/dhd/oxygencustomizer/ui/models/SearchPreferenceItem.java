package it.dhd.oxygencustomizer.ui.models;

import androidx.annotation.StringRes;
import androidx.annotation.XmlRes;
import androidx.fragment.app.Fragment;

public class SearchPreferenceItem {
    @XmlRes
    private final int xml;

    @StringRes
    private final int title;

    private final Fragment fragment;
    private final boolean shouldAdd;

    public SearchPreferenceItem(@XmlRes int xml, @StringRes int title, Fragment fragment) {
        this(xml, title, fragment, true);
    }

    public SearchPreferenceItem(@XmlRes int xml, @StringRes int title, Fragment fragment, boolean shouldAdd) {
        this.xml = xml;
        this.title = title;
        this.fragment = fragment;
        this.shouldAdd = shouldAdd;
    }

    public int getXml() {
        return xml;
    }

    public int getTitle() {
        return title;
    }

    public Fragment getFragment() {
        return fragment;
    }


}
