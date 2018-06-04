package com.etone.universe.dmp.task.problem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 扫描工单文件夹
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年11月25日  上午11:19:04
 */
public class WorkFileTask extends ProblemTask {

	/**
	 * 扫描成功的日志信息
	 */
	private StringBuilder suBuilder = new StringBuilder();

	@Override
	public void execute() {

		// 初始化日志信息
		event.initialize(this);

		// 设置日志信息
		addDetaileLog("本次工单文件扫描环节开始...");

		// 判断环节是否存在正在执行的线程,如果存在则跳出
		if (ProblemUtil.paramMap.size() > 0) {

			addDetaileLog("问题点聚类存在正在执行的线程,第一个参数为:"
					+ ProblemUtil.paramMap.keySet().iterator().next());

		} else {

			// 记录要插入日志的下标
			int index = detailLogs.size() + 1;

			try {
				// 查询数据库获取工单文件记录,其中未派单的则为需要处理的
				List<Map<String, Object>> workList = this.selectMap(conn,
						sqlMap.get("queryWorkJob"));

				if (workList.size() <= 0) {
					addDetaileLog("本次扫描未发现工单文件...");
				} else {
					addDetaileLog("本次扫描工单文件入下：");

					for (Map<String, Object> map : workList) {

						// 处理工单
						processorWorkFile(map);

					}

					// 设置日志信息
					processorLog();
					addDetaileLog(logInfoStr.toString(), index);

					// 是否存在合规的工单文件
					if (!getJudgeKey().equals(ProblemUtil.JUDGE_ONE)) {
						addDetaileLog("本次扫描没有合规的工单文件...");
					}
				}

			} catch (Exception e) {
				addDetaileLog("扫描工单文件错误：" + e.getLocalizedMessage());
				event.setException("扫描工单文件错误...");
				event.setVcstatus(ProblemUtil.STATUS_ER);
				this.setException(e.getMessage());
				// 出错置空流程参数
				ProblemUtil.paramMap = new HashMap<String, Map<String, String>>();
			}
		}

		// 设置日志信息
		addDetaileLog("本次工单扫描环节结束...");

		// 调度指定job
		super.execute();

	}

	/**
	 * 处理工单
	 * @param map
	 * @throws IOException
	 */
	private void processorWorkFile(Map<String, Object> map) throws IOException {

		String workPath = map.get("file_path").toString();
		String title = map.get("title").toString();
		String dutygrid = "";
		if(title.indexOf("责任网格")>0) {
			dutygrid=title.split("责任网格")[1].split("指标分析")[0].toString();
		}

		// 可能有多个工单
//		for (String filePath : workPath.split(",")) {
		String filePath =  workPath.split(",")[0].toString();
			try {
				logger.info("=====================" + filePath);
				// 取得文件名到下载后的目录里面取该文件
				String newFile = getFilePath() + "/"
						+ filePath.substring(filePath.lastIndexOf("/") + 1);

				// 扫描工单文件夹,若存在 文件,则说明有工单文件需要处理
				String orderCode = map.get("order_code").toString();
				File jobFiles = new File(newFile);
				addDetaileLog("开始核查工单文件《" + jobFiles.getName() + "》格式...");

				// 核查工单sheet名称
				if (checkFileName(jobFiles, orderCode)) {

					// 设置流程状态为1,标识有正常的工单文件,转至核查工单数据job
					setJudgeKey(ProblemUtil.JUDGE_ONE);

					// 将合格的文件存放进去缓存里面(静态变量),方便下一个流程调用,这里要确保任务是串行的
					Map<String, String> oederCodeMap = new HashMap<String, String>();
					oederCodeMap.put("filePath", newFile);
					oederCodeMap.put("fileName", jobFiles.getName());
					oederCodeMap.put("city", map.get("city").toString());
					oederCodeMap.put("dutygrid", dutygrid);
					ProblemUtil.paramMap.put(orderCode, oederCodeMap);
				}
				addDetaileLog("核查工单文件《" + jobFiles.getName() + "》格式结束...");
			} catch (Exception e) {
				logger.error("核查工单《" + filePath + "》格式错误：", e);
			}

//		}
	}

	/**
	 * 设置日志信息
	 */
	private void processorLog() {

		// 判断是否存在错误工单
		if (logInfoStr.length() > 0) {
			event.setVcstatus(ProblemUtil.STATUS_ER);
			String suStr = suBuilder.insert(0, "本次扫描工单文件如下：").append(".其中：")
					.toString();
			logInfoStr.insert(0, suStr).append("工单格式核查不通过...");
		} else {
			logInfoStr = suBuilder.insert(0, "本次扫描工单文件如下：");
		}
	}

	/**
	 * 核查工单文件名及sheet名称是否符合规范
	 * @param jobFile
	 * @param orderCode
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private boolean checkFileName(File jobFile, String orderCode)
			throws IOException {

		InputStream is = null;
		// 是否正常
		boolean isSu = true;
		try {

			// 获取配置的工单核查sheet名称
			Map<String, String> nameMap = ProblemUtil.getIdNames(namesFileName,
					"sheeNames");

			// 创建工单文件对象
			is = new FileInputStream(jobFile);
			Workbook hssfWorkbook = new XSSFWorkbook(is);

			// 遍历每个sheet
			Map<String, String> sheetMap = new HashMap<String, String>();
			for (int i = 0; i < hssfWorkbook.getNumberOfSheets(); i++) {
				String sheetName = hssfWorkbook.getSheetName(i);
				sheetMap.put(sheetName, sheetName);
			}

			// 核对配置文件里面的sheet名称是否都存在这个工单里面
			for (String key : nameMap.keySet()) {
				if (sheetMap.get(key) == null) {
					// sheet名称匹配不上的,记录错误日志
					addDetaileLog("核查工单文件《" + jobFile.getName() + "》的sheet名称《"
							+ key + "》未匹配上...");
					isSu = false;
					break;
				}
			}

			suBuilder.append("《").append(jobFile.getName()).append("》");

		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (is != null) {
				is.close();
			}
		}
		// 工单文件核查通过
		if (isSu) {
			// 设置日志信息
			addDetaileLog("核查工单文件《" + jobFile.getName() + "》合规...");
			return true;
		} else {
			addDetaileLog("核查工单文件《" + jobFile.getName() + "》不合规...");
			return false;
		}

	}

}
