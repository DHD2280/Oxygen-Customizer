package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

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
import android.os.Build;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

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
    public void onPreferenceUpdated(String... Key) {
        if (Xprefs == null) return;

        mCustomizeQSTiles = Xprefs.getBoolean(QS_CUSTOMIZE_TILES, false);
        QQSTileQty = Xprefs.getInt(QS_QUICK_TILES, QQS_NOT_SET);
        QSRowQty = Xprefs.getInt(QS_ROWS, QS_ROW_NOT_SET);
        QSColQty = Xprefs.getInt(QS_COLUMNS, QS_COL_NOT_SET);
        QSColQtyL = Xprefs.getInt(QS_COLUMNS_LANDSCAPE, QS_COL_NOT_SET);

        if (Key.length > 0) {
            for (String qsTilePref : QS_TILES_PREFS) {
                if (Key[0].equals(qsTilePref)) {
                    SystemUtils.doubleToggleDarkMode();
                }
            }
        }
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {

        ReflectedClass QuickQSPanel = ReflectedClass.ofIfPossible("com.android.systemui.qs.QuickQSPanel");
        QuickQSPanel
                .before("getNumQuickTiles")
                .run(param -> {
                    if (mCustomizeQSTiles) {
                        param.setResult(QQSTileQty);
                    }
                });

        ReflectedClass TileLayout = ReflectedClass.of("com.android.systemui.qs.TileLayout");
        TileLayout
                .before("updateMaxRows")
                .run(param -> {
                    if (!mCustomizeQSTiles ||
                            mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                        return;

                    int mRows = getIntField(param.thisObject, "mRows");
                    setIntField(param.thisObject, "mRows", QSRowQty);
                    param.setResult(mRows != QSRowQty);
                });


        TileLayout
                .before("updateColumns")
                .run(param -> {
                    if (!mCustomizeQSTiles) return;

                    int mColumns = getIntField(param.thisObject, "mColumns");
                    int orientation = mContext.getResources().getConfiguration().orientation;
                    int newColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? QSColQty : QSColQtyL;
                    setIntField(param.thisObject, "mColumns", newColumns);
                    param.setResult(mColumns != newColumns);

                });

        if (Build.VERSION.SDK_INT >= 35) {
            // Columns and Rows for separate qs
//            ReflectedClass OplusTileContainerView = ReflectedClass.of("com.oplus.systemui.plugins.qs.tile.OplusTileContainerView");
//            OplusTileContainerView
//                    .before("updateCell")
//                    .run(param -> {
//                        if (!mCustomizeQSTiles) return;
//                        setIntField(param.thisObject, "mRows", QSRowQty);
//                        setIntField(param.thisObject, "mColumns", QSColQty);
//                        param.setResult(null);
//                    });

//            ReflectedClass DeviceProfile = ReflectedClass.of("com.oplus.systemui.plugins.qs.DeviceProfile$DefaultCellCalculator");
//            DeviceProfile
//                    .before("getMaxItemNum")
//                    .run(param1 -> {
//                        if (!mCustomizeQSTiles) return;
//                        param1.setResult(QSRowQty * QSColQty);
//                    });

        }

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
