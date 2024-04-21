package it.dhd.oxygencustomizer.customprefs.preferencesearch;

import androidx.annotation.NonNull;

public interface SearchPreferenceResultListener {
    void onSearchResultClicked(@NonNull SearchPreferenceResult result);
}