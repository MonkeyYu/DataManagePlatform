/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.task.problem;

import java.sql.Connection;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.daemon.db.DB;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.SqlEvent;
import com.etone.universe.dmp.problem.DisTaskProcessorThread;

/** 
 * 数据分发使用任务配置执行,每天扫一次任务表
 * 
 * User: JiangWei
 * Date: 2017年5月31日
 * Time: 上午10:58:47
 * 
 */
public class DisTaskCfgTask extends ProblemTask {

	private static final Logger logger = LoggerFactory.getLogger(DisTaskCfgTask.class);

	@Override
	public void execute() {

		SqlEvent event = new SqlEvent();
		event.setStart(Calendar.getInstance().getTime());
		event.setPriority(getPriority());
		event.setName(getName());
		logger.info("Start Greenplum sql file task : {}", this.getName());
		try {

			Connection mysqlConn = DB.getConnection(parpams.get("disdatasource"));

			// 查询出所有的任务分发配置任务
			java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
			List<Map<String, Object>> tasks = selectMap(mysqlConn, sqlMap.get("queryTask"), date, date);

			// 创建线程池。默认最大并发为10
			int threadNum = parpams.get("threadnum") == null || "".equals(parpams.get("threadnum")) ? 10
					: Integer.valueOf(parpams.get("threadnum"));

			ExecutorService executor = Executors.newFixedThreadPool(threadNum);

			// 遍历任务，把符合执行条件的任务执行一遍
			for (Map<String, Object> task : tasks) {

				String[] sqls = null;

				// 判断执行类型
				String dateType = task.get("vcdatetype").toString();

				if ("day".equals(dateType)) {

					// 每天执行
					sqls = getSqls(task, false);

				} else if ("week".equals(dateType)) {

					// 每周执行,判断周几执行
					String executedate = task.get("vcexecutedate").toString();
					int week = Integer.valueOf(executedate);
					int nowweek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

					// 今天是执行天
					if (week == nowweek) {
						sqls = getSqls(task, false);
					}

				} else if ("month".equals(dateType)) {

					// 每月执行，判断每月几号执行
					String executedate = task.get("vcexecutedate").toString();
					int day = Integer.valueOf(executedate);
					int nowday = Calendar.getInstance().get(Calendar.DATE);

					// 今天是执行天
					if (day == nowday) {
						sqls = getSqls(task, true);
					}
				}
				
				// 如果配置不是天、周、月或者不是分发时间则跳过
				if(sqls == null){
					continue;
				}

				// 创建线程对象
				String insertLogSql = sqlMap.get("insertLog");
				DisTaskProcessorThread thread = new DisTaskProcessorThread(sqls, parpams.get("disdatasource"),
						getDataSource(), task, insertLogSql);
				executor.execute(thread);

			}

			// 启动线程池
			executor.shutdown();

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

		// 调度指定job
		super.execute();
	}

	/**
	 * 组织执行语句
	 * @param taskCfg 任务配置对象
	 * @param isMonth 是否月任务
	 * @return
	 */
	private String[] getSqls(Map<String, Object> taskCfg, boolean isMonth) {

		// 组织表名
		String tableName = "ext_edmp_dis_" + System.currentTimeMillis();
		taskCfg.put("tablename", tableName);

		// 预编译语句,判断有多少个字段
		Object precompilesql = taskCfg.get("vcprecompilesql");
		String vcsql = taskCfg.get("vcsql").toString();
		// 如果sql语句不为空，则表示是录入的sql则按录入的语句为准
		if (vcsql != null && !"".equals(vcsql)) {
			precompilesql = vcsql;
		}
		Integer intfields = Integer.valueOf(taskCfg.get("intfields").toString());
		StringBuffer sBuffer = new StringBuffer();
		for (int i = 0; i < intfields; i++) {
			sBuffer.append("a").append(i).append(" varchar,");
		}
		String fieldSql = sBuffer.substring(0, sBuffer.length() - 1);
		sBuffer.delete(0, sBuffer.length());

		// 组织文件名
		String fileName = "";
		String removeFile = "";
		int saveDate = Integer.valueOf(taskCfg.get("intsaveDate").toString());
		String vcfilename = taskCfg.get("vcfilename").toString();
		int intdelay = Integer.valueOf(taskCfg.get("intdelay").toString());
		if (isMonth) {
			fileName = vcfilename + "_${hiveconf:yyyyMM-" + intdelay + "}.csv";
			removeFile = vcfilename + "_${hiveconf:yyyyMM-" + (intdelay + saveDate) + "}.csv.gz";
		} else {
			fileName = vcfilename + "_${hiveconf:yyyyMMdd-" + intdelay + "}.csv";
			removeFile = vcfilename + "_${hiveconf:yyyyMMdd-" + (intdelay + saveDate) + "}.csv.gz";
		}
		taskCfg.put("filename", fileName);
		taskCfg.put("removefile", removeFile);

		String[] sqls = new String[8];

		// 1.删除外部表
		String droptable = "drop EXTERNAL table if EXISTS " + tableName;
		sqls[0] = droptable;

		// 2.创建外部表
		sqls[1] = "CREATE WRITABLE EXTERNAL TABLE " + tableName + "(" + fieldSql
				+ ") location ('gpfdist://192.168.36.120:8081/" + fileName + "') format 'csv' (delimiter '|' null '')";

		// 3.写入数据
		sqls[2] = "insert into " + tableName + " " + precompilesql;

		// 4.删除外部表
		sqls[3] = droptable;

		// 5.把导出文件移到指定目录
		String fileaddress = taskCfg.get("vcfileaddress").toString();
		String filePath = fileaddress + "/" + fileName;
		sqls[4] = "$linux$mv /data1/load/" + fileName + " " + filePath;

		// 6.压缩文件，减少使用空间
		sqls[5] = "$linux$gzip " + filePath;

		// 7.文件天文件只保留指定日期
		sqls[6] = "$linux$rm -f " + fileaddress + "/" + removeFile;
		
		// 8.修改文件权限，其他用户可读
		sqls[7] = "$linux$chmod o=r-- " + fileaddress + "/" + fileName;
		return sqls;
	}
}
