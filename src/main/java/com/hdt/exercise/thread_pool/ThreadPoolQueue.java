package com.hdt.exercise.thread_pool;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolQueue<T> {

    private final CustomThreadPool mThreadPool;
    private final Queue<T> mQueue;
    private final int mMaxQueueSize;

    public ThreadPoolQueue(CustomThreadPool threadPool, int maxQueueSize) {
        mThreadPool = threadPool;
        mQueue = new LinkedList<>();
        mMaxQueueSize = maxQueueSize;
    }

    public synchronized void enqueue(T task, AtomicInteger activeThreads, int maxPoolSize) {
        try {
            while (mQueue.size() == mMaxQueueSize) {
                int actThreads = activeThreads.get();
                if (actThreads < maxPoolSize && actThreads < mMaxQueueSize) {
                    mThreadPool.addAndStartNewThread();
                }

                wait();
            }
        } catch (InterruptedException e) {
            System.out.println("Waiting for queue resize failed");
        }

        mQueue.offer(task);
        notifyAll();
    }

    public synchronized T dequeue(AtomicBoolean active, AtomicInteger activeThreads, boolean isCore) {
        try {
            while (mQueue.isEmpty()) {
                if (active.get()) {
                    if (isCore) {
                        wait();
                    } else {
                        wait(6000);
                        if (mQueue.isEmpty()) {
                            active.set(false);
                        }
                    }
                } else {
                    mThreadPool.removeRedundantThreads();
                    return null;
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Waiting for adding new task failed");
        }

        if (mQueue.size() == mMaxQueueSize) {
            notifyAll();
        }

        return mQueue.poll();
    }
}
