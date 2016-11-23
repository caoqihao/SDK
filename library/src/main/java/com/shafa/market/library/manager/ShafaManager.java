package com.shafa.market.library.manager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.shafa.market.library.bean.ShafaMarketBean;
import com.shafa.market.library.ShafaMarket;
import com.shafa.market.library.download.DownloadDef;
import com.shafa.market.library.download.DownloadInfo;
import com.shafa.market.library.install.InstallCallback;
import com.shafa.market.library.install.InstallMode;
import com.shafa.market.library.install.InstallRunnable;
import com.shafa.market.library.utils.UMessage;

import java.io.File;
import java.util.HashSet;

/**
 * Created by caoqihao on 2016/11/14.
 */

public class ShafaManager {

    private ShafaDownloadManager mShafaDownloadManager;

    private InstallMode mInstallMode;
    private Context mContext;

    private ShafaMarketBean appBean;

    public ShafaManager(Context context) {
        mContext = context.getApplicationContext();
        mShafaDownloadManager = new ShafaDownloadManager(mContext);
        initAppbean();
    }

    private static volatile ShafaManager mSingle;

    public static ShafaManager getInstance(Context mContext) {
        if (null == mSingle) {
            synchronized (ShafaManager.class) {
                mSingle = new ShafaManager(mContext);
            }
        }
        return mSingle;
    }

    public ShafaDownloadManager getShafaDownloadManager() {
        return mShafaDownloadManager;
    }

    public void setInstallMode(InstallMode mode) {
        this.mInstallMode = mode;
    }

    private void initAppbean(){
        appBean = new ShafaMarketBean();
        appBean.apkUrl = DownloadDef.APK_DOWNLOAD_URL ;//"http://pub.shafa.com/download/webwww/sfmkt5tst/latest";
        appBean.packageName = DownloadDef.APK_PACKAGENAME;//"com.shafa.market";
    }

    public ShafaMarketBean getAppBean(){
        return appBean;
    }


    /**
     * 根据包名检测软件是否安装
     *
     * @param packageName
     * @return
     */
    public boolean checkApkInstalledByPackageName(String packageName) {
        try {
            return mShafaDownloadManager.checkApkInstalledByPackageName(packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void installApk(DownloadInfo apkFileInfo , InstallCallback installCallback) {
        if (apkFileInfo != null) {
            performInstallApp( new Handler(), installCallback, apkFileInfo);
        }
    }

    /**
     * 检验签名并安装app
     *
     * @param mHandler
     * @param callback
     * @param info
     */
    public void performInstallApp(Handler mHandler, InstallCallback callback,
                                  DownloadInfo info) {
        try {
            Log.d("size", "run  installApk " + info.mPackageName);
            if (mHandler != null) {
                Thread installThread = new Thread(
                        new CheckApkSignRunnable(mHandler, callback,
                                info));
                installThread.start();
            } else {
                useServiceInstallApp(callback, info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class CheckApkSignRunnable implements Runnable {
        private Handler mHandler;
        private InstallCallback callback;
        private DownloadInfo info;

        CheckApkSignRunnable(Handler handler, InstallCallback tCallback,
                             DownloadInfo tInfo) {
            mHandler = handler;
            callback = tCallback;
            info = tInfo;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            File file = new File(info.mFilePath);
            if (file.exists()) {
                //比较签名和版本号
                boolean canInstall = compareApkBySign(info.mPackageName, info.mFilePath);
                if (canInstall) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            useServiceInstallApp(callback, info);
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // 判断文件是否存在
                            try {
                                if (!TextUtils.isEmpty(info.mFilePath)) {
                                    File file = new File(info.mFilePath);
                                    if (file.exists()) {
                                        if (callback != null) {
                                            callback.onError(info, false);
                                        }
                                    } else {
                                        if (callback != null) {
                                            callback.onError(info, true);
                                        }
                                    }
                                } else {
                                    if (callback != null) {
                                        callback.onError(info, true);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onError(info, true);
                        }
                    }
                });
            }
        }
    }

    /**
     * 检查更新包与安装的apk签名是否相同
     *
     * @param packageName
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean compareApkBySign(String packageName, String apkFilePath) {
        try {
            Signature[] appSign = getAppSign(packageName);
            Signature[] apkFileSign = getApkFileSign(apkFilePath);
            if (appSign == null) {
                return true;
            }
            if (apkFileSign == null) {
                return false;
            }
            HashSet set1 = new HashSet();
            for (Signature sig : appSign) {
                set1.add(sig);
            }
            HashSet set2 = new HashSet();
            for (Signature sig : apkFileSign) {
                set2.add(sig);
            }
            if (set1.equals(set2)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removeInstallPackagePath(String packageName) {
        if (ShafaMarket.removeInstalledApkFile) {
            try {
                if (!TextUtils.isEmpty(packageName)) {
                    String path = mShafaDownloadManager.getApkFilePath(packageName);

                    if(!TextUtils.isEmpty(path)){
                        File file = new File(path);
                        if (file.isFile() && file.exists()) {
                            if (file.delete()) {
                            }
                        }

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public Signature[] getAppSign(String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = mShafaDownloadManager.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }
        if (packageInfo == null) {
            return null;
        } else {
            return packageInfo.signatures;
        }
    }

    public Signature[] getApkFileSign(String apkFilePath) {
        try {
            PackageInfo pi = mShafaDownloadManager.getPackageManager().getPackageArchiveInfo
                    (apkFilePath,
                    PackageManager.GET_SIGNATURES);
            if (pi != null) {
                return pi.signatures;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private InstallCallback mInstallCallback;

    private DownloadInfo mInfos;

    public void useServiceInstallApp(InstallCallback callback,
                                     DownloadInfo info) {

        mInfos = info;
        mInstallCallback = callback;

        try {
            if (mInstallCallback == null) {
                mInstallCallback = new InstallCallback() {

                    @Override
                    public void onSuccess(DownloadInfo apk) {
                    }

                    @Override
                    public void onError(DownloadInfo apk, boolean isMustError) {
                        try {
                            UMessage.showTask(mContext, apk.mPackageName + " install failed");
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onFinish(boolean isAllSuccess) {
                        // Do nothing
                    }

                    @Override
                    public void onInstallActivityStart(DownloadInfo apk) {
                        // Do nothing
                    }

                    @Override
                    public void onStart(DownloadInfo apk) {
                        // TODO Auto-generated method stub

                    }

                };
            }

            if (mInstallMode == null) {
                System.out.println("mInstallMode  is null !!!");
                mInstallMode = new InstallMode(mContext);
                setInstallMode(mInstallMode);
            }
            new Thread(new InstallRunnable(mInstallMode, mContext, mInstallCallback, info)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
