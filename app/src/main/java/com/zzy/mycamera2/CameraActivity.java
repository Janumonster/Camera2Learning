package com.zzy.mycamera2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.nio.ByteBuffer;
import java.util.Arrays;

import pub.devrel.easypermissions.EasyPermissions;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = CameraActivity.class.getName();

    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private CameraManager mCameraManager;
    private String mCameraID;
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private String[] mCameraIDs;

    private boolean isCaptured = false;

    private HandlerThread mBackGroundThread;
    private Handler mHandler;

    private Button btnCapture, btnRePreview;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        initView();
    }

    private void initView() {
        imageView = findViewById(R.id.image);
        btnCapture = findViewById(R.id.capture);
        btnRePreview = findViewById(R.id.button2);
        btnRePreview.setOnClickListener(this);
        btnCapture.setOnClickListener(this);
        mSurfaceView = findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setKeepScreenOn(true);
        initCamera();
    }

    private void initCamera() {
        mHandler = new Handler(getMainLooper());
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        if (mCameraManager != null) {
            try {
                mCameraIDs = mCameraManager.getCameraIdList();
                for (String id : mCameraIDs) {
                    CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(id);
                    Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (facing != null && facing != CameraCharacteristics.LENS_FACING_FRONT) {
                        mCameraID = id;
                        break;
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.capture:
                takePicture();
                isCaptured = true;
                break;
            case R.id.button2:
                imageView.setVisibility(View.GONE);
                mSurfaceView.setVisibility(View.VISIBLE);
                openCamera();
                isCaptured = false;
                break;
        }
    }

    public void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            EasyPermissions.requestPermissions(this,"权限请求",0,Manifest.permission.CAMERA);
            return;
        }
        try {
            mCameraManager.openCamera(mCameraID, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    mCameraDevice = cameraDevice;
                    startCameraPreview();
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
            }, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera(){
        if (mCameraDevice != null){
            mCameraDevice.close();
        }
        mCameraDevice = null;
    }


    private void startCameraPreview(){
        if (mCameraDevice == null){
            return;
        }
        mImageReader = ImageReader.newInstance(mSurfaceView.getWidth(),mSurfaceView.getHeight(),ImageFormat.JPEG,1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                imageView.setVisibility(View.VISIBLE);
                mSurfaceView.setVisibility(View.GONE);

                Image image = imageReader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                Matrix matrix = new Matrix();
                matrix.setRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                imageView.setImageBitmap(rotatedBitmap);
            }
        },mHandler);
        try {
            final CaptureRequest.Builder previewRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequest.addTarget(mSurfaceHolder.getSurface());
            previewRequest.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        mCameraCaptureSession.setRepeatingRequest(previewRequest.build(),null,mHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            },mHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void stopPreview(){
        if (mCameraCaptureSession != null){
            try {
                mCameraCaptureSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void takePicture(){
        if (mCameraDevice == null){
            return;
        }
        try {
            CaptureRequest.Builder pictureRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            pictureRequest.addTarget(mImageReader.getSurface());

            pictureRequest.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            pictureRequest.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                int ratation = windowManager.getDefaultDisplay().getRotation();
                pictureRequest.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATION.get(ratation));
            }
            stopPreview();
            mCameraCaptureSession.capture(pictureRequest.build(),null,mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isCaptured){
            imageView.setVisibility(View.GONE);
            mSurfaceView.setVisibility(View.VISIBLE);
            openCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }
}
