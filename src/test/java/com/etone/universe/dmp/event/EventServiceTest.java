package com.etone.universe.dmp.event;

import com.etone.universe.dmp.util.FileAppender;
import com.google.common.eventbus.Subscribe;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * Created by Lanny on 2016-9-9.
 */
public class EventServiceTest {
    @Test
    public void post() throws Exception {

        CdrEvent cdr = new CdrEvent();
        cdr.initialize();
        long counter = 0;

        long start = System.currentTimeMillis();

        double size = 1e6;


        EventService.getInstance().register(new CdrListener() {
            long receive = 0;
            FileAppender appender = new FileAppender("e:/temp/", "utf-8", 1024 * 1024 * 1024, 120);

            @Override
            @Subscribe
            public void log(CdrEvent event) {
                receive++;
                if (receive % 10000 == 0)
                    appender.append("cdr", event.toString());
            }
        });



        while (true) {

            EventService.getInstance().post(cdr);
            counter++;
            if (counter > 0 && counter % size == 0) {
                long ms = System.currentTimeMillis() - start;
                System.out.println("post " + size + " cdr in " + ms + " speed=" + size / ms + " totla=" + counter);
                start = System.currentTimeMillis();
            }
        }


    }

}