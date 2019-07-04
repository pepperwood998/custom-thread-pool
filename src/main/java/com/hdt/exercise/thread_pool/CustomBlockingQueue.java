package com.hdt.exercise.thread_pool;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomBlockingQueue<T> {

    private final CustomThreadPool mThreadPool;
    private final Queue<T> mQueue;
    private final int mMaxQueueSize;

    public CustomBlockingQueue(CustomThreadPool threadPool, int maxQueueSize) {
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
        System.out.println("Active Threads: " + activeThreads);
        if (mQueue.isEmpty()) {
            notifyAll();
        }

        mQueue.offer(task);
    }

    public synchronized T dequeue(AtomicBoolean active) {
        try {
            while (mQueue.isEmpty()) {
                if (active.get()) {
                    wait(100);
                } else {
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
