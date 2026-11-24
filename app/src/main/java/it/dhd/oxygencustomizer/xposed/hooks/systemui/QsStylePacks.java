package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_STYLE_PACK;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_BORDER_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_BORDER_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_BORDER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_BORDER_WIDTH;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
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
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.QsStylePackUtil;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

/**
 * Draws the tile edge Quick Settings style packs ask for.
 * <p>
 * OxygenOS 17 draws its tile backgrounds in code, so the edge is painted after the ROM
 * has drawn the tile. The drawable itself is left alone on purpose: it carries a blur
 * layer and its own path clipping, and anything that wraps it loses both.
 */
public class QsStylePacks extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;

    private static final String[] TILE_DRAWABLES = {
            "com.oplus.systemui.qs.base.res.drawable.MixColorTileDrawable",
            "com.oplus.systemui.qs.base.res.drawable.GradientTileDrawable",
    };

    /** OPlus declares its own state attribute instead of reusing the framework ones. */
    private static final long RETRY_MS = 2000L;

    private static final String STATE_ACTIVE_ATTR = "state_active";
    private static final int STATE_SELECTED = android.R.attr.state_selected;
    private static final int STATE_ACTIVATED = android.R.attr.state_activated;
    private static final int STATE_CHECKED = android.R.attr.state_checked;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<Class<?>, Field> mPathField = new ConcurrentHashMap<>();
    private final Map<Class<?>, Field> mInnerPath = new ConcurrentHashMap<>();
    private final Path mScratch = new Path();
    private final Matrix mMatrix = new Matrix();

    private static final Set<String> OWN_PREFS = new HashSet<>(Arrays.asList(
            QS_STYLE_PACK, QS_TILE_BORDER_SWITCH, QS_TILE_BORDER_WIDTH,
            QS_TILE_BORDER_COLOR, QS_TILE_BORDER_ACTIVE));

    private volatile QsStylePackUtil.Values mValues = null;
    private volatile boolean mPending = false;
    private long mLastRetry = 0L;
    private String mStyleKey = null;
    private int mStateActive = 0;
    private boolean mHooked = false;

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
        mValues = resolve();
    }

    /** The style in force, from the selected pack if there is one, else from the prefs. */
    private QsStylePackUtil.Values resolve() {
        QsStylePackUtil.Values values = null;

        if (!TextUtils.isEmpty(mStyleKey)) {
            QsStylePackUtil.Style style = QsStylePackUtil.loadStyle(mContext, mStyleKey);
            if (style != null) values = style.values;
            // SystemUI starts before the user unlocks, and a pack that is not direct boot
            // aware cannot be queried until then, so an empty result is retried later.
            mPending = style == null;
        } else if (Xprefs.getBoolean(QS_TILE_BORDER_SWITCH, false)) {
            mPending = false;
            values = new QsStylePackUtil.Values();
            values.borderWidthDp = Xprefs.getSliderInt(QS_TILE_BORDER_WIDTH, 1);
            int color = Xprefs.getInt(QS_TILE_BORDER_COLOR, QsStylePackUtil.DEFAULT_COLOR_DARK);
            values.borderColorDark = color;
            values.borderColorLight = color;
            values.borderActive = Xprefs.getBoolean(QS_TILE_BORDER_ACTIVE, false);
        } else {
            mPending = false;
        }

        return values != null && values.hasEdge() ? values : null;
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        if (!PRParam.getPackageName().equals(listenPackage)) return;
        if (!isSupported()) return;
        if (mHooked) return;
        mHooked = true;

        for (String name : TILE_DRAWABLES) {
            ReflectedClass.ofIfPossible(name)
                    .after("draw")
                    .run(param -> {
                        if (mValues == null && !retry()) return;
                        if (!(param.thisObject instanceof Drawable)) return;
                        if (param.args.length != 1 || !(param.args[0] instanceof Canvas)) return;
                        try {
                            edge((Drawable) param.thisObject, (Canvas) param.args[0]);
                        } catch (Throwable t) {
                            log(t);
                        }
                    });
        }
    }

    /** Resolve a pack that was not reachable when the preference was read. */
    private boolean retry() {
        if (!mPending) return false;
        long now = SystemClock.uptimeMillis();
        if (now - mLastRetry < RETRY_MS) return false;
        mLastRetry = now;
        mValues = resolve();
        return mValues != null;
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
        float width = Math.max(1f, values.borderWidthDp * density);

        mPaint.setStrokeWidth(width);
        int top = values.color(night);
        int bottom = values.colorBottom(night);
        if (Color.alpha(bottom) != 0) {
            mPaint.setShader(new LinearGradient(0, bounds.top, 0, bounds.bottom,
                    top, bottom, Shader.TileMode.CLAMP));
            mPaint.setColor(Color.WHITE);
        } else {
            mPaint.setShader(null);
            mPaint.setColor(top);
        }

        canvas.drawPath(inset(shape, values.borderInsetDp * density, bounds), mPaint);
    }

    /**
     * A path has no inset, so pull the shape towards its own centre instead. The stroke
     * is centred on the path, so half of it already sits inside the tile.
     */
    private Path inset(Path shape, float inset, Rect bounds) {
        if (inset <= 0f || bounds.width() <= 0 || bounds.height() <= 0) return shape;
        float scaleX = Math.max(0f, bounds.width() - 2 * inset) / bounds.width();
        float scaleY = Math.max(0f, bounds.height() - 2 * inset) / bounds.height();
        if (scaleX <= 0f || scaleY <= 0f) return shape;
        mMatrix.setScale(scaleX, scaleY, bounds.exactCenterX(), bounds.exactCenterY());
        mScratch.rewind();
        shape.transform(mMatrix, mScratch);
        return mScratch;
    }

    /**
     * The shape the ROM drew with, which is the only thing that knows the tile shape the
     * user picked. Neither the drawable outline nor the view outline reports a usable
     * radius for the squircles.
     */
    private Path tilePath(Drawable drawable) {
        Class<?> type = drawable.getClass();
        Field field = mPathField.get(type);
        if (field == null) {
            for (Class<?> c = type; c != null && field == null; c = c.getSuperclass()) {
                try {
                    field = c.getDeclaredField("oplusPath");
                    field.setAccessible(true);
                } catch (NoSuchFieldException ignored) {
                }
            }
            if (field == null) {
                log(type.getSimpleName() + " has no oplusPath field");
                return null;
            }
            mPathField.put(type, field);
        }
        try {
            Object value = field.get(drawable);
            if (value == null) return null;
            if (value instanceof Path) return ((Path) value).isEmpty() ? null : (Path) value;

            // com.oplus.graphics.OplusPath is not a Path, it wraps one.
            Field inner = mInnerPath.get(value.getClass());
            if (inner == null) {
                for (Field candidate : value.getClass().getDeclaredFields()) {
                    if (Path.class.isAssignableFrom(candidate.getType())) {
                        candidate.setAccessible(true);
                        inner = candidate;
                        break;
                    }
                }
                if (inner == null) {
                    log(value.getClass().getName() + " exposes no Path field");
                    return null;
                }
                mInnerPath.put(value.getClass(), inner);
            }
            Object path = inner.get(value);
            return path instanceof Path && !((Path) path).isEmpty() ? (Path) path : null;
        } catch (Throwable t) {
            log(t);
            return null;
        }
    }

    private boolean isActive(Drawable drawable) {
        int[] state = drawable.getState();
        if (state == null || state.length == 0) return false;
        if (mStateActive == 0) {
            mStateActive = mContext.getResources().getIdentifier(
                    STATE_ACTIVE_ATTR, "attr", mContext.getPackageName());
        }
        for (int entry : state) {
            if (mStateActive != 0 && entry == mStateActive) return true;
            if (entry == STATE_SELECTED || entry == STATE_ACTIVATED || entry == STATE_CHECKED) return true;
        }
        return false;
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
