package com.angcyo.drivevideodemo;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;

import org.wysaid.camera.CameraInstance;
import org.wysaid.view.CameraRecordGLSurfaceView;

public class MainActivity3 extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    TextureView mTextureView;
    CameraRecordGLSurfaceView mGLSurfaceView;
    boolean isBlur = false;
    View rootLayout;
    //    FrameLayout mFrameLayout;
    boolean isOnSurfaceTextureAvailable = false;
    private CameraInstance mCameraInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        mGLSurfaceView = (CameraRecordGLSurfaceView) findViewById(R.id.glSurfaceView);
        mTextureView = (TextureView) findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(this);
        rootLayout = findViewById(R.id.rootLayout);
        mCameraInstance = CameraInstance.getInstance();
        mCameraInstance.tryOpenCamera(null);

//        mGLSurfaceView.setOnSurfaceCreate(new CameraGLSurfaceView.OnSurfaceCreate() {
//            @Override
//            public void onSurfaceCreate() {
//                mGLSurfaceView.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Camera cameraDevice = mCameraInstance.getCameraDevice();
//                        cameraDevice.stopPreview();
//                        try {
//                            cameraDevice.setPreviewTexture(mGLSurfaceView.getSurfaceTexture());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        cameraDevice.startPreview();
//                    }
//                }, 1000);
//            }
//        });
    }

    public void startActivity(View view) {
        TestActivity.show(this);
    }

    public void takePicture(View view) {
        RecorderThread3.takePhoto();
    }

    public void switchPreview(View view) {
    }

    public void exit(View view) {
        RecorderThread3.exitThread();
        mCameraInstance.stopCamera();
        finish();
    }

    public void blur(View view) {
        isBlur = !isBlur;
//        mGLSurfaceView.setBlur(isBlur);
        rePreview();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        RecorderThread2.e("onSurfaceTextureAvailable");
        if (!RecorderThread3.isRecordStart()) {
            RecorderThread3.startThread(mCameraInstance.getCameraDevice(), surface);
        } else {
            rePreview();
        }
    }

    private void rePreview() {
        RecorderThread3.stopMediaRecorder();
//        Camera cameraDevice = mCameraInstance.getCameraDevice();
//        if (cameraDevice == null) {
//            return;
//        }
//        cameraDevice.stopPreview();
        if (isBlur) {
            try {
                mTextureView.animate().alpha(0f).setDuration(200).start();
                RecorderThread2.e("rePreview setPreviewTexture");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mCameraInstance.stopPreview();
                mCameraInstance.startPreview(mGLSurfaceView.getSurfaceTexture(), 270);
//                cameraDevice.setPreviewTexture(mGLSurfaceView.getSurfaceTexture());
//                cameraDevice.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mTextureView.animate().alpha(1f).setDuration(200).start();
            RecorderThread2.e("rePreview restartMediaRecorder");
            RecorderThread3.restartMediaRecorder();
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        RecorderThread2.e("onSurfaceTextureDestroyed");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

}
