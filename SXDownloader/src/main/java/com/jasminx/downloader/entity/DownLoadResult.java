package com.jasminx.downloader.entity;

public class DownLoadResult {
    /**
     * 未知
     */
    public static final int UNKNOWN = -1;
    /**
     * 成功
     */
    public static final int SUCCESS = 0;
    /**
     * MD5错误
     */
    public static final int FAILED_MD5 = 1;
    /**
     * 文件错误
     */
    public static final int FAILED_FILE = 2;
    /**
     * 网络链接建立错误 (无法访问文件下载地址)
     */
    public static final int FAILED_INITCONNECTION = 3;
    /**
     * 无网络连接
     */
    public static final int FAILED_NONETWORK = 4;

}
