package com.shafa.market.library.download;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by caoqihao on 2016/11/15.
 */

public class DownLoadThreadPool {
    private static volatile ExecutorService mPools;

    public static ExecutorService getSinglePools(){
        if(null == mPools){
            synchronized (DownLoadThreadPool.class){
                if(null == mPools){
                    mPools = Executors.newFixedThreadPool(1);
                }
            }
        }
        return mPools;
    }

}
