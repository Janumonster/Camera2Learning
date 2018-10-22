package com.zzy.cameracarryer.MySurface;

import android.view.Surface;

public interface CameraCarryerSurfaceCallback {

    void onSurfaceCreated(Surface surface,int width,int height);

    void onSurfaceChanged(Surface surface,int width,int height);

    void onSurfaceDestroyed();
}
