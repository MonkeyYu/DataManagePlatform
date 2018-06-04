package com.etone.universe.dmp.task.hive;

import com.etone.universe.dmp.task.Variables;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/**
 * Created by Lanny on 2016-8-26.
 */
public class HiveConfTest {
    private static final Logger logger = LoggerFactory.getLogger(HiveConfTest.class);

    @Test
    public void testPlusHour() throws Exception {
        logger.info(Variables.plusDay("${hiveconf:yyyyMMdd-5}", Calendar.getInstance().getTime()));

        DateTime s = new DateTime("2016-06-06");
        DateTime n = DateTime.now();

        Duration d = new Duration(s, n);
        logger.info("Minute:" + d.getStandardMinutes());

    }

    @Test
    public void testPlusDay() throws Exception {

    }

    @Test
    public void testPlusMonth() throws Exception {

    }

    @Test
    public void testSetParameter() throws Exception {

    }
}
