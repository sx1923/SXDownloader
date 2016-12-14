package com.jasminx.downloader.utils;

import android.content.Context;

import java.io.File;

import static com.jasminx.downloader.utils.FileHelper.isSDCardExist;

/**
 * 文件夹路径提供者
 * Created by sunxiao5 on 2016/12/6.
 */

public class FilePathProvider {

    /**
     * 文件下载默认存储的文件夹
     */
    public static final String DOWNLOADFILE_DEFAULT_FOLDER = "download";

    /**
     * 获取内部路径， 路径结尾带分隔符
     *
     * @return /data/data/xxxxx/files/
     */
    private static String getInternalFilePath(Context context) {
        try {
            return context.getFilesDir().getPath() + File.separator;
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * 获取SD卡Android应用程序目录；
     * 若SD卡不存在，返回内部内存根目录，路径结尾带分隔符
     *
     * @return /storage/emulated/0/Android/data/xxxxx/files/
     */
    private static String getAndroidFilePath(Context context) {
        if (!isSDCardExist()) {
            // SD卡不存在，读取内部路径
            return getInternalFilePath(context);
        }
        try {
            return context.getExternalFilesDir(null)
                    .getPath() + File.separator;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取SD卡下载路径，若SD卡不存在，返回内部内存根目录下download目录，路径结尾带分隔符
     *
     * @return /storage/emulated/0/Android/data/xxxxx/files/download/
     */
    public static String getDownloadDefaultPath(Context context) {
        String downloadFilePath = getAndroidFilePath(context) + DOWNLOADFILE_DEFAULT_FOLDER;
        if (!FileHelper.isExist(downloadFilePath)) {
            FileHelper.createFolder(downloadFilePath);
        }
        return downloadFilePath + File.separator;
    }

}
