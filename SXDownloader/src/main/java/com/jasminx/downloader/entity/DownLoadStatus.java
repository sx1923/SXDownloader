package com.jasminx.downloader.entity;
/**
 * 下载状态
 * @author sunxiao5
 *
 */
public class DownLoadStatus {
	/**
	 * 准备下载
	 */
	public static final int READY = 0;
	/**
	 * 下载中
	 */
	public static final int RUNNING = 1;
	/**
	 * 执行完成（包括下载失败的完成）
	 */
	public static final int FINISHED = 2;
}
