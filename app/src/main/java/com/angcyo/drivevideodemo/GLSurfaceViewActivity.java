package com.angcyo.drivevideodemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.wysaid.camera.CameraInstance;
import org.wysaid.view.CameraGLSurfaceView;
import org.wysaid.view.CameraRecordGLSurfaceView;

public class GLSurfaceViewActivity extends AppCompatActivity {

    CameraRecordGLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glsurface_view);

        mGLSurfaceView = (CameraRecordGLSurfaceView) findViewById(R.id.glSurfaceView);

        mGLSurfaceView.setOnSurfaceCreate(new CameraGLSurfaceView.OnSurfaceCreate() {
            @Override
            public void onSurfaceCreate() {
//                mGLSurfaceView.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
                        CameraInstance.getInstance().tryOpenCamera(null);
                        CameraInstance.getInstance().stopPreview();
                        CameraInstance.getInstance().startPreview(mGLSurfaceView.getSurfaceTexture());
//                    }
//                }, 100);

            }
        });


//        mGLSurfaceView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                CameraInstance mCameraInstance = CameraInstance.getInstance();
//                mCameraInstance.tryOpenCamera(new CameraInstance.CameraOpenCallback() {
//                    @Override
//                    public void cameraReady() {
////                        Camera cameraDevice = CameraInstance.getInstance().getCameraDevice();
//                        CameraInstance.getInstance().stopPreview();
//                        CameraInstance.getInstance().startPreview(mGLSurfaceView.getSurfaceTexture());
////                        try {
////                            cameraDevice.setPreviewTexture();
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
////                        cameraDevice.startPreview();
//                    }
//                });
//
//            }
//        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraInstance.getInstance().stopCamera();
    }
}
