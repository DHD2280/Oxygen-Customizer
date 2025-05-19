package it.dhd.oxygencustomizer.xposed.hooks.launcher;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.uioverrides.states.blurdrawable.LayerBlurDrawable;
import com.android.launcher3.uioverrides.states.blurdrawable.OplusBlurProperties;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class DockBackground extends XposedMods {

    private static final String listenPackage = LAUNCHER;

    private boolean mDockBackground = false;
    private boolean mDockBackgroundMaterial = false;
    private final int[] mBlurAmount = new int[]{ 240, 300, 480, 660, 800 };
    private int mDockBackgroundBlurAmount = 0;
    private int mDockBackgroundRadius = 30;
    private ViewGroup mOplusHotseat = null;
    private OplusBlurProperties mBlurProp = null;

    public DockBackground(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {

        mDockBackground = Xprefs.getBoolean("dockBackground", false);
        mDockBackgroundMaterial = Xprefs.getBoolean("dockBackgroundMaterial", false);
        mDockBackgroundBlurAmount = mBlurAmount[Xprefs.getInt("dockBackgroundMaterialAmount", 0)];
        mDockBackgroundRadius = Xprefs.getInt("dockBackgroundRadius", 30);

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (Build.VERSION.SDK_INT < 35) return;
        ReflectedClass OplusHotseatCls = ReflectedClass.of("com.android.launcher3.OplusHotseat");

        OplusHotseatCls
                .after("init")
                .run(param -> {
                    if (mDockBackground) {
                        callMethod(param.thisObject, "setDockerBackground");
                    } else if (mDockBackgroundMaterial) {
                        initBlurBackground(param.thisObject);
                    }
                });

        OplusHotseatCls
                .after("onDraw")
                        .run(param -> {
                            if (mDockBackgroundMaterial) {
                                initBlurBackground(param.thisObject);
                            } else {
                                try {
                                    View mShortcutsAndWidgets = (View) getObjectField(param.thisObject, "mShortcutsAndWidgets");
                                    if (mShortcutsAndWidgets.getBackground() != null) {
                                        mShortcutsAndWidgets.setBackground(null);
                                    }
                                } catch (Throwable ignored) {}
                            }
                        });

    }

    // Reference: OplusPageIndicator
    public final void initBlurBackground(Object thisObject) {
        this.mOplusHotseat = (ViewGroup) thisObject;
        View mShortcutsAndWidgets = (View) getObjectField(mOplusHotseat, "mShortcutsAndWidgets");
        Context context = mOplusHotseat.getContext();
        OplusBlurProperties.BlendColorParams blurColorParams = OplusBlurProperties.getBlurColorParams(mOplusHotseat.getContext());
        if (this.mBlurProp == null) {
            OplusBlurProperties prepareBlur = OplusBlurProperties.prepareBlur(mShortcutsAndWidgets, context, true, true, 3);
            if (prepareBlur != null) {
                prepareBlur.setBlurCornerRadius(dp2px(mContext, mDockBackgroundRadius), false);
                prepareBlur.setBlurParams(mDockBackgroundBlurAmount, blurColorParams.getBlendMode(), blurColorParams.getBlendColor(), blurColorParams.getMixColor());
                this.mBlurProp = prepareBlur;
                return;
            }
            return;
        }
        if (!(mShortcutsAndWidgets.getBackground() instanceof LayerBlurDrawable)) {
            mBlurProp.setBlurBgToView(mShortcutsAndWidgets);
        }
        mBlurProp.setBlurCornerRadius(dp2px(mContext, mDockBackgroundRadius), false);
        mBlurProp.setBlurParams(mDockBackgroundBlurAmount, blurColorParams.getBlendMode(), blurColorParams.getBlendColor(), blurColorParams.getMixColor());
    }


    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
