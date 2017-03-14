package com.linchaolong.apktoolplus.utils;

import javafx.application.Platform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  后台任务处理器
 *
 * Created by linchaolong on 2015/9/18.
 */
public class TaskManager {

    public static final String TAG = TaskManager.class.getSimpleName();

    // 任务队列
    private TaskQueue taskQueue;
    // 线程池，使用线程池实现并发，优化执行效率
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    // 单例
    private static TaskManager instance = null;

    public static synchronized TaskManager get(){
        if (instance == null)
            instance = new TaskManager();
        return instance;
    }

    private TaskManager() {
        // 初始化任务队列
        taskQueue = new TaskQueue();
        taskQueue.start();
    }

    /**
     * 提交任务到后台任务队列
     *
     * @param task  任务
     */
    public void queue(Runnable task){
        taskQueue.submit(task);
    }

    /**
     * 提交任务到线程池
     *
     * @param task  任务
     */
    public void submit(Runnable task){
        threadPool.submit(task);
    }

    /**
     * 在JavaFX Application Thead（UI线程）中执行任务
     *
     * @param task
     */
    public void runOnUiThread(Runnable task){
        Platform.runLater(task);
    }

    /**
     * 设置任务队列变化监听
     *
     * @param listner   TaskListener
     */
    public void setTaskQueueListener(TaskQueue.TaskListener listner){
        taskQueue.setTaskListener(listner);
    }

    /**
     * 获取任务队列大小
     *
     * @return  任务队列大小
     */
    public int queueSize(){
        return taskQueue.size();
    }
}
