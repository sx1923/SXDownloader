package com.jasminx.downloader.entity;

import android.text.TextUtils;

import com.jasminx.downloader.utils.FileHelper;
import com.jasminx.downloader.utils.Md5Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

/**
 * 文件信息
 * Created by sunxiao5 on 2016/12/12.
 */

public class DownLoadFile {

    public static final String MD5FILE_EXTENSION = ".md5";
    /**
     * 待下载文件所在文件夹
     */
    private File mDownloadFolder;
    /**
     * 待下载的文件
     */
    private File mDownloadFile;
    /**
     * MD5值保存文件
     */
    private File mMD5File;

    public DownLoadFile(DownLoadInfo downLoadInfo) {
        mDownloadFolder = new File(downLoadInfo.getFolder());
        mDownloadFile = new File(mDownloadFolder, downLoadInfo.getFileName());
        mMD5File = new File(mDownloadFolder, "." + downLoadInfo.getFileName()
                + MD5FILE_EXTENSION);
    }


    /**
     * 检查文件长度及MD5
     * 删除下载的不正确的文件
     *
     * @return true：可以使用； false:不可以使用，需要下载
     */
    public boolean checkFile(DownLoadInfo downLoadInfo, long fileTotalLength) {
        // 本地文件不存在，返回检查失败
        if (!mDownloadFile.exists()) {
            return checkFailed(downLoadInfo);
        }
        // 没有MD5 , 本地文件与流文件长度相同，返回检查成功
        if (TextUtils.isEmpty(downLoadInfo.getFileMac())
                && mDownloadFile.length() == fileTotalLength) {
            return true;
        }
        // 本地文件计算的md5与外部传递的MD5一样 返回检查成功
        String downloadFileMD5 = Md5Utils.md5sum(mDownloadFile.getAbsolutePath());
        if (downLoadInfo.getFileMac().equals(downloadFileMD5)) {
            return true;
        }

        // 本地记录的文件MD5值 与外部传递MD5不一样，清空下载的文件
        downloadFileMD5 = getLocalMD5(downLoadInfo);
        if (TextUtils.isEmpty(downloadFileMD5) || !downLoadInfo.getFileMac().equals
                (downloadFileMD5)) {
            return checkFailed(downLoadInfo);
        }

        initMD5File(downLoadInfo.getFileMac());

        return false;
    }

    /**
     * 获取本地记录的MD5值
     *
     * @return MD5
     */
    private String getLocalMD5(DownLoadInfo downLoadInfo) {
        if (mMD5File.exists()) {
            return null;
        }
        FileInputStream tempFile = null;
        try {
            tempFile = new FileInputStream(mMD5File);
            byte[] temp = new byte[downLoadInfo.getFileMac().length() + 1];
            int len = tempFile.read(temp);
            return new String(temp, 0, len);
        } catch (Exception e) {
            return null;
        } finally {
            if (tempFile != null) {
                try {
                    tempFile.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 将MD5值记录到本地文件
     */
    private void initMD5File(String md5) {
        if (mMD5File.exists()) {
            return;
        }
        FileWriter fileWriter = null;
        try {
            mMD5File.createNewFile();

            fileWriter = new FileWriter(mMD5File);
            fileWriter.write(md5);
            fileWriter.flush();
        } catch (Exception e) {
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (Exception e) {
            }
        }
    }

   private boolean checkFailed(DownLoadInfo downLoadInfo) {
        FileHelper.deleteDir(downLoadInfo.getFolder());
        return false;
    }

    public File getmDownloadFolder() {
        return mDownloadFolder;
    }

    public File getmDownloadFile() {
        return mDownloadFile;
    }

    public File getmMD5File() {
        return mMD5File;
    }
}
