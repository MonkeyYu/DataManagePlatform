/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.task;

import com.etone.daemon.util.Mixeds;
import com.etone.universe.dmp.event.BaseEvent;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.task.ftp.UploadTask;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Created by Lanny on 2016-8-23.
 */
public class BaseTask implements Callable {

    private static final Logger logger = LoggerFactory.getLogger(BaseTask.class);

    protected long jobId = 0;

    protected int priority = 0;

    protected String name = "";

    protected Date time = null;

    protected boolean block = true;

    protected int skip = -1;

    protected int retry = 3;

    protected long timeOut = 720l;// task执行超时时间，单位：min，默认 720min

    protected String exception = null;

    /**
     * Task 调用的入口
     */
    @Override
    public Object call() throws Exception {

        // 任务开始执行时，设置任务状态为1，并向事件总线提交一次任务（Finish）
        postEvent(BaseEvent.RUNNING);

        logger.info("\n-----------------------------jobId={},name={},priority={}----------------------------", this.getJobId(), this.getName(), this.getPriority());
        // do task
        execute();

        // 任务执行完成后，将任务状态设置为2，并向事件总线提交一次任务（Finish）
        postEvent(BaseEvent.FINISH);

        return null;
    }

    /**
     * 执行任务的代码，各子类分别实现各自的逻辑
     */
    public void execute() {
    }

    /**
     * 向事件总线提交任务的状态
     *
     * @param status
     */
    public void postEvent(int status) {
        // 任务启动时，向事件总线提交一次任务的状态（Running）
        BaseEvent event = new BaseEvent();
        event.initialize(this);
        event.setStatus(status);
        event.setStart(Calendar.getInstance().getTime());
        EventService.getInstance().post(event);
    }

    /**
     * 从xml中初始化任务的属性
     *
     * @param task
     */
    public void fromXml(Element task) throws Exception {

        // 从xml中读取task的名称
        String taskName = task.attr("name");
        if (Mixeds.isNullOrEmpty(taskName)) {
            throw new Exception("Attribute name is not set : " + task.toString());
        }
        setName(taskName);

        // 任务优先级
        String priority = task.attr("priority");
        if (Mixeds.isNullOrEmpty(priority)) {
            throw new Exception("Attribute priority is not set on task[name=" + getName() + "]");
        }
        setPriority(Integer.parseInt(priority));

        // 任务失败跳至的task
        String skipToPriority = task.attr("goto");
        if (!Mixeds.isNullOrEmpty(skipToPriority)) {
            setSkip(Integer.parseInt(skipToPriority));
        }

        // 任务失败重试次数
        String retry = task.attr("retry");
        if (!Mixeds.isNullOrEmpty(retry)) {
            setRetry(Integer.parseInt(retry));
        }

        // 任务失败重试次数
        String timeOut = task.attr("timeOutMinute");
        if (!Mixeds.isNullOrEmpty(timeOut)) {
            setTimeOut(Long.parseLong(timeOut));
        }

        // 是否为阻塞任务，默认都是阻塞任务
        String block = task.attr("block");
        if ("false".equalsIgnoreCase(block)) setBlock(false);
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

}
