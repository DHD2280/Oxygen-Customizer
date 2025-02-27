package it.dhd.oxygencustomizer.xposed.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public class ExtendedViewPager extends ViewPager {

    public boolean isExpanded = false;

    public ExtendedViewPager(@NonNull Context context) {
        super(context);
    }

    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // returning false will not propagate the swipe event
        if (isExpanded) {
            return false;
        } else {
            return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isExpanded) {
            return false;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

}
