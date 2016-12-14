package com.jasminx.downloader.resulthandle;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import static android.R.attr.data;


/**
 * 下载回调
 *
 * @author sunxiao5
 */
public abstract class DownLoadCallBack implements
        DownLoadResultNotifier, DownLoadResultHandler {
    /**
     * 成功消息
     */
    private static final int MESSAGE_SUCCESS = 5270;
    /**
     * 失败消息
     */
    private static final int MESSAGE_FAILURE = 5271;
    /**
     * 下载进度
     */
    private static final int MESSAGE_PERCENT = 5272;
    /**
     * 结束
     */
    private static final int MESSAGE_FINISH = 5273;

    /**
     * 相关的上下文（Context）
     */
    private WeakReference<Context> mContextRef = null;

    /**
     * handler消息到主线程
     */
    protected Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Context context = null;
            if (mContextRef != null) {
                context = mContextRef.get();
                if (context == null) {
                    return;
                } else if (context instanceof Activity
                        && ((Activity) context).isFinishing()) {
                    return;
                }
            }
            switch (msg.what) {
                case MESSAGE_PERCENT: {
                    Object[] params = (Object[]) msg.obj;
                    onProgressChange((Long) params[0], (Long) params[1]);
                    break;
                }
                case MESSAGE_SUCCESS: {
                    Object[] params = (Object[]) msg.obj;
                    onSuccess((String) params[0]);
                    onFinish();
                    break;
                }
                case MESSAGE_FAILURE: {
                    Object[] params = (Object[]) msg.obj;
                    onFailure((Integer) params[0], (String) params[1]);
                    onFinish();
                    break;
                }
                case MESSAGE_FINISH:
                    onFinish();
                    break;
            }
        }

    };

    @Override
    public final boolean notifyPrepare(Context context) {
        if (context != null) {
            mContextRef = new WeakReference<Context>(context);
        } else {
            mContextRef = null;
        }
        return onStart();
    }

    @Override
    public final void notifyProgressChange(long currentLength, long totalLength) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_PERCENT,
                new Object[]{currentLength, totalLength}));
    }

    @Override
    public final void notifyFinish() {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_FINISH));
    }

    @Override
    public final void notifySuccess(String message) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SUCCESS,
                new Object[]{data}));
    }

    @Override
    public final void notifyFailure(int resultCode, String message) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_FAILURE,
                new Object[]{resultCode, message}));
    }

}
