package com.etone.universe.dmp.task.sql;

import com.etone.universe.dmp.task.Variables;
import org.junit.Test;

/**
 * Created by Lanny on 2016-9-7.
 */
public class VariablesTest {
    @Test
    public void plusHour() throws Exception {

        System.out.println(Variables.parse("${hiveconf:yyyyMmdd}", java.util.Calendar.getInstance().getTime()));

    }

}