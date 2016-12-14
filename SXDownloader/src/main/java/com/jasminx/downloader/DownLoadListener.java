package com.jasminx.downloader;

/**
 * 下载监听器 监听进度变化
 * 线程中的回调，不能用来直接更新UI
 *
 * @author sunxiao5
 */
public interface DownLoadListener {
    /**
     * 开始下载
     */
    void onStart(String url);

    /**
     * 下载进度变化
     *
     * @param currentLength 下载进度
     * @param fileLength    文件总长度（按字节算）
     */
    void onProgressChange(long currentLength, long fileLength);

    /**
     * 下载成功；下载的文件本身存在 不在下载
     *
     * @param folderPath 下载文件夹路径
     * @param fileName   下载文件名
     */
    void onSuccess(String folderPath, String fileName);

    /**
     * 下载失败
     *
     * @param errorCode 失败code
     */
    void onFail(int errorCode);

    /**
     * 下载完成
     */
    void onFinish();
}
