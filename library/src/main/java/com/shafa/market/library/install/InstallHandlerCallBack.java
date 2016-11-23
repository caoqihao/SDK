package com.shafa.market.library.install;

import android.os.Handler;
import android.os.Message;

public class InstallHandlerCallBack implements Handler.Callback {

	@Override
	public boolean handleMessage(Message msg) {
		boolean ret = true;
		try {
			switch (msg.what) {
			case PackageUtils.INSTALL_FAILED_ALREADY_EXISTS:

				break;
			case PackageUtils.INSTALL_FAILED_INVALID_APK:

				break;
			case PackageUtils.INSTALL_FAILED_INVALID_URI:

				break;
			case PackageUtils.INSTALL_FAILED_INSUFFICIENT_STORAGE:

				break;
			case PackageUtils.INSTALL_FAILED_DUPLICATE_PACKAGE:

				break;
			case PackageUtils.INSTALL_FAILED_NO_SHARED_USER:

				break;
			case PackageUtils.INSTALL_FAILED_UPDATE_INCOMPATIBLE:

				break;
			case PackageUtils.INSTALL_FAILED_SHARED_USER_INCOMPATIBLE:

				break;
			case PackageUtils.INSTALL_FAILED_MISSING_SHARED_LIBRARY:

				break;
			case PackageUtils.INSTALL_FAILED_REPLACE_COULDNT_DELETE:

				break;
			case PackageUtils.INSTALL_FAILED_DEXOPT:

				break;
			case PackageUtils.INSTALL_FAILED_OLDER_SDK:

				break;
			case PackageUtils.INSTALL_FAILED_CONFLICTING_PROVIDER:

				break;
			case PackageUtils.INSTALL_FAILED_NEWER_SDK:

				break;
			case PackageUtils.INSTALL_FAILED_TEST_ONLY:

				break;
			case PackageUtils.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE:

				break;
			case PackageUtils.INSTALL_FAILED_MISSING_FEATURE:

				break;
			case PackageUtils.INSTALL_FAILED_CONTAINER_ERROR:

				break;
			case PackageUtils.INSTALL_FAILED_INVALID_INSTALL_LOCATION:

				break;
			case PackageUtils.INSTALL_FAILED_MEDIA_UNAVAILABLE:

				break;
			case PackageUtils.INSTALL_FAILED_VERIFICATION_TIMEOUT:

				break;
			case PackageUtils.INSTALL_FAILED_VERIFICATION_FAILURE:

				break;
			case PackageUtils.INSTALL_FAILED_PACKAGE_CHANGED:

				break;
			case PackageUtils.INSTALL_FAILED_UID_CHANGED:

				break;
			default:
				ret = false;
				break;
			}
			
//			Toast.makeText((Context)APPGlobal.appContext, "安装发送消息 ： " + msg.what, Toast.LENGTH_SHORT).show();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

}
