package com.linchaolong.apktoolplus.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * 命令行工具
 *
 注意：在Windows下执行批处理不能省略后缀，但exe可以
 示例：
     if(OSUtils.isWindows()){
        CmdUtils.exec("ant.bat compile -f "+buildFile.getAbsolutePath());
     }else{
        CmdUtils.exec("ant compile -f "+buildFile.getAbsolutePath());
     }
 *
 * Created by linchaolong on 2015/11/12.
 */
public class CmdUtils {

    /**
     * 执行指定命令
     *
     * @param cmd   命令
     * @return
     */
    public static boolean exec(String cmd){
        return exec(cmd, true);
    }

    /**
     * 执行指定命令
     *
     * @param cmd       命令
     * @param isInput   是否输出执行日志
     * @return
     */
    public static boolean exec(String cmd, boolean isInput){
        return exec(cmd, null, isInput);
    }

    /**
     * 执行指定命令
     *
     * @param cmd       命令
     * @param workDir   工作目录
     * @return
     */
    public static boolean exec(String cmd, File workDir){
        return exec(cmd, workDir, true);
    }

    /**
     * 执行指定命令
     *
     * @param cmd       命令
     * @param workDir   工作目录
     * @param isOutput  是否输出执行日志
     * @return
     */
    public static boolean exec(String cmd, File workDir, boolean isOutput){
        return exec(cmd, null, workDir, isOutput);
    }

    /**
     * 执行指定命令
     *
     * @param cmd       命令
     * @param env       环境变量
     * @param workDir   工作目录
     * @param isOutput  是否输出执行日志
     * @return
     */
    public static boolean exec(String cmd, String[] env, File workDir, boolean isOutput){
        LogUtils.d( "exec=" + cmd);
        boolean isSuccess  = true;
        if(FileHelper.exists(workDir) && OSUtils.isWindows()){
            cmd = String.format("cmd /c %s",cmd);
        }
        Runtime runtime = Runtime.getRuntime();
        try {
            if (!isOutput){
                runtime.exec(cmd, env, workDir);
                return true;
            }
            Process proc = runtime.exec(cmd, env, workDir);

            String encoding = System.getProperty("sun.jnu.encoding");
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream(),encoding));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream(),encoding));

            // read the _out from the command
            //LogUtils.d("Here is the standard _out of the command:\n");
            String s;
            while ((s = stdInput.readLine()) != null) {
                LogUtils.d(s);
            }

            // read any errors from the attempted command
            //LogUtils.e( "Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                LogUtils.e(s);
                isSuccess = false;
            }
        } catch (Exception e) {
            isSuccess = false;
            e.printStackTrace();
        }
        return isSuccess;
    }

    public static String execAndGetOutput(String cmd){
        Runtime runtime = Runtime.getRuntime();
        try {
            Process proc = runtime.exec(cmd);
            String encoding = System.getProperty("sun.jnu.encoding");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream(),encoding));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream(),encoding));
            String temp;
            StringBuilder output = new StringBuilder();
            while ((temp = stdInput.readLine()) != null) {
                output.append(temp).append('\n');
            }
            while ((temp = stdError.readLine()) != null) {
                output.append(temp).append('\n');
            }
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
