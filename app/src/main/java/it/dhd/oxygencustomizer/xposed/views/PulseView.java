package it.dhd.oxygencustomizer.xposed.views;

/*
 * Modified from crDroid
 * https://github.com/crdroidandroid/android_frameworks_base/blob/9ce156fa20d3f7222b8ec63c281b44d9899abc48/packages/SystemUI/src/com/android/systemui/pulse
 *
 * Copyright (C) 2019 The AquariOS Project
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
 * This is were we draw Pulse. Attach to a ViewGroup and let the
 * eye candy happen
 *
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import it.dhd.oxygencustomizer.xposed.views.pulse.PulseControllerImpl;

@SuppressLint("ViewConstructor")
public class PulseView extends View {
    public static final String TAG = "PulseView";

    @SuppressLint("StaticFieldLeak")
    private static PulseControllerImpl mPulse;

    public PulseView(Context context, PulseControllerImpl controller) {
        super(context);
        mPulse = controller;
        setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setWillNotDraw(false);
        setTag(TAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mPulse.onSizeChanged(w, h, oldw, oldh);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        mPulse.onDraw(canvas);
        super.onDraw(canvas);
    }

}
