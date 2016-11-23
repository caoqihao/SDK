package com.shafa.market.library;

import com.shafa.market.library.net.UpdateResult;

/**
 * 检测更新
 */
public interface AppUpdateListener {

    /**
     * 更新结果
     * @param status 能立即跳转到沙发管家
     * @param result
     */
    void onUpdateResult(AppUpdate.UpdateStatus status, UpdateResult result);

}
