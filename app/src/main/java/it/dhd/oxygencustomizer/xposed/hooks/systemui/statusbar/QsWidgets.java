package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_BLUR_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_FILTER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_TINT_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_TINT_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_SHOW_ALBUM_ART;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_TILE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_TILE_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR_HIGHLIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR_HIGHLIGHT_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_CUSTOM_COLORS_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_DISABLED_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_DISABLED_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_DISABLED_COLOR_HIGHLIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_DISABLED_COLOR_HIGHLIGHT_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHLIGHT_CUSTOM_COLORS_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR_HIGHLIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR_HIGHLIGHT_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_BOTTOM_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_BOTTOM_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_TOP_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_TOP_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_PHOTO_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_PHOTO_SHOWCASE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_WIDGETS_LIST;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_WIDGETS_SWITCH;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.oplus.systemui.qs.base.widget.QsStaticViewInfoProvider;
import com.oplus.systemui.qs.base.widget.QsViewBackgroundProxy;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.QsStyleObserver;
import it.dhd.oxygencustomizer.xposed.utils.systemui.StaticViewBackgroundProxyImplExOC;
import it.dhd.oxygencustomizer.xposed.utils.systemui.StaticViewBackgroundProxyImplOC;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.views.controls.QsControlsView;

public class QsWidgets extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    public Object mActivityStarter = null;
    private ViewGroup mOplusQsMediaView = null;
    private float mDownX, mDownY;
    private boolean mIsDragging;
    private final int TOUCH_SLOP = ViewConfiguration.get(mContext).getScaledTouchSlop();
    private boolean mIsTouchOnWidgets;
    private Object mOplusPanelPagerController = null; // separate view pager controller
    private boolean mQsWidgetsEnabled = false;
    private String mQsWidgetsList = "media";
    // Photo Showcase
    private int mQsPhotoRadius = 22;
    private boolean mQsPhotoShowcase = false;

    // Qs Colors
    // Media
    private boolean qsCustomMediaTileColor = false;
    private Drawable mDefaultMediaBg = null;
    private int qsMediaTileColor = Color.WHITE;
    // Qs Tile Colors Highlight
    private boolean qsCustomHighlightTileColors = false; // Main Switch OOS15
    private int qsInactiveColorHighlight, qsActiveColorHighlight, qsDisabledColorHighlight;
    private boolean qsInactiveColorEnabledHighlight = false, qsActiveColorEnabledHighlight = false, qsDisabledColorEnabledHighlight = false;
    // Base
    // Qs Tile Colors Base
    private boolean qsCustomTileColors = false; // Main Switch OOS15
    private int qsInactiveColor, qsActiveColor, qsDisabledColor;
    private boolean qsInactiveColorEnabled = false, qsActiveColorEnabled = false, qsDisabledColorEnabled = false;

    // Qs Tile Radius
    private boolean customHighlightTileRadius = false, customTileRadius = false;
    private int highlightTSRadius, highlightTDRadius, highlightBSRadius, highlightBDRadius;
    private int tileTSRadius, tileTDRadius, tileBSRadius, tileBDRadius;

    // Qs Media Tile Album Art
    private boolean showMediaArtMediaQs = false;
    private int mMediaQsArtFilter = 0, mMediaQsTintColor = Color.WHITE, mMediaQsTintAmount = 20;
    private float mMediaQsArtBlurAmount = 7.5f;

    public QsWidgets(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        mQsWidgetsEnabled = Xprefs.getBoolean(QS_WIDGETS_SWITCH, false);
        mQsWidgetsList = Xprefs.getString(QS_WIDGETS_LIST, "media");
        mQsPhotoRadius = Xprefs.getSliderInt(QS_PHOTO_RADIUS, 22);
        mQsPhotoShowcase = Xprefs.getString(QS_PHOTO_SHOWCASE, "0").equals("1");

        // Media QS
        showMediaArtMediaQs = Xprefs.getBoolean(QS_MEDIA_SHOW_ALBUM_ART, false);
        mMediaQsArtFilter = Integer.parseInt(Xprefs.getString(QS_MEDIA_ART_FILTER, "0"));
        mMediaQsArtBlurAmount = (Xprefs.getSliderInt(QS_MEDIA_ART_BLUR_AMOUNT, 30) / 100f) * 25f;
        mMediaQsTintColor = Xprefs.getInt(QS_MEDIA_ART_TINT_COLOR, Color.WHITE);
        mMediaQsTintAmount = Xprefs.getSliderInt(QS_MEDIA_ART_TINT_AMOUNT, 20);

        // Tile Colors
        qsCustomMediaTileColor = Xprefs.getBoolean(QS_MEDIA_TILE_CUSTOM_COLOR, false);
        qsMediaTileColor = Xprefs.getInt(QS_MEDIA_TILE_COLOR, Color.WHITE);
        // Highlight
        qsCustomHighlightTileColors = Xprefs.getBoolean(QS_TILE_HIGHLIGHT_CUSTOM_COLORS_SWITCH, false);
        qsActiveColorEnabledHighlight = Xprefs.getBoolean(QS_TILE_ACTIVE_COLOR_HIGHLIGHT_ENABLED, false);
        qsActiveColorHighlight = Xprefs.getInt(QS_TILE_ACTIVE_COLOR_HIGHLIGHT, Color.RED);
        qsInactiveColorEnabledHighlight = Xprefs.getBoolean(QS_TILE_INACTIVE_COLOR_HIGHLIGHT_ENABLED, false);
        qsInactiveColorHighlight = Xprefs.getInt(QS_TILE_INACTIVE_COLOR_HIGHLIGHT, Color.GRAY);
        qsDisabledColorEnabledHighlight = Xprefs.getBoolean(QS_TILE_DISABLED_COLOR_HIGHLIGHT_ENABLED, false);
        qsDisabledColorHighlight = Xprefs.getInt(QS_TILE_DISABLED_COLOR_HIGHLIGHT, Color.DKGRAY);
        // Base
        qsCustomTileColors = Xprefs.getBoolean(QS_TILE_CUSTOM_COLORS_SWITCH, false);
        qsActiveColorEnabled = Xprefs.getBoolean(QS_TILE_ACTIVE_COLOR_ENABLED, false);
        qsActiveColor = Xprefs.getInt(QS_TILE_ACTIVE_COLOR, Color.RED);
        qsInactiveColorEnabled = Xprefs.getBoolean(QS_TILE_INACTIVE_COLOR_ENABLED, false);
        qsInactiveColor = Xprefs.getInt(QS_TILE_INACTIVE_COLOR, Color.GRAY);
        qsDisabledColorEnabled = Xprefs.getBoolean(QS_TILE_DISABLED_COLOR_ENABLED, false);
        qsDisabledColor = Xprefs.getInt(QS_TILE_DISABLED_COLOR, Color.DKGRAY);

        // Qs Radius
        customHighlightTileRadius = Xprefs.getBoolean(QS_TILE_HIGHTLIGHT_RADIUS, false);
        highlightTSRadius = Xprefs.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT, 0);
        highlightTDRadius = Xprefs.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT, 0);
        highlightBSRadius = Xprefs.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT, 0);
        highlightBDRadius = Xprefs.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT, 0);
        customTileRadius = Xprefs.getBoolean(QS_TILE_RADIUS, false);
        tileTSRadius = Xprefs.getSliderInt(QS_TILE_RADIUS_TOP_LEFT, 0);
        tileTDRadius = Xprefs.getSliderInt(QS_TILE_RADIUS_TOP_RIGHT, 0);
        tileBSRadius = Xprefs.getSliderInt(QS_TILE_RADIUS_BOTTOM_LEFT, 0);
        tileBDRadius = Xprefs.getSliderInt(QS_TILE_RADIUS_BOTTOM_RIGHT, 0);

        if (Key.length > 0) {
            if (Key[0].equals(QS_WIDGETS_LIST)) {
                updateWidgets();
            }
            if (Key[0].equals(QS_PHOTO_RADIUS)) {
                updatePhotoRadius();
            }
            if (Key[0].equals(QS_WIDGETS_SWITCH)) {
                updateShowcaseMode();
            }
            if (Key[0].equals(QS_MEDIA_SHOW_ALBUM_ART) ||
                    Key[0].equals(QS_MEDIA_ART_FILTER) ||
                    Key[0].equals(QS_MEDIA_ART_BLUR_AMOUNT) ||
                    Key[0].equals(QS_MEDIA_ART_TINT_COLOR) ||
                    Key[0].equals(QS_MEDIA_ART_TINT_AMOUNT)) {
                updateMediaPlayerPrefs();
            }
            if (Key[0].equals(QS_TILE_INACTIVE_COLOR) ||
                    Key[0].equals(QS_TILE_INACTIVE_COLOR_ENABLED) ||
                    Key[0].equals(QS_TILE_ACTIVE_COLOR) ||
                    Key[0].equals(QS_TILE_ACTIVE_COLOR_ENABLED)) {
                updateTileColors(true);
            }
            if (Key[0].equals(QS_TILE_HIGHTLIGHT_RADIUS) ||
                    Key[0].equals(QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT) ||
                    Key[0].equals(QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT) ||
                    Key[0].equals(QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT) ||
                    Key[0].equals(QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT) ||
                    Key[0].equals(QS_TILE_RADIUS) ||
                    Key[0].equals(QS_TILE_RADIUS_TOP_LEFT) ||
                    Key[0].equals(QS_TILE_RADIUS_TOP_RIGHT) ||
                    Key[0].equals(QS_TILE_RADIUS_BOTTOM_LEFT) ||
                    Key[0].equals(QS_TILE_RADIUS_BOTTOM_RIGHT)) {
                updateTileShapes(true);
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        ReflectedClass OplusQSTileMediaContainer = ReflectedClass.of(
                "com.oplus.systemui.qs.OplusQSTileMediaContainer", /* OOS 14-15 */
                "com.oplusos.systemui.qs.OplusQSContainerImpl" /* OOS 13 */);

        OplusQSTileMediaContainer
                .afterConstruction()
                .run(param -> {
                    if (!mQsWidgetsEnabled) return;
                    setBooleanField(param.thisObject, "mIsMediaMode", true);
                });

        OplusQSTileMediaContainer
                .before("setMediaMode")
                .run(param -> {
                    if (!mQsWidgetsEnabled) return;
                    param.args[0] = true;
                });
        if (Build.VERSION.SDK_INT == 33) {
            forceMediaPanelA13();
        }

        ReflectedClass QSSecurityFooterUtilsClass = ReflectedClass.of(
                "com.android.systemui.qs.QSSecurityFooterUtils", /* OOS 14-15 */
                "com.android.systemui.qs.QSSecurityFooter" /* OOS 13 */
        );
        QSSecurityFooterUtilsClass
                .afterConstruction()
                .run(param -> mActivityStarter = getObjectField(param.thisObject, "mActivityStarter"));

        ReflectedClass OplusQsMediaPanelView = ReflectedClass.of(
                "com.oplus.systemui.qs.media.OplusQsBaseMediaPanelView", /* OOS16 */
                "com.oplus.systemui.qs.media.OplusQsMediaPanelView");
        if (Build.VERSION.SDK_INT >= 35) {
            ReflectedClass BaseQsViewBackgroundClz = ReflectedClass.ofIfPossible("com.oplus.systemui.qs.base.widget.BaseQsViewBackground");
            OplusQsMediaPanelView
                    .afterConstruction()
                    .run(param -> {
                        if (!mQsWidgetsEnabled) return;
                        try {
                            QsViewBackgroundProxy TransparentBackgroundProxy;
                            if (BaseQsViewBackgroundClz.getClazz() != null) {
                                TransparentBackgroundProxy = new StaticViewBackgroundProxyImplOC((QsStaticViewInfoProvider) param.thisObject);
                            } else {
                                TransparentBackgroundProxy = new StaticViewBackgroundProxyImplExOC((QsStaticViewInfoProvider) param.thisObject);
                            }
                            QsViewBackgroundProxy mBackgroundProxy = (QsViewBackgroundProxy) getObjectField(param.thisObject, "backgroundProxy");
                            mBackgroundProxy = TransparentBackgroundProxy;
                            setObjectField(param.thisObject, "backgroundProxy", mBackgroundProxy);
                        } catch (Throwable t) {
                            log(t);
                        }
                    });
        }
        OplusQsMediaPanelView
                .after("onFinishInflate")
                .run(param -> {
                    if (!mQsWidgetsEnabled) return;

                    mOplusQsMediaView = (ViewGroup) param.thisObject;
                    if (Build.VERSION.SDK_INT < 35) { // OOS 15
                        mDefaultMediaBg = mOplusQsMediaView.getBackground();
                        mOplusQsMediaView.setBackground(null);
                    }
                    mOplusQsMediaView.removeAllViews();

                    placeWidgets();
                });

        if (Build.VERSION.SDK_INT >= 35) {
            // Hook oplus view pager in split mode
            ReflectedClass OplusPanelViewPagerController = ReflectedClass.of("com.oplus.systemui.separate.OplusPanelViewPagerController");
            OplusPanelViewPagerController
                    .afterConstruction()
                    .run(param -> mOplusPanelPagerController = param.thisObject);
            OplusPanelViewPagerController
                    .before("onScrollX")
                    .run(param -> {
                        MotionEvent event = (MotionEvent) param.args[1];
                        try {
                            hookTouchHandler(param, event, "onScrollX");
                        } catch (Throwable ignored) {
                        }
                    });
            OplusPanelViewPagerController
                    .before("onScrollY")
                    .run(param -> {
                        MotionEvent event = (MotionEvent) param.args[1];
                        hookTouchHandler(param, event, "onScrollX");
                    });
            ReflectedClass TouchHandler = ReflectedClass.of("com.oplus.systemui.separate.OplusPanelViewPagerController$TouchHandler");
            TouchHandler
                    .before("onTouchEvent")
                    .run(param -> {
                        MotionEvent event = (MotionEvent) param.args[0];
                        hookTouchHandler(param, event, "onTouchEvent");
                    });
            TouchHandler
                    .before("onInterceptTouchEvent")
                    .run(param -> {
                        XposedBridge.log("QsWidgets: onInterceptTouchEvent");
                        MotionEvent event = (MotionEvent) param.args[0];
                        hookTouchHandler(param, event, "onInterceptTouchEvent");
                    });

            ReflectedClass OplusSeparateQSManager = ReflectedClass.ofIfPossible("com.oplus.systemui.separate.OplusSeparateQSManager");
            OplusSeparateQSManager
                    .before("collapseQSPanel")
                    .run(param -> {
                        if (!mQsWidgetsEnabled) return;

                        if (mIsTouchOnWidgets) {
                            param.setResult(null);
                        }
                    });
        }

    }

    private void hookTouchHandler(XC_MethodHook.MethodHookParam param, MotionEvent event, String methodName) {
        if (!mQsWidgetsEnabled) return;

        if (event != null && event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            log("QsWidgets - " + methodName + " - ACTION_OUTSIDE received, ignoring");
            param.setResult(false);
            return;
        }

        Object separateQSManager = getObjectField(mOplusPanelPagerController, "separateQSManager");
        boolean isQsFullyExpanded = (boolean) callMethod(separateQSManager, "isFullyExpanded");
        if (!QsStyleObserver.isSeparateStyle()) return;
        if (!isQsFullyExpanded) return;
        boolean isKeyguardVisible = (boolean) callMethod(mOplusPanelPagerController, "isKeyguardVisible");
        log("QsWidgets - " + methodName + " - isKeyguardVisible: " + isKeyguardVisible);
        if (isKeyguardVisible) return;
        if (mOplusQsMediaView == null) return;
        int[] location = new int[2];
        mOplusQsMediaView.getLocationOnScreen(location);
        Rect panelView = new Rect(location[0], location[1],
                location[0] + mOplusQsMediaView.getWidth(),
                location[1] + mOplusQsMediaView.getHeight());

        float x = event.getRawX();
        float y = event.getRawY();

        mOplusQsMediaView.getLocationOnScreen(location);
        boolean isTouchOnWidgets = x >= location[0] && x <= location[0] + mOplusQsMediaView.getWidth() &&
                y >= location[1] && y <= location[1] + mOplusQsMediaView.getHeight();

        ViewGroup parent = (ViewGroup) mOplusQsMediaView.getParent();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsTouchOnWidgets = isTouchOnWidgets;
                mDownX = x;
                mDownY = y;
                mIsDragging = false;

                if (mIsTouchOnWidgets) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            setDownExpandedNone();
                            parent.requestDisallowInterceptTouchEvent(true);

                            MotionEvent translatedEvent = MotionEvent.obtain(event);
                            translatedEvent.setLocation(x - location[0], y - location[1]);
                            QsControlsView.getInstance().getPager().dispatchTouchEvent(translatedEvent);
                            translatedEvent.recycle();
                        } catch (Throwable t) {
                            log(t);
                        }
                    });
                    param.setResult(true);
                    return;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsTouchOnWidgets) {
                    float dx = Math.abs(x - mDownX);
                    float dy = Math.abs(y - mDownY);

                    if (dx > TOUCH_SLOP || dy > TOUCH_SLOP) {
                        mIsDragging = true;
                    }

                    MotionEvent translatedEvent = MotionEvent.obtain(event);
                    translatedEvent.setLocation(x - location[0], y - location[1]);

                    if (dy > TOUCH_SLOP) {
                        // vertical scroll
                        translatedEvent.recycle();
                        param.setResult(true);
                        return;
                    } else {
                        QsControlsView.getInstance().getPager().dispatchTouchEvent(translatedEvent);
                    }

                    translatedEvent.recycle();
                    param.setResult(true);
                    return;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsTouchOnWidgets) {
                    float dx = Math.abs(x - mDownX);
                    float dy = Math.abs(y - mDownY);

                    if (!mIsDragging && dx < TOUCH_SLOP && dy < TOUCH_SLOP) {
                        // tap
                        MotionEvent translatedUp = MotionEvent.obtain(event);
                        translatedUp.setLocation(x - location[0], y - location[1]);
                        QsControlsView.getInstance().getPager().dispatchTouchEvent(translatedUp);
                        translatedUp.recycle();
                        QsControlsView.getInstance().getPager().performClick();
                    } else {
                        // swipe
                        MotionEvent translatedEvent = MotionEvent.obtain(event);
                        translatedEvent.setLocation(x - location[0], y - location[1]);
                        QsControlsView.getInstance().getPager().dispatchTouchEvent(translatedEvent);
                        translatedEvent.recycle();
                    }

                    parent.requestDisallowInterceptTouchEvent(false);
                    mIsTouchOnWidgets = false;
                    mIsDragging = false;

                    param.setResult(true);
                    return;
                }
                break;
        }
    }

    private void setDownExpandedNone() {
        if (Build.VERSION.SDK_INT != 36) return;
        try {
            Object downExpanded = getObjectField(mOplusPanelPagerController, "downExpanded");
            if (downExpanded != null) {
                Object noneValue = Enum.valueOf((Class<Enum>) downExpanded.getClass(), "NONE");
                setObjectField(mOplusPanelPagerController, "downExpanded", noneValue);
            }
        } catch (Exception e) {
            XposedBridge.log("QsWidgets: Error setting downExpanded: " + e);
        }
    }

    private void forceMediaPanelA13() {
        // Classes
        ReflectedClass OplusQSFooterImpl = ReflectedClass.of("com.oplusos.systemui.qs.OplusQSFooterImpl"); //1
        ReflectedClass OplusQSContainerImpl = ReflectedClass.of("com.oplusos.systemui.qs.OplusQSContainerImpl"); //2
        ReflectedClass QuickStatusBarHeader = ReflectedClass.of("com.android.systemui.qs.QuickStatusBarHeader");

        // Hooks
        ReflectedClass.ReflectionConsumer boolHook = param -> {
            if (!mQsWidgetsEnabled) return;
            try {
                setBooleanField(param.thisObject, "mIsMediaMode", true);
            } catch (Throwable t) {
                log("No boolean field mIsMediaMode: " + t.getMessage());
            }
        };

        ReflectedClass.ReflectionConsumer methodHook = param -> {
            if (!mQsWidgetsEnabled) return;
            param.args[0] = true;
        };

        // Constructors
        OplusQSFooterImpl.afterConstruction().run(boolHook);
        OplusQSContainerImpl.afterConstruction().run(boolHook);
        QuickStatusBarHeader.afterConstruction().run(boolHook);

        // Methods
        OplusQSContainerImpl.before("setQsMediaPanelShown").run(methodHook);
        OplusQSFooterImpl.before("setQsMediaPanelShown").run(methodHook);
        OplusQSFooterImpl.before("setMediaMode").run(methodHook);
        QuickStatusBarHeader.before("setQsMediaPanelShown").run(methodHook);
    }

    private void placeWidgets() {
        try {
            QsControlsView qsControlsView = QsControlsView.getInstance(mContext);
            try {
                ((ViewGroup) qsControlsView.getParent()).removeView(qsControlsView);
            } catch (Throwable ignored) {
            }
            mOplusQsMediaView.addView(qsControlsView, 0);
            qsControlsView.bringToFront();
            qsControlsView.requestLayout();
            updateControlsBg(qsControlsView, false);
            updateTileColors(false);
            updateTileShapes(false);
            updateWidgets();
            updateMediaPlayerPrefs();
            updatePhotoRadius();
            updateShowcaseMode();
        } catch (Throwable t) {
            log("Error while placing widgets: " + Log.getStackTraceString(t));
        }
    }

    private void updateWidgets() {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        if (qsControlsView != null) {
            qsControlsView.updateWidgets(mQsWidgetsList);
        }
    }

    private void updateMediaPlayerPrefs() {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        if (qsControlsView != null) {
            qsControlsView.updateMediaPlayerPrefs(showMediaArtMediaQs, mMediaQsArtFilter, mMediaQsTintColor, mMediaQsTintAmount, mMediaQsArtBlurAmount);
        }
    }

    private void updateControlsBg(QsControlsView qsControlsView, boolean force) {
        if (qsControlsView != null) {
            qsControlsView.updateDefaultMediaBg(mDefaultMediaBg, force);
        }
    }

    private void updateTileColors(boolean force) {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        if (qsControlsView != null) {
            qsControlsView.updateQsTileColors(
                    // Media
                    qsCustomMediaTileColor,
                    qsMediaTileColor,
                    // Highlight
                    qsCustomHighlightTileColors,
                    qsActiveColorEnabledHighlight,
                    qsInactiveColorEnabledHighlight,
                    qsActiveColorHighlight,
                    qsInactiveColorHighlight,
                    // Base
                    qsCustomTileColors,
                    qsActiveColorEnabled,
                    qsInactiveColorEnabled,
                    qsActiveColor,
                    qsInactiveColor,
                    force);
        }
    }

    private void updateTileShapes(boolean force) {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        if (qsControlsView != null) {
            qsControlsView.updateTileShapes(customHighlightTileRadius,
                    new float[]{
                            dp2px(mContext, highlightTSRadius),
                            dp2px(mContext, highlightTSRadius),
                            dp2px(mContext, highlightTDRadius),
                            dp2px(mContext, highlightTDRadius),
                            dp2px(mContext, highlightBDRadius),
                            dp2px(mContext, highlightBDRadius),
                            dp2px(mContext, highlightBSRadius),
                            dp2px(mContext, highlightBSRadius)},
                    customTileRadius,
                    new float[]{
                            dp2px(mContext, tileTSRadius),
                            dp2px(mContext, tileTSRadius),
                            dp2px(mContext, tileTDRadius),
                            dp2px(mContext, tileTDRadius),
                            dp2px(mContext, tileBDRadius),
                            dp2px(mContext, tileBDRadius),
                            dp2px(mContext, tileBSRadius),
                            dp2px(mContext, tileBSRadius)},
                    force);
        }
    }

    private void updatePhotoRadius() {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        log("updatePhotoRadius: " + mQsPhotoRadius);
        if (qsControlsView != null) {
            qsControlsView.updatePhotoRadius(mQsPhotoRadius);
        }
    }

    private void updateShowcaseMode() {
        QsControlsView qsControlsView = QsControlsView.getInstance();
        if (qsControlsView != null) {
            qsControlsView.updateShowcase(mQsPhotoShowcase);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
