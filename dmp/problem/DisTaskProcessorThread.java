package com.etone.universe.dmp.problem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.daemon.db.DB;
import com.etone.daemon.db.helper.QueryHelper;
import com.etone.universe.dmp.task.Variables;
import com.etone.universe.dmp.util.ProblemUtil;

public class DisTaskProcessorThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(DisTaskProcessorThread.class);

	public DisTaskProcessorThread(String[] sqls, String disdatasource, String datasource, Map<String, Object> taskObj,
			String logSql) throws SQLException {
		this.sqls = sqls;
		this.conn = DB.getDataSource(datasource).getConnection();
		this.disdatasource = disdatasource;
		this.taskObj = taskObj;
		this.logSql = logSql;
	}

	// 日志sql
	private String logSql;

	// 保存日志的数据库
	private String disdatasource;

	// 语句集合
	private String[] sqls;

	// 数据库连接
	private Connection conn;

	// 任务配置信息
	private Map<String, Object> taskObj;

	@Override
	public void run() {

		try {
			for (String sql : sqls) {
				sql = Variables.parse(sql, new Date());
				logger.info(sql);

				// 若出现$linux$特殊字符，则是linux命令
				if (sql.contains("$linux$")) {
					sql = sql.replace("$linux$", "");
					ProblemUtil.excuteLiuxOrde3r(sql);
					continue;
				}

				if (sql != null && sql.trim().toLowerCase().startsWith("select")) {
					// 针对GreenPlum的存储过程调用方式进行特殊处理
					QueryHelper.selectMap(conn, sql);
				} else {
					QueryHelper.execute(conn, sql);
				}
			}

			// 执行成功后保存日志
			save("数据分发成功...", 1);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("执行命令出错：", e);

			// 记录日志
			save("数据分发失败：" + e.getMessage(), 0);

		} finally {

			// 不管成功與否,都要关闭数据库连接
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error("关闭数据库连接失败:", e);
				}
			}
		}

	}

	/**
	 * 保存日志信息
	 * @param string
	 * @param taskStatus
	 */
	private void save(String message, int taskStatus) {

		// 执行语句
		String vcsql = "";
		for (String sql : sqls) {
			vcsql += sql;
		}
		Connection logConn = null;
		try {
			String removefile = Variables.parse(taskObj.get("removefile").toString(), new Date());
			String filename = Variables.parse(taskObj.get("filename").toString(), new Date());
			logConn = DB.getDataSource(disdatasource).getConnection();
			QueryHelper.execute(logConn, logSql, taskObj.get("vcid"), taskStatus, message, filename, removefile,
					taskObj.get("tablename"), vcsql);
		} catch (SQLException e) {
			// 保存日志出错
			e.printStackTrace();
			logger.error("保存日志信息出错：" + e.getMessage());
		} finally {
			if (logConn != null) {
				try {
					logConn.close();
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error("关闭数据库连接失败:", e);
				}
			}
		}

	}

}
