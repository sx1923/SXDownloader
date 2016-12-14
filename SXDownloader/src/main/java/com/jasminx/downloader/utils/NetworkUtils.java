package com.jasminx.downloader.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import com.jasminx.downloader.RunningEnvironment;


/**
 * 网络帮助类
 * Created by sunxiao5 on 2016/12/6.
 */

public class NetworkUtils {

    /**
     * 判断是否连接wifi
     *
     * @return 是否连接
     */
    public static boolean isWifiConnected() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) RunningEnvironment.sAppContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiNetworkInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetworkInfo.isConnected()) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }


    /**
     * 检查网络情况
     *
     * @return true:畅通，false:不畅通
     */
    public static boolean checkNetwork(@NonNull Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }

        return false;
    }
}
