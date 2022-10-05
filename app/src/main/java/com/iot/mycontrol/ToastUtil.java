package com.iot.mycontrol;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

/**
 * @ClassName: ToastUtil
 * @Description:
 * @Author: TangJW
 */
public class ToastUtil {
    public static void toast(Context context, String text) {
        Looper.prepare();
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }
}
