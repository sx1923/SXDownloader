package com.jasminx.downloader.core;


import com.jasminx.downloader.entity.DownLoadThreadContext;
import com.jasminx.downloader.utils.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 执行下载线程
 *
 * @author sunxiao5
 */
public class DownLoadThread extends Thread {
    /**
     * 每次读取流的长度
     */
    private static final int DOWNLOADFILE_BUFFSIZE = 1024 * 20;
    /**
     * 线程ID
     */
    private int mThreadId;
    /**
     * 下载文件开始位置
     */
    private long mStartIndex;
    /**
     * 下载文件结束位置
     */
    private long mEndIndex;
    /**
     * 是否是首次执行线程下载
     */
    private boolean mIsFirst = true;
    /**
     * 下载线程上下文
     */
    private DownLoadThreadContext mContext;

    /**
     * 构造函数
     *
     * @param context  下载线程所需上下文
     * @param threadId 线程ID
     * @param start    开始位置
     * @param end      结束位置
     */
    public DownLoadThread(DownLoadThreadContext context, int threadId, long start, long end) {
        mContext = context;
        mThreadId = threadId;
        mStartIndex = start;
        mEndIndex = end;
    }

    /**
     * 执行下载操作，重试三次
     */
    @Override
    public void run() {
        for (int i = 0; i < 3; i++) {
            boolean needRetry = downloadFile();
            if (!needRetry || mContext.isStopThread) {
                // 只有在需要重试，且线程为运行状态时，执行重试
                break;
            }
        }
        mContext.finishedSignal.countDown();
    }

    /**
     * 下载文件
     *
     * @return 是否需要重试
     */
    public boolean downloadFile() {
        boolean needRetry = false;
        // 下载的文件流
        InputStream downloadFileStream = null;
        // 下载的文件
        RandomAccessFile downloadFileRaf = null;
        // 记录文件下载长度的文件
        RandomAccessFile downloadLengthFileRaf = null;
        // 上一次下载到的位置 (默认为当前线程下载开始位置)
        long downloadIndexOfLast = mStartIndex;
        HttpURLConnection conn = null;
        try {
            // 获取记录当前线程下载长度的文件
            File downloadLengthFile = mContext.downloadedLengthFileList
                    .get(mThreadId - 1);
            long downloadedFileLength = 0;
            if (mContext.supportBreakpoint) {
                // 获取已下载文件长度
                downloadedFileLength = calcDownloadedFileLength();
                // 线程开始位置 + 已下载文件长度
                downloadIndexOfLast += downloadedFileLength;
            } else {
                // 不支持断点续传，每次启动下载，都是从头开始下载（单线程）
                mContext.downloadedTotalLength = new AtomicLong(0);
            }

            // 文件当前下载长度大于文件结束位置，说明已经下载完
            if (downloadIndexOfLast >= mEndIndex) {
                return false;
            }

            // 线程在外部停止了，不需要进行重试下载
            if (!isThreadRunning()) {
                return false;
            }

            // 初始化连接失败 返回需要重试
            conn = new DownLoadConnection(mContext).init(downloadIndexOfLast, mEndIndex);
            if (conn == null) {
                return true;
            }

            // 定位随机写文件时候在那个位置开始写
            downloadFileRaf = new RandomAccessFile(mContext.downloadFile.getmDownloadFile(), "rwd");
            downloadFileRaf.seek(downloadIndexOfLast);

            int downLoadBuffLength = 0;
            byte[] downLoadBuff = new byte[DOWNLOADFILE_BUFFSIZE];

            downloadFileStream = conn.getInputStream();
            downloadLengthFileRaf = new RandomAccessFile(
                    downloadLengthFile, "rwd");
            while (isThreadRunning()) {
                // 读取的流长度为0 退出循环
                downLoadBuffLength = downloadFileStream.read(downLoadBuff);
                if (downLoadBuffLength <= 0) {
                    break;
                }

                // 将下载的文件流写入文件
                downloadFileRaf.write(downLoadBuff, 0, downLoadBuffLength);

                if (mContext.supportBreakpoint) {
                    downloadedFileLength += downLoadBuffLength;

                    // 将下载的文件的长度 写入记录文件长度的文件
                    downloadLengthFileRaf.seek(0);
                    downloadLengthFileRaf.write(String.valueOf(
                            downloadedFileLength).getBytes());
                }

                if (isThreadRunning()) {
                    // 当写入操作完成后，发现如果线程已经停止，不通知
                    long downloadedLength = mContext.downloadedTotalLength.addAndGet(downLoadBuffLength);
                    notifyProcessing(downloadedLength);
                }
            }
        } catch (Exception e) {
            needRetry = true;
        } finally {
            if (downloadFileStream != null) {
                try {
                    downloadFileStream.close();
                } catch (IOException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e2) {
                }
            }
            if (downloadFileRaf != null) {
                try {
                    downloadFileRaf.close();
                } catch (IOException e) {
                }
            }
            if (downloadLengthFileRaf != null) {
                try {
                    downloadLengthFileRaf.close();
                } catch (Exception e) {
                }
            }
        }
        return needRetry;
    }

    /**
     * 计算已经下载的文件长度
     *
     * @return 已经下载的文件长度
     */
    private long calcDownloadedFileLength() {
        FileInputStream tempFile = null;
        long fileLength = 0;
        try {
            File downloadLengthInfoFile = mContext.downloadedLengthFileList
                    .get(mThreadId - 1);
            if (downloadLengthInfoFile.exists()
                    && downloadLengthInfoFile.length() > 0) {
                tempFile = new FileInputStream(downloadLengthInfoFile);
                byte[] temp = new byte[1024];
                int len = tempFile.read(temp);
                // 读取文件信息，获得已下载的文件长度
                fileLength = Integer.parseInt(new String(temp, 0, len));
                if (mIsFirst) {
                    // 第一次进入下载线程，将已经下载的长度通知到外界。
                    long downloadLength = mContext.downloadedTotalLength.addAndGet(fileLength);
                    notifyProcessing(downloadLength);
                }
            }
            mIsFirst = false;
        } catch (Exception e) {

        } finally {
            if (tempFile != null) {
                try {
                    tempFile.close();
                } catch (Exception e) {
                }
            }
        }
        LogUtil.i("lastDownloadedLength:" + fileLength);
        return fileLength;
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
        LogUtil.i("notifyProcessing -- downloadedLength: " + downloadedLength);
    }


    /**
     * 线程正在执行
     *
     * @return 是否正在运行
     */
    private boolean isThreadRunning() {
        return !isInterrupted() && !mContext.isStopThread;
    }
}