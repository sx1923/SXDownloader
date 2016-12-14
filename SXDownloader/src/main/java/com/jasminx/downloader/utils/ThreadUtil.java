package com.jasminx.downloader.utils;

/**
 * 线程帮助类
 * Created by sunxiao5 on 2016/12/12.
 */

public class ThreadUtil {

    public static final String MAINTHREADNAME = "main";

    /**
     * 是否在主线程中
     *
     * @return
     */
    public static boolean isInMainThread() {
        return MAINTHREADNAME.equalsIgnoreCase(Thread.currentThread().getName());
    }
}
