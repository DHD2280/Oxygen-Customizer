package it.dhd.oxygencustomizer.xposed.views.controls;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getArt;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.LaunchableImageView;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getMediaOutputDialogFactory;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getOplusQsMediaTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getIconLightColor;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.isMediaIconNeedUseLightColor;
import static it.dhd.oxygencustomizer.xposed.utils.QsTileHelper.getMediaPanelRadius;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.palette.graphics.Palette;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.MediaPlayerObserver;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.SettingsLibUtilsProvider;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ThemeEnabler;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.QsWidgets;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.utils.WidgetUtils;

public class QsMediaTile extends LinearLayout {

    public static final String DEFAULT_LABEL = "oplus_qs_media_panel_title_default";
    public static final String APP_ICON = "ic_music_note";
    public static final String DEVICE_ICON = "status_bar_qs_bt_cellphone_ic";
    public static final String DEVICE_SWITCH_ICON = "status_bar_qs_bt_device_switch";
    public static final String PREV_ICON = "ic_oplus_media_panel_action_pre";
    public static final String PLAY_ICON = "ic_oplus_media_panel_action_play";
    public static final String PAUSE_ICON = "ic_oplus_media_panel_action_pause";
    public static final String NEXT_ICON = "ic_oplus_media_panel_action_next";

    private final Context mContext;
    private Context appContext;

    private Object mMediaData = null;

    private Drawable mAppIconDrawable;
    private Drawable mDeviceIconDrawable;
    private Drawable mSwitchDeviceIconDrawable;
    private String mDefaultTipText;
    private Drawable mPrevIconDrawable, mPlayIconDrawable, mPauseIconDrawable, mNextIconDrawable;

    private Drawable mOplusQsMediaDefaultBackground = null;

    private ColorStateList mTextColor = ColorStateList.valueOf(Color.WHITE);

    private ImageView mAppIcon, mDeviceIcon;
    private ImageView mPrev, mPlayPause, mNext;
    private TextView mTitle, mText;

    private final ActivityLauncherUtils mActivityLauncherUtils;

    // Qs Bg to match tiles
    private boolean qsInactiveColorEnabled = false;
    private int qsInactiveColor = Color.WHITE;
    private Drawable mOplusQsMediaDrawable = null;

    // Qs Media Tile Album Art
    private boolean showMediaArtMediaQs = false;
    private int mMediaQsArtFilter = 0, mMediaQsTintColor = Color.WHITE, mMediaQsTintAmount = 20;
    private float mMediaQsArtBlurAmount = 7.5f;
    private Bitmap mArt = null;
    private int mColorOnAlbum = Color.WHITE;

    private boolean mBound = false;

    public QsMediaTile(Context context) {
        super(context);

        mContext = context;
        try {
            appContext = context.createPackageContext(
                    BuildConfig.APPLICATION_ID,
                    Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, QsWidgets.mActivityStarter);

        mAppIconDrawable = WidgetUtils.getDrawable(mContext, APP_ICON, SYSTEM_UI);
        mDeviceIconDrawable = WidgetUtils.getDrawable(mContext, DEVICE_ICON, SYSTEM_UI);
        mSwitchDeviceIconDrawable = WidgetUtils.getDrawable(mContext, DEVICE_SWITCH_ICON, SYSTEM_UI);
        mDefaultTipText = WidgetUtils.getString(mContext, DEFAULT_LABEL, SYSTEM_UI);
        mPrevIconDrawable = WidgetUtils.getDrawable(mContext, PREV_ICON, SYSTEM_UI);
        mPlayIconDrawable = WidgetUtils.getDrawable(mContext, PLAY_ICON, SYSTEM_UI);
        mPauseIconDrawable = WidgetUtils.getDrawable(mContext, PAUSE_ICON, SYSTEM_UI);
        mNextIconDrawable = WidgetUtils.getDrawable(mContext, NEXT_ICON, SYSTEM_UI);

        inflateView();

        MediaPlayerObserver.registerMediaData(mMediaDataObserver);

        ThemeEnabler.registerThemeChangedListener(this::setupColors);

    }

    private final MediaPlayerObserver.OnBindMediaData mMediaDataObserver = new MediaPlayerObserver.OnBindMediaData() {
        @Override
        public void onBindMediaData(Object mediaData) {
            mMediaData = mediaData;
            if (mediaData == null) {
                setDefaultTip();
                hideMediaQsBackground();
                mBound = false;
                return;
            }
            mBound = true;
            setAppIcon(mediaData);
            setDeviceIcon(mediaData);
            setTitleAndText(mediaData);
            bindMediaAction(mediaData);
            updateBackground();
        }

        @Override
        public void onUnBindMediaData() {
            setDefaultTip();
            hideMediaQsBackground();
        }
    };

    private void setupColors() {

        mTextColor = SettingsLibUtilsProvider.getColorAttr(mContext.getResources().getIdentifier("couiColorPrimaryNeutral", "attr", SYSTEM_UI), mContext);
        post(() -> {
            if (mTextColor != null) {
                mDeviceIcon.setImageTintList(mTextColor);
                mTitle.setTextColor(mTextColor);
                mText.setTextColor(mTextColor);
                mPrev.setImageTintList(mTextColor);
                mPlayPause.setImageTintList(mTextColor);
                mNext.setImageTintList(mTextColor);
            }
            updateBackground();
        });
    }

    public void bindMediaAction(Object mediaData) {
        if (mediaData == null) {
            log("QsMediaTile bindMediaAction mediaData is null");
            return;
        }
        Object MediaButton = callMethod(mediaData, "getSemanticActions");
        Object prevOrCustom = MediaButton != null ? callMethod(MediaButton, "getPrevOrCustom") : null;
        Object playOrPause = MediaButton != null ? callMethod(MediaButton, "getPlayOrPause") : null;
        Object nextOrCustom = MediaButton != null ? callMethod(MediaButton, "getNextOrCustom") : null;

        setSemanticButton(mPrev, mPrevIconDrawable, prevOrCustom, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        setSemanticButton(mPlayPause, (boolean) callMethod(mediaData, "isPlaying") ? mPauseIconDrawable : mPlayIconDrawable, playOrPause, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        setSemanticButton(mNext, mNextIconDrawable, nextOrCustom, KeyEvent.KEYCODE_MEDIA_NEXT);

    }

    private void setSemanticButton(ImageView imageView, Drawable image, Object mediaAction, final int keycode) {
        if (imageView == null) {
            return;
        }

        imageView.setVisibility(VISIBLE);
        imageView.setImageDrawable(image);

        if (mediaAction == null) {
            imageView.setEnabled(true);
            imageView.setOnClickListener(v -> dispatchMediaKeyWithWakeLockToMediaSession(keycode));
            return;
        }

        imageView.setOnClickListener(view -> {
            handleSemanticButtonClick(mediaAction);
        });
    }

    private void handleSemanticButtonClick(Object mediaAction) {

        // Execute media action
        Runnable action = (Runnable) callMethod(mediaAction, "getAction");
        if (action != null) {
            action.run();
        }
    }


    private void dispatchMediaKeyWithWakeLockToMediaSession(final int keycode) {
        Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent keyEvent = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keycode, 0);
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        KeyEvent mediaEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keycode);
        SystemUtils.AudioManager().dispatchMediaKeyEvent(mediaEvent);

        mediaEvent = KeyEvent.changeAction(mediaEvent, KeyEvent.ACTION_UP);
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        SystemUtils.AudioManager().dispatchMediaKeyEvent(mediaEvent);

        vibrate(1);
    }

    private void setAppIcon(Object mediaData) {
        try {
            String packageName = (String) callMethod(mediaData, "getPackageName");
            Drawable icon = mContext.getPackageManager().getApplicationIcon(packageName);
            mAppIcon.setVisibility(VISIBLE);
            mAppIcon.setOnClickListener(v -> {
                try {
                    Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
                    if (intent != null) {
                        mActivityLauncherUtils.launchApp(intent);
                    }
                } catch (Throwable t) {
                    log("QsMediaTile setAppIcon onClick error " + t);
                }
            });
            if (icon != null) {
                mAppIcon.setImageDrawable(icon);
            } else {
                mAppIcon.setImageDrawable(mAppIconDrawable);
            }
        } catch (Throwable t) {
            log("QsMediaTile setAppIcon error " + t);
        }
    }

    private void setDeviceIcon(Object mediaData) {
        try {
            mDeviceIcon.setEnabled(true); // Enable button if media data is present

            Object MediaDeviceData = callMethod(mediaData, "getDevice");

            // Adjust alpha based on device state
            if (!showMediaArtMediaQs) {
                float alpha = (MediaDeviceData == null ||
                        !(boolean) callMethod(MediaDeviceData, "getEnabled") ||
                        (boolean) callMethod(mediaData, "getResumption")) ? 0.38f : 1.0f;
                mDeviceIcon.setAlpha(alpha);
            }

            // Set icon and content description
            if (MediaDeviceData != null) {
                mDeviceIcon.setImageDrawable((Drawable) callMethod(MediaDeviceData, "getIcon"));
            } else {
                mDeviceIcon.setImageDrawable(Build.VERSION.SDK_INT >= 35 ? mSwitchDeviceIconDrawable : mDeviceIconDrawable);
            }

            // Set color filter based on context
            mDeviceIcon.clearColorFilter();
            if (showMediaArtMediaQs) {
                mDeviceIcon.setAlpha(1.0f);
                mDeviceIcon.setColorFilter(mColorOnAlbum, PorterDuff.Mode.SRC_IN);
            } else {
                setupDeviceIconColor();
            }
        } catch (Throwable t) {
            log("QsMediaTile setDeviceIcon error " + t);
        }
    }

    private void setupDeviceIconColor() {
        if (isMediaIconNeedUseLightColor(mContext)) {
            mDeviceIcon.setColorFilter(getIconLightColor(), PorterDuff.Mode.SRC_IN);
        } else {
            mDeviceIcon.setColorFilter(
                    ContextCompat.getColor(mContext,
                            mContext.getResources().getIdentifier("selected_device_tint", "color", SYSTEM_UI)),
                    PorterDuff.Mode.SRC_IN);
        }
    }

    private void setTitleAndText(Object mediaData) {
        String song = (String) callMethod(mediaData, "getSong");
        String artist = (String) callMethod(mediaData, "getArtist");
        if (!TextUtils.isEmpty(song)) {
            mTitle.post(() -> mTitle.setText(song));
        }
        if (!TextUtils.isEmpty(artist)) {
            mText.post(() -> {
                mText.setVisibility(VISIBLE);
                mText.setText(artist);
            });
        }

    }

    private void setDefaultTip() {
        mBound = false;
        post(() -> {
            // App Icon
            mAppIcon.setVisibility(GONE);
            mAppIcon.setOnClickListener(null);
            // Device Icon
            mDeviceIcon.setVisibility(VISIBLE);
            mDeviceIcon.setImageDrawable(WidgetUtils.getDrawable(mContext, Build.VERSION.SDK_INT >= 35 ? DEVICE_SWITCH_ICON : DEVICE_ICON, SYSTEM_UI));
            setupDeviceIconColor();

            mTitle.setText(mDefaultTipText);
            mText.setVisibility(GONE);

            mPlayPause.setImageDrawable(WidgetUtils.getDrawable(mContext, PLAY_ICON, SYSTEM_UI));
            mPlayPause.setOnClickListener(v -> dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));

            setupOtherViews(mTextColor.getDefaultColor());
        });
    }

    private void inflateView() {
        inflate(appContext, R.layout.view_qs_media_tile, this);
        setupViews();
    }

    private void setupViews() {
        mAppIcon = (ImageView) ViewHelper.findViewWithTag(this, "app_icon");

        LinearLayout mDeviceParent = (LinearLayout) ViewHelper.findViewWithTag(this, "media_output_switch_btn_parent");
        try {
            mDeviceIcon = (ImageView) LaunchableImageView.getConstructor(Context.class).newInstance(mContext);
        } catch (Throwable t) {
            mDeviceIcon = new ImageView(mContext);
            log("QsMediaTile LaunchableImageView error " + t);
        }
        mDeviceIcon.setLayoutParams(mDeviceParent.getLayoutParams());
        mDeviceIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mDeviceParent.addView(mDeviceIcon);

        mTitle = (TextView) ViewHelper.findViewWithTag(this, "media_panel_title");
        mText = (TextView) ViewHelper.findViewWithTag(this, "media_panel_text");

        mPrev = (ImageView) ViewHelper.findViewWithTag(this, "media_panel_action_pre");
        mPlayPause = (ImageView) ViewHelper.findViewWithTag(this, "media_panel_action_play_or_pause");
        mNext = (ImageView) ViewHelper.findViewWithTag(this, "media_panel_action_next");

        mDeviceIcon.setOnClickListener(v -> showDeviceDialog());
        mPrev.setOnClickListener(v -> dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        mPlayPause.setOnClickListener(v -> dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        mNext.setOnClickListener(v -> dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_NEXT));

        mTextColor = SettingsLibUtilsProvider.getColorAttr(mContext.getResources().getIdentifier("couiColorPrimaryNeutral", "attr", SYSTEM_UI), mContext);

        post(() -> {
            if (mTextColor != null) {
                mDeviceIcon.setImageTintList(mTextColor);
                mTitle.setTextColor(mTextColor);
                mText.setTextColor(mTextColor);
                mPrev.setImageTintList(mTextColor);
                mPlayPause.setImageTintList(mTextColor);
                mNext.setImageTintList(mTextColor);
            }
            mAppIcon.setImageDrawable(mAppIconDrawable);
            mDeviceIcon.setImageDrawable(Build.VERSION.SDK_INT >= 35 ? mSwitchDeviceIconDrawable : mDeviceIconDrawable);

            mTitle.setText(mDefaultTipText);
            mText.setVisibility(GONE);

            mPrev.setImageDrawable(mPrevIconDrawable);
            mPlayPause.setImageDrawable(mPlayIconDrawable);
            mNext.setImageDrawable(mNextIconDrawable);

        });
    }

    private void updateBackground() {
        if (!showMediaArtMediaQs || !mBound) {
            hideMediaQsBackground();
            return;
        }
        Bitmap oldArt = mArt;
        Bitmap tempArt = getArt();
        if (tempArt == null) {
            hideMediaQsBackground();
            return;
        }
        mArt = getFilteredArt(tempArt);
        float radius = 0f;
        if (Build.VERSION.SDK_INT >= 35) {
            radius = getMediaPanelRadius(mContext);
        } else if (mOplusQsMediaDefaultBackground != null && mOplusQsMediaDefaultBackground instanceof GradientDrawable gradientDrawable) {
                radius = gradientDrawable.getCornerRadius();
        }
        RoundedBitmapDrawable artRounded = RoundedBitmapDrawableFactory.create(mContext.getResources(), mArt);
        artRounded.setCornerRadius(radius);
        artRounded.setAntiAlias(true);
        RoundedBitmapDrawable oldArtRounded = RoundedBitmapDrawableFactory.create(mContext.getResources(), oldArt);
        oldArtRounded.setCornerRadius(radius);
        oldArtRounded.setAntiAlias(true);
        Palette.Builder builder = new Palette.Builder(mArt);
        builder.generate(palette -> {
            int dominantColor = palette.getDominantColor(Color.WHITE);
            mColorOnAlbum =
                    isColorDark(dominantColor) ?
                            DrawableConverter.findContrastColorAgainstDark(Color.WHITE, dominantColor, true, 2) :
                            DrawableConverter.findContrastColor(Color.BLACK, dominantColor, true, 2);
            post(() -> setupOtherViews(mColorOnAlbum));
        });

        post(() -> {
            Drawable[] layers = new Drawable[]{oldArtRounded, artRounded};
            TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
            setBackground(transitionDrawable);
            transitionDrawable.startTransition(250);
        });
    }

    private void setupOtherViews(int color) {
        mDeviceIcon.clearColorFilter();
        mDeviceIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        mTitle.setTextColor(color);
        mText.setTextColor(color);
        mPrev.setImageTintList(ColorStateList.valueOf(color));
        mPlayPause.setImageTintList(ColorStateList.valueOf(color));
        mNext.setImageTintList(ColorStateList.valueOf(color));
    }

    private Bitmap getFilteredArt(Bitmap art) {
        if (art == null) {
            return null;
        }
        Bitmap finalArt;
        switch (mMediaQsArtFilter) {
            case 1 -> finalArt = DrawableConverter.toGrayscale(art);
            case 2 ->
                    finalArt = DrawableConverter.getColoredBitmap(new BitmapDrawable(mContext.getResources(), art),
                            getPrimaryColor(mContext));
            case 3 ->
                    finalArt = DrawableConverter.getBlurredImage(mContext, art, mMediaQsArtBlurAmount);
            case 4 ->
                    finalArt = DrawableConverter.getGrayscaleBlurredImage(mContext, art, mMediaQsArtBlurAmount);
            case 5 ->
                    finalArt = DrawableConverter.getColoredBitmap(new BitmapDrawable(mContext.getResources(), art),
                            mMediaQsTintColor, mMediaQsTintAmount);
            default -> finalArt = art;
        }
        return finalArt;
    }

    private void hideMediaQsBackground() {
        if (Build.VERSION.SDK_INT == 35) {
            setBackground(null);
            return;
        }
        if (qsInactiveColorEnabled) {
            if (mOplusQsMediaDrawable == null) return;
            mOplusQsMediaDrawable.invalidateSelf();
            setBackground(mOplusQsMediaDrawable);
        } else {
            if (mOplusQsMediaDefaultBackground == null) return;
            mOplusQsMediaDefaultBackground.invalidateSelf();
            setBackground(mOplusQsMediaDefaultBackground);
        }
    }

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    /**
     * Vibrate the device
     *
     * @param type 0 = Long Press, 1 = Click
     */
    private void vibrate(int type) {
        if (type == 0) {
            this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        } else if (type == 1) {
            this.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        }
    }

    public void updatePrefs(boolean showAlbumArt, int mediaQsArtFilter, int mediaQsTintColor, int mediaQsTintAmount, float mediaQsArtBlurAmount) {
        showMediaArtMediaQs = showAlbumArt;
        mMediaQsArtFilter = mediaQsArtFilter;
        mMediaQsTintColor = mediaQsTintColor;
        mMediaQsTintAmount = mediaQsTintAmount;
        mMediaQsArtBlurAmount = mediaQsArtBlurAmount;
        updateBackground();
    }

    public void updateDefaultBackground(Drawable defDrawable) {
        if (defDrawable == null) return;
        try {
            mOplusQsMediaDefaultBackground = defDrawable.getConstantState().newDrawable().mutate();
        } catch (Throwable ignored) {
            mOplusQsMediaDefaultBackground = defDrawable.mutate();
        }
        try {
            mOplusQsMediaDrawable = defDrawable.getConstantState().newDrawable().mutate();
        } catch (Throwable ignored) {
            mOplusQsMediaDrawable = defDrawable.mutate();
        }
        updateBackground();
    }

    public void updateColors(Drawable defDrawable, boolean customColor, int color) {
        qsInactiveColorEnabled = customColor;
        qsInactiveColor = color;
        updateDefaultBackground(defDrawable);
        updateBackground();
    }

    private void showDeviceDialog() {
        try {
            if (Build.VERSION.SDK_INT >= 35) {
                Object OplusQsMediaTile = getOplusQsMediaTile();
                callMethod(OplusQsMediaTile, "handleClickMediaOutputBtn", mMediaData);
            } else {
                Object mediaOutput = getMediaOutputDialogFactory();
                callMethod(mediaOutput, "create", mMediaData != null ? callMethod(mMediaData, "getPackageName") : null, true, ((View) mDeviceIcon), true);
            }
        } catch (Throwable t) {
            log("QsMediaTile showDeviceDialog error " + t);
        }
    }
}
