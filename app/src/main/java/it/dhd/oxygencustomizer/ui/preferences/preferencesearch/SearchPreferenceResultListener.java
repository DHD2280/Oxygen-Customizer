package it.dhd.oxygencustomizer.ui.preferences.preferencesearch;

import androidx.annotation.NonNull;

public interface SearchPreferenceResultListener {
    void onSearchResultClicked(@NonNull SearchPreferenceResult result);
}