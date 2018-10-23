package com.zzy.cameracarryer.MySurface;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class CarryerSufaceView extends SurfaceView implements CameraCarryer,SurfaceHolder.Callback{

    private static final String TAG = "Camera_v2";

    private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;

    private int viewWidth = 0;
    private int viewHeight = 0;

    private boolean isSurfaceAvailable;
    private SurfaceHolder mSurfaceHolder;

    private boolean isFirsetCreated = true;

    private Context mContext;

    private CameraCarryerSurfaceCallback mSurfaceCallback = new CameraCarryerSurfaceCallback() {
        @Override
        public void onSurfaceCreated(Surface surface, int width, int height) {
        }

        @Override
        public void onSurfaceChanged(Surface surface, int width, int height) {

        }

        @Override
        public void onSurfaceDestroyed() {

        }
    };

    public CarryerSufaceView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public CarryerSufaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public CarryerSufaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private void init() {
        mSurfaceHolder = getHolder();
        if (mSurfaceHolder == null){
            Log.d(TAG, "init: null");
        }
        mSurfaceHolder.setKeepScreenOn(true);
        mSurfaceHolder.addCallback(this);
    }


    /**
     * SurfaceHolderCallback
     * @param surfaceHolder .
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated");
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: "+format+"/"+width+"-"+height);
        isSurfaceAvailable = true;
        mSurfaceHolder = surfaceHolder;
        if (isFirsetCreated){
            isFirsetCreated = false;
            mSurfaceCallback.onSurfaceCreated(surfaceHolder.getSurface(),width,height);
            mSurfaceCallback.onSurfaceChanged(surfaceHolder.getSurface(),width,height);
        }else {
            mSurfaceCallback.onSurfaceChanged(surfaceHolder.getSurface(),width,height);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed");
        isSurfaceAvailable = false;
        isFirsetCreated = true;
        mSurfaceHolder = null;
        mSurfaceCallback.onSurfaceDestroyed();
    }

    @Override
    public boolean isSurfaceAvailable() {
        return isSurfaceAvailable;
    }

    @Override
    public Surface getSurface() {
        return mSurfaceHolder == null ? null:mSurfaceHolder.getSurface();
    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    @Override
    public void setSurfaceCallbcak(CameraCarryerSurfaceCallback callbcak) {
        this.mSurfaceCallback = callbcak;
    }

    @Override
    public int getViewWidth() {
        return mPreviewWidth;
    }

    @Override
    public int getViewHeight() {
        return mPreviewHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mPreviewWidth == 0 ||mPreviewHeight == 0){
            Log.d(TAG, "onMeasure: mPrevirewSize is 0*0");
            super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        }else {
            viewWidth = MeasureSpec.getSize(widthMeasureSpec);
            viewHeight = MeasureSpec.getSize(heightMeasureSpec);

            int widthFixed = viewWidth;
            int heightFixed = viewWidth*mPreviewHeight/mPreviewWidth;

            if (isFirsetCreated){
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                layoutParams.height = heightFixed;
                setLayoutParams(layoutParams);
            }


            Log.d(TAG, "onMeasure: FixedSize:"+widthFixed+"*"+heightFixed);
            setMeasuredDimension(widthFixed,heightFixed);
        }
    }

    @Override
    public void setPreviewSize(final int width, final int height, final SetSurfaceSizeCallback callback) {
        if (width < 0 || height < 0){
            throw new IllegalArgumentException("Size cannot be negative");
        }

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            mPreviewWidth = width;
            mPreviewHeight = height;
        }else {
            mPreviewWidth = height;
            mPreviewHeight = width;
        }

        int widthFixed = viewWidth;
        int heightFixed = viewWidth*mPreviewHeight/mPreviewWidth;

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = heightFixed;
        setLayoutParams(layoutParams);

        Log.d(TAG, "setPreviewSize: "+viewWidth+"*"+heightFixed);
        if (mSurfaceHolder != null) {
            //surface仍持有preview的真实尺寸，而surfaceview只体现比例相同。
            Log.d(TAG, "setPreviewSize:width:" + width + "    height:" + height);
            mSurfaceHolder.setFixedSize(width, height);
        }
        requestLayout();
        if (callback != null){
            callback.setSurfaceSizeComplete();
        }
    }

}
