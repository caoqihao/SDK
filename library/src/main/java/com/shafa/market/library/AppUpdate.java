package com.shafa.market.library;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.shafa.market.library.activity.ShafaDownloadAct;
import com.shafa.market.library.bean.AppInfo;
import com.shafa.market.library.download.DownloadDef;
import com.shafa.market.library.net.UpdateResult;
import com.shafa.market.library.net.UpdateRequest;

import java.lang.ref.WeakReference;


public final class AppUpdate {

    private static final String TAG = "AppUpdate";

    private static AppUpdateListener sUpdateListener;

    private static WeakReference<Context> sContext;

    private static volatile boolean sUpdateing = false;


    public enum UpdateStatus {
        /**
         * 有更新并且安装了沙发管家
         */
        NEW_UPDATE,
        /**
         * 有更新，但是没有安装沙发管家
         */
        NO_SUCH_SHAFAMARKET,
        /**
         * 没有更新
         */
        NO_UPDATE
    }

    /**
     * 设置更新回调
     *
     * @param updateListener
     */
    @Nullable
    public static void setUpdateListener(AppUpdateListener updateListener) {
        sUpdateListener = updateListener;
    }

    /**
     * 检查更新
     *
     * @param context
     * @param appKey    @Nullable
     * @param secretKey @Nullable
     */
    public static void update(Context context, String appKey, String secretKey) {
        if (context == null) {
            return;
        } else {
            sContext = new WeakReference<Context>(context);
        }

        if(!TextUtils.isEmpty(appKey)){
            DownloadDef.APP_KEY = appKey;
        }
        if(!TextUtils.isEmpty(secretKey)){
            DownloadDef.SECRET_KEY = secretKey;
        }

        if (!DownloadDef.isPreReady()) {
            return;
        }

        update();
    }

    private static void update() {

        if (!sUpdateing) {
            sUpdateing = true;

//            String pkg = sContext.get().getPackageName();
            String pkg = "cn.beevideo";

            UpdateRequest request = new UpdateRequest(pkg);

            request.setOnRequestCallback(new UpdateRequest.OnRequestCallback() {
                @Override
                public void onResponse(UpdateResult result) {
                    sUpdateing = false;
                    handleUpdate(result);
                }
            });

            request.requestUpdateData();
        }
    }


    private static void handleUpdate(final UpdateResult result) {
        if (sContext == null) {
            return;
        }
        try {
            AppInfo localApp = AppInfo.generateFromLocal(sContext.get());
            Handler handler = new Handler(Looper.getMainLooper());

            if (localApp != null && result != null && TextUtils.equals(localApp.getPackageName(), result.getPackageName())) {

                final boolean hasNewVersion = result.getVersionCode() > localApp.getVersionCode();

                UpdateStatus status;

                if (hasNewVersion) {

                    boolean installed = isInstalledShafaMarket(sContext.get());
                    status = installed ? UpdateStatus.NEW_UPDATE : UpdateStatus.NO_SUCH_SHAFAMARKET;

                } else {
                    status = UpdateStatus.NO_UPDATE;
                }

                final UpdateStatus updateStatus = status;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (sUpdateListener != null) {
                            sUpdateListener.onUpdateResult(updateStatus, result);
                        } else {
                            handleDefaultUpdate(updateStatus, result);
                        }
                    }
                });


            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    private static void handleDefaultUpdate(AppUpdate.UpdateStatus status, UpdateResult result) {
        switch (status) {
            case NEW_UPDATE:
                startActivity(sContext.get() , "cn.beevideo" , true);
                break;
            case NO_SUCH_SHAFAMARKET:
                startActivity(sContext.get() , "cn.beevideo" , false);
                break;
            case NO_UPDATE:
                //没有更新
                break;
        }
    }

    /**
     * 本机当前是否安装了沙发管家
     *
     * @param context
     * @return
     */
    public static boolean isInstalledShafaMarket(Context context) {
        if (context == null) {
            return false;
        }

        AppInfo bean = AppInfo.queryLocalAppInfo(context, DownloadDef.APK_PACKAGENAME);

        return bean != null && TextUtils.equals(bean.getPackageName(), DownloadDef.APK_PACKAGENAME);
    }


    static Context getContext(){
        if(sContext != null){
            return sContext.get();
        }
        return null;
    }


    public static void startActivity(Context context , String packageName ,  boolean hasInstalledMarket){
        if(hasInstalledMarket){
            ComponentName componentName = new ComponentName(
                    DownloadDef.APK_PACKAGENAME, DownloadDef.APK_CLASS);
            Intent intent = new Intent();
            intent.putExtra("pkg", packageName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(componentName);
            context.startActivity(intent);
        }else{
            Intent intent = new Intent(context, ShafaDownloadAct.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("pkg" , packageName);
            context.startActivity(intent);
        }
    }

}
