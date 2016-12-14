package com.jasminx.downloader.resulthandle;

import android.content.Context;

/**
 * 结果通知器
 *
 * @author sunxiao5
 */
public interface DownLoadResultNotifier {

    /**
     * 进度控制，通知之前的准备工作
     *
     * @param context
     * @return true : 可以通知, false : 取消通知后续步骤
     */
    boolean notifyPrepare(Context context);

    /**
     * 通知下载进度
     *
     * @param currentLength 当前长度
     * @param totalLength   总长度
     */
    void notifyProgressChange(long currentLength, long totalLength);

    /**
     * 结束
     */
    void notifyFinish();

    /**
     * 成功
     *
     * @param message 提示信息
     */
    void notifySuccess(String message);

    /**
     * 错误,做错误处理
     *
     * @param resultCode 错误码
     * @param message    错误信息
     */
    void notifyFailure(int resultCode, String message);
}
