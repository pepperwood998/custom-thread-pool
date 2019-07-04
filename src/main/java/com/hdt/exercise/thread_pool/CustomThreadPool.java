package com.hdt.exercise.thread_pool;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool {

    private int mCorePoolSize;
    private int mMaxPoolSize;
    private AtomicInteger mActiveThreads;

    private ArrayList<WorkerThread> mWorkers;

    // FIFO ordering
    private final CustomBlockingQueue<Runnable> mQueue;

    public CustomThreadPool(int corePoolSize, int maxPoolSize, int queueSize) {
        mCorePoolSize = corePoolSize;
        mMaxPoolSize = maxPoolSize;
        mQueue = new CustomBlockingQueue<>(this, queueSize);
        mWorkers = new ArrayList<>();

        for (int i = 0; i < corePoolSize; i++) {
            mWorkers.add(new WorkerThread(true));
            mWorkers.get(i).start();
        }

        mActiveThreads = new AtomicInteger();
        mActiveThreads.set(corePoolSize);
    }

    public void execute(Runnable task) {
        mQueue.enqueue(task, mActiveThreads, mMaxPoolSize);
    }

    public void shutdown() {
        for (WorkerThread worker : mWorkers) {
            worker.close();
        }
    }

    public void addAndStartNewThread() {
        mWorkers.add(new WorkerThread(false));
        mWorkers.get(mActiveThreads.getAndAdd(1)).start();
    }

    private class WorkerThread extends Thread {

        private AtomicBoolean mActive;
        private boolean mIsCore;

        public WorkerThread(boolean isCore) {
            mActive = new AtomicBoolean(true);
            mIsCore = isCore;
        }

        public void run() {
            Runnable task = null;

            while (true) {
                task = mQueue.dequeue(mActive);

                if (task == null) {
                    break;
                }

                task.run();
            }
        }

        public void close() {
            mActive.set(false);
        }
    }
}
