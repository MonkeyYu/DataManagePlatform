package com.etone.universe.dmp.thread;

import com.etone.daemon.util.Threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by Lanny on 2016-9-22.
 */
public class ThreadTest {

    public static void main(String[] args) {
        new ThreadTest().execute2();
    }

    public void execute() {
        // 创建一个单线程执行程序
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i <= 100; i++) {
            int id = new Random().nextInt(10);
            Task task = new Task(id);
            System.out.println("task " + i + "[" + id + "] has start.");
            Future<Boolean> future = exec.submit(task);
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                System.out.println("task " + i + "[" + id + "] has time out.");
                future.cancel(true);
                continue;
            }

            System.out.println("task " + i + "[" + id + "] has success.");
        }
        exec.shutdown();

    }


    public void execute2() {
        // 创建一个单线程执行程序
        ExecutorService exec = Executors.newFixedThreadPool(5);
        CompletionService<Integer> cs = new ExecutorCompletionService<Integer>(exec);
        final HashMap<Task, Future> map = new HashMap<Task, Future>();
        final ArrayList<Task> tasks = new ArrayList<Task>();
        for (int i = 0; i <= 10; i++) {
            Task task = new Task(i);
            tasks.add(task);
            Future<Boolean> future = exec.submit(task);
            map.put(task, future);
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isDone;
                do {
                    isDone = true;
                    for (int i = 0; i < tasks.size(); i++) {
                        Task task = tasks.get(i);
                        long d = (System.currentTimeMillis() - task.getTime());
                        Future<Boolean> future = map.get(task);

                        if (!future.isDone() && task.isStart && d >= (1000 * 5)) {// 如果任务已经超时
                            System.out.println("task [" + task.getId() + "] timeout witch has run : " + d);
                            future.cancel(true);
                        }
                        isDone = isDone & future.isDone();
                    }
                    Threads.sleep(1000);
                } while (isDone == false);
            }
        }).start();

        exec.shutdown();

    }

    private class Task implements Callable<Boolean> {

        private final int id;

        private long time = 0;

        private boolean isDone = false;

        private boolean isStart = false;

        public Task(int id) {
            this.id = id;
        }

        @Override
        public Boolean call() throws Exception {
            setStart(true);
            setTime(System.currentTimeMillis());
            Random random = new Random();
            int s = random.nextInt(10);
            System.out.println("task [" + getId() + "] start and sleep " + s);
            Thread.sleep(s * 1000);
            System.out.println("task [" + getId() + "] success done.");
            return true;
        }

        public int getId() {
            return id;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public boolean isDone() {
            return isDone;
        }

        public void setDone(boolean done) {
            isDone = done;
        }

        public boolean isStart() {
            return isStart;
        }

        public void setStart(boolean start) {
            isStart = start;
        }
    }


}
