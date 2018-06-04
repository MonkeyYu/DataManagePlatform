/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.task;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lanny on 2016-8-25.
 */
public class Variables {

    /**
     * 替换SQL脚本中的${hiveconf:yyyyMMddHHmm-i}的变量
     *
     * @param sql
     * @param date
     * @return
     */
    public static String plusMinute(String sql, Date date) {
        String result = sql;

        // yyyyMMddHH-i
        Pattern patten = Pattern.compile("(?i)\\$\\{hiveconf:yyyyMMddHHmm([\\-|\\+][0-9]+)\\}");
        Matcher matcher = patten.matcher(result);
        while (matcher.find()) {

            DateTime time = new DateTime(date);
            int i = Integer.parseInt(matcher.group(1));
            time = time.plusMinutes(i);

            result = result.replace(matcher.group(0), time.toString("yyyyMMddHHmm"));
        }

        return result;
    }

    /**
     * 替换SQL脚本中的${hiveconf:yyyyMMddHH-i}的变量
     *
     * @param sql
     * @param date
     * @return
     */
    public static String plusHour(String sql, Date date) {
        String result = sql;

        // yyyyMMddHH-i
        Pattern patten = Pattern.compile("(?i)\\$\\{hiveconf:yyyyMMddHH([\\-|\\+][0-9]+)\\}");
        Matcher matcher = patten.matcher(result);
        while (matcher.find()) {

            DateTime time = new DateTime(date);
            int i = Integer.parseInt(matcher.group(1));
            time = time.plusHours(i);

            result = result.replace(matcher.group(0), time.toString("yyyyMMddHH"));
        }

        return result;
    }

    public static String plusDay(String sql, Date date) {
        String result = sql;

        // yyyyMMdd-i
        Pattern patten = Pattern.compile("(?i)\\$\\{hiveconf:yyyyMMdd([\\-|\\+][0-9]+)\\}");
        Matcher matcher = patten.matcher(result);
        while (matcher.find()) {

            DateTime time = new DateTime(date);
            int i = Integer.parseInt(matcher.group(1));
            time = time.plusDays(i);
            result = result.replace(matcher.group(0), time.toString("yyyyMMdd"));
        }

        return result;
    }

    public static String plusMonth(String sql, Date date) {
        String result = sql;

        // yyyyMM-i
        Pattern patten = Pattern.compile("(?i)\\$\\{hiveconf:yyyyMM([\\-|\\+][0-9]+)\\}");
        Matcher matcher = patten.matcher(result);

        while (matcher.find()) {

            DateTime time = new DateTime(date);
            int i = Integer.parseInt(matcher.group(1));
            time = time.plusMonths(i);

            result = result.replace(matcher.group(0), time.toString("yyyyMM"));
        }

        return result;
    }

    public static String parse(String sql, Date date) {

        String result = sql;

        if (result == null || date == null) {
            return result;
        }

        DateTime time = new DateTime(date);

        result = result.replaceAll("(?i)\\$\\{hiveconf:year\\}", time.getYear() + "");
        result = result.replaceAll("(?i)\\$\\{hiveconf:month\\}", time.getMonthOfYear() + "");
        result = result.replaceAll("(?i)\\$\\{hiveconf:day\\}", time.getDayOfMonth() + "");
        result = result.replaceAll("(?i)\\$\\{hiveconf:hour\\}", time.getHourOfDay() + "");
        result = result.replaceAll("(?i)\\$\\{hiveconf:minute\\}", time.getMinuteOfHour() + "");

        result = result.replaceAll("(?i)\\$\\{hiveconf:yyyyMMddHHmm\\}", time.toString("yyyyMMddHHmm"));
        result = result.replaceAll("(?i)\\$\\{hiveconf:yyyyMMddHH\\}", time.toString("yyyyMMddHH"));
        result = result.replaceAll("(?i)\\$\\{hiveconf:yyyyMMdd\\}", time.toString("yyyyMMdd"));
        result = result.replaceAll("(?i)\\$\\{hiveconf:yyyyMM\\}", time.toString("yyyyMM"));

        result = result.replaceAll("(?i)\\$\\{hiveconf:now\\}", System.currentTimeMillis() + "");

        // 替换yyyyMMddHHmm - i
        result = plusMinute(result, date);

        // 替换yyyyMMddHH - i
        result = plusHour(result, date);

        // 替换yyyyMMdd - i
        result = plusDay(result, date);

        // 替换yyyyMM - i
        result = plusMonth(result, date);

        return result;
    }


}
