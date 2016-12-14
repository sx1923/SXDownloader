package com.jasminx.downloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jasminx.downloader.core.DownLoader;
import com.jasminx.downloader.entity.DownLoadInfo;
import com.jasminx.downloader.entity.DownLoadResult;
import com.jasminx.downloader.entity.StringConstant;
import com.jasminx.downloader.resulthandle.DownLoadCallBack;
import com.jasminx.downloader.utils.FileHelper;
import com.jasminx.downloader.utils.Md5Utils;
import com.jasminx.downloader.utils.NetworkUtils;

/**
 * 下载管理 对外暴露
 * Created by sunxiao5 on 2016/12/6.
 */

public class DownloadManager {

    public static void startDownload(@NonNull final Context context, @NonNull final DownLoadInfo
            downLoadInfo, @NonNull final DownLoadCallBack downLoadCallBack) {
        final DownLoader downLoader = new DownLoader(context, downLoadInfo);
        DownLoadListener downloadListener = new DownLoadListener() {

            @Override
            public void onStart(String url) {
                downLoadCallBack.notifyPrepare(context);
            }

            /**
             * 通知进度
             * @param currentLength 当前长度
             * @param totalLength 总长度
             */
            @Override
            public void onProgressChange(long currentLength,
                                         long totalLength) {
                downLoadCallBack.notifyProgressChange(currentLength,
                        totalLength);
            }

            @Override
            public void onSuccess(String folderPath, String fileName) {
                //  md5校验成功
                if (TextUtils.isEmpty(downLoadInfo.getFileMac())
                        || downLoadInfo.getFileMac().equals(Md5Utils.md5sum(fileName))) {
                    downLoadCallBack.notifySuccess(folderPath + fileName);
                    return;
                }
                FileHelper.deleteDir(folderPath);
                downLoadCallBack.notifyFailure(DownLoadResult.FAILED_MD5, StringConstant
                        .DOWNLOADFAILED_MD5ERROR);
            }


            @Override
            public void onFail(int errorCode) {
                if (NetworkUtils.checkNetwork(context)) {
                    downLoadCallBack.notifyFailure(
                            DownLoadResult.FAILED_INITCONNECTION, StringConstant
                                    .DOWNLOADFAILED_NONETWORK);
                    return;
                }
                downLoadCallBack.notifyFailure(errorCode, "");
            }


            @Override
            public void onFinish() {
                downLoadCallBack.notifyFinish();
                downLoader.deleteDownloadListener(this);
            }
        };
        downLoader.start(downloadListener);
    }
}
