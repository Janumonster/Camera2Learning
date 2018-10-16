package com.zzy.mycamera2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.content.ContextCompat;

public class Camera_v1 {

    //get the number of camera available , need android 2.3 or later
    private int camerasNumber = Camera.getNumberOfCameras();
    //current camera id
    private int mCameraID;



    /**
     * check out if you device has camera device
     * @param context context
     * @return true or false
     */
    private boolean checkCameraHardware(Context context){
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            //this device has a camera
            return true;
        }else {
            //no camera on this device
            return false;
        }
    }

    /**
     * attempt to open defule camera
     * @return camera
     */
    private Camera getDefultCameraInstance(){
        Camera camera = null;
        try {
            camera = Camera.open();//attempt to get a Camera instance
        }catch (Exception e){
            //Camera is not available (in use or does not exist)
        }
        return camera;
    }

    /**
     * need api >= 9
     * attempt to open camera by id
     * @param cameraID .
     * @return camera
     */
    private Camera getCameraInstance(int cameraID){
        Camera camera = null;
        try {
            camera = Camera.open(cameraID);//attempt to get a Camera instance
        }catch (Exception e){
            //Camera is not available (in use or does not exist)
        }
        return camera;
    }

}
