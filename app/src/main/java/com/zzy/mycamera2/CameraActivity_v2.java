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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity_v2 extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "Camera_v2";

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ImageView imageHolder;
    private Button capture,back;

    private Camera_v2 camera_v2;
    private boolean isBack = false;

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

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
//            if (camera_v2.isCameraClosed()){
//                camera_v2.openCamera();
//            }
            camera_v2.openCamera();
            camera_v2.startPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

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
        capture.setOnClickListener(this);
        back.setOnClickListener(this);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.addCallback(callback);

        camera_v2 = new Camera_v2(CameraActivity_v2.this);
        camera_v2.setOnCaptureListener(new OnCaptureListener() {
            @Override
            public void onCaptured(byte[] bytes) {
                Message message = new Message();
                message.what =0;
                message.obj = bytes;
                handler.sendMessage(message);
            }
        });
        camera_v2.setmPreviewSurface(surfaceHolder.getSurface());

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
