package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.setIntField;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTiles.QS_COLUMNS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTiles.QS_COLUMNS_LANDSCAPE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTiles.QS_CUSTOMIZE_TILES;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTiles.QS_QUICK_TILES;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTiles.QS_ROWS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTiles.QS_TILES_PREFS;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.content.res.Configuration;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

public class QSTiles extends XposedMods {

    public static final String listenPackage = Constants.Packages.SYSTEM_UI;

    private static final int NOT_SET = 0;
    private static final int QS_COL_NOT_SET = 4;
    private static final int QS_ROW_NOT_SET = 3;
    private static final int QQS_NOT_SET = 5;

    private static int QSRowQty = NOT_SET;
    private static int QSColQty = QS_COL_NOT_SET;
    private static int QQSTileQty = QQS_NOT_SET;

    private static int QSColQtyL = QS_COL_NOT_SET;

    private boolean mCustomizeQSTiles = false;

    public QSTiles(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        mCustomizeQSTiles = Xprefs.getBoolean(QS_CUSTOMIZE_TILES, false);
        QQSTileQty = Xprefs.getSliderInt(QS_QUICK_TILES, QQS_NOT_SET);
        QSRowQty = Xprefs.getSliderInt(QS_ROWS, QS_ROW_NOT_SET);
        QSColQty = Xprefs.getSliderInt(QS_COLUMNS, QS_COL_NOT_SET);
        QSColQtyL = Xprefs.getSliderInt(QS_COLUMNS_LANDSCAPE, QS_COL_NOT_SET);

        if (Key.length > 0) {
            for (String qsTilePref : QS_TILES_PREFS) {
                if (Key[0].equals(qsTilePref)) {
                    SystemUtils.doubleToggleDarkMode();
                }
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listenPackage.equals(lpparam.packageName)) return;

        Class<?> QuickQSPanel = findClass("com.android.systemui.qs.QuickQSPanel", lpparam.classLoader);
        findAndHookMethod(QuickQSPanel, "getNumQuickTiles", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (mCustomizeQSTiles) {
                    param.setResult(QQSTileQty);
                }
            }
        });

        Class<?> TileLayout = findClass("com.android.systemui.qs.TileLayout", lpparam.classLoader);
        findAndHookMethod(TileLayout, "updateMaxRows",
                int.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!mCustomizeQSTiles ||
                                mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                            return;

                        int mRows = getIntField(param.thisObject, "mRows");
                        setIntField(param.thisObject, "mRows", QSRowQty);
                        param.setResult(mRows != QSRowQty);

                    }
                });


        hookAllMethods(TileLayout, "updateColumns",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!mCustomizeQSTiles) return;

                        int mColumns = getIntField(param.thisObject, "mColumns");
                        int orientation = mContext.getResources().getConfiguration().orientation;
                        int newColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? QSColQty : QSColQtyL;
                        setIntField(param.thisObject, "mColumns", newColumns);
                        param.setResult(mColumns != newColumns);

                    }
                });

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
