/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.task.sql;

import com.etone.daemon.db.DB;
import com.etone.daemon.db.helper.QueryHelper;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.SqlEvent;
import com.etone.universe.dmp.task.BaseTask;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * Hive任务
 *
 * @author <a href="mailto:lanny@qq.com">LiangYonghua</a>
 * @since $$Id$$
 */
public class ProcTask extends BaseTask {
    private static final Logger logger = LoggerFactory.getLogger(ProcTask.class);

    @Override
    public void execute() {

        SqlEvent event = new SqlEvent();
        event.setStart(Calendar.getInstance().getTime());
        event.setPriority(getPriority());
        event.setName(getName());

        // do something
        logger.info("Start GreenPlum procedure task : {}", this.getName());


        Connection conn = DB.getConnection("gp");
        final String sql = "select " + getName().replaceAll("\\?", "'" + new DateTime(getTime()).toString("yyyyMMddHHmmss") + "'");
        logger.info(sql);

        QueryHelper.transaction(new QueryHelper.Template() {
            @Override
            public void execute(Connection conn) throws SQLException {
                conn.createStatement().execute(sql);
            }
        }, conn);

        event.setEnd(Calendar.getInstance().getTime());
        Duration d = new Duration(new DateTime(event.getStart()), new DateTime(event.getEnd()));
        event.setSpend(d.getMillis());

        EventService.getInstance().post(event);
    }
}
