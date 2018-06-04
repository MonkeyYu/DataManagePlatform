package com.etone.universe.dmp.task.problem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.etone.universe.dmp.problem.ClusterProcessor;
import com.etone.universe.dmp.problem.QueryCriteria;
import com.etone.universe.dmp.util.ProblemUtil;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 问题点数据聚类处理
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年12月5日  上午10:00:52
 */
public class ClusterTask extends ProblemTask {

	private static final int FIRST_DAY = Calendar.MONDAY;

	// 日志步骤号,用于线程里面,防止日志错乱
	private int logNum = 300;

	@Override
	public void execute() {

		// 初始化日志信息
		event.initialize(this);

		// 初始化参数
		initParam();

		// 设置日志信息
		addDetaileLog("本次问题点聚类环节开始...");

		// 获取上一个环节(工单核查数据环节),有效的工单文件
		if (ProblemUtil.paramMap == null || ProblemUtil.paramMap.size() == 0) {
			addDetaileLog("没有在缓存对象中找到相应的工单文件数据...");
		} else {

			try {

				// 1.初始化序列号
				ClusterProcessor.num = -1;
				List<Map<String, Object>> seqList = selectMap(conn, sqlMap.get("getDataList"), "%_" + yearMonth + "_%");
				if (seqList == null || seqList.size() == 0) {
					selectMap(conn, sqlMap.get("resetProblemSeq"));
				}

				// 2.创建线程池,默认最大并发数为5
				int threadNum = parpams.get("threadnum") == null || "".equals(parpams.get("threadnum")) ? 5
						: Integer.valueOf(parpams.get("threadnum"));

				ExecutorService executor = Executors.newFixedThreadPool(threadNum);

				// 3.将每个工单的线程放进线程池里面
				for (String order_code : ProblemUtil.paramMap.keySet()) {

					// 设置参数信息
					QueryCriteria criteria = new QueryCriteria();
					setParpam(criteria, order_code);

					// 创建聚类线程
					ClusterProcessor cluster = new ClusterProcessor(logNum, sqlMap, this, criteria);
					executor.execute(cluster);
					logNum = logNum + 300;
				}

				// 4.启动线程池
				executor.shutdown();

				// 5.等待聚类完成
				while (!executor.isTerminated()) {

				}

				// 6.设置回序列号
				setNum(ClusterProcessor.num);

				if (ProblemUtil.paramMap.size() > 0) {
					// 设置流程状态
					setJudgeKey(ProblemUtil.JUDGE_ONE);

					// 调用过程前,先处理问题点指标
					processorProblemTarget();

					// 存在有效的工单,则需要调用存储过程
					callFunction();
				} else {
					addDetaileLog("本次问题点聚类没有聚类到有效的工单文件... ");
				}

			} catch (Exception e) {

				e.printStackTrace();
				logger.error("问题点聚类出错：", e);
				addDetaileLog(logNum++, "问题点聚类出错：" + e.getMessage());
				event.setException(e.getMessage());
				event.setVcstatus(ProblemUtil.STATUS_ER);
				this.setException(e.getMessage());

				// 遍历删除外部表
				for (String order_code : ProblemUtil.paramMap.keySet()) {
					Map<String, String> workMap = ProblemUtil.paramMap.get(order_code);
					String tableName = workMap.get("tableName");
					String extPath = workMap.get("extFilePath");
					try {
						processorExtData(extPath, tableName);
					} catch (SQLException e1) {
						e1.printStackTrace();
						logger.error("移除外部表出错！", e);
					}
				}

				// 出错置空流程参数
				ProblemUtil.paramMap = new HashMap<String, Map<String, String>>();

			}
		}

		// 设置详细日志
		addDetaileLog(logNum++, "本次问题点聚类环节结束...");

		// 调度指定job
		super.execute();
	}

	/**
	 * 处理问题点指标字段
	 * @throws SQLException 
	 */
	@SuppressWarnings("unchecked")
	private void processorProblemTarget() throws SQLException {
		Map<String, String> ptargetMap = ProblemUtil.getIdNames(getNamesFileName(), "ptargetname");
		String sql = "";
		String field2 = "vcproblemnum2";
		for (String key : ptargetMap.keySet()) {
			field2 = "vcproblemnum2";
			if (key.startsWith("yt_")||key.startsWith("yy_")) {
				field2 = "vcproblemnum";
			}
			sql = "update f_et_plb_result_new t set vcproblemtarget=" + ptargetMap.get(key) + " from " + key
					+ " t1 where t.vcproblemtarget is null and t1." + field2 + "=t.vcproblemnum";
			execute(conn, sql);
		}
		// 同步更新opt表指标字段
		execute(conn, sqlMap.get("updateProblemTarget"));
	}

	/**
	 * 回写新的自增序列号
	 * @param num 要回写的序列号
	 */
	private void setNum(long num) {
		try {
			logger.info("回写的自增序列号为:[" + num + "]");
			if (num != -1) {
				num = selectOne(logNum++, conn, long.class, "select setval('problem_num_seq'," + num + ",true) num");
			}
		} catch (Exception e) {

			// 这里应该改成查一次，接着再代码里面自增，完了之后再设置回去
			addDetaileLog(logNum++, "回写新的自增序列号出错：" + e.getMessage());

		}
	}

	/**
	 * 调用存储过程
	 * @throws SQLException 
	 */
	private void callFunction() throws SQLException {

		// 获取所有需要处理的工单编号
		Map<String, Map<String, String>> paramMap = ProblemUtil.paramMap;

		Set<String> keySet = paramMap.keySet();
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("'");
		for (String order_code : keySet) {
			sBuffer.append("''" + order_code + "''").append(",");
		}
		String orders = sBuffer.substring(0, sBuffer.length() - 1) + "'";

		// 按顺序执行8步分析法存储过程
		callFunction(sqlMap.get("znfx_gjfx"), orders);
		callFunction(sqlMap.get("znfx_csfx"), orders);
		callFunction(sqlMap.get("znfx_lqfx"), orders);
		callFunction(sqlMap.get("znfx_zyfx"), orders);
		// 干扰分析增加时间限定
		String grfxsql = sqlMap.get("znfx_grfx");
		callFunction(grfxsql, orders);
		callFunction(sqlMap.get("znfx_lqdfx"), orders);
		callFunction(sqlMap.get("znfx_fgfx"), orders);
		callFunction(sqlMap.get("znfx_counterfx"), orders);
		callFunction(sqlMap.get("znfx_sjtxds"), orders);
		callFunction(sqlMap.get("order_temp"), orders);

	}

	/**
	 * 调用过程,超时时间默认为1小时
	 * @param sql 调用过程语句
	 * @param orders 工单编号
	 * @throws SQLException 
	 */
	public void callFunction(String sql, String orders) throws SQLException {
		sql = sql.replace("${orders}", orders);
		addDetaileLog(logNum++, "执行[" + sql + "]过程...");
		CallableStatement prepareCall = conn.prepareCall(sql);
		// 获取配置的超时时间,若没有配置,则默认3600
		String timeout = getParpams().get("timeout");
		timeout = timeout == null || "".equals(timeout) ? "3600" : timeout;
		prepareCall.setQueryTimeout(Integer.valueOf(timeout));
		prepareCall.execute();
		prepareCall.close();
	}

	private Date startDate = null;
	private String yearMonth = null;
	private int startfix = 0;

	/**
	 * 初始化参数
	 */
	private void initParam() {

		// 获取当前月份
		startDate = printWeekdays();
		yearMonth = new SimpleDateFormat("yyyyMM").format(startDate);

		// 本周周三
		startfix = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(startDate));

	}

	/**
	 * 设置基础参数
	 * @param criteria 参数对象
	 * @param order_code 工单编号
	 */
	private void setParpam(QueryCriteria criteria, String order_code) {

		// 当前月份
		criteria.put("yearMonth", yearMonth);

		// 最小最大距离
		criteria.put("minDistance", 50);
		criteria.put("maxDistance", 1000);

		// 时间为本周周三
		criteria.put("intfiletime", startfix);

		Map<String, String> workMap = ProblemUtil.paramMap.get(order_code);
		String tableName = workMap.get("tableName");
		String extPath = workMap.get("extFilePath");
		criteria.put("rkhcTablename", tableName);
		criteria.put("rkhcExtPath", extPath);
		criteria.put("orderCode", order_code);
		criteria.put("tableName", tableName);
		criteria.put("city", workMap.get("city").toString());
		criteria.put("dutygrid",workMap.get("dutygrid").toString());
	}

	/**
	 * 获取本周周三时间
	 * @return
	 */
	private static Date printWeekdays() {
		Calendar calendar = Calendar.getInstance();
		while (calendar.get(Calendar.DAY_OF_WEEK) != FIRST_DAY) {
			calendar.add(Calendar.DATE, -1);
		}
		calendar.add(Calendar.DATE, 2);
		return calendar.getTime();
	}

//	public static void main(String args[]) throws IOException {
//		String str="LTE,GSM河源市责任网格河源东源县指标分析1+N_2017/8/17 16:13:13|LTE,GSM河源市责任网格河源东源县指标分析1+N";
//		System.out.println(str.split("责任网格")[1].split("指标分析")[0].toString());
//		System.out.println(str.indexOf("责任网格")>0);
//		int p=0;
//		for(int i=0;i<3;i++){
//			for(int j=0;j<5;j++){
//				p=i+j;
//				System.out.print("i:"+i);
//				System.out.print("j:"+j);
//				System.out.println("p:"+p);
//				if(p%3==0){
//					break;
//				}
//			}
//		}
//	}

}
