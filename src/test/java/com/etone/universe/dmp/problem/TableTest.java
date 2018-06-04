package com.etone.universe.dmp.problem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年5月18日  上午9:31:25
 */
public class TableTest {

	public static final String url = "jdbc:mysql://127.0.0.1:3306/ltemr_gd";
	public static final String name = "com.mysql.jdbc.Driver";
	public static final String user = "root";
	public static final String password = "root";

	public static void main(String[] args) throws SQLException {

		// 获取链接
		Connection conn = null;
		try {
			// 获取链接
			conn = getConn();

			// 查询数据
			PreparedStatement pst = conn
					.prepareStatement("select t.vcid,t1.vctablename tablename,t1.vcfieldname tablefield,t2.vcfieldname fieldname from f_et_distribute_task t,f_et_distribute_task_table t1,f_et_distribute_task_field t2 where t.vcid=t1.vctaskid and t1.vcid=t2.vctableid and t.vcid='1' order by t2.intordernum");
			ResultSet executeQuery = pst.executeQuery();

			// 处理表字段信息
			String querySql = proSql(executeQuery);

			// 打印语句
			System.out.println(querySql);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			// 关闭链接
			if (conn != null) {
				conn.close();
			}
		}

	}

	/**
	 * 处理表字段信息
	 * @param executeQuery 结果集
	 * @return 返回sql语句
	 * @throws SQLException
	 */
	private static String proSql(ResultSet executeQuery) throws SQLException {

		// 组织sql语句
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("select ");

		// 记录当前表数量
		int countTable = 0;

		// 表别名
		Map<String, String> tableAliasMap = new HashMap<String, String>();

		// 表关联字段
		Map<String, String> tableMap = new HashMap<String, String>();

		while (executeQuery.next()) {

			// 取出表信息
			String tablename = executeQuery.getString("tablename");
			String tablefield = executeQuery.getString("tablefield");

			// 记录表的别名跟表关联字段
			if (tableMap.get(tablename) == null) {
				tableMap.put(tablename, tablefield);
				countTable++;
				tableAliasMap.put(tablename, "t" + countTable);
			}

			// 取出字段信息
			String fieldname = executeQuery.getString("fieldname");
			sBuilder.append(tableAliasMap.get(tablename)).append(".")
					.append(fieldname).append(",");
		}

		String sqlQ = sBuilder.substring(0, sBuilder.length() - 1) + " from ";

		// 组织表语句
		sBuilder.delete(0, sBuilder.length());
		StringBuilder fieldBuilder = new StringBuilder();
		countTable = 1;
		for (String tablename : tableMap.keySet()) {

			// 处理表信息
			String aliasName = tableAliasMap.get(tablename);
			sBuilder.append(tablename).append(" ").append(aliasName)
					.append(",");

			// 处理表字段关联信息
			String filed = tableMap.get(tablename);
			if (countTable++ % 2 != 0) {
				fieldBuilder.append(aliasName).append(".").append(filed);
				if (countTable <= tableMap.size()) {
					fieldBuilder.append("=");
				}
			} else {
				fieldBuilder.append(aliasName).append(".").append(filed);
				if (countTable <= tableMap.size()) {
					fieldBuilder.append(" and ").append(aliasName).append(".")
							.append(filed).append("=");
				}
			}
		}

		String tableStr = sBuilder.substring(0, sBuilder.length() - 1)
				+ " where ";
		// 组织最后的结果语句
		String querySql = sqlQ + tableStr + fieldBuilder.toString();
		return querySql;
	}

	/**
	 * 获取数据库链接
	 * @return
	 */
	public static Connection getConn() {

		try {
			Class.forName(name);//指定连接类型  
			return DriverManager.getConnection(url, user, password);//获取连接  
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
