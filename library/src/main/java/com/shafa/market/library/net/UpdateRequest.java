package com.shafa.market.library.net;


import android.util.Log;

import com.shafa.market.library.download.DownLoadThreadPool;
import com.shafa.market.library.download.DownloadDef;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;


public class UpdateRequest {

    private static final String TAG = "UpdateRequest";

    public static final String UTF_8 = "UTF-8";

    private volatile boolean isRequesting = false;

    private OnRequestCallback mOnRequestCallback;

    private String mPkgName;

    private volatile String mUpdateResp;
    private volatile String mShafaResp;

    public UpdateRequest(String pkgName) {
        this.mPkgName=pkgName;
    }

    public boolean isRequesting() {
        return isRequesting;
    }

    public void setOnRequestCallback(OnRequestCallback onRequestCallback) {
        mOnRequestCallback = onRequestCallback;
    }

    /**
     * 请求更新数据
     */
    public void requestUpdateData() {
        if (!isRequesting) {
            isRequesting = true;
            try {
                request();
            } catch (Throwable e) {
                e.printStackTrace();
                isRequesting = false;
            }
        }
    }

    private void request(){

        final CyclicBarrier barr=new CyclicBarrier(1, new Runnable() {
            @Override
            public void run() {

                //多个请求都完成后通知
                isRequesting = false;
                try{
                    if(mUpdateResp != null){
                        Log.i("myLog" , "----请求到的数据---" + mUpdateResp);
                        UpdateResult result = UpdateResult.parseResponse(mUpdateResp);
//                        if(result != null && mShafaResp != null){
//                            final UpdateResult.ShafaMarketInfo marketInfo = UpdateResult.parseMarketResponse(mShafaResp);
//                            result.setShafaMarketInfo(marketInfo);
//                        }

                        if(mOnRequestCallback != null){
                            mOnRequestCallback.onResponse(result);
                        }
                    }else {
                        if(mOnRequestCallback != null){
                            mOnRequestCallback.onResponse(null);
                        }
                    }
                }catch (Throwable e){
                    e.printStackTrace();

                    if(mOnRequestCallback != null){
                        mOnRequestCallback.onResponse(null);
                    }
                }
            }
        });


        //请求更新信息
        DownLoadThreadPool.getSinglePools().execute(
            new Runnable() {
                @Override
                public void run() {
                    try{
                        Log.i("myLog" ,"--URL--" + getUpdateUrl());
                        mUpdateResp= HttpRequest.get(getUpdateUrl());
                        barr.await();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
    }

    //worker thread
    public interface OnRequestCallback {
        void onResponse(UpdateResult result);
    }

    private String getUpdateUrl() {
        Map<String, String> params = newPublicParams();
        params.put("pkg",mPkgName);

        return toURL(DownloadDef.OPEN_API_SERVER, params, DownloadDef.SECRET_KEY);
    }

    private static Map<String, String> newPublicParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_key", DownloadDef.APP_KEY);
        return params;
    }

    private static String toURL(String base,
                                Map<String, String> params, String secret) {
        StringBuilder sb = new StringBuilder(base);
        sb.append('?');
        if (secret != null) {
            sb.append(sign(params, secret));
        } else {
            sb.append(encodeParams(params));
        }

        return sb.toString();
    }


    private static String sign(Map<String, String> params, String key) {
        StringBuilder sb = new StringBuilder();

        String paramsString = encodeParams(params);
        sb.append(paramsString);
        sb.append("&sign=");
        sb.append(generate(paramsString, key));

        return sb.toString();
    }


    private static String generate(String src, String suffix) {
        StringBuilder sb = new StringBuilder();
        src = src == null ? "" : src;
        suffix = suffix == null ? "" : suffix;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((src + suffix).getBytes(UTF_8));
            for (byte b : md.digest()) {
                sb.append(String.format("%02x", b & 0xff));
//                sb.append(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
//    private static String generate1(String src, String suffix) {
//        StringBuilder sb = new StringBuilder();
//        src = src == null ? "" : src;
//        suffix = suffix == null ? "" : suffix;
//
//        try {
//            MessageDigest md = MessageDigest.getInstance("MD5");
//
//            byte[] bytes = md.digest((src + suffix).getBytes());
//            String result = "";
//            for (byte b : bytes) {
//                String temp = Integer.toHexString(b & 0xff);
//                if (temp.length() == 1) {
//                    temp = "0" + temp;
//                }
//                result += temp;
//            }
//            return result;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }


    private static String encodeParams(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();

        if (params != null && params.size() > 0) {
            List<String> list = new ArrayList<String>();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                try {
                    list.add(URLEncoder.encode(entry.getKey(), UTF_8) + '=' + URLEncoder.encode(entry.getValue(), UTF_8));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            Collections.sort(list, new Comparator<String>() {

                @Override
                public int compare(String lhs, String rhs) {
                    return lhs.compareToIgnoreCase(rhs);
                }
            });

            for (String s : list) {
                sb.append(s);
                sb.append('&');
            }

            final int count = sb.length();
            if (count > 0 && sb.charAt(count - 1) == '&') {
                sb.deleteCharAt(count - 1);
            }
        }

        return sb.toString();
    }

}
