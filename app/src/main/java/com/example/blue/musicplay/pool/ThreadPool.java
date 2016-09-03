package com.example.blue.musicplay.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by blue on 2016/8/22.
 */

public class ThreadPool {
    /*线程池*/
    private ExecutorService mThreadPool;
    /*默认线程数*/
    private static final int DEAFULT_THREAD_COUNT = 1;
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    private Semaphore mSemaphoreThreadPool;

    private static ThreadPool mInstance;

    /* 音乐播放线程  */
    private  Thread playerThread;

    /*任务队列方式*/
    public enum Type {FIFO, LIFO;}
    /*任务类型*/
    public enum TaskType {
        dowloadMusic,MusicPlayer,ImageLoader,
    }
    private ThreadPool(int threadCount, Type type) {
        init(threadCount, type);
    }

    private void init(int threadCount, Type type) {

        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mSemaphorePoolThreadHandler = new Semaphore(threadCount);
    }

    public static ThreadPool getInstance() {
        if (mInstance == null) {
            synchronized (ThreadPool.class) {
                if (mInstance == null) {
                    mInstance = new ThreadPool(DEAFULT_THREAD_COUNT, Type.LIFO);
                }
            }
        }
        return mInstance;
    }


    public void startDowloadTask() {

    }
    private void loadImageTask() {

    }

}
