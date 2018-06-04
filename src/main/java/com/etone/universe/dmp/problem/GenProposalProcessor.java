package com.etone.universe.dmp.problem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.universe.dmp.task.problem.ProblemTask;

/**
 * 派单前数据同步
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年12月28日  下午4:37:16
 */
public class GenProposalProcessor {

	public GenProposalProcessor(Connection conn, Map<String, String> sqlMap,
			ProblemTask task) {
		this.conn = conn;
		this.sqlMap = sqlMap;
		this.task = task;
	}

	private static final Logger logger = LoggerFactory
			.getLogger(GenProposalProcessor.class);

	/**
	 * 获取数据库链接
	 */
	protected Connection conn = null;

	/**
	 * sql语句集合
	 */
	protected Map<String, String> sqlMap = null;

	/**
	 * 任务对象
	 */
	protected ProblemTask task = null;

	/**
	 * 派单前同步数据
	 * @return
	 */
	public boolean genProposal() {

		task.addDetaileLog("开始进行派单前数据同步...");

		QueryCriteria criteria = null;

		try {

			criteria = new QueryCriteria();
			criteria.put("tableName", "f_et_plb_vcpro_vcjl_maxoptid");
			long numbers = task.selectOne(conn, long.class,
					sqlMap.get("existTable"), criteria.get("tableName"));
			if (numbers == 0) {
				// 最大opt值的表
				task.execute(conn, sqlMap.get("createMaxoptid"));
			}
			criteria.put("tableName", "f_et_plb_vcpro_vcjl");
			numbers = task.selectOne(conn, long.class,
					sqlMap.get("existTable"), criteria.get("tableName"));
			if (numbers == 0) {
				// 根据opt最大的值对应的ID取里面的值
				task.execute(conn, sqlMap.get("createMaxProJl"));
			}

			//信念问题点表备份,yt_gtrfg_cell_month
			criteria.put("tableName", "xl_gtrfg_cell_month");
			//派单标识符
			criteria.put("xl_state", "xl_state");
			criteria.put("order_code", "order_code");
			criteria.put("tableName2", "yt_gtrfg_cell_month");
			bakDataTable(criteria);

			//信念问题点表备份,yt_lyrfg_cell_month
			criteria.put("tableName", "xl_lyrfg_cell_month");
			criteria.put("tableName2", "yt_lyrfg_cell_month");
			bakDataTable(criteria);

			//信念问题点表备份,yt_xldlgr_grid_month
			criteria.put("tableName", "xl_xldlgr_grid_month");
			criteria.put("tableName2", "yt_xldlgr_grid_month");
			bakDataTable(criteria);

			//信念问题点表备份,yt_xldlfg_grid_month
			criteria.put("tableName", "xl_xldlfg_grid_month");
			criteria.put("tableName2", "yt_xldlfg_grid_month");
			bakDataTable(criteria);

			//信念问题点表备份,yt_gtgr_cell_month
			criteria.put("tableName", "xl_gtgr_cell_month");
			criteria.put("tableName2", "yt_gtgr_cell_month");
			bakDataTable(criteria);

			//drop问题点临时表
			criteria.put("tableName", "f_et_plb_vcpro_optimization");
			String deleteTable = sqlMap.get("delteTable").replace(
					"${tableName}", criteria.get("tableName").toString());
			task.execute(conn, deleteTable);
			//创建问题点临时表
			task.execute(conn, sqlMap.get("createUniqueprotable"));
			//插入数据
			task.execute(conn, sqlMap.get("saveUniqueprotable"));

			task.execute(conn, sqlMap.get("deleteMaxoptid"));
			task.execute(conn, sqlMap.get("deleteMaxProJl"));
			task.execute(conn, sqlMap.get("saveMaxoptid"));
			task.execute(conn, sqlMap.get("saveMaxProJl"));
			//更新order_temp表状态，标识是否已派单过
			task.execute(conn, sqlMap.get("updatePlbOrdertemp"));

			saveXLQuestion(criteria); //同步信令数据到表中

			task.execute(conn, sqlMap.get("updatePlbOrdertemp"));//更新order_temp表状态
			
			// 每周更新一次聚类问题点数量
			task.execute(conn, sqlMap.get("updateProblemCount"));
		} catch (Exception e) {
			logger.error("同步数据失败：", e);
			task.addDetaileLog("同步数据失败：" + e.getMessage());
			return false;
		}
		task.addDetaileLog("同步数据成功...");
		
		return true;
	}

	/**
	 * 数据表备份
	 * @param criteria
	 * @throws SQLException
	 */
	private void bakDataTable(QueryCriteria criteria) throws SQLException {
		long numbers = task.selectOne(conn, long.class,
				sqlMap.get("existTable"), criteria.get("tableName"));
		if (numbers == 0) {
			String createXLTempTable = sqlMap
					.get("createXLTempTable")
					.replace("${tableName}",
							criteria.get("tableName").toString())
					.replace("${tableName2}",
							criteria.get("tableName2").toString());
			task.execute(conn, createXLTempTable);
			//添加标识字段
			String addXLstate = sqlMap
					.get("addXLstate")
					.replace("${tableName}",
							criteria.get("tableName").toString())
					.replace("${xl_state}", criteria.get("xl_state").toString());
			task.execute(conn, addXLstate);
			//添加工单号
			String addXLorder = sqlMap
					.get("addXLorder")
					.replace("${tableName}",
							criteria.get("tableName").toString())
					.replace("${order_code}",
							criteria.get("order_code").toString());
			task.execute(conn, addXLorder);
		}
	}

	//同步信令问题点
	public void saveXLQuestion(QueryCriteria criteria) throws SQLException {

		task.execute(conn, sqlMap.get("savegtrfgCode"));

		task.execute(conn, sqlMap.get("savelyrfgCode"));

		task.execute(conn, sqlMap.get("savexldlgrCode"));

		task.execute(conn, sqlMap.get("savexldlfgCode"));

		task.execute(conn, sqlMap.get("savegtgrCode"));

	}
}
