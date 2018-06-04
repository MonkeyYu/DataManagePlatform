package com.etone.universe.dmp.task.file;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Created by Lanny on 2016-9-7.
 */
public class ChmodTaskTest {
    @Test
    public void readConf() throws Exception {
        ChmodTask task = new ChmodTask();

        task.setName("f_ps_a_day.properties");
        task.setTime(Calendar.getInstance().getTime());

        task.readConf();

        System.out.println(task.getPrefix());
        System.out.println(task.getPath());
    }

}