package com.shafa.market.library.install;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.shafa.market.library.download.DownloadInfo;

import java.io.File;


public class InstallRunnable implements Runnable {
	
	private Context mContext;
	private DownloadInfo mInfo;
	private InstallCallback mCallback;
	public Handler installHandler;
	
	private InstallMode mInstallMode;
	
	public InstallRunnable(InstallMode mode, Context context, InstallCallback callback, DownloadInfo info) {
		mInstallMode = mode;
		mContext = context;
		mCallback = callback;
		mInfo = info;
		installHandler = new Handler(Looper.getMainLooper(), new InstallHandlerCallBack());
	}

	@Override
	public void run() {
		boolean success = true;
		
		if (mInfo != null) {
			success &= install(mInfo);
		}
		
		if (mCallback != null) {
			mCallback.onFinish(success);
		}
	}
	
	private boolean install(DownloadInfo info) {
		boolean ret = false;
		
		
		try {
			File tmpFile = new File(info.mFilePath);
			while (null != tmpFile && tmpFile.exists()) {
				
				String command = "chmod 777 " + tmpFile.getAbsolutePath() ;
				Runtime runtime = Runtime.getRuntime();
				runtime.exec(command);
				
				tmpFile = tmpFile.getParentFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (info != null) {
			if (mCallback != null) {
				mCallback.onStart(info);
			}
            Log.d("install","  start   install.....  ");

            int retCode = PackageUtils.install(mInstallMode, mContext, info, installHandler);
			//int retCode = PackageUtils.install(mInstallMode, mContext, info.path, installHandler);
			switch (retCode) {
			case PackageUtils.INSTALL_ACTIVITY_START:
				if (mCallback != null) {
					mCallback.onInstallActivityStart(info);
				}
				break;
			case PackageUtils.INSTALL_SUCCEEDED:
				if (mCallback != null) {
					mCallback.onSuccess(info);
				}
				ret = true;
				break;
			default:
				if (mCallback != null) {
					Log.d("size", "install(ApkFileInfo info) default: ");
					mCallback.onError(info, true);
				}
				break;
			}
		}
		
		return ret;
	}

}
