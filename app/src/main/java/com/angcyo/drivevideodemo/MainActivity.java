package com.angcyo.drivevideodemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;

import org.wysaid.camera.CameraInstance;
import org.wysaid.view.CameraRecordGLSurfaceView;

public class MainActivity extends AppCompatActivity {

    TextureView mTextureView;
    CameraRecordGLSurfaceView mGLSurfaceView;
    boolean isBlur = true;
    View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextureView = (TextureView) findViewById(R.id.textureView);
        mGLSurfaceView = (CameraRecordGLSurfaceView) findViewById(R.id.glSurfaceView);

        rootLayout = findViewById(R.id.rootLayout);
    }

    public void blur(View view) {
        CameraInstance.getInstance().stopPreview();
        if (isBlur) {
//            mTextureView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
//            mTextureView.setLayoutParams(new RelativeLayout.LayoutParams(10, 10));
            mGLSurfaceView.setLayoutParams(new RelativeLayout.LayoutParams(10, 10));
            CameraInstance.getInstance().startPreview(mTextureView.getSurfaceTexture());
        } else {
//            mGLSurfaceView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
            mTextureView.setLayoutParams(new RelativeLayout.LayoutParams(10, 10));
            CameraInstance.getInstance().startPreview(mGLSurfaceView.getSurfaceTexture());
        }

//        rootLayout.requestLayout();
        isBlur = !isBlur;
    }
}
