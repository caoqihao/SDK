package com.shafa.market.library.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.shafa.market.library.utils.Strings;
import com.shafa.market.library.download.DownloadDef;
import com.shafa.market.library.download.DownloadInfo;
import com.shafa.market.library.download.ShafaFileDownload;
import com.shafa.market.library.utils.UMessage;
import com.shafa.market.library.utils.Util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by caoqihao on 2016/11/15.
 */

public class ShafaDownloadManager {

    public static final int STATUS_NO_INFO = 0; //未安装
    public static final int STATUS_DOWNLOADING = 1;//下载中
    public static final int STATUS_APK_EXISTS = 2; //已存在 未安装
    public static final int STATUS_INSTALLING = 3; //安装中
    public static final int STATUS_INSTALLED = 4;  //已安装
    public static final int STATUS_APK_AGIN_DOWNLOADING = 5;//再次下载
    public static final int STATUS_APK_CHECKING_= 6; //apk文件校验

    private PackageManager mPackageManager;
    private Context mContext;
    private Map<String, ShafaFileDownload> mFileDownloads;

    public static String SHAFA_DOWNLOAD_DIR;
    public static String TEMP_FILE;

    private final String TAG = "ShafaDownloadManager";

    public ShafaDownloadManager(Context context) {
        mContext = context;
        mPackageManager = mContext.getPackageManager();
        mFileDownloads = new HashMap<String  ,ShafaFileDownload >();
        SHAFA_DOWNLOAD_DIR = Util.getDiskCacheDir(context);
        TEMP_FILE = Util.getDiskCacheDir(context) + "/.temp/";
    }

    public PackageManager getPackageManager(){
        return mPackageManager;
    }

    public int getApkStatus(String url , String packageName){
        if(checkApkInstalledByPackageName(packageName)){
            return STATUS_INSTALLED;
        }else{
            boolean isApkFileExsit = false;
            File file = new File(getApkFilePath(packageName));
            isApkFileExsit = file.exists();
            if(isApkFileExsit)
                return STATUS_APK_EXISTS;

            DownloadInfo dlInfo = getDownloadInfoByUri(url);

            boolean isDownloading = dlInfo != null && dlInfo.mStatus == DownloadInfo.STATUS_DOWNLOADING;

            if(isDownloading){
                return STATUS_DOWNLOADING;
            }

        }
        return STATUS_NO_INFO;
    }

    public DownloadInfo getDownloadInfoByUri(String url){
        if(mFileDownloads == null || mFileDownloads.size() <= 0)  return null;


        if(mFileDownloads.containsKey(url)){
            return mFileDownloads.get(url).getDownloadInfo();
        }else{
            return null;
        }
    }

    public ShafaFileDownload downloadApk(DownloadInfo info){

        info.mFilePath = getApkFilePath(info.mPackageName);

        return downloadFile(info);
    }

    private ShafaFileDownload downloadFile(DownloadInfo info){
        ShafaFileDownload download = new ShafaFileDownload(info);

        if(mFileDownloads.containsKey(info.mUri)){
            if(toastHandler != null){
                toastHandler.sendEmptyMessage(TASK_EXIST);
            }
            return null;
        }

        if (checkMemoryToDownload(info)) {
            try {
                mFileDownloads.put(info.mUri , download);
                ShafaFileDownload fileDownload = download.beginDownload(mListener);
                if(null != fileDownload){
                    if(new File(info.mFilePath).exists()){
                        if (new File(info.mFilePath).exists()) {
//                            if (toastHandler != null) {
//                                toastHandler.sendEmptyMessage(3);
//                            }
                        } else {
                            if (toastHandler != null) {
                                toastHandler.sendEmptyMessage(DOWNLOAD_FAIL);
                            }
                        }
                        if (mFileDownloads.containsKey(info.mUri)) {
                            mFileDownloads.remove(info.mUri);
                        }
                    }
                    return fileDownload;
                }
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }else{
            try {
                toastHandler.removeMessages(NO_MEMORY);
                toastHandler.sendEmptyMessage(NO_MEMORY);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        return null;
    }


    private ShafaFileDownload.IDownloadListener mListener = new ShafaFileDownload.IDownloadListener() {
        @Override
        public void onProgressChange(DownloadInfo info, int currentBytes, int totalBytes) {
            if(currentBytes == totalBytes){
                mFileDownloads.remove(info.mUri);
            }
            /*  一直发广播?....  用观察者模式比较 等待优化  */
            Intent intent = new Intent(DownloadDef.ACTION_DOWNLOAD_CHANGE);
            intent.putExtra(DownloadDef.EXTRA_URI, info.mUri);
            intent.putExtra(DownloadDef.EXTRA_NOW_BYTES, currentBytes);
            intent.putExtra(DownloadDef.EXTRA_TOTAL_BYTES, totalBytes);

            mContext.sendBroadcast(intent);
        }

        @Override
        public void onEnd(DownloadInfo mDownloadInfo) {
            Intent intent = new Intent(DownloadDef.ACTION_DOWNLOAD_OVER);
            intent.putExtra(DownloadDef.EXTRA_DOWNLOAD_INFO, mDownloadInfo);
            mContext.sendBroadcast(intent);
        }

        @Override
        public void onFailed(String uri, String appName) {
            // 删除下载任务
            try {
                deleteDownload(uri);
                Intent intent = new Intent(DownloadDef.ACTION_DOWNLOAD_FAILED);
                intent.putExtra(DownloadDef.EXTRA_URI, uri);
                intent.putExtra(DownloadDef.EXTRA_NAME, appName);
                mContext.sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(String error) {
            try {
                toastHandler.removeMessages(NO_MEMORY);
                toastHandler.sendEmptyMessage(NO_MEMORY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public final int NO_MEMORY = 1;
    public final int TASK_EXIST = 2;
    public final int DOWNLOAD_FAIL = 3;
    private Handler toastHandler = new Handler(Looper.getMainLooper()) {


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case NO_MEMORY:
                    try {
                        String no_memory = Util.checkSDcardCanReadAndWrite() ? Strings.SHAFA_SD_NO_MEMORY
                                : Strings.SHAFA_NO_MEMORY;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case TASK_EXIST:
                    try {
                        UMessage.show(mContext, Strings.SHAFA_DOWNLOAD_TASK_EXIST);
                    } catch (Exception e) {

                    }
                    break;
                case DOWNLOAD_FAIL:
                    try {
                        UMessage.show(mContext, Strings.SHAFA_DOWNLOAD_FAIL);
                    } catch (Exception e) {
                    }
                    break;
                default:
                    break;
            }
        }
    };


    public boolean checkMemoryToDownload(DownloadInfo info) {
        boolean canDownload = true;
        try {
            long memoryCanUseSize = 0;
            String path = Util.checkSDcardCanReadAndWrite() ? Environment
                    .getExternalStorageDirectory().getPath() : Environment
                    .getDataDirectory().getPath();
            StatFs stat = new StatFs(path);
            memoryCanUseSize = ((long) stat.getAvailableBlocks())
                    * ((long) stat.getBlockSize());
            //这里第一次下载 info.getTotalBytes() 肯定是0啊
            Log.d("selfup", "剩余容量是：" + memoryCanUseSize + "KB and 文件大小为： "
                    + info.getTotalBytes() + "KB");
            if (memoryCanUseSize != 0
                    && memoryCanUseSize < info.getTotalBytes()) {
                canDownload = false;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return canDownload;
    }


    /**
     * 获取apk下载所对应的文件存放地址
     *
     * @param packageName
     * @return
     */
    public String getApkFilePath(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException();
        }
        return SHAFA_DOWNLOAD_DIR + getApkFileName(packageName);
    }

    /**
     * 获取apk下载所对应的名称
     *
     * @param packageName
     * @return
     */
    public String getApkFileName(String packageName) {
        StringBuffer buff = new StringBuffer();

        buff.append(packageName);
        buff.append(".apk");

        return buff.toString();
    }

    /**
     * 根据包名检测软件是否安装
     * @param packageName
     * @return
     */
    public boolean checkApkInstalledByPackageName(String packageName){
        PackageInfo packageInfo;
        try {
            packageInfo = mPackageManager.getPackageInfo(packageName , 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }

        return packageInfo != null;
    }

    /**
     * 删除一个已经存在的下载任务
     *
     * @param downloadUrl
     * @return
     */
    public boolean deleteDownload(String downloadUrl) {
        boolean delete = false;
        try {
            ShafaFileDownload download = getFileDownloadByUri(downloadUrl);
            if (download == null) {
                Log.d(TAG,"FileDownload find is null  --> "+downloadUrl);
                DownloadInfo info = new DownloadInfo();
                info.mUri = downloadUrl;
                download = new ShafaFileDownload(info);
            }else {
                Log.d(TAG,"FileDownload find ok");
            }

            delete = download.deleteDownload();
            if (mFileDownloads.containsKey(downloadUrl)) {
                mFileDownloads.remove(downloadUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return delete;
    }

    /**
     * 删除一个已经存在的下载任务
     *
     * @param downloadUrl
     * @return
     */
    public boolean deleteDownload(String downloadUrl, String packageName,long updateTime) {
        boolean delete = false;
        try {
            ShafaFileDownload download = getFileDownloadByUri(downloadUrl);
            if (download == null) {
                Log.d(TAG,"FileDownload find is null ");
                DownloadInfo info = new DownloadInfo();
                info.mUri = downloadUrl;
                info.mFilePath= getApkFilePath(packageName);
                download = new ShafaFileDownload(info);
            }else {
                Log.d(TAG,"FileDownload find ok");
            }
            Log.d(TAG,"  delete path  "+download.getDownloadInfo().mFilePath);
            delete = download.deleteDownload();
            if (mFileDownloads.containsKey(downloadUrl)) {
                mFileDownloads.remove(downloadUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return delete;
    }

    /**
     * 获取下载未完成的FileDOwnload.有可能null
     *
     * @param uri
     */
    public ShafaFileDownload getFileDownloadByUri(String uri) {

        if (TextUtils.isEmpty(uri)) {
            Log.d(TAG, "get file downloadUri null");
            return null;
        }

        return mFileDownloads.get(uri);
    }

    /**
     * 查询下载路径，获取当前apk文件的正确下载路径
     *
     * @param packageName
     * @param url
     * @return
     */
    public DownloadInfo getApkFile(String url, String packageName) {
        if (packageName == null || url == null || url.length() == 0) {
            return null;
        }
        String path = getApkFilePath(packageName);
        File file = new File(path);
        if (file.exists()) {
            DownloadInfo info = new DownloadInfo();
            info.mFilePath = file.getAbsolutePath();
            info.mPackageName = packageName;
            info.mUri = url;

            return info;
        }

        return null;
    }
}
