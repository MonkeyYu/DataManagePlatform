package com.etone.universe.dmp.event;

import com.etone.daemon.db.sql.SqlBuilder;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/**
 * Created by Macro on 2016/9/6.
 * Copyright 2016 Guangdong Eastone Century Technology Co.,Ltd.
 * All rights reserved.
 */
public class SqlEventTest {
    private static final Logger logger = LoggerFactory.getLogger(SqlEventTest.class);
    @Test
    public void test() throws Exception{
        SqlEvent event = new SqlEvent();
        event.setStart(Calendar.getInstance().getTime());
        event.setPriority(1);
        event.setName("test");

        event.setEnd(Calendar.getInstance().getTime());
        Duration d = new Duration(new DateTime(event.getStart()), new DateTime(event.getEnd()));
        event.setSpend(d.getMillis());

        EventService.getInstance().post(event);

        SqlBuilder bulider = SqlBuilder.create(SqlEvent.class, SqlBuilder.DbType.MYSQL);
        String insert = bulider.insert().build();
        Object params = bulider.getParams(event, false);

        logger.info("{}, {}", insert, params);
    }
}