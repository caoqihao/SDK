package com.shafa.market.library.utils.shell;

import com.shafa.market.library.utils.shell.Shell.Result;

public class PMUtils {
	
	public static int install(String path) {
		return install(path, true);
	}
	
	public static int install(String path, boolean reinstall) {
		StringBuilder sb = new StringBuilder("pm install ");
		if (reinstall) {
			sb.append("-r ");
		}
		sb.append(path);
		
		Result result = Shell.execSU(false, sb.toString());
		
		return result.result;
	}
	
	public static int uninstall(String packageName) {
		return uninstall(packageName, false);
	}
	
	public static int uninstall(String packageName, boolean keepData) {
		StringBuilder sb = new StringBuilder("pm uninstall ");
		if (keepData) {
			sb.append("-k ");
		}
		sb.append(packageName);

		Result result = Shell.execSU(false, sb.toString());
		
		return result.result;
	}
	
	public static int enable(String... pkgOrComponents) {
		String[] cmds = null;
		if (pkgOrComponents != null && pkgOrComponents.length > 0) {
			cmds = new String[pkgOrComponents.length];
			for (int i = 0; i < cmds.length; i++) {
				if (pkgOrComponents[i] != null) {
					cmds[i] = "pm enable " + pkgOrComponents[i];
				}
			}
		}
		
		Result result = Shell.execSU(false, cmds);
		
		return result.result;
	}
	
	public static int disable(String... pkgOrComponents) {
		String[] cmds = null;
		if (pkgOrComponents != null && pkgOrComponents.length > 0) {
			cmds = new String[pkgOrComponents.length];
			for (int i = 0; i < cmds.length; i++) {
				if (pkgOrComponents[i] != null) {
					cmds[i] = "pm disable " + pkgOrComponents[i];
				}
			}
		}
		
		Result result = Shell.execSU(false, cmds);
		
		return result.result;
	}
	
	public static Result doInitDisable(String... pkgOrComponents) {
	    String[] cmds = null;
        if (pkgOrComponents != null && pkgOrComponents.length > 0) {
            cmds = new String[pkgOrComponents.length];
            for (int i = 0; i < cmds.length; i++) {
                if (pkgOrComponents[i] != null) {
                    cmds[i] = "pm disable " + pkgOrComponents[i];
                }
            }
        }
        
        return Shell.execSU(false, cmds);
	}

}
