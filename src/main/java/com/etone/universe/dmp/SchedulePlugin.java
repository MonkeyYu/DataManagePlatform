/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */

package com.etone.universe.dmp;

import com.etone.daemon.db.DB;
import com.etone.daemon.plugin.Plugin;
import com.etone.daemon.plugin.Service;
import com.etone.daemon.plugin.Shutdown;
import com.etone.daemon.support.Env;
import com.etone.daemon.support.ScheduleService;
import com.etone.daemon.util.Mixeds;
import com.etone.universe.dmp.task.BaseTask;
import com.etone.universe.dmp.task.CronRunner;
import com.etone.universe.dmp.task.StatefulRunner;
import com.etone.universe.dmp.task.StatelessRunner;

//import com.google.common.collect.Lists;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * 定时任务服务入口，插件风格
 *
 * @author <a href="mailto:maxid@qq.com">ZengHan</a>
 * @since $$Id$$
 */
@Plugin(name = "SchedulePlugin", priority = 1, enable = true)
public class SchedulePlugin extends ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulePlugin.class);

    @Override
    public void addScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Service
    public void service() {
        logger.info("crontab plugin start ...");
        // 初始化调度器
        try {

            // 初始化数据库连接池
//            DB.initSource();

            // 从xml中读取并添加任务
            initJobs();

            // 启动调度器
            this.start();

        } catch (SchedulerException e) {
            logger.error("scheduler init failure : ", e);
        } catch (Exception e) {
            logger.error("scheduler init failure : ", e);
        }
    }

    @Shutdown
    public void shutdown() {
        logger.info("crontab plugin shutdown ...");
        try {
            this.stop();
        } catch (SchedulerException e) {
            logger.error("scheduler shutdown failure : ", e);
        }
    }

    /**
     * 从xml文件中加载任务列表
     */
    private void  initJobs() throws ParseException, SchedulerException, IOException {

        // 读取系统环境变量，该变量在启动脚本run.sh中通过-D参数配置
        String xml = System.getProperty("xml");
        String path = Env.getConfigFile(xml);

        File config = new File(path);
        if (!config.exists()) {
            logger.error("schedule configure file {} not exists : ", path);
            logger.info("shutdown schedule plugin");
            System.exit(1);
        }
        logger.info("read schedule configure from xml : {}", path);

        Document root = Jsoup.parse(config, "UTF-8");

        // 这个必须加，构建Quartz调度器
        StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
        String threadSize = root.select("configuration").attr("threadSize");
        // 如果用户在配置文件中设定了线程数，则使用设定值，否则系统默认10
        if(!Mixeds.isNullOrEmpty(threadSize)){
            Properties props = new Properties();
            props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
            props.setProperty("org.quartz.threadPool.threadCount", threadSize);
            stdSchedulerFactory.initialize(props);
        }
        addScheduler(stdSchedulerFactory.getScheduler());

        // 从xml文件中读取job列表
        for (Element job : root.select("configuration > job")) {

            String status = job.attr("status");
            if (Mixeds.isNotNull(status) && "on".equalsIgnoreCase(status)) {

                JobDataMap params = new JobDataMap();
                params.put("jobElement", job);

                String id = job.attr("id");
                if (Mixeds.isNotNull(id)) params.put("id", Long.parseLong(id));

                String jobName = job.attr("name");
                if (Mixeds.isNotNull(jobName)) params.put("name", jobName);

                String delay = job.attr("delay");
                if (Mixeds.isNotNull(delay)) params.put("delay", Integer.parseInt(delay));

                String script = job.attr("script");
                if (Mixeds.isNotNull(script)) params.put("script", script);

                // 往QuartZ里面添加job
                if ("stateless".equalsIgnoreCase(job.attr("class"))) {
                    this.addJob(StatelessRunner.class, params.getString("name"), params.getString("script"), params, false);
                } else {
                    this.addJob(StatefulRunner.class, params.getString("name"), params.getString("script"), params, false);
                }
            }
        }
    }
}
