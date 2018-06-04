package com.etone.universe.dmp.problem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.daemon.db.DB;
import com.etone.universe.dmp.task.problem.ProblemTask;
import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 聚类处理类
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年12月8日  上午10:23:08
 */
public class ClusterProcessor implements Runnable {

	public static final Logger logger = LoggerFactory.getLogger(ClusterProcessor.class);

	public ClusterProcessor(int logNum, Map<String, String> sqlMap, ProblemTask task, QueryCriteria criteria)
			throws SQLException {

		this.logNum = logNum;
		this.sqlMap = sqlMap;
		this.task = task;
		this.criteria = criteria;
	}

	@Override
	public void run() {

		// 工单编号
		String orderCode = criteria.get("orderCode").toString();
		task.addDetaileLog(logNum++, "工单[" + orderCode + "]聚类开始...");

		try {

			this.conn = DB.getDataSource(task.getDataSource()).getConnection();

			// 1.删除工单的聚类
			deleteData(orderCode);

			// 20170628优化语句，先过滤掉重复的问题点生成一张新的表
			createFilterTable(criteria);

			// 2.开始工单聚类
			getJlList(criteria);

		} catch (Exception e) {

			e.printStackTrace();
			logger.error("聚类出错：", e);
			task.addDetaileLog(logNum++, "工单[" + orderCode + "]聚类出错：" + e.getMessage());

			try {
				processorExtData(criteria.get("rkhcExtPath").toString(), criteria.get("rkhcTablename").toString());
			} catch (SQLException e1) {
				e1.printStackTrace();
				task.addDetaileLog(logNum++, "移除工单外部表出错...");
			}

			// 移除出错的工单
			ProblemUtil.paramMap.remove(orderCode);

		} finally {
			
			// 无论成功与否，都要删掉临时表
			if(newTabelname != null && !"".equals(newTabelname)){
				try {
					task.execute(logNum, conn, "drop table "+newTabelname);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			// 结束后关闭数据库链接
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
					task.addDetaileLog(logNum++, "关闭数据库链接出错...");
				}
			}
		}

		task.addDetaileLog(logNum++, "工单[" + orderCode + "]聚类结束...");

	}

	/**
	 * 20170628优化语句，先过滤掉重复的问题点生成一张新的表
	 * @throws SQLException 
	 */
	private void createFilterTable(QueryCriteria criteria) throws SQLException {
        // 外部表名
        extTableName = criteria.get("tableName").toString();

		// 1.生成新的表名
		newTabelname = ProblemUtil.EXT_EDMP_TABLENAME + System.currentTimeMillis() + logNum;

		// 2.创建过滤后的表
		String sql = sqlMap.get("createFilterTable").replace("${tableName}", newTabelname);
		task.execute(logNum, conn, sql);

		// 3.插入表数据
		sql = sqlMap.get("insertFilterTable").replace("${tableName}", newTabelname).replace("${orderTableName}", extTableName);
		task.execute(logNum, conn, sql);
	}

	private int logNum = 0;

	// 获取数据库链接
	private Connection conn = null;

	private QueryCriteria criteria = null;

	// sql语句集合
	private Map<String, String> sqlMap = null;

	// 任务对象
	protected ProblemTask task = null;

	// 外部表名
	private String extTableName = "";
	
	// 过滤历史问题点后的新表
	private String newTabelname = "";

	// 聚合类别权重
	private Map<String, Integer> jhlbqzMap = null;

	// 聚类类别权重基准
	private Map<String, Double> jhlbjzMap = null;

	// 城市编号集合
	private Map<String, String> cityMap = null;

	// 城市编号集合
	private Map<String, String> cityNameMap = null;

	// 聚类对应的聚类编号
	private Map<String, Integer> intjlTypeMap = null;

	// 初始化聚合类别
	@SuppressWarnings("unchecked")
	public void initJllb() {

		// 聚合类别权重
		if (jhlbqzMap == null) {
			jhlbqzMap = ProblemUtil.getIdNames(task.getNamesFileName(), "jhlbqz");
		}

		// 聚类类别权重基准
		if (jhlbjzMap == null) {
			jhlbjzMap = ProblemUtil.getIdNames(task.getNamesFileName(), "jhlbjz");
		}

		// 20161211 新版聚类编号集合
		if (intjlTypeMap == null) {
			intjlTypeMap = ProblemUtil.getIdNames(task.getNamesFileName(), "intjlType");
		}

		// 初始化城市编号集合
		if (cityMap == null) {
			cityMap = ProblemUtil.getIdNames(task.getNamesFileName(), "city");
		}

		// 初始化城市编号集合
		if (cityNameMap == null || cityNameMap.size() == 0) {
			cityNameMap = ProblemUtil.getIdNames(task.getNamesFileName(), "cityname");
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map> jlTempList(List<Map> list, QueryCriteria criteria) throws SQLException {

		// 城市+聚类类型
		String strPlb = "";
		List<Map> finalList = new ArrayList();
		int intcityid = 0;

		if (list.size() > 0) {
			strPlb = cityMap.get(list.get(0).get("intcityid").toString());
			intcityid = Integer.valueOf(list.get(0).get("intcityid").toString());
		}
		criteria.put("tempCity", intcityid);

		// 未关闭派单的聚类
		List<Map<String, Object>> oldJlList = new ArrayList<Map<String, Object>>();

		// 经纬度聚类
		if (criteria.get("jllb").equals("jwd")) {

			// 未关闭派单的经纬度聚类
			oldJlList = task.selectMap(logNum++, conn, sqlMap.get("getCoordinateList"), criteria.get("tempCity"), criteria.get("tempCity"));

			// 开始经纬度聚类
			jwdJl(list, criteria, strPlb, finalList, intcityid, oldJlList);
		}

		// 信令经纬度聚类
		if (criteria.get("jllb").equals("xljwd")) {

			// 回写字段类型
			criteria.put("jllb", "jwd");
			// 未关闭派单的经纬度聚类
			oldJlList = task.selectMap(logNum++, conn, sqlMap.get("getXlCoordinateList"), criteria.get("tempCity"), criteria.get("tempCity"));

			// 开始经纬度聚类
			jwdJl(list, criteria, strPlb, finalList, intcityid, oldJlList);
		}

		// 小区聚类
		if (criteria.get("jllb").equals("xq")) {

			// 未关闭派单的小区聚类
			oldJlList = task.selectMap(logNum++, conn, sqlMap.get("getCgiList"), criteria.get("tempCity"));

			// 开始小区聚类
			xqJl(list, criteria, strPlb, finalList, intcityid, oldJlList);
		}

		// 信令小区聚类
		if (criteria.get("jllb").equals("xlxq")) {

			// 回写字段类型
			criteria.put("jllb", "xq");
			// 未关闭派单的小区聚类
			oldJlList = task.selectMap(logNum++, conn, sqlMap.get("getXlCgiList"), criteria.get("tempCity"));

			// 开始小区聚类
			xqJl(list, criteria, strPlb, finalList, intcityid, oldJlList);
		}

		return finalList;
	}

	/**
	 * 经纬度聚类
	 * @param list
	 * @param criteria
	 * @param strPlb
	 * @param finalList
	 * @param intcityid
	 * @param oldJlList
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void jwdJl(List<Map> list, QueryCriteria criteria, String strPlb, List<Map> finalList, int intcityid,
			List<Map<String, Object>> oldJlList) {

		int intfiletime = Integer.parseInt(criteria.get("intfiletime").toString());
		String yearMonth = criteria.get("yearMonth").toString();

		// 最大最小距离
		Double minDistance = Double.parseDouble(criteria.get("minDistance").toString());
		Double maxDistance = Double.parseDouble(criteria.get("maxDistance").toString());

		while (list.size() > 0) {

			List<Map> jlList = new ArrayList();// 存放同一聚类的点，计算经纬度等
			jlList.add(list.get(0));
			double fplbbalance = Double.parseDouble(list.get(0).get("fplbbalance").toString());

			// 获取距离范围内的所有数据
			List<Map> tList = new ArrayList();
			getScopeData(list, maxDistance, tList);

			// 筛选数据
			fplbbalance = filtrateData(minDistance, jlList, fplbbalance, tList);

			// 计算中心经纬度
			double jllonb = 0;
			double jllat = 0;
			double jlradiu = 0;
			double radiuTemp = 0;
			double flat = 0;
			double flonb = 0;
			if (jlList.size() > 0) {

				// 计算本次聚类结果的中心经纬度
				for (int p = 0; p < jlList.size(); p++) {
					jllonb += Double.parseDouble(jlList.get(p).get("flonb").toString());
					jllat += Double.parseDouble(jlList.get(p).get("flat").toString());
				}

				jllonb = new BigDecimal(jllonb / jlList.size()).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();// 中心经度
				jllat = new BigDecimal(jllat / jlList.size()).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();// 中心纬度

				for (int p = 0; p < jlList.size(); p++) {
					flonb = Double.parseDouble(jlList.get(p).get("flonb").toString());
					flat = Double.parseDouble(jlList.get(p).get("flat").toString());
					radiuTemp = distance(jllat, jllonb, flat, flonb);// 聚类半径
					if (jlradiu <= radiuTemp) {
						jlradiu = radiuTemp;
					}
				}

				if (oldJlList.size() > 0) {

					// 存在未关闭派单的数据,需要新旧重聚
					oldMateNew(criteria, intfiletime, strPlb, yearMonth, finalList, intcityid, oldJlList, jlList,
							fplbbalance, jllonb, jllat, jlradiu);

				} else {

					// 不存在未关闭派单的数据,处理完的数据存放进结果集
					notOldData(criteria, intfiletime, strPlb, finalList, intcityid, yearMonth, jlList, fplbbalance,
							jllonb, jllat, jlradiu);
				}
			}

			// lsjList中删除聚类的点或者没用的点
			if (jlList.size() != 0) {
				list.removeAll(jlList);
			} else {
				list.remove(0);
			}
		}
	}

	/**
	 * 把聚好类的数据放进结果集
	 * @param criteria
	 * @param intfiletime
	 * @param strPlb
	 * @param finalList
	 * @param intcityid
	 * @param yearMonth
	 * @param jlList
	 * @param fplbbalance
	 * @param jllonb
	 * @param jllat
	 * @param jlradiu
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void notOldData(QueryCriteria criteria, int intfiletime, String strPlb, List<Map> finalList, int intcityid,
			String yearMonth, List<Map> jlList, double fplbbalance, double jllonb, double jllat, double jlradiu) {

		// 取集合内优先级最高的集合类型
		String jhType = getJhlbqzMax(jlList);
		String strNum = String.format("%06d", getNum(task, logNum, conn));
		String vcjlplbnum = yearMonth + "_" + strNum;

		double flat = 0;
		double flonb = 0;
		double fjlbalance = 0;
		for (int p = 0; p < jlList.size(); p++) {
			Map jlMap = new HashMap();
			jlMap.put("vcjllb", criteria.get("jllb").toString());
			fjlbalance = new BigDecimal(
					fplbbalance * Double.parseDouble(String.valueOf(jhlbjzMap.get(jhType).toString())))
							.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			jlMap.put("fjlbalance", fjlbalance);
			jlMap.put("fplbbalance",
					new BigDecimal(Double.parseDouble(jlList.get(p).get("fplbbalance").toString()) / fplbbalance)
							.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			jlMap.put("intjltype", Integer.parseInt(intjlTypeMap.get(jhType).toString()));
			jlMap.put("intjlcount", jlList.size());
			jlMap.put("fjlradiu", jlradiu);
			jlMap.put("fjllonb", jllonb);
			jlMap.put("fjllat", jllat);
			// 20161211 新增聚合分类
			jlMap.put("vcjhplb", jhType);
			jlMap.put("vcjlplbnum", strPlb + jhType + "_" + vcjlplbnum);
			jlMap.put("vccgi", jlList.get(p).get("vccellname").toString());
			flonb = Double.parseDouble(jlList.get(p).get("flonb").toString());
			flat = Double.parseDouble(jlList.get(p).get("flat").toString());
			jlMap.put("flonb", flonb);
			jlMap.put("flat", flat);
			if (jlList.get(p).get("vcproblemnum") != null) {
				jlMap.put("vcproblemnum", jlList.get(p).get("vcproblemnum").toString());
			} else {
				jlMap.put("vcproblemnum", "");
			}
			jlMap.put("intdateid", Integer.parseInt(jlList.get(p).get("intdateid").toString()));
			jlMap.put("intfiletime", intfiletime);
			jlMap.put("vcplbtype", jlList.get(p).get("vcplbtype").toString());
			jlMap.put("intplbtype", Integer.parseInt(jlList.get(p).get("intplbtype").toString()));
			if (jlList.get(p).get("vcgridid") != null) {
				jlMap.put("vcgridid", jlList.get(p).get("vcgridid").toString());
			} else {
				jlMap.put("vcgridid", "0");
			}
			jlMap.put("intcityid", intcityid);
			jlMap.put("vccoverscenes", "");
			jlMap.put("vcvaluetag", "");
			jlMap.put("vcspecialtag", "");
			finalList.add(jlMap);
		}

	}

	/**
	 * 存在历史数据,需要新旧匹配一次
	 * @param criteria
	 * @param intfiletime
	 * @param strPlb
	 * @param yearMonth
	 * @param finalList
	 * @param intcityid
	 * @param oldJlList
	 * @param jlList
	 * @param fplbbalance
	 * @param jllonb
	 * @param jllat
	 * @param jlradiu
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private void oldMateNew(QueryCriteria criteria, int intfiletime, String strPlb, String yearMonth,
			List<Map> finalList, int intcityid, List<Map<String, Object>> oldJlList, List<Map> jlList,
			double fplbbalance, double jllonb, double jllat, double jlradiu) {

		double foldlonb = 0;
		double foldlat = 0;
		double fnewlonb = 0;
		double fnewlat = 0;
		double foldradiu = 0;
		double fnewjlradiu = 0;
		String newjlplbnum = "";
		String vcjhplb = "";
		int intjltype = 0;
		int jlcount = 0;
		double fjlbalance = 0;

		// 匹配未关闭派单的问题点
		for (int p = 0; p < oldJlList.size(); p++) {
			foldlat = Double.parseDouble(oldJlList.get(p).get("fjllat").toString());
			foldlonb = Double.parseDouble(oldJlList.get(p).get("fjllonb").toString());
			foldradiu = Double.parseDouble(oldJlList.get(p).get("fjlradiu").toString());
			double distance = distance(foldlat, foldlonb, jllat, jllonb);
			if (distance <= 50) {
				fnewlat = new BigDecimal((foldlat + jllat) / 2).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
				fnewlonb = new BigDecimal((foldlonb + jllonb) / 2).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
				fnewjlradiu = new BigDecimal((jlradiu + foldradiu + distance) / 2).setScale(2, BigDecimal.ROUND_HALF_UP)
						.doubleValue();
				newjlplbnum = oldJlList.get(p).get("vcjlplbnum").toString();
				jlcount = Integer.parseInt(oldJlList.get(p).get("intjlcount").toString());
				intjltype = Integer.parseInt(oldJlList.get(p).get("intjltype").toString());
				fjlbalance = Double.parseDouble(oldJlList.get(p).get("fjlbalance").toString());

			}
		}

		if (jlcount > 0) {

			// 匹配上旧数据，把聚好类的数据放进结果集
			oldData(criteria, intfiletime, finalList, intcityid, jlList, fnewlonb, fnewlat, fnewjlradiu, newjlplbnum,
					vcjhplb, intjltype, jlcount, fjlbalance, fplbbalance);
		} else {

			// 没有旧数据，把聚好类的数据放进结果集
			notOldData(criteria, intfiletime, strPlb, finalList, intcityid, yearMonth, jlList, fplbbalance, jllonb,
					jllat, jlradiu);
		}
	}

	/**
	 * 匹配上旧数据，把聚好类的数据放进结果集
	 * @param criteria
	 * @param intfiletime
	 * @param finalList
	 * @param intcityid
	 * @param jlList
	 * @param fnewlonb
	 * @param fnewlat
	 * @param fnewjlradiu
	 * @param newjlplbnum
	 * @param vcjhplb
	 * @param intjltype
	 * @param jlcount
	 * @param fjlbalance
	 * @param fplbbalance
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void oldData(QueryCriteria criteria, int intfiletime, List<Map> finalList, int intcityid, List<Map> jlList,
			double fnewlonb, double fnewlat, double fnewjlradiu, String newjlplbnum, String vcjhplb, int intjltype,
			int jlcount, double fjlbalance, double fplbbalance) {

		for (int p = 0; p < jlList.size(); p++) {
			Map jlMap = new HashMap();
			jlMap.put("vcjllb", criteria.get("jllb").toString());
			jlMap.put("fjlbalance", fjlbalance);
			jlMap.put("fplbbalance",
					new BigDecimal(Double.parseDouble(jlList.get(p).get("fplbbalance").toString()) / fplbbalance)
							.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			jlMap.put("intjltype", intjltype);
			jlMap.put("intjlcount", jlcount + jlList.size());
			jlMap.put("fjlradiu", fnewjlradiu);
			jlMap.put("fjllonb", fnewlonb);
			jlMap.put("fjllat", fnewlat);
			// 20161211 新增聚合分类
			jlMap.put("vcjhplb", vcjhplb);
			jlMap.put("vcjlplbnum", newjlplbnum);
			jlMap.put("vccgi", jlList.get(p).get("vccellname").toString());
			double flonb = Double.parseDouble(jlList.get(p).get("flonb").toString());
			double flat = Double.parseDouble(jlList.get(p).get("flat").toString());
			jlMap.put("flonb", flonb);
			jlMap.put("flat", flat);
			if (jlList.get(p).get("vcproblemnum") != null) {
				jlMap.put("vcproblemnum", jlList.get(p).get("vcproblemnum").toString());
			} else {
				jlMap.put("vcproblemnum", "");
			}
			jlMap.put("intdateid", Integer.parseInt(jlList.get(p).get("intdateid").toString()));
			jlMap.put("intfiletime", intfiletime);
			jlMap.put("vcplbtype", jlList.get(p).get("vcplbtype").toString());
			jlMap.put("intplbtype", Integer.parseInt(jlList.get(p).get("intplbtype").toString()));
			if (jlList.get(p).get("vcgridid") != null) {
				jlMap.put("vcgridid", jlList.get(p).get("vcgridid").toString());
			} else {
				jlMap.put("vcgridid", "0");
			}
			jlMap.put("intcityid", intcityid);
			jlMap.put("vccoverscenes", "");
			jlMap.put("vcvaluetag", "");
			jlMap.put("vcspecialtag", "");
			finalList.add(jlMap);
		}
	}

	/**
	 * 筛选数据
	 * @param minDistance
	 * @param jlList
	 * @param fplbbalance
	 * @param tList
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private double filtrateData(Double minDistance, List<Map> jlList, double fplbbalance, List<Map> tList) {

		List<Map> tempList = new ArrayList();// 筛选用的
		double lat1 = Double.parseDouble(jlList.get(0).get("flat").toString());
		double lonb1 = Double.parseDouble(jlList.get(0).get("flonb").toString());
		String proname1 = jlList.get(0).get("vcproblemnum").toString();
		String proname2 = "";
		String proname3 = "";
		for (int i = 0; i < jlList.size(); i++) {
			tempList = new ArrayList();
			for (int j = 0; j < tList.size(); j++) {
				double lat2 = Double.parseDouble(tList.get(j).get("flat").toString());
				double lonb2 = Double.parseDouble(tList.get(j).get("flonb").toString());
				double distance = distance(lat1, lonb1, lat2, lonb2);
				proname2 = tList.get(j).get("vcproblemnum").toString();
				if (distance <= minDistance && !proname2.equals(proname1)) {
					tempList.add(tList.get(j));
					tList.remove(j);
				}
			}
			for (int j = 0; j < tempList.size(); j++) {
				double lat2 = Double.parseDouble(tempList.get(j).get("flat").toString());
				double lonb2 = Double.parseDouble(tempList.get(j).get("flonb").toString());
				proname2 = tempList.get(j).get("vcproblemnum").toString();
				for (int p = 0; p < tList.size(); p++) {
					double lat3 = Double.parseDouble(tList.get(p).get("flat").toString());
					double lonb3 = Double.parseDouble(tList.get(p).get("flonb").toString());
					double distance = distance(lat2, lonb2, lat3, lonb3);
					proname3 = tList.get(p).get("vcproblemnum").toString();
					if (distance <= minDistance && !proname3.equals(proname2)) {
						tempList.add(tList.get(p));
						tList.remove(p);
					}
				}
			}
			for (int j = 0; j < tempList.size(); j++) {
				fplbbalance = fplbbalance + Double.parseDouble(tempList.get(j).get("fplbbalance").toString());
				jlList.add(tempList.get(j));
			}
		}
		return fplbbalance;
	}

	/**
	 * 获取距离范围内的所有数据
	 * @param list 所有数据集合
	 * @param maxDistance 距离
	 * @param tList 范围数据集合
	 */
	@SuppressWarnings("rawtypes")
	private void getScopeData(List<Map> list, Double maxDistance, List<Map> tList) {
		for (int i = 1; i < list.size(); i++) {
			double lat1 = Double.parseDouble(list.get(0).get("flat").toString());
			double lonb1 = Double.parseDouble(list.get(0).get("flonb").toString());
			double lat2 = Double.parseDouble(list.get(i).get("flat").toString());
			double lonb2 = Double.parseDouble(list.get(i).get("flonb").toString());
			double distance = distance(lat1, lonb1, lat2, lonb2);
			if (distance <= maxDistance) {
				tList.add(list.get(i));
			}
		}
	}

	/**
	 * 小区聚类
	 * @param list
	 * @param criteria
	 * @param strPlb
	 * @param finalList
	 * @param intcityid
	 * @param oldJlList
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void xqJl(List<Map> list, QueryCriteria criteria, String strPlb, List<Map> finalList, int intcityid,
			List<Map<String, Object>> oldJlList) {

		int intfiletime = Integer.parseInt(criteria.get("intfiletime").toString());
		String yearMonth = criteria.get("yearMonth").toString();

		// 优先处理历史数据成HASHMAP加快匹配数据
		Map<String, Map<String, Object>> oldMaps = new HashMap<String, Map<String, Object>>();
		for (Map<String, Object> map : oldJlList) {
			oldMaps.put(map.get("vccgi").toString(), map);
		}

		// 开始遍历集合进行聚类
		while (list.size() > 0) {
			List<Map> jlList = new ArrayList();// 存放同一聚类的点，计算经纬度等
			jlList.add(list.get(0));
			double fplbbalance = Double.parseDouble(list.get(0).get("fplbbalance").toString());
			double fjlbalance = 0;

			// 只要vccgi相同就属于同一个聚类
			for (int i = 1; i < list.size(); i++) {
				if (list.get(i).get("vccgi").equals(jlList.get(0).get("vccgi"))) {
					jlList.add(list.get(i));
					fplbbalance = fplbbalance + Double.parseDouble(list.get(i).get("fplbbalance").toString());
				}
			}

			if (jlList.size() > 0) {

				// ============================================ 分割线Start
				// ===================================================
				// 遍历聚类问题点与未关闭派单的聚类，判断是否符合条件
				if (oldMaps.size() > 0) {
					String newjlplbnum = "";
					String vcjhplb = "";
					int jlcount = 0;
					int intjltype = 0;

					// 在历史中有相同的小区
					Map<String, Object> oldMap = oldMaps.get(jlList.get(0).get("vccgi").toString());
					if (oldMap != null) {
						newjlplbnum = oldMap.get("vcjlplbnum").toString();
						jlcount = Integer.parseInt(oldMap.get("intjlcount").toString());
						intjltype = Integer.parseInt(oldMap.get("intjltype").toString());
						fjlbalance = Double.parseDouble(oldMap.get("fjlbalance").toString());
					}
					if (jlcount > 0) {

						// 有小区历史数据,并且匹配上了,把本次聚类结果放进结果集
						oldXqData(criteria, finalList, intcityid, intfiletime, jlList, newjlplbnum, vcjhplb, jlcount,
								intjltype, fjlbalance, fplbbalance);
					} else {

						// 有小区历史数据,但是没有匹配上,把本次聚类结果放进结果集
						notOldXqData(criteria, strPlb, finalList, intcityid, intfiletime, yearMonth, jlList,
								fplbbalance);
					}
				} else {
					// ============================================ 分割线End
					// =====================================================

					// 没有历史小区数据,把本次聚类结果放进结果集
					notOldXqData(criteria, strPlb, finalList, intcityid, intfiletime, yearMonth, jlList, fplbbalance);
				}
			}
			// lsjList中删除聚类的点或者没用的点
			if (jlList.size() != 0) {
				list.removeAll(jlList);
			} else {
				list.remove(0);
			}

		}
	}

	/**
	 * 有小区历史数据,并且匹配上了,把本次聚类结果放进结果集
	 * @param criteria
	 * @param finalList
	 * @param intcityid
	 * @param intfiletime
	 * @param jlList
	 * @param newjlplbnum
	 * @param vcjhplb
	 * @param jlcount
	 * @param intjltype
	 * @param fjlbalance
	 * @param fplbbalance
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void oldXqData(QueryCriteria criteria, List<Map> finalList, int intcityid, int intfiletime,
			List<Map> jlList, String newjlplbnum, String vcjhplb, int jlcount, int intjltype, double fjlbalance,
			double fplbbalance) {
		double flat = 0;
		double flonb = 0;
		String jlcgi = "";

		for (int p = 0; p < jlList.size(); p++) {
			jlcgi = jlList.get(p).get("vccgi").toString();
			flonb = Double.parseDouble(jlList.get(p).get("flonb").toString());
			flat = Double.parseDouble(jlList.get(p).get("flat").toString());
			Map jlMap = new HashMap();
			jlMap.put("vcjllb", criteria.get("jllb").toString());
			jlMap.put("fjlbalance", fjlbalance);
			jlMap.put("fplbbalance",
					new BigDecimal(Double.parseDouble(jlList.get(p).get("fplbbalance").toString()) / fplbbalance)
							.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			jlMap.put("intjltype", intjltype);
			jlMap.put("intjlcount", jlcount + jlList.size());
			jlMap.put("fjlradiu", 0);
			jlMap.put("fjllonb", flonb);
			jlMap.put("fjllat", flat);
			// 20161211 新增聚合分类
			jlMap.put("vcjhplb", vcjhplb);
			jlMap.put("vcjlplbnum", newjlplbnum);
			jlMap.put("vccgi", jlcgi);
			flonb = Double.parseDouble(jlList.get(p).get("flonb").toString());
			flat = Double.parseDouble(jlList.get(p).get("flat").toString());
			jlMap.put("flonb", flonb);
			jlMap.put("flat", flat);
			if (jlList.get(p).get("vcproblemnum") != null) {
				jlMap.put("vcproblemnum", jlList.get(p).get("vcproblemnum").toString());
			} else {
				jlMap.put("vcproblemnum", "");
			}
			jlMap.put("intdateid", Integer.parseInt(jlList.get(p).get("intdateid").toString()));
			jlMap.put("intfiletime", intfiletime);
			jlMap.put("vcplbtype", jlList.get(p).get("vcplbtype").toString());
			jlMap.put("intplbtype", Integer.parseInt(jlList.get(p).get("intplbtype").toString()));
			if (jlList.get(p).get("vcgridid") != null) {
				jlMap.put("vcgridid", jlList.get(p).get("vcgridid").toString());
			} else {
				jlMap.put("vcgridid", "0");
			}
			jlMap.put("intcityid", intcityid);
			if(!"".equals(jlList.get(p).get("vccoverscenes"))&&jlList.get(p).get("vccoverscenes")!=null) {
				jlMap.put("vccoverscenes", jlList.get(p).get("vccoverscenes").toString());
			}else{
				jlMap.put("vccoverscenes", "");
			}
			if(null!=jlList.get(p).get("vcvaluetag")&&!jlList.get(p).get("vcvaluetag").equals("")) {
				jlMap.put("vcvaluetag", jlList.get(p).get("vcvaluetag").toString());
			}else{
				jlMap.put("vcvaluetag", "");
			}
			if(null!=jlList.get(p).get("vcspecialtag")&&!jlList.get(p).get("vcspecialtag").equals("")) {
				jlMap.put("vcspecialtag", jlList.get(p).get("vcspecialtag").toString());
			}else{
				jlMap.put("vcspecialtag", "");
			}
			finalList.add(jlMap);
		}
	}

	/**
	 * 没有小区的历史数据,把本次聚类结果放进结果集
	 * @param criteria
	 * @param strPlb
	 * @param finalList
	 * @param intcityid
	 * @param intfiletime
	 * @param yearMonth
	 * @param jlList
	 * @param fplbbalance
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void notOldXqData(QueryCriteria criteria, String strPlb, List<Map> finalList, int intcityid,
			int intfiletime, String yearMonth, List<Map> jlList, double fplbbalance) {

		String vcjlplbnum;
		String jlcgi;
		String strNum;
		double flat;
		double flonb;
		// 取集合内优先级最高的集合类型
		String jhType = getJhlbqzMax(jlList);
		strNum = String.format("%06d", getNum(task, logNum, conn));
		vcjlplbnum = yearMonth + "_" + strNum;
		double fjlbalance = 0;

		for (int p = 0; p < jlList.size(); p++) {
			jlcgi = jlList.get(p).get("vccgi").toString();
			flonb = Double.parseDouble(jlList.get(p).get("flonb").toString());
			flat = Double.parseDouble(jlList.get(p).get("flat").toString());
			Map jlMap = new HashMap();
			jlMap.put("vcjllb", criteria.get("jllb").toString());
			fjlbalance = new BigDecimal(
					fplbbalance * Double.parseDouble(String.valueOf(jhlbjzMap.get(jhType).toString())))
							.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			jlMap.put("fjlbalance", fjlbalance);
			jlMap.put("fplbbalance",
					new BigDecimal(Double.parseDouble(jlList.get(p).get("fplbbalance").toString()) / fplbbalance)
							.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			jlMap.put("intjltype", Integer.parseInt(intjlTypeMap.get(jhType).toString()));
			jlMap.put("intjlcount", jlList.size());
			jlMap.put("fjlradiu", 0);
			jlMap.put("fjllonb", flonb);
			jlMap.put("fjllat", flat);
			// 20161211 新增聚合分类
			jlMap.put("vcjhplb", jhType);
			jlMap.put("vcjlplbnum", strPlb + jhType + "_" + vcjlplbnum);
			jlMap.put("vccgi", jlcgi);
			flonb = Double.parseDouble(jlList.get(p).get("flonb").toString());
			flat = Double.parseDouble(jlList.get(p).get("flat").toString());
			jlMap.put("flonb", flonb);
			jlMap.put("flat", flat);
			if (jlList.get(p).get("vcproblemnum") != null) {
				jlMap.put("vcproblemnum", jlList.get(p).get("vcproblemnum").toString());
			} else {
				jlMap.put("vcproblemnum", "");
			}
			jlMap.put("intdateid", Integer.parseInt(jlList.get(p).get("intdateid").toString()));
			jlMap.put("intfiletime", intfiletime);
			jlMap.put("vcplbtype", jlList.get(p).get("vcplbtype").toString());
			jlMap.put("intplbtype", Integer.parseInt(jlList.get(p).get("intplbtype").toString()));
			if (jlList.get(p).get("vcgridid") != null) {
				jlMap.put("vcgridid", jlList.get(p).get("vcgridid").toString());
			} else {
				jlMap.put("vcgridid", "0");
			}
			jlMap.put("intcityid", intcityid);
			if(!"".equals(jlList.get(p).get("vccoverscenes"))&&jlList.get(p).get("vccoverscenes")!=null) {
				jlMap.put("vccoverscenes", jlList.get(p).get("vccoverscenes").toString());
			}else{
				jlMap.put("vccoverscenes", "");
			}
			if(null!=jlList.get(p).get("vcvaluetag")&&!jlList.get(p).get("vcvaluetag").equals("")) {
				jlMap.put("vcvaluetag", jlList.get(p).get("vcvaluetag").toString());
			}else{
				jlMap.put("vcvaluetag", "");
			}
			if(null!=jlList.get(p).get("vcspecialtag")&&!jlList.get(p).get("vcspecialtag").equals("")) {
				jlMap.put("vcspecialtag", jlList.get(p).get("vcspecialtag").toString());
			}else{
				jlMap.put("vcspecialtag", "");
			}
			finalList.add(jlMap);
		}
	}

	// 获取本次聚合优先级最高的聚合类别返回
	@SuppressWarnings("rawtypes")
	public String getJhlbqzMax(List<Map> jlList) {

		if (jlList == null || jlList.size() == 0) {
			return null;
		}

		// 遍历集合取最高优先级的聚合类型
		String jllb = "";
		int num = 50;
		int count = 0;
		for (Map map : jlList) {
			if (count++ == 0) {
				jllb = map.get("vcjhplb").toString();
			}
			int nowNum = jhlbqzMap.get(map.get("vcjhplb").toString());
			if (nowNum < num) {
				jllb = map.get("vcjhplb").toString();
				num = nowNum;
			}
		}
		return jllb;
	}

	/**
	 * 聚类方法
	 * @param criteria
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	public List<Map> getJlList(QueryCriteria criteria) throws IOException, SQLException {

		// 初始化聚合类别权重
		initJllb();

		// 外部表名
		extTableName = criteria.get("tableName").toString();

		// 城市名称
		criteria.put("citycode", cityNameMap.get(criteria.get("city").toString()));

		// 根据分类查询需要聚类的数据,并进行聚类后返回结果集
		List<Map> jlList = null;
		try {
			jlList = queryJlDatas(criteria);
		} catch (SQLException e) {
			logger.error("聚类出错：", e);
			logger.info(e.getMessage());
			logger.info(e.getNextException().getMessage());
			throw new SQLException(e);
		}

		// 将聚类结果写入到结果表,目前使用外部表
		insertDatas(jlList, criteria);

		return jlList;
	}

	/**
	 * 以外部表的形式插入聚类结果数据
	 * @param dataList 聚类的结果集
	 * @param criteria 基础信息对象
	 * @throws IOException 
	 * @throws SQLException 
	 */
	@SuppressWarnings("rawtypes")
	private void insertDatas(List<Map> dataList, QueryCriteria criteria) throws IOException, SQLException {

		if (dataList == null || dataList.size() == 0) {
			task.addDetaileLog(logNum++, "工单《" + criteria.get("orderCode") + "》未聚类到有效的问题点...");
			return;
		}

		String ordercode = criteria.get("orderCode").toString();
		task.addDetaileLog(logNum++, "工单《" + ordercode + "》共聚类到[" + dataList.size() + "]的问题点...");

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
			task.execute(logNum++, conn, insertDataSql, ordercode);
			String insertOptimizationDataSql = sqlMap.get("insertOptimizationData").replace("${tableName}", tableName);
			task.execute(logNum++, conn, insertOptimizationDataSql, ordercode);

		} catch (Exception e) {

			task.addDetaileLog(logNum++, "工单《" + ordercode + "》聚类结果通过外部表入库时出错:" + e.getLocalizedMessage());

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
		String fileName = "/edmp_jl_" + nowTimeStr + ".txt";
		String filePath = task.getParpams().get("loadpath") + fileName;
		File file = new File(filePath);
		FileWriter out = null;
		try {

			// 处理文件,若已经存在,则更换文件名
			file = createFile(file);
			out = new FileWriter(file);
			StringBuilder sb = new StringBuilder();
			for (Map map : dataList) {
				Object obj = map.get("fjlbalance");
				obj = obj == null || "".equals(obj.toString()) || "null".equals(obj.toString()) ? 0 : obj;
				sb.append(map.get("intdateid")).append("|").append(map.get("intjltype")).append("|")
						.append(map.get("intjlcount")).append("|").append(map.get("fjlradiu")).append("|")
						.append(map.get("fjllonb")).append("|").append(map.get("fjllat")).append("|")
						.append(map.get("flonb")).append("|").append(map.get("flat")).append("|")
						.append(map.get("vcproblemnum")).append("|").append(map.get("vcplbtype")).append("|")
						.append(map.get("intplbtype")).append("|").append(map.get("intcityid")).append("|")
						.append(map.get("vcgridid")).append("|").append(map.get("vcjlplbnum")).append("|")
						.append(map.get("vccgi")).append("|").append(map.get("intfiletime")).append("|").append(obj)
						.append("|").append(map.get("fplbbalance")).append("|").append(map.get("vcjllb"))
						.append("|").append(map.get("vccoverscenes")).append("|").append(map.get("vcvaluetag"))
						.append("|").append(map.get("vcspecialtag"));
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
			String pathname = task.getParpams().get("loadpath") + "/edmp_jl_" + str + ".txt";
			file = new File(pathname);
			return createFile(file);
		} else {
			file.createNewFile();
			return file;
		}

	}

	/**
	 * 根据分类查询需要聚类的数据,并进行聚类后返回结果集
	 * @param criteria 基础信息对象
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	private List<Map> queryJlDatas(QueryCriteria criteria) throws SQLException {

		// 定义最终聚类集合
		List<Map> jlList = new ArrayList<Map>();

		// 聚类方式,目前只有两种:1.按经纬度聚类。2.按小区聚类
		// 20170301 聚类方式变更,信令数据与非信令数据区分开来聚类
		String[] jllbS = { "jwd", "xljwd", "xq", "xlxq" };
		List<Map> lsjList = new ArrayList<Map>();
		for (String lb : jllbS) {
			lsjList = new ArrayList<Map>();
			String dutygrid = criteria.get("dutygrid").toString();

			criteria.put("jllb", lb);
			// 经纬度聚类
			if (lb.equals("jwd")) {
				//20170830删除该问题点聚类
				//lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getLjgdList")));
				List<Map<String, Object>> selectMap = task.selectMap(logNum++, conn, getQuerySql("getLsjList"));
				lsjList.addAll(selectMap);
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getLyyList")));
				lsjList.addAll(task.selectMap(conn, getQuerySql("getVlrtoList")));
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getOzcList")));
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getDlfgList")));
				task.addDetaileLog(logNum++,
						"工单《" + criteria.get("orderCode") + "》文件共匹配出[" + lsjList.size() + "]非信令经纬度问题点数据...");
			}

			// 信令经纬度聚类
			if (lb.equals("xljwd")&&!dutygrid.equals("")&&dutygrid!=null) {
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getHYDlgrxlList"), criteria.get("citycode"),
						criteria.get("citycode"),dutygrid));
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getHYDlfgxlList"), criteria.get("citycode"),
						criteria.get("citycode"),dutygrid));
				task.addDetaileLog(logNum++,
						"工单《" + criteria.get("orderCode") + "》文件共匹配出[" + lsjList.size() + "]信令经纬度问题点数据...");
			}else if(lb.equals("xljwd")&&(dutygrid.equals("")||dutygrid==null)){
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getDlgrxlList"), criteria.get("citycode"),
						criteria.get("citycode")));
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getDlfgxlList"), criteria.get("citycode"),
						criteria.get("citycode")));
				task.addDetaileLog(logNum++,
						"工单《" + criteria.get("orderCode") + "》文件共匹配出[" + lsjList.size() + "]信令经纬度问题点数据...");
			}

			// 小区聚类
			if (lb.equals("xq")) {
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getSdfgmrList")));
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getLgrList")));
				/*
				 * lsjList.addAll(task .selectMap(conn,
				 * getQuerySql("getWgzbgList")));
				 */
                //20170628新增LTE切换差小区
                //20170706新增高流量问题严重小区
				lsjList.addAll(task.selectMap(conn, getQuerySql("getWgzblList")));
				//20170830删除该问题点聚类
				//lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getLjgcList")));
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getLrlList")));
				lsjList.addAll(task.selectMap(conn, getQuerySql("getEsvhoList")));
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getOflList")));
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getIflList")));
				// 20170502补回QCI2及视频问题点-20170830删除
				//lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getQci2List")));
				//20170830删除该问题点聚类
				//lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getErabSpList")));
				//20170830删除该问题点聚类
				//lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getVdjtSpList")));

				// 20170503新增投诉点问题点聚类-20170720删除该问题点聚类
				//lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getTs4grfgxqzzList")));

				// 20170619新增两个指标问题点(VoLTE上行高丢包、Volte下行高丢包)
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getVshgdbList")));
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getVxhgdbList")));

				//20171009新增集中投诉问题点
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getYhtsList"), criteria.get("citycode"),dutygrid));

				task.addDetaileLog(logNum++,
						"工单《" + criteria.get("orderCode") + "》文件共匹配出[" + lsjList.size() + "]非信令小区问题点数据...");
			}

			// 信令小区聚类
			if (lb.equals("xlxq")&&!dutygrid.equals("")&&dutygrid!=null) {
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getHYSdfgxlList"), criteria.get("citycode"),dutygrid));
				task.addDetaileLog(logNum++,
						"工单《" + criteria.get("orderCode") + "》文件共匹配出[" + lsjList.size() + "]信令小区问题点数据...");
			}else if(lb.equals("xlxq")&&(dutygrid.equals("")||dutygrid==null)){
				lsjList.addAll(task.selectMap(logNum++, conn, getQuerySql("getSdfgxlList"), criteria.get("citycode")));
				task.addDetaileLog(logNum++,
						"工单《" + criteria.get("orderCode") + "》文件共匹配出[" + lsjList.size() + "]信令小区问题点数据...");
			}

			// 开始调用聚类方法
			jlList.addAll(jlTempList(lsjList, criteria));
		}

		// 20170504新增投诉_LTE补覆盖问题点，不需要聚类，直接放进结果-20170720删除该问题点
		//jlList.addAll(task.selectMap(logNum++, conn, getQuerySql("getTsb4gfgList"), criteria.get("intfiletime")));

		return jlList;
	}

	/**
	 * 获取查询sql语句
	 * @param tableName 工单文件外部表名
	 * @param queryKey 查询语句key值
	 * @return
	 */
	public String getQuerySql(String queryKey) {

		// 获取sql语句并替换外部表名
		String sql = sqlMap.get(queryKey);
		sql = sql.replace(ProblemUtil.ORDERTABLENAME_STR, newTabelname);

		return sql;
	}

	/**
	 * 以工单文件名为条件,删除以前聚类过的数据
	 * @param order_code 工单编号
	 * @throws SQLException
	 */
	public void deleteData(String order_code) throws SQLException {

		// 执行删除语句
		task.execute(logNum++, conn, sqlMap.get("deleteData"), order_code);
		// 20170306先去掉删除opt表,删不删不影响,但删除会对表有一点的效率影响,所以先不删除
		/*
		 * task.execute(logNum++, conn, sqlMap.get("deleteOptimizationData"),
		 * order_code);
		 */

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

	/**
	 * 聚类自增编号
	 */
	public static long num = -1;

	/**
	 * 获取自增序列号
	 * @return
	 */
	public static synchronized long getNum(ProblemTask task, int logNum, Connection conn) {
		try {
			if (num == -1) {
				num = task.selectOne(logNum++, conn, long.class, "select nextval('problem_num_seq') num");
				task.addDetaileLog(logNum, "本次聚类,自增序列号起始为：【" + num + "】");
			} else {
				num++;
			}

			return num;
		} catch (Exception e) {

			// 这里应该改成查一次，接着再代码里面自增，完了之后再设置回去
			task.addDetaileLog(logNum++, "获取自增序号出错：" + e.getMessage());

		}
		return 0;
	}

	/**
	 * 处理外部表
	 * @param extPath 外部表文件路径
	 * @param tableName 外部表名
	 * @throws SQLException
	 */
	public void processorExtData(String extPath, String tableName) throws SQLException {

		// DROP外部表
		String dropTableSql = sqlMap.get("deleteExtTable") + " " + tableName;
		task.execute(logNum++, conn, dropTableSql);

		// 删除外部表数据文件
		if (extPath != null) {
			File file = new File(extPath);
			file.delete();
		}
	}
}
