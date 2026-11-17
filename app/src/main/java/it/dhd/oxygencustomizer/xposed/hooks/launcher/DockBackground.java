package it.dhd.oxygencustomizer.xposed.hooks.launcher;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callStaticMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getObjectField;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.android.launcher3.uioverrides.states.blurdrawable.BlurUtils;
import com.android.launcher3.uioverrides.states.blurdrawable.IBlurBaseController;
import com.android.launcher3.uioverrides.states.blurdrawable.LayerBlurDrawable;
import com.android.launcher3.uioverrides.states.blurdrawable.OplusBlurProperties;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.Logger;
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
    private Class<?> mBlurPropertiesCompanion = null;
    private IBlurBaseController mBlurController = null;
    private Drawable mBlurBackground = null;

    public DockBackground(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {

        mDockBackground = Xprefs.getBoolean("dockBackground", false);
        mDockBackgroundMaterial = Xprefs.getBoolean("dockBackgroundMaterial", false);
        mDockBackgroundBlurAmount = mBlurAmount[Xprefs.getInt("dockBackgroundMaterialAmount", 0)];
        mDockBackgroundRadius = Xprefs.getInt("dockBackgroundRadius", 30);

        ViewGroup hotseat = mOplusHotseat;
        if (hotseat != null && mDockBackgroundMaterial && !mDockBackground) {
            hotseat.post(() -> initBlurBackground(hotseat));
        }

    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {

        if (Build.VERSION.SDK_INT < 35) return;
        ReflectedClass OplusHotseatCls = ReflectedClass.of("com.android.launcher3.OplusHotseat");
        if (ReflectedClass.ofIfPossible("com.android.launcher3.uioverrides.states.blurdrawable.BlurBaseFactory").getClazz() != null) {
            mBlurPropertiesCompanion = ReflectedClass.of("com.android.launcher3.uioverrides.states.blurdrawable.OplusBlurProperties$Companion").getClazz();
        }

        if (mBlurPropertiesCompanion != null) {
            OplusHotseatCls
                    .after("setDockerBackground")
                    .run(param -> {
                        if (mDockBackgroundMaterial && !mDockBackground) {
                            initBlurBackground(param.thisObject);
                        }
                    });

            OplusHotseatCls
                    .after("onDetachedFromWindow")
                    .run(param -> {
                        if (mBlurController != null) {
                            mBlurController.recycle();
                            mBlurController = null;
                        }
                        mBlurBackground = null;
                    });
        }

        OplusHotseatCls
                .after("init")
                .run(param -> {
                    if (mDockBackground) {
                        callMethod(param.thisObject, "setDockerBackground");
                    } else if (mDockBackgroundMaterial) {
                        try {
                            initBlurBackground(param.thisObject);
                        } catch (Throwable t) {
                            Logger.log(t);
                        }
                    }
                });

        OplusHotseatCls
                .after("onAttachedToWindow")
                        .run(param -> {
                            ViewGroup oplusHotseat = (ViewGroup) param.thisObject;
                            oplusHotseat.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                                @Override
                                public boolean onPreDraw() {
                                    oplusHotseat.getViewTreeObserver().removeOnPreDrawListener(this);
                                    if (mDockBackgroundMaterial) {
                                        try {
                                            initBlurBackground(param.thisObject);
                                        } catch (Throwable t) {
                                            Logger.log(t);
                                        }
                                    }
                                    return true;
                                }
                            });
                        });

        OplusHotseatCls
                .after("onDraw")
                        .run(param -> {
                            if (mDockBackground) {
                                try {
                                    callMethod(param.thisObject, "setDockerBackground");
                                } catch (Throwable t) {
                                    log(t);
                                }
                            } else if (!mDockBackgroundMaterial) {
                                try {
                                    View mShortcutsAndWidgets = (View) getObjectField(param.thisObject, "mShortcutsAndWidgets");
                                    if (mShortcutsAndWidgets.getBackground() != null) {
                                        mShortcutsAndWidgets.setBackground(null);
                                    }
                                } catch (Throwable t) {
                                    log(t);
                                }
                            }
                        });

    }

    // Reference: OplusPageIndicator
    public final void initBlurBackground(Object thisObject) {
        if (mBlurPropertiesCompanion != null) {
            initBlurBaseBackground((ViewGroup) thisObject);
            return;
        }
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

    private void initBlurBaseBackground(ViewGroup hotseat) {
        mOplusHotseat = hotseat;
        View container = (View) getObjectField(hotseat, "mShortcutsAndWidgets");
        Context context = hotseat.getContext();
        if (mBlurController == null || container.getBackground() != mBlurBackground) {
            if (mBlurController != null) {
                mBlurController.recycle();
            }
            mBlurController = (IBlurBaseController) callStaticMethod(mBlurPropertiesCompanion, "prepareBlur", container, context, true, true, 4, true);
            mBlurBackground = container.getBackground();
            if (mBlurController == null) return;
        }
        BlurUtils.BlendColorParams colors = BlurUtils.getBlurColorParams(context);
        mBlurController.setBlurCornerRadius(dp2px(mContext, mDockBackgroundRadius), false, null, true);
        mBlurController.setBlurParams(mDockBackgroundBlurAmount, 2, colors.getBlendColor(), colors.getMixColor());
        mBlurController.setIgnorePageScrollBlurAnim(true);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
