package com.hdt.exercise.thread_pool;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool {

    private int mCorePoolSize;
    private int mMaxPoolSize;
    private AtomicInteger mActiveThreads;

    private ArrayList<WorkerThread> mWorkers;

    // FIFO ordering
    private final LinkedBlockingQueue<Runnable> mQueue;

    public CustomThreadPool(int corePoolSize, int maxPoolSize, int queueSize) {
        mCorePoolSize = corePoolSize;
        mMaxPoolSize = maxPoolSize;
        mQueue = new LinkedBlockingQueue<>(queueSize);
        mWorkers = new ArrayList<>();

        for (int i = 0; i < corePoolSize; i++) {
            mWorkers.add(new WorkerThread());
            mWorkers.get(i).start();
        }

        mActiveThreads = new AtomicInteger();
        mActiveThreads.set(corePoolSize);
    }

    public void execute(Runnable task) {
        synchronized (mQueue) {
            // when queue is full, initialize more threads
            while (mQueue.remainingCapacity() == 0) {
                if (mActiveThreads.get() < mMaxPoolSize) {
                    mWorkers.add(new WorkerThread());
                    mWorkers.get(mActiveThreads.getAndAdd(1)).start();
                }

                try {
                    mQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mQueue.add(task);
            mQueue.notifyAll();
        }
    }

    public void shutdown() {
        for (WorkerThread worker : mWorkers) {
            worker.close();
        }
    }

    private class WorkerThread extends Thread {

        private boolean mActive;

        public boolean isActive() {
            return mActive;
        }

        public void run() {
            mActive = true;
            Runnable task = null;

            while (true) {
                synchronized (mQueue) {
                    mQueue.notifyAll();
                    task = mQueue.poll();
                }

                try {
                    if (task != null) {
                        System.out.println("Active Thread Number: " + mActiveThreads);
                        task.run();
                    } else {
                        if (!isActive())
                            break;
                        else {
                            synchronized (mQueue) {
                                while (mQueue.isEmpty()) {
                                    try {
                                        mQueue.wait();
                                    } catch (InterruptedException e) {
                                        System.out.println("An error occurred while queue is waiting: " 
                                                            + e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    System.out.println("Thread pool is interrupted due to an issue: " 
                                        + e.getMessage());
                }
            }
        }

        public void close() {
            mActive = false;
        }
    }
}
