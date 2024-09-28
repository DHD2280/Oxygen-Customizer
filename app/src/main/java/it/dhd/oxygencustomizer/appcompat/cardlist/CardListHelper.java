package it.dhd.oxygencustomizer.appcompat.cardlist;

import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.List;

public class CardListHelper {

    public static final int FULL = 4;
    public static final int HEAD = 1;
    public static final int MIDDLE = 2;
    public static final int NONE = 0;
    public static final int TAIL = 3;

    /**
     * Determines the position of an item in a group.
     *
     * @param groupSize Total number of items in the group.
     * @param position The current position of the item within the group.
     * @return The position type (HEAD, TAIL, MIDDLE, FULL) of the item in the group.
     */
    public static int getPositionInGroup(int groupSize, int position) {
        if (groupSize == 1) {
            return FULL;
        }
        if (position == 0) {
            return HEAD;
        }
        return (position == groupSize - 1) ? TAIL : MIDDLE;
    }

    /**
     * Determines the position type of a preference within its parent group.
     *
     * @param preference The preference whose position in the group is being determined.
     * @return The position type (HEAD, TAIL, MIDDLE, FULL) of the preference.
     */
    public static int getPositionInGroup(Preference preference) {
        Preference previousPreference = null, nextPreference = null;
        PreferenceGroup parent = preference.getParent();

        if (parent == null) {
            return NONE;
        }

        List<Preference> visiblePreferences = new ArrayList<>();
        for (int i = 0; i < parent.getPreferenceCount(); i++) {
            Preference p = parent.getPreference(i);
            if (p.isVisible()) {
                visiblePreferences.add(p);
            }
        }

        int index = visiblePreferences.indexOf(preference);
        if (index == -1) {
            return NONE;
        }

        if (index > 0) {
            previousPreference = visiblePreferences.get(index - 1);
        }
        if (index < visiblePreferences.size() - 1) {
            nextPreference = visiblePreferences.get(index + 1);
        }

        boolean prevIsCard = previousPreference != null && isCardSupported(parent, previousPreference);
        boolean nextIsCard = nextPreference != null && isCardSupported(parent, nextPreference);

        if (prevIsCard && nextIsCard) {
            return MIDDLE;
        } else if (prevIsCard) {
            return TAIL;
        } else if (nextIsCard) {
            return HEAD;
        } else {
            return FULL;
        }
    }

    /**
     * Determines whether a preference in a given group is card-supported (e.g., not a PreferenceCategory).
     *
     * @param parent The parent preference group.
     * @param preference The preference to check.
     * @return True if the preference is card-supported; false otherwise.
     */
    private static boolean isCardSupported(PreferenceGroup parent, Preference preference) {
        return !(preference instanceof PreferenceCategory) || parent instanceof PreferenceScreen;
    }

    /**
     * Sets the configuration change listener for a given view.
     *
     * @param view The view to set the listener on.
     * @param listener The configuration change listener to apply.
     */
    public static void setConfigurationChangeListener(View view, CardListSelectedItemLayout.ConfigurationChangedListener listener) {
        if (view instanceof CardListSelectedItemLayout) {
            ((CardListSelectedItemLayout) view).setConfigurationChangeListener(listener);
        }
    }

    /**
     * Sets the background of the item based on its position in the group.
     *
     * @param view The view representing the item.
     * @param position The position of the item in the group (HEAD, TAIL, MIDDLE, etc.).
     */
    public static void setItemCardBackground(View view, int position) {
        if (view instanceof CardListSelectedItemLayout) {
            ((CardListSelectedItemLayout) view).setPositionInGroup(position);
        }
    }
}
