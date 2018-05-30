package com.xbbdb.utils;

import android.util.Log;

/**
 * LOG信息打印工具类，需要和代码提示模版配套使用
 *
 * @author Administrator
 */
public class LogUtil {
    public static final boolean debug = true; // ture,开启日志打印，false关闭日志打印

    public static void e(String tag, String info) {
        if (!debug) {
            return;
        } else {
            Log.e(tag, info);
        }
    }

    public static void i(String tag, String info) {
        if (!debug) {
            return;
        } else {
            Log.i(tag, info);
        }
    }

    public static void d(String tag, String info) {
        if (!debug) {
            return;
        } else {
            Log.d(tag, info);
        }
    }
}
