package ca.quanta.quantaevents.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class MapTouchWrapper extends FrameLayout {
    public MapTouchWrapper(Context context) { super(context); }
    public MapTouchWrapper(Context context, @Nullable AttributeSet attrs) { super(context, attrs); }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        requestDisallowInterceptTouchEvent(true);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        requestDisallowInterceptTouchEvent(true);
        return false;
    }
}
