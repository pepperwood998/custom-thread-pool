package com.hdt.exercise.thread_pool;

public class App {
    public static void main(String[] args) {
        CustomThreadPool customThreadPool = new CustomThreadPool(15, 18, 30);

        for (int i = 1; i <= 10; i++) {
            Task task = new Task("Task " + i);

            customThreadPool.execute(task);
            System.out.println("Added task " + task.getName());
        }

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        for (int i = 1; i <= 80; i++) {
//            Task task = new Task("Task " + i);
//
//            customThreadPool.execute(task);
//            System.out.println("Added : " + task.getName());
//        }
//
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        customThreadPool.shutdown();
    }
}

class Task implements Runnable {
    private String mName;

    public Task(String name) {
        this.mName = name;
    }

    public String getName() {
        return mName;
    }

    @Override
    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() + " is executing: " + mName);
            Thread.sleep(500);
            System.out.println(Thread.currentThread().getName() + " finished: " + mName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
