package it.dhd.oxygencustomizer.utils.json;

public class SeparateQsWidgetInfo {
    public String viewId;
    public int x, y, width, height;

    public SeparateQsWidgetInfo(String viewId, int x, int y, int width, int height) {
        this.viewId = viewId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}