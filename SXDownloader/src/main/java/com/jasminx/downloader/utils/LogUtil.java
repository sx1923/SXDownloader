package com.jasminx.downloader.utils;

import android.util.Log;

import com.jasminx.downloader.RunningEnvironment;

/**
 * 日志记录类
 * Created by sunxiao5 on 2016/12/12.
 */

public class LogUtil {
    public static void i(String value) {
        if (RunningEnvironment.DEBUG) {
            Log.i(RunningEnvironment.TAG, value);
        }
    }
}
