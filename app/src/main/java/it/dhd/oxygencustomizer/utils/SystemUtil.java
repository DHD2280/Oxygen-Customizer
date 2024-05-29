package it.dhd.oxygencustomizer.utils;

import com.topjohnwu.superuser.Shell;

public class SystemUtil {

    public static void mountRW() {
        Shell.cmd("mount -o remount,rw /").exec();
        if (RootUtil.moduleExists("magisk_overlayfs")) {
            Shell.cmd("-mm -c magic_remount_rw").exec();
        } else if (RootUtil.moduleExists("overlayfs")) {
            Shell.cmd("/data/overlayfs/tmp/overlayrw -rw /system/product/overlay").exec();
        }
    }

    public static void mountRO() {
        Shell.cmd("mount -o remount,ro /").exec();
        if (RootUtil.moduleExists("magisk_overlayfs")) {
            Shell.cmd("-mm -c magic_remount_ro").exec();
        } else if (RootUtil.moduleExists("overlayfs")) {
            Shell.cmd("/data/overlayfs/tmp/overlayrw -ro /system/product/overlay").exec();
        }
    }

}
