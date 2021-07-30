package com.android.inputmethod.pinyin.demo;

import android.os.Handler;
import android.os.Looper;


import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程管理类
 */
public class ThreadPoolManager {
    private final ExecutorService service;
    private final Executor mainThread;
    private final Timer delayTimer = new Timer();

    private ThreadPoolManager() {
        int num = Runtime.getRuntime().availableProcessors() * 20;
        service = Executors.newFixedThreadPool(num);
        mainThread = new MainThreadExecutor();
    }

    private static final ThreadPoolManager manager = new ThreadPoolManager();

    public static ThreadPoolManager getInstance() {
        return manager;
    }

    public void executeTask(Runnable runnable) {

        service.execute(runnable);

    }

    public void executeTaskDelay(final Runnable runnable, long delay) {
        delayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                service.execute(runnable);
            }
        }, delay);
    }

    public void executeMainThreadTaskDelay(final Runnable runnable, long delay) {
        delayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mainThread.execute(runnable);
            }
        }, delay);
    }


    public void executeMainThreadTask(Runnable runnable) {
        mainThread.execute(runnable);

    }

    public void executeTasks(LinkedList<Runnable> list) {
        for (Runnable runnable : list) {
            service.execute(runnable);
        }
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
