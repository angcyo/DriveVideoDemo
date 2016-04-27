package com.angcyo.drivevideodemo;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

/**
 * Created by robi on 2016-04-24 11:00.
 */
@SuppressWarnings("deprecation")
public class RecorderThread3 extends HandlerThread implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener, Camera.PictureCallback, Camera.ShutterCallback {

    public static final int MAX_DURATION = 10 * 1000;//最常录制时间
    public static final int MSG_START = 0x01;
    public static final int MSG_ERROR = MSG_START << 1;
    public static final int MSG_CAMERA_ERROR = MSG_ERROR << 1;
    public static final int MSG_NO_PREVIEW = MSG_CAMERA_ERROR << 1;
    public static final int MSG_TAKE_PICTURE = MSG_NO_PREVIEW << 1;
    public static final int MSG_SWITCH_PREVIEW = MSG_TAKE_PICTURE << 1;
    public static final int MSG_STOP_RECORDER = MSG_SWITCH_PREVIEW << 1;
    public static final int MSG_RESTART_RECORDER = MSG_STOP_RECORDER << 1;
    static RecorderThread3 mThread;
    Object lock = new Object();
    MediaRecorder mMediaRecorder;
    SurfaceTexture mSurfaceTexture;
    int takePictureCount = 0;
    boolean isMute = false;//拍照静音
    boolean isSetPreview = false;
    Camera mCamera;
    boolean isRecordStart = false;
    private Handler mHandler;

    private RecorderThread3(String name, Camera camera, SurfaceTexture surface) {
        super(name);
        this.mSurfaceTexture = surface;
        mCamera = camera;
        start();
    }


    public static void startThread(Camera camera, SurfaceTexture surface) {
        if (camera == null || surface == null) {
            e("startThread 失败,请检查参数");
            return;
        }

        if (mThread == null) {
            synchronized (RecorderThread3.class) {
                if (mThread == null) {
                    mThread = new RecorderThread3("RecorderThread", camera, surface);
                }
            }
        }
    }

    public static boolean isRecordStart() {
        return mThread != null;
    }

    public static void exitThread() {
        synchronized (RecorderThread3.class) {
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

    public static void stopMediaRecorder() {
        if (mThread != null) {
            mThread.mHandler.sendEmptyMessage(MSG_STOP_RECORDER);
        }
    }

    public static void restartMediaRecorder() {
        if (mThread != null) {
            mThread.mHandler.sendEmptyMessage(MSG_RESTART_RECORDER);
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
        stringBuilder.append("/storage/sdcard1/angcyo1/");
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
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(System.currentTimeMillis());
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
        if (mCamera != null) {
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
                mCamera.takePicture(null, null, this);
            } else {
                mCamera.takePicture(this, null, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            e("拍照异常:" + e.getMessage());
        }
    }


    private void exit() {
        releaseMediaRecorder();
        //请手动释放camera
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

    private void stopRecorder() {
        if (isRecordStart && mMediaRecorder != null) {
            mMediaRecorder.reset();
            if (mCamera != null) {
                mCamera.stopPreview();
            }
            isSetPreview = false;
            isRecordStart = false;
            e("stopRecorder mMediaRecorder.reset()");
        }
    }

    private void restartRecorder() {
        if (!isRecordStart) {
            restartRecorder(getVideoFileName());
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
                        break;
                    case MSG_STOP_RECORDER:
                        stopRecorder();
                        break;
                    case MSG_RESTART_RECORDER:
                        restartRecorder();
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
        isRecordStart = false;

        if (mMediaRecorder == null) {
            createMediaRecorder();
        }
        Camera cameraDevice = mCamera;
        mMediaRecorder.reset();

        if (cameraDevice == null) {
            e(" camera == null, 终止录制");
            return;
        }

        if (!isSetPreview) {
            try {
                cameraDevice.stopPreview();
                cameraDevice.setDisplayOrientation(0);
                cameraDevice.setPreviewTexture(mSurfaceTexture);
                isSetPreview = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        cameraDevice.unlock();
        mMediaRecorder.setCamera(cameraDevice);

        CamcorderProfile mProfile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_HIGH);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(mProfile);

        mMediaRecorder.setOutputFile(filePath);
        mMediaRecorder.setMaxDuration(MAX_DURATION);
        mMediaRecorder.setOnInfoListener(this);
        mMediaRecorder.setOnErrorListener(this);

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            isRecordStart = true;
            e("重新 开始录制:" + filePath);
        } catch (Exception e) {
            e("重新 录制失败:" + e.getMessage());
            e.printStackTrace();
            mHandler.sendEmptyMessage(MSG_ERROR);
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        e("onInfo " + what + " " + extra);
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            restartRecorder(getVideoFileName());
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
}
