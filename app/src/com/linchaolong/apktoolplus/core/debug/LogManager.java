package com.linchaolong.apktoolplus.core.debug;

import com.linchaolong.apktoolplus.core.AppManager;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 自定义控制台
 *
 * Created by linchaolong on 2015/9/7.
 */
public class LogManager {

    public static final String TAG = LogManager.class.getSimpleName();

    /** 是否把输出到文件 **/
    private boolean isLogFileOutput = true;

    /** 日志打印输出代理 **/
    private OutputDelegate outputDelegate = null;
    private OutputDelegate errOutputDelegate = null;

    /** 日志文件输出流 **/
    private FileOutputStream logFileOut = null;

    /** 单例 **/
    private static LogManager instance = null;
    private OutputListener outputListener = null;

    // 单例
    private LogManager() {
        outputRedirect();
    }

    /**
     * 日志输出重定向
     */
    private void outputRedirect() {
        try {
            // log file
            File logFile;
            File logOutDir = AppManager.getLogDir();

            // 按日期管理log
            String logFileName = new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + ".log";
            logFile = new File(logOutDir, logFileName);

            // log文件输出流
            logFileOut = new FileOutputStream(logFile,true);

            // 创建代理类
            outputDelegate = new OutputDelegate(System.out);
            errOutputDelegate = new OutputDelegate(System.err);

            // 设置日志输出监听
            outputDelegate.setOutputListener(new OutputListener() {
                @Override
                public void write(int b) {
                    try {
                        if (isLogFileOutput()) {
                            logFileOut.write(b);
                        }
                        if (outputListener != null) {
                            outputListener.write(b);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void write(byte[] buf, int off, int len) {
                    try {
                        if (isLogFileOutput()) {
                            logFileOut.write(buf, off, len);
                        }
                        if (outputListener != null) {
                            outputListener.write(buf, off, len);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            errOutputDelegate.setOutputListener(new OutputListener() {
                @Override
                public void write(int b) {
                    try {
                        if(isLogFileOutput()){
                            logFileOut.write(b);
                        }
                        if(outputListener != null){
                            outputListener.write(b);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void write(byte[] buf, int off, int len) {
                    try {
                        if(isLogFileOutput()){
                            logFileOut.write(buf,off,len);
                        }
                        if(outputListener != null){
                            outputListener.write(buf,off,len);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            System.setOut(outputDelegate);
            System.setErr(errOutputDelegate);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static LogManager getInstance(){
        if(instance == null){
            instance = new LogManager();
        }
        return instance;
    }

    /**
     * 是否输出日志到文件，默认为true
     *
     * @return
     */
    public boolean isLogFileOutput() {
        return this.isLogFileOutput;
    }

    /**
     * 设置是否输出日志到文件
     *
     * @param isLogFileOutput
     */
    public void setIsLogFileOutput(boolean isLogFileOutput) {
        this.isLogFileOutput = isLogFileOutput;
    }

    /**
     * 设置日志输出监听
     *
     * @param listener  OutputListener
     */
    public void setOutputListener(OutputListener listener){
        outputListener = listener;
    }
}

