package com.shafa.market.library.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by caoqihao on 2016/11/15.
 */

public class Util {

    public static boolean checkSDcardCanReadAndWrite() {
        boolean canWrite = false;
        boolean canRead = false;
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                File file = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath());
                if (file.canRead()) {
                    canRead = true;
                }
                if (file.canWrite()) {
                    canWrite = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return canWrite && canRead;
    }

    public static String getDiskCacheDir(Context context){
        String cachePath = null;

        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()){
            cachePath = context.getExternalCacheDir().getPath()+"/";
        }else{
            cachePath = context.getCacheDir().getPath() + "/";
        }

        return cachePath;
    }
}
