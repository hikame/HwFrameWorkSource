package com.huawei.facerecognition.utils;

import android.util.Log;

public class LogUtil {
    private static final boolean HWDBG;
    private static final boolean HWERROR = true;
    private static final boolean HWINFO;
    private static final String SEPARATOR = " - ";
    private static final String TAG = "FaceR";

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        HWDBG = isLoggable;
        if (!Log.HWINFO) {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false;
        }
        HWINFO = z;
    }

    public static void v(String tag, String msg) {
        if (HWDBG) {
            Log.v(TAG, tag + SEPARATOR + msg);
        }
    }

    public static void v(String tag, String... msg) {
        if (HWDBG) {
            Log.v(TAG, tag + SEPARATOR + appendString(msg));
        }
    }

    public static void d(String tag, String msg) {
        if (HWDBG) {
            Log.d(TAG, tag + SEPARATOR + msg);
        }
    }

    public static void d(String tag, String... msg) {
        if (HWDBG) {
            Log.d(TAG, tag + SEPARATOR + appendString(msg));
        }
    }

    public static void i(String tag, String msg) {
        if (HWINFO) {
            Log.i(TAG, tag + SEPARATOR + msg);
        }
    }

    public static void i(String tag, String... msg) {
        if (HWINFO) {
            Log.i(TAG, tag + SEPARATOR + appendString(msg));
        }
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + SEPARATOR + msg);
    }

    public static void w(String tag, String... msg) {
        w(tag, appendString(msg));
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + SEPARATOR + msg);
    }

    private static String appendString(String... msg) {
        StringBuilder builder = new StringBuilder();
        for (String s : msg) {
            builder.append(s);
            builder.append(" ");
        }
        return builder.toString().trim();
    }
}
