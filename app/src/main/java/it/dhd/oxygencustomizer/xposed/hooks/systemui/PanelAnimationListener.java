package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.systemui.panelanimation.PanelAnimationEx;

import java.util.ArrayList;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class PanelAnimationListener extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    @SuppressLint("StaticFieldLeak")
    private static PanelAnimationListener instance = null;
    private final ArrayList<ExpandedFractionChangeListener> mExpandedFractionChangeListeners = new ArrayList<>();

    public static void registerExpandedFractionChangeCallback(ExpandedFractionChangeListener callback) {
        instance.mExpandedFractionChangeListeners.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterExpandedFractionChangeCallback(ExpandedFractionChangeListener callback) {
        instance.mExpandedFractionChangeListeners.remove(callback);
    }

    private final PanelAnimationEx.ExpandedFractionChangeListener fractionChangeListener = new PanelAnimationEx.ExpandedFractionChangeListener() {
        @Override
        public void onFractionChanged(float fraction) {
            notifyFractionChanged(fraction);
        }

        @Override
        public void onFractionEnd(float fraction) {
            notifyFractionEnd(fraction);
        }

        @Override
        public void onBlurFractionChange() {
            notifyBlurFractionChange();
        }
    };

    public PanelAnimationListener(Context context) {
        super(context);
        instance = this;
    }

    @Override
    public void updatePrefs(String... Key) {
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        ReflectedClass OplusSimpleQSFakeController = ReflectedClass.ofIfPossible("com.oplus.systemui.separate.OplusSimpleQSFakeController");
        OplusSimpleQSFakeController
                .after("onViewCreated")
                .run(param -> {
                    PanelAnimationEx panelAnimationEx = (PanelAnimationEx) getObjectField(param.thisObject, "panelAnimationEx");
                    if (panelAnimationEx == null) return;
                    if (panelAnimationEx.getUseLayeredAnimation()) {
                        panelAnimationEx.getPanelExpandFraction().addCallback(this.fractionChangeListener);
                    }
                });
        OplusSimpleQSFakeController
                .after("onDestroy")
                .run(param -> {
                    PanelAnimationEx panelAnimationEx = (PanelAnimationEx) getObjectField(param.thisObject, "panelAnimationEx");
                    if (panelAnimationEx == null) return;
                    if (panelAnimationEx.getUseLayeredAnimation()) {
                        panelAnimationEx.getPanelExpandFraction().removeCallback(this.fractionChangeListener);
                    }
                });
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private void notifyFractionChanged(float fraction) {
        for (ExpandedFractionChangeListener callback : mExpandedFractionChangeListeners) {
            try {
                callback.onFractionChanged(fraction);
            } catch (Throwable ignored) {
            }
        }
    }

    private void notifyFractionEnd(float fraction) {
        for (ExpandedFractionChangeListener callback : mExpandedFractionChangeListeners) {
            try {
                callback.onFractionEnd(fraction);
            } catch (Throwable ignored) {
            }
        }
    }

    private void notifyBlurFractionChange() {
        for (ExpandedFractionChangeListener callback : mExpandedFractionChangeListeners) {
            try {
                callback.onBlurFractionChange();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Callback for QS Panel (notification side) fraction change /OOS16
     */
    public interface ExpandedFractionChangeListener {
        void onFractionChanged(float fraction);

        default void onBlurFractionChange() {
        }

        default void onFractionEnd(float fraction) {
        }
    }

}
