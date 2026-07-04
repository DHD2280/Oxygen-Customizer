package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.isOOS1501;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.regex.Pattern;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class NotificationVanillaIceCream extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private boolean hasOverlays = false;
    private final Pattern capsulePattern = Pattern.compile(
            "Capsule|" +
                    "NotificationCapsuleContainer|" +
                    "CapsuleEarView|" +
                    "OplusClearAllButton|" +
                    "OplusNotificationChildrenContainerOverlayExImpl|" +
                    "OplusCustomRow"
    );

    public NotificationVanillaIceCream(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        hasOverlays = Xprefs.getBoolean("hasNotificationOverlays", false);
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {

        if (Build.VERSION.SDK_INT < 35) return;

        ReflectedClass ViewBlurManager = ReflectedClass.of("com.oplus.systemui.notification.blur.ViewBlurManager");

        ReflectedClass.ReflectionConsumer nullReturner = param -> {
            if (hasOverlays) param.setResult(null);
        };

        ViewBlurManager
                .before("updateCardPlatformMixConfig")
                .run(nullReturner);
        ViewBlurManager
                .before("updateRowsBlur")
                .run(nullReturner);
        ViewBlurManager
                .before("headsupCardMotionMixConfig")
                .run(nullReturner);

        ViewBlurManager
                .before("requireBlurProxyForView")
                .run(param -> {
                    if (!isOOS1501() && hasOverlays) {
                        View v = (View) param.args[0];
                        String className = v != null ? v.getClass().getName() : "";
                        if (v != null &&
                                capsulePattern.matcher(className).find()) return;
                        param.setResult(null);
                    }
                });
        ViewBlurManager
                .before("blurForHeadsUp")
                .run(nullReturner);
        ViewBlurManager
                .before("cancelBlurForHeadsUp")
                .run(nullReturner);
        ViewBlurManager
                .before("applyBlurConfig")
                .run(nullReturner);

        ReflectedClass NotificationChildrenContainerExtImp = ReflectedClass.of("com.oplus.systemui.statusbar.notification.stack.NotificationChildrenContainerExtImp");
        NotificationChildrenContainerExtImp
                .before("getHeaderBlurDrawable")
                .run(nullReturner);
        NotificationChildrenContainerExtImp
                .before("getHeaderBgDrawable")
                .run(param -> {
                    if (Build.VERSION.SDK_INT != 36) return;
                    if (!hasOverlays) return;
                    Drawable colorDrawable = new ColorDrawable(mContext.getColor(mContext.getResources().getIdentifier(
                            "notification_material_background_color_compass",
                            "color",
                            mContext.getPackageName()
                    )));
                    Drawable header = new HeaderDrawable(
                            colorDrawable,
                            (int) callMethod(param.thisObject, "getStackedNotificationMaskColor")
                    );
                    param.setResult(header);

                });

        ReflectedClass OplusCloseAllController = ReflectedClass.ofIfPossible("com.oplus.systemui.statusbar.notification.OplusCloseAllController");
        OplusCloseAllController
                .before("access$getPlatformBlurDrawable")
                .run(param -> {
                    if (Build.VERSION.SDK_INT < 36) return;
                    Drawable d = (Drawable) param.args[1];
                    param.setResult(d);
                });
        OplusCloseAllController
                .before("updatePlatformBlurDrawable")
                .run(nullReturner);
        OplusCloseAllController
                .before("updatePlatformBlurDrawable$default")
                .run(param -> {
                    if (hasOverlays) {
                        param.setResult(null);
                    }
                });

        ReflectedClass ClearAllController = ReflectedClass.ofIfPossible("com.oplus.systemui.notification.clearall.ClearAllController");
        ClearAllController
                .before("getPlatformBlurDrawable")
                .run(param -> {
                    if (Build.VERSION.SDK_INT < 36) return;
                    Drawable d = (Drawable) param.args[0];
                    param.setResult(d);
                });
        ClearAllController
                .before("updatePlatformBlurDrawable")
                .run(nullReturner);
        ClearAllController
                .before("updatePlatformBlurDrawable$default")
                .run(param -> {
                    if (hasOverlays) {
                        param.setResult(null);
                    }
                });

        ReflectedClass FullScreenBannerContainer = ReflectedClass.ofIfPossible("com.oplus.systemui.notification.interruption.fullscreenbanner.view.FullScreenBannerContainer");
        FullScreenBannerContainer
                .before("onViewAttachedToWindow")
                .run(nullReturner);
        FullScreenBannerContainer
                .before("onViewDetachedFromWindow")
                .run(nullReturner);

        ReflectedClass OplusNotificationChildrenContainerOverlayExImpl = ReflectedClass.ofIfPossible("com.oplus.systemui.statusbar.notification.stack.OplusNotificationChildrenContainerOverlayExImpl");
        OplusNotificationChildrenContainerOverlayExImpl
                .before("flushBackground")
                .run(param -> {
                    // run method without blur
                    // fucking oos16.0.7 makes things harder
                    if (!hasOverlays) return;
                    View view = (View) param.args[0];
                    Object expandableNotificationRow = param.args[1];
                    Drawable drawableMutate;
                    Drawable drawableNewDrawable;
                    if (view != null) {
                        Object notificationBackgroundView = expandableNotificationRow != null ? getObjectField(expandableNotificationRow, "mBackgroundNormal") : null;
                        Drawable baseBackLayer = notificationBackgroundView != null ? (Drawable) callMethod(notificationBackgroundView, "getBaseBackgroundLayer") : null;
                        Drawable baseBackgroundLayer = notificationBackgroundView != null ? baseBackLayer : null;
                        if (baseBackgroundLayer != null) {
                            Drawable.ConstantState constantState = baseBackgroundLayer.getConstantState();
                            if (constantState == null || (drawableNewDrawable = constantState.newDrawable(view.getContext().getResources())) == null || (drawableMutate = drawableNewDrawable.mutate()) == null) {
                                drawableMutate = baseBackgroundLayer.mutate();
                            }
                        } else {
                            drawableMutate = null;
                        }
                        view.setBackground(drawableMutate);
                        view.invalidate();
                    }
                    param.setResult(null);
                });

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    public static class HeaderDrawable extends Drawable {

        private final Drawable drawable;
        private final int maskColor;

        public HeaderDrawable(Drawable drawable, int maskColor) {
            this.drawable = drawable;
            this.maskColor = maskColor;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            this.drawable.setBounds(getBounds());
            this.drawable.draw(canvas);
            if (maskColor != 0) {
                canvas.drawColor(maskColor);
            }

        }

        @Override
        public void setAlpha(int alpha) {
            drawable.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            drawable.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return drawable.getOpacity();
        }
    }

}
