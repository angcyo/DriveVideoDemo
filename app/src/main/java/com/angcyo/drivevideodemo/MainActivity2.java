package com.angcyo.drivevideodemo;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import org.wysaid.camera.CameraInstance;
import org.wysaid.view.CameraRecordGLSurfaceView;

public class MainActivity2 extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    TextureView mTextureView;
    CameraRecordGLSurfaceView mGLSurfaceView;
    boolean isBlur = true;
    View rootLayout;
    FrameLayout mFrameLayout;
    int switchCount = 0;
//    private CameraInstance mCameraInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        mGLSurfaceView = (CameraRecordGLSurfaceView) findViewById(R.id.glSurfaceView);
//        mGLSurfaceView.setOnSurfaceCreate(new CameraGLSurfaceView.OnSurfaceCreate() {
//            @Override
//            public void onSurfaceCreate() {
//                CameraInstance cameraInstance = CameraInstance.getInstance();
//                cameraInstance.tryOpenCamera(null);
//                cameraInstance.stopPreview();
//                cameraInstance.startPreview(mGLSurfaceView.getSurfaceTexture());
//            }
//        });

        mTextureView = (TextureView) findViewById(R.id.textureView);
//        mTextureView.setRotation(90);
//        mTextureView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
        mTextureView.setSurfaceTextureListener(this);

//        mGLSurfaceView = new CameraRecordGLSurfaceView(this);
//        mGLSurfaceView.setOnSurfaceCreate(new CameraGLSurfaceView.OnSurfaceCreate() {
//            @Override
//            public void onSurfaceCreate() {
////                CameraInstance cameraInstance = CameraInstance.getInstance();
////                cameraInstance.tryOpenCamera(null);
////                cameraInstance.stopPreview();
////                cameraInstance.startPreview(mGLSurfaceView.getSurfaceTexture());
//
//
//
//                rootLayout.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        RecorderThread2.e("setOnSurfaceCreate-->OnSurfaceCreate");
////                        CameraInstance cameraInstance = CameraInstance.getInstance();
////                        cameraInstance.stopPreview();
////                        cameraInstance.startPreview(mGLSurfaceView.getSurfaceTexture());
////
//////                        RecorderThread2.startThread(mGLSurfaceView.getSurfaceTexture());
////                        mCameraInstance.stopPreview();
////                        mCameraInstance.startPreview(mGLSurfaceView.getSurfaceTexture());
//                    }
//                }, 800);
//            }
//        });

//        mFrameLayout.addView(mGLSurfaceView, new FrameLayout.LayoutParams(-1, -1));
        rootLayout = findViewById(R.id.rootLayout);

//        mCameraInstance = CameraInstance.getInstance();
//        mCameraInstance.tryOpenCamera(null);
    }

    public void startActivity(View view) {
        TestActivity.show(this);
    }

    public void takePicture(View view) {
        RecorderThread2.takePhoto();
    }

    public void switchPreview(View view) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        RecorderThread2.e("switchPreview-->run");

        final SurfaceTexture surfaceTexture;
        int degrees;
        if (switchCount % 2 == 0) {
            degrees = 270;
            surfaceTexture = mGLSurfaceView.getSurfaceTexture();
        } else {
            degrees = 0;
            surfaceTexture = mTextureView.getSurfaceTexture();
        }
        switchCount++;
        final int finalDegrees = degrees;
        rootLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                CameraInstance.getInstance().stopPreview();
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                CameraInstance.getInstance().startPreview(surfaceTexture, finalDegrees);
//                if (finalDegrees == 270) {
//                    surfaceTexture.setOnFrameAvailableListener(mGLSurfaceView);
//                }
////                        runOnUiThread(new Runnable() {
////                            @Override
////                            public void run() {
////                                surfaceTexture.updateTexImage();
////                            }
////                        });
//                RecorderThread2.startThread(surfaceTexture);
            }
        }, 100);
//            }
//        }).start();
    }

    public void exit(View view) {
        RecorderThread2.exitThread();
        finish();
    }

    public void blur(View view) {
//        final SurfaceTexture surfaceTexture;
//        int degrees;
//        if (isBlur) {
//            mTextureView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
////            mTextureView.setLayoutParams(new RelativeLayout.LayoutParams(10, 10));
//            mGLSurfaceView.setLayoutParams(new RelativeLayout.LayoutParams(1, 1));
//            surfaceTexture = mTextureView.getSurfaceTexture();
//            degrees = 0;
//        } else {
//            mGLSurfaceView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
//            mTextureView.setLayoutParams(new RelativeLayout.LayoutParams(1, 1));
//            surfaceTexture = mGLSurfaceView.getSurfaceTexture();
//            degrees = 270;
//        }
//
//        final int finalDegrees = degrees;
//        rootLayout.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                CameraInstance.getInstance().stopPreview();
//                CameraInstance.getInstance().startPreview(surfaceTexture, finalDegrees);
//                if (finalDegrees == 0) {
//                    RecorderThread2.startThread(surfaceTexture);
//                }
//            }
//        }, 1);

//        rootLayout.requestLayout();
        isBlur = !isBlur;
        mGLSurfaceView.setBlur(isBlur);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        RecorderThread2.e("onSurfaceTextureAvailable");
        if (RecorderThread2.isRecordStart()) {
            switchBlurPreview();
        }

        RecorderThread2.startThread(surface, new RecorderThread2.ISwitchPreview() {
            @Override
            public void onSwitchPreview() {
                RecorderThread2.e("onSwitchPreview:1");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RecorderThread2.e("onSwitchPreview:2");
//                        if (mGLSurfaceView.getParent() == null) {
//                            RecorderThread2.e("onSwitchPreview:addView");
//                            mFrameLayout.addView(mGLSurfaceView, new FrameLayout.LayoutParams(-1, -1));
//                        }
//                        switchBlurPreview();

                        CameraInstance.getInstance().startPreview(mGLSurfaceView.getSurfaceTexture());
                    }
                });
            }
        });
//        switchBlurPreview();
    }

    private void switchBlurPreview() {
        RecorderThread2.e("switchBlurPreview");
        CameraInstance.getInstance().stopPreview();
        CameraInstance.getInstance().startPreview(mGLSurfaceView.getSurfaceTexture());
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
