package com.shafa.market.library.utils.shell;

import java.io.File;

public class ADB {
	private static volatile ADB instance;
	
	public static ADB getInstance() {
		if (instance == null) {
			synchronized (ADB.class) {
				if (instance == null) {
					instance = new ADB();
				}
			}
		}
		
		return instance;
	}
	
	public final boolean connectable;
	
	public ADB() {
		Shell.execSH(false, Encrypt.decrypt("417u6:;;06!udgb{e{e{d"));
		Shell.Result result = Shell.execSH(true, Encrypt.decrypt("417u10#<60&"));
		
		connectable = result.result == 0 && result.successMsg != null 
				&& result.successMsg.contains(Encrypt.decrypt("dgb{e{e{do````\\10#<60"));
	}
	
	private int exec(String cmd) {
		return connectable ? Shell.execSH(false, cmd).result : -1;
	}
	
	private int execSU(String cmd){
	    return Shell.execSU(false, cmd).result;
	}
	
	public static boolean isValid() {
		return getInstance().connectable;
	}
	
	public static int install(String path) {
		StringBuilder sb = new StringBuilder();
		sb.append("417u6:;;06!udgb{e{e{d_417ux&udgb{e{e{do````u<;&!499ux'u");
		sb.append(Encrypt.encrypt(path));

		return getInstance().exec(Encrypt.decrypt(sb.toString()));
	}
	
	public static int uninstall(String packageName) {
		StringBuilder sb = new StringBuilder();
		sb.append("417u6:;;06!udgb{e{e{d_417ux&udgb{e{e{do````u ;<;&!499u");
		sb.append(Encrypt.encrypt(packageName));
		
		return getInstance().exec(Encrypt.decrypt(sb.toString()));
	}
	
	public static int reboot() {
		String cmd = "417u6:;;06!udgb{e{e{d_417ux&udgb{e{e{do````u'07::!";
		return getInstance().exec(Encrypt.decrypt(cmd));
	}
	
	public static int rebootSu(){
	    String cmd = "'07::!";
        return getInstance().execSU(Encrypt.decrypt(cmd));
	}
	
	public static int shutdown() {
		String cmd = "'07::!ux%";
		return getInstance().execSU(Encrypt.decrypt(cmd));
	}
	
	private static class Encrypt {
		private static final int KEY = 0x55;
		
		public static String encrypt(String src) {
			return decrypt(src);
		}
		
		public static String decrypt(String src) {
			if (src != null) {
				char[] chars = src.toCharArray();
				for (int i = 0; i < chars.length; i++) {
					chars[i] ^= KEY;
				}
				return new String(chars);
			}
			
			return null;
		}
	}
	
	/**
	 * 判断系统是否root 通过文件判断（宽松判断，精确度低于通过获取su进程判断）
	 * @return
	 */
	public static boolean isRoot() {  
        boolean root = false;  
        try {  
        	File binsu = new File("/system/bin/su");
        	File xbinsu = new File("/system/xbin/su");
            if (!(binsu.exists() && binsu.isFile()) && !(xbinsu.exists() && xbinsu.isFile())) {  
                root = false;  
            } else {  
                root = true;  
            }  
        } catch (Exception e) {
        }  
  
        return root;  
    }  

}
