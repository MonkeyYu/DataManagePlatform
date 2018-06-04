/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.event;

import com.etone.daemon.db.DB;
import com.etone.daemon.db.helper.QueryHelper;
import com.etone.daemon.db.sql.SqlBuilder;
import com.etone.daemon.db.sql.SqlColumn;
import com.etone.daemon.support.Env;
import com.etone.universe.dmp.task.BaseTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lanny on 2016-8-25.
 */
public class BaseEvent {
    private static final Logger logger = LoggerFactory.getLogger(BaseEvent.class);

    public static final int WATTING = 0;

    public static final int RUNNING = 1;

    public static final int FINISH = 2;

    @SqlColumn(name = "intPriority")
    protected int priority = 0;

    @SqlColumn(name = "vcName")
    protected String name = "";

    @SqlColumn(name = "dtStime")
    protected Date start = null;

    @SqlColumn(name = "dtEtime")
    protected Date end = null;

    @SqlColumn(name = "longSpend")
    protected Long spend = 0l;

    @SqlColumn(name = "vcException")
    protected String exception = "";

    @SqlColumn(name = "intJobId")
    protected long jobId = 0;

    protected String clazz = "";

    protected int status = 0; // 0:wait; 1:running; 2:finish

    @SqlColumn(name = "vcFireTime")
    protected String fireTime = null;

    public void initialize(BaseTask task){
        setJobId(task.getJobId());
        setName(task.getName());
        setPriority(task.getPriority());
        setFireTime(new SimpleDateFormat("yyyyMMddHHmm").format(task.getTime()));
    }

    /**
     * 日志写入数据库
     */
    public void save() {
        logger.info("save {} log jobId={},name={},spend={}", this.getClazz(), this.getJobId(), this.getName(), this.getSpend());
        SqlBuilder bulider = SqlBuilder.create(this.getClass(), SqlBuilder.DbType.MYSQL);
        String insert = bulider.insert().build();
        Object[] params = bulider.getParams(this, false);
        Connection conn = DB.getConnection(Env.getProperties().getValue("datasource.log"));
        try {
            QueryHelper.execute(conn, insert, params);
        }catch (SQLException e) {
            logger.error("SQLException : ", e);
        }finally {
            DB.close();
        }
    }

    public BaseEvent() {
        setClazz(this.getClass().toString());
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

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public long getSpend() {
        return spend;
    }

    public void setSpend(long spend) {
        this.spend = spend;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFireTime() {
        return fireTime;
    }

    public void setFireTime(String fireTime) {
        this.fireTime = fireTime;
    }

}
