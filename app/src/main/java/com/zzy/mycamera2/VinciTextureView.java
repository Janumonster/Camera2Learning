package com.zzy.mycamera2;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

public class VinciTextureView extends TextureView implements TextureView.SurfaceTextureListener{

    private static final String TAG = VinciTextureView.class.getSimpleName();

    private int mPreviewWidth = 1080;
    private int mPreviewHeight = 1920;

    private boolean isSurfaceAvailable;

    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    public VinciTextureView(Context context) {
        super(context,null);
    }

    public VinciTextureView(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }

    public VinciTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSurfaceTextureListener(this);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"Click");
            }
        });
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        mSurfaceTexture = surfaceTexture;
        mSurface = new Surface(mSurfaceTexture);
        isSurfaceAvailable = true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        mSurfaceTexture = surfaceTexture;
        mSurface = new Surface(mSurfaceTexture);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = null;
        mSurface = null;
        isSurfaceAvailable = false;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (0 == mPreviewWidth || 0 == mPreviewHeight){
            Log.d(TAG, "onMeasure PreviewSize is not ready.");
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthFixed,heightFixed;
        if (width < height * mPreviewWidth/mPreviewHeight){
            widthFixed = width;
            heightFixed = width * mPreviewHeight/mPreviewWidth;
        }else {
            widthFixed = height * mPreviewWidth/mPreviewHeight;
            heightFixed = height;
        }
        setMeasuredDimension(widthFixed,heightFixed);
    }
}
