package com.zzy.mycamera2;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.zzy.cameracarryer.MySurface.CameraCarryerSurfaceCallback;
import com.zzy.cameracarryer.MySurface.CarryerSufaceView;
import com.zzy.cameracarryer.MySurface.SetSurfaceSizeCallback;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity_v2 extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "Camera_v2";

    private CarryerSufaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ImageView imageHolder;
    private Button capture,back,recorder,stop,size;
    private Button btn_4_3,btn_1_1;

    private Camera_v2 camera_v2;
    private boolean isBack = false;

    private Size previewSize;

    private Size[] mPreviewSizes;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    surfaceView.setVisibility(View.GONE);
                    imageHolder.setVisibility(View.VISIBLE);

                    byte[] bytes = (byte[]) msg.obj;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    Matrix matrix = new Matrix();
                    matrix.setRotate(90);
                    Bitmap bitmap1 = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                    imageHolder.setImageBitmap(bitmap1);
                    break;
            }
        }
    };

    private CameraCarryerSurfaceCallback surfaceCallback = new CameraCarryerSurfaceCallback() {
        @Override
        public void onSurfaceCreated(Surface surface, int width, int height) {
            Log.d(TAG, "carryersurface Created width:"+width+"  height:"+height);
            if (surfaceView.isSurfaceAvailable()) {
                camera_v2.openCamera();
            }
            previewSize = new Size(width,height);
        }

        @Override
        public void onSurfaceChanged(Surface surface, int width, int height) {
            Log.d(TAG, "carryersuface Changed width:"+width+"  height:"+height);
            previewSize = new Size(width,height);
        }

        @Override
        public void onSurfaceDestroyed() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_v2);
        initView();
    }


    private void initView() {
        surfaceView = findViewById(R.id.preview_layout);
        imageHolder = findViewById(R.id.image);
        capture = findViewById(R.id.take_picture);
        back = findViewById(R.id.back_preview);
        recorder = findViewById(R.id.recorder);
        stop = findViewById(R.id.stop_recorder);
        size = findViewById(R.id.preview_size);
        btn_4_3 = findViewById(R.id.size_four_three);
        btn_1_1 = findViewById(R.id.size_one_one);
        btn_1_1.setOnClickListener(this);
        btn_4_3.setOnClickListener(this);
        size.setOnClickListener(this);
        stop.setOnClickListener(this);
        recorder.setOnClickListener(this);
        capture.setOnClickListener(this);
        back.setOnClickListener(this);

        surfaceView.setSurfaceCallbcak(surfaceCallback);

        camera_v2 = new Camera_v2(CameraActivity_v2.this);
        mPreviewSizes = camera_v2.getSupportPreviewSize();
        for (Size s : mPreviewSizes){
            Log.d(TAG, "supportPreviewSize:"+s.getWidth()+"*"+s.getHeight());
        }
        surfaceView.setPreviewSize(camera_v2.getPreviewSize().getWidth(), camera_v2.getPreviewSize().getHeight(), new SetSurfaceSizeCallback() {
            @Override
            public void setSurfaceSizeComplete() {

            }
        });
        camera_v2.setmPreviewSurface(surfaceView.getSurface());
        camera_v2.setOnCaptureListener(new OnCaptureListener() {
            @Override
            public void onCaptured(byte[] bytes) {
                Message message = new Message();
                message.what =0;
                message.obj = bytes;
                handler.sendMessage(message);
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.take_picture:
                camera_v2.takePicture();
                isBack = true;
                break;
            case R.id.back_preview:
                surfaceView.setVisibility(View.VISIBLE);
                imageHolder.setVisibility(View.GONE);
                isBack = false;
                break;
            case R.id.recorder:
                camera_v2.startPreview();
                break;
            case R.id.stop_recorder:
                camera_v2.stopPreview();
                break;
            case R.id.preview_size:
                camera_v2.stopPreview();
                surfaceView.setPreviewSize(mPreviewSizes[1].getWidth(), mPreviewSizes[1].getHeight(), new SetSurfaceSizeCallback() {
                    @Override
                    public void setSurfaceSizeComplete() {
                        Log.d(TAG, "setSurfaceSizeComplete: ");
                        camera_v2.setPreviewSize(mPreviewSizes[1].getWidth(),mPreviewSizes[1].getHeight());
                        camera_v2.startPreview();
                    }
                });

                break;
            case R.id.size_four_three:
                camera_v2.stopPreview();
                surfaceView.setPreviewSize(mPreviewSizes[0].getWidth(), mPreviewSizes[0].getHeight(), new SetSurfaceSizeCallback() {
                    @Override
                    public void setSurfaceSizeComplete() {
                        camera_v2.setPreviewSize(mPreviewSizes[0].getWidth(),mPreviewSizes[0].getHeight());
                    }
                });
                break;
            case R.id.size_one_one:
                camera_v2.stopPreview();
                camera_v2.setPreviewSize(mPreviewSizes[1].getWidth(),mPreviewSizes[1].getHeight());
                surfaceView.setPreviewSize(mPreviewSizes[1].getWidth(), mPreviewSizes[1].getHeight(), new SetSurfaceSizeCallback() {
                    @Override
                    public void setSurfaceSizeComplete() {
                        camera_v2.startPreview();
                    }
                });
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        camera_v2.closeCamera();
        isBack = true;
    }
}
