package com.linchaolong.apktoolplus.utils;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 线程安全的异步任务队列
 *
 * Created by linchaolong on 2015/9/10.
 */
public class TaskQueue {

    /**
     * 任务包装器
     */
    public static abstract  class TaskWrapper implements Runnable{

        public static int WAITING = 0;
        public static int DOING = 1;
        public static int FINISHED = 2;

        private Runnable task;
        private int state = WAITING;

        public TaskWrapper(Runnable task) {
            this.task = task;
        }

        private void setState(int state){
            this.state = state;
        }

        public boolean isWaiting(){
            return state == WAITING;
        }
        public boolean isDoing(){
            return state == DOING;
        }
        public boolean isFinished(){
            return state == FINISHED;
        }

        @Override
        public final void run() {
            setState(DOING);
            task.run();
        }

        /**
         * 获取任务
         *
         * @return
         */
        public Runnable getTask(){
            return task;
        }

        /**
         * 任务完成是调用
         */
        public void onFinish(){
            setState(FINISHED);
        }
    }

    // 线程安全的任务队列
    private ConcurrentLinkedQueue<TaskWrapper> taskQueue = new ConcurrentLinkedQueue<>();

    // 后台线程
    private Thread mThread = null;

    // 运行标识
    private boolean isRun = true;

    // 任务状态变化监听
    private TaskListener taskListener;

    /**
     * 启动任务队列轮询
     */
    public synchronized  void start(){
        isRun = true;
        if(mThread != null){
            return;
        }
        mThread = new Thread(() -> {
            loop();
        });
        mThread.start();
    }

    /**
     * 轮询任务队列
     */
    protected  void loop(){
        while(isRun){
            // poll：获取并移除此队列的头，如果此队列为空，则返回 null。
            // peek：获取但不移除此队列的头；如果此队列为空，则返回 null。
            TaskWrapper task = taskQueue.peek();
            // 如果任务队列为空则让线程等待
            if(task == null){
                try {
                    synchronized (mThread){
                        mThread.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                task.run();
                taskQueue.remove(task);
                task.onFinish();
            }
        }
    }


    /**
     * 提交任务到任务队列
     *
     * @param task  任务
     */
    public synchronized void submit(Runnable task){
        TaskWrapper taskWrapper = new TaskWrapper(task) {
            @Override
            public void onFinish() {
                super.onFinish();
                // 任务状态变化回调
                safeCallTaskLister(this, TaskListener.FINISH);
            }
        };
        taskQueue.add(taskWrapper);
        // 任务状态变化回调
        safeCallTaskLister(taskWrapper, TaskListener.ADD);
        synchronized (mThread){
            mThread.notify();
        }
    }

    /**
     * 停止任务队列
     */
    public void destory(){
        isRun = false;
        mThread.notify();
    }

    /**
     * 获取当前任务队列大小
     *
     * @return
     */
    public int size(){
        return taskQueue.size();
    }


    /**
     * 任务队列变化监听
     */
    public interface TaskListener {

        /** 当前任务添加到队列 **/
        int ADD = 0;
        /** 当前任务完成 **/
        int FINISH = 1;

        /**
         * 任务状态变化回调方法
         *
         * @param task  任务
         * @param code  状态码
         */
        void onChange(TaskWrapper task, int code);
    }

    /**
     *  设置任务状态变化监听
     */
    public void setTaskListener(TaskListener listener){
        taskListener = listener;
    }


    private void safeCallTaskLister(TaskWrapper task, int code){
        if(taskListener != null){
            taskListener.onChange(task,code);
        }
    }
}
