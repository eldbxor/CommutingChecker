package com.example.taek.commutingchecker.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Taek on 2016-09-05.
 */
public class NetworkUtil {

    public static int getConnectivityStatus(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo  activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null){
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return Constants.NETWORK_TYPE_WIFI;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return Constants.NETWORK_TYPE_MOBILE;
            }
        }
        return Constants.NETWORK_TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {
        int conn = NetworkUtil.getConnectivityStatus(context);
        String status = null;
        if (conn == Constants.NETWORK_TYPE_WIFI) {
            status = "Wifi enabled";
        } else if (conn == Constants.NETWORK_TYPE_MOBILE) {
            status = "Mobile data enabled";
        } else if (conn == Constants.NETWORK_TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }
}
