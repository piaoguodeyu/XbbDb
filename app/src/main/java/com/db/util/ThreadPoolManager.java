package com.db.util;

import android.os.Looper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangxiaowei 2020/12/11
 */
public class ThreadPoolManager {
    private static ExecutorService executorService;
    private static BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(100);
    private static final RejectedExecutionHandler handler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {

            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        recycler(runnable, executor);
                    }
                }.start();
            } else {
                recycler(runnable, executor);
            }
        }
    };

    private static void recycler(Runnable runnable, ThreadPoolExecutor executor) {
        try {
            Thread.sleep(500);
            executor.submit(runnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    static {
        executorService = new ThreadPoolExecutor(5,
                50,
                300,
                TimeUnit.SECONDS,
                blockingQueue,
                Executors.defaultThreadFactory(),
                handler);
    }

    public static void submit(Runnable runnable) {
        executorService.submit(runnable);
    }
}
