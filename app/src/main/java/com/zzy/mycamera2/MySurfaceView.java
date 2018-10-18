package com.zzy.mycamera2;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "Camera_v2";

    private int Width = 0;
    private int Height = 0;

    private Context mContext;

    private SurfaceHolder mSurfaceHolder;

    private MySurfaceView.OnMySurfaceViewCallback mySurfaceViewCallback;

    public void setMySurfaceViewCallback(OnMySurfaceViewCallback mySurfaceViewCallback) {
        this.mySurfaceViewCallback = mySurfaceViewCallback;
    }

    public MySurfaceView(Context context) {
        super(context,null);
        this.mContext = context;
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
    }

    public Surface getSurface(){
        return mSurfaceHolder.getSurface();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated");
        mySurfaceViewCallback.mySurfaceCreated(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mySurfaceViewCallback.mySurfaceChanged(surfaceHolder,i,i1,i2);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mySurfaceViewCallback.mySurfaceDestoryed(surfaceHolder);
    }

    public interface OnMySurfaceViewCallback{
        void mySurfaceCreated(SurfaceHolder surfaceHolder);
        void mySurfaceChanged(SurfaceHolder surfaceHolder,int format,int width,int height);
        void mySurfaceDestoryed(SurfaceHolder surfaceHolder);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
