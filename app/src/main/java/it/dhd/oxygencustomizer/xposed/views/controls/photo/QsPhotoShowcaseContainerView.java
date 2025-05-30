package it.dhd.oxygencustomizer.xposed.views.controls.photo;

import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.oplus.systemui.qs.base.tile.PressFeedbackHelper;

import it.dhd.oxygencustomizer.xposed.views.base.BaseQsStaticView;

/**
 * A container for {@link QsPhotoShowcaseView}
 * Note, this view is compatible with OOS15
 * using a {@link com.oplus.systemui.qs.base.widget.QsStaticViewInfoProvider}
 */
@SuppressWarnings("viewConstructor")
public class QsPhotoShowcaseContainerView extends BaseQsStaticView {

    private final Context mContext;
    private QsPhotoShowcaseView mQsPhotoShowcaseView;
    private int mRadius = 0;

    public QsPhotoShowcaseContainerView(@NonNull Context context) {
        super(context);

        mContext = context;

        setLayoutParams(new ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));

        mRadius = dp2px(mContext, 22);

        mQsPhotoShowcaseView = new QsPhotoShowcaseView(mContext);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        layoutParams.setMargins(
                dp2px(mContext, 6),
                dp2px(mContext, 6),
                dp2px(mContext, 6),
                dp2px(mContext, 6)
        );
        mQsPhotoShowcaseView.setLayoutParams(layoutParams);
        mQsPhotoShowcaseView.setRadius(mRadius);

        addView(mQsPhotoShowcaseView);
    }

    public void setRadius(int radius) {
        mRadius = radius;
        mQsPhotoShowcaseView.setRadius(mRadius);
    }

    public void setPhotoMode(boolean showcase) {
        mQsPhotoShowcaseView.setPhotoMode(showcase);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        PressFeedbackHelper.attachPressFeedback(this, this);
        this.backgroundProxy.onBackgroundAttach();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.backgroundProxy.onBackgroundDetach();
    }
}
