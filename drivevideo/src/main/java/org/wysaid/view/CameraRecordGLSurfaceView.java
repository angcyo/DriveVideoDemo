package org.wysaid.view;

/**
 * Created by wangyang on 15/7/27.
 */


import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by wangyang on 15/7/17.
 */
public class CameraRecordGLSurfaceView extends CameraGLSurfaceView {

    public static final String effectConfigs[] = {
            "",
            "@beautify bilateral 10 4 1 @style haze -0.5 -0.5 1 1 1 @curve RGB(0, 0)(94, 20)(160, 168)(255, 255) @curve R(0, 0)(129, 119)(255, 255)B(0, 0)(135, 151)(255, 255)RGB(0, 0)(146, 116)(255, 255)",
            "#unpack @blur lerp 0.5", //可调节模糊强度
            "@blur lerp 1", //可调节混合强度
            "#unpack @dynamic wave 1", //可调节速度
            "@dynamic wave 0.5",       //可调节混合
    };

    public CameraRecordGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setFilterWithConfig(effectConfigs[2]);
            }
        }, 100);
    }

    //Not provided by now.
}
