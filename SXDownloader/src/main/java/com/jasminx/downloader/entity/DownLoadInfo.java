package com.jasminx.downloader.entity;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * 下载所需信息
 *
 * @author sunxiao
 */
public class DownLoadInfo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 下载地址
     */
    private String mUrl;
    /**
     * 文件夹地址
     */
    private String mFolder;
    /**
     * 文件名
     */
    private String mFileName;
    /**
     * 文件验签md5编码，若为空则不校验md5值（如：9ac572bb2698cd238b0821beee468da4）
     */
    private String mFileMac;

    /**
     * 私有化默认构造函数
     */
    public DownLoadInfo(@NonNull String url) {
        mUrl = url;
    }

    public DownLoadInfo setFolder(String folder) {
        mFolder = folder;
        return this;
    }

    public DownLoadInfo setFileName(String fileName) {
        mFileName = fileName;
        return this;
    }

    public DownLoadInfo setFileMac(String fileMac) {
        mFileMac = fileMac;
        return this;
    }


    public String getUrl() {
        return mUrl;
    }

    public String getFolder() {
        return mFolder;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getFileMac() {
        return mFileMac;
    }
}
