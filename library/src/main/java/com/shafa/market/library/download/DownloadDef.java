package com.shafa.market.library.download;

import android.text.TextUtils;

/**
 * Created by caoqihao on 2016/11/15.
 */

public class DownloadDef {

    public static final String ACTION_DOWNLOAD_CHANGE = "com.shafa.download.change";

    public static final String ACTION_DOWNLOAD_OVER = "com.shafa.download.over";

    public static final String ACTION_DOWNLOAD_FAILED = "com.shafa.download.failed";

    public static final String EXTRA_URI = "com.shafa.extra.uri";

    public static final String EXTRA_NAME = "com.shafa.extra.name";

    public static final String EXTRA_DOWNLOAD_INFO="com.shafa.extra.download_info";

    public static final String EXTRA_NOW_BYTES = "com.shafa.extra.nowbytes";

    public static final String EXTRA_TOTAL_BYTES = "com.shafa.extra.totalbytes";


    public static final String APK_DOWNLOAD_URL = "http://pub.sfgj.org/download/sflauncher/shafa_market.apk";
    public static final String APK_PACKAGENAME = "com.shafa.market";
    public static final String APK_CLASS = "com.shafa.market.modules.detail.AppDetailAct";

    public static final String GET_DOWNLOAD_URL = "http://pub.sfgj.org/api/version/0?channel=update_sdk&src_pkg=";

    public static final String OPEN_API_SERVER = "http://openapi.shafa.com/sdk/app_by_pkg.json";

    public static String APP_KEY = "58353653358e1fa22d8b4568"; //key

    public static String SECRET_KEY = "jN0SCem72hOA56jGg5A5nQP6fGT89Gh1"; //密钥

    public static boolean isPreReady() {
        return !TextUtils.isEmpty(APP_KEY) && !TextUtils.isEmpty(SECRET_KEY);
    }

}
