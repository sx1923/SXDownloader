package com.jasminx.downloader.entity;

import com.jasminx.downloader.DownLoadListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 下载线程所需要的上下文环境 属性要注意线程安全
 * Created by sunxiao5 on 2016/12/9.
 */

public class DownLoadThreadContext {
    /**
     * 默认下载线程数目
     */
    public static final int DEFAULT_DOWNLOAD_THREAD_COUNT = 2;
    /**
     * 下载线程数
     */
    public int threadCount = DEFAULT_DOWNLOAD_THREAD_COUNT;
    /**
     * 是否支持断点续传
     */
    public boolean supportBreakpoint = true;
    /**
     * 是否停止线程
     */
    public volatile boolean isStopThread = false;
    /**
     * 是否第一次运行
     */
    public volatile boolean isFirstRunThread = true;
    /**
     * 下载线程全部执行完成后，检查文件是否完成
     */
    public CountDownLatch finishedSignal;
    /**
     * 文件长度
     * TODO 保证原子性
     */
    public long fileTotalLength = -1;
    /**
     * 当前已下载的文件总长度
     * 读写锁
     */
    public AtomicLong downloadedTotalLength = new AtomicLong(0);
    /**
     * 下载状态监听器
     * 线程中只读
     */
    public List<DownLoadListener> downloadListeners = new ArrayList<DownLoadListener>();
    /**
     * 记录已下载文件长度 的文件 （分块下载时，要用多个文件保存）
     * 线程中只读
     */
    public List<File> downloadedLengthFileList = new ArrayList<File>();
    /**
     * 文件信息  线程中只读
     */
    public DownLoadFile downloadFile;
    /**
     * 下载需要信息 线程中只读
     */
    public DownLoadInfo downLoadInfo;

    public DownLoadThreadContext(DownLoadInfo info) {
        downLoadInfo = info;
        downloadFile = new DownLoadFile(downLoadInfo);
    }
}
