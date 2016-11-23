package com.shafa.market.library.utils.shell;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

class Shell {
	
    private static final String CMD_SU       = "su";
    private static final String CMD_SH       = "sh";
    private static final String CMD_EXIT     = "exit\n";
    private static final String CMD_LINE_END = "\n";
    
    public static Result execSU(String... commands) {
    	return exec(CMD_SU, true, commands);
    }
    
    public static Result execSU(boolean isNeedResultMsg, String... commands) {
    	return exec(CMD_SU, isNeedResultMsg, commands);
    }
    
    public static Result execSH(String... commands) {
    	return exec(CMD_SH, true, commands);
    }
    
    public static Result execSH(boolean isNeedResultMsg, String... commands) {
    	return exec(CMD_SH, isNeedResultMsg, commands);
    }

    /**
     * execute shell commands
     * 
     * @return <ul>
     * <li>if isNeedResultMsg is false, {@link Result#successMsg} is null and {@link Result#errorMsg} is
     * null.</li>
     * <li>if {@link Result#result} is -1, there maybe some excepiton.</li>
     * </ul>
     */
    private static Result exec(String prog, boolean isNeedResultMsg, String... commands) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new Result(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(prog);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(command.getBytes());
                os.writeBytes(CMD_LINE_END);
                os.flush();
            }
            os.writeBytes(CMD_EXIT);
            os.flush();
            
            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }

        return new Result(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null
            : errorMsg.toString());
    }

    /**
     * result of command,
     * <ul>
     * <li>{@link Result#result} means result of command, 0 means normal, else means error, same to excute in
     * linux shell</li>
     * <li>{@link Result#successMsg} means success message of command result</li>
     * <li>{@link Result#errorMsg} means error message of command result</li>
     * </ul>
     * 
     */
    public static class Result {

        public int    result;
        
        public String successMsg;

        public String errorMsg;

        public Result(int result){
            this.result = result;
        }

        public Result(int result, String successMsg, String errorMsg){
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}
