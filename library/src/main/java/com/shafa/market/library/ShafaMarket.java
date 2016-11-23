package com.shafa.market.library;

import android.content.Context;

import com.shafa.market.library.download.DownloadDef;
import com.shafa.market.library.manager.ShafaManager;

/**
 * Created by caoqihao on 2016/11/14.
 */

public class ShafaMarket {
    public static boolean removeInstalledApkFile = true;

    /**
     *
     * @param mContext
     * @param packageName
     *
     * 根据包名跳转到相应详情页面。如果只是检查本应用是否有跟新，不建议使用。
     */
    public static synchronized void start(Context mContext , String packageName){

        AppUpdate.startActivity(mContext , packageName , ShafaManager.getInstance(mContext).checkApkInstalledByPackageName(DownloadDef.APK_PACKAGENAME));

    }

    /**
     *
     * @param mContext
     * @param updateListener 允许为空，空的时候：
     *                       1.沙发管家已经安装：直接跳转到沙发管家的详情页面进行更新
     *                       2.沙发管家未安装：进到沙发管家下载页面直接下载沙发管家，安装完成后跳转至详情（保持在沙发管家下载页面）
     */
    public static synchronized void checkUpdate(Context mContext , AppUpdateListener updateListener){
        AppUpdate.setUpdateListener(updateListener);
        AppUpdate.update(mContext , null , null);
    }
}
