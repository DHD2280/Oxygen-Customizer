package it.dhd.oxygencustomizer.utils;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.ui.adapters.CreditsAdapter.VIEW_TYPE_ITEM;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import it.dhd.oxygencustomizer.ui.models.CreditsModel;

public class TranslatorParser {

    public ArrayList<CreditsModel> parseContributors() throws JSONException {
        ArrayList<CreditsModel> contributorsList = new ArrayList<>();
        String jsonStr = readJsonFileFromAssets("Misc/translators.json");
        JSONArray jsonArray = new JSONArray(jsonStr);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("name").replaceAll("\\s*\\(.*\\)", "");
            String username = jsonObject.getString("username");
            if (username.equals("lc98")) continue;
            String picture = jsonObject.getString("picture");
            JSONArray languagesArray = jsonObject.getJSONArray("languages");
            ArrayList<String> languagesList = new ArrayList<>();
            for (int j = 0; j < languagesArray.length(); j++) {
                languagesList.add(languagesArray.getJSONObject(j).getString("name"));
            }
            String languages = String.join(", ", languagesList);
            String url = "https://crowdin.com/profile/" + username;
            contributorsList.add(new CreditsModel(VIEW_TYPE_ITEM, name, languages, url, picture));
        }
        return contributorsList;
    }

    public String readJsonFileFromAssets(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = getAppContext().getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
