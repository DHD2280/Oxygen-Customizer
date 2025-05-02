package it.dhd.oxygencustomizer.xposed.views.peek;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.content.Context;

@SuppressLint("ViewConstructor")
public class PeekDisplayHolder extends LinearLayout {

    private int mLocation = 0;
    private PeekDisplayView mView;

    public PeekDisplayHolder(Context context, String tag) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mView = new PeekDisplayView(context, tag, false);
        addView(mView);
    }

    @Override
    protected void  onAttachedToWindow() {
        super.onAttachedToWindow();
        updateBottomPeekDisplayTopMargin();
    }

    public void updatePeekDisplayLocation(int location) {
        mLocation = location;
        updateBottomPeekDisplayTopMargin();
        updatePeekDisplayView();
    }

    private void updatePeekDisplayView() {
        mView.updatePeekDisplayView(mLocation);
    }

    private void updateBottomPeekDisplayTopMargin() {
//        int topMargin = Settings.Secure.getIntForUser(
//                context.contentResolver,
//                "peek_display_bottom_margin", 64, android.os.UserHandle.USER_CURRENT
//        );
//        val marginTop = Settings.Secure.getIntForUser(
//                context.contentResolver,
//                "peek_display_bottom_margin", 64, android.os.UserHandle.USER_CURRENT
//        )
//        setBottomPeekDisplayTopMargin(marginTop);
    }

}
