package com.jasminx.downloader.core;

import com.jasminx.downloader.entity.DownLoadInfo;

import java.util.HashMap;
import java.util.Iterator;

/**
 * 下载池-所有的下载任务，都存在这个池中
 *
 * @author sunxiao5
 */
public class DownLoadPool {
    /**
     * 下载池中所有下载器
     */

    private static HashMap<String, DownLoader> sDownloaders = new HashMap<String, DownLoader>();
    /**
     *
     */
    private static final Object sLockObject = new Object();

    /**
     * 获取下载器；
     * 若任务已经在下载池中，则直接返回该任务对应的下载器；
     * 若任务不在下载池中，创建该任务对应的下载器，添加到下载池，返回下载器
     *
     * @param info 下载信息
     * @return 下载器
     */
    public static DownLoader getDownLoader(DownLoadInfo info) {
        synchronized (sLockObject) {
            if (sDownloaders.containsKey(info.getUrl())) {
                return sDownloaders.get(info.getUrl());
            }
            DownLoader loader = new DownLoader(info);
            sDownloaders.put(info.getUrl(), loader);
            return loader;
        }
    }

    /**
     * 从下载池中删除任务下载器
     *
     * @param key
     */
    public static void removeDownloader(String key) {
        synchronized (sLockObject) {
            if (sDownloaders.containsKey(key)) {
                sDownloaders.remove(key);
            }
        }
    }

    /**
     * 将任务下载器添加到下载池
     *
     * @param key
     * @param downLoader
     */
    public static void addDownloader(String key, DownLoader downLoader) {
        synchronized (sLockObject) {
            sDownloaders.put(key, downLoader);
        }
    }

    /**
     * 暂停下载
     */
    public static void pauseDownLoad() {
        synchronized (sLockObject) {
            Iterator<String> iterator = sDownloaders.keySet().iterator();
            while (iterator.hasNext()) {
                DownLoader downLoader = sDownloaders.get(iterator.next());
                downLoader.stop(true);
            }
        }
    }
}