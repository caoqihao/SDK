package com.shafa.market.library.install;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;


import com.shafa.market.library.utils.UPreference;
import com.shafa.market.library.utils.shell.ADB;

import java.io.File;

public class InstallMode {
	
	private Context mContext;
	
	private Process mProcess;
	
	private final String KEY_INSTALL_MODE = "market.install.mode";
	
	private final String TAG = "InstallMode";
	
	public static final int INSTALL_MODE_UNDEFINE = -1;
	
	/**
	 * 使用静默安装
	 */
	public static final int INSTALL_MODE_SILENT = 0;
	
	/**
	 * 使用系统安装器安装
	 */
	public static final int INSTALL_MODE_SYSTEM = 1;
	
	
	private boolean mCanInstallADB = false;
	
	private int mOneceInstallMode = INSTALL_MODE_SYSTEM;
	
	public InstallMode(Context context) {
		this.mContext = context;
		
		configADBMode();
	}
	
	public int getOnceInstallMode() {
		return mOneceInstallMode;
	}
	
	public void setOnceInstallMode(int mode) {
		mOneceInstallMode = mode;
	}
	

	/**
	 * 获取用户自定义的安装模式
	 * @return -1 用户未自定义过安装模式<br>
	 * 0 静默安装， 静默安装失败后，可以采用系统安装器<br>
	 * 1 系统安装器安装，系统安装器失败后可以使用静默安装
	 */
	public int getInstallModeByUser() {

		int ret = UPreference.getInt(mContext, KEY_INSTALL_MODE, -1);
		Log.d(TAG, "getInstallModeByUser " + ret );
		return ret;
	}

	/**
	 * 设置用户的安装模式
	 * @param mode
	 * @return
	 */
	public boolean setInstallMode(int mode) {
		return UPreference.putInt(mContext, KEY_INSTALL_MODE, mode);
	}
	

	private void configADBMode() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (InstallMode.this) {
					Log.d(TAG, "configADBMode begin  " + mCanInstallADB);
					mCanInstallADB = new ADB().connectable;
					Log.d(TAG, "configADBMode end " + mCanInstallADB);
				}
			}
		}).start( );
	}
	
	
	/**
	 * 可否使用ADB安装
	 * @return
	 */
	public boolean canInstallADB() {
		synchronized (this) {
			Log.d(TAG, "canInstallADB " + mCanInstallADB);
			return mCanInstallADB;
		}
	}
	
	private final String INSTALL_PROCESS_NAME = "com.android.packageinstaller";
	
	/**
	 * 可否使用ADB安装
	 * @return
	 */
	public boolean canInstallSystem() {
		
		// 检查是否存在系统安装器
		
		PackageInfo info = null;
		
		try {
			info = mContext.getPackageManager().getPackageInfo(INSTALL_PROCESS_NAME, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int allow = 1;
		try {
			allow = android.provider.Settings.System.getInt(mContext.getContentResolver(), android.provider.Settings.System.INSTALL_NON_MARKET_APPS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		
		if (allow == 0 || null == info) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * (不是绝对准确的状态)可否通过静默安装安装
	 * @return
	 */
	@SuppressLint("NewApi")
	public boolean canInstallSU() {
		
		File su1 =  new File("/system/bin/su");
		if (su1.exists() && su1.isFile() && su1.canExecute()) {
			Log.d(TAG, "canInstallSU true ");
			return true;
		}
		
		File su2 =  new File("/system/xbin/su");
		if (su2.exists() && su2.isFile() && su2.canExecute()) {
			Log.d(TAG, "canInstallSU true ");
			return true;
		}
		Log.d(TAG, "canInstallSU false ");
		return false;
	}
	
	
}
