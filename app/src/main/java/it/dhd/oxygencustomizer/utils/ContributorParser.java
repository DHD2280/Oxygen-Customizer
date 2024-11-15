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
import java.util.Arrays;
import java.util.List;

import it.dhd.oxygencustomizer.ui.models.CreditsModel;

public class ContributorParser {

    private List<String> mExcludedContributors = new ArrayList<>(){{
        addAll(Arrays.asList("DHD2280", "crowdin-bot", "github-actions[bot]"));
    }};

    public ArrayList<CreditsModel> parseContributors() throws JSONException {
        ArrayList<CreditsModel> contributorsList = new ArrayList<>();
        String jsonStr = readJsonFileFromAssets("Misc/contributors.json");
        JSONArray jsonArray = new JSONArray(jsonStr);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("login");
            if (mExcludedContributors.contains(name)) continue;
            String picture = jsonObject.getString("avatar_url");
            String profileUrl = jsonObject.getString("html_url");
            contributorsList.add(new CreditsModel(VIEW_TYPE_ITEM, name, "GitHub Contributor", profileUrl, picture));
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
