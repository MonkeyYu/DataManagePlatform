package com.etone.universe.dmp.task.problem;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.daemon.db.DB;
import com.etone.daemon.db.helper.QueryHelper;
import com.etone.daemon.support.Env;
import com.etone.daemon.util.Mixeds;
import com.etone.universe.dmp.event.ProblemEvent;
import com.etone.universe.dmp.problem.DetailLog;
import com.etone.universe.dmp.task.BaseTask;
import com.etone.universe.dmp.util.DirFilter;
import com.etone.universe.dmp.util.FileCharsetUtil;
import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 问题点聚类task基础类
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年11月25日  上午10:17:34
 */
public class ProblemTest extends BaseTask {

	public static final Logger logger = LoggerFactory
			.getLogger(ProblemTest.class);

	/**
	 * 流程状态对应配置
	 */
	protected Map<String, String> judgeMap = new HashMap<String, String>();

	/**
	 * 流程状态key
	 */
	protected String judgeKey = "";

	/**
	 * 文件夹所在路径
	 */
	protected String filePath = "";

	/**
	 * 备份文件路径
	 */
	protected String bakFilePath = "";

	/**
	 * ftp文件路径
	 */
	protected String ftpPath = "";

	/**
	 * ftp用户名
	 */
	protected String ftpUser = "";

	/**
	 * ftp用户密码
	 */
	protected String ftpPassword = "";

	/**
	 * 外部表存放文件路径
	 */
	protected String externalPath = "";

	/**
	 * 错误文件存放路径
	 */
	protected String errorFilePath = "";

	/**
	 * 所有参数键值对集合
	 */
	protected Map<String, String> parpams = new HashMap<String, String>();

	/**
	 * 流程序号
	 */
	protected int num = 0;

	/**
	 * 日志信息
	 */
	protected StringBuilder logInfoStr = new StringBuilder();

	/**
	 * 日志信息对象
	 */
	protected ProblemEvent event = new ProblemEvent();

	/**
	 * 文件后缀过滤为.xlsx
	 */
	protected static DirFilter XLSXF = new DirFilter(ProblemUtil.FILE_XLSX);

	/**
	 * 文件后缀过滤为.csv
	 */
	protected static DirFilter CSV = new DirFilter(ProblemUtil.FILE_CSV);

	/**
	 * 文件后缀过滤为.xls
	 */
	protected static DirFilter XLSF = new DirFilter(ProblemUtil.FILE_XLS);

	/**
	 * 工单sheet名称配置文件
	 */
	protected String namesFileName = "";

	/**
	 * sql文件名
	 */
	protected String sqlFile = "";

	/**
	* 数据库
	*/
	protected String dataSource = "";

	/**
	 * 日志表UUID
	 */
	protected String logUUID = "";

	/**
	 * 详细日志
	 */
	protected List<DetailLog> detailLogs;

	/**
	 * 获取数据库链接
	 */
	protected Connection conn = null;

	/**
	 * 日志数据库链接
	 */
	protected static String logConnKey = Env.getProperties().getValue(
			"datasource.log");

	/**
	 * 时间格式化
	 */
	protected static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/**
	 * sql语句集合
	 */
	protected Map<String, String> sqlMap = null;

	@Override
	public void execute() {

//		String path1 = "/opt/ltemr/edmp_problem/lte_data/111.csv";
//		String path2 = "/opt/ltemr/edmp_problem/lte_data/222.csv";
//		long currentTimeMillis = System.currentTimeMillis();
//		FileCharsetUtil.transfer2(path1, path2);
//		System.out.println("处理时间为:"+(System.currentTimeMillis()-currentTimeMillis));
		System.out.println("This is Yu's test program");
	}

	/**
	 * 设置详细日志
	 * @param logInfo 日志信息
	 */
	public void addDetaileLog(String logInfo) {
		logger.info(logInfo);
		DetailLog log = new DetailLog();
		log.setIntnum(detailLogs.size() + 1);
		log.setVcinfo(logInfo);
		log.setVclogid(logUUID);
		detailLogs.add(log);
	}

	/**
	 * 设置详细日志
	 * @param logNum 步骤数
	 * @param logInfo 日志信息
	 */
	public void addDetaileLog(int logNum, String logInfo) {
		logger.info(logInfo);
		DetailLog log = new DetailLog();
		log.setIntnum(logNum);
		log.setVcinfo(logInfo);
		log.setVclogid(logUUID);
		detailLogs.add(log);
	}

	/**
	 * 设置详细日志,插入之前的日志中
	 * @param logInfo 日志信息
	 * @param index 下标
	 */
	public void addDetaileLog(String logInfo, int index) {
		logger.info(logInfo);
		detailLogs.remove(index);
		DetailLog log = new DetailLog();
		log.setIntnum(index + 1);
		log.setVcinfo(logInfo);
		log.setVclogid(logUUID);
		detailLogs.add(index, log);
	}

	@Override
	public void fromXml(Element task) throws Exception {

		// 初始化父类属性
		super.fromXml(task);

		// 初始化所有属性配置
		for (Attribute attr : task.attributes().asList()) {
			parpams.put(attr.getKey(), attr.getValue());
		}

		// 增加特有的属性,流程状态
		String judges = task.attr(ProblemUtil.JUDGE_STR);
		if (!Mixeds.isNullOrEmpty(judges)) {
			String[] splits = judges.split(ProblemUtil.JUDGE_SPLIT);
			for (int i = 0; i < splits.length; i++) {
				String[] jobs = splits[i].split(ProblemUtil.JUDGE_SPLIT1);
				if (jobs.length == 2) {
					judgeMap.put(jobs[0], jobs[1]);
				}
			}
		}

//		String source = task.attr("source");
//		if (Mixeds.isNullOrEmpty(source)) {
//			throw new Exception("Attribute source is not set on task[name="
//					+ getName() + "]");
//		}
//		setDataSource(source);
//		// 获取数据库链接
//		conn = DB.getConnection(getDataSource());

		// 文件夹路径
		String filePathStr = task.attr(ProblemUtil.FILEPATH_STR);
		if (!Mixeds.isNullOrEmpty(filePathStr)) {
			setFilePath(filePathStr);
		}

		// 备份文件夹路径
		String bakFilePathStr = task.attr(ProblemUtil.BAKFILEPATH_STR);
		if (!Mixeds.isNullOrEmpty(bakFilePathStr)) {
			setBakFilePath(bakFilePathStr);
		}

		// ftp文佳夹路径
		String ftpPathStr = task.attr(ProblemUtil.FTPPATH_STR);
		if (!Mixeds.isNullOrEmpty(ftpPathStr)) {
			setFtpPath(ftpPathStr);
		}

		// ftp用户名
		String ftpUserStr = task.attr(ProblemUtil.FTPUSER_STR);
		if (!Mixeds.isNullOrEmpty(ftpUserStr)) {
			setFtpUser(ftpUserStr);
		}

		// ftp用户密码
		String ftpPasswordStr = task.attr(ProblemUtil.FTPPASSWORD_STR);
		if (!Mixeds.isNullOrEmpty(ftpPasswordStr)) {
			setFtpPassword(ftpPasswordStr);
		}

		// 错误文件存放文件夹路径
		String errorFilePathStr = task.attr(ProblemUtil.ERRORFILEPATH_STR);
		if (!Mixeds.isNullOrEmpty(errorFilePathStr)) {
			setErrorFilePath(errorFilePathStr);
		}

		// 流程序号
		String numStr = task.attr(ProblemUtil.NUM_STR);
		if (!Mixeds.isNullOrEmpty(errorFilePathStr)) {
			setNum(Integer.valueOf(numStr));
		}

		// 工单sheet名称配置文件
		String namesFile = task.attr(ProblemUtil.NAMESFILENAME_STR);
		if (!Mixeds.isNullOrEmpty(namesFile)) {
			setNamesFileName(namesFile);
		}

		// 错误文件存放文件夹路径
		String externalPathStr = task.attr(ProblemUtil.EXTERNALPATH_STR);
		if (!Mixeds.isNullOrEmpty(externalPathStr)) {
			setExternalPath(externalPathStr);
		}

		// 错误文件存放文件夹路径
		String sqlFile = task.attr(ProblemUtil.SQLFILE_STR);
		if (!Mixeds.isNullOrEmpty(sqlFile)) {
			setSqlFile(sqlFile);
			sqlMap = ProblemUtil.getSqlMap(getSqlFile());
		}

		// 设置日志ID
		setLogUUID(ProblemUtil.getUUID());

		// 初始化日志集合
		setDetailLogs(new ArrayList<DetailLog>());

	}

	/**
	 * 处理外部表
	 * @param extPath 外部表文件路径
	 * @param tableName 外部表名
	 * @throws SQLException
	 */
	public void processorExtData(String extPath, String tableName)
			throws SQLException {
		// DROP外部表
		String dropTableSql = sqlMap.get("deleteExtTable") + " " + tableName;
		this.execute(conn, dropTableSql);

		// 删除外部表数据文件
		if (extPath != null) {
			File file = new File(extPath);
			file.delete();
		}
	}

	/**
	 * @return the judgeMap
	 */
	public Map<String, String> getJudgeMap() {
		return judgeMap;
	}

	/**
	 * @param judgeMap the judgeMap to set
	 */
	public void setJudgeMap(Map<String, String> judgeMap) {
		this.judgeMap = judgeMap;
	}

	/**
	 * @return the judgeKey
	 */
	public String getJudgeKey() {
		return judgeKey;
	}

	/**
	 * @param judgeKey the judgeKey to set
	 */
	public void setJudgeKey(String judgeKey) {
		this.judgeKey = judgeKey;
	}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @return the bakFilePath
	 */
	public String getBakFilePath() {
		return bakFilePath;
	}

	/**
	 * @param bakFilePath the bakFilePath to set
	 */
	public void setBakFilePath(String bakFilePath) {
		this.bakFilePath = bakFilePath;
	}

	/**
	 * @return the errorFilePath
	 */
	public String getErrorFilePath() {
		return errorFilePath;
	}

	/**
	 * @param errorFilePath the errorFilePath to set
	 */
	public void setErrorFilePath(String errorFilePath) {
		this.errorFilePath = errorFilePath;
	}

	/**
	 * @return the logInfoStr
	 */
	public StringBuilder getLogInfoStr() {
		return logInfoStr;
	}

	/**
	 * @param logInfoStr the logInfoStr to set
	 */
	public void setLogInfoStr(StringBuilder logInfoStr) {
		this.logInfoStr = logInfoStr;
	}

	/**
	 * @return the num
	 */
	public int getNum() {
		return num;
	}

	/**
	 * @param num the num to set
	 */
	public void setNum(int num) {
		this.num = num;
	}

	/**
	 * @return the event
	 */
	public ProblemEvent getEvent() {
		return event;
	}

	/**
	 * @param event the event to set
	 */
	public void setEvent(ProblemEvent event) {
		this.event = event;
	}

	/**
	 * @return the namesFileName
	 */
	public String getNamesFileName() {
		return namesFileName;
	}

	/**
	 * @param namesFileName the namesFileName to set
	 */
	public void setNamesFileName(String namesFileName) {
		this.namesFileName = namesFileName;
	}

	/**
	 * @return the xlsxF
	 */
	public static DirFilter getXlsxF() {
		return XLSXF;
	}

	/**
	 * @return the xlsF
	 */
	public static DirFilter getXlsF() {
		return XLSF;
	}

	/**
	 * @return the externalPath
	 */
	public String getExternalPath() {
		return externalPath;
	}

	/**
	 * @param externalPath the externalPath to set
	 */
	public void setExternalPath(String externalPath) {
		this.externalPath = externalPath;
	}

	/**
	 * @return the dataSource
	 */
	public String getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @return the sqlFile
	 */
	public String getSqlFile() {
		return sqlFile;
	}

	/**
	 * @param sqlFile the sqlFile to set
	 */
	public void setSqlFile(String sqlFile) {
		this.sqlFile = sqlFile;
	}

	/**
	 * @return the logUUID
	 */
	public String getLogUUID() {
		return logUUID;
	}

	/**
	 * @param logUUID the logUUID to set
	 */
	public void setLogUUID(String logUUID) {
		this.logUUID = logUUID;
	}

	/**
	 * @return the detailLogs
	 */
	public List<DetailLog> getDetailLogs() {
		return detailLogs;
	}

	/**
	 * @param detailLogs the detailLogs to set
	 */
	public void setDetailLogs(List<DetailLog> detailLogs) {
		this.detailLogs = detailLogs;
	}

	/**
	 * @return the conn
	 */
	public Connection getConn() {
		return conn;
	}

	/**
	 * @param conn the conn to set
	 */
	public void setConn(Connection conn) {
		this.conn = conn;
	}

	/**
	 * @return the sqlMap
	 */
	public Map<String, String> getSqlMap() {
		return sqlMap;
	}

	/**
	 * @param sqlMap the sqlMap to set
	 */
	public void setSqlMap(Map<String, String> sqlMap) {
		this.sqlMap = sqlMap;
	}

	/**
	 * @return the ftpPath
	 */
	public String getFtpPath() {
		return ftpPath;
	}

	/**
	 * @param ftpPath the ftpPath to set
	 */
	public void setFtpPath(String ftpPath) {
		this.ftpPath = ftpPath;
	}

	/**
	 * @return the ftpUser
	 */
	public String getFtpUser() {
		return ftpUser;
	}

	/**
	 * @param ftpUser the ftpUser to set
	 */
	public void setFtpUser(String ftpUser) {
		this.ftpUser = ftpUser;
	}

	/**
	 * @return the ftpPassword
	 */
	public String getFtpPassword() {
		return ftpPassword;
	}

	/**
	 * @param ftpPassword the ftpPassword to set
	 */
	public void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}

	/**
	 * 执行sql语句,并记录日志
	 * @param conn 数据库链接
	 * @param sql sql语句
	 * @param params 参数
	 * @return 影响行数
	 * @throws SQLException
	 */
	public int execute(Connection conn, String sql, Object... params)
			throws SQLException {

		// 记录日志
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("执行sql语句:[" + sql + "]	参数:[");

		for (Object object : params) {
			sBuilder.append(object.toString()).append(",");
		}

		sBuilder.append("]");
		addDetaileLog(sBuilder.toString());

		return QueryHelper.execute(conn, sql, params);

	}

	/**
	 * 执行sql语句,并记录日志
	 * 
	 * @param logNum 日志步骤
	 * @param conn 数据库链接
	 * @param sql sql语句
	 * @param params 参数
	 * @return 影响行数
	 * @throws SQLException
	 */
	public int execute(int logNum, Connection conn, String sql,
			Object... params) throws SQLException {

		// 记录日志
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("执行sql语句:[" + sql + "]	参数:[");

		for (Object object : params) {
			sBuilder.append(object.toString()).append(",");
		}

		sBuilder.append("]");
		addDetaileLog(logNum, sBuilder.toString());

		return QueryHelper.execute(conn, sql, params);

	}

	/**
	 * 查询一条记录
	 *
	 * @param conn      连接
	 * @param beanClazz 实体类型
	 * @param sql       查询语句
	 * @param params    参数
	 * @param <T>       实体类型
	 * @return 实体
	 */
	public <T> T selectOne(Connection conn, Class<T> beanClazz, String sql,
			Object... params) throws SQLException {

		// 记录日志
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("执行sql语句:[" + sql + "]	参数:[");

		for (Object object : params) {
			sBuilder.append(object.toString()).append(",");
		}

		sBuilder.append("]");
		addDetaileLog(sBuilder.toString());

		return QueryHelper.selectOne(conn, beanClazz, sql, params);
	}

	/**
	 * 查询一条记录
	 * 
	 * @param logNum    日志步骤
	 * @param conn      连接
	 * @param beanClazz 实体类型
	 * @param sql       查询语句
	 * @param params    参数
	 * @param <T>       实体类型
	 * @return 实体
	 */
	public <T> T selectOne(int logNum, Connection conn, Class<T> beanClazz,
			String sql, Object... params) throws SQLException {

		// 记录日志
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("执行sql语句:[" + sql + "]	参数:[");

		for (Object object : params) {
			sBuilder.append(object.toString()).append(",");
		}

		sBuilder.append("]");
		addDetaileLog(logNum, sBuilder.toString());

		return QueryHelper.selectOne(conn, beanClazz, sql, params);
	}

	/**
	 * 查询返回动态结果集
	 *
	 * @param conn   连接
	 * @param sql    查询语句
	 * @param params 参数
	 * @return 动态结果集
	 * @throws SQLException
	 */
	public List<Map<String, Object>> selectMap(Connection conn, String sql,
			Object... params) throws SQLException {

		// 记录日志
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("执行sql语句:[" + sql + "]	参数:[");

		for (Object object : params) {
			sBuilder.append(object.toString()).append(",");
		}

		sBuilder.append("]");
		addDetaileLog(sBuilder.toString());

		return QueryHelper.selectMap(conn, sql, params);
	}

	/**
	 * 查询返回动态结果集
	 *
	 * @param logNum 日志步骤
	 * @param conn   连接
	 * @param sql    查询语句
	 * @param params 参数
	 * @return 动态结果集
	 * @throws SQLException
	 */
	public List<Map<String, Object>> selectMap(int logNum, Connection conn,
			String sql, Object... params) throws SQLException {

		// 记录日志
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("执行sql语句:[" + sql + "]	参数:[");

		for (Object object : params) {
			sBuilder.append(object.toString()).append(",");
		}

		sBuilder.append("]");
		addDetaileLog(sBuilder.toString());

		return QueryHelper.selectMap(conn, sql, params);
	}

	/**
	 * 查询
	 *
	 * @param conn      连接
	 * @param beanClazz 实体类型
	 * @param sql       查询语句
	 * @param params    参数
	 * @param <T>       实体类型
	 * @return 实体
	 */
	public <T> List<T> select(Connection conn, Class<T> beanClazz, String sql,
			Object... params) throws SQLException {

		// 记录日志
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("执行sql语句:[" + sql + "]	参数:[");

		for (Object object : params) {
			sBuilder.append(object.toString()).append(",");
		}

		sBuilder.append("]");
		addDetaileLog(sBuilder.toString());

		return QueryHelper.select(conn, beanClazz, sql, params);
	}

	/**
	 * 查询
	 *
	 * @param logNum    日志步骤
	 * @param conn      连接
	 * @param beanClazz 实体类型
	 * @param sql       查询语句
	 * @param params    参数
	 * @param <T>       实体类型
	 * @return 实体
	 */
	public <T> List<T> select(int logNum, Connection conn, Class<T> beanClazz,
			String sql, Object... params) throws SQLException {

		// 记录日志
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("执行sql语句:[" + sql + "]	参数:[");

		for (Object object : params) {
			sBuilder.append(object.toString()).append(",");
		}

		sBuilder.append("]");
		addDetaileLog(sBuilder.toString());

		return QueryHelper.select(conn, beanClazz, sql, params);
	}

	/**
	 * @return the parpams
	 */
	public Map<String, String> getParpams() {
		return parpams;
	}

	/**
	 * @param parpams the parpams to set
	 */
	public void setParpams(Map<String, String> parpams) {
		this.parpams = parpams;
	}

}
