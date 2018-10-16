package com.zzy.mycamera2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView {

    public MySurfaceView(Context context) {
        super(context,null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }
}
