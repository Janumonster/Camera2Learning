package com.zzy.mycamera2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class CameraTest extends AppCompatActivity implements View.OnClickListener{

    private Camera_v2 camera_v2;

    private SurfaceView previewHolder;
    private SurfaceHolder surfaceHolder;
    private ImageView imageView;
    private Button capture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_test);
        initView();
    }

    private void initView() {
        capture = findViewById(R.id.test_capture);
        capture.setOnClickListener(this);
        imageView = findViewById(R.id.image_show);
        previewHolder = findViewById(R.id.preview_container);
        surfaceHolder = previewHolder.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            camera_v2 = new Camera_v2(this,surfaceHolder.getSurface());
            camera_v2.setOnCaptureListener(new OnCaptureListener() {
                @Override
                public void onCaptured(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    Matrix matrix = new Matrix();
                    matrix.setRotate(90);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                    imageView.setImageBitmap(rotatedBitmap);
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.test_capture:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    camera_v2.takePicture();
                }
                break;
        }
    }
}
