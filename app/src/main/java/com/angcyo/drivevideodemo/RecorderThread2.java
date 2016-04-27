package com.angcyo.drivevideodemo;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import org.wysaid.camera.CameraInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by robi on 2016-04-24 11:00.
 */
@SuppressWarnings("deprecation")
public class RecorderThread2 extends HandlerThread implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener, Camera.PictureCallback, Camera.ShutterCallback {

    public static final int MAX_DURATION = 10 * 1000;//最常录制时间
    public static final int MSG_START = 0x01;
    public static final int MSG_ERROR = MSG_START << 1;
    public static final int MSG_CAMERA_ERROR = MSG_ERROR << 1;
    public static final int MSG_NO_PREVIEW = MSG_CAMERA_ERROR << 1;
    public static final int MSG_TAKE_PICTURE = MSG_NO_PREVIEW << 1;
    public static final int MSG_SWITCH_PREVIEW = MSG_TAKE_PICTURE << 1;
    static RecorderThread2 mThread;
    Object lock = new Object();
    MediaRecorder mMediaRecorder;
    SurfaceTexture mSurfaceTexture;
    int takePictureCount = 0;
    boolean isMute = false;//拍照静音
    ISwitchPreview mISwitchPreview;
    boolean isStopPreview = false;
    boolean isSetPreview = false;
    boolean isCallBack = false;
    private Handler mHandler;
    CameraInstance mCameraInstance;

    private RecorderThread2(String name, SurfaceTexture surface, ISwitchPreview switchPreview) {
        super(name);
        this.mSurfaceTexture = surface;
        if (surface != null) {
            mCameraInstance = CameraInstance.getInstance();
            mCameraInstance.tryOpenCamera(new CameraInstance.CameraOpenCallback() {
                @Override
                public void cameraReady() {
                    mCameraInstance.getCameraDevice();
                    start();
                }
            });

//            mCamera = camera;
//            try {
//                mCamera.setPreviewTexture(surface);
//                isSetPreview = true;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }
        mISwitchPreview = switchPreview;
    }


    public static void startThread(SurfaceTexture surface, ISwitchPreview switchPreview) {
        if (mThread == null) {
            synchronized (RecorderThread2.class) {
                if (mThread == null) {
                    mThread = new RecorderThread2("RecorderThread", surface, switchPreview);
                }
            }
        } else {
        }
    }

    public static boolean isRecordStart() {
        return mThread != null;
    }

    public static void exitThread() {
        synchronized (RecorderThread2.class) {
            if (mThread != null) {
                mThread.exit();
                mThread = null;
            }
        }
    }

    public static void takePhoto() {
        if (mThread != null) {
            mThread.mHandler.sendEmptyMessage(MSG_TAKE_PICTURE);
        }
    }

    public static void e(String msg) {
        Log.e("angcyo-->" + Thread.currentThread().getId(), msg);
    }

    public static String getVideoFileName() {
        return getFileName(".mp4");
    }

    public static String getPhotoFileName() {
        return getFileName(".png");
    }

    public static String getFileName(String ext) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/sdcard/angcyo/");
        stringBuilder.append(getTempFileName());
        stringBuilder.append(ext);
        String filePath = stringBuilder.toString();
        File parentFile = new File(filePath).getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        return filePath;
    }

    public static String getTempFileName() {
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss:SSS").format(System.currentTimeMillis());
    }

    private static void savePictureAction(byte[] pictureData) {
        String picturePath = getPhotoFileName();
        try {
            FileOutputStream fos = new FileOutputStream(new File(picturePath));
            fos.write(pictureData);
            fos.flush();
            fos.close();
            e("图片保存至:" + picturePath);
        } catch (Exception e) {
            e("保存图片失败:" + e.getMessage());
        }
    }

    private void takePictureCount(boolean increase) {
        if (mCameraInstance != null && mCameraInstance.getCameraDevice() != null) {
            if (takePictureCount == 0) {
                startTakePicture();
            }
            if (increase) {
                takePictureCount++;
            }
            e("takePictureCount:" + takePictureCount);
        }
    }

    private void startTakePicture() {
        try {
            if (isMute) {
                mCameraInstance.getCameraDevice().takePicture(null, null, this);
            } else {
                mCameraInstance.getCameraDevice().takePicture(this, null, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            e("拍照异常:" + e.getMessage());
        }
    }


    private void exit() {
        releaseMediaRecorder();
        releaseCamera();
        quit();
    }

    @Override
    public void run() {
        super.run();
        e("线程退出");
    }

    @Override
    protected void onLooperPrepared() {
        initHandler();
        restartRecorder(getVideoFileName());
    }

    private void createMediaRecorder() {
        releaseMediaRecorder();

        mMediaRecorder = new MediaRecorder();
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void releaseCamera() {
       mCameraInstance.stopCamera();
    }

    private void initHandler() {
        mHandler = new Handler(getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                e("handleMessage " + msg.what);
                switch (msg.what) {
                    case MSG_START:
                        break;
                    case MSG_ERROR:
                        break;
                    case MSG_TAKE_PICTURE:
                        takePictureCount(true);
                        break;
                    case MSG_SWITCH_PREVIEW:
                        break;
                    default:
                        break;
                }
//            if (msg.what == 1001) {
//                e("收到消息:" + 1001);
//                mMediaRecorder.reset();
//            }
                return true;
            }
        });
    }

    private void restartRecorder(String filePath) {
        if (mMediaRecorder == null) {
            createMediaRecorder();
        }
        mMediaRecorder.reset();

        if (mCameraInstance.getCameraDevice() == null) {
            e("赋值 camera");
            mCameraInstance.tryOpenCamera(null);
        }

        if (mCameraInstance.getCameraDevice() == null) {
//            synchronized (lock) {
//                try {
//                    e("等待camera...");
//                    lock.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            return;
        }

        if (!isSetPreview) {
            try {
                mCameraInstance.getCameraDevice().setPreviewTexture(mSurfaceTexture);
                isSetPreview = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mCameraInstance.getCameraDevice().unlock();
        mMediaRecorder.setCamera(mCameraInstance.getCameraDevice());

        CamcorderProfile mProfile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_HIGH);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(mProfile);
//
        mMediaRecorder.setOutputFile(filePath);
        mMediaRecorder.setMaxDuration(MAX_DURATION);
        mMediaRecorder.setOnInfoListener(this);
        mMediaRecorder.setOnErrorListener(this);

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            e("重新 开始录制:" + filePath);

            if (mISwitchPreview != null && !isCallBack) {
                mISwitchPreview.onSwitchPreview();
                isCallBack = true;
            }
        } catch (Exception e) {
            e("重新 录制失败:" + e.getMessage());
            e.printStackTrace();
            mHandler.sendEmptyMessage(MSG_ERROR);
        }

        if (!isStopPreview) {
            mCameraInstance.getCameraDevice().stopPreview();
            isStopPreview = true;
        }
    }

//    private void openCamera(int cameraId) throws Exception {
//        if (mCamera != null) {
//            return;
//        }
//        Camera camera = Camera.open(cameraId);
//        Camera.Parameters parameters = camera.getParameters();
//        parameters.setFlashMode("off");
//        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
//        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
//        int width, height;
//        width = 1920;
//        height = 1080;
//        parameters.setPreviewSize(width, height);
//        parameters.setPictureSize(width, height);
////            this.mCamera.setDisplayOrientation(90);
////        mCameraPreviewCallback = new CameraPreviewCallback();
////        mCamera.addCallbackBuffer(mImageCallbackBuffer);
////        mCamera.setPreviewCallbackWithBuffer(mCameraPreviewCallback);
////            mCamera.setPreviewCallback(mCameraPreviewCallback);
//        List<String> focusModes = parameters.getSupportedFocusModes();
//        if (focusModes.contains("continuous-video")) {
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//        }
//        camera.setParameters(parameters);
//        try {
//            if (mSurfaceTexture != null) {
//                camera.setPreviewTexture(mSurfaceTexture);
//            } else if (mSurfaceHolder != null) {
//                camera.setPreviewDisplay(mSurfaceHolder);
//            } else {
//                mHandler.sendEmptyMessage(MSG_NO_PREVIEW);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            mHandler.sendEmptyMessage(MSG_ERROR);
//            return;
//        }
//
//        mCamera = camera;
//    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        e("onInfo " + what + " " + extra);
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
//            e("MEDIA_RECORDER_INFO_MAX_DURATION_REACHED");
            restartRecorder(getVideoFileName());
//            mHandler.sendMessage(mHandler.obtainMessage(1001));
        }
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        e("onError " + what + " " + extra);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        e("onPictureTaken " + data.length + " count:" + takePictureCount);
        savePictureAction(data);
        takePictureCount--;
        if (takePictureCount > 0) {
            startTakePicture();
        } else {
            takePictureCount = 0;
        }
    }

    @Override
    public void onShutter() {
        e("onShutter");
    }

    public interface ISwitchPreview {
        void onSwitchPreview();
    }
}
