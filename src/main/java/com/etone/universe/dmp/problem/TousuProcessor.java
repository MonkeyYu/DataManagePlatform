package com.etone.universe.dmp.problem;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import com.etone.daemon.db.DB;
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
 * @author <a href="mailto:azpao@qq.com">xiejialin</a>
 * @version $Revision: 14169 $
 * @date 2017年9月5日  下午3:22:40
 */
public class TousuProcessor implements Runnable{

	public TousuProcessor(int logNum,Connection conn, Map<String, String> sqlMap,ProblemTask task,QueryCriteria criteria) {
		this.logNum = logNum;
		this.conn = conn;
		this.sqlMap = sqlMap;
		this.task = task;
		this.criteria = criteria;
	}

	private static final Logger logger = LoggerFactory.getLogger(TousuProcessor.class);

	private int logNum = 0;

	/**
	 * 获取数据库链接
	 */
	protected Connection conn = null;

	private QueryCriteria criteria = null;

	/**
	 * sql语句集合
	 */
	protected Map<String, String> sqlMap = null;

	/**
	 * 任务对象
	 */
	protected ProblemTask task = null;

	@Override
	public void run() {
		//
		try {

			this.conn = DB.getDataSource(task.getDataSource()).getConnection();

			// 查询非TOP N问题并处理入到表2
			List<Map> notTopNList = getNotTopNJlList(criteria);
			indsertNotTopNDatas(notTopNList, criteria);

			List<Map> topNList = getTopNJlList(criteria);
			indsertTopNDatas(topNList,criteria);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//非TOP N结果集
	public List<Map> getNotTopNJlList(QueryCriteria criteria) throws SQLException {
		// 1.先查询集中投诉的数据;
		List<Map<String, Object>> tsList = task.selectMap(logNum++, conn,sqlMap.get("queryTousuWorkJob"));
		List<Map> notTopNList = new ArrayList<Map>();//非TOP N问题数据
		List<Map<String,Object>> clusterList = task.selectMap(logNum++, conn,sqlMap.get("queryClusterList"));

		double flat = 0;
		double flonb = 0;
		double jllat = 0;
		double jllonb = 0;
		List<Map> jlList = null;

		String vcarea = "";

		// 2.若无,直接退出; 若有，做数据筛选
		if (tsList != null && tsList.size() > 0) {
			for (int i=0;i<tsList.size();i++) {
				if(null==tsList.get(i).get("vcgridid").toString()&&tsList.get(i).get("vcgridid").toString().equals("")){
					notTopNList.addAll((Collection<? extends Map>) tsList.get(i));
				}
			}
		}

		for(int i=0;i<notTopNList.size();i++){
			Map map = new HashMap();
			double distance = 0;
			flat = Double.parseDouble(notTopNList.get(i).get("flat").toString());
			flonb = Double.parseDouble(notTopNList.get(i).get("flonb").toString());
			for(int j=0;j<clusterList.size();j++){
				jllat = Double.parseDouble(clusterList.get(j).get("cluster_latitude").toString());
				jllonb = Double.parseDouble(clusterList.get(j).get("cluster_longitude").toString());
				distance = distance(flat,flonb,jllat,jllonb);
				if(distance<=300){
					map.put("intid",notTopNList.get(i).get("intid").toString());
					map.put("vctsid",notTopNList.get(i).get("vctsid").toString());
					map.put("dttstime",notTopNList.get(i).get("dttstime").toString());
					map.put("vccity",notTopNList.get(i).get("vccity").toString());
					map.put("vcarea",notTopNList.get(i).get("vcarea").toString());
					map.put("vctsmsg",notTopNList.get(i).get("vctsmsg").toString());
					map.put("vctstype",notTopNList.get(i).get("vctstype").toString());
					map.put("vcaddress",notTopNList.get(i).get("vcaddress").toString());
					map.put("vcnettype",notTopNList.get(i).get("vcnettype").toString());
					map.put("flong",notTopNList.get(i).get("flong").toString());
					map.put("flat",notTopNList.get(i).get("flat").toString());
					map.put("vcgridid",notTopNList.get(i).get("vcgridid").toString());
					map.put("vcchgrid",notTopNList.get(i).get("vcchgrid").toString());
					map.put("inttsnum",notTopNList.get(i).get("inttsnum").toString());
					map.put("vcaffectarea",notTopNList.get(i).get("vcaffectarea").toString());
					map.put("vcyhgrid",notTopNList.get(i).get("vcyhgrid").toString());

					map.put("vcjlplbnum",clusterList.get(j).get("cluster_code").toString());
					map.put("intjlcount",clusterList.get(j).get("question_count").toString());
					map.put("vcjlcity",clusterList.get(j).get("city").toString());
					map.put("fjllonb",clusterList.get(j).get("cluster_longitude").toString());
					map.put("fjllat",clusterList.get(j).get("cluster_latitude").toString());
					map.put("vccgi",clusterList.get(j).get("involve_site").toString());
					map.put("intdateid",clusterList.get(j).get("int_month").toString());
					map.put("vcdatasource",clusterList.get(j).get("data_source").toString());
					map.put("vcjlgridid",clusterList.get(j).get("home_grid").toString());
					map.put("vcfilename",clusterList.get(j).get("file_name").toString());
					map.put("vcquestiontype",clusterList.get(j).get("question_type").toString());
					map.put("first_analysis",clusterList.get(j).get("first_analysis").toString());
					map.put("first_proposal",clusterList.get(j).get("first_proposal").toString());
					map.put("reason_classify",clusterList.get(j).get("reason_classify").toString());
					map.put("first_proposal_type",clusterList.get(j).get("first_proposal_type").toString());
					map.put("detail_analysis",clusterList.get(j).get("detail_analysis").toString());
					map.put("detail_proposal",clusterList.get(j).get("detail_proposal").toString());
					map.put("detail_reason",clusterList.get(j).get("detail_reason").toString());
					map.put("detail_proposal_type",clusterList.get(j).get("detail_proposal_type").toString());
					map.put("trim_village",clusterList.get(j).get("trim_village").toString());
					map.put("property",clusterList.get(j).get("property").toString());
					map.put("target",clusterList.get(j).get("target").toString());
					map.put("order_state",clusterList.get(j).get("order_state").toString());
					map.put("is_solved",clusterList.get(j).get("is_solved").toString());
					map.put("vcordercode",clusterList.get(j).get("order_code").toString());
					map.put("createtime",clusterList.get(j).get("createtime").toString());
					map.put("problemstatus",clusterList.get(j).get("problemstatus").toString());
					jlList.add(map);
					break;
				}
			}
		}
		return jlList;
	}

	//TOP N结果集
	public List<Map> getTopNJlList(QueryCriteria criteria) throws SQLException {
		// 1.先查询集中投诉的数据;
		List<Map<String, Object>> tsList = task.selectMap(logNum++, conn,sqlMap.get("queryTousuWorkJob"));
		List<Map> topNList = new ArrayList<Map>();//TOP N问题数据
		List<Map<String,Object>> clusterList = task.selectMap(logNum++, conn,sqlMap.get("queryClusterList"));

		double flat = 0;
		double flonb = 0;
		double jllat = 0;
		double jllonb = 0;
		List<Map> jlList = null;

		String vcarea = "";

		// 2.若无,直接退出; 若有，做数据筛选
		if (tsList != null && tsList.size() > 0) {
			for (int i=0;i<tsList.size();i++) {
				if(null!=tsList.get(i).get("vcgridid").toString()&&!tsList.get(i).get("vcgridid").toString().equals("")){
					topNList.addAll((Collection<? extends Map>) tsList.get(i));
				}
			}
		}

		for(int i=0;i<topNList.size();i++){
			Map map = new HashMap();
			double distance = 0;
			flat = Double.parseDouble(topNList.get(i).get("flat").toString());
			flonb = Double.parseDouble(topNList.get(i).get("flonb").toString());
			for(int j=0;j<clusterList.size();j++){
				jllat = Double.parseDouble(clusterList.get(j).get("cluster_latitude").toString());
				jllonb = Double.parseDouble(clusterList.get(j).get("cluster_longitude").toString());
				distance = distance(flat,flonb,jllat,jllonb);
				if(distance<=300){
					map.put("intid",topNList.get(i).get("intid").toString());
					map.put("vctsid",topNList.get(i).get("vctsid").toString());
					map.put("dttstime",topNList.get(i).get("dttstime").toString());
					map.put("vccity",topNList.get(i).get("vccity").toString());
					map.put("vcarea",topNList.get(i).get("vcarea").toString());
					map.put("vctsmsg",topNList.get(i).get("vctsmsg").toString());
					map.put("vctstype",topNList.get(i).get("vctstype").toString());
					map.put("vcaddress",topNList.get(i).get("vcaddress").toString());
					map.put("vcnettype",topNList.get(i).get("vcnettype").toString());
					map.put("flong",topNList.get(i).get("flong").toString());
					map.put("flat",topNList.get(i).get("flat").toString());
					map.put("vcgridid",topNList.get(i).get("vcgridid").toString());
					map.put("vcchgrid",topNList.get(i).get("vcchgrid").toString());
					map.put("inttsnum",topNList.get(i).get("inttsnum").toString());
					map.put("vcaffectarea",topNList.get(i).get("vcaffectarea").toString());
					map.put("vcyhgrid",topNList.get(i).get("vcyhgrid").toString());
					jlList.add(map);
					break;
				}
			}
		}
		return jlList;
	}

	//非TOP N问题处理结果插入表2
	private void indsertNotTopNDatas(List<Map> dataList, QueryCriteria criteria)throws IOException,SQLException{
		if(dataList==null || dataList.size()==0){
			task.addDetaileLog("未找到投诉有效问题点...");
			return;
		}

		File extFile = null;
		String millis = System.currentTimeMillis() + "";
		String str = millis.substring(millis.length() - 12, 12);
		// 开启并发线程后,外部表名加上日志编号防止冲突
		String tableName = "edmp_" + logNum + "_" + str;

		try {
			// 创建外部表基础数据文件
			extFile = createExtFile(dataList);

			// 根据基础数据文件创建外部表
			String createTbaleSql = sqlMap.get("createExtTable").replace("${tableName}", tableName)
					.replace("${filePath}", task.getExternalPath() + extFile.getName());

			// 执行创建外部表语句
			task.execute(logNum++, conn, createTbaleSql);

			// 通过外部表把数据load进结果表
			String insertDataSql = sqlMap.get("insertData").replace("${tableName}", tableName);
			task.execute(logNum++, conn, insertDataSql);

		} catch (Exception e) {
			task.addDetaileLog(logNum++, "投诉数据结果通过外部表入库时出错:" + e.getLocalizedMessage());
		} finally {

			// 无论成功与否,都需要把表跟文件清除
			String deleteExtSql = sqlMap.get("deleteExtTable") + " " + tableName;
			task.execute(logNum++, conn, deleteExtSql);

			// 删除基础数据文件
			if (extFile != null && extFile.exists()) {
				extFile.delete();
			}

		}
	}

	//TOP N问题集合插入中间表
	private void indsertTopNDatas(List<Map> dataList, QueryCriteria criteria)throws IOException,SQLException{
		if(dataList==null || dataList.size()==0){
			task.addDetaileLog("未找到投诉有效问题点...");
			return;
		}

		File extFile = null;
		String millis = System.currentTimeMillis() + "";
		String str = millis.substring(millis.length() - 12, 12);
		// 开启并发线程后,外部表名加上日志编号防止冲突
		String tableName = "edmp_topn_" + logNum + "_" + str;

		try {
			// 创建外部表基础数据文件
			extFile = createExtFile(dataList);

			// 根据基础数据文件创建外部表
			String createTbaleSql = sqlMap.get("createTopNExtTable").replace("${tableName}", tableName)
					.replace("${filePath}", task.getExternalPath() + extFile.getName());

			// 执行创建外部表语句
			task.execute(logNum++, conn, createTbaleSql);

			// 通过外部表把数据load进结果表
			String insertDataSql = sqlMap.get("insertTempData").replace("${tableName}", tableName);
			task.execute(logNum++, conn, insertDataSql);

		} catch (Exception e) {
			task.addDetaileLog(logNum++, "投诉数据结果通过外部表入库时出错:" + e.getLocalizedMessage());
		} finally {

			// 无论成功与否,都需要把表跟文件清除
			String deleteExtSql = sqlMap.get("deleteExtTable") + " " + tableName;
			task.execute(logNum++, conn, deleteExtSql);

			// 删除基础数据文件
			if (extFile != null && extFile.exists()) {
				extFile.delete();
			}

		}
	}

	/**
	 * 创建外部表基础数据文件
	 * @param dataList
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	private File createExtFile(List<Map> dataList) throws IOException {

		// 如果该文件已经存在,则要换一个名字否则创建该文件
		String nowTimeStr = ProblemUtil.getNowTimeStr();
		String fileName = "/edmp_ts_" + nowTimeStr + ".txt";
		String filePath = task.getParpams().get("loadpath") + fileName;
		File file = new File(filePath);
		FileWriter out = null;
		try {

			// 处理文件,若已经存在,则更换文件名
			file = createFile(file);
			out = new FileWriter(file);
			StringBuilder sb = new StringBuilder();
			for (Map map : dataList) {
				sb.append(map.get("intid")).append("|").append(map.get("vctsid")).append("|").append(map.get("dttstime")).append("|")
					.append(map.get("vccity")).append("|").append(map.get("vcarea")).append("|").append(map.get("vctsmsg")).append("|")
					.append(map.get("vctstype")).append("|").append(map.get("vcaddress")).append("|").append(map.get("vcnettype")).append("|")
					.append(map.get("flong")).append("|").append(map.get("flat")).append("|").append(map.get("vcgridid")).append("|")
					.append(map.get("vcchgrid")).append("|").append(map.get("inttsnum")).append("|").append(map.get("vcaffectarea")).append("|")
					.append(map.get("vcyhgrid")).append("|").append(map.get("vcjlplbnum")).append("|").append(map.get("intjlcount")).append("|")
					.append(map.get("vcjlcity")).append("|").append(map.get("fjllonb")).append("|").append(map.get("fjllat")).append("|")
					.append(map.get("vccgi")).append("|").append(map.get("intdateid")).append("|").append(map.get("vcdatasource")).append("|")
					.append(map.get("vcjlgridid")).append("|").append(map.get("vcfilename")).append("|")
					.append(map.get("vcquestiontype")).append("|").append(map.get("first_analysis")).append("|")
					.append(map.get("first_proposal")).append("|").append(map.get("reason_classify")).append("|")
					.append(map.get("first_proposal_type")).append("|").append(map.get("detail_analysis")).append("|")
					.append(map.get("detail_proposal")).append("|").append(map.get("detail_reason")).append("|")
					.append(map.get("detail_proposal_type")).append("|").append(map.get("trim_village")).append("|")
					.append(map.get("property")).append("|").append(map.get("target")).append("|").append(map.get("order_state")).append("|")
					.append(map.get("is_solved")).append("|").append(map.get("vcordercode")).append("|")
					.append(map.get("createtime")).append("|").append(map.get("problemstatus"));
				out.write(sb.toString());
				out.write("\n");
				sb.delete(0, sb.length());
			}
		} catch (Exception e) {
			task.addDetaileLog(logNum++, "处理工单文件,转成外部表基础数据txt文件出错:" + e.getMessage());
		} finally {
			if (out != null) {
				out.close();
			}
		}

		return file;
	}

	/**
	 * 处理文件,若已经存在,则更换文件名
	 * @param nowTimeStr 当前时间字符串
	 * @param file 当前文件
	 * @throws IOException
	 */
	private File createFile(File file) throws IOException {

		// 若文件存在,则取当前毫秒数拼接新的文件名
		if (file.exists()) {
			String millis = System.currentTimeMillis() + "";
			String str = millis.substring(millis.length() - 12, 12);
			String pathname = task.getParpams().get("loadpath") + "/edmp_ts_" + str + ".txt";
			file = new File(pathname);
			return createFile(file);
		} else {
			file.createNewFile();
			return file;
		}

	}

	/**
	 * 计算两个经纬度之间的距离
	 * @param lat1
	 * @param lonb1
	 * @param lat2
	 * @param lonb2
	 * @return
	 */
	public Double distance(double lat1, double lonb1, double lat2, double lonb2) {
		double a, b, R;
		R = 6378137; // 地球半径
		lat1 = lat1 * Math.PI / 180.0;
		lat2 = lat2 * Math.PI / 180.0;
		a = lat1 - lat2;
		b = (lonb1 - lonb2) * Math.PI / 180.0;
		double d;
		double sa2, sb2;
		sa2 = Math.sin(a / 2.0);
		sb2 = Math.sin(b / 2.0);
		d = 2 * R * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1) * Math.cos(lat2) * sb2 * sb2));
		BigDecimal temp = new BigDecimal(d);
		double result = temp.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		return result;
	}
}
