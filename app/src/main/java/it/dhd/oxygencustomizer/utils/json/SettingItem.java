package it.dhd.oxygencustomizer.utils.json;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingItem {
    public enum Type { BOOLEAN, STRING, INT, LIST, SEEKBAR_FLOAT, SEEKBAR_INT, MENU }

    @SerializedName("title_res_name")
    private String title;

    @SerializedName("key")
    private String key;

    @SerializedName("type")
    private Type type; // Type (BOOLEAN, STRING, INT, LIST, SEEKBAR_FLOAT, SEEKBAR_INT, MENU)

    @SerializedName("value")
    private Object value; // current value

    @SerializedName("options_values")
    private List<String> optionsValues;

    @SerializedName("options_entries")
    private List<String> optionsEntries;



    public SettingItem(String titleResName, String key, Type type, Object value) {
        this(titleResName, key, type, value, Collections.emptyList(), Collections.emptyList());
    }

    public SettingItem(String title, String key, Type type, Object value, List<String> entries, List<String> values) {
        this.title = title;
        this.key = key;
        this.type = type;
        this.value = value;
        this.optionsEntries = entries;
        this.optionsValues = values;
    }

    public String getKey() { return key; }
    public Type getType() { return type; }
    public Object getValue() { return value; }
    public String getTitleResName() { return title; }
    public List<String> getEntries() { return optionsEntries; }
    public List<String> getValues() { return optionsValues; }

    public void setValue(Object value) { this.value = value; }
}
