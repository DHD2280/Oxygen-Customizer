package it.dhd.oxygencustomizer.xposed.hooks.systemui.advancedreboot;

public class AdvancedRebootResources {

    private static int id_fake_title_advanced_reboot_string = -1;
    private static int id_fake_title_recovery_string = -1;
    private static int id_fake_title_bootloader_string = -1;
    private static int id_fake_title_safe_mode_string = -1;
    private static int id_fake_title_fast_reboot_string = -1;
    private static int id_fake_title_systemui_string = -1;
    private static int id_fake_icon_recovery = -1;
    private static int id_fake_icon_bootloader = -1;
    private static int id_fake_icon_safe_mode = -1;

    public static int getFakeIdDialogTitle() {
        return id_fake_title_advanced_reboot_string;
    }

    public static int getFakeIdTitleRecovery() {
        return id_fake_title_recovery_string;
    }

    public static int getFakeIdTitleBootloader() {
        return id_fake_title_bootloader_string;
    }

    public static int getFakeIdTitleSafeMode() {
        return id_fake_title_safe_mode_string;
    }

    public static int getFakeIdTitleFastReboot() {
        return id_fake_title_fast_reboot_string;
    }

    public static int getFakeIdTitleSystemUi() {
        return id_fake_title_systemui_string;
    }

    public static int getFakeIdIconRecovery() {
        return id_fake_icon_recovery;
    }

    public static int getFakeIdIconBootloader() {
        return id_fake_icon_bootloader;
    }

    public static int getFakeIdIconSafeMode() {
        return id_fake_icon_safe_mode;
    }

    public static void setFakeIdDialogTitle(int resId) {
        id_fake_title_advanced_reboot_string = resId;
    }

    public static void setFakeIdTitleRecovery(int resId) {
        id_fake_title_recovery_string = resId;
    }

    public static void setFakeIdTitleBootloader(int resId) {
        id_fake_title_bootloader_string = resId;
    }

    public static void setFakeIdTitleSafeMode(int resId) {
        id_fake_title_safe_mode_string = resId;
    }

    public static void setFakeIdTitleFastReboot(int resId) {
        id_fake_title_fast_reboot_string = resId;
    }

    public static void setFakeIdTitleSystemUi(int resId) {
        id_fake_title_systemui_string = resId;
    }

    public static void setFakeIdIconRecovery(int resId) {
        id_fake_icon_recovery = resId;
    }

    public static void setFakeIdIconBootloader(int resId) {
        id_fake_icon_bootloader = resId;
    }

    public static void setFakeIdIconSafeMode(int resId) {
        id_fake_icon_safe_mode = resId;
    }

}
