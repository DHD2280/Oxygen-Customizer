package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.ProgressImageView;

@SuppressLint("ViewConstructor")
public class SoundWidget extends BaseDeviceWidget {

    private final ProgressImageView mSoundProgress;

    public SoundWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mSoundProgress = new ProgressImageView(mContext);
        mSoundProgress.setProgressType(ProgressImageView.ProgressType.VOLUME);
        mSoundProgress.setColors(mProgressColor, mTextColor);
        addView(mSoundProgress);
    }

    @Override
    public String getWidgetReference() {
        return getClass().getSimpleName();
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.sound);
    }

    @Override
    public boolean hasBigMode() {
        return false;
    }

    @Override
    public boolean hasSmallMode() {
        return true;
    }

    @Override
    public View getBigPreview() {
        return null;
    }

    @Override
    public View getSmallPreview() {
        ProgressImageView soundPreview = new ProgressImageView(mContext);
        soundPreview.setProgressType(ProgressImageView.ProgressType.VOLUME);
        soundPreview.setColors(mProgressColor, mTextColor);
        return soundPreview;
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        return Collections.emptyList();
    }

    @Override
    public void applySettings(Map<String, Object> settings) {}

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        mSoundProgress.setColors(mProgressColor, mTextColor);
    }
}
