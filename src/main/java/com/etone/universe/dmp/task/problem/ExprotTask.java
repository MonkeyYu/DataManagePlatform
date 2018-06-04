/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.task.problem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Calendar;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.daemon.db.DB;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.SqlEvent;
import com.etone.universe.dmp.task.Variables;

/**
 * 数据分发使用任务对象
 * 通过外部表对数据进行导出，相比GpSqlTask增加了linux命令支持
 *
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @since $$Id$$
 */
public class ExprotTask extends ProblemTask {

	private static final Logger logger = LoggerFactory
			.getLogger(ExprotTask.class);

	@Override
	public void execute() {

		SqlEvent event = new SqlEvent();
		event.setStart(Calendar.getInstance().getTime());
		event.setPriority(getPriority());
		event.setName(getName());
		logger.info("Start Greenplum sql file task : {}", this.getName());
		try {

			String[] sqls = readSQL(getName());

			Connection conn = DB.getConnection(getDataSource());
			for (String sql : sqls) {
				sql = Variables.parse(sql, getTime());
				logger.info(sql);

				// 若出现$linux$特殊字符，则是linux命令
				if (sql.contains("$linux$")) {
					sql = sql.replace("$linux$", "");
					excuteLiuxOrde3r(sql);
					continue;
				}

				if (sql != null
						&& sql.trim().toLowerCase().startsWith("select")) {
					// 针对GreenPlum的存储过程调用方式进行特殊处理
					selectMap(conn, sql);
				} else {
					execute(conn, sql);
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
		Duration d = new Duration(new DateTime(event.getStart()), new DateTime(
				event.getEnd()));
		event.setSpend(d.getMillis());

		EventService.getInstance().post(event);

		// 调度指定job
		super.execute();
	}

	/**
	 * 执行linux命令方法
	 * @param command
	 * @return
	 */
	public boolean excuteLiuxOrde3r(String command) {
		addDetaileLog("执行命令：" + command);
		boolean returnFlag = false;
		StringBuilder sb = new StringBuilder();
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
			returnFlag = true;
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			String lo;
			while ((lo = bufferedReader.readLine()) != null) {
				sb.append(lo);
				sb.append("\n");
			}
			process.waitFor();
			// 等待n秒后destory进程
		} catch (Exception e) {
			addDetaileLog("执行命令[" + command + "]出错:" + e.getMessage());
		}
		process.destroy();
		return returnFlag;
	}
}
