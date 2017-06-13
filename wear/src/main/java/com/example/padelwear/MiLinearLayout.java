package com.example.padelwear;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by jamarfal on 13/6/17.
 */

public class MiLinearLayout extends LinearLayout {
    public MiLinearLayout(Context context) {
        super(context);
    }

    public MiLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MiLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean canScrollHorizontally(int dir) {
        return true;
    }
}
