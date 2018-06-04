/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */

package com.etone.universe.dmp;

import com.etone.daemon.plugin.Plugin;
import com.etone.daemon.plugin.Service;
import com.etone.daemon.plugin.Shutdown;
import com.etone.universe.dmp.event.*;
import com.etone.universe.dmp.event.EventListener;
import com.etone.universe.dmp.util.Utils;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 审计日志写入数据库服务入口，插件风格
 *
 * @author <a href="mailto:maxid@qq.com">ZengHan</a>
 * @since $$Id$$
 */
@Plugin(name = "AuditPlugin", priority = 1, enable = true)
public class AuditPlugin {
    private static final Logger logger = LoggerFactory.getLogger(AuditPlugin.class);

    private ConcurrentHashMap<String, BaseEvent> runningMap = new ConcurrentHashMap<String, BaseEvent>();

    EventListener eventListener = new EventListener() {
        @Subscribe
        public void saveLog(SqlEvent log) {
            log.save();
        }

        @Subscribe
        public void saveLog(MergeEvent log) {
            log.save();
        }

        @Subscribe
        public void saveLog(SplitEvent log) {
            log.save();
        }

        @Subscribe
        public void saveLog(FtpEvent log) {
            log.save();
        }

        @Subscribe
        public void saveLog(ExportEvent log) {
            log.save();
        }

        @Subscribe
        public void saveLog(ChmodEvent log) {
            log.save();
        }
        
        @Subscribe
        public void saveLog(ProblemEvent log) {
            log.save();
        }

        @Subscribe
        public void updateStatus(BaseEvent log) {
            String key = log.getJobId() + log.getPriority() + log.getName() + log.getFireTime();
            if (log.getStatus() == BaseEvent.RUNNING) {// running
                runningMap.put(key, log);
            } else {// finish or exception
                runningMap.remove(key);
            }
        }
    };

    @Service
    public void service() {
        logger.info("save plugin start ...");
        EventService.getInstance().register(eventListener);

        // start printout
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(60000);
                        if (runningMap.size() > 0) logger.info(getPrintOut());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        ).start();
    }

    @Shutdown
    public void shutdown() {
        logger.info("crontab plugin shutdown ...");
        EventService.getInstance().unregister(eventListener);
    }


    public String getPrintOut() {

        ArrayList<BaseEvent> list = new ArrayList<BaseEvent>();
        Iterator<Map.Entry<String, BaseEvent>> iter = runningMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, BaseEvent> entry = iter.next();
            BaseEvent baseEvent = entry.getValue();
            list.add(baseEvent);
        }

        // running task进行sort， 按jobId， priority，datatime升序排列
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                BaseEvent be1 = (BaseEvent) o1;
                BaseEvent be2 = (BaseEvent) o2;
                return Long.parseLong(be1.getFireTime()) > Long.parseLong(be2.getFireTime()) ? 1 : be1.getJobId() > be2.getJobId() ? 1 : be1.getPriority() > be2.getPriority() ? 1 : -1;
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        StringBuffer sb = new StringBuffer();
        sb.append("Active count : " + runningMap.size());
        sb.append("\n");
        sb.append(Utils.alignLeft("", 105, (byte) '-'));
        sb.append("\n");
        sb.append(Utils.alignLeft("jobId", 10, (byte) ' '));
        sb.append(Utils.alignLeft("priority", 10, (byte) ' '));
        sb.append(Utils.alignLeft("dataTime", 15, (byte) ' '));
        sb.append(Utils.alignLeft("name", 30, (byte) ' '));
        sb.append(Utils.alignLeft("status", 10, (byte) ' '));
        sb.append(Utils.alignLeft("start", 20, (byte) ' '));
        sb.append(Utils.alignLeft("spent(s)", 10, (byte) ' '));

        for (BaseEvent baseEvent : list) {
            sb.append("\n");
            sb.append(Utils.alignLeft(baseEvent.getJobId() + "", 10, (byte) ' '));
            sb.append(Utils.alignLeft(baseEvent.getPriority() + "", 10, (byte) ' '));
            sb.append(Utils.alignLeft(baseEvent.getFireTime(), 15, (byte) ' '));
            sb.append(Utils.alignLeft(baseEvent.getName(), 30, (byte) ' '));
            if (baseEvent.getStatus() == BaseEvent.WATTING) {
                sb.append(Utils.alignLeft("Waiting", 10, (byte) ' '));
            } else if (baseEvent.getStatus() == BaseEvent.RUNNING) {
                sb.append(Utils.alignLeft("Running", 10, (byte) ' '));
            } else {
                sb.append(Utils.alignLeft("Finish", 10, (byte) ' '));
            }
            sb.append(Utils.alignLeft(sdf.format(baseEvent.getStart()), 20, (byte) ' '));
            sb.append(Utils.alignLeft((System.currentTimeMillis() - baseEvent.getStart().getTime()) / 1000 + "", 10, (byte) ' '));
        }
        sb.append("\n");
        sb.append(Utils.alignLeft("", 105, (byte) '-'));
        return sb.toString();
    }

}
