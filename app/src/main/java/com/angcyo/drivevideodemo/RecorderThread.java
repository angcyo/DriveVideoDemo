package com.angcyo.drivevideodemo;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import org.wysaid.camera.CameraInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by robi on 2016-04-24 11:00.
 */
@SuppressWarnings("deprecation")
public class RecorderThread extends HandlerThread implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener, Camera.PictureCallback, Camera.ShutterCallback {

    public static final int MAX_DURATION = 10 * 1000;//最常录制时间
    public static final int MSG_START = 0x01;
    public static final int MSG_ERROR = MSG_START << 1;
    public static final int MSG_CAMERA_ERROR = MSG_ERROR << 1;
    public static final int MSG_NO_PREVIEW = MSG_CAMERA_ERROR << 1;
    public static final int MSG_TAKE_PICTURE = MSG_NO_PREVIEW << 1;
    public static final int MSG_SWITCH_PREVIEW = MSG_TAKE_PICTURE << 1;
    static RecorderThread mThread;
    Object lock = new Object();
    Camera mCamera;
    MediaRecorder mMediaRecorder;
    SurfaceHolder mSurfaceHolder;
    SurfaceTexture mSurfaceTexture;
    int takePictureCount = 0;
    boolean isMute = false;//拍照静音
    ISwitchPreview mISwitchPreview;
    boolean isStopPreview = false;
    private Handler mHandler;

    public RecorderThread(String name, SurfaceHolder surface) {
        super(name);
        this.mSurfaceHolder = surface;
        mSurfaceTexture = null;
//        CameraInstance.getInstance().tryOpenCamera(null);
//        mCamera = CameraInstance.getInstance().getCameraDevice();
    }

    public RecorderThread(String name, SurfaceTexture surface) {
        super(name);
        this.mSurfaceTexture = surface;
        mSurfaceHolder = null;
//        CameraInstance.getInstance().tryOpenCamera(null);
//        mCamera = CameraInstance.getInstance().getCameraDevice();
//        try {
//            mCamera.setPreviewTexture(surface);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public RecorderThread(String name, SurfaceTexture surface, ISwitchPreview switchPreview) {
        super(name);
        this.mSurfaceTexture = surface;
        mSurfaceHolder = null;
//        CameraInstance.getInstance().tryOpenCamera(null);
        mCamera = CameraInstance.getInstance().getCameraDevice();
        try {
            mCamera.setPreviewTexture(surface);
        } catch (IOException e) {
            e.printStackTrace();
        }
//
        mISwitchPreview = switchPreview;
    }

    public static void startThread(SurfaceHolder surface) {
        if (mThread == null) {
            synchronized (RecorderThread.class) {
                if (mThread == null) {
                    mThread = new RecorderThread("RecorderThread", surface);
                    mThread.start();
                }
            }
        } else {
            mThread.setSurfaceHolder(surface);
        }
    }

    public static void startThread(SurfaceTexture surface, ISwitchPreview switchPreview) {
        if (mThread == null) {
            synchronized (RecorderThread.class) {
                if (mThread == null) {
                    mThread = new RecorderThread("RecorderThread", surface, switchPreview);
                    mThread.start();
                }
            }
        } else {
//            if (switchPreview != null) {
//                switchPreview.onSwitchPreview();
//            }
//            mThread.setSurfaceTexture(surface);
        }
    }

    public static void startThread(SurfaceTexture surface) {
        if (mThread == null) {
            synchronized (RecorderThread.class) {
                if (mThread == null) {
                    mThread = new RecorderThread("RecorderThread", surface);
                    mThread.start();
                }
            }
        } else {
            mThread.setSurfaceTexture(surface);
//            mThread.mHandler.sendEmptyMessage(MSG_SWITCH_PREVIEW);
        }
    }

    public static void setThreadCamera(Camera camera) {
        if (mThread != null) {
            mThread.setCamera(camera);
        }
    }

    public static void destroyThread() {
        if (mThread != null) {
            mThread.setSurfaceTexture(null);
        }
    }

    public static void exitThread() {
        synchronized (RecorderThread.class) {
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

    private void setCamera(Camera camera) {
        if (mCamera != null) {
            return;
        }
        mCamera = camera;
        synchronized (lock) {
            lock.notify();
        }
    }

    private void takePictureCount(boolean increase) {
        if (mCamera != null) {
            if (takePictureCount == 0) {
                startTakePicture();
            } else if (increase) {
                takePictureCount++;
            }
            e("takePictureCount:" + takePictureCount);
        }
    }

    private void startTakePicture() {
        if (isMute) {
            mCamera.takePicture(null, null, this);
        } else {
            mCamera.takePicture(this, null, this);
        }
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
//        if (mCamera != null) {
//            try {
//                mCamera.stopPreview();
//                mCamera.setPreviewTexture(mSurfaceTexture);
////                mMediaRecorder.setPreviewDisplay(new Surface(mSurfaceTexture));
//                mCamera.startPreview();
//                e("startPreview");
//            } catch (Exception e) {
//                e.printStackTrace();
//                e("setSurfaceTexture " + e.getMessage());
//            }
//        }
        mHandler.sendEmptyMessage(MSG_SWITCH_PREVIEW);
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
//        if (mCamera != null) {
//            try {
//                mCamera.stopPreview();
//                mCamera.setPreviewDisplay(mSurfaceHolder);
//                mCamera.startPreview();
//                e("startPreview");
//            } catch (Exception e) {
//                e.printStackTrace();
//                e("setSurfaceHolder " + e.getMessage());
//            }
//        }
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
        if (mCamera != null) {
            // release the camera for other applications
            try {
                mCamera.stopPreview();
            } catch (Exception e) {

            }
            mCamera.release();
            mCamera = null;
        }
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
                        if (mSurfaceTexture != null) {
                            e("MSG_SWITCH_PREVIEW-->mSurfaceTexture");
                            if (mCamera != null) {
                                try {
                                    mCamera.stopPreview();
                                    mCamera.setPreviewTexture(mSurfaceTexture);
                                    mCamera.startPreview();
                                    e("MSG_SWITCH_PREVIEW-->mSurfaceTexture-->startPreview");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (mSurfaceHolder != null) {
                            e("MSG_SWITCH_PREVIEW-->mSurfaceTexture");

                            if (mCamera != null) {
                                try {
                                    mCamera.stopPreview();
                                    mCamera.setPreviewDisplay(mSurfaceHolder);
                                    mCamera.startPreview();
                                    e("MSG_SWITCH_PREVIEW-->mSurfaceHolder-->startPreview");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
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

        if (mCamera == null) {
            e("赋值 camera");
            mCamera = CameraInstance.getInstance().getCameraDevice();
//            try {
//                openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
//            } catch (Exception e) {
//                e.printStackTrace();
//                mHandler.sendEmptyMessage(MSG_CAMERA_ERROR);
//                return;
//            }
//            synchronized (lock) {
//                try {
//                    e("等待camera...");
//                    lock.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

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

            if (mISwitchPreview != null) {
                mISwitchPreview.onSwitchPreview();
            }
        } catch (Exception e) {
            e("重新 录制失败:" + e.getMessage());
            e.printStackTrace();
            mHandler.sendEmptyMessage(MSG_ERROR);
        }

        if (!isStopPreview) {
            mCamera.stopPreview();
            isStopPreview = true;
        }
    }

    private void openCamera(int cameraId) throws Exception {
        if (mCamera != null) {
            return;
        }
        Camera camera = Camera.open(cameraId);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode("off");
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        int width, height;
        width = 1920;
        height = 1080;
        parameters.setPreviewSize(width, height);
        parameters.setPictureSize(width, height);
//            this.mCamera.setDisplayOrientation(90);
//        mCameraPreviewCallback = new CameraPreviewCallback();
//        mCamera.addCallbackBuffer(mImageCallbackBuffer);
//        mCamera.setPreviewCallbackWithBuffer(mCameraPreviewCallback);
//            mCamera.setPreviewCallback(mCameraPreviewCallback);
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains("continuous-video")) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        camera.setParameters(parameters);
        try {
            if (mSurfaceTexture != null) {
                camera.setPreviewTexture(mSurfaceTexture);
            } else if (mSurfaceHolder != null) {
                camera.setPreviewDisplay(mSurfaceHolder);
            } else {
                mHandler.sendEmptyMessage(MSG_NO_PREVIEW);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(MSG_ERROR);
            return;
        }

        mCamera = camera;
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        e("onInfo " + what + " " + extra);
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            e("MEDIA_RECORDER_INFO_MAX_DURATION_REACHED");
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
