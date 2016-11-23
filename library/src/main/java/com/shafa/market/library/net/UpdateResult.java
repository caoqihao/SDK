package com.shafa.market.library.net;

import com.shafa.market.library.bean.AppInfo;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * 更新结果数据
 * Created by zl on 16/1/20.
 */
public final class UpdateResult implements Serializable {

    private AppInfo mAppInfo;
    private long mUpdateTime;

    private String mUpdateLog;
    private String mAppTitle;
    private long mFileSize;

//    private ShafaMarketInfo mShafaMarketInfo;
    private boolean mForceUpdate=false;

    private TrackUrls mTrackUrls;

    UpdateResult(){}

    /**
     * 应用包名
     * @return
     */
    public String getPackageName() {
        return mAppInfo!=null?mAppInfo.getPackageName():null;
    }

    /**
     * 应用版本号
     * @return
     */
    public int getVersionCode() {
        return mAppInfo!=null?mAppInfo.getVersionCode():0;
    }

    /**
     * 应用版本名
     * @return
     */
    public String getVersionName() {
        return mAppInfo!=null?mAppInfo.getVersionName():null;
    }

    /**
     * 是否强制更新
     * @return
     */
    public boolean isForceUpdate(){
        return mForceUpdate;
    }

    /**
     * 最后更新时候
     * @return
     */
    public long getUpdateTime() {
        return mUpdateTime;
    }



    /**
     * 应用更新日志
     * @return
     */
    public String getUpdateLog() {
        return mUpdateLog;
    }

    /**
     * 应用名称
     * @return
     */
    public String getAppTitle() {
        return mAppTitle;
    }

    /**
     * 应用安装包文件大小
     * @return
     */
    public long getFileSize(){
        return mFileSize;
    }

    TrackUrls getTrackUrls(){
        return mTrackUrls;
    }

//    void setShafaMarketInfo(ShafaMarketInfo shafaMarketInfo) {
//        mShafaMarketInfo = shafaMarketInfo;
//    }

    /**
     * 沙发管家版本信息
     * @return
     */
//    public ShafaMarketInfo getShafaMarketInfo(){
//        return mShafaMarketInfo;
//    }

    @Override
    public String toString() {
        return "UpdateResult{" +
                "mAppInfo=" + mAppInfo +
                ", mUpdateTime=" + mUpdateTime +
                ", mShafaMarketInfo="  +
                '}';
    }


    static class TrackUrls implements Serializable {
        String trackPop; //统计弹出
        String trackUpdate; //统计点击更新按钮
        String trackDownload; //统计点击下载安装沙发管家
    }


    /**
     * 最新沙发管家版本信息
     */
//    public static class ShafaMarketInfo extends AppInfo implements Serializable {
//
//        ShafaMarketInfo(String packageName, int versionCode, String versionName, long updateTime, String downloadUrl, String fileMd5){
//            super(packageName,versionCode,versionName);
//            this.mUpdateTime=updateTime;
//            this.mDownloadUrl=downloadUrl;
//            this.mFileMd5=fileMd5;
//        }
//
//        private long mUpdateTime;
//        private String mDownloadUrl;
//        private String mFileMd5;
//
//
//        /**
//         * 沙发管家包名
//         * @return
//         */
//        @Override
//        public String getPackageName() {
//            return super.getPackageName();
//        }
//
//        /**
//         * 沙发管家最新版本号
//         * @return
//         */
//        @Override
//        public int getVersionCode() {
//            return super.getVersionCode();
//        }
//
//        /**
//         * 最新版本名
//         * @return
//         */
//        @Override
//        public String getVersionName() {
//            return super.getVersionName();
//        }
//
//        /**
//         * 最后更新时间
//         * @return
//         */
//        public long getUpdateTime() {
//            return mUpdateTime;
//        }
//
//        /**
//         * apk下载地址
//         * @return
//         */
//        public String getDownloadUrl() {
//            return mDownloadUrl;
//        }
//
//        /**
//         * apk 文件md5，下载完成后需要校验
//         * @return
//         */
//        public String getFileMd5() {
//            return mFileMd5;
//        }
//
//
//        @Override
//        public String toString() {
//            return "ShafaMarketInfo{" +
//                    "mUpdateTime=" + mUpdateTime +
//                    ", mDownloadUrl='" + mDownloadUrl + '\'' +
//                    ", mFileMd5='" + mFileMd5 + '\'' +
//                    '}';
//        }
//    }

    static UpdateResult parseResponse(String resp){
        try{

            JSONObject jsonObject=new JSONObject(resp);
            if(jsonObject.optBoolean("status")){
                jsonObject= jsonObject.optJSONObject("data");
                if(jsonObject != null && jsonObject.has("package_name")){

                    UpdateResult bean=new UpdateResult();

                    String packageName=jsonObject.optString("package_name");
                    String versionName=jsonObject.optString("version_name");
                    int versionCode=jsonObject.optInt("version_code");

                    bean.mAppInfo=new AppInfo(packageName,versionCode,versionName);
                    bean.mUpdateTime=jsonObject.optLong("update_time");
                    bean.mAppTitle=jsonObject.optString("title");
                    bean.mUpdateLog=jsonObject.optString("change_logs");
                    bean.mFileSize=jsonObject.optLong("file_size");
                    bean.mForceUpdate=jsonObject.optBoolean("force_update",false);

                    if(jsonObject.has("track_urls")){

                        JSONObject track_urls = jsonObject.optJSONObject("track_urls");
                        if (track_urls != null) {
                            bean.mTrackUrls = new TrackUrls();

                            bean.mTrackUrls.trackPop = track_urls.optString("update_pop");
                            bean.mTrackUrls.trackUpdate = track_urls.optString("update_redirect");
                            bean.mTrackUrls.trackDownload = track_urls.optString("update_market");
                        }
                    }

                    return bean;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


//    static ShafaMarketInfo parseMarketResponse(String resp){
//        try{
//
//            JSONObject jsonObject=new JSONObject(resp);
//            String packageName=UpdateConfig.SHAFA_MARKET_PACKAGENAME;
//            String versionName=jsonObject.optString("version_name");
//            int versionCode=jsonObject.optInt("code");
//            long updateTime=jsonObject.getLong("update_time");
//            String downloadUrl=jsonObject.optString("update_url");
//            String fileMd5=jsonObject.optString("hash_md5");
//
//            return new ShafaMarketInfo(packageName,versionCode,versionName,updateTime,downloadUrl,fileMd5);
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }

}
