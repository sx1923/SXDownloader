package com.jasminx.downloader;

import android.text.TextUtils;

import com.jasminx.downloader.core.DownLoader;
import com.jasminx.downloader.core.DownLoadPool;
import com.jasminx.downloader.entity.DownLoadInfo;
import com.jasminx.downloader.utils.NetworkUtils;

import java.util.HashMap;
import java.util.Iterator;

/**
 * 自动下载管理器
 *
 * @author sunxiao zhouzhitong
 */
public class AutoDownloadManager {

    /**
     * 当前正在下载的文件url
     */
    private static String sDownUrl = null;
    /**
     * 自动下载池
     *
     * @see DownLoadPool 键值是DownloadPool的一个子集
     */
    private static HashMap<String, DownLoadInfo> sAutoDownloaderPool = new HashMap<String,
            DownLoadInfo>();
    /**
     * 同步锁
     */
    private static Object sLockObject = new Object();

    /**
     * 启动后台自动下载-Wifi连接时调用
     */
    public static void startAutoDownload() {
        startAutoDownload(null);
    }

    /**
     * 启动后台自动下载-Wifi连接时调用
     *
     * @param outerDownloadListener
     */
    public static void startAutoDownload(final DownLoadListener outerDownloadListener) {
        if (!NetworkUtils.isWifiConnected()) {
            sDownUrl = null;
            return;
        }
        synchronized (sLockObject) {
            Iterator<String> iterator = sAutoDownloaderPool.keySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            DownLoadInfo info = sAutoDownloaderPool.get(iterator.next());
            if (info == null || TextUtils.isEmpty(info.getUrl())) {
                return;
            }
            // 如果有已经在下载的任务则不下载
            if (!TextUtils.isEmpty(sDownUrl)) {
                return;
            }


            // 下载文件
            DownLoadListener downloadListener = new DownLoadListener() {

                @Override
                public void onStart(String url) {
                    synchronized (sLockObject) {
                        sDownUrl = url;
                    }
                    if (outerDownloadListener != null) {
                        outerDownloadListener.onStart(url);
                    }
                }

                /**
                 * 下载完成
                 */
                @Override
                public void onFinish() {
                    removeDownloaderFromPool(sDownUrl);
                    synchronized (sLockObject) {
                        sDownUrl = null;
                    }
                    if (outerDownloadListener != null) {
                        outerDownloadListener.onFinish();
                    }
                    startAutoDownload();
                }

                /**
                 *
                 */
                @Override
                public void onSuccess(String folderPath, String fileName) {
                    if (outerDownloadListener != null) {
                        outerDownloadListener.onSuccess(folderPath, fileName);
                    }
                }

                /**
                 *
                 */
                @Override
                public void onFail(int errorCode) {
                    if (outerDownloadListener != null) {
                        outerDownloadListener.onFail(errorCode);
                    }
                }

                @Override
                public void onProgressChange(long currentLength,
                                             long fileLength) {
                    if (outerDownloadListener != null) {
                        outerDownloadListener.onProgressChange(currentLength, fileLength);
                    }
                }
            };
            DownLoadPool.getDownLoader(info).start(downloadListener);
        }
    }

    /**
     * 添加到自动下载池，并启动下载
     *
     * @param info
     */
    public static void addToAutoDownloadPool(final DownLoadInfo info) {
        addToAutoDownloadPool(info, null);
    }

    /**
     * 添加到自动下载池，并启动下载
     *
     * @param info
     * @param outerDownLoadListener
     */
    public static void addToAutoDownloadPool(final DownLoadInfo info, final DownLoadListener
            outerDownLoadListener) {
        if (info == null || TextUtils.isEmpty(info.getUrl())) {
            return;
        }
        // 文件名或文件夹为空，返回
        if (TextUtils.isEmpty(info.getFolder())
                || TextUtils.isEmpty(info.getFileName())) {
            return;
        }
        // 将下载器添加到下载池 TODO
        DownLoadPool.addDownloader(info.getUrl(), new DownLoader(null, info));
        addDownloaderToPool(info.getUrl(), info);
        startAutoDownload();
    }

    /**
     * 从自动下载池中删除任务
     *
     * @param key
     */
    private static void removeDownloaderFromPool(String key) {
        synchronized (sLockObject) {
            if (sAutoDownloaderPool.containsKey(key)) {
                sAutoDownloaderPool.remove(key);
            }
        }
    }

    /**
     * 添加任务到自动下载池
     *
     * @param key
     * @param downLoadInfo
     */
    private static void addDownloaderToPool(String key,
                                            DownLoadInfo downLoadInfo) {
        synchronized (sLockObject) {
            if (!sAutoDownloaderPool.containsKey(key)) {
                sAutoDownloaderPool.put(key, downLoadInfo);
            }
        }
    }

}
