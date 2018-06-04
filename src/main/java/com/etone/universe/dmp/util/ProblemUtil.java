package com.etone.universe.dmp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.daemon.support.Env;
import com.etone.universe.dmp.task.problem.ProblemTask;

/**
 * 问题点聚类工具类
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年11月25日  上午9:46:21
 */
public class ProblemUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(ProblemUtil.class);

	public static Scheduler scheduler = null;

	public static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	/**
	 * 工单shee名称
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, Map> namesMap = new HashMap<String, Map>();

	/**
	 * table名称
	 */
	public static Map<String, String> tableNames = null;

	/**
	 * 缓存流程参数的对象
	 */
	public static Map<String, Map<String, String>> paramMap = new HashMap<String, Map<String, String>>();

	/**
	 * 根据jobName调用相应的job执行一次
	 * @param jobName job名称
	 * @return 调度成功返回null,调度失败返回失败信息
	 */
	public static String gotoJob(String jobName) {

		// 获取调度器job名称
		String sdJobName = new StringBuilder(jobName).append("_")
				.append("DETAIL").toString();

		try {
			// 获取调度器对象
			Scheduler scheduler = getSchedulerInstance();

			// 通过jobName，执行一次对应的job
			scheduler.triggerJob(sdJobName, jobName);

		} catch (Exception e) {
			logger.error("调用JOB[" + jobName + "]失败：", e);
			return "调用JOB[" + jobName + "]失败！";
		}

		return null;
	}

	/**
	 * 调用wsdl接口
	 * @param url 接口描述地址
	 * @param method 调用的接口方法
	 * @param parameters 调用的参数
	 * @return
	 * @throws Exception
	 */
	public static String executeMethod(String url, String method,
			String parameters) throws Exception {

		JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
		if (!url.endsWith("wsdl")) {
			url += "?wsdl";
		}

		org.apache.cxf.endpoint.Client client = dcf.createClient(url);
		return client.invoke(method, parameters)[0].toString();
	}

	/**
	 * 获取调度器对象
	 * @throws SchedulerException
	 */
	private static Scheduler getSchedulerInstance() throws SchedulerException {
		if (scheduler == null) {
			StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
			Properties props = new Properties();
			props.setProperty("org.quartz.threadPool.class",
					"org.quartz.simpl.SimpleThreadPool");
			stdSchedulerFactory.initialize(props);
			scheduler = stdSchedulerFactory.getScheduler();
			return scheduler;
		}
		return scheduler;
	}

	/**
	 * 获取当前时间格式化字符串精确到秒(yyyyMMddHHmmss)
	 * @return
	 */
	public static String getNowTimeStr() {
		return dateFormat.format(new Date(System.currentTimeMillis()));
	}

	/**
	 * 根据ID获取配置数据
	 * @param fileName 配置文件名
	 * @param id 配置的id
	 * @return Map<key,value>
	 */
	@SuppressWarnings("rawtypes")
	public static Map getIdNames(String fileName, String id) {

		if (namesMap.get(id) != null) {
			return namesMap.get(id);
		}

		String path = Env.getConfigFile(fileName);

		File config = new File(path);
		if (!config.exists()) {
			logger.error("找不到对应的工单文件名配置文件 : ", path);
		}

		Map resultMap = new HashMap();
		try {
			Document root = Jsoup.parse(config, UTF8_STR);
			Element elementIds = root.getElementById(id);

			// 获取value类型,默认为string
			String type = elementIds.attr(TYPE_STR);
			type = type == null || "".equals(type) ? STRING_STR : type;

			Elements elementByName = elementIds.select(NAME_STR);

			// 根据配置的类型转换类型
			putData(resultMap, type, elementByName);

			namesMap.put(id, resultMap);
		} catch (IOException e) {
			logger.error("解析工单文件名配置文件出错: ", e);
			return null;
		}

		return resultMap;
	}

	/**
	 * @param resultMap
	 * @param type
	 * @param elementByName
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void putData(Map resultMap, String type,
			Elements elementByName) {
		// 字符串
		if (STRING_STR.equals(type)) {
			for (Element ele : elementByName) {
				String key = ele.text();
				if (ele.attr(KEY_STR) != null) {
					key = ele.attr(KEY_STR);
				}
				String text = ele.text();
				//System.out.println("<name key=\""+text+"\">"+key+"</name>");
				resultMap.put(key, text);
			}
		}

		// 双精度
		if (DOUBLE_STR.equals(type)) {
			for (Element ele : elementByName) {
				String key = ele.text();
				if (ele.attr(KEY_STR) != null) {
					key = ele.attr(KEY_STR);
				}
				resultMap.put(key, Double.valueOf(ele.text()));
			}
		}

		// 整数
		if (INT_STR.equals(type)) {
			for (Element ele : elementByName) {
				String key = ele.text();
				if (ele.attr(KEY_STR) != null) {
					key = ele.attr(KEY_STR);
				}
				resultMap.put(key, Integer.valueOf(ele.text()));
			}
		}
	}

	/**
	 * 获取配置文件中的sql语句
	 * @param fileName 配置文件名
	 * @return 返回sql集合 <name,sql>
	 * @throws IOException
	 */
	public static Map<String, String> getSqlMap(String fileName)
			throws IOException {

		// 读取sql语句
		Map<String, String> sqlMap = readSqlXml(fileName);

		// 把默认的sql文件也加进去
		String defaultXML = Env.getProperties().getValue(
				"problem.defaultsqlxml");
		if (defaultXML == null || "".equals(defaultXML)) {
			return sqlMap;
		}
		sqlMap.putAll(readSqlXml(defaultXML));

		return sqlMap;
	}

	/**
	 * 循环遍历工单文件及shee,一个工单的所有sheet放到同一个目录一次性校验
	 * @param jobFile 工单文件
	 * @param workMap 工单流程参数对象
	 * @param task 任务对象
	 * @param createSql 外部表建表语句
	 * @return 返回为空的sheet集合
	 * @throws IOException
	 * @throws SQLException 
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> processorJobFile(File jobFile,
			Map<String, String> workMap, ProblemTask task, String createSql)
			throws IOException, SQLException {

		// 写文件流
		FileOutputStream fw = null;
		BufferedWriter bw = null;
		File txtFile = null;
		String jobName = jobFile.getName();
		String txtName = ProblemUtil.EXT_EDMP_TABLENAME
				+ System.currentTimeMillis() + ProblemUtil.FILE_TXT;
		String txtPath = task.getParpams().get("loadpath") + ProblemUtil.FILE_P
				+ txtName;

		// 记录空sheet
		Map<String, String> nullMap = new HashMap<String, String>();

		try {

			// 设置日志信息
			task.addDetaileLog("工单:《" + jobName + "》转txt开始...");

			InputStream is = new FileInputStream(jobFile);
			XSSFWorkbook hssfWorkbook = new XSSFWorkbook(is);

			// 创建外部表TXT文件
			txtFile = new File(txtPath);
			if (txtFile.exists()) {
				txtFile.createNewFile();
			}

			fw = new FileOutputStream(txtFile);
			bw = new BufferedWriter(new OutputStreamWriter(fw, "UTF-8"));
			task.addDetaileLog("工单《" + jobName + "》生成的txt文件：《" + txtPath
					+ "》...");

			// 获取配置的工单核查sheet名称,遍历每个sheet
			Map<String, String> nameMap = ProblemUtil.getIdNames(
					task.getNamesFileName(), "sheeNames");
			int problemCount = proSheet(bw, nullMap, hssfWorkbook, nameMap);
			workMap.put("problemCount", problemCount + "");
		} catch (Exception e) {
			logger.error("转外部表出错：", e);
			// 出错删除外部表文件
			if (txtFile != null) {
				txtFile.delete();
			}
			throw new IOException(e);
		} finally {
			if (bw != null) {
				bw.close();
			}
			if (fw != null) {
				fw.close();
			}
		}

		// 外部表文件绑定到工单信息里面,方便后续派单完后删除
		workMap.put("extFilePath", txtPath);
		workMap.put("extFileName", txtFile.getName());

		task.addDetaileLog("工单:《" + jobName + "》转txt完成...");

		// 创建GP外部表语句
		String tableName = ProblemUtil.EXT_EDMP_TABLENAME
				+ System.currentTimeMillis();
		String extPath = task.getExternalPath() + workMap.get("extFileName");
		String createTbaleSql = createSql.replace("${tableName}", tableName)
				.replace("${externalPathStr}", extPath);

		// 创建外部表
		task.execute(task.getConn(), createTbaleSql);
		workMap.put("tableName", tableName);
		return nullMap;

	}

	/**
	 * 处理sheet
	 * @param bw
	 * @param nullMap
	 * @param hssfWorkbook
	 * @param nameMap
	 * @throws IOException
	 */
	public static int proSheet(BufferedWriter bw, Map<String, String> nullMap,
			XSSFWorkbook hssfWorkbook, Map<String, String> nameMap)
			throws IOException {
		int problemCount = 0;
		for (String nameKey : nameMap.keySet()) {
			String sheetName = nameKey;
			XSSFSheet sheet = hssfWorkbook.getSheet(sheetName);
			int wtdhh = 0;
			// 第一列包含聚类编号
			if (sheet.getRow(0).getCell(0).getStringCellValue().contains("聚类")) {
				wtdhh = 1;
			}

			// 判断第二行第二列是否为空
			if ("本周暂无".equals(sheet.getRow(1).getCell(0).getStringCellValue())
					&& sheet.getRow(1).getCell(1) == null) {
				nullMap.put(sheetName, nameMap.get(nameKey));
				continue;
			}

			boolean isGyxx = "LTE高重叠覆盖路段概要信息".equals(sheet.getSheetName());

			for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
				Row next = sheet.getRow(i);
				int rowNum = next.getRowNum();
				String valStr = next.getCell(wtdhh).getStringCellValue();
				if (isGyxx) {
					double flong = next.getCell(5).getNumericCellValue();
					double flat = next.getCell(6).getNumericCellValue();
					bw.write(valStr + "|" + sheet.getSheetName() + "|" + rowNum
							+ "|" + flong + "|" + flat);

				} else {
					bw.write(valStr + "|" + sheet.getSheetName() + "|" + rowNum
							+ "|0|0");
				}

				bw.newLine();
				problemCount++;
			}
		}
		return problemCount;
	}

	/**
	 * 读取xml里面的sql语句
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private static Map<String, String> readSqlXml(String fileName)
			throws IOException {
		String path = Env.getConfigFile(fileName);

		File config = new File(path);
		System.out.println(config.getAbsolutePath());
		if (!config.exists()) {
			logger.error("schedule configure file {} not exists : ", path);
			logger.info("shutdown schedule plugin");
			System.exit(1);
		}
		logger.info("read schedule configure from xml : {}", path);

		Document root = Jsoup.parse(config, "UTF-8");
		Elements sqlElements = root.select(ProblemUtil.SQL_STR);
		if (sqlElements == null) {
			return null;
		}
		Map<String, String> sqlMap = new HashMap<String, String>();
		for (Element ele : sqlElements) {
			//logger.info("读取配置文件sql语句:" + ele.text());
			sqlMap.put(ele.attr(ProblemUtil.NAME_STR), ele.text());
		}
		return sqlMap;
	}

	/**
	 * 执行linux命令方法
	 * @param command 命令
	 * @param task 问题点任务对象
	 * @return
	 */
	public static boolean excuteLiuxOrde3r(String command, ProblemTask task) {
		task.addDetaileLog("执行命令：" + command);
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
			e.printStackTrace();
			task.addDetaileLog("执行命令[" + command + "]出错:" + e.getMessage());
		}
		process.destroy();
		return returnFlag;
	}

	/**
	 * 执行linux命令方法
	 * @param command[] 命令集
	 * @param task 问题点任务对象
	 * @return
	 */
	public static boolean excuteLiuxOrde3r(String command[], ProblemTask task) {
		task.addDetaileLog("执行命令：" + command[2]);
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
			logger.info("正确执行命令结果:" + sb.toString());
			bufferedReader = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));
			lo = null;
			while ((lo = bufferedReader.readLine()) != null) {
				logger.error("ERROR:" + lo);
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
	 * 执行linux命令方法
	 * @param command 命令
	 * @return
	 */
	public static boolean excuteLiuxOrde3r(String command) {
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
			e.printStackTrace();
			logger.error("执行命令[" + command + "]出错:" + e.getMessage(), e);
		}
		process.destroy();
		return returnFlag;
	}

	/**
	 * 获取UUID
	 * @return 返回JDK自带的API生成的UUID
	 */
	public static String getUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 流程状态标识,目前只用于问题点这块
	 * 配置：在xml的job标签上配置相应的judge属性
	 * 格式：以,隔开，如：0-jobName,1-jobName1
	 * 作用：根据不同的流程状态,调用配置对应job
	 */
	public static final String JUDGE_ZERO = "0";
	public static final String JUDGE_ONE = "1";
	public static final String JUDGE_TOW = "2";
	public static final String JUDGE_THREE = "3";

	/**
	 *常量 
	 */
	public static final String JUDGE_STR = "judge";
	public static final String FILEPATH_STR = "filePath";
	public static final String ERRORFILEPATH_STR = "errorFilePath";
	public static final String BAKFILEPATH_STR = "bakFilePath";
	public static final String FTPPATH_STR = "ftpPath";
	public static final String FTPUSER_STR = "ftpUser";
	public static final String FTPPASSWORD_STR = "ftpPassword";
	public static final String EXTERNALPATH_STR = "externalPath";
	public static final String NUM_STR = "num";
	public static final String NAMESFILENAME_STR = "namesFileName";
	public static final String EXT_EDMP_TABLENAME = "ext_edmp_";
	public static final String NAME_STR = "name";
	public static final String SQL_STR = "sql";
	public static final String SQLFILE_STR = "sqlfile";
	public static final String CONFIG_STR = "config";
	public static final String LOGDATA_STR = "logData";
	public static final String JUDGE_SPLIT = ",";
	public static final String JUDGE_SPLIT1 = "-";
	public static final String SPLIT2 = ".";
	public static final String FILE_XLS = ".xls";
	public static final String FILE_XLSX = ".xlsx";
	public static final String FILE_CSV = ".csv";
	public static final String FILE_TXT = ".txt";
	public static final String FILE_P = "/";
	public static final String FILE_S = ",";
	public static final String JOBFILE_STR = "jobfile";
	public static final String CHECK_STR = "check";
	public static final String CLUSTER_STR = "cluster";
	public static final String ORDERTABLENAME_STR = "${orderTableName}";
	public static final String KEY_STR = "key";
	public static final String UTF8_STR = "UTF-8";
	public static final String TYPE_STR = "type";
	public static final String INT_STR = "int";
	public static final String DOUBLE_STR = "double";
	public static final String STRING_STR = "string";

	/**
	 * 日志状态,1(正常)0(异常)
	 */
	public static final String STATUS_SU = "1";
	public static final String STATUS_ER = "0";

}
