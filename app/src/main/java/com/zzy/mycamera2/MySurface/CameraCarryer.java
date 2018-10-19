package com.zzy.mycamera2.MySurface;

import android.view.Surface;
import android.view.SurfaceHolder;

public interface CameraCarryer {

    boolean isSurfaceAvailable();

    Surface getSurface();

    SurfaceHolder getSurfaceHolder();

    void setSurfaceCallbcak(CameraCarryerSurfaceCallback callbcak);

    int getViewWidth();

    int getViewHeight();

    void setPreviewSize(int width, int height, SetSurfaceSizeCallback callback);

}
