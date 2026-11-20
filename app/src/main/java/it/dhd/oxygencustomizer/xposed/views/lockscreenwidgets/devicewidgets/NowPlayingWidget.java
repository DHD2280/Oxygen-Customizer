package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.app.WallpaperColors;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.LiquidGlass;

@SuppressLint("ViewConstructor")
public class NowPlayingWidget extends BaseDeviceWidget {

    private static final int CORNER_DP = 18;
    private static final int ICON_DP = 22;
    private static final int FILTER_NONE = 0;
    private static final int FILTER_GRAYSCALE = 1;
    private static final int FILTER_TINT = 2;
    private static final int FILTER_BLUR = 3;
    private static final int FILTER_GRAY_BLUR = 4;
    private static final float MAX_BLUR_RADIUS = 25f;
    private static final String KEY_SHOW_ART = "show_art";
    private static final String KEY_FILTER = "art_filter";
    private static final String KEY_BLUR = "blur_amount";
    private static final String KEY_TINT = "tint_amount";
    private static final String KEY_DIM = "dim_amount";
    private static final String KEY_RIM_ART = "rim_art";
    private static final String KEY_SWIPE = "swipe_tracks";
    private static final String KEY_IDLE_ART = "idle_art";
    private static final int SWIPE_THRESHOLD_DP = 40;
    private static final int SWIPE_VELOCITY_DP = 100;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private static final int MIN_PADDING_DP = 10;
    private static final int MIN_HEIGHT_DP = 52;
    private static final int SAMPLE_SIZE = 256;
    private static Bitmap sSampleArt;

    private FrameLayout mCard;
    private ImageView mArt;
    private View mScrim;
    private LinearLayout mContent;
    private final boolean mPreview;
    private final ImageView mIcon;
    private final TextView mTitle;
    private final TextView mArtist;
    private MediaController mController;
    private final List<MediaController> mWatched = new ArrayList<>();
    private boolean mListening = false;

    private boolean mShowArt = true;
    private int mFilter = FILTER_NONE;
    private int mBlur = 30;
    private int mTint = 40;
    private int mDim = 40;
    private boolean mRimArt = false;
    private int mRimColor = Color.TRANSPARENT;
    private Bitmap mRimSource;
    private boolean mColorsListening = false;
    private boolean mSwipe = true;
    private boolean mIdleArt = true;
    private boolean mShowingSample = false;
    private GestureDetector mSwipeDetector;

    private final AudioDataProvider.MediaMetadataListener mColorsListener = new AudioDataProvider.MediaMetadataListener() {
        @Override
        public void onMediaMetadataChanged() {
        }

        @Override
        public void onPlaybackStateChanged() {
        }

        @Override
        public void onMediaColorsChanged(int mediaColor, WallpaperColors wallpaperColors, Object colorScheme) {
            mHandler.post(() -> updateRim(mRimSource));
        }
    };
    private Bitmap mSourceArt;
    private String mFilteredKey;
    private int mRequest = 0;

    private final MediaSessionManager.OnActiveSessionsChangedListener mSessionsListener = controllers -> mHandler.post(this::refresh);

    private final MediaController.Callback mControllerCallback = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(@Nullable MediaMetadata metadata) {
            mHandler.post(NowPlayingWidget.this::render);
        }

        @Override
        public void onPlaybackStateChanged(@Nullable PlaybackState state) {
            mHandler.post(NowPlayingWidget.this::refresh);
        }

        @Override
        public void onSessionDestroyed() {
            mHandler.post(NowPlayingWidget.this::refresh);
        }
    };

    public NowPlayingWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mPreview = settingsInterface || BuildConfig.APPLICATION_ID.equals(context.getPackageName());
        mCard = new FrameLayout(mContext) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int[] size = measureCard(widthMeasureSpec, heightMeasureSpec);
                setMeasuredDimension(size[0], size[1]);
            }
        };
        mCard.setClipToOutline(true);
        mCard.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp2px(mContext, CORNER_DP));
            }
        });
        mArt = new ImageView(mContext);
        mArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mCard.addView(mArt, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        mScrim = new View(mContext);
        mCard.addView(mScrim, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        mContent = new LinearLayout(mContext);
        mContent.setMinimumHeight(dp2px(mContext, MIN_HEIGHT_DP));
        int padding = dp2px(mContext, MIN_PADDING_DP);
        mContent.setPadding(padding, padding, padding, padding);
        mContent.setOrientation(HORIZONTAL);
        mContent.setGravity(Gravity.CENTER_VERTICAL);
        mIcon = new ImageView(mContext);
        mContent.addView(mIcon, new LayoutParams(dp2px(mContext, ICON_DP), dp2px(mContext, ICON_DP)));
        LinearLayout texts = new LinearLayout(mContext);
        texts.setOrientation(VERTICAL);
        mTitle = text(14, true);
        mArtist = text(12, false);
        texts.addView(mTitle);
        texts.addView(mArtist);
        LayoutParams textParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        textParams.setMarginStart(dp2px(mContext, 10));
        mContent.addView(texts, textParams);
        mCard.addView(mContent, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        addView(mCard, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mSwipeDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                if (e1 != null && Math.abs(e2.getX() - e1.getX()) > Math.abs(e2.getY() - e1.getY()) && getParent() != null)
                    getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null) return false;
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) < dp2px(mContext, SWIPE_THRESHOLD_DP) || Math.abs(diffX) < Math.abs(e2.getY() - e1.getY()) || Math.abs(velocityX) < dp2px(mContext, SWIPE_VELOCITY_DP))
                    return false;
                return skipTrack(diffX < 0);
            }
        });
        render();
    }

    private boolean skipTrack(boolean next) {
        if (mPreview || mController == null) return false;
        if (next) mController.getTransportControls().skipToNext();
        else mController.getTransportControls().skipToPrevious();
        performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSwipe && !mPreview && mSwipeDetector != null && mSwipeDetector.onTouchEvent(event)) {
            MotionEvent cancel = MotionEvent.obtain(event);
            cancel.setAction(MotionEvent.ACTION_CANCEL);
            super.onTouchEvent(cancel);
            cancel.recycle();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private int[] measureCard(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int contentHeightSpec = heightMode == MeasureSpec.EXACTLY
                ? heightMeasureSpec
                : MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), heightMode == MeasureSpec.UNSPECIFIED ? MeasureSpec.UNSPECIFIED : MeasureSpec.AT_MOST);
        mContent.measure(widthMeasureSpec, contentHeightSpec);
        int width = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(widthMeasureSpec) : mContent.getMeasuredWidth();
        int height = heightMode == MeasureSpec.EXACTLY ? MeasureSpec.getSize(heightMeasureSpec) : mContent.getMeasuredHeight();
        int exactWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int exactHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        mArt.measure(exactWidth, exactHeight);
        mScrim.measure(exactWidth, exactHeight);
        mContent.measure(exactWidth, exactHeight);
        return new int[]{width, height};
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(0, 0, 0, 0);
        if (mContent == null) return;
        int min = dp2px(mContext, MIN_PADDING_DP);
        mContent.setPadding(Math.max(left, min), Math.max(top, min), Math.max(right, min), Math.max(bottom, min));
    }

    private static Bitmap sampleArt(int accent) {
        if (sSampleArt != null) return sSampleArt;
        Bitmap bitmap = Bitmap.createBitmap(SAMPLE_SIZE, SAMPLE_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new LinearGradient(0, 0, SAMPLE_SIZE, SAMPLE_SIZE, accent, 0xFFE85D75, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, SAMPLE_SIZE, SAMPLE_SIZE, paint);
        paint.setShader(null);
        paint.setColor(0x66FFFFFF);
        canvas.drawCircle(SAMPLE_SIZE * 0.72f, SAMPLE_SIZE * 0.3f, SAMPLE_SIZE * 0.22f, paint);
        paint.setColor(0x55141E3C);
        canvas.drawCircle(SAMPLE_SIZE * 0.25f, SAMPLE_SIZE * 0.78f, SAMPLE_SIZE * 0.3f, paint);
        sSampleArt = bitmap;
        return bitmap;
    }

    private TextView text(int size, boolean bold) {
        TextView view = new TextView(mContext);
        view.setTextSize(size);
        view.setSingleLine(true);
        view.setEllipsize(TextUtils.TruncateAt.END);
        if (bold) view.setTypeface(Typeface.DEFAULT_BOLD);
        else view.setAlpha(0.85f);
        return view;
    }

    private MediaSessionManager sessions() {
        return mContext.getSystemService(MediaSessionManager.class);
    }

    private void refresh() {
        List<MediaController> controllers = Collections.emptyList();
        try {
            MediaSessionManager manager = sessions();
            if (manager != null) controllers = manager.getActiveSessions(null);
        } catch (Throwable ignored) {
        }
        MediaController next = null;
        MediaController current = null;
        for (MediaController controller : controllers) {
            PlaybackState state = controller.getPlaybackState();
            if (next == null && state != null && state.getState() == PlaybackState.STATE_PLAYING)
                next = controller;
            if (current == null && sameSession(controller)) current = controller;
        }
        if (next == null)
            next = current != null ? current : (controllers.isEmpty() ? null : controllers.get(0));
        watch(controllers);
        mController = next;
        render();
    }

    private void watch(List<MediaController> controllers) {
        List<MediaController> keep = new ArrayList<>();
        for (MediaController watched : mWatched) {
            boolean active = false;
            for (MediaController controller : controllers) {
                if (watched.getSessionToken().equals(controller.getSessionToken())) {
                    active = true;
                    break;
                }
            }
            if (active) keep.add(watched);
            else watched.unregisterCallback(mControllerCallback);
        }
        for (MediaController controller : controllers) {
            boolean known = false;
            for (MediaController watched : keep) {
                if (watched.getSessionToken().equals(controller.getSessionToken())) {
                    known = true;
                    break;
                }
            }
            if (!known) {
                controller.registerCallback(mControllerCallback, mHandler);
                keep.add(controller);
            }
        }
        mWatched.clear();
        mWatched.addAll(keep);
    }

    private boolean sameSession(MediaController next) {
        if (mController == null || next == null) return mController == next;
        return mController.getSessionToken().equals(next.getSessionToken());
    }

    private void render() {
        MediaMetadata metadata = mPreview || mController == null ? null : mController.getMetadata();
        String title;
        String artist;
        Bitmap art = null;
        if (mPreview) {
            title = "Song title";
            artist = "Artist";
            art = sampleArt(getPrimaryColor(mContext));
        } else if (metadata == null) {
            title = appContext.getString(R.string.now_playing_none);
            artist = "";
            if (mIdleArt) art = sampleArt(getPrimaryColor(mContext));
        } else {
            title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
            if (TextUtils.isEmpty(artist))
                artist = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
            art = artwork(metadata);
        }
        mTitle.setText(title);
        mArtist.setText(artist);
        mArtist.setVisibility(TextUtils.isEmpty(artist) ? GONE : VISIBLE);
        mShowingSample = art != null && art == sSampleArt;
        showArt(mShowArt ? art : null);
        updateRim(art);
    }

    private void updateRim(Bitmap art) {
        mRimSource = art;
        if (!mRimArt || art == null || Build.VERSION.SDK_INT < 37) {
            setRimColor(Color.TRANSPARENT);
        } else if (mPreview || mShowingSample) {
            setRimColor(getPrimaryColor(mContext));
        } else {
            AudioDataProvider provider = AudioDataProvider.instance;
            setRimColor(provider != null ? provider.getMediaColor() : Color.TRANSPARENT);
        }
    }

    private void syncColorsListener(boolean attached) {
        boolean wanted = mRimArt && !mPreview && attached && AudioDataProvider.instance != null && Build.VERSION.SDK_INT >= 37;
        if (wanted == mColorsListening) return;
        try {
            if (wanted) AudioDataProvider.registerMediaMetadataListener(mColorsListener);
            else AudioDataProvider.unregisterMediaMetadataListener(mColorsListener);
            mColorsListening = wanted;
        } catch (Throwable ignored) {
        }
    }

    private void setRimColor(int color) {
        mRimColor = color;
        LiquidGlass.setRimColor(this, color);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        LiquidGlass.drawRim(this, canvas);
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
        LiquidGlass.setRimColor(this, mRimColor);
    }

    private void showArt(Bitmap art) {
        boolean hasArt = art != null;
        int textColor = hasArt ? Color.WHITE : mTextColor;
        mTitle.setTextColor(textColor);
        mArtist.setTextColor(textColor);
        mIcon.setImageDrawable(tintedIcon(R.drawable.ic_widget_music));
        if (mIcon.getDrawable() != null) mIcon.getDrawable().setTint(textColor);
        mIcon.setVisibility(hasArt ? GONE : VISIBLE);
        if (!hasArt) {
            mSourceArt = null;
            mFilteredKey = null;
            mArt.setImageDrawable(null);
            mScrim.setBackground(null);
            return;
        }
        mScrim.setBackground(scrim());
        String key = System.identityHashCode(art) + ":" + mFilter + ":" + mBlur + ":" + mTint;
        if (art == mSourceArt && Objects.equals(key, mFilteredKey)) return;
        mSourceArt = art;
        mFilteredKey = key;
        int request = ++mRequest;
        int filter = mFilter;
        float radius = mBlur / 100f * MAX_BLUR_RADIUS;
        int tint = mTint;
        int accent = getPrimaryColor(mContext);
        EXECUTOR.execute(() -> {
            Bitmap filtered;
            try {
                filtered = filtered(art, filter, radius, tint, accent);
            } catch (Throwable t) {
                filtered = art;
            }
            Bitmap result = filtered;
            mHandler.post(() -> {
                if (request == mRequest) mArt.setImageBitmap(result);
            });
        });
    }

    private Bitmap filtered(Bitmap art, int filter, float radius, int tint, int accent) {
        return switch (filter) {
            case FILTER_GRAYSCALE -> DrawableConverter.toGrayscale(art);
            case FILTER_TINT ->
                    DrawableConverter.getColoredBitmap(new BitmapDrawable(appContext.getResources(), art), accent, tint);
            case FILTER_BLUR -> DrawableConverter.getBlurredImage(mContext, art, radius);
            case FILTER_GRAY_BLUR ->
                    DrawableConverter.getGrayscaleBlurredImage(mContext, art, radius);
            default -> art;
        };
    }

    private GradientDrawable scrim() {
        int alpha = Math.round(255 * mDim / 100f);
        int bottom = Color.argb(Math.min(255, alpha + 60), 0, 0, 0);
        int top = Color.argb(alpha / 2, 0, 0, 0);
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{top, bottom});
    }

    private static Bitmap artwork(@NonNull MediaMetadata metadata) {
        for (String key : new String[]{MediaMetadata.METADATA_KEY_ALBUM_ART, MediaMetadata.METADATA_KEY_ART, MediaMetadata.METADATA_KEY_DISPLAY_ICON}) {
            Bitmap bitmap = metadata.getBitmap(key);
            if (bitmap != null) return bitmap;
        }
        return null;
    }

    @Override
    protected boolean onWidgetTap() {
        if (mPreview || mController == null) return false;
        PlaybackState state = mController.getPlaybackState();
        if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
            mController.getTransportControls().pause();
        } else {
            mController.getTransportControls().play();
        }
        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        syncColorsListener(true);
        if (mPreview) return;
        if (!mListening) {
            try {
                MediaSessionManager manager = sessions();
                if (manager != null) {
                    manager.addOnActiveSessionsChangedListener(mSessionsListener, null, mHandler);
                    mListening = true;
                }
            } catch (Throwable ignored) {
            }
        }
        refresh();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        syncColorsListener(false);
        if (mListening) {
            try {
                MediaSessionManager manager = sessions();
                if (manager != null)
                    manager.removeOnActiveSessionsChangedListener(mSessionsListener);
            } catch (Throwable ignored) {
            }
            mListening = false;
        }
        for (MediaController watched : mWatched) watched.unregisterCallback(mControllerCallback);
        mWatched.clear();
        mController = null;
    }

    @Override
    public String getWidgetReference() {
        return getClass().getSimpleName();
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.now_playing_widget);
    }

    @Override
    public boolean hasBigMode() {
        return true;
    }

    @Override
    public boolean hasSmallMode() {
        return false;
    }

    @Override
    public View getBigPreview() {
        return new NowPlayingWidget(mContext, true);
    }

    @Override
    public View getSmallPreview() {
        return null;
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        List<SettingItem> settings = new ArrayList<>();
        settings.add(new SettingItem(appContext.getString(R.string.media_art_title), KEY_SHOW_ART, SettingItem.Type.BOOLEAN, mShowArt));
        List<String> filters = new ArrayList<>();
        filters.add(appContext.getString(R.string.albumart_filter_none));
        filters.add(appContext.getString(R.string.albumart_filter_grayscale));
        filters.add(appContext.getString(R.string.albumart_filter_tint));
        filters.add(appContext.getString(R.string.albumart_filter_blur));
        filters.add(appContext.getString(R.string.albumart_filter_grayblur));
        List<String> filterValues = new ArrayList<>();
        for (int i = FILTER_NONE; i <= FILTER_GRAY_BLUR; i++) filterValues.add(String.valueOf(i));
        settings.add(new SettingItem(appContext.getString(R.string.albumart_filter_title), KEY_FILTER, SettingItem.Type.LIST, String.valueOf(mFilter), filters, filterValues));
        settings.add(new SettingItem(appContext.getString(R.string.albumart_blur_title), KEY_BLUR, SettingItem.Type.SEEKBAR_INT, mBlur, 10, 100));
        settings.add(new SettingItem(appContext.getString(R.string.albumart_tint_intensity_title), KEY_TINT, SettingItem.Type.SEEKBAR_INT, mTint, 10, 90));
        settings.add(new SettingItem(appContext.getString(R.string.now_playing_dim), KEY_DIM, SettingItem.Type.SEEKBAR_INT, mDim, 0, 80));
        settings.add(new SettingItem(appContext.getString(R.string.now_playing_idle_art), KEY_IDLE_ART, SettingItem.Type.BOOLEAN, mIdleArt));
        settings.add(new SettingItem(appContext.getString(R.string.now_playing_swipe), KEY_SWIPE, SettingItem.Type.BOOLEAN, mSwipe));
        if (Build.VERSION.SDK_INT >= 37)
            settings.add(new SettingItem(appContext.getString(R.string.now_playing_rim_art), KEY_RIM_ART, SettingItem.Type.BOOLEAN, mRimArt));
        return settings;
    }

    @Override
    public void applySettings(Map<String, Object> settings) {
        Object show = settings.get(KEY_SHOW_ART);
        if (show != null)
            mShowArt = show instanceof Boolean value ? value : Boolean.parseBoolean(String.valueOf(show));
        mFilter = intSetting(settings.get(KEY_FILTER), mFilter);
        mBlur = intSetting(settings.get(KEY_BLUR), mBlur);
        mTint = intSetting(settings.get(KEY_TINT), mTint);
        mDim = intSetting(settings.get(KEY_DIM), mDim);
        Object swipe = settings.get(KEY_SWIPE);
        if (swipe != null)
            mSwipe = swipe instanceof Boolean value ? value : Boolean.parseBoolean(String.valueOf(swipe));
        Object idle = settings.get(KEY_IDLE_ART);
        if (idle != null)
            mIdleArt = idle instanceof Boolean value ? value : Boolean.parseBoolean(String.valueOf(idle));
        Object rim = settings.get(KEY_RIM_ART);
        if (rim != null)
            mRimArt = rim instanceof Boolean value ? value : Boolean.parseBoolean(String.valueOf(rim));
        syncColorsListener(isAttachedToWindow());
        mFilteredKey = null;
        if (mTitle != null) render();
    }

    private static int intSetting(Object value, int fallback) {
        if (value == null) return fallback;
        if (value instanceof Number number) return number.intValue();
        try {
            return Math.round(Float.parseFloat(String.valueOf(value)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        if (mTitle != null) render();
    }

    @Override
    public void onSetDeviceName(String deviceName) {
    }
}
