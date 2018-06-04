/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.task.sql;

import com.etone.daemon.db.DB;
import com.etone.daemon.db.helper.QueryHelper;
import com.etone.daemon.support.Env;
import com.etone.daemon.util.FileAccessor;
import com.etone.daemon.util.Mixeds;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.SqlEvent;
import com.etone.universe.dmp.task.BaseTask;
import com.etone.universe.dmp.task.Variables;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.util.Calendar;

/**
 * 执行SQL脚本的任务
 *
 * @author <a href="mailto:lanny@qq.com">LiangYonghua</a>
 * @since $$Id$$
 */
public class SqlTask extends BaseTask {
    private static final Logger logger = LoggerFactory.getLogger(SqlTask.class);

    private String dataSource = "";

    @Override
    public void execute() {

        SqlEvent event = new SqlEvent();
        event.initialize(this);
        event.setStart(Calendar.getInstance().getTime());

        // do something
        logger.info("Start sql file task : {}", this.getName());
        try {
            String[] sqls = readSQL(getName());

            Connection conn = DB.getConnection(getDataSource());
            for (String sql : sqls) {
                sql = Variables.parse(sql, getTime());
                logger.info(sql);
                QueryHelper.execute(conn, sql);
            }
        } catch (Exception e) { // 脚本过程中的任意一条SQL异常，则跳出循环，不继续往下执行
            e.printStackTrace();
            logger.error("Exception : ", e);
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

    /**
     * 从文件中读取SQL脚本
     *
     * @param name
     * @return
     */
    public String[] readSQL(String name) throws Exception {

        StringBuilder content = new StringBuilder();

        // read configure from conf/fop/${this.name}
        String path = Env.getPath("conf") + File.separator + "sql" + File.separator + this.getName();
        if (Mixeds.isNullOrEmpty(path)) {
            throw new Exception("file not found : " + path);
        }

        logger.debug("read sql from file : " + path);
        FileAccessor accessor = FileAccessor.getInstance(path);
        for (String line : accessor) {
            line = line.trim();
            // 跳过注解行或者空行；
            if (!Mixeds.isNullOrEmpty(line) && !line.startsWith("--") && !line.startsWith("#")) {
                content.append("\n");
                content.append(line);
            }
        }

        // 对SQL内容进行分隔，分隔符为英文;
        String[] sql = content.toString().split(";");
        return sql;
    }

    @Override
    public void fromXml(Element task) throws Exception {
        super.fromXml(task);

        // DataSource
        String source = task.attr("source");
        if (Mixeds.isNullOrEmpty(source)) {
            throw new Exception("Attribute source is not set on task[name=" + getName() + "]");
        }
        setDataSource(source);
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
