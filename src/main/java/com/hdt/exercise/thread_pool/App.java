package com.hdt.exercise.thread_pool;

public class App {
    public static void main(String[] args) {
        CustomThreadPool customThreadPool = new CustomThreadPool(55, 58, 3);

        for (int i = 1; i <= 30; i++) {
            Task task = new Task("Task " + i);

            customThreadPool.execute(task);
            System.out.println("Added : " + task.getName());
        }

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
            System.out.println("Executing : " + mName);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

