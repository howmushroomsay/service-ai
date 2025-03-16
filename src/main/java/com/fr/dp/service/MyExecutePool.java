package com.fr.dp.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyExecutePool {
    private static final MyExecutePool instance = new MyExecutePool(3);
    private final ExecutorService executorService;


    private MyExecutePool(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    public static MyExecutePool getInstance() {
        return instance;
    }

    public void execute(Runnable task) {
        executorService.execute(task);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
