package com.shafa.market.library.net;

import android.os.SystemClock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zl on 16/1/21.
 */
class HttpRequest {


    public static final String UTF_8 = "UTF-8";


    public static String post(String url, byte[] data){
        return request("POST",url,data,true,null,3);
    }

    public static String get(String url){
        return request("GET",url,null,true,null,3);
    }


    /**
     *
     * @param method 请求方法GET,POST...
     * @param requestUrl 请求url
     * @param data POST 可能会发送的数据
     * @param readResponse 是否读取返回的数据
     * @param callback 回调
     * @param retryNumber 错误重试次数
     * @return
     */
    public static String request(String method, String requestUrl, byte[] data, boolean readResponse, OnRequestCallback callback, int retryNumber) {
        HttpURLConnection connection = null;
        String resp=null;
        boolean success=false;
        int code = 0;
        Throwable err=null;
        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5*1000);
            connection.setReadTimeout(5*1000);
            connection.setUseCaches(false);
            connection.setRequestMethod(method);

            if("POST".equals(method)){
                connection.setDoOutput(true);
                writeData(connection,data);
            }

            try {
                code = connection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchMethodError error) {
                code = -1000;
            }

            if (code == HttpURLConnection.HTTP_OK || code == -1000) {
                //request success
                if(readResponse) {
                    resp = readContent(connection.getInputStream());
                    success = (resp != null);
                }else {
                    success=true;
                }
            }


        } catch (Throwable e) {
            err=e;
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }


        //重试
        if(!success && retryNumber > 0){
            retryNumber-=1;
            SystemClock.sleep(1000);

            return request(method,requestUrl,data,readResponse,callback,retryNumber);
        }

        if (callback != null) {
            if (success) {
                callback.onResponse(resp);
            } else if (err != null) {
                callback.onResponseError(err);
            } else {
                callback.onResponseError(code);
            }
        }

        return resp;
    }



    private static void writeData(HttpURLConnection connection, byte[] data) {
        OutputStream output = null;
        try {
            if (data != null) {
                connection.addRequestProperty("Content-Length", "" + data.length);
                output = connection.getOutputStream();
                output.write(data);
                output.flush();
            } else {
                connection.addRequestProperty("Content-Length", "0");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private static String readContent(InputStream is) {
        String ret = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int read = -1;
            while ((read = is.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            ret = baos.toString(UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    //worker thread
    interface OnRequestCallback {
        void onResponse(String content);

        void onResponseError(int code);

        void onResponseError(Throwable t);
    }

}
