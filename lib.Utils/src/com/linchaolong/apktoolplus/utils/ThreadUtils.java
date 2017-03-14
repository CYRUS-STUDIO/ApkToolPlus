package com.linchaolong.apktoolplus.utils;

import javafx.application.Platform;

/**
 * 线程工具类
 *
 * Created by linchaolong on 2016/4/11.
 */
public class ThreadUtils {

    /**
     * 当前线程是否UI线程
     *
     * @return
     */
    public static boolean isUiThread(){
        return Platform.isFxApplicationThread();
    }

    /**
     * 在UI线程中执行任务
     *
     * @param run
     */
    public static void runOnUiThread(Runnable run){
        if(isUiThread()){
            run.run();
        }else{
            Platform.runLater(run);
        }
    }

    /**
     * 休眠
     *
     * @param millis
     */
    public static void sleep(long millis){
        try {
            Thread.currentThread().sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
