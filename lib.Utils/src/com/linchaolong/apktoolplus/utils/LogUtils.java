package com.linchaolong.apktoolplus.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {
	
	/** 日志级别 **/
	public static final int VERBOSE = 0;
	public static final int INFO = 1;
	public static final int DEBUG = 2;
	public static final int WARN= 3;
	public static final int ERROR = 4;
	
	/** 当前日志级别 **/
	private static int mLogLevel = VERBOSE;
	public static void setLogLevel(int logLevel){
		mLogLevel = logLevel;
	}
	public static int getLogLevel(){
		return mLogLevel;
	}

    // 得到日期时间的DateFormat对象
    private static DateFormat localDateFromat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 获取当前堆栈元素
	 *
	 * @return
	 */
	private static StackTraceElement getCurrentStackTraceElement() {
        return Thread.currentThread().getStackTrace()[6]; // 0是调用的栈顶元素，也就是当前方法，索引0-5都是Debug里的方法，6是调用Logger的所在方法
	}

    /**
     * 日期Log
     *
     * @return
     */
    public static String getDateLog(){
        StringBuilder sBuff = new StringBuilder();
        StackTraceElement s = getCurrentStackTraceElement();
        sBuff.append(localDateFromat.format(new Date()))
                .append(format(s));
        return sBuff.toString();
    }

    public static String format(StackTraceElement s){
        //return String.format("%s.%s(%s:%s)%n", s.getClassName(), s.getMethodName(),s.getFileName(), s.getLineNumber());
        return String.format(".(%s:%s)",s.getFileName(), s.getLineNumber());
    }

	/** 
	 * 格式化log
	 * 
	 * @param logLevel		log级别
	 * @param log				log
	 * @return
	 */
	private static String format(int logLevel, String log) {

//		四月 08, 2016 10:35:04 下午 com.sun.javafx.css.StyleManager loadStylesheetUnPrivileged
//		INFO: Could not find stylesheet: jar:file:/C:/Users/Administrator/AppData/Local/Temp/e4jA5CD.tmp_dir1460126084/ApkToolPlus_release_1.0.6.jar!/css/jiagu.css

        StringBuilder sBuff = new StringBuilder();
        // 日志级别
        switch (logLevel){
            case VERBOSE:
                sBuff.append("VERBOSE");
            break;
            case INFO:
                sBuff.append("INFO");
                break;
            case DEBUG:
                sBuff.append("DEBUG");
                break;
            case WARN:
                sBuff.append("WARN");
                break;
            case ERROR:
                sBuff.append("ERROR");
                break;
        }
        sBuff.append(":");
        sBuff.append(getDateLog()).append(": ");
        // log
        if (log != null){
            sBuff.append(log);
        }else{
            sBuff.append("null");
        }
		return sBuff.toString();
	}

	/**
	 * 日志输出
	 *
	 * @param log
	 */
	private static void stdOutput(String log){
		System.out.println(log);
	}

	/**
	 * 输出错误日志（红色）
	 *
	 * @param log
	 */
	private static void errOutput(String log){
		System.err.println(log);
	}

	/**
	 * 格式化并输出
	 * 
	 * @param logLevel		log级别
	 * @param log				log
	 */
	private static void formatOutput(int logLevel, String log) {
        if(logLevel < WARN){
            stdOutput(format(logLevel, log));
        }else{
            errOutput(format(logLevel, log));
        }
	}

	// 不同级别的日志输出函数
    public static void v(String msg){
        if (VERBOSE >= mLogLevel) {
            formatOutput(VERBOSE, msg);
        }
    }

    public static void i(String msg){
        if (INFO >= mLogLevel) {
            formatOutput(INFO, msg);
        }
    }

	public static void d(String msg){
		if (DEBUG >= mLogLevel) {
            formatOutput(DEBUG, msg);
		}
	}

    public static void w(String msg){
        if (WARN >= mLogLevel) {
            formatOutput(WARN, msg);
        }
    }

	public static void e(String msg){
		if (ERROR >= mLogLevel) {
            formatOutput(ERROR, msg);
		}
	}

    public static void d(Throwable e){
        if(DEBUG >= mLogLevel && e != null ){
            stdOutput(getDateLog());
            e.printStackTrace();
        }
    }

    public static void e(Throwable e){
        if(ERROR >= mLogLevel && e != null ){
            errOutput(getDateLog());
            e.printStackTrace();
        }
    }
}
