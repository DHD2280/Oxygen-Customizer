package it.dhd.oxygencustomizer.utils;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;

import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context) {
        SharedPreferences prefs = getDefaultSharedPreferences(context.createDeviceProtectedStorageContext());

        String localeCode = prefs.getString("appLanguage", "system");
        Locale locale = !localeCode.equals("system") ? Locale.forLanguageTag(localeCode) : Locale.getDefault();

        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();

        configuration.setLocale(locale);

        LocaleList localeList = new LocaleList(locale);
        LocaleList.setDefault(localeList);
        configuration.setLocales(localeList);

        return context.createConfigurationContext(configuration);
    }
}
