package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

import java.io.FileNotFoundException;
import java.io.InputStream;

import it.dhd.oxygencustomizer.R;

/**
 * IllustrationPreference is a preference that can play lottie format animation
 */
public class ImageIllustrationPreference extends Preference {

    private static final String TAG = "ImageIllustrationPreference";

    private static final boolean IS_ENABLED_LOTTIE_ADAPTIVE_COLOR = false;
    private static final int SIZE_UNSPECIFIED = -1;

    private int mMaxHeight = SIZE_UNSPECIFIED;
    private int mBackgroundResId;
    private int mImageResId;
    private boolean mCacheComposition = true;
    private boolean mIsAutoScale;
    private Uri mImageUri;
    private Drawable mImageDrawable;
    private View mMiddleGroundView;

    private boolean mLottieDynamicColor;

    public ImageIllustrationPreference(Context context) {
        super(context);
        init(context, /* attrs= */ null);
    }

    public ImageIllustrationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ImageIllustrationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ImageIllustrationPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                       int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);

        final LinearLayout backgroundView =
                (LinearLayout) holder.findViewById(R.id.background_layer);
        final ImageView imageLayer =
                (ImageView) holder.findViewById(R.id.image_layer);

        handleBackground(backgroundView);
        handleImage(imageLayer);
    }

    /**
     * Sets the image drawable to display image in {@link LottieAnimationView}.
     *
     * @param imageDrawable the drawable of an image
     */
    public void setImageDrawable(Drawable imageDrawable) {
        if (imageDrawable != mImageDrawable) {
            resetImageResourceCache();
            mImageDrawable = imageDrawable;
            notifyChanged();
        }
    }

    private void handleBackground(LinearLayout backgroundView) {
        if (mBackgroundResId != 0x0) {
            backgroundView.setBackgroundResource(mBackgroundResId);
        }
    }

    private void handleImage(ImageView imageLayer) {
        if (mImageResId != 0x0) {
            imageLayer.setImageResource(mImageResId);
        }
    }

    private void resetImageResourceCache() {
        mImageDrawable = null;
        mImageUri = null;
        mImageResId = 0;
    }

    private void init(Context context, AttributeSet attrs) {
        setLayoutResource(R.layout.custom_preference_image_illustration);

        setSelectable(false);

        mIsAutoScale = false;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.ImageIllustrationPreference, 0 /*defStyleAttr*/, 0 /*defStyleRes*/);
            mBackgroundResId = a.getResourceId(R.styleable.ImageIllustrationPreference_backgroundResource, 0);
            mImageResId = a.getResourceId(R.styleable.ImageIllustrationPreference_imageResource, 0);

            /*a = context.obtainStyledAttributes(attrs,
                    R.styleable.IllustrationPreference, 0, 0);
            mLottieDynamicColor = a.getBoolean(R.styleable.IllustrationPreference_dynamicColor,
                    false); */

            a.recycle();
        }
    }
}