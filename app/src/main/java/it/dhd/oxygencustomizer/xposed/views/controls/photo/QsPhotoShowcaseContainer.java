package it.dhd.oxygencustomizer.xposed.views.controls.photo;

import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

/**
 * A container for {@link QsPhotoShowcaseView}
 */
@SuppressWarnings("viewConstructor")
public class QsPhotoShowcaseContainer extends LinearLayout {

    private final Context mContext;
    private QsPhotoShowcaseView mQsPhotoShowcaseView;
    private int mRadius = 0;

    public QsPhotoShowcaseContainer(@NonNull Context context) {
        this(context, false);
    }

    public QsPhotoShowcaseContainer(Context context, boolean settingsInterface) {
        super(context);
        mContext = context;

        setLayoutParams(new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        mRadius = dp2px(mContext, 22);

        mQsPhotoShowcaseView = new QsPhotoShowcaseView(mContext, settingsInterface);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
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

}
