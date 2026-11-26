package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static android.R.attr.state_activated;
import static android.R.attr.state_checked;
import static android.R.attr.state_selected;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_STYLE_PACK;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_BORDER_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_BORDER_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_BORDER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_BORDER_WIDTH;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.QsStylePackUtil;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class QsStylePacks extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;

    private static final String[] TILE_DRAWABLES = {
            "com.oplus.systemui.qs.base.res.drawable.MixColorTileDrawable",
            "com.oplus.systemui.qs.base.res.drawable.GradientTileDrawable",
    };

    private static final String STATE_ACTIVE_ATTR = "state_active";

    private static final Set<String> OWN_PREFS = Set.of(
            QS_STYLE_PACK, QS_TILE_BORDER_SWITCH, QS_TILE_BORDER_WIDTH,
            QS_TILE_BORDER_COLOR, QS_TILE_BORDER_ACTIVE);

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<Class<?>, Method> mGetPath = new ConcurrentHashMap<>();
    private final Path mScratch = new Path();
    private final Matrix mMatrix = new Matrix();

    private volatile QsStylePackUtil.Values mValues = null;
    private volatile boolean mPending = false;
    private String mStyleKey = null;
    private int mStateActive = 0;
    private boolean mHooked = false;
    private boolean mReceiverRegistered = false;

    private LinearGradient mShader;
    private float mShaderTop, mShaderBottom;
    private int mShaderStart, mShaderEnd;

    private final BroadcastReceiver mPackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_USER_UNLOCKED.equals(intent.getAction())) {
                if (mPending) reload();
                return;
            }
            Uri data = intent.getData();
            if (data == null || TextUtils.isEmpty(mStyleKey)) return;
            if (mStyleKey.startsWith(data.getSchemeSpecificPart() + "/")) reload();
        }
    };

    public QsStylePacks(Context context) {
        super(context);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public static boolean isSupported() {
        return Build.VERSION.SDK_INT >= 37;
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        if (Xprefs == null) return;
        if (Key.length > 0 && !OWN_PREFS.contains(Key[0])) return;

        mStyleKey = Xprefs.getString(QS_STYLE_PACK, "");
        reload();
    }

    private void reload() {
        if (Xprefs == null) return;
        mValues = resolve();
    }

    private QsStylePackUtil.Values resolve() {
        QsStylePackUtil.Values values = null;

        if (!TextUtils.isEmpty(mStyleKey)) {
            QsStylePackUtil.Style style = mContext == null ? null : QsStylePackUtil.loadStyle(mContext, mStyleKey);
            if (style != null) values = style.values;
            mPending = style == null;
        } else {
            mPending = false;
            if (Xprefs.getBoolean(QS_TILE_BORDER_SWITCH, false)) {
                values = new QsStylePackUtil.Values();
                values.borderWidthDp = Xprefs.getSliderInt(QS_TILE_BORDER_WIDTH, 1);
                int color = Xprefs.getInt(QS_TILE_BORDER_COLOR, QsStylePackUtil.DEFAULT_COLOR_DARK);
                values.borderColorDark = color;
                values.borderColorLight = color;
                values.borderActive = Xprefs.getBoolean(QS_TILE_BORDER_ACTIVE, false);
            }
        }

        return values != null && values.hasEdge() ? values : null;
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        if (!PRParam.getPackageName().equals(listenPackage)) return;
        if (!isSupported()) return;
        if (mHooked) return;
        mHooked = true;

        registerPackReceiver();

        for (String name : TILE_DRAWABLES) {
            ReflectedClass drawable = ReflectedClass.ofIfPossible(name);
            Class<?> clazz = drawable.getClazz();
            if (clazz == null) continue;
            Method getPath;
            try {
                getPath = clazz.getMethod("getPath");
            } catch (NoSuchMethodException e) {
                log(clazz.getSimpleName() + " has no getPath");
                continue;
            }
            mGetPath.put(clazz, getPath);
            drawable.after("draw")
                    .run(param -> {
                        if (mValues == null) return;
                        if (param.args.length != 1 || !(param.args[0] instanceof Canvas canvas))
                            return;
                        edge((Drawable) param.thisObject, canvas);
                    });
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerPackReceiver() {
        if (mReceiverRegistered || mContext == null) return;
        mContext.registerReceiver(mPackReceiver, new IntentFilter(Intent.ACTION_USER_UNLOCKED), Context.RECEIVER_EXPORTED);
        IntentFilter packages = new IntentFilter();
        packages.addAction(Intent.ACTION_PACKAGE_ADDED);
        packages.addAction(Intent.ACTION_PACKAGE_REPLACED);
        packages.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packages.addDataScheme("package");
        mContext.registerReceiver(mPackReceiver, packages, Context.RECEIVER_EXPORTED);
        mReceiverRegistered = true;
    }

    private void edge(Drawable drawable, Canvas canvas) {
        QsStylePackUtil.Values values = mValues;
        if (values == null) return;

        Rect bounds = drawable.getBounds();
        if (bounds.isEmpty()) return;
        if (!values.borderActive && isActive(drawable)) return;

        Path shape = tilePath(drawable);
        if (shape == null) return;

        boolean night = (mContext.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        float density = mContext.getResources().getDisplayMetrics().density;

        mPaint.setStrokeWidth(Math.max(1f, values.borderWidthDp * density));
        int top = values.color(night);
        int bottom = values.colorBottom(night);
        if (Color.alpha(bottom) != 0) {
            mPaint.setShader(shader(bounds.top, bounds.bottom, top, bottom));
            mPaint.setColor(Color.WHITE);
        } else {
            mPaint.setShader(null);
            mPaint.setColor(top);
        }

        canvas.drawPath(inset(shape, values.borderInsetDp * density, bounds), mPaint);
    }

    private LinearGradient shader(float top, float bottom, int start, int end) {
        if (mShader == null || mShaderTop != top || mShaderBottom != bottom
                || mShaderStart != start || mShaderEnd != end) {
            mShader = new LinearGradient(0, top, 0, bottom, start, end, Shader.TileMode.CLAMP);
            mShaderTop = top;
            mShaderBottom = bottom;
            mShaderStart = start;
            mShaderEnd = end;
        }
        return mShader;
    }

    private Path inset(Path shape, float inset, Rect bounds) {
        if (inset <= 0f) return shape;
        float scaleX = (bounds.width() - 2 * inset) / bounds.width();
        float scaleY = (bounds.height() - 2 * inset) / bounds.height();
        if (scaleX <= 0f || scaleY <= 0f) return shape;
        mMatrix.setScale(scaleX, scaleY, bounds.exactCenterX(), bounds.exactCenterY());
        mScratch.rewind();
        shape.transform(mMatrix, mScratch);
        return mScratch;
    }

    private Path tilePath(Drawable drawable) {
        Method getPath = mGetPath.get(drawable.getClass());
        if (getPath == null) return null;
        try {
            Object path = getPath.invoke(drawable);
            return path instanceof Path p && !p.isEmpty() ? p : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @SuppressLint("DiscouragedApi")
    private boolean isActive(Drawable drawable) {
        int[] state = drawable.getState();
        if (state == null || state.length == 0) return false;
        if (mStateActive == 0) {
            mStateActive = mContext.getResources().getIdentifier(
                    STATE_ACTIVE_ATTR, "attr", mContext.getPackageName());
        }
        for (int entry : state) {
            if (mStateActive != 0 && entry == mStateActive) return true;
            if (entry == state_selected || entry == state_activated || entry == state_checked)
                return true;
        }
        return false;
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
