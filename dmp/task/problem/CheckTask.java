package com.etone.universe.dmp.task.problem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 工单数据核查入库数据
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年12月6日  上午11:11:35
 */
public class CheckTask extends ProblemTask {

	@Override
	public void execute() {

		// 初始化日志信息
		event.initialize(this);

		// 设置日志信息
		addDetaileLog("本次工单核查入库数据环节开始...");

		// 需要聚类的工单文件若没有工单文件,则直接记录日志
		if (ProblemUtil.paramMap == null || ProblemUtil.paramMap.size() < 1) {

			// 记录详细日志
			addDetaileLog("没有在缓存对象中找到相应的工单文件数据...");

		} else {

			// 记录要移除的key
			List<String> removeKeys = new ArrayList<String>();

			try {
				for (String key : ProblemUtil.paramMap.keySet()) {

					// 获取工单文件
					Map<String, String> workMap = ProblemUtil.paramMap.get(key);
					String jobfilePath = workMap.get("filePath");
					File jobFile = new File(jobfilePath);

					// 记录详细日志
					addDetaileLog("工单《" + key + "》开始核查入库数据...");

					// 开始核查工单数据
					String tableName = checkJobFileData(jobFile, workMap);
					if (tableName != null) {

						// 设置流程状态1
						setJudgeKey(ProblemUtil.JUDGE_ONE);
						// 将表名绑定到工单信息里面,方便派单完毕后删除
						workMap.put("tableName", tableName);

					} else {

						// 核查不通过,标记流程为异常
						event.setVcstatus(ProblemUtil.STATUS_ER);
						// 移除核查不通过的工单
						removeKeys.add(key);

					}

					// 记录详细日志
					addDetaileLog("工单《" + key + "》核查入库数据结束...");
				}

				for (String key : removeKeys) {
					ProblemUtil.paramMap.remove(key);
				}

			} catch (Exception e) {
				event.setException("核查工单数据出错：" + e.getMessage());
				addDetaileLog("核查工单数据出错：" + e.getMessage());
				event.setVcstatus(ProblemUtil.STATUS_ER);
				this.setException(e.getMessage());
				// 出错置空流程参数
				ProblemUtil.paramMap = new HashMap<String, Map<String, String>>();
			}

		}

		// 设置日志信息
		addDetaileLog("本次工单核查入库数据环节结束...");

		// 调度指定job
		super.execute();
	}

	/**
	 * 核查工单数据匹配入库数据,
	 * 1.若核查都通过,则本次数据是完整的。
	 * 2.若核查不通过,则入库源数据不完整,要么提供的源数据文件不完整,要么数据没有完整入库。
	 * @param jobFile 工单文件
	 * @param workMap 工单参数
	 * @return 表名
	 * @throws IOException 
	 * @throws SQLException 
	 */
	@SuppressWarnings("unchecked")
	private String checkJobFileData(File jobFile, Map<String, String> workMap)
			throws IOException, SQLException {

		// 创建唯一的表名
		String tableName = ProblemUtil.EXT_EDMP_TABLENAME
				+ System.currentTimeMillis();

		try {
			// 循环遍历工单文件及shee,一个工单的所有sheet放到同一个目录一次性校验
			Map<String, String> nullMap = processorJobFile(jobFile, workMap);

			// 创建GP外部表语句
			String extPath = getExternalPath() + workMap.get("extFileName");
			String createTbaleSql = sqlMap.get("createExtTable")
					.replace("${tableName}", tableName)
					.replace("${externalPathStr}", extPath);

			// 外部表核查工单数据语句
			String checkSql = sqlMap.get("checkData").replace("${tableName2}",
					tableName);

			// 创建外部表
			this.execute(conn, createTbaleSql);

			// 40多张表数据需要检查,把需要检查的表配置到xml里面
			Map<String, String> nameMap = ProblemUtil.getIdNames(namesFileName,
					"sheeNames");
			for (String name : nameMap.keySet()) {
				if (nullMap.get(name) != null) {
					logger.info("sheet《" + name + "》数据为空,不用核查...");
					continue;
				}
				String checkSqlNew = checkSql.replace("${tableName1}",
						nameMap.get(name));
				List<String> problemnums = this.select(conn, String.class,
						checkSqlNew, name);

				// 存在匹配不上的问题点编号
				if (problemnums != null && problemnums.size() > 0) {
					StringBuilder sBuilder = new StringBuilder();
					sBuilder.append("核查工单《" + jobFile.getName()
							+ "》数据不通过，以下问题点编号未在入库表中找到：");
					for (String num : problemnums) {
						sBuilder.append(num).append(",");
					}
					addDetaileLog(sBuilder.toString());
					// 处理外部表
					processorExtData(workMap.get("extFilePath"), tableName);
					return null;
				}
			}
		} catch (Exception e) {

			logger.error("核查工单数据出错：", e);
			addDetaileLog("核查工单数据出错：" + e.getMessage());

			// 处理外部表
			processorExtData(workMap.get("extFilePath"), tableName);
			return null;
		}

		addDetaileLog("核查工单《" + jobFile.getName() + "》数据通过...");

		return tableName;
	}

	/**
	 * 循环遍历工单文件及shee,一个工单的所有sheet放到同一个目录一次性校验
	 * @param jobFile 工单文件
	 * @param workMap 工单流程参数对象
	 * @return 返回为空的sheet集合
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String> processorJobFile(File jobFile,
			Map<String, String> workMap) throws IOException {

		// 写文件流
		FileOutputStream fw = null;
		BufferedWriter bw = null;
		File txtFile = null;
		String jobName = jobFile.getName();
		String txtName = ProblemUtil.EXT_EDMP_TABLENAME
				+ System.currentTimeMillis() + ProblemUtil.FILE_TXT;
		String txtPath = getParpams().get("loadpath") + ProblemUtil.FILE_P
				+ txtName;

		// 记录空sheet
		Map<String, String> nullMap = new HashMap<String, String>();

		try {

			// 设置日志信息
			addDetaileLog("工单:《" + jobName + "》转txt开始...");

			InputStream is = new FileInputStream(jobFile);
			XSSFWorkbook hssfWorkbook = new XSSFWorkbook(is);

			// 创建外部表TXT文件
			txtFile = new File(txtPath);
			if (txtFile.exists()) {
				txtFile.createNewFile();
			}

			fw = new FileOutputStream(txtFile);
			bw = new BufferedWriter(new OutputStreamWriter(fw, "UTF-8"));
			addDetaileLog("工单《" + jobName + "》生成的txt文件：《" + txtPath + "》...");

			// 获取配置的工单核查sheet名称,遍历每个sheet
			Map<String, String> nameMap = ProblemUtil.getIdNames(namesFileName,
					"sheeNames");
			// 处理sheet
			int problemCount = ProblemUtil.proSheet(bw, nullMap, hssfWorkbook, nameMap);
			workMap.put("problemCount", problemCount+"");
		} catch (Exception e) {
			e.printStackTrace();
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

		addDetaileLog("工单:《" + jobName + "》转txt完成...");

		return nullMap;

	}

}
