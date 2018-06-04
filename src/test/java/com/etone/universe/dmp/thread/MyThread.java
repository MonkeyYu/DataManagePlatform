package com.etone.universe.dmp.thread;

import com.etone.daemon.util.Threads;

/**
 * Created by Lanny on 2016-9-27.
 */
public class MyThread implements Runnable {

    private long timeout = 5000;

    public void run() {
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Threads.sleep(30 * 1000);
                System.out.println("done");
            }
        });
        t.start();

        final long start = System.currentTimeMillis();


        while (true) {
            Threads.sleep(1000);
            if (timeout < (System.currentTimeMillis() - start)) {
                t.interrupt();
                System.out.println("thread timeout : " + (System.currentTimeMillis() - start));
                break;
            }
        }


    }

    public static void main(String[] args) {
        new Thread(new MyThread()).start();
    }

}
