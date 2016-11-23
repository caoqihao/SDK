package com.shafa.market.library.bean;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.Serializable;


public class AppInfo implements Serializable {
    private String mPackageName;
    private int mVersionCode;
    private String mVersionName;

    public AppInfo() {
    }

    public AppInfo(String packageName, int versionCode, String versionName) {
        mPackageName = packageName;
        mVersionCode = versionCode;
        mVersionName = versionName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public int getVersionCode() {
        return mVersionCode;
    }

    public String getVersionName() {
        return mVersionName;
    }


    public static AppInfo generateFromLocal(Context context) {
        try {
            if (context != null) {
                return queryLocalAppInfo(context, "cn.beevideo");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


    public static AppInfo queryLocalAppInfo(Context context, String packageName) {
        try {
            if (context != null) {
                final PackageManager manager = context.getPackageManager();
                final PackageInfo info = manager.getPackageInfo(packageName, 0);
                return new AppInfo(info.packageName, info.versionCode, info.versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            //empty
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


}
