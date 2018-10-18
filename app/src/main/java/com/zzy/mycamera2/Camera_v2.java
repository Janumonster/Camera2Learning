package com.zzy.mycamera2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera_v2 {

    private static final String TAG = Camera_v2.class.getSimpleName();

    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private String[] cameraIds;
    private String mCameraID;

    public Size[] getSupportPreviewSize() {
        return supportPreviewSize;
    }

    private Size[] supportPreviewSize;
    private Integer mCameraOrientation;

    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCameraCharacteristics;

    private List<Surface> mSurfaceList = new ArrayList<>();
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;

    private boolean isFirstPreview = true;

    private Context mContext;

    public void setmPreviewSurface(Surface mPreviewSurface) {
        this.mPreviewSurface = mPreviewSurface;
    }

    private Surface mPreviewSurface;
    private ImageReader mImageReader;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private OnCaptureListener onCaptureListener;

    public void setOnCaptureListener(OnCaptureListener onCaptureListener) {
        this.onCaptureListener = onCaptureListener;
    }

    public Camera_v2(Context mContext) {
        this.mContext = mContext;
        initCamera();
    }


    private void initCamera() {
        startBackgroundThread();
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (mCameraManager != null) {
                cameraIds = mCameraManager.getCameraIdList();
                if (cameraIds.length != 0){
                    for (String id : cameraIds){
                        CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);
                        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                        if (facing != null && facing != CameraCharacteristics.LENS_FACING_FRONT){
                            mCameraID = id;
                            Log.d(TAG, "initCamera: mCameraID:"+mCameraID);
                            mCameraCharacteristics = characteristics;
                            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                            if (map != null) {
                                supportPreviewSize = map.getOutputSizes(SurfaceHolder.class);
                            }
                            break;
                        }
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread(){
        mBackgroundThread = new HandlerThread("cameraV2");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    public void openCamera(){
        Log.d(TAG, "openCamera");
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            EasyPermissions.requestPermissions((Activity) mContext,"权限请求",0,Manifest.permission.CAMERA);
            return;
        }
        try {
            if (mCameraID == null){
                Log.d(TAG, "CameraID is null!");
                return;
            }
            mCameraManager.openCamera(mCameraID,new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    mCameraDevice = cameraDevice;
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                }

                @Override
                public void onError(@NonNull CameraDevice cameraDevice, int i) {
                    if (mCameraDevice != null){
                        mCameraDevice.close();
                        mCameraDevice = null;
                    }
                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void startPreview() {
        Log.d(TAG, "startPreview");
        if (mCameraDevice == null){
            return;
        }
        try {
            mImageReader = ImageReader.newInstance(1280,960,ImageFormat.JPEG,2);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    //读取image的数据，获取字节流
                    Image image = imageReader.acquireNextImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    onCaptureListener.onCaptured(bytes);
                    //保存图片
                }
            }, mBackgroundHandler);
            mSurfaceList.clear();
            mSurfaceList.add(mPreviewSurface);
            mSurfaceList.add(mImageReader.getSurface());

            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mPreviewSurface);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            mCameraDevice.createCaptureSession(mSurfaceList, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "onConfigured: Create a new Session");
                    mCaptureSession = cameraCaptureSession;
                    try {
                        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(),null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stopPreview() {
        Log.d(TAG, "stopPreview");
        if (mCameraDevice == null){
            return;
        }
        if (mCaptureSession != null){
            try {
                mCaptureSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    }

    public void takePicture(){
        Log.d(TAG, "takePicture");
        if (mCameraDevice == null){
            return;
        }
        try {
            final CaptureRequest.Builder pictureRequset = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            pictureRequset.addTarget(mImageReader.getSurface());
            //自动对焦
            pictureRequset.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            //自动曝光
            pictureRequset.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                int ratation = windowManager.getDefaultDisplay().getRotation();
                pictureRequset.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATION.get(ratation));
            }

            mCaptureSession.capture(pictureRequset.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeCamera(){
        Log.d(TAG, "closeCamera");
        if (mCameraDevice != null){
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

}
