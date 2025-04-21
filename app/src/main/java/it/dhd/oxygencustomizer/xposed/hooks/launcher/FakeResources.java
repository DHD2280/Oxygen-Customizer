package it.dhd.oxygencustomizer.xposed.hooks.launcher;

public class FakeResources {

    private static int id_fake_icon_kill = -1;
    private static int id_fake_drawable_divider = -1;
    private static int id_fake_string_kill = -1;

    public static int getFakeIdIcon() {
        return id_fake_icon_kill;
    }

    public static int getIdDivider() {
        return id_fake_drawable_divider;
    }

    public static int getFakeIdString() {
        return id_fake_string_kill;
    }

    public static void setFakeIdIcon(int resId) {
        id_fake_icon_kill = resId;
    }

    public static void setIdDivider(int resId) {
        id_fake_drawable_divider = resId;
    }

    public static void setFakeIdString(int resId) {
        id_fake_string_kill = resId;
    }

}
