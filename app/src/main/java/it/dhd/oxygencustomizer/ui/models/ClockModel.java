package it.dhd.oxygencustomizer.ui.models;


import android.view.ViewGroup;

public class ClockModel {

    private String title;
    private int layout;
    private boolean isSelected;
    private ViewGroup clock;

    public ClockModel(String title, int layout) {
        this.title = title;
        this.layout = layout;
        this.isSelected = false;
    }

    public ClockModel(ViewGroup clock) {
        this.clock = clock;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public ViewGroup getClock() {
        return clock;
    }

    public void setClock(ViewGroup clock) {
        this.clock = clock;
    }
}
