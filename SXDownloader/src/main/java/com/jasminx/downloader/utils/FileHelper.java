package com.jasminx.downloader.utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 文件帮助类
 * Created by sunxiao5 on 2016/12/6.
 */

public class FileHelper {

    /**
     * 文件或文件夹是否存在
     *
     * @param path 文件夹路径
     * @return 是否存在
     */
    public static boolean isExist(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists();
    }

    /**
     * SD卡是否存在
     *
     * @return 是否存在
     */
    public static boolean isSDCardExist() {
        String SDCardState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(SDCardState);
    }

    /**
     * 创建文件夹
     *
     * @param folderPath 文件夹路径
     */
    public static boolean createFolder(String folderPath) {
        if (TextUtils.isEmpty(folderPath)) {
            return false;
        }

        File folder = new File(folderPath);
        if (folder.exists()) {
            if (isFolderReadable(folderPath)) {
                // 路径存在并可以读取，不用重新创建
                return true;
            }
            // 路径存在但是无法读取，删除路径
            folder.delete();
        }
        return folder.mkdirs();
    }

    /**
     * 文件夹是否可以读取
     *
     * @param folderPath 文件夹路径
     * @return 是否
     */
    private static boolean isFolderReadable(String folderPath) {
        File tempFile = new File(folderPath + "temp.txt");
        FileOutputStream tempStream = null;
        try {
            tempStream = new FileOutputStream(tempFile);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                tempStream.close();
            } catch (Exception e) {
            }
            try {
                tempFile.delete();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 删除文件
     *
     * @param filePath
     */
    public boolean deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory())
            return false;
        return file.delete();
    }

    /**
     * 清空目录下所有文件和文件夹
     */
    public static void deleteDir(String rootPath) {
        if (TextUtils.isEmpty(rootPath)) {
            return;
        }
        File file = new File(rootPath);
        if (!file.exists()) {
            return;
        }
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                deleteFolderIncludeSelf(files[i]);
            }
        }
    }

    /**
     * 删除文件和文件夹
     *
     * @param dir
     */
    private static void deleteFolderIncludeSelf(File dir) {
        if (dir == null || !dir.exists())
            return;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files)
                if (file.isDirectory())
                    deleteFolderIncludeSelf(file);
                else
                    file.delete();
            dir.delete();
        } else
            dir.delete();
    }
}
