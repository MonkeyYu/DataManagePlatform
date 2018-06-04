/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.task.sql;

import com.etone.daemon.db.DB;
import com.etone.daemon.db.helper.QueryHelper;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.SqlEvent;
import com.etone.universe.dmp.task.Variables;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Calendar;

/**
 * Hive任务
 *
 * @author <a href="mailto:lanny@qq.com">LiangYonghua</a>
 * @since $$Id$$
 */
public class GpSqlTask extends SqlTask {
    private static final Logger logger = LoggerFactory.getLogger(GpSqlTask.class);

    @Override
    public void execute() {

        SqlEvent event = new SqlEvent();
        event.setStart(Calendar.getInstance().getTime());
        event.setPriority(getPriority());
        event.setName(getName());
//        event.setFireTime(Calendar.getInstance().getTime());
//        event.setDataTime(getTime());

        // do something
        logger.info("Start Greenplum sql file task : {}", this.getName());
        try {

            String[] sqls = readSQL(getName());

            Connection conn = DB.getConnection(getDataSource());
            for (String sql : sqls) {
                sql = Variables.parse(sql, getTime());
                logger.info(sql);

                if (sql != null && sql.trim().toLowerCase().startsWith("select")) {
                    // 针对GreenPlum的存储过程调用方式进行特殊处理
                    QueryHelper.selectMap(conn, sql);
                } else {
                    QueryHelper.execute(conn, sql);
                }
            }
        } catch (Exception e) { // 脚本过程中的任意一条SQL异常，则跳出循环，不继续往下执行
            logger.error("SQLException : ", e);
            event.setException(e.getMessage());
            this.setException(e.getMessage());
        } finally {
            DB.close();
        }
        event.setEnd(Calendar.getInstance().getTime());
        Duration d = new Duration(new DateTime(event.getStart()), new DateTime(event.getEnd()));
        event.setSpend(d.getMillis());

        EventService.getInstance().post(event);
    }
}
