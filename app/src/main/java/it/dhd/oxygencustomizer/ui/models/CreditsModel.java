package it.dhd.oxygencustomizer.ui.models;

import android.graphics.drawable.Drawable;

import it.dhd.oxygencustomizer.ui.adapters.CreditsAdapter;

public class CreditsModel {

    private final int viewType;
    private final String title;
    private final String summary;
    private final String url;
    private int icon;
    private Drawable drawable;
    private String onlineIcon;

    public CreditsModel(String title) {
        this.viewType = CreditsAdapter.VIEW_TYPE_HEADER;
        this.title = title;
        this.summary = null;
        this.url = null;
        this.onlineIcon = null;
    }

    public CreditsModel(int viewType, String title, String summary, String url, int icon) {
        this.viewType = viewType;
        this.title = title;
        this.summary = summary;
        this.url = url;
        this.icon = icon;
        this.onlineIcon = null;
    }

    public CreditsModel(int viewType, String title, String summary, String url, Drawable drawable) {
        this.viewType = viewType;
        this.title = title;
        this.summary = summary;
        this.url = url;
        this.drawable = drawable;
        this.onlineIcon = null;
    }

    public CreditsModel(int viewType, String title, String summary, String url, String onlinePicture) {
        this.viewType = viewType;
        this.title = title;
        this.summary = summary;
        this.url = url;
        this.onlineIcon = onlinePicture;
    }

    /**
     * View Type of Credit List
     * Could be:
     * 0 - Header {@link CreditsAdapter#VIEW_TYPE_HEADER}
     * 1 - Item {@link CreditsAdapter#VIEW_TYPE_ITEM}
     * {@link CreditsAdapter}
     * @return viewType
     */
    public int getViewType() {
        return viewType;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public int getIcon() {
        return icon;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public String getUrl() {
        return url;
    }

    public String getOnlineIcon() {
        return onlineIcon;
    }

}
