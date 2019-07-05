package com.hdt.exercise.thread_pool;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool {

    private int mMaxPoolSize;
    private AtomicInteger mActiveThreads;
    private ArrayList<WorkerThread> mWorkers;

    // FIFO ordering
    private final ThreadPoolQueue<Runnable> mQueue;

    public CustomThreadPool(int corePoolSize, int maxPoolSize, int queueSize) {
        mMaxPoolSize = maxPoolSize;
        mQueue = new ThreadPoolQueue<>(this, queueSize);
        mWorkers = new ArrayList<>();

        for (int i = 0; i < corePoolSize; i++) {
            mWorkers.add(new WorkerThread("Thread-" + (i + 1), true));
            mWorkers.get(i).start();
        }

        mActiveThreads = new AtomicInteger();
        mActiveThreads.set(corePoolSize);
    }

    public void execute(Runnable task) {
        mQueue.enqueue(task, mActiveThreads, mMaxPoolSize);
    }

    public void shutdown() {
        // put worker threads in closable state
        for (WorkerThread worker : mWorkers) {
            worker.close();
        }

        // notify all worker threads that are waiting
        // to end the life cycle when they are closable
        synchronized (mQueue) {
            mQueue.notifyAll();
        }
    }

    public void addAndStartNewThread() {
        mWorkers.add(new WorkerThread("Thread-" + (mActiveThreads.get() + 1), false));
        mWorkers.get(mActiveThreads.getAndAdd(1)).start();
    }
    
    public void removeRedundantThreads() {
        mWorkers.remove((WorkerThread) Thread.currentThread());
        mActiveThreads.set(mWorkers.size());
        System.out.println("After break " + Thread.currentThread().getName() + ", Active Threads: " + mActiveThreads.get());
    }

    private class WorkerThread extends Thread {

        private AtomicBoolean mClosable;
        private boolean mIsCore;

        public WorkerThread(String name, boolean isCore) {
            super(name);
            mClosable = new AtomicBoolean(true);
            mIsCore = isCore;
        }

        public void run() {
            Runnable task = null;

            while (true) {
                task = mQueue.dequeue(mClosable, mActiveThreads, mIsCore);

                if (task == null) {
                    break;
                }

                task.run();
            }
        }

        public void close() {
            mClosable.set(false);
        }
    }
}
