package com.jasminx.downloader.resulthandle;

/**
 * 结果处理
 * Created by sunxiao5 on 2016/12/13.
 */

public interface DownLoadResultHandler<T> {
    /**
     * 开始
     *
     * @return 是否开始
     */
    boolean onStart();

    /**
     * 通知下载进度
     *
     * @param currentLength 当前长度
     * @param totalLength   总长度
     */
    void onProgressChange(long currentLength, long totalLength);

    /**
     * 结束
     */
    void onFinish();

    /**
     * 成功
     *
     * @param data 返回的实体对象
     */
    void onSuccess(String data);

    /**
     * 错误,做错误处理
     *
     * @param resultCode 错误码
     * @param message    错误信息
     */
    void onFailure(int resultCode, String message);
}
