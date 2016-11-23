package com.shafa.market.library.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.shafa.market.library.ShafaMarket;
import com.shafa.market.library.bean.ShafaMarketBean;
import com.shafa.market.library.download.DownLoadThreadPool;
import com.shafa.market.library.download.DownloadDef;
import com.shafa.market.library.download.DownloadInfo;
import com.shafa.market.library.manager.ShafaDownloadManager;
import com.shafa.market.library.download.ShafaFileDownload;
import com.shafa.market.library.install.InstallCallback;
import com.shafa.market.library.view.Images;
import com.shafa.market.library.utils.ShafaLayout;
import com.shafa.market.library.manager.ShafaManager;
import com.shafa.market.library.utils.Strings;
import com.shafa.market.library.utils.UMessage;
import com.shafa.market.library.view.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by caoqihao on 2016/11/14.
 */

public class ShafaDownloadAct extends BaseActivity {
    private ProgressBar mProgress;

    private ShafaMarketBean mAPPBean;

    private String targetPackageName = "";

    private boolean installing= false;

    private boolean isRequestUrl = false;

    private final String TAG = "ShafaDownloadAct";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout(this);
        if(null != getIntent()){
            targetPackageName = getIntent().getStringExtra("pkg");
        }
        mHandler.sendEmptyMessageDelayed(START_DOWNLOAD , 100);
        addReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        installing = false;
        Log.i("myLog" ,"--------onResume--");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDownloadReceiver);
        unregisterReceiver(packageInstallReceiver);
    }

    private void downLoadAndInstall(){
        checkShafaStatus(ShafaManager.getInstance(getApplicationContext()).getAppBean());
    }

    private void installApk(ShafaMarketBean b){
        DownloadInfo afi = ShafaManager.getInstance(this).getShafaDownloadManager().getApkFile(b.apkUrl, b.packageName);
        ShafaManager.getInstance(getApplicationContext()).installApk(afi, new InstallCallback() {
            @Override
            public void onStart(DownloadInfo apk) {
                installing = true;
            }

            @Override
            public void onInstallActivityStart(DownloadInfo apk) {
            }

            @Override
            public void onSuccess(DownloadInfo apk) {
            }

            @Override
            public void onError(DownloadInfo apk, boolean isMustError) {
            }

            @Override
            public void onFinish(boolean isAllSuccess) {
            }
        });
    }

    private void downloadApkAndInstall(ShafaMarketBean bean) {
        // 开始下载
        DownloadInfo dInfo = new DownloadInfo();

        dInfo.mUri = bean.apkUrl;
        dInfo.mPackageName = bean.packageName;
        try {
            ShafaFileDownload fd = ShafaManager.getInstance(getApplicationContext()).getShafaDownloadManager().downloadApk(dInfo);
            if (fd != null) {
                bean.apkStatus = ShafaDownloadManager.STATUS_DOWNLOADING;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkShafaStatus(ShafaMarketBean bean){
        if(null == bean) return;
        mAPPBean = bean;
        int status = ShafaManager.getInstance(this).getShafaDownloadManager().getApkStatus(bean.apkUrl , bean.packageName);
        bean.apkStatus = status;
        if(status == ShafaDownloadManager.STATUS_APK_EXISTS){
            mProgress.requestFocus();
            mProgress.setProgress(100);
            installApk(bean);

        }else if(status == ShafaDownloadManager.STATUS_DOWNLOADING){

        }else if(status == ShafaDownloadManager.STATUS_NO_INFO){
            if(!isRequestUrl){
                requestDownloadUrl();
            }
        }

    }

    private void downloadAPk(String url){
        isRequestUrl = false;
        if(!TextUtils.isEmpty(url)){
            mAPPBean.apkUrl = url;
        }
        Log.i(TAG , "----DOWNLOAD APK---->>> " + url);
        mProgress.setProgress(0);
        downloadApkAndInstall(mAPPBean);
    }

    private void requestDownloadUrl(){
        isRequestUrl = true;
        DownLoadThreadPool.getSinglePools().execute(new RequestDownloadUrl(DownloadDef.GET_DOWNLOAD_URL + targetPackageName));
    }

    private final int START_DOWNLOAD = 1;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case START_DOWNLOAD :
                    downLoadAndInstall();
                    mProgress.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private void addReceiver() {
        IntentFilter downloadIntentFilter = new IntentFilter();
        downloadIntentFilter.addAction(DownloadDef.ACTION_DOWNLOAD_CHANGE);
        downloadIntentFilter.addAction(DownloadDef.ACTION_DOWNLOAD_OVER);
        downloadIntentFilter.addAction(DownloadDef.ACTION_DOWNLOAD_FAILED);
        registerReceiver(mDownloadReceiver, downloadIntentFilter);

        IntentFilter pkgIntentFilter = new IntentFilter();
        pkgIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        pkgIntentFilter.addDataScheme("package");
        registerReceiver(packageInstallReceiver,pkgIntentFilter);
    }

    private void initLayout(final Context mContext){
        FrameLayout.LayoutParams params;

        FrameLayout container = new FrameLayout(mContext);
        try {
            setBackGround(container , getBgDrawable());
        } catch (Exception e) {
            e.printStackTrace();
        }
        params = new FrameLayout.LayoutParams(-1 , -1);
        setContentView(container , params);

        ImageView logo = new ImageView(mContext);
        logo.setImageBitmap(Images.getBitmap(Images.ShafaDownloadAct.LOGO));
        params = new FrameLayout.LayoutParams(ShafaLayout.L1080P.w(300), ShafaLayout.L1080P.w(210));
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = ShafaLayout.L1080P.h(240);
        container.addView(logo, params);

        ImageView logoName = new ImageView(mContext);
        logoName.setImageBitmap(Images.getBitmap(Images.ShafaDownloadAct.LOGONAME));
        params = new FrameLayout.LayoutParams(ShafaLayout.L1080P.w(240), ShafaLayout.L1080P.w(60));
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = ShafaLayout.L1080P.h(510);
        container.addView(logoName, params);

        mProgress = new ProgressBar(mContext);
        mProgress.setVisibility(View.INVISIBLE);
        mProgress.setFocusable(true);
        mProgress.setProgressDrawable(getProgressBg());
        mProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!installing && null != mAPPBean){
                    installing = true;
                    checkShafaStatus(mAPPBean);
                    UMessage.show(mContext , Strings.SHAFA_WAIT_ALERT);
                }
            }
        });

        params = new FrameLayout.LayoutParams(ShafaLayout.L1080P.w(840) , ShafaLayout.L1080P.h(60));
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        params.bottomMargin = ShafaLayout.L1080P.h(165);
        container.addView(mProgress , params);
    }

    private BroadcastReceiver mDownloadReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(TextUtils.equals(DownloadDef.ACTION_DOWNLOAD_CHANGE , action)){
                int currentBytes = intent.getIntExtra(DownloadDef.EXTRA_NOW_BYTES , 0);
                int totalBytes = intent.getIntExtra(DownloadDef.EXTRA_TOTAL_BYTES , 0);

                int percent = 0;
                if (totalBytes != 0) {
                    percent = (int) ((double) currentBytes / totalBytes * 100);
                }
                mProgress.setProgress(percent);

            }else if(TextUtils.equals(DownloadDef.ACTION_DOWNLOAD_OVER , action)){
                mProgress.setProgress(100);

                DownloadInfo info = intent.getParcelableExtra(DownloadDef.EXTRA_DOWNLOAD_INFO);
                mAPPBean.apkStatus = info.mStatus;
                mProgress.requestFocus();
                installApk(mAPPBean);


            }else if(TextUtils.equals(DownloadDef.ACTION_DOWNLOAD_FAILED , action)){
                ShafaManager.getInstance(getApplicationContext()).getAppBean().apkStatus = ShafaDownloadManager.STATUS_NO_INFO;
                UMessage.showLong(context, Strings.SHAFA_DOWNLOAD_FAIL);
            }
        }
    };

    private BroadcastReceiver packageInstallReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
                    String packageName = intent.getDataString();
                    if (!TextUtils.isEmpty(packageName)) {
                        packageName = packageName.replace("package:", "");
                    }

                    if(mAPPBean == null )  return;

                    if (TextUtils.equals(mAPPBean.packageName , packageName)) {
                        ShafaManager.getInstance(getApplicationContext()).removeInstallPackagePath(packageName);

                        mAPPBean.isInstalled = true;
                        mAPPBean.apkStatus = ShafaDownloadManager.STATUS_INSTALLED;

                        trackDownload();

                        if(!TextUtils.isEmpty(targetPackageName)) {
                            try {
                                ShafaMarket.start(getApplicationContext() , targetPackageName);
                            } catch (Exception e) {

                            }
                        }
                        ShafaDownloadAct.this.finish();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private LayerDrawable getProgressBg(){
        Drawable[] drawables = new Drawable[3];
        Drawable bg =getProgressDrawable(Color.parseColor("#FF192d5f") , true); //进度条背景图
        Drawable pgDownloading =getProgressDrawable(Color.parseColor("#FF358349") , false); //进度图(下载中)
        Drawable pgDownloaded =getProgressDrawable(Color.parseColor("#FF358349") , true); //进度图(已完成)
        drawables[0] = bg;
        drawables[1] = pgDownloading;
        drawables[2] = pgDownloaded;
        LayerDrawable layerDrawable = new LayerDrawable(drawables);

        return layerDrawable;
    }

    private Drawable getProgressDrawable(int fillColor , boolean isBg){
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(fillColor);//内部填充颜色
        if(isBg){
            gd.setCornerRadius(12);
        }else{
            gd.setCornerRadii(new float[] { 12, 12, 0, 0, 0, 0, 12, 12 });
        }
        return gd;
    }

    private Drawable getBgDrawable(){
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] { 0xff112969, 0xff0e235c, 0xff0d1b48 });
        return bg;
    }

    private void setBackGround(View view , Drawable drawable){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    private class RequestDownloadUrl implements Runnable{
        String requestUrl;
        String downApkUrl = "";

        public RequestDownloadUrl(String httpUrl) {
            requestUrl = httpUrl;
        }

        @Override
        public void run() {
            if(TextUtils.isEmpty(requestUrl)) {
                downloadAPk("");
                return;
            }
            try {
                URL url = new URL(requestUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5*1000);
                conn.connect();
                if(conn.getResponseCode() == 200){
                    InputStream is = conn.getInputStream();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1){
                        byteArrayOutputStream.write(buffer , 0 , len);
                    }
                    String jsonString = byteArrayOutputStream.toString();
                    byteArrayOutputStream.close();
                    is.close();

                    try {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        if(null != jsonObject){

                            if(jsonObject.has("update_url")){
                                downApkUrl = jsonObject.getString("update_url");
                                Log.i(TAG , "----APK URL---->>> " + downApkUrl);
                            }

                            if(jsonObject.has("track")){
                                trackUrl = jsonObject.getString("track");
                            }


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            downloadAPk(downApkUrl);
        }
    }

    private String trackUrl;
    /**
     * 统计沙发管家的下载
     */
    private void trackDownload(){
        if(TextUtils.isEmpty(trackUrl)) return;

        DownLoadThreadPool.getSinglePools().execute(

            new Runnable(){

                @Override
                public void run() {
                    try {
                        URL url = new URL(trackUrl);
                        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                        urlConn.setConnectTimeout(5 * 1000);
                        urlConn.connect();
                        if (urlConn.getResponseCode() == 200) {
                            //统计ok
                        }
                        urlConn.disconnect();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        );

    }

}
