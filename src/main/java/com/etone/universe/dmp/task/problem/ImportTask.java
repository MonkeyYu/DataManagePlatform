package com.etone.universe.dmp.task.problem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.etone.universe.dmp.problem.ImportProcessor;
import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 数据分发文件入库
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年11月25日  上午11:49:11
 */
public class ImportTask extends ProblemTask {

	private List<Map<String, Object>> configList = new ArrayList<Map<String, Object>>();

	@Override
	public void execute() {

		// 初始化日志信息
		event.initialize(this);

		try {

			// 设置日志信息
			addDetaileLog("本次扫描入库文件夹环节开始...");

			// 查询数据库数据表信息
			configList = this.selectMap(conn,
					sqlMap.get(ProblemUtil.CONFIG_STR));

			// 查看是否存在需要入库的文件，过滤.csv文件
			File rkFiles = new File(getFtpPath());
			if (rkFiles.list(CSV).length > 0) {

				addDetaileLog("本次入库文件如下：");
				int index = detailLogs.size() + 1;
				StringBuilder sBuilder = new StringBuilder();

				ImportProcessor importPro = new ImportProcessor(conn, sqlMap,
						this);

				// 有需要入库文件，调用文件入库方法
				for (File file : rkFiles.listFiles(CSV)) {

					// 设置日志信息
					addDetaileLog("文件：《" + file.getName() + "》开始入库...");
					sBuilder.append("《").append(file.getName()).append("》");

					try {
						// 调用文件入库方法
						importPro.checkAndInsert(file, configList);

					} catch (Exception e) {
						addDetaileLog("文件：《" + file.getName() + "》入库错误:"
								+ e.getMessage());
					}

					// 设置日志信息
					addDetaileLog("文件：《" + file.getName() + "》入库结束...");
				}

				// 先聚类再派单
				setJudgeKey(ProblemUtil.JUDGE_ONE);
				addDetaileLog("本次入库文件如下：" + sBuilder.toString(), index);
				logInfoStr.append("本次入库文件如下：" + sBuilder.toString());

			} else {

				// 没有需要入库的数据,直接派单
				setJudgeKey(ProblemUtil.JUDGE_ZERO);
				logInfoStr.append("本次扫描未发需要入库的文件！");
				addDetaileLog("本次扫描未发需要入库的文件...");
			}

		} catch (Exception e) {
			addDetaileLog("数据入库错误：" + e.getMessage());
			event.setException(e.getMessage());
			event.setVcstatus(ProblemUtil.STATUS_ER);
			this.setException(e.getMessage());
		}

		// 设置日志信息
		addDetaileLog("本次扫描入库文件夹环节结束...");

		// 调度指定job
		super.execute();

	}

}
