package com.zzy.mycamera2;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraTest extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = CameraTest.class.getSimpleName();

    private Camera_v2 camera_v2;

    private SurfaceView previewHolder;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ImageView imageView;
    private Button capture,repreview;
    private TextureView textureView;
    private Surface surface;
    private SurfaceTexture surfaceTexture;

    private boolean isCaptured = false;

    private Size surfaceSize;

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
        repreview = findViewById(R.id.rePreview);
        repreview.setOnClickListener(this);
        imageView = findViewById(R.id.image_show);
        previewHolder = findViewById(R.id.preview_container);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.test_capture:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    camera_v2.takePicture();
                }
                isCaptured = true;
                break;
            case R.id.rePreview:
                previewHolder.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                camera_v2.startPreview();
                isCaptured = false;
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        surfaceHolder = previewHolder.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        if (!isCaptured){
            previewHolder.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                camera_v2 = new Camera_v2(this,surfaceHolder.getSurface());
                camera_v2.openCamera();
                surfaceSize = camera_v2.getSupportPreviewSize()[0];
                Log.d(TAG, "initView: "+surfaceSize.getWidth()+"*"+surfaceSize.getHeight());
                camera_v2.setOnCaptureListener(new OnCaptureListener() {
                    @Override
                    public void onCaptured(byte[] bytes) {
                        previewHolder.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);

                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        Matrix matrix = new Matrix();
                        matrix.setRotate(90);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                        imageView.setImageBitmap(rotatedBitmap);
                    }
                });
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        camera_v2.closeCamera();
    }
}
