package com.jasminx.downloader.core;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jasminx.downloader.DownLoadListener;
import com.jasminx.downloader.RunningEnvironment;
import com.jasminx.downloader.entity.DownLoadInfo;
import com.jasminx.downloader.entity.DownLoadResult;
import com.jasminx.downloader.entity.DownLoadStatus;
import com.jasminx.downloader.entity.DownLoadThreadContext;
import com.jasminx.downloader.utils.FileHelper;
import com.jasminx.downloader.utils.FilePathProvider;
import com.jasminx.downloader.utils.ListUtil;
import com.jasminx.downloader.utils.LogUtil;
import com.jasminx.downloader.utils.Md5Utils;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 文件下载器
 *
 * @author sunxiao
 */
public class DownLoader {
    /**
     * 线程同步锁
     */
    private final Object mLockObject = new Object();
    /**
     * 下载线程上下文
     */
    private DownLoadThreadContext mContext;
    /**
     * 下载状态
     */
    private volatile int mDownloadStatus = DownLoadStatus.READY;
    /**
     * 结果通知 当下载失败或者暂停时，是否需要发出通知
     */
    private volatile boolean mNeedResultNofity = true;
    /**
     * 下载结果
     */
    private int mDownloadResult = DownLoadResult.UNKNOWN;
    /**
     * 下载线程队列
     */
    private List<DownLoadThread> mDownloadThreads = new
            ArrayList<DownLoadThread>();

    /**
     * 私有默认构造函数
     */
    @SuppressWarnings("unused")
    private DownLoader() {
    }

    /**
     * 构造函数
     *
     * @param info 下载文件信息
     */
    public DownLoader(@NonNull Context context, @NonNull DownLoadInfo info) {
        if (TextUtils.isEmpty(info.getFileName())) {
            info.setFileName(info.getUrl().substring(info.getUrl()
                    .lastIndexOf('/') + 1, info
                    .getUrl().length()));
        }
        if (TextUtils.isEmpty(info.getFolder())) {
            info.setFolder(FilePathProvider.getDownloadDefaultPath(context));
        }
        mContext = new DownLoadThreadContext(info);
    }

    /**
     * 开始下载  开启子线程开始下载
     * 可以向已经执行的下载器中添加监听器
     *
     * @param downloadListener 下载监听器
     */
    public void start(@NonNull DownLoadListener downloadListener) {
        if (mContext.downLoadInfo == null) {
            throw new IllegalArgumentException("download info is null");
        }
        switch (mDownloadStatus) {
            case DownLoadStatus.READY:
                // 修改下载状态
                mDownloadStatus = DownLoadStatus.RUNNING;
                // 下载状态为等待：添加监听器，开始下载
                addDownloadListener(downloadListener);
                // 通知下载开始
                downloadListener.onStart(mContext.downLoadInfo.getUrl());
                startDownload();
                break;
            case DownLoadStatus.RUNNING:
                // 下载状态为下载中：添加监听器，对外通知开始事件
                addDownloadListener(downloadListener);
                downloadListener.onStart(mContext.downLoadInfo.getUrl());
                break;
            case DownLoadStatus.FINISHED:
                // 下载状态为下载完： 对外通知开始事件，以及下载结果
                downloadListener.onStart(mContext.downLoadInfo.getUrl());
                if (mDownloadResult == DownLoadResult.SUCCESS) {
                    downloadListener.onSuccess(mContext.downLoadInfo.getFolder(), mContext
                            .downLoadInfo.getFileName());
                } else {
                    downloadListener.onFail(mDownloadResult);
                }
                downloadListener.onFinish();
                break;
            default:
                break;
        }
    }

    /**
     * 停止下载
     *
     * @param needNofify 是否需要抛出通知
     */
    public void stop(boolean needNofify) {
        mContext.isStopThread = true;
        mNeedResultNofity = needNofify;
    }

    /**
     * 开始下载，启动线程进行初始化下载连接，分块下载
     */
    private void startDownload() {
        RunningEnvironment.threadPool().execute(new Runnable() {
            @Override
            public void run() {
                // 初始化下载连接，获取文件长度
                initConnection();
                if (mContext.downloadFile.checkFile(mContext.downLoadInfo, mContext
                        .fileTotalLength)) {
                    // 下载目标文件已完毕
                    downloadFinished(DownLoadResult.SUCCESS);
                    return;
                }
                initDownloadThread();
                startDownloadThread();
                try {
                    mContext.finishedSignal.await();
                } catch (Exception e) {
                }
                verifyDownloadedFile();
            }
        });
    }

    /**
     * 初始化网络连接，获取下载文件长度
     */
    private void initConnection() {
        HttpURLConnection conn = null;
        try {
            conn = new DownLoadConnection(mContext).init();
            if (conn == null || mContext.fileTotalLength <= 0) {
                // 下载连接初始化失败
                downloadFinished(DownLoadResult.FAILED_INITCONNECTION);
                return;
            }
            LogUtil.i("fileTotalLength:" + mContext.fileTotalLength);
        } finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                    conn = null;
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 初始化下载线程 确定每个线程下载的开始结束位
     */
    private void initDownloadThread() {
        // 设置已下载长度为0
        mContext.downloadedTotalLength = new AtomicLong(0);
        // 每快大小
        long blockSize = mContext.fileTotalLength / mContext.threadCount;
        for (int threadId = 1; threadId <= mContext.threadCount; threadId++) {
            long startIndex = (threadId - 1) * blockSize;
            long endIndex = threadId * blockSize - 1;
            if (threadId == mContext.threadCount) {
                // 最后一个线程下载到末尾
                endIndex = mContext.fileTotalLength;
            }
            mDownloadThreads.add(new DownLoadThread(mContext, threadId,
                    startIndex,
                    endIndex));
            mContext.downloadedLengthFileList
                    .add(new File(mContext.downloadFile.getmDownloadFolder(),
                            "."
                                    + mContext.downLoadInfo.getFileName() +
                                    "_" +
                                    threadId
                                    + ".length"));
            LogUtil.i("线程" + threadId + "--startIndex:" + startIndex +
                    "endIndex:" + endIndex);
        }
    }

    /**
     * 启动线程开始下载
     */
    private void startDownloadThread() {
        mContext.finishedSignal = new CountDownLatch(mContext.threadCount);
        for (int threadId = 1; threadId <= mContext.threadCount; threadId++) {
            mDownloadThreads.get(threadId - 1).start();
        }
    }

    /**
     * 校验下载完成的文件
     */
    private void verifyDownloadedFile() {
        // 下载长度小于文件长度，说明下载未完成（失败）
        if (mContext.downloadedTotalLength.get() < mContext.fileTotalLength) {
            downloadFinished(DownLoadResult.FAILED_FILE);
            return;
        }
        // mac存在 且mac校验失败
        if (!TextUtils.isEmpty(mContext.downLoadInfo.getFileMac())
                && !mContext.downLoadInfo.getFileMac().equals(
                Md5Utils.md5sum(mContext.downloadFile.getmDownloadFile()
                        .getAbsolutePath()))) {
            FileHelper.deleteDir(mContext.downLoadInfo.getFolder());
            downloadFinished(DownLoadResult.FAILED_MD5);
            return;
        }
        downloadFinished(DownLoadResult.SUCCESS);
    }

    /**
     * 下载进度改变时的通知
     *
     * @param downloadedLength 已下载文件长度
     */
    private void notifyProcessing(long downloadedLength) {
        for (int i = 0; i < mContext.downloadListeners.size(); i++) {
            mContext.downloadListeners.get(i).onProgressChange(downloadedLength,
                    mContext.fileTotalLength);
        }
    }

    /**
     * 下载完成
     *
     * @param downloadResult 是否下载成功
     */
    private void downloadFinished(int downloadResult) {
        // 清空下载所用资源
        disposeDownloader();
        // 设置下载是否成功
        mDownloadResult = downloadResult;
        // 下载状态设置为完成
        mDownloadStatus = DownLoadStatus.FINISHED;
        // 向外界通知下载结果
        notifyDownloadResult();
    }

    /**
     * 向外界通知下载结果
     */
    private void notifyDownloadResult() {
        if (mNeedResultNofity) {
            // 需要通知成功或失败的结果
            if (mDownloadResult == DownLoadResult.SUCCESS) {
                for (int i = 0; i < mContext.downloadListeners.size(); i++) {
                    mContext.downloadListeners.get(i).onSuccess(
                            mContext.downLoadInfo.getFolder(),
                            mContext.downLoadInfo.getFileName());
                }
            } else {
                for (int i = 0; i < mContext.downloadListeners.size(); i++) {
                    mContext.downloadListeners.get(i).onFail(mDownloadResult);
                }
            }
        }
        for (int i = 0; i < mContext.downloadListeners.size(); i++) {
            mContext.downloadListeners.get(i).onFinish();
        }
    }

    /**
     * 清空下载资源
     */
    private void disposeDownloader() {
        // 在正在下载池中删除自己
        DownLoadPool.removeDownloader(mContext.downLoadInfo.getUrl());
        // 停止并清空下载线程
        clearDownloadThreads();
    }

    /**
     * 清空下载线程
     */
    private void clearDownloadThreads() {
        if (!ListUtil.isEmpty(mDownloadThreads)) {
            return;
        }
        for (int i = 0; i < mDownloadThreads.size(); i++) {
            try {
                mDownloadThreads.get(i).interrupt();
                mDownloadThreads.get(i).join();
            } catch (Exception e) {

            }
        }
        mDownloadThreads.clear();
    }


    /**
     * 添加监听器
     *
     * @param downloadListener
     */
    private void addDownloadListener(DownLoadListener downloadListener) {
        synchronized (mLockObject) {
            if (downloadListener != null) {
                mContext.downloadListeners.add(downloadListener);
            }
        }
    }

    /**
     * 删除下载监听器
     *
     * @param downloadListener
     */
    public void deleteDownloadListener(DownLoadListener downloadListener) {
        synchronized (mLockObject) {
            if (downloadListener != null) {
                mContext.downloadListeners.remove(downloadListener);
            }
        }
    }

    /**
     * 清除下载文件目录
     */
    private void clearDownLoadFolder() {
        if (mContext.downLoadInfo.getFolder() == null) {
            return;
        }
        FileHelper.deleteDir(mContext.downLoadInfo.getFolder());
    }

}
