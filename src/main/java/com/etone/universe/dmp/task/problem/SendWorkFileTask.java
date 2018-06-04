package com.etone.universe.dmp.task.problem;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.etone.universe.dmp.problem.GenProposalProcessor;
import com.etone.universe.dmp.problem.QueryCriteria;
import com.etone.universe.dmp.problem.SendWorkFile;
import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 工单文件派单
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年12月5日  上午10:10:46
 */
public class SendWorkFileTask extends ProblemTask {

	// 日志步骤号,用于线程里面,防止日志错乱
	private int logNum = 300;

	// 高速工单号
	private String gaosuOrderCode = null;

	@Override
	public void execute() {

		// 初始化日志信息
		event.initialize(this);

		// 设置日志信息
		addDetaileLog("本次工单派单环节开始...");

		//###20170210临时处理测试派单
		temporaryWork();

		// 需要聚类的工单文件若没有工单文件,则直接记录日志
		if (ProblemUtil.paramMap == null || ProblemUtil.paramMap.size() < 1) {
			addDetaileLog("没有在缓存对象中找到相应的工单文件数据...");
		} else {

			try {

				// 1.同步数据
				genProcessor();

				// 2.创建线程池,默认最大并发数为2
				int threadNum = parpams.get("threadnum") == null
						|| "".equals(parpams.get("threadnum")) ? 2 : Integer
						.valueOf(parpams.get("threadnum"));

				ExecutorService executor = Executors
						.newFixedThreadPool(threadNum);

				// 3.将每个工单的线程放进线程池里面
				for (String order_code : ProblemUtil.paramMap.keySet()) {

					// 设置参数信息
					QueryCriteria criteria = new QueryCriteria();
					setParpam(criteria, order_code);

					// 创建聚类线程
					SendWorkFile cluster = new SendWorkFile(logNum, sqlMap,
							this, criteria);
					executor.execute(cluster);
					logNum = logNum + 300;

				}

				// 4.启动线程池
				executor.shutdown();

				// 5.等待派单完成
				while (!executor.isTerminated()) {

				}

				// 6.更新最高权重字段(20170413)
				execute(conn, sqlMap.get("updateMaxJlType"));

			} catch (Exception e) {

				e.printStackTrace();
				addDetaileLog("工单派单出错：" + e.getMessage());
				event.setException(e.getMessage());
				event.setVcstatus(ProblemUtil.STATUS_ER);
				this.setException(e.getMessage());
				// 出错置空流程参数
				ProblemUtil.paramMap = new HashMap<String, Map<String, String>>();

			} finally {

				// 无论是否成功,都要置空参数
				clearParam();
			}
		}

		// 设置日志信息
		addDetaileLog("本次工单派单环节结束...");

		// 派单完成
		super.execute();
	}

	/**
	 * 无论是否成功,都要置空参数
	 */
	private void clearParam() {
		Map<String, Map<String, String>> parmMap = ProblemUtil.paramMap;
		for (String key : parmMap.keySet()) {
			Map<String, String> map = parmMap.get(key);
			String tableName = map.get("tableName");
			String extPath = map.get("extFilePath");
			try {
				processorExtData(extPath, tableName);
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("移除外部表出错：", e);
			}
		}
		ProblemUtil.paramMap = new HashMap<String, Map<String, String>>();
	}

	/**
	 * 20170210临时处理测试派单
	 */
	private void temporaryWork() {
		if (ProblemUtil.paramMap == null || ProblemUtil.paramMap.size() == 0) {
			try {
				List<Map<String, Object>> workList = selectMap(conn,
						sqlMap.get("queryWorkJob"));
				for (Map<String, Object> map : workList) {
					// 将合格的文件存放进去缓存里面(静态变量),方便下一个流程调用,这里要确保任务是串行的
					Map<String, String> oederCodeMap = new HashMap<String, String>();
					String orderCode = map.get("order_code").toString();
					String workPath = map.get("file_path").toString();
					workPath = workPath.split(",")[0].toString();

					// 取得文件名到下载后的目录里面取该文件
					String newFile = getFilePath() + "/"
							+ workPath.substring(workPath.lastIndexOf("/") + 1);
					File jobFile = new File(newFile);

					// 创建外部表数据
					ProblemUtil.processorJobFile(jobFile, oederCodeMap, this,
							sqlMap.get("rkhcExtTable"));
					ProblemUtil.paramMap.put(orderCode, oederCodeMap);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 同步数据
	 * @throws Exception 
	 * 
	 */
	private void genProcessor() throws Exception {

		GenProposalProcessor genpp = new GenProposalProcessor(conn, sqlMap,
				this);

		// 同步出错置空流程参数,并删除外部表
		if (!genpp.genProposal()) {

			// 遍历删除外部表
			for (String order_code : ProblemUtil.paramMap.keySet()) {
				Map<String, String> workMap = ProblemUtil.paramMap
						.get(order_code);
				String tableName = workMap.get("tableName");
				String extPath = workMap.get("extFilePath");
				processorExtData(extPath, tableName);
			}

			// 置空流程参数
			ProblemUtil.paramMap = new HashMap<String, Map<String, String>>();
		}

	}

	/**
	 * 设置基础参数
	 * @param criteria 参数对象
	 * @param order_code 工单编号
	 */
	private void setParpam(QueryCriteria criteria, String order_code) {

		Map<String, String> workMap = ProblemUtil.paramMap.get(order_code);
		String tableName = workMap.get("tableName");
		String extPath = workMap.get("extFilePath");
		criteria.put("rkhcTablename", tableName);
		criteria.put("rkhcExtPath", extPath);
		criteria.put("orderCode", order_code);
		criteria.put("tableName", tableName);
		criteria.put("problemCount", workMap.get("problemCount"));
		criteria.put("gaosuOrderCode", gaosuOrderCode);
	}

}
