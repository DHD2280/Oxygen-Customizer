package it.dhd.oxygencustomizer.xposed.views.pulse;

/*
 * Modified from crDroid
 * https://github.com/crdroidandroid/android_frameworks_base/blob/9ce156fa20d3f7222b8ec63c281b44d9899abc48/packages/SystemUI/src/com/android/systemui/pulse
 * Copyright (C) 2014 The TeamEos Project
 * Copyright (C) 2016-2023 crDroid Android Project
 *
 * @author: Randall Rushing <randall.rushing@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Control class for Pulse media fuctions and visualizer state management
 * Basic logic flow inspired by Roman Birg aka romanbb in his Equalizer
 * tile produced for Cyanogenmod
 *
 */
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;

public class ColorController
        implements ColorAnimator.ColorAnimationListener {

    @SuppressLint("StaticFieldLeak")
    private static ColorController instance = null;
    public static final int COLOR_TYPE_ACCENT = 0;
    public static final int COLOR_TYPE_USER = 1;
    public static final int COLOR_TYPE_LAVALAMP = 2;
    public static final int COLOR_TYPE_AUTO = 3;
    public static final int LAVA_LAMP_SPEED_DEF = 10000;

    private Context mContext;
    private Renderer mRenderer;
    private ColorAnimator mLavaLamp;
    private int mColorType = COLOR_TYPE_LAVALAMP;
    private int mAccentColor;
    private int mColor;
    private int mAlbumColor;

    public ColorController(
            Context context) {
        mContext = context;
        instance = this;
        mLavaLamp = new ColorAnimator();
        mLavaLamp.setColorAnimatorListener(this);
        mAccentColor = getAccentColor();
        mAlbumColor = mAccentColor;
    }

    public static boolean hasInstance() {
        return (instance != null);
    }

    public static ColorController getInstance() {
        return instance;
    }

    void setRenderer(Renderer renderer) {
        mRenderer = renderer;
        notifyRenderer();
    }

    public void setColorType(int colorType) {
        mColorType = colorType;
        if (colorType == COLOR_TYPE_LAVALAMP) {
            stopLavaLamp();
        }
        notifyRenderer();
    }

    public void setCustomColor(int color) {
        mColor = color;
        notifyRenderer();
    }

    public void setLavaLampSpeed(int speed) {
        mLavaLamp.setAnimationTime(speed);
    }

    void notifyRenderer() {
        if (mRenderer != null) {
            if (mColorType == COLOR_TYPE_ACCENT) {
                mRenderer.onUpdateColor(mAccentColor);
            } else if (mColorType == COLOR_TYPE_USER) {
                mRenderer.onUpdateColor(mColor);
            } else if (mColorType == COLOR_TYPE_LAVALAMP && mRenderer.isValidStream()) {
                startLavaLamp();
            } else if (mColorType == COLOR_TYPE_AUTO) {
                mRenderer.onUpdateColor(mAlbumColor);
            }
        }
    }

    void startLavaLamp() {
        if (mColorType == COLOR_TYPE_LAVALAMP) {
            mLavaLamp.start();
        }
    }

    void stopLavaLamp() {
        mLavaLamp.stop();
    }

    int getAccentColor() {
        return ResourcesCompat.getColor(mContext.getResources(), android.R.color.system_accent1_600, mContext.getTheme());
    }

    @Override
    public void onConfigChanged(Configuration newConfig) {
        final int lastAccent = mAccentColor;
        final int currentAccent = getAccentColor();
        if (lastAccent != currentAccent) {
            mAccentColor = currentAccent;
            if (mRenderer != null && mColorType == COLOR_TYPE_ACCENT) {
                mRenderer.onUpdateColor(mAccentColor);
            }
        }
    }

    @Override
    public void onColorChanged(ColorAnimator colorAnimator, int color) {
        if (mRenderer != null) {
            mRenderer.onUpdateColor(color);
        }
    }

    public void setMediaNotificationColor(int color) {
        Log.d("ColorController", "setMediaNotificationColor: " + color);
        if (color != 0) {
            // be sure the color has an acceptable contrast against black navbar
            mAlbumColor = DrawableConverter.findContrastColorAgainstDark(color, 0x000000, true, 2);
            // now be sure the color also has an acceptable contrast against white navbar
            mAlbumColor = DrawableConverter.findContrastColor(mAlbumColor, 0xffffff, true, 2);
        } else {
            // fallback to accent color if the media notification isn't colorized
            mAlbumColor = mAccentColor;
        }
        if (mRenderer != null && mColorType == COLOR_TYPE_AUTO) {
            mRenderer.onUpdateColor(mAlbumColor);
        }
    }
}
