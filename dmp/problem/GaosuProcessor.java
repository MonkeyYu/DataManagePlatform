package com.etone.universe.dmp.problem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.universe.dmp.task.problem.ProblemTask;
import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 高速问题点处理类
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年2月28日  下午3:22:40
 */
public class GaosuProcessor {

	public GaosuProcessor(Connection conn, Map<String, String> sqlMap,
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
	 * 核查高速跟踪表数据,成功则把数据写入1+N跟踪表,为了实现界面高速问题点呈现
	 * @param path 工单文件路径
	 * @param orderCode 工单编号
	 * @throws Exception
	 */
	public File checkGaosuData(String path, String orderCode, String tableName)
			throws Exception {

		logger.info("开始核查高速跟踪表数据...");

		// 先根据工单编号删除跟踪表及汇总表的数据
		task.execute(conn, sqlMap.get("removeAllQuestionByCode"), orderCode);
		task.execute(conn, sqlMap.get("removeCountCode"), orderCode);

		// 文件流
		FileOutputStream fw = null;
		BufferedWriter bw = null;
		InputStream is = null;

		// 外部表数据文件
		String txtName = ProblemUtil.EXT_EDMP_TABLENAME
				+ System.currentTimeMillis() + ProblemUtil.FILE_TXT;
		String extFilePath = task.getParpams().get("loadpath") + txtName;
		File txtFile = new File(extFilePath);
		try {

			// 文件路径
			File jobFile = new File(path);

			// 创建work对象
			is = new FileInputStream(jobFile);
			Workbook hssfWorkbook = new XSSFWorkbook(is);

			// 写入外部表数据文件
			fw = new FileOutputStream(txtFile);
			bw = new BufferedWriter(new OutputStreamWriter(fw, "UTF-8"));

			// 将汇聚表数据写入到外部表文件
			writeExtTxt(bw, hssfWorkbook, "LTE高速问题点汇聚", "LTE");
			writeExtTxt(bw, hssfWorkbook, "GSM高速问题点汇聚", "GSM");

			// 创建外部表
			createExtTable(txtFile.getName(), tableName);

			// 核查LTE入库表数据
			StringBuilder logInfo = new StringBuilder();
			int checkData = checkData(tableName, logInfo, "queryGaosuLte",
					"LTE");
			if (checkData > 0) {
				throw new SQLException("核查高速问题点数据出错！");
			}

			// 核查GSM入库表数据
			checkData = checkData(tableName, logInfo, "queryGaosuGsm", "GSM");
			// 存在匹配不上的问题点,写入日志到数据库
			if (checkData > 0) {
				throw new SQLException("核查高速问题点数据出错！");
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("核查高速跟踪表数据出错：", e);
			// 删除外部表及外部表文件
			if (tableName != null) {
				String dropTableSql = sqlMap.get("deleteExtTable") + " "
						+ tableName;
				task.execute(conn, dropTableSql);
			}
			if (txtFile != null) {
				txtFile.delete();
			}
			throw new Exception("核查高速跟踪表数据出错：", e);
		} finally {

			// 关闭文件流
			if (is != null) {
				is.close();
			}
			if (bw != null) {
				bw.close();
			}
			if (fw != null) {
				fw.close();
			}
		}

		return txtFile;

	}

	/**
	 * 匹配成功,将匹配上的入库数据写入到聚类结果跟踪表lte_cluster_question
	 * @param tableName
	 * @param orderCode
	 * @throws SQLException 
	 */
	public void insertGaosuClusterData(String tableName, String orderCode)
			throws SQLException {

		// 插入LTE数据
		String inertSql = sqlMap.get("insertGaosuLte").replace("${tableName}",
				tableName);
		task.execute(conn, inertSql, orderCode, "LTE");

		// 插入GSM数据
		inertSql = sqlMap.get("insertGaosuGsm").replace("${tableName}",
				tableName);
		task.execute(conn, inertSql, orderCode, "GSM");

		// 插入LTE汇总表
		inertSql = sqlMap.get("insertLteCount").replace("${tableName}",
				tableName);
		task.execute(conn, inertSql, orderCode, "LTE");

		// 插入GSM汇总表
		inertSql = sqlMap.get("insertGsmCount").replace("${tableName}",
				tableName);
		task.execute(conn, inertSql, orderCode, "GSM");

	}

	/**
	 * 核查入库数据
	 * @param tableName
	 * @param logInfo
	 * @throws SQLException
	 */
	private int checkData(String tableName, StringBuilder logInfo,
			String sqlName, String type) throws SQLException {

		// 组织sql语句
		String lteSql = sqlMap.get(sqlName).replace("${tableName}", tableName);
		List<Map<String, Object>> selectOne = task
				.selectMap(conn, lteSql, type);

		// 核查通过返回0
		if (selectOne == null || selectOne.size() == 0) {
			return 0;
		}

		// 核查不通过记录相应日志
		if (selectOne.size() > 0) {
			// 数据不完整
			logInfo.append("高速" + type + "跟踪表以下问题点未找到:");
			for (Map<String, Object> map : selectOne) {
				logInfo.append(map.get("vcjlplbnum")).append(",");
			}
		}
		task.addDetaileLog(logInfo.toString());

		return selectOne.size();
	}

	/**
	 * 创建高速外部表
	 * @param name
	 * @param tableName
	 * @throws SQLException 
	 */
	private void createExtTable(String name, String tableName)
			throws SQLException {

		// 组织创建sql语句
		String extPath = task.getExternalPath() + name;
		String createTbaleSql = sqlMap.get("createGaosuExtTable")
				.replace("${tableName}", tableName)
				.replace("${externalPathStr}", extPath);

		// 执行语句
		task.execute(conn, createTbaleSql);

	}

	/**
	 * 将汇聚表数据写入到外部表文件
	 * @param bw 外部表数据文件流
	 * @param hssfWorkbook work对象
	 * @param sheetName sheet名称
	 * @param sheetType sheet类型
	 * @throws IOException
	 */
	private static void writeExtTxt(BufferedWriter bw, Workbook hssfWorkbook,
			String sheetName, String sheetType) throws IOException {

		// 取出里面两个跟踪sheet的聚类编号,数据写入文件
		Sheet lteSheet = hssfWorkbook.getSheet(sheetName);
		// 行数小于2说明没有数据
		if (lteSheet.getPhysicalNumberOfRows() > 2) {

			Iterator<Row> rowIterator = lteSheet.rowIterator();
			while (rowIterator.hasNext()) {
				Row next = rowIterator.next();
				// 第一行是标题
				if (next.getRowNum() == 0) {
					continue;
				}
				// 若聚类编号为空,跳过
				Cell cell = next.getCell(0);
				if (cell == null) {
					continue;
				}
				String jlbh = cell.getStringCellValue();
				bw.write(jlbh + "|" + sheetType);
				bw.newLine();
			}
		}
	}

}
