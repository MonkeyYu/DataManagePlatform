package com.etone.universe.dmp.event;

import com.etone.universe.dmp.util.FileAppender;

/**
 * Created by Lanny on 2016-9-9.
 */
public class EventLoggerTest {

    public static void main(String[] args) {

        final FileAppender appender = new FileAppender("e:/temp/", "utf-8", 1024, 60);
        EventService.getInstance().register(new CdrListener() {
            @Override
            public void log(CdrEvent event) {
                appender.append("cdr", event.toString());
            }
        });

    }

}
