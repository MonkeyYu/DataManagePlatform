package com.etone.universe.dmp.util;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 提共对日期的一些操作的方法
 *
 * @author guojian
 * @date 2014-07-07
 */
public class DateUtils {

    private static Logger logger = LoggerFactory.getLogger(DateUtils.class);

    /**
     * 将Date类型的日期转换为String类型(yyyy-MM-dd HH:mm:ss)的日期
     *
     * @param date ate类型的日期
     * @return String类型的日期
     */
    public static String convertDateToLongString(Date date) {
        String value = null;
        DateFormat dateFormat = null;

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        value = dateFormat.format(date);

        dateFormat = null;

        return value;
    }


    public static String convertDateToShortString(Date date) {
        String value = null;
        DateFormat dateFormat = null;

        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (date != null)
        value = dateFormat.format(date);

        dateFormat = null;

        return value;
    }

    /**
     * 将Date类型的日期转换为String类型的日期
     *
     * @param date ate类型的日期
     * @return String类型的日期
     */
    public static String convertDateToString(Date date) {
        String value = null;
        DateFormat dateFormat = null;

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        value = dateFormat.format(date);

        dateFormat = null;

        return value;
    }

    /**
     * 将String类型的日期转换为Date类型的日期
     *
     * @param date String类型的日期
     * @return Date类型的日期
     * @throws java.text.ParseException
     */
    public  static Date convertStringToDate(String date) throws ParseException {
        Date value = null;
        DateFormat dateFormat = null;

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (date != null)
            value = dateFormat.parse(date);

        dateFormat = null;

        return value;
    }

    /**
     * @param formatString
     * @param date
     * @return
     */
    public static Date convertStringToDate(String formatString, String date) {
        Date value = null;
        DateFormat dateFormat = new SimpleDateFormat(formatString);
        if (date != null) {
            try {
                value = dateFormat.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    /**
     * @param formatString
     * @param date
     * @return
     */
    public static String convertDateToString(String formatString, Date date) {
        String value = null;

        if (formatString == null)
            formatString = "yyyy-MM-dd HH:mm:ss.SSS";

        SimpleDateFormat format = new SimpleDateFormat(formatString);
        value = format.format(date);
        format = null;

        return value;
    }

    /**
     * 将Date类型的日期转换为String类型的日期,日期格式自己定
     *
     * @param format
     * @param date
     * @return
     */
    public static String convertDateToString(DateFormat format, Date date) {
        String value = null;

        if (format == null)
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        value = format.format(date);
        format = null;

        return value;
    }

    /**
     * 将String类型的日期转换为Date类型的日期,日期格式自己定
     *
     * @param format
     * @param loadDate
     * @return
     * @throws Exception
     */
    public static Date convertStringToDate(DateFormat format, String loadDate)
        throws ParseException {
        Date value = null;

        if (format == null)
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        value = format.parse(loadDate);
        format = null;

        return value;
    }

    /**
     * 把long类型的纳秒时间转换为Date类型的日期
     *
     * @return
     * @throws Exception
     */
    public static Date convertLongToDate(long millis) throws Exception {
        Date date = new Date(millis);

        return date;
    }

    /**
     * 取得系统的当前时间
     *
     * @return String类型的日期
     */
    public static String getCurrentDateString() {
        String value = null;
        DateFormat dateFormat = null;

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        value = dateFormat.format(new Date(System.currentTimeMillis()));

        dateFormat = null;

        return value;
    }

    /**
     * 取得系统的当前时间
     *
     * @return String类型的日期
     */
    public static Date getCurrentDate() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * 取得系统的当前时间的long类型
     *
     * @return String类型的日期
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 根据年，月，日，取得对应的date
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static Date getDate(int year, int month, int day) {
        Calendar calendar = new GregorianCalendar();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, day);

        return calendar.getTime();
    }

    /**
     * 根据年，月，日，时，分，秒，取得对应的date
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public static Date getDate(int year, int month, int day, int hour,
                               int minute, int second) {
        Calendar calendar = new GregorianCalendar();

        // calendar.set(Calendar.YEAR, year);
        // calendar.set(Calendar.MONTH, month - 1);
        // calendar.set(Calendar.DATE, day);
        calendar.set(year, month - 1, day, hour, minute, second);
        return calendar.getTime();
    }

    /**
     * 取得系统当前时间的年份
     *
     * @return 系统时间的当前年份
     */
    public static int getCurrentYear() {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(new Date(System.currentTimeMillis()));

        return calendar.get(Calendar.YEAR);
    }

    /**
     * 取得这个时间的年份
     *
     * @param date 时间
     * @return 年份
     */
    public static int getYear(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        return calendar.get(Calendar.YEAR);
    }

    /**
     * 取得系统当前时间的月份
     *
     * @return
     */
    public static int getCurrentMonth() {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(new Date(System.currentTimeMillis()));

        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 取得这个时间的月份
     *
     * @param date 时间
     * @return 月份
     */
    public static int getMonth(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 判断是否周末
     *
     * @param date
     * @return
     */
    public static boolean isWeekend(Date date) {
        boolean result = false;

        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        if (calendar.get(Calendar.DAY_OF_WEEK) == 1
            || calendar.get(Calendar.DAY_OF_WEEK) == 7)
            result = true;

        return result;
    }

    /**
     * 取得系统当前时间，在一年中的第几周
     *
     * @param date
     * @return
     */
    public static int getWeekOfYear(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * 取得系统当前时间的日
     *
     * @return
     */
    public static int getCurrentDay() {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(new Date(System.currentTimeMillis()));

        return calendar.get(Calendar.DATE);
    }

    /**
     * 取得这个时间的日
     *
     * @param date 时间
     * @return 日
     */
    public static int getDay(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        return calendar.get(Calendar.DATE);
    }

    /**
     * 取得系统当前时间的小时(24小时制度)
     *
     * @return
     */
    public static int getCurrentHour() {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(new Date(System.currentTimeMillis()));

        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 取得这个时间的小时
     *
     * @param date 时间
     * @return 小时
     */
    public static int getHour(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 取得这个时间的分钟
     *
     * @param date 时间
     * @return 分钟
     */
    public static int getMinute(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        return calendar.get(Calendar.MINUTE);
    }

    /**
     * 取得这个时间的毫秒
     *
     * @param date 时间
     * @return 毫秒
     */
    public static int getMsecond(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        return calendar.get(Calendar.MILLISECOND);
    }

    /**
     * 取得这个时间的秒
     *
     * @param date 时间
     * @return 秒
     */
    public static int getSecond(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        return calendar.get(Calendar.SECOND);
    }

    /**
     * 取得系统当前时间的月份，一共有多少天
     *
     * @return
     */
    public static int getCurrentDayOfMonth() {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(new Date(System.currentTimeMillis()));

        return calendar.getActualMaximum(Calendar.DATE);
    }

    /**
     * 指定年月，取得该月一共有多少天
     *
     * @param year  年
     * @param month 月
     * @return 该月一共有多少天
     */
    public static int getDayOfMonth(int year, int month) {
        Calendar calendar = new GregorianCalendar();

        calendar.set(Calendar.YEAR, year);
        /**
         * Java月份才0开始算
         */
        calendar.set(Calendar.MONTH, month - 1);

        return calendar.getActualMaximum(Calendar.DATE);
    }

    /**
     * 把该date类型的时间变为0时0刻的时间
     *
     * @param date 需要转变的时间
     * @return 0时0刻的时间
     */
    public static Date getDateStart(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间变为0分0秒0毫秒的时间
     *
     * @param date 需要转变的时间
     * @return 0时0刻的时间
     */
    public static Date getDateHourStart(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间变为0分0秒0毫秒的时间
     *
     * @param date 需要转变的时间
     * @return 0时0刻的时间
     */
    public static Date getTimeHourStart(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);
        calendar.add(Calendar.HOUR, -1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
    /**
     * 把该date类型的时间变为0分0秒0毫秒的时间
     *
     * @param date 需要转变的时间
     * @return 0时0刻的时间
     */
    public static Date getTimeHourEnd(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间变为23时59分59秒999毫秒的时间
     *
     * @param date 需要转变的时间
     * @return 99时99刻的时间
     */
    public static Date getDateEnd(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    /**
     * 获得date类型的时间的分钟间隔时间
     *
     * @param date       需要转换的时间
     * @param minInteval 分钟间隔
     * @return
     */
    public static Date getMinIntevalDate(Date date, int minInteval) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minInteval);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间增加n天
     *
     * @param date   需要转变的时间
     * @param amount 需要增加的天数
     * @return 时间
     */
    public static Date addDay(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.add(Calendar.DATE, amount);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间增加n分钟
     *
     * @param date   需要转变的时间
     * @param amount 需要增加的分钟数
     * @return 时间
     */
    public static Date addHour(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.add(Calendar.HOUR_OF_DAY, amount);

        return calendar.getTime();
    }

    /**
     * @param date
     * @param amount
     * @return
     */
    public static Date setHour(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, amount);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间的分钟设置为指定值
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date setMinute(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.set(Calendar.MINUTE, amount);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间增加n分钟
     *
     * @param date   需要转变的时间
     * @param amount 需要增加的分钟数
     * @return 时间
     */
    public static Date addMinute(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.add(Calendar.MINUTE, amount);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间增加n秒
     *
     * @param date   需要转变的时间
     * @param amount 需要增加的秒数
     * @return 时间
     */
    public static Date addSecond(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.add(Calendar.SECOND, amount);

        return calendar.getTime();
    }

    /**
     * @param date
     * @param amount
     * @return
     */
    public static Date setSecond(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.set(Calendar.SECOND, amount);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间增加n毫秒
     *
     * @param date   需要转变的时间
     * @param amount 需要增加的毫秒数
     * @return 时间
     */
    public static Date addMillSecond(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.add(Calendar.MILLISECOND, amount);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间的天设置为指定值
     *
     * @param date   需要转变的时间
     * @param amount 需要设置的值
     * @return 时间
     */
    public static Date setDay(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.set(Calendar.DATE, amount);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间增加n个月
     *
     * @param date   需要转变的时间
     * @param amount 需要增加的天数
     * @return 时间
     */
    public static Date addMonth(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.add(Calendar.MONTH, amount);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间的月份设置为指定值
     *
     * @param date   需要转变的时间
     * @param amount 需要设置的值
     * @return 时间
     */
    public static Date setMonth(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.set(Calendar.MONTH, amount - 1);

        return calendar.getTime();
    }

    /**
     * 把该date类型的时间增加n年
     *
     * @param date   需要转变的时间
     * @param amount 需要增加的年数
     * @return 时间
     */
    public static Date addYear(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.add(Calendar.YEAR, amount);

        return calendar.getTime();
    }

    /**
     * @param date
     * @param amount
     * @return
     */
    public static Date setYear(Date date, int amount) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.set(Calendar.YEAR, amount);

        return calendar.getTime();
    }

    /**
     * 求两个时间之间的间隔
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 两个时间之间的间隔天，以一个Date类型的list返回
     * @throws Exception
     */
    public static ArrayList<Date> getDateInterval(Date startDate, Date endDate)
        throws Exception {
        Calendar calendar = new GregorianCalendar();
        ArrayList<Date> reslut = new ArrayList<Date>();
        Date intervalDate = null;

        /**
         * 当结束时间小时开始时间时
         */
        if (endDate.compareTo(startDate) < 0)
            throw new Exception("求取时间间隔出错,结束时间小于开始时间！");
        /**
         * 当结束时间大于等于开始时间时
         */
        if (endDate.compareTo(startDate) >= 0) {
            /**
             * 将时间设为整0时
             */
            calendar.setTime(startDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            startDate = calendar.getTime();

            calendar.setTime(endDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            endDate = calendar.getTime();

            calendar.setTime(startDate);
            /**
             * 不断地增加开始时间的天数，与结束的日期进行对比
             */
            intervalDate = startDate;
            while (true) {
                reslut.add(intervalDate);

                calendar.add(Calendar.DATE, 1);

                intervalDate = calendar.getTime();

                if (endDate.compareTo(intervalDate) < 0)
                    return reslut;
            }
        }

        return reslut;
    }

    /**
     * 判断两个时间是否在同一天
     *
     * @param
     * @param otherDate 时间2
     * @return 是否
     */
    public static boolean isSameDay(Date date, Date otherDate) {
        Calendar calendar = new GregorianCalendar();
        boolean result = false;

        /**
         * 将时间设为整0时
         */
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();

        calendar.setTime(otherDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        otherDate = calendar.getTime();

        if (date.compareTo(otherDate) == 0)
            result = true;

        return result;
    }

    /**
     * 比较两个日期之间的时间差，取得毫秒的间隔
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long getTimeInterval(Date beginDate, Date endDate) {
        long between;

        between = endDate.getTime() - beginDate.getTime();

        return between;

    }

    /**
     * 比较两个日期之间的时间差，取得毫秒的间隔
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long getTimeInterval(long beginDate, long endDate) {
        long between;

        between = endDate - beginDate;

        return between;
    }

    /**
     * 根据时间间隔,获取上一分钟粒度时间
     *
     * @param dateNow
     * @param timeSpan
     * @return
     */
    public static Date getPrevMinDate(Date dateNow, Integer timeSpan) {
        Date date = null;
        Integer minute = getMinute(dateNow);
        Integer beginM = 0;
        if (minute >= timeSpan) {
            date = dateNow;
            beginM = minute - (minute % timeSpan + timeSpan);
        } else {
            date = addHour(dateNow, -1);
            beginM = 60 - timeSpan;
        }
        date = setMinute(date, beginM);
        return date;
    }

    /**
     * @param date
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public static Date setDate(Date date, Integer year, Integer month,
                               Integer day, Integer hour, Integer minute, Integer second) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        if (year != null) {
            calendar.set(Calendar.YEAR, year);
        }
        if (month != null) {
            calendar.set(Calendar.MONTH, month - 1);
        }
        if (day != null) {
            calendar.set(Calendar.DATE, day);
        }
        if (hour != null) {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute != null) {
            calendar.set(Calendar.MINUTE, minute);
        }
        if (second != null) {
            calendar.set(Calendar.SECOND, second);
        }
        return calendar.getTime();
    }

    /**
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public static Date setDate(Integer year, Integer month, Integer day,
                               Integer hour, Integer minute, Integer second) {
        return setDate(new Date(), year, month, day, hour, minute, second);
    }

    /**
     * 对比时间. (1表示最大,2表示最小)
     *
     * @param type
     * @param dates
     * @return
     */
    public static Date getCompareDate(Integer type, Date... dates) {
        Date res = null;
        if (dates != null && dates.length > 0) {
            for (int i = 0; i < dates.length; i++) {
                if (i == 0) {
                    res = dates[i];
                } else {
                    if (type.equals(1) && dates[i].compareTo(res) > 0) {
                        res = dates[i];
                    } else if (type.equals(2) && dates[i].compareTo(res) < 0) {
                        res = dates[i];
                    }
                }
            }
        }
        return res;
    }


    /**
     * 获取昨天的yyyyMMdd格式的字符串
     *
     * @return
     */
    public static String getYesterdayYMD() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(new Date(System.currentTimeMillis() - 24L * 60
            * 60 * 1000));
    }

    public static String getCurrentFullDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(new Date());
    }

    public static void main(String[] args) {
        System.out.println(getYesterdayYMD());
    }
    
    /**
     * 格式化时间 字符串转换为日期时间 格式为yyyy-MM-dd HH:mm:ss 转换失败返回null
     *
     * @param s
     * @return 格式为yyyy-MM-dd HH:mm:ss
     */
    public static Date getDate(String s) {
        if (s == null)
            return null;
        if (s.split("-").length == 2) {// 当时间格式为yyyy-MM格式时
            s = s + "-01";
        }
        String[] ds = s.split(" ");
        if (ds.length == 1) {
            s = s + " 00:00:00";
        } else if (ds.length == 2) {
            String[] ts = ds[1].split(":");
            if (ts.length == 1) {
                s = s + ":00:00";
            } else if (ts.length == 2) {
                s = s + ":00";
            }
        } else {
            logger.error("时间格式有误:请按此时间格式:yyyy-MM-dd HH:mm:ss");
            return null;
        }
        SimpleDateFormat simpledateformat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
        try {
            Date date = simpledateformat.parse(s);
            return date;
        } catch (Exception _ex) {
            logger.error("时间格式有误:请按此时间格式:yyyy-MM-dd HH:mm:ss");
            return null;
        }
    }

    /**
     * 返回两个时间之间小时时间列
     * @param timeStart
     * @param timeEnd
     * @return
     */
    public static List<String> getSuffixList(String timeStart,String timeEnd){
        long addLg = DateUtils.convertStringToDate("yyyy-MM-dd HH:mm:ss", timeStart).getTime();
        long endLg = DateUtils.convertStringToDate("yyyy-MM-dd HH:mm:ss", timeEnd).getTime();
        List<String> timeList = Lists.newArrayList();
        timeList.add(timeStart);
        while (true) {

            // 下一个小时
            addLg = addLg + 3600000;
            String endTime =DateUtils.convertDateToLongString(new Date(addLg));
            if (addLg >= endLg)
                break;
            timeList.add(endTime);
        }
        return timeList;
    }


    /**
     * 返回两个时间之间小时时间列
     * @param timeStart
     * @param timeEnd
     * @return
     */
    public static List<String> getSuffixListMinutes(String timeStart,String timeEnd){
        long addLg = DateUtils.convertStringToDate("yyyy-MM-dd HH:mm:ss", timeStart).getTime();
        long endLg = DateUtils.convertStringToDate("yyyy-MM-dd HH:mm:ss", timeEnd).getTime();
        List<String> timeList = Lists.newArrayList();
        timeList.add(timeStart);
        while (true) {

            // 下一个分钟
            addLg = addLg + 60000;
            String endTime =DateUtils.convertDateToLongString(new Date(addLg));
            if (addLg >= endLg)
                break;
            timeList.add(endTime);
        }
        return timeList;
    }
    /**
     * 返回两个时间之间天列表
     * @param timeStart
     * @param timeEnd
     * @return
     */
    public static List<String> getSuffixDayList(String timeStart,String timeEnd){
        long addLg = DateUtils.convertStringToDate("yyyy-MM-dd HH:mm:ss", timeStart).getTime();
        long endLg = DateUtils.convertStringToDate("yyyy-MM-dd HH:mm:ss", timeEnd).getTime();
        List<String> timeList = Lists.newArrayList();
        timeList.add(timeStart);
        while (true) {

            // 下一个小时
            addLg = addLg + 3600000*24;
            String endTime =DateUtils.convertDateToLongString(new Date(addLg));
            if (addLg >= endLg)
                break;
            timeList.add(endTime);
        }
        return timeList;
    }


    /**
     * 格式化时间 字符串转换为日期时间 格式为yyyy-MM-dd HH:mm:ss 转换失败返回null
     *        返回15分钟粒度的时间
     *          param  s
     * @return 格式为yyyy-MM-dd HH:mm:ss
     */
    public static Date getPrevMinDate15(Date dateNow, Integer timeSpan) {
        Date date = null;
        Integer minute = getMinute(dateNow);
        Integer beginM = 0;
        if (minute >= timeSpan*3) {
            date = dateNow;
            beginM = minute - (minute % timeSpan + timeSpan*3);
        } else {
            date = addHour(dateNow, -1);
            beginM = minute - minute % timeSpan - timeSpan*3+60;
        }
        date = setMinute(date, beginM);

        date = setSecond(date, 0);
        return date;
    }

    //15分钟粒度，结束时间
    public static Date getPrevMaxDate15(Date dateNow, Integer timeSpan) {
        Date date = null;
        Integer minute = getMinute(dateNow);
        Integer beginM = 0;
        if (minute >= timeSpan*2) {
            date = dateNow;
            beginM = minute - (minute % timeSpan + timeSpan*2);
        } else {
            date = addHour(dateNow, -1);
            beginM = minute - minute % timeSpan - timeSpan*2+60;
        }

        date = setMinute(date, beginM);
        date = setSecond(date, 0);
        return date;
    }
}
