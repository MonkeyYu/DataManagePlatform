package com.etone.universe.dmp.problem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.universe.dmp.task.problem.ProblemTask;
import com.etone.universe.dmp.util.FileCharsetUtil;
import com.etone.universe.dmp.util.FindDisErrorDataUtil;
import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 数据分发入库处理类
 *
 * @author <a href="mailto:295789798@qq.com">Xumingjian</a>
 * @version $$Revision: 14169 $$
 * @date 2016-05-20 上午9:57:59
 */

public class ImportProcessor {

	public static final Logger logger = LoggerFactory.getLogger(ImportProcessor.class);

	public ImportProcessor(Connection conn, Map<String, String> sqlMap, ProblemTask task) {
		this.conn = conn;
		this.sqlMap = sqlMap;
		this.task = task;
	}

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
	 * 检查入库文件及入库数据
	 * @param file 入库文件
	 * @param configList 文件配置
	 * @throws Exception
	 */
	public void checkAndInsert(File file, List<Map<String, Object>> configList) throws Exception {

		QueryCriteria criteria = new QueryCriteria();
		// 文件名
		String name = file.getName();
		criteria.put("filename", name);
		boolean issucess = false;

		// 遍历配置文件列表找出对应配置文件并且进行入库操作
		for (int i = 0; i < configList.size(); i++) {

			Map<String, Object> map = configList.get(i);
			// 表缩写
			String csvname = map.get("csvname").toString();
			// 数据目录
			String fullpath = map.get("srcpath").toString() + name;

			if (name.toLowerCase().contains(csvname)) {

				// 文件名与表名配对成功
				issucess = true;
				task.addDetaileLog("文件《" + name + "》与表名《" + csvname + "》配对成功！");

				// 设置基础参数
				setParam(criteria, name, map, csvname);

				// 是否天月同名表
				// csvname = isDayFile(criteria, name, reptables, csvname);

				// 根据数据库配置分发数据
				checkDirtribute(map, name, fullpath);

				// 检查是否需要入库:isinsert==0 代表需要入库，isinsert==1代表不需要入库，不需要入库则进去下一个循环
				if (Integer.parseInt(map.get("isinsert").toString()) == 1) {
					// 记录日志
					excuteLogDataSql(criteria);
					continue;
				}

				try {

					// 第一步检查表数据是否完整 ，如果不完整直接记录日志，处理为数据不完整或者数据冗余
					checkData(criteria, name, map, csvname, fullpath);

				} catch (Exception e) {
					e.printStackTrace();
					task.addDetaileLog("入库出错：" + e.getMessage());
					logger.error("入库出错：", e);
					// 出错内部已经记录日志，这里直接执行下一个
					continue;
				}

				// 记录日志
				excuteLogDataSql(criteria);
			}
		}

		if (!issucess) {
			task.addDetaileLog("文件《" + name + "》配对表名不成功！");
		}
	}

	/**
	 * 是否天月同名表
	 * @param criteria
	 * @param name
	 * @param reptables
	 * @param csvname
	 * @return
	 */
	public String isDayFile(QueryCriteria criteria, String name, Map<String, String> reptables, String csvname) {

		if (reptables.get(csvname) != null) {
			// 若是天文件，则将表名变更为天表名
			String[] st = name.split("_");
			if (st[st.length - 1].indexOf(".") == 8) {
				String[] tstrs = reptables.get(csvname).split("\\|");
				csvname = tstrs[0];
				task.addDetaileLog("文件《" + name + "》是天文件，表名变更为《" + csvname + "》");
				String dayTablename = tstrs[1] + "_month";
				criteria.put("vcetablename", dayTablename);
				criteria.put("tablename", dayTablename);
				criteria.put("queryname", dayTablename);
			}
		}

		return csvname;
	}

	/**
	 * 第一步检查表数据是否完整 ，如果不完整直接记录日志，处理为数据不完整或者数据冗余
	 * @param criteria
	 * @param name
	 * @param map
	 * @param vcshorttablename
	 * @param fullpath
	 * @throws SQLException
	 * @throws Exception
	 */
	private void checkData(QueryCriteria criteria, String name, Map<String, Object> map, String vcshorttablename,
			String fullpath) throws SQLException, Exception {

		// 核查字段是否缺失或者多余
		int filedLen = checkData(fullpath);

		// 配置文件中表配置字段数
		int fields = Integer.parseInt(map.get("fields").toString());

		if (filedLen != -1 && filedLen > fields && filedLen != -2) {

			// 数据冗余判断
			String message = "数据冗余，多了" + (filedLen - fields) + " 个字段...";
			criteria.put("vcdescribe", message);
			task.addDetaileLog(message);
			criteria.put("databckpath", task.getErrorFilePath());
			String command = "mv  " + fullpath + " " + task.getErrorFilePath();
			excuteLiuxOrde3r(command);

		} else if (filedLen != -1 && filedLen < fields && filedLen != -2) {

			// 数据缺失判断
			String message = "该文件缺少字段，少了" + (fields - filedLen) + " 个字段...";
			criteria.put("vcdescribe", message);
			task.addDetaileLog(message);
			criteria.put("databckpath", task.getErrorFilePath());
			String command = "mv  " + fullpath + " " + task.getErrorFilePath();
			excuteLiuxOrde3r(command);

		} else if (filedLen == -2) {

			// 读取文件失败
			String message = "检查文件列数时，读取文件错误...";
			criteria.put("vcdescribe", message);
			task.addDetaileLog(message);

		} else if (filedLen == -1) {

			// 文件为空
			criteria.put("intflagsucc", 3);
			criteria.put("vcdescribe", "文件为空");
			task.addDetaileLog("文件为空...");
			criteria.put("databckpath", task.getErrorFilePath());

			// 20170228 空文件也要建表
			createNullTbale(criteria, name, vcshorttablename);

			String command = "mv  " + fullpath + " " + task.getErrorFilePath();
			excuteLiuxOrde3r(command);

		} else {

			// 核查文件是否重复入库
			int flgInsert = checkLog(name);

			// 把文件数据load进表
			loadData(criteria, name, map, vcshorttablename, fullpath, flgInsert);

		}
	}

	/**
	 * 20170228 空文件也要建表
	 * @param criteria
	 * @param name
	 * @param vcshorttablename
	 * @throws SQLException
	 */
	private void createNullTbale(QueryCriteria criteria, String name, String vcshorttablename) throws SQLException {

		// 组织参数
		String randomStr = "ext_mro1_" + (int) Math.random() * 10;
		String[] st = name.split("_");
		String vcYear = st[st.length - 1].substring(0, 4);
		String tablename = criteria.get("tablename").toString();

		// 调用建表方法
		processorTable(criteria, vcYear, vcshorttablename, tablename, randomStr);
	}

	/**
	 * 把文件数据load进表
	 * @param criteria
	 * @param name
	 * @param map
	 * @param vcshorttablename
	 * @param fullpath
	 * @param flgInsert
	 * @throws SQLException
	 * @throws Exception
	 */
	private void loadData(QueryCriteria criteria, String name, Map<String, Object> map, String vcshorttablename,
			String fullpath, int flgInsert) throws SQLException, Exception {

		// "/data1/load/" 路径在xml中配置
		String filePath = task.getFilePath();
		String iconv_fullpath = filePath + name;
		String iconv_fullpath_1 = filePath + name.split("\\.")[0] + "_1.csv";

		String[] st = name.split("_");
		String intpid = st[st.length - 1].substring(0, 6);
		String vcYear = st[st.length - 1].substring(0, 4);
		String tablename = criteria.get("tablename").toString();

		// 导入第二部需要把文件转码，转成数据编码utf-8,需要进入linux 服务器 (需要先把文件拷贝到/data1/load/目录)
		processorFile(name, fullpath, iconv_fullpath, iconv_fullpath_1, tablename);

		// 转码成功开始使用外部表方式入库,记录导入开始时间
		long startTime = System.currentTimeMillis();
		String randomStr = "ext_mro1_" + (int) Math.random() * 10;

		// 处理普通表
		String sc2 = processorTable(criteria, vcYear, vcshorttablename, tablename, randomStr);

		// 区分表是否需要关联yy_ltesource_cell表
		int isindoor = Integer.parseInt(map.get("isindoor").toString());

		// 处理外部表
		processorExtTable(criteria, name, tablename, isindoor, flgInsert, randomStr, sc2);

		try {

			// 数据load进数据表
			insertData(criteria, tablename, isindoor, randomStr, intpid);

			// 数据load进表后的需要处理文件及统计错误行
			dataLoadAfterThings(criteria, map, name, fullpath, startTime, tablename, intpid);

		} catch (Exception e) {
			e.printStackTrace();
			criteria.put("vcdescribe", "数据插入失败");
			task.addDetaileLog("数据插入失败:" + e.getMessage());
			// 标记失败
			criteria.put("intflagsucc", 1);
			// 记录日志
			excuteLogDataSql(criteria);
			throw new Exception("数据插入失败", e);
		} finally {
			// 无论成功失败都需要吧外部表删除
			String dsql = sqlMap.get("deleteTable") + " " + randomStr;
			criteria.put("sql", dsql);
			// 无论成功失败都需要删除load目录下文件
			String com = " rm -rf " + iconv_fullpath;
			excuteLiuxOrde3r(com);
			String com_1 = " rm -rf " + iconv_fullpath_1;
			excuteLiuxOrde3r(com_1);
		}
	}

	/**
	 * 核查数据是否需要分发：isdirtribute ==1 该数据不分发直接入库，  isdirtribute ==0 数据需要分发再入库
	 * @param map
	 * @param name
	 * @param fullpath
	 */
	private void checkDirtribute(Map<String, Object> map, String name, String fullpath) {

		if (Integer.parseInt(map.get("isdirtribute").toString()) == 0) {
			// 取出分发目录
			String distripath = map.get("distripath").toString();
			// 拆分为数据，判断需要分发到几个目录
			String arristripath[] = distripath.split(",");

			for (int a = 0; a < arristripath.length; a++) {

				if (Integer.parseInt(map.get("isinsert").toString()) == 1) {

					excuteLiuxOrde3r("cp " + fullpath + " " + map.get("databckpath").toString());
					excuteLiuxOrde3r("mv " + fullpath + " " + arristripath[a]);

				} else {

					excuteLiuxOrde3r("cp " + fullpath + " " + arristripath[a]);

				}

				excuteLiuxOrde3r("chmod 644 " + arristripath[a] + name);
			}
		}
	}

	/**
	 * 处理建表语句
	 * @param criteria
	 * @param vcYear
	 * @param vcshorttablename
	 * @param tablename
	 * @param randomStr
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	private String processorTable(QueryCriteria criteria, String vcYear, String vcshorttablename, String tablename,
			String randomStr) throws SQLException {

		String sc2 = "";
		// 获取表字段
		List<Map<String, Object>> fieldsList = task.selectMap(conn, sqlMap.get("getFields"),
				criteria.get("ytablename"));
		// 普通建表语句
		StringBuffer sqlCreateTab = new StringBuffer();
		// 建表公共部分
		StringBuffer sqlCloumn = new StringBuffer();
		// 外部表建表部分
		StringBuffer sqlOutTab = new StringBuffer();
		sqlCreateTab.append("create table " + tablename + " (");
		sqlOutTab.append("create external table " + randomStr + "(");

		StringBuffer fileCloumn = new StringBuffer();

		// 拼接外部表建表字段
		for (int l = 0; l < fieldsList.size(); l++) {
			Map linkedHashMap = fieldsList.get(l);
			String ecloumnStr = linkedHashMap.get("ecloumn").toString();
			sqlCloumn.append(ecloumnStr);
			sqlCloumn.append(" " + linkedHashMap.get("vccloumn").toString());
			sqlCloumn.append(" ,");

			// 组织插入语句字段
			fileCloumn.append(ecloumnStr).append(",");
		}

		String sc = sqlCloumn.substring(0, sqlCloumn.length() - 1);

		String fileCloumnStr = fileCloumn.substring(0, fileCloumn.length() - 1);
		criteria.put("fileCloumnStr", fileCloumnStr);
		criteria.put("fileCloumnStrSize", fieldsList.size());
		// 是否分表,1代表不分表，0代表分表，默认全部按月分表
		sc2 = sqlCloumn.substring(0, sqlCloumn.length() - 17);
		sqlOutTab.append(sc2);
		sqlCreateTab.append(sc);
		// 创建主物理表
		sqlCreateTab.append(")");

		// 是否分表,1代表不分表，0代表分表，默认全部按月分表
		criteria.put("sqlCreateTab", sqlCreateTab.toString());
		sqlCreateTab.append("with (appendonly=true,orientation=row,compresstype=zlib,compresslevel=5) ");
		sqlCreateTab.append("distributed randomly partition by list (intpid)(partition pp_197001 values(197001))");
		criteria.put("sql", sqlCreateTab);

		// 判断是否存在目标表
		int flagExists = task.selectMap(conn, sqlMap.get("flagExists"), "%" + criteria.get("queryname")).size();

		if (flagExists == 0) {

			// 创建表 ,第一次建表会把当年的分区表建立起来
			task.execute(conn, criteria.get("sql").toString());
			// 建立分区
			createPartTable(criteria, tablename, vcYear);

		} else if (flagExists != 0) {

			// 存在数据表,则先检查是否存在本年度分区,不存在则创建
			checkTablePar(criteria, tablename, vcYear);

		}

		return sc2;
	}

	/**
	 * 导入第二部需要把文件转码，转成数据编码utf-8,需要进入linux 服务器  (需要先把文件拷贝到/data1/load/目录)
	 * @param name
	 * @param fullpath
	 * @param iconv_fullpath
	 * @param iconv_fullpath_1
	 * @param tablename
	 */
	private void processorFile(String name, String fullpath, String iconv_fullpath, String iconv_fullpath_1,
			String tablename) {

		String command = "cp " + fullpath + " " + task.getFilePath();
		excuteLiuxOrde3r(command);

		// 告警的转码方式改成java转码,否则会出错,并且告警需要自己补日期字段
		transferCharset(name, iconv_fullpath, iconv_fullpath_1, tablename);

		// 删除转码之前的文件
		String deleteComm = "rm -rf " + iconv_fullpath;
		excuteLiuxOrde3r(deleteComm);
		// 改名转码之后的文件
		String renameComm = " mv " + iconv_fullpath_1 + " " + iconv_fullpath;
		excuteLiuxOrde3r(renameComm);
		// 每行数据添加文件名
		String[] cmds3 = { "/bin/sh", "-c", "sed -i 's/^/" + name + "|&/g' " + task.getFilePath() + name };
		excuteLiuxOrde3r(cmds3);

		String[] cmdss = { "/bin/sh", "-c", "sed -i 's/\"//g' " + iconv_fullpath };
		excuteLiuxOrde3r(cmdss);
	}

	/**
	 * 转换文件编码
	 * @param fileName
	 * @param iconv_fullpath
	 * @param iconv_fullpath_1
	 * @param tablename
	 */
	@SuppressWarnings("unchecked")
	private void transferCharset(String fileName, String iconv_fullpath, String iconv_fullpath_1, String tablename) {

		// 需要特殊处理的表
		Map<String, String> transferTabls = ProblemUtil.getIdNames(task.getNamesFileName(), "transfercharsettables");

		if (transferTabls.get(tablename) == null) {
			// 普通文件：把文件转码为 utf-8
			String iconvStr = "iconv -c -f GBK -t utf-8 " + iconv_fullpath + " -o " + " " + iconv_fullpath_1;
			excuteLiuxOrde3r(iconvStr);
			return;
		} else {
			// 需要使用java的转码方式否则会出现换行
			FileCharsetUtil.transfer2(iconv_fullpath, iconv_fullpath_1);
		}

		// 需要单独加年月日的数据
		Map<String, String> addDateTables = ProblemUtil.getIdNames(task.getNamesFileName(), "addDateTables");
		if (addDateTables.get(tablename) != null) {
			// 添加年，月，日
			String yyyymmdd = fileName.split("_")[3]; // 截取字符串: 20160116.csv
			String ymd = yyyymmdd.substring(0, 8); // 年月日：20160116
			String yyyy = yyyymmdd.substring(0, 4); // 年：2016
			String mm = yyyymmdd.substring(4, 6); // 月：01
			String dd = yyyymmdd.substring(6, 8); // 日：16
			// 2016|01|12|20160112|
			String changeStr = yyyy + "|" + mm + "|" + dd + "|" + ymd + "|";
			String[] cmds3 = { "/bin/sh", "-c", "sed -i 's/^/" + changeStr + "&/g' " + iconv_fullpath_1 };
			excuteLiuxOrde3r(cmds3);
		}
	}

	/**
	 * 数据load进数据表后，需要处理文件及统计错误数据
	 * @param criteria
	 * @param name
	 * @param fullpath
	 * @param startTime
	 * @param tablename
	 * @throws SQLException
	 */
	private void dataLoadAfterThings(QueryCriteria criteria, Map<String, Object> map, String name, String fullpath,
			long startTime, String tablename, String intpid) throws SQLException {

		// 备份目录
		String databckpath = map.get("databckpath").toString();
		// 表归属
		String vctabletype = map.get("vctabletype").toString();

		// 查询此次入库是否有异常
		List<Map<String, Object>> result = task.selectMap(conn, sqlMap.get("queryErrorList").toString(),
				"%" + name + "%");
		int errorList = 0;
		if (result != null) {
			errorList = Integer.valueOf(result.get(0).get("num").toString());
		}

		// 统计异常行数
		criteria.put("errorCount", errorList);
		String message = "";
		if (errorList > 0) {
			message = "数据已经入库,但是有部分数据错误...";
			criteria.put("vcdescribe", message);
			// 标记异常
			criteria.put("intflagsucc", 2);

			// 20170517增加错误数据补录功能，目前只能补录多分隔符的数据
			errDataPro(criteria, tablename, intpid);
		} else {
			message = "数据已经入库...";
			criteria.put("vcdescribe", message);
			// 标记成功
			criteria.put("intflagsucc", 0);
		}

		task.addDetaileLog(message);

		// 记录导入结束时间
		long endTime = System.currentTimeMillis();
		long takkingtime = (endTime - startTime) / 1000;
		criteria.put("takkingtime", takkingtime);

		// 入库没有错误，到这里把/data1/ftp_data/下面的源文件转移到备份目录
		String mvcommand = "mv " + fullpath + " " + databckpath;
		excuteLiuxOrde3r(mvcommand);

		// 压缩文件
		String gzipcommand = "gzip " + databckpath + name;
		excuteLiuxOrde3r(gzipcommand);

		if (vctabletype.equals("告警")) {
			String[] st = name.split("_");
			String yyyymmdd = st[st.length - 1].substring(0, 8);
			// 实行处理存储过程
			String execuSqlFunction = "select usp_gaojing_u_ltesource_day20170426('" + yyyymmdd + "','" + tablename
					+ "','" + name + "')";
			// 处理告警数据
			criteria.put("sql", execuSqlFunction);
			task.selectMap(conn, execuSqlFunction);
		}

	}

	/**
	 * 20170517增加错误数据补录功能，目前只能补录多分隔符的数据
	 * @param criteria
	 * @param tablename
	 * @param intpid
	 */
	private void errDataPro(QueryCriteria criteria, String tablename, String intpid) {
		String filename = criteria.get("filename").toString();
		int size = Integer.valueOf(criteria.get("fileCloumnStrSize").toString());
		String fileCloumnStr = criteria.get("fileCloumnStr").toString();
		FindDisErrorDataUtil insertThred = new FindDisErrorDataUtil(filename, tablename, fileCloumnStr, size - 1,
				intpid);
		new Thread(insertThred).start();
	}

	/**
	 * 数据load进数据表
	 * @param criteria
	 * @param tablename
	 * @param isindoor
	 * @param randomStr
	 * @param intpid
	 * @throws SQLException
	 */
	private void insertData(QueryCriteria criteria, String tablename, int isindoor, String randomStr, String intpid)
			throws SQLException {
		String lsql = "";
		String fileCloumnStr = criteria.get("fileCloumnStr").toString();
		if (isindoor == 1) { // 不需要关联ltesource资源表
			lsql = " insert into  " + tablename + "(" + fileCloumnStr + ") select t1.*, " + intpid + " from "
					+ randomStr + " t1";
		} else if (isindoor == 0) { // 需要关联ltesource资源表
			lsql = " insert into  " + tablename + "(" + fileCloumnStr
					+ ") select case when t2.cover_type is not null or t2.cover_type ='' then t2.cover_type  else '室外'  end , t1.*,"
					+ intpid + " from " + randomStr
					+ " t1 LEFT JOIN (select t1.vccgi,cover_type from yy_ltesource_cell_month t1,(select MAX(scan_start_time) scan_start_time,vccgi  from yy_ltesource_cell_month t3 group by vccgi) t2 where t1.scan_start_time=t2.scan_start_time and t1.vccgi=t2.vccgi) t2  on t1.vccgi = t2.vccgi  order by t1.vccgi";
		} else if (isindoor == 2) { // 不需要关联ltesource资源表 ,直接把coverType这只为室外
			lsql = " insert into  " + tablename + "(" + fileCloumnStr + ") select '室外',t1.*," + intpid + " from "
					+ randomStr + " t1";
		}

		criteria.put("insertDataSql", lsql);

		try {

			// 20170306 增加更新表入库配置
			deleteData(criteria, tablename, randomStr);

		} catch (Exception e) {
			e.printStackTrace();
			conn.rollback();
			task.addDetaileLog("load数据出错,数据已经回滚：" + e.getMessage());
			throw new SQLException(e);
		} finally {
			conn.setAutoCommit(true);
		}

		try {

			// 从临时表把数据插入到物理表中
			task.execute(conn, lsql);

			// 20170328 增加敏感字段配置
			sensitivityFieldPro(tablename);

		} catch (SQLException e) {
			e.printStackTrace();
			task.addDetaileLog("load数据出错,数据已经回滚：" + e.getMessage());
			throw new SQLException(e);
		}

	}

	/**
	 * 20170328 增加敏感字段配置
	 * @param tablename
	 * @throws SQLException 
	 */
	@SuppressWarnings("unchecked")
	private void sensitivityFieldPro(String tablename) throws SQLException {

		// 获取敏感信息表
		Map<String, String> sensitivityTables = ProblemUtil.getIdNames(task.getNamesFileName(),
				"sensitivitytablenames");
		String fieldNames = sensitivityTables.get(tablename);
		if (fieldNames != null) {

			// 组织更新敏感字段信息语句,将敏感字段更新为空
			StringBuilder sql = new StringBuilder();
			sql.append("update ").append(tablename).append(" set ");
			String[] fields = fieldNames.split(",");
			for (String field : fields) {
				sql.append(field).append("=null,");
			}

			String sqlStr = sql.substring(0, sql.length() - 1);

			// 执行更新语句
			task.execute(conn, sqlStr);
		}

	}

	/**
	 * 20170306增加更新表配置，则需要先删除原来的信息再插入新的信息，多个字段唯一时请用','隔开
	 * @param criteria
	 * @param tablename
	 * @param randomStr
	 * @throws SQLException
	 */
	@SuppressWarnings({ "unchecked" })
	private void deleteData(QueryCriteria criteria, String tablename, String randomStr) throws SQLException {

		// 防止错删,将配置移至edmp用xml配置,只处理指定表
		Map<String, String> updateTables = ProblemUtil.getIdNames(task.getNamesFileName(), "tablenames");

		if (updateTables.get(tablename) != null) {

			// 设置唯一校验字段可以多个用','隔开
			String vcsolefields = updateTables.get(tablename);

			// 获取备份表表名
			String bakTablename = tablename + "_all";

			// 检查备份表是否需要重建
			String[] st = criteria.get("filename").toString().split("_");
			String vcYear = st[st.length - 1].substring(0, 4);
			criteria.put("queryname", bakTablename);

			// 调用建表方法
			processorTable(criteria, vcYear, criteria.get("vcshorttablename").toString(), bakTablename, randomStr);

			String[] fileds = vcsolefields.split(",");

			// 控制事务
			conn.setAutoCommit(false);

			// 组织删除sql语句
			StringBuilder sbBuilder = new StringBuilder();
			sbBuilder.append("delete from ").append(bakTablename).append(" t where exists(select 1 from ")
					.append(randomStr).append(" t1 where 1=1 ");
			for (String field : fileds) {
				sbBuilder.append(" and t.").append(field).append("= t1.").append(field);
			}
			sbBuilder.append(")");

			// 执行去重语句,若没有配置去重字段（值为0）,则不需要去重
			if (!"0".equals(vcsolefields)) {
				task.execute(conn, sbBuilder.toString());
			}

			// 执行插入备份表语句
			String sql = criteria.get("insertDataSql").toString();
			String baklsql = sql.replace(tablename, bakTablename);
			task.execute(conn, baklsql);

			// 删除最新表的所有数据
			task.execute(conn, "delete from " + tablename);

			// 提交事务
			conn.commit();

		}
	}

	/**
	 * 检查文件是否入库过
	 * @param name
	 * @return
	 * @throws SQLException 
	 */
	public int checkLog(String name) throws SQLException {
		int flgInsert = 0;
		// 检查改文件是否已经入库过
		List<Map<String, Object>> result = task.selectMap(conn, sqlMap.get("checkFlgInsert"), "%" + name + "%");

		if (result != null) {
			flgInsert = Integer.valueOf(result.get(0).get("num").toString());
		}

		if (flgInsert > 0) {
			// 删除日志表中已经存在的记录
			task.execute(conn, sqlMap.get("deleteFromLog"), "%" + name + "%");
			// 删除错误日志表已经存在的记录
			task.execute(conn, sqlMap.get("deleteFromErr"), name);
		}
		return flgInsert;
	}

	/**
	 * 设置基础参数
	 * @param criteria
	 * @param name
	 * @param map
	 * @param vcshorttablename
	 */
	@SuppressWarnings("rawtypes")
	private void setParam(QueryCriteria criteria, String name, Map map, String vcshorttablename) {

		// 是否分表,1代表不分表，0代表分表，默认全部按月分表
		String split = map.get("split").toString();
		// 数据来源
		String vcfactory = map.get("factory").toString();
		// 表维度
		String vcweidu = map.get("timegranularity").toString();
		// 中文表名
		String vcctablename = map.get("vcdesribe").toString();
		// 数据目录
		String srcpath = map.get("srcpath").toString();
		// 备份目录
		String databckpath = map.get("databckpath").toString();
		// 配置文件中表配置字段数
		int fields = Integer.parseInt(map.get("fields").toString());
		// 需要创建的表，不管是什么表统一按月分表，以数据中的数据后缀为准
		String tablename = map.get("tablename").toString() + "_month";

		criteria.put("vcfactory", vcfactory);
		criteria.put("srcpath", srcpath);
		criteria.put("filename", name);
		criteria.put("vcshorttablename", vcshorttablename);
		criteria.put("vcctablename", vcctablename);
		criteria.put("vcweidu", vcweidu);
		criteria.put("intflagsprit", split);
		criteria.put("intfieldcount", fields);
		criteria.put("vcetablename", tablename);
		criteria.put("tablename", tablename);
		criteria.put("queryname", tablename);
		criteria.put("databckpath", databckpath);
		criteria.put("csvname", vcshorttablename);
		criteria.put("intflagsucc", 1);
		criteria.put("takkingtime", 10.01);
		criteria.put("countHangShu", 0);
		criteria.put("errorCount", 0);
		criteria.put("ytablename", map.get("tablename").toString());

		// 匹配更新唯一字段(用于识别更新时所用的)

	}

	/**
	 * 处理外部表
	 * @param criteria
	 * @param name
	 * @param tablename
	 * @param isindoor
	 * @param flgInsert
	 * @param randomStr
	 * @param sc2
	 * @throws SQLException
	 */
	private void processorExtTable(QueryCriteria criteria, String name, String tablename, int isindoor, int flgInsert,
			String randomStr, String sc2) throws SQLException {

		// 先删除外部表
		String deleteTable = sqlMap.get("deleteTable") + " " + randomStr;
		criteria.put("sql", deleteTable);
		task.execute(conn, deleteTable);

		// 创建外部表
		String extFilePath = task.getExternalPath() + name;
		String olumns = sc2;
		if (isindoor == 0 || isindoor == 2) {
			olumns = sc2.replace("coverType varchar(150) ,", "");
		}
		String createExtTableSql = sqlMap.get("createExtTable").replace("${tableName}", randomStr)
				.replace("${columns}", olumns).replace("${filePath}", extFilePath);

		criteria.put("sql", createExtTableSql);
		task.execute(conn, createExtTableSql);
		if (flgInsert > 0) {
			String del = "delete from " + tablename + " where filemaik='" + name + "'";
			criteria.put("sql", del);
			task.execute(conn, del);
		}

		// 获取文件总共有多少行记录
		criteria.put("randomStr", randomStr);
		List<Map<String, Object>> resultShu = task.selectMap(conn,
				sqlMap.get("getCountHangShu").toString() + " " + criteria.get("randomStr"));

		int countHangShu = 0;
		if (resultShu != null) {
			countHangShu = Integer.valueOf(resultShu.get(0).get("num").toString());
		}
		criteria.put("countHangShu", countHangShu > 0 ? countHangShu : 0);
	}

	/**
	 * 存在数据表,则先检查是否存在本年度分区,不存在则创建
	 * @param criteria
	 * @param tablename
	 * @param vcYear
	 * @throws SQLException
	 */
	private void checkTablePar(QueryCriteria criteria, String tablename, String vcYear) throws SQLException {
		// 检查这个是否有这一年的的分区表
		criteria.put("parTableName", "_1_prt_pp_" + vcYear);

		List<Map<String, Object>> resultCount = task.selectMap(conn, sqlMap.get("getGetPartTabCount"),
				"%" + criteria.get("queryname") + "%", "%" + criteria.get("parTableName") + "%");

		int exitCount = 0;
		if (resultCount != null) {
			exitCount = Integer.valueOf(resultCount.get(0).get("num").toString());
		}

		if (exitCount == 0) {
			createPartTable(criteria, tablename, vcYear);
		}
	};

	// 执行linux命令方法
	private boolean excuteLiuxOrde3r(String command) {
		task.addDetaileLog("执行命令：" + command);
		boolean returnFlag = false;
		StringBuilder sb = new StringBuilder();
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
			returnFlag = true;
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String lo;
			while ((lo = bufferedReader.readLine()) != null) {
				sb.append(lo);
				sb.append("\n");
			}
			process.waitFor();
			// 等待n秒后destory进程
		} catch (Exception e) {
			task.addDetaileLog("执行命令[" + command + "]出错:" + e.getMessage());
		}
		process.destroy();
		return returnFlag;
	}

	public int checkData(String filePath) {
		InputStreamReader inputReader = null;
		BufferedReader bufferReader = null;
		int strLength = 0;
		try {
			File file = new File(filePath);
			InputStream inputStream = new FileInputStream(file);
			inputReader = new InputStreamReader(inputStream, "gbk");
			bufferReader = new BufferedReader(inputReader);
			String line = null;
			String str = new String();
			while ((line = bufferReader.readLine()) != null) {
				if (str.length() > 0) {
					break;
				} else {
					str = line;
				}
			}
			if (str.trim().length() == 0) {
				task.addDetaileLog("文件为空");
				return -1;
			}

			int count = 0;
			char ch = "|".charAt(0);
			for (int i = 0; i < str.length(); i++) {
				if (ch == (str.charAt(i)))
					count++;
			}
			strLength = count + 1;

		} catch (Exception fe) {
			task.addDetaileLog("检查文件列数时,读取文件错误！");
			return -2;
		} finally {
			if (bufferReader != null) {
				try {
					bufferReader.close();
				} catch (Exception e) {

				}
			}
			if (inputReader != null) {
				try {
					bufferReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return strLength;
	}

	// 执行linux命令方法
	private boolean excuteLiuxOrde3r(String command[]) {
		task.addDetaileLog("执行命令[]：" + command[2]);
		boolean returnFlag = false;
		StringBuilder sb = new StringBuilder();
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
			returnFlag = true;
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String lo;
			while ((lo = bufferedReader.readLine()) != null) {
				sb.append(lo);
				sb.append("\n");
			}
			bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			lo = null;
			while ((lo = bufferedReader.readLine()) != null) {
				task.addDetaileLog("ERROR:" + lo);
			}
			process.getInputStream().close();
			process.getErrorStream().close();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			task.addDetaileLog("执行命令[" + command + "]出错:" + e.getMessage());
		}
		process.destroy();
		return returnFlag;
	}

	/**
	 * 创建今年的表分区
	 * @param criteria
	 * @param tablename
	 * @param vcYear
	 * @throws SQLException
	 */
	private void createPartTable(QueryCriteria criteria, String tablename, String vcYear) throws SQLException {
		String months[] = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
		for (int r = 0; r < months.length; r++) {
			String fenQuSql = " alter table " + tablename + " add partition pp_" + vcYear + months[r] + " values ("
					+ vcYear + months[r] + ")";
			task.execute(conn, fenQuSql);
		}
	}

	/**
	 * 记录日志
	 * @param criteria
	 * @throws SQLException
	 */
	private void excuteLogDataSql(QueryCriteria criteria) throws SQLException {
		task.execute(conn, sqlMap.get(ProblemUtil.LOGDATA_STR), criteria.get("vcfactory"), criteria.get("filename"),
				criteria.get("vcshorttablename"), criteria.get("vcctablename"), criteria.get("vcweidu"),
				criteria.get("vcetablename"), Integer.valueOf(criteria.get("intflagsprit").toString()),
				criteria.get("intfieldcount"), criteria.get("intflagsucc"), criteria.get("vcdescribe"),
				criteria.get("takkingtime"), criteria.get("databckpath"), criteria.get("countHangShu"),
				criteria.get("errorCount"));
	}

}
