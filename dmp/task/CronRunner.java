/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */

package com.etone.universe.dmp.task;

import com.etone.daemon.util.Mixeds;
import com.etone.daemon.util.Threads;
import com.etone.universe.dmp.event.BaseEvent;
import com.etone.universe.dmp.event.EventService;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.jsoup.nodes.Element;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * 默认任务，实现QuartZ.StatefulJob
 *
 * @author <a href="mailto:maxid@qq.com">ZengHan</a>
 * @since $$Id$$
 */
public class CronRunner {
    protected static Logger logger = LoggerFactory.getLogger(CronRunner.class);

    protected long id = 0;

    protected String name = "default job";

    protected int delay = 0;

    protected ArrayList<BaseTask> taskList = Lists.newArrayList();

    protected HashMap<Integer, Integer> priorityIdMap = new HashMap<Integer, Integer>();

    public void run(JobExecutionContext context) {

        ExecutorService executorService = Executors.newCachedThreadPool();

        // 获取TASK执行的业务时间(scheduledFireTime - intDelay)
        Date dataTime = getDataTime(context);

        // 遍历任务列表
        int i = 0;
        while (i < taskList.size()) {
            BaseTask task = taskList.get(i);

            // 如果任务重试次数已经达到阀值，则直接跳过执行下一个任务
            if (task.getRetry() <= 0) {
                i++;
                logger.error("Task={} has run out of retry count, skip next task", task.getName());
                continue;
            }

            // 设置task的业务时间
            task.setTime(dataTime);

            FutureTask<Object> future = new FutureTask(task);
            executorService.submit(future);

            if (task.isBlock()) {
                // 如果是阻塞任务，则直接执行；否则，以独立线程的方式启动任务（非阻塞）
                try {
                    future.get(task.getTimeOut(), TimeUnit.MINUTES);
                } catch (Exception e) {
                    task.setException("TimeoutException: task timeout");
                    future.cancel(true);
                    task.postEvent(BaseEvent.FINISH);
                }
            }

            // 如果任务执行异常，且有配置异常跳转:goto
            if (!Mixeds.isNullOrEmpty(task.getException()) && task.getSkip() >= 0) {

                // 重置任务的异常属性，避免对任务重试的影响
                task.setException(null);

                // retry--
                task.setRetry(task.getRetry() - 1);
                if (priorityIdMap.get(task.getSkip()) != null) i = priorityIdMap.get(task.getSkip());

                logger.error("Exception when run task={}, skip to {}, retry={}", task.getName(), task.getSkip(), task.getRetry());
                Threads.sleep(5000);
                continue;
            }

            // 如果任务执行正常，则进入下一个任务
            i++;
        }

        try {
            // 启动一次顺序关闭，执行以前提交的任务，但不接受新任务。
            executorService.shutdown();
            // 请求关闭、发生超时或者当前线程中断，无论哪一个首先发生之后，都将导致阻塞，直到所有任务完成执行
            // 设置最长等待2天
            executorService.awaitTermination(2, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从上下文中读取传入的参数列表，装箱操作
     *
     * @param context
     * @throws Exception
     */
    public void initialize(JobExecutionContext context) throws Exception {

        JobDataMap params = context.getJobDetail().getJobDataMap();

        setId(params.getLong("id"));
        setName(params.getString("name"));
        setDelay(params.getInt("delay"));

        // 从上下文中后期Element对象
        Element job = (Element) params.get("jobElement");

        // 初始化任务列表
        for (Element task : job.select("task")) {
            Class<?> clazz = Class.forName(task.attr("class"));
            // 实例化task列表
            BaseTask instance = (BaseTask) clazz.getConstructor().newInstance();
            instance.setJobId(getId());
            instance.fromXml(task);
            taskList.add(instance);
        }

        // 对任务进行排序
        Collections.sort(taskList, new Comparator<BaseTask>() {
            @Override
            public int compare(BaseTask o1, BaseTask o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });

        // 设置 Map<priority,index>
        int index = 0;
        for (BaseTask task : taskList) {
            if (Mixeds.isNotNull(priorityIdMap.get(task.getPriority()))) {
                logger.error("Found same task priority in job id={},name={}", id, this.getName());
                // 任务存在priority时，暂不抛出异常
                // throw new Exception("Found same task priority in job name=" + this.getName());
            }

            priorityIdMap.put(task.getPriority(), index);
            index++;
        }

    }

    /**
     * 获取task业务时间
     *
     * @param context
     * @return
     */
    private Date getDataTime(JobExecutionContext context) {
        // 计算任务的执行时间，取小于当前时间且离当前时间最近的一个周期时间作为任务的执行时间
        Date scheduledTime = context.getPreviousFireTime();
        if (scheduledTime == null) {// 第一次运行时scheduledTime设置为context.getFireTime()
            scheduledTime = context.getFireTime();
        }

        while (scheduledTime.before(context.getFireTime())) {
            Date tmp = context.getTrigger().getFireTimeAfter(scheduledTime);
            if (tmp.after(context.getFireTime())) {
                break;
            } else {
                scheduledTime = tmp;
            }
        }

        // 取任务的执行时间-delay后，设置为task的业务时间
        DateTime time = new DateTime(scheduledTime);
        return time.minusMinutes(delay).toDate();
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        CronRunner.logger = logger;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    public ArrayList<BaseTask> getTaskList() {
        return taskList;
    }

    public void setTaskList(ArrayList<BaseTask> taskList) {
        this.taskList = taskList;
    }
}
