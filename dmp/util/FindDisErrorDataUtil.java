package com.etone.universe.dmp.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.etone.daemon.db.DB;
import com.etone.daemon.db.helper.QueryHelper;

/**
 * 补入多分隔符的问题数据
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年5月17日  下午7:40:23
 */
public class FindDisErrorDataUtil implements Runnable {

	public FindDisErrorDataUtil(String filename, String tablename, String fileCloumnStr, int size, String intpid) {
		this.filename = filename;
		this.tablename = tablename;
		this.size = size;
		this.fileCloumnStr = fileCloumnStr;
		this.intpid = intpid;
	}

	/**
	 * 文件名
	 */
	private String filename;

	/**
	 * intpid
	 */
	private String intpid;

	/**
	 * 表名
	 */
	private String tablename;

	/**
	 * 插入字段字符
	 */
	private String fileCloumnStr;

	/**
	 * 字段个数
	 */
	private int size;

	@Override
	public void run() {

		// 查询需要处理的数据
		String querySql = "select linenum,rawdata from (select *,rank() OVER (PARTITION BY linenum ORDER BY cmdtime)n from err_sales_ext_t where filename like '%"
				+ filename + "%' and errmsg='extra data after last expected column')t where n=1 order by linenum desc";

		Connection conn = null;
		try {

			// 获取数据库链接
			conn = DB.getDataSource("gp").getConnection();

			// 查询数据
			List<Map<String, Object>> datas = QueryHelper.selectMap(conn, querySql);

			// 处理数据
			Map<String, String> sqlMap = insertData(conn, tablename, fileCloumnStr, size, intpid, datas);

			conn.setAutoCommit(false);

			// 插入数据
			for (String key : sqlMap.keySet()) {
				System.out.println("执行语句：" + sqlMap.get(key));
				// 插入数据的同时,插入一条日志表记录到日志表
				QueryHelper.execute(conn, sqlMap.get(key));
				QueryHelper.execute(conn,
						"insert into f_et_err_dis_data_log(vcfilename,vctablename,vclinenum,vcsql) values(?,?,?,?)",
						filename, tablename, key, sqlMap.get(key));
			}
			
			// 更新文件处理数量
			QueryHelper.execute(conn, "update f_et_distribution_log set intprocount=? where vcfilename=?",
					sqlMap.size(), filename);
			conn.commit();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}

	}

	/**
	 * @param tableName 表名
	 * @param fileCloumnStr 字段语句
	 * @param size 字段个数
	 * @param intpid 分区
	 * @param datas 数据
	 */
	public static Map<String, String> insertData(Connection conn, String tableName, String fileCloumnStr, int size,
			String intpid, List<Map<String, Object>> datas) {

		// 先组织好insert语句前缀
		StringBuffer sBufferq = new StringBuffer();
		sBufferq.append("insert into ").append(tableName).append("(").append(fileCloumnStr).append(") values(");

		// 组织插入内容
		StringBuffer sBuffer = new StringBuffer();
		StringBuffer sBufferEnd = new StringBuffer();

		// 存放插入语句集合
		Map<String, String> sqlmap = new HashMap<String, String>();

		for (Map<String, Object> dataMap : datas) {
			sBuffer.append(sBufferq.toString());
			String data = dataMap.get("rawdata").toString();
			String[] split = data.split("\\|");

			for (int i = 0; i < split.length; i++) {
				if ((i + 1) < size) {
					sBuffer.append("'").append(split[i]).append("',");
				} else {
					sBufferEnd.append(split[i]).append("|");
				}
			}

			sBuffer.append("'").append(sBufferEnd.substring(0, sBufferEnd.length() - 1)).append("'," + intpid + ")");

			// 加入结果集
			sqlmap.put(dataMap.get("linenum").toString(), sBuffer.toString());

			// 清空字符对象
			sBuffer.delete(0, sBuffer.length());
			sBufferEnd.delete(0, sBufferEnd.length());
		}

		return sqlmap;

	}

}
