package com.jasminx.downloader.core;

import com.jasminx.downloader.entity.DownLoadThreadContext;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载的网络连接
 * Created by sunxiao5 on 2016/12/9.
 */

public class DownLoadConnection {
    /**
     * 连接超时时间 3000毫秒
     */
    public static final int HTTPURLCONNECTION_CONNECTTIMEOUT = 3000;
    /**
     * 读取超时时间 3000毫秒
     */
    public static final int HTTPURLCONNECTION_READTIMEOUT = 3000;
    /**
     * 请求方式
     */
    public static final String HTTPURLCONNECTION_REQUESTMETHOD = "GET";
    /**
     * 使用缓存
     */
    public static final boolean HTTPURLCONNECTION_USECACHES = false;
    /**
     * 下载线程所需的上下文
     */
    private DownLoadThreadContext mContext;

    public DownLoadConnection(DownLoadThreadContext context) {
        mContext = context;
    }

    /**
     * 初始化连接，设置文件长度
     *
     * @return 下载连接
     */
    public HttpURLConnection init() {
        return init(-1, -1);
    }

    /**
     * 初始化连接，在指定位置开始下载
     *
     * @param start   下载开始位置
     * @param end     下载结束位置
     * @return 下载连接
     */
    public HttpURLConnection init(long start, long end) {
        HttpURLConnection conn = null;
        long fileLength = 0;
        int size = 0;
        try {
            while (fileLength <= 0 && size <= 10) {
                if (conn != null) {
                    conn.disconnect();
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                }
                size++;
                URL url = new URL(mContext.downLoadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(HTTPURLCONNECTION_CONNECTTIMEOUT);
                conn.setReadTimeout(HTTPURLCONNECTION_READTIMEOUT);
                conn.setUseCaches(HTTPURLCONNECTION_USECACHES);
                conn.setRequestMethod(HTTPURLCONNECTION_REQUESTMETHOD);
                conn.setRequestProperty("Accept-Encoding", "identity");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Pragma", "no-cache");
                if (mContext.supportBreakpoint) {
                    conn.setRequestProperty("Range", "bytes=" + start + "-"
                            + end);
                }
                conn.connect();

                fileLength = conn.getContentLength();

                // 当第一次初始化连接，为了获取文件长度时，若获取长度失败，则不支持断点续传
                if (start == -1) {
                    mContext.fileTotalLength = fileLength;
                    if (mContext.fileTotalLength < 0 && size >= 4) {
                        mContext.supportBreakpoint = false;
                    }
                    if (!mContext.supportBreakpoint) {
                        // 不支持断点续传，不能启动多线程分块下载
                        mContext.threadCount = 1;
                    }
                }
            }
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.disconnect();
                    conn = null;
                } catch (Exception e2) {
                }
            }
        }
        return conn;
    }
}
