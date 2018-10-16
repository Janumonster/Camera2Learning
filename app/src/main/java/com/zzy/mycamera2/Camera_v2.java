package com.zzy.mycamera2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
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
import android.view.SurfaceView;
import android.view.TextureView;
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
    private Integer mCameraOrientation;
    private CameraCharacteristics mCameraCharacteristics;

    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;

    private List<Surface> mSurfaceList = new ArrayList<>();
    private CameraCaptureSession mCaptureSession;

    private Context mContext;

    private Surface mPreviewSurface;
    private ImageReader mImageReader;

    private Handler mMainHandler;

    private OnCaptureListener onCaptureListener;

    public void setOnCaptureListener(OnCaptureListener onCaptureListener) {
        this.onCaptureListener = onCaptureListener;
    }

    public Camera_v2(Context mContext,Surface surface) {
        this.mContext = mContext;
        this.mPreviewSurface = surface;
        initCamera();
    }


    private void initCamera() {
        mMainHandler = new Handler(mContext.getMainLooper());
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mImageReader = ImageReader.newInstance(1280,960,ImageFormat.JPEG,1);
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
        },mMainHandler);
        mSurfaceList.add(mPreviewSurface);
        mSurfaceList.add(mImageReader.getSurface());
        try {
            cameraIds = mCameraManager != null ? mCameraManager.getCameraIdList() : new String[0];
            if (cameraIds.length != 0) {
                for (String id : cameraIds) {
                    CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (facing != null && facing != CameraCharacteristics.LENS_FACING_FRONT) {
                        mCameraID = id;
                        openCamera();
                        break;
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public void openCamera(){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            EasyPermissions.requestPermissions((Activity) mContext,"权限请求",0,Manifest.permission.CAMERA);
            return;
        }
        try {
            mCameraManager.openCamera(mCameraID,new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    mCameraDevice = cameraDevice;
                    try {
                        mCameraDevice.createCaptureSession(mSurfaceList, new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                mCaptureSession = cameraCaptureSession;
                                startPreview();
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                            }
                        }, mMainHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                    if (mCameraDevice != null){
                        mCameraDevice.close();
                    }
                    mCameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice cameraDevice, int i) {
                    if (mCameraDevice != null){
                        mCameraDevice.close();
                    }
                    mCameraDevice = null;
                }
            }, mMainHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        if (mCameraDevice == null){
            return;
        }
        try {
            CaptureRequest.Builder previewRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            previewRequest.addTarget(mPreviewSurface);
            previewRequest.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            mCaptureSession.setRepeatingRequest(previewRequest.build(),null,mMainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void takePicture(){
        if (mCameraDevice == null){
            return;
        }
        try {
            CaptureRequest.Builder pictureRequset = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
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
            mCaptureSession.capture(pictureRequset.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,@NonNull CaptureRequest request,@NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }
            }, mMainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}
