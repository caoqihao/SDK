package com.shafa.market.library.install;

import com.shafa.market.library.download.DownloadInfo;

public interface InstallCallback {
	public void onStart(DownloadInfo apk);
	public void onInstallActivityStart(DownloadInfo apk);
	public void onSuccess(DownloadInfo apk);
	public void onError(DownloadInfo apk, boolean isMustError);
	public void onFinish(boolean isAllSuccess);
}
