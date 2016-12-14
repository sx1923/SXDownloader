package com.jasminx.downloader;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 下载器运行环境
 * Created by sunxiao5 on 2016/12/6.
 */

public class RunningEnvironment {

    public static final String TAG = RunningEnvironment.class.getPackage().getName();
    /**
     * 是否为debug版本，debug会输出日志
     */
    public static final boolean DEBUG = false;

    /**
     * 下载线程数
     */
    public static int sThreadCount;

    /**
     * 全局线程池
     */
    private static ThreadPoolExecutor sThreadPool = (ThreadPoolExecutor) Executors
            .newCachedThreadPool();

    /**
     * 全局线程池
     */
    public static ThreadPoolExecutor threadPool() {
        return sThreadPool;
    }

}
