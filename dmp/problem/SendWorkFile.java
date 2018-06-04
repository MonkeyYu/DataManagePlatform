package com.etone.universe.dmp.problem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.daemon.db.DB;
import com.etone.universe.dmp.task.problem.ProblemTask;
import com.etone.universe.dmp.util.Common;
import com.etone.universe.dmp.util.FtpToolkit;
import com.etone.universe.dmp.util.ProblemUtil;
import com.etone.universe.dmp.util.VarLteProposalConstants;

/**
 * 工单派发处理类
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年12月27日  下午4:54:05
 */
public class SendWorkFile implements Runnable {

	public SendWorkFile(int logNum, Map<String, String> sqlMap, ProblemTask task, QueryCriteria criteria) {

		this.logNum = logNum;
		this.sqlMap = sqlMap;
		this.task = task;
		this.criteria = criteria;
	}

	@Override
	public void run() {

		// 工单编号
		String orderCode = criteria.get("orderCode").toString();
		task.addDetaileLog(logNum++, "工单[" + orderCode + "]派单开始...");
		try {

			// 获取链接
			this.conn = DB.getDataSource(task.getDataSource()).getConnection();

			// 调用派单方法z
			pushHandleFile2(orderCode);

			// 更新工单编号
			task.execute(conn, sqlMap.get("updateNewOrderCode"), orderCode,orderCode,orderCode);

			// 删除全网指标问题点汇总表本工单记录
			task.execute(conn, sqlMap.get("deletecountqwzb"), orderCode);

			// 全网指标问题点汇总表
			String sql = sqlMap.get("countqwzb").replace("${tablename}", criteria.get("rkhcTablename").toString());
			task.execute(conn, sql, orderCode, orderCode);

		} catch (Exception e) {
			e.printStackTrace();
			task.addDetaileLog(logNum++, "工单[" + orderCode + "]派单出错：" + e.getMessage());

			// 移除出错的工单
			ProblemUtil.paramMap.remove(orderCode);
		} finally {
			// 聚完类移除外部表跟文件
			try {
				processorExtData(criteria.get("rkhcExtPath").toString(), criteria.get("rkhcTablename").toString());
			} catch (SQLException e) {
				e.printStackTrace();
				task.addDetaileLog(logNum++, "移除入库核查外部表出错" + e.getMessage());
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

		task.addDetaileLog(logNum++, "工单[" + orderCode + "]派单结束...");
	}

	private static final Logger logger = LoggerFactory.getLogger(SendWorkFile.class);

	/**
	 * 参数对象
	 */
	private QueryCriteria criteria = null;

	/**
	 * 日志步骤
	 */
	private int logNum = 0;

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
	 * 工单派单方法
	 * @param orderCode 工单编号
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void pushHandleFile2(String orderCode) throws Exception {
		String resultInfo = "<rtInfo><rtCode>code</rtCode><rtMessage>message</rtMessage></rtInfo>";
		String resultCode = "000";
		String resultMessage = "执行命令成功!";
		String filePaths = "";
		String cluster_codes = "";
		String files = "";
		String dutygrid = "";
		QueryCriteria criteria = new QueryCriteria();
		try {
			List<Map<String, Object>> list = task.selectMap(conn, sqlMap.get("queryNotPushOrderFile2"), orderCode);
			if (list != null && !list.isEmpty()) {
				Map map = list.get(0);
				String handle_state = String.valueOf(map.get("handle_state"));
				if (!handle_state.equals("未派单")) {
					task.addDetaileLog(logNum++, "只有未派单状态的工单才能派单");
					return;
				}
				String title = map.get("title").toString();
				if(title.indexOf("责任网格")>0) {
					dutygrid=title.split("责任网格")[1].split("指标分析")[0].toString();
				}
				filePaths = String.valueOf(map.get("file_path"));
				filePaths = filePaths.split(",")[0].toString();
				orderCode = String.valueOf(map.get("order_code"));
				criteria.put("order_code", orderCode);
				criteria.put("dutygrid", dutygrid);

				// 路径要修改成再xml配置"/opt/ltemr/apache-tomcat-7.0.55-8980/webapps/ltemr/file/upload"
				String filepath = queryRealPath();
				files = filepath + "/" + orderCode + "_" + System.currentTimeMillis();
				// 删除此工单信息
				removeAllQuestionByCode(criteria);

				if (Common.judgeString(filePaths)) {
					new File(files).mkdir();
					resultMessage = updateExcel(filePaths, orderCode, files);
					String[] resultMessageArr = resultMessage.split("\\|");
					int questionCount = 0;
					int clusterCount = 0;
					if (resultMessageArr != null && resultMessageArr.length == 2) {
						clusterCount = resultMessageArr[0].split(",").length;
						questionCount = resultMessageArr[1].split(",").length;
						cluster_codes = resultMessageArr[0];
						criteria.put("order_code", orderCode);
						criteria.put("handle_state", "初步方案制定");
						if (Common.judgeString(cluster_codes)) {
							criteria.put("cluster_count", clusterCount);
						} else {
							criteria.put("cluster_count", 0);
						}
						criteria.put("question_count", questionCount);
						criteria.put("title", map.get("title"));
						criteria.put("content", map.get("content"));
						criteria.put("create_date", map.get("create_date"));
						criteria.put("city", map.get("city"));
						criteria.put("dispatcher", map.get("dispatcher"));
						criteria.put("file_path", filePaths);
						criteria.put("WYId", orderCode);
						if (Common.judgeString(cluster_codes)) {
							List<String> clusterCodeList = new ArrayList(Arrays.asList(cluster_codes.split(",")));
							int num = 0;

							//20171012修改逻辑，原来是在232行的if里面
							// 处理信令问题点
							List<Map<String, Object>> maps = updateExcelbyXL(criteria, files, filePaths, orderCode);
							if (maps.size() > 0) {
								for (int i = 0; i < maps.size(); i++) {
									Map m = maps.get(i);
									if (m != null) {
										clusterCodeList.add(String.valueOf(m.get("聚类编号")));
									}
								}
							}
							//20171009新增投诉问题点
							List<Map<String,Object>> tsMaps = updateExcelbyTS(criteria,files,filePaths,orderCode);
							if (tsMaps.size() > 0) {
								for (int i = 0; i < tsMaps.size(); i++) {
									Map m = tsMaps.get(i);
									if (m != null) {
										clusterCodeList.add(String.valueOf(m.get("聚类编号")));
									}
								}
							}
							if (clusterCodeList != null && !clusterCodeList.isEmpty()) {

								criteria.put("order_code", orderCode);
								criteria.put("isThird", "true");
								HashSet<String> hs = new HashSet<String>(clusterCodeList);
								num = hs.size();
								criteria.put("clusterCodeList", hs);
								//task.execute(logNum++, conn, sqlMap.get("removeQuestionByCode"), orderCode);
								saveClusterQuestion(criteria, orderCode); // 聚类问题点写入工单号
																			// //插入此工单的所有聚类问题点

								long number = task.selectOne(logNum++, conn, long.class,
										sqlMap.get("queryClusterQuestionCount"), orderCode,orderCode,orderCode);
								/**  20170727聚类新规则，将此处注释
								if (num != number) {
									// lteDispatchManager.saveClusterandorder(criteria);###这里需要讨论
									resultCode = "001";
									resultMessage = "问题跟踪点表输出数量【" + number + "】与问题点文件数量【" + num + "】对不上，请重新派单";
									task.addDetaileLog(logNum++, resultMessage);
									throw new SQLException(resultMessage);
								}
								 */
							}

						}
						// 生成"1+N集中优化问题点跟踪模板"的excel
						String messge = genTrackTemplate(criteria, files);
						resultMessage = messge + "|" + resultMessage;
					} else {
						resultCode = "001";
					}
				}
			} else {
				task.addDetaileLog(logNum++, "根据工单编号[" + orderCode + "]未查询出有效的记录...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("工单派单出错：", e);
			task.addDetaileLog(logNum++, "工单派单出错：" + e.getMessage());
			resultCode = "001";
			resultMessage = e.getMessage();
			task.execute(logNum++, conn, sqlMap.get("updateFailureTime"), orderCode);
		} finally {
			long clusterCount = task.selectOne(logNum++, conn, long.class, sqlMap.get("queryClusterQuestionCount"),
					orderCode,orderCode,orderCode);
			criteria.put("cluster_count", clusterCount);
			long problemCount = task.selectOne(logNum++, conn, long.class, sqlMap.get("getProblemCountList"),orderCode);
			criteria.put("problemCount", problemCount);
			if ("000".equals(resultCode)) {
				if (!pushAddAttachment(orderCode, resultMessage, filePaths, clusterCount, files, criteria)) {
					// 删除此工单信息
					removeAllQuestionByCode(criteria);
					resultCode = "001";
					resultMessage = "追加附件推送失败!";
				} else {
					String question_type = task.selectOne(logNum++, conn, String.class,
							sqlMap.get("queryClusterTypeByClusterCode"), orderCode, orderCode);
					criteria.put("cluster_type", question_type);

					String sqlStr = "";
					Object cluster_count = criteria.get("cluster_count");
					if (!isNull(cluster_count)) {
						sqlStr += "cluster_count = '" + cluster_count + "',";
					}
					Object question_count = this.criteria.get("problemCount");
					if (!isNull(question_count)) {
						sqlStr += "question_count = '" + question_count + "',";
					}
					String updateOrderTrackInfoSql = sqlMap.get("updateOrderTrackInfo").replace("${sqlStr}", sqlStr);
					task.execute(logNum++, conn, updateOrderTrackInfoSql, criteria.get("title"),
							criteria.get("content"), criteria.get("create_date"), criteria.get("city"),
							criteria.get("handle_state"), criteria.get("dispatcher"), criteria.get("file_path"),
							criteria.get("WYId"));

					// 更新信令问题状态
					criteria.put("tableName1", "xl_temp_" + orderCode);
					criteria.put("order_code", orderCode);
					updateXLproInfo(criteria);

					task.execute(logNum++, conn, sqlMap.get("updatePlbOptimization"), orderCode);
				}
			} else {
				// 删除此工单信息
				removeAllQuestionByCode(criteria);
				task.execute(logNum++, conn, sqlMap.get("updateFailureTime"), orderCode);
			}
			resultInfo = resultInfo.replace("code", resultCode).replace("message", resultMessage);

			saveLog(filePaths, resultCode, resultMessage, resultInfo, orderCode);
		}
	}

	private String updateExcel(String filePaths, String ordercode, String files) throws Exception {

		org.apache.poi.ss.usermodel.Sheet sheet = null;
		String sheetName = "";
		String[] titleArr = null;
		OutputStream outputStream = null;
		InputStream inputStream = null;
		org.apache.poi.ss.usermodel.Workbook book = null;
		List<String> clusterCodeList = new ArrayList<String>();
		List<String> questionCodeList = new ArrayList<String>();
		List<String> codes = new ArrayList<String>();
		boolean flag = false;
		try {
			if (Common.judgeString(filePaths)) {
					String filePath=filePaths.split(",")[0].toString();
					logger.info("==================================" + filePath);
					String filepath = queryRealPath();
					String newFile = filepath + "/" + filePath.substring(filePath.lastIndexOf("/") + 1);
					if (inputStream != null) {
						inputStream.close();
					}
					if (outputStream != null) {
						outputStream.close();
					}
					inputStream = new FileInputStream(newFile);
					book = WorkbookFactory.create(inputStream);
					logger.info("=================create inputStream end====================");
					logger.info(filepath);
					newFile = files + "/" + "分析定位_" + newFile.substring(newFile.lastIndexOf("/") + 1);
					logger.info("newfile" + newFile);
					Common.mkdir(newFile);
					outputStream = new FileOutputStream(newFile);
					int numberOfSheets = book.getNumberOfSheets();
					logger.info("执行工单号:" + ordercode + "包含有" + numberOfSheets + "个shell");
					String errorinfo = "未聚类问题点:";
					String errorstr = "原始表不存在数据:";
					if (numberOfSheets > 0) {
						for (int i = 0; i < numberOfSheets; i++) {
							sheet = book.getSheetAt(i);
							sheetName = book.getSheetName(i);
							logger.info("正在执行工单号:" + ordercode + "共有" + numberOfSheets + "个shell，正在执行第" + i
									+ "个shell,名称为:" + sheetName);
							titleArr = VarLteProposalConstants.SHEET_TITLE_MAP.get(sheetName);
							if (Arrays.asList(VarLteProposalConstants.mt_sheet_name).contains(sheetName)) {
								flag = true;
								String message = validQuestionCodeTitle(sheet);
								if (Common.judgeString(message)) {
									return message;
								} else {

									codes = updateExcel(sheet, titleArr, book, ordercode);

									if (codes != null && !codes.isEmpty()) {
										if (codes.size() == 2) {
											clusterCodeList.addAll(Arrays.asList(codes.get(1).split(",")));
											questionCodeList.addAll(Arrays.asList(codes.get(0).split(",")));
										} else {
											errorinfo += codes.get(0) + ";";
											errorstr += codes.get(1);
											// return "excel里的‘" + sheetName +
											// "’sheet里的以下问题点不存在聚类问题点:" +
											// codes.get(0) + "!";
										}
									}
								}
							}
							// System.gc();
						}
						logger.info("开始处理信令sheet-----------------------");
						// 添加新的sheet
						// 创建单元格，并设置值表头 设置表头居中
						CellStyle style = book.createCellStyle();
						style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
						// 设置背景颜色
						style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
						style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
						style.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
						style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
						style.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
						style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
						style.setWrapText(true); // 设置自动换行
						String[] sheetnames = new String[] { "深度覆盖-信令", "高铁覆盖-信令", "道路干扰-信令", "道路覆盖-信令", "高铁干扰-信令", "用户投诉" };
						String[] sheetArt1 = new String[] { "聚类问题点编号", "编号", "城市", "经度", "纬度", "日期", "网格A", "网格B",
								"网格C", "楼宇名称", "问题类型", "楼宇ID", "楼宇采样点总数", "用户数", "楼宇平均RSRP", "楼宇覆盖率", "楼宇弱覆盖采样点数",
								"楼宇平均RSRQ", "楼宇干扰概率", "楼宇模三干扰采样点数", "楼宇平均CQI", "楼宇CQI低于6比例", "楼宇CQI低于6的采样点数",
								"楼宇平均上行SINR", "楼宇平均上行SINR低于0比例", "楼宇内上行SINR低于0采样点数", "楼宇覆盖空洞比例", "楼宇重叠覆盖比例", "主覆盖小区CGI",
								"主覆盖小区ID", "主覆盖小区名", "主覆盖小区采样点总数", "主覆盖小区平均rsrp", "问题小区ID", "问题小区名", "问题小区覆盖率",
								"问题小区弱覆盖采样点数", "问题小区平均TA", "问题小区平均PHR", "问题小区覆盖空洞", "问题小区重叠覆盖" };
						String[] sheetArt2 = new String[] { "聚类问题点编号", "编号ID", "地市", "经度", "纬度", "时间", "高铁名称", "高铁方向",
								"栅格名称", "问题类型", "栅格ID", "栅格采样点总数", "栅格用户数", "栅格平均RSRP", "栅格覆盖率", "栅格弱覆盖采样点数",
								"栅格平均RSRQ", "栅格干扰概率", "栅格模三干扰采样点数", "栅格平均CQI", "栅格CQI低于6比例", "栅格CQI低于6的采样点数",
								"栅格平均上行SINR", "栅格平均上行SINR低于0比例", "栅格内上行SINR低于0采样点数", "栅格覆盖空洞比例", "栅格重叠覆盖比例", "主覆盖小区CGI",
								"主覆盖小区号", "主覆盖小区名", "主覆盖小区采样点总数", "主覆盖小区平均RSRP", "问题小区号", "问题小区名", "问题小区覆盖率",
								"问题小区弱覆盖采样点数", "问题小区平均TA", "问题小区平均PHR", "问题小区覆盖空洞比例", "问题小区重叠覆盖比例" };
						String[] sheetArt3 = new String[] { "聚类问题点编号", "编号ID", "地市", "经度", "纬度", "时间", "网格A", "网格B",
								"网格C", "道路名称", "问题类型", "道路栅格ID", "栅格采样点总数", "栅格用户数", "栅格平均RSRP", "栅格覆盖率", "栅格弱覆盖采样点数",
								"栅格干扰概率", "栅格模三干扰采样点数", "主覆盖小区CGI", "主覆盖小区号", "主覆盖小区名", "主覆盖小区采样点总数", "主覆盖小区平均RSRP",
								"问题小区号", "问题小区名", "问题小区干扰概率", "问题小区模三干扰采样点数", "问题小区PCI", "干扰小区PCI", "干扰小区平均RSRP",
								"问题小区平均RSRP", "问题小区覆盖率", "问题小区平均TA", "问题小区平均PHR", "问题小区覆盖空洞比例", "问题小区重叠覆盖比例" };
						String[] sheetArt4 = new String[] { "聚类问题点编号", "编号ID", "地市", "经度", "纬度", "时间", "网格A", "网格B",
								"网格C", "道路名称", "问题类型", "道路栅格ID", "栅格采样点总数", "栅格用户数", "栅格平均RSRP", "栅格覆盖率", "栅格弱覆盖采样点数",
								"主覆盖小区CGI", "主覆盖小区号", "主覆盖小区名", "主覆盖小区采样点总数", "主覆盖小区平均RSRP", "问题小区号", "问题小区名",
								"问题小区覆盖率", "问题小区弱覆盖采样点数", "问题小区平均TA", "问题小区平均PHR", "问题小区覆盖空洞比例", "问题小区重叠覆盖比例" };
						String[] sheetArt5 = new String[] { "聚类问题点编号", "编号ID", "地市", "经度", "纬度", "时间", "高铁名称", "高铁方向",
								"栅格名称", "问题类型", "栅格ID", "栅格采样点总数", "栅格用户数", "栅格平均RSRP", "栅格覆盖率", "栅格弱覆盖采样点数",
								"栅格平均RSRQ", "栅格干扰概率", "栅格模三干扰采样点数", "栅格平均CQI", "栅格CQI低于6比例", "栅格CQI低于6的采样点数",
								"栅格平均上行SINR", "栅格平均上行SINR低于0比例", "栅格内上行SINR低于0采样点数", "栅格覆盖空洞比例", "栅格重叠覆盖比例", "主覆盖小区CGI",
								"主覆盖小区号", "主覆盖小区名", "主覆盖小区采样点总数", "主覆盖小区平均RSRP", "问题小区号", "问题小区名", "问题小区干扰概率",
								"问题小区模三干扰采样点数", "问题小区PCI", "干扰小区PCI", "干扰小区平均RSRP", "问题小区平均RSRP", "问题小区覆盖率", "问题小区平均TA",
								"问题小区平均PHR", "问题小区覆盖空洞比例", "问题小区重叠覆盖比例" };
						String[] sheetArt6 = new String[] { "聚类问题点编号", "问题点编号","流水号","投诉时间","地市","行政区","投诉业务","问题细项",
								"地址","网格类型","经度","纬度","网格ID","网格中文名称","网格投诉量","影响范围","归属网格名称"};
						logger.info("sheetnames.length" + sheetnames.length);
						for (int i = 0; i < sheetnames.length; i++) {
							sheet = book.createSheet(sheetnames[i]);
							Row row = sheet.createRow(0);
							String[] sheetArt = new String[] {};
							if (i == 0) {
								sheetArt = sheetArt1;
							} else if (i == 1) {
								sheetArt = sheetArt2;
							} else if (i == 2) {
								sheetArt = sheetArt3;
							} else if (i == 3) {
								sheetArt = sheetArt4;
							} else if (i == 4){
								sheetArt = sheetArt5;
							} else if (i == 5){
								sheetArt = sheetArt6;
							}
							for (int j = 0; j < sheetArt.length; j++) {
								// 设置列宽
								sheet.setColumnWidth(j, 5000);
								Cell cell = row.createCell(j);
								cell.setCellValue(sheetArt[j]);
								cell.setCellStyle(style);
							}

						}

					} else {
						return "excel里的sheet名称‘" + sheetName + "’不正确!";
					}
					logger.info("flag:---" + flag);
					if (!flag) {
						return "excel里的sheet名称‘" + sheetName + "’不正确!";
					}
					logger.info("文件输出:---");
					book.write(outputStream);
					logger.info("文件输出完成:---");
					if (!errorinfo.equals("未聚类问题点:")) {
						QueryCriteria criteria = new QueryCriteria();
						String nullvcjlplbnums = errorinfo.replace("未聚类问题点:", "").replace(";", ",");
						if (nullvcjlplbnums.length() > 0) {
							String[] list = nullvcjlplbnums.split(",");
							int len = (list.length + 999) / 1000;
							List<List<String>> questionCodes = new ArrayList<List<String>>();
							for (int i = 0; i < len; i++) {
								questionCodes.add(new ArrayList<String>());
							}
							for (int i = 0; i < len - 1; i++) {
								for (int j = 0; j < 1000; j++) {
									String code = list[(i * 1000) + j];
									if (code != null && code != "") {
										String city = code.split("-")[0];
										String type = code.split("-")[1];
										String time = code.split("-")[2];
										String tables = FindTableName(code);
										questionCodes.get(i).add("'" + ordercode + "','" + code + "','" + tables
												+ "_month','" + city + "','" + type + "'," + time);
									}
								}
							}
							for (int i = 0; i < list.length % 1000; i++) {
								String code = list[((len - 1) * 1000) + i];
								if (code != null && code != "") {
									String city = code.split("-")[0];
									String type = code.split("-")[1];
									String time = code.split("-")[2];
									String tables = FindTableName(code);
									questionCodes.get(len - 1).add("'" + ordercode + "','" + code + "','" + tables
											+ "_month','" + city + "','" + type + "'," + time);
								}
							}

							StringBuilder sBuilder = new StringBuilder();
							String saveCjnullnumberSql = sqlMap.get("saveCjnullnumber");
							for (int i = 0; i < questionCodes.size(); i++) {
								criteria.put("listnumber", questionCodes.get(i));
								sBuilder.append(saveCjnullnumberSql);
								for (String itemStr : questionCodes.get(i)) {
									sBuilder.append("(").append(itemStr).append(",date_trunc('second', now()),0),");
								}
								task.execute(logNum++, conn, sBuilder.substring(0, sBuilder.length() - 1));
								sBuilder.delete(0, sBuilder.length());
							} // 分段批量更新

							if (!errorstr.equals("原始表不存在数据:")) {
								String nullcode = errorstr.replace("原始表不存在数据:", "").replace(";", ",").replace(",,", ",")
										.replace(" ", "").replace("[", "").replace("]", "").replace("''", "'");
								if (nullcode.length() > 0) {
									String[] list2 = nullcode.split(",");
									int ln = (list2.length + 999) / 1000;
									List<List<String>> state = new ArrayList<List<String>>();
									for (int i = 0; i < ln; i++) {
										state.add(new ArrayList<String>());
									}
									for (int i = 0; i < ln - 1; i++) {
										for (int j = 0; j < 1000; j++) {
											String code = list2[(i * 1000) + j];
											if (code != null && code != "") {
												state.get(i).add(code);
											}
										}
									}
									for (int i = 0; i < list2.length % 1000; i++) {
										String code = list2[((ln - 1) * 1000) + i];
										if (code != null && code != "") {
											state.get(ln - 1).add(code);
										}
									}

									StringBuilder sbBuilder = new StringBuilder();
									for (int i = 0; i < state.size(); i++) {
										logger.info(state.size() + ">>>>>>>>>>>>>>" + i);
										criteria.put("listnumber2", state.get(i));
										if (state.get(i).size() > 0) {
											sbBuilder.append("(");
											for (String data : state.get(i)) {
												sbBuilder.append("'" + data + "',");
											}
											sbBuilder.append(")");
											String updateStateSql = sqlMap.get("updateState")
													+ sbBuilder.substring(0, sbBuilder.length() - 1);
											task.execute(logNum++, conn, updateStateSql);
										}
										sbBuilder.delete(0, sbBuilder.length());
									} // 分段批量更新

								}

							}
						}

						return errorinfo + errorstr;

					}
				//}
				// 生成"1+N集中优化问题点跟踪模板"的excel
				clusterCodeList = new ArrayList<String>(new HashSet<String>(clusterCodeList));
				questionCodeList = new ArrayList<String>(new HashSet<String>(questionCodeList));
				clusterCodeList.remove("历史已派单，处理中");
				clusterCodeList.remove("没有所属聚类编号");
				clusterCodeList.remove("");
				logger.info("clusterCodeList长度" + clusterCodeList.size());
				if (clusterCodeList != null && !clusterCodeList.isEmpty()) {
					QueryCriteria criteria = new QueryCriteria();
					criteria.put("clusterCodeList", clusterCodeList);
					StringBuilder sBuilder = new StringBuilder();
					for (String data : clusterCodeList) {
						sBuilder.append("'").append(data).append("',");
					}
					String datas = sBuilder.substring(0, sBuilder.length() - 1);
					task.execute(logNum++, conn, sqlMap.get("updateNullClusterQuestion").replace("${datas}", datas));
				}
				logger.info("生成1+N集中优化问题点跟踪模板的excel" + clusterCodeList.size() + "..." + questionCodeList.size());
				return Common.ListToString(clusterCodeList, "0").replace(", ", ",") + "|"
						+ Common.ListToString(questionCodeList, "0").replace(", ", ",");
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("更新工单信息出错：", e);
			task.addDetaileLog(logNum++, "更新工单信息出错：" + e.getMessage());
			return e.getMessage();
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace(); // To change body of catch statement use
										// File | Settings | File Templates.
			}
		}
		return "";
	}

	/**
	 * @return
	 */
	private String queryRealPath() {
		return task.getFilePath();
	}

	// 保存工单的信令问题点
	public void createTempXLpronumber(QueryCriteria criteria) throws SQLException {
		// 先删除再创建
		String table1 = criteria.get("tableName1").toString();
		task.execute(logNum++, conn, sqlMap.get("delteTable1").replace("${tableName1}", table1));
		task.execute(logNum++, conn, sqlMap.get("createTempXLpronumber").replace("${tableName1}", table1));

	}

	// 查询工单的信令问题点数量
	public long queryXLpronumber(QueryCriteria criteria) throws SQLException {
		// 同步信令未派单的数据到临时表
		String table2 = criteria.get("tableName2").toString();
		String table1 = criteria.get("tableName1").toString();
		long result = 0;
		conn.setAutoCommit(false);
		try {
			String saveTempXLpronumberSql = sqlMap.get("saveTempXLpronumber").replace("${tableName1}", table1)
					.replace("${tableName2}", table2);
			task.execute(logNum++, conn, saveTempXLpronumberSql, criteria.get("city"),criteria.get("order_code").toString());
			String sql = sqlMap.get("queryXLpronumber").replace("${tableName1}", table1);

			result = task.selectOne(logNum++, conn, long.class, sql, criteria.get("vcplbtype"),criteria.get("order_code").toString());
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			conn.rollback();
			logger.error("执行查询工单的信令问题点数量sql语句错误：", e);
			throw new SQLException(e);
		} finally {
			conn.setAutoCommit(true);
		}
		return result;
	}

	// 查询工单的投诉问题点数量
	public long queryTousuList(QueryCriteria criteria) throws SQLException {
		long result = 0;
		conn.setAutoCommit(false);
		try {
			String sql = sqlMap.get("queryTousuList");

			result = task.selectOne(logNum++, conn, long.class, sql, criteria.get("order_code").toString());
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			conn.rollback();
			logger.error("执行查询工单的投诉问题点数量sql语句错误：", e);
			throw new SQLException(e);
		} finally {
			conn.setAutoCommit(true);
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	private List<Map<String, Object>> updateExcelbyXL(QueryCriteria criteria, String filePaths, String files,
			String order_code) throws SQLException {
		org.apache.poi.ss.usermodel.Workbook book = null;
		org.apache.poi.ss.usermodel.Sheet sheet = null;
		String dutygrid = "";
		dutygrid = criteria.get("dutygrid").toString();
		criteria.put("tableName1", "xl_temp_" + order_code);
		criteria.put("order_code",order_code);
		conn.setAutoCommit(false);
		try {
			createTempXLpronumber(criteria);
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			conn.rollback();
			logger.error("执行保存工单的信令问题点sql语句错误：", e);
			throw new SQLException(e);
		} finally {
			conn.setAutoCommit(true);
		}
		OutputStream outputStream = null;
		InputStream inputStream = null;
		// filePaths=filePaths.replace("/","\\");
		String newFile = filePaths + "/分析定位_" + files;
		try {
			inputStream = new FileInputStream(newFile.trim());
			book = WorkbookFactory.create(inputStream);
			int numberOfSheets = book.getNumberOfSheets();
			outputStream = new FileOutputStream(newFile);
			CellStyle style = book.createCellStyle();
			style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 居中
			style.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
			style.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
			style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
			String sheetName = "";
			// 20170220 跟踪表里面详细sheet缺少信令数据,需要追加到外部表文件里面
			List<Map> xlDatasList = new ArrayList<Map>();
			logger.info("包含有" + numberOfSheets + "个shell");
			if (numberOfSheets > 0) {
				for (int i = 0; i < numberOfSheets; i++) {
					sheet = book.getSheetAt(i);
					sheetName = book.getSheetName(i);
					if (sheetName.equals("深度覆盖-信令")) {
						// 根据聚类编号查询深度覆盖-信令的问题点
						criteria.put("vcplbtype", "信令_深度覆盖");
						criteria.put("tableName2", "xl_lyrfg_cell_month");
						long number = queryXLpronumber(criteria);

						logger.info(number + "");
						if (number > 0) {
							String exportXLSDFGSql = "";
							List<Map<String, Object>> XLlist = null;
							if(!dutygrid.equals("")&&null!=dutygrid){
								exportXLSDFGSql = sqlMap.get("exportHYXLSDFG").replace("${tableName1}",
										criteria.get("tableName1").toString());
								XLlist = task.selectMap(conn, exportXLSDFGSql,criteria.get("vcplbtype"),dutygrid);
							}else {
								exportXLSDFGSql = sqlMap.get("exportXLSDFG").replace("${tableName1}",
										criteria.get("tableName1").toString());
								XLlist = task.selectMap(conn, exportXLSDFGSql, criteria.get("vcplbtype"));
							}
							for (int j = 0; j < XLlist.size(); j++) {
								Row row = sheet.createRow((int) j + 1);
								// 第四步，创建单元格，并设置值
								Map map = XLlist.get(j);

								// 追加信令数据
								xlDatasList.add(map);

								Cell cell = row.createCell(0);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("聚类问题点编号") != null
										&& Common.judgeString(String.valueOf(map.get("聚类问题点编号")))
												? String.valueOf(map.get("聚类问题点编号")) : "");
								cell = row.createCell(1);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("编号ID") != null && Common.judgeString(String.valueOf(map.get("编号ID")))
												? String.valueOf(map.get("编号ID")) : "");
								cell = row.createCell(2);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("地市") != null && Common.judgeString(String.valueOf(map.get("地市")))
												? String.valueOf(map.get("地市")) : "");
								cell = row.createCell(3);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("经度") != null && Common.judgeString(String.valueOf(map.get("经度")))
												? String.valueOf(map.get("经度")) : "");
								cell = row.createCell(4);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("纬度") != null && Common.judgeString(String.valueOf(map.get("纬度")))
												? String.valueOf(map.get("纬度")) : "");
								cell = row.createCell(5);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("时间") != null && Common.judgeString(String.valueOf(map.get("时间")))
												? String.valueOf(map.get("时间")) : "");
								cell = row.createCell(6);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格A") != null && Common.judgeString(String.valueOf(map.get("网格A")))
												? String.valueOf(map.get("网格A")) : "");
								cell = row.createCell(7);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格B") != null && Common.judgeString(String.valueOf(map.get("网格B")))
												? String.valueOf(map.get("网格B")) : "");
								cell = row.createCell(8);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格C") != null && Common.judgeString(String.valueOf(map.get("网格C")))
												? String.valueOf(map.get("网格C")) : "");
								cell = row.createCell(9);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("楼宇名称") != null && Common.judgeString(String.valueOf(map.get("楼宇名称")))
												? String.valueOf(map.get("楼宇名称")) : "");
								cell = row.createCell(10);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题类型") != null && Common.judgeString(String.valueOf(map.get("问题类型")))
												? String.valueOf(map.get("问题类型")) : "");
								cell = row.createCell(11);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("楼宇ID") != null && Common.judgeString(String.valueOf(map.get("楼宇ID")))
												? String.valueOf(map.get("楼宇ID")) : "");
								cell = row.createCell(12);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇采样点总数") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇采样点总数")))
												? String.valueOf(map.get("楼宇采样点总数")) : "");
								cell = row.createCell(13);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("用户数") != null && Common.judgeString(String.valueOf(map.get("用户数")))
												? String.valueOf(map.get("用户数")) : "");
								cell = row.createCell(14);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇平均RSRP")))
												? String.valueOf(map.get("楼宇平均RSRP")) : "");
								cell = row.createCell(15);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("楼宇覆盖率") != null && Common.judgeString(String.valueOf(map.get("楼宇覆盖率")))
												? String.valueOf(map.get("楼宇覆盖率")) : "");
								cell = row.createCell(16);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇弱覆盖采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇弱覆盖采样点数")))
												? String.valueOf(map.get("楼宇弱覆盖采样点数")) : "");
								cell = row.createCell(17);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇平均RSRQ") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇平均RSRQ")))
												? String.valueOf(map.get("楼宇平均RSRQ")) : "");
								cell = row.createCell(18);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇干扰概率") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇干扰概率")))
												? String.valueOf(map.get("楼宇干扰概率")) : "");
								cell = row.createCell(19);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇模三干扰采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇模三干扰采样点数")))
												? String.valueOf(map.get("楼宇模三干扰采样点数")) : "");
								cell = row.createCell(20);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇平均CQI") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇平均CQI")))
												? String.valueOf(map.get("楼宇平均CQI")) : "");
								cell = row.createCell(21);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇CQI低于6比例") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇CQI低于6比例")))
												? String.valueOf(map.get("楼宇CQI低于6比例")) : "");
								cell = row.createCell(22);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇CQI低于6的采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇CQI低于6的采样点数")))
												? String.valueOf(map.get("楼宇CQI低于6的采样点数")) : "");
								cell = row.createCell(23);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇平均上行SINR") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇平均上行SINR")))
												? String.valueOf(map.get("楼宇平均上行SINR")) : "");
								cell = row.createCell(24);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇平均上行SINR低于0比例") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇平均上行SINR低于0比例")))
												? String.valueOf(map.get("楼宇平均上行SINR低于0比例")) : "");
								cell = row.createCell(25);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇内上行SINR低于0采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇内上行SINR低于0采样点数")))
												? String.valueOf(map.get("楼宇内上行SINR低于0采样点数")) : "");
								cell = row.createCell(26);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇覆盖空洞比例") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇覆盖空洞比例")))
												? String.valueOf(map.get("楼宇覆盖空洞比例")) : "");
								cell = row.createCell(27);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("楼宇重叠覆盖比例") != null
										&& Common.judgeString(String.valueOf(map.get("楼宇重叠覆盖比例")))
												? String.valueOf(map.get("楼宇重叠覆盖比例")) : "");
								cell = row.createCell(28);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区CGI") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区CGI")))
												? String.valueOf(map.get("主覆盖小区CGI")) : "");
								cell = row.createCell(29);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区ID") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区ID")))
												? String.valueOf(map.get("主覆盖小区ID")) : "");
								cell = row.createCell(30);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区名") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区名")))
												? String.valueOf(map.get("主覆盖小区名")) : "");
								cell = row.createCell(31);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区采样点总数") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区采样点总数")))
												? String.valueOf(map.get("主覆盖小区采样点总数")) : "");
								cell = row.createCell(32);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区平均rsrp") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区平均rsrp")))
												? String.valueOf(map.get("主覆盖小区平均rsrp")) : "");
								cell = row.createCell(33);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区ID") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区ID")))
												? String.valueOf(map.get("问题小区ID")) : "");
								cell = row.createCell(34);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题小区名") != null && Common.judgeString(String.valueOf(map.get("问题小区名")))
												? String.valueOf(map.get("问题小区名")) : "");
								cell = row.createCell(35);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区覆盖率") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区覆盖率")))
												? String.valueOf(map.get("问题小区覆盖率")) : "");
								cell = row.createCell(36);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区弱覆盖采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区弱覆盖采样点数")))
												? String.valueOf(map.get("问题小区弱覆盖采样点数")) : "");
								cell = row.createCell(37);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区平均TA") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区平均TA")))
												? String.valueOf(map.get("问题小区平均TA")) : "");
								cell = row.createCell(38);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区平均PHR") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区平均PHR")))
												? String.valueOf(map.get("问题小区平均PHR")) : "");
								cell = row.createCell(39);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区覆盖空洞") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区覆盖空洞")))
												? String.valueOf(map.get("问题小区覆盖空洞")) : "");
								cell = row.createCell(40);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区重叠覆盖") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区重叠覆盖")))
												? String.valueOf(map.get("问题小区重叠覆盖")) : "");

							}
						} else {
							Row row = sheet.createRow(1);
							Cell cell = row.createCell(0);
							cell.setCellValue("本周暂无");
							cell.setCellStyle(style);
						}

					}
					if (sheetName.equals("高铁覆盖-信令")) {
						// 根据聚类编号查询高铁覆盖-信令的问题点
						criteria.put("vcplbtype", "信令_高铁_弱覆盖");
						criteria.put("tableName2", "xl_gtrfg_cell_month");
						long number = queryXLpronumber(criteria);
						if (number > 0) {
							String exportXLSDFGSql = "";
							List<Map<String, Object>> XLlist = null;
							if(!dutygrid.equals("")&&null!=dutygrid){
								exportXLSDFGSql = sqlMap.get("exportHYXLGTRFG").replace("${tableName1}",
										criteria.get("tableName1").toString());
								XLlist = task.selectMap(conn, exportXLSDFGSql,criteria.get("vcplbtype"),dutygrid);
							}else {
								exportXLSDFGSql = sqlMap.get("exportXLGTRFG").replace("${tableName1}",
										criteria.get("tableName1").toString());
								XLlist = task.selectMap(conn, exportXLSDFGSql,criteria.get("vcplbtype"));
							}

							for (int j = 0; j < XLlist.size(); j++) {
								Row row = sheet.createRow((int) j + 1);
								// 第四步，创建单元格，并设置值
								Map map = XLlist.get(j);

								// 追加信令数据
								xlDatasList.add(map);

								Cell cell = row.createCell(0);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("聚类问题点编号") != null
										&& Common.judgeString(String.valueOf(map.get("聚类问题点编号")))
												? String.valueOf(map.get("聚类问题点编号")) : "");
								cell = row.createCell(1);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("编号ID") != null && Common.judgeString(String.valueOf(map.get("编号ID")))
												? String.valueOf(map.get("编号ID")) : "");
								cell = row.createCell(2);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("地市") != null && Common.judgeString(String.valueOf(map.get("地市")))
												? String.valueOf(map.get("地市")) : "");
								cell = row.createCell(3);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("经度") != null && Common.judgeString(String.valueOf(map.get("经度")))
												? String.valueOf(map.get("经度")) : "");
								cell = row.createCell(4);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("纬度") != null && Common.judgeString(String.valueOf(map.get("纬度")))
												? String.valueOf(map.get("纬度")) : "");
								cell = row.createCell(5);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("时间") != null && Common.judgeString(String.valueOf(map.get("时间")))
												? String.valueOf(map.get("时间")) : "");
								cell = row.createCell(6);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("高铁名称") != null && Common.judgeString(String.valueOf(map.get("高铁名称")))
												? String.valueOf(map.get("高铁名称")) : "");
								cell = row.createCell(7);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("高铁方向") != null && Common.judgeString(String.valueOf(map.get("高铁方向")))
												? String.valueOf(map.get("高铁方向")) : "");
								cell = row.createCell(8);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("栅格名称") != null && Common.judgeString(String.valueOf(map.get("栅格名称")))
												? String.valueOf(map.get("栅格名称")) : "");
								cell = row.createCell(9);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题类型") != null && Common.judgeString(String.valueOf(map.get("问题类型")))
												? String.valueOf(map.get("问题类型")) : "");
								cell = row.createCell(10);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("栅格ID") != null && Common.judgeString(String.valueOf(map.get("栅格ID")))
												? String.valueOf(map.get("栅格ID")) : "");
								cell = row.createCell(11);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格采样点总数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格采样点总数")))
												? String.valueOf(map.get("栅格采样点总数")) : "");
								cell = row.createCell(12);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("栅格用户数") != null && Common.judgeString(String.valueOf(map.get("栅格用户数")))
												? String.valueOf(map.get("栅格用户数")) : "");
								cell = row.createCell(13);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("栅格平均RSRP")))
												? String.valueOf(map.get("栅格平均RSRP")) : "");
								cell = row.createCell(14);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("栅格覆盖率") != null && Common.judgeString(String.valueOf(map.get("栅格覆盖率")))
												? String.valueOf(map.get("栅格覆盖率")) : "");
								cell = row.createCell(15);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格弱覆盖采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格弱覆盖采样点数")))
												? String.valueOf(map.get("栅格弱覆盖采样点数")) : "");
								cell = row.createCell(16);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格平均RSRQ") != null
										&& Common.judgeString(String.valueOf(map.get("栅格平均RSRQ")))
												? String.valueOf(map.get("栅格平均RSRQ")) : "");
								cell = row.createCell(17);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格干扰概率") != null
										&& Common.judgeString(String.valueOf(map.get("栅格干扰概率")))
												? String.valueOf(map.get("栅格干扰概率")) : "");
								cell = row.createCell(18);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格模三干扰采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格模三干扰采样点数")))
												? String.valueOf(map.get("栅格模三干扰采样点数")) : "");
								cell = row.createCell(19);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格平均CQI") != null
										&& Common.judgeString(String.valueOf(map.get("栅格平均CQI")))
												? String.valueOf(map.get("栅格平均CQI")) : "");
								cell = row.createCell(20);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格CQI低于6比例") != null
										&& Common.judgeString(String.valueOf(map.get("栅格CQI低于6比例")))
												? String.valueOf(map.get("栅格CQI低于6比例")) : "");
								cell = row.createCell(21);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格CQI低于6的采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格CQI低于6的采样点数")))
												? String.valueOf(map.get("栅格CQI低于6的采样点数")) : "");
								cell = row.createCell(22);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格平均上行SINR") != null
										&& Common.judgeString(String.valueOf(map.get("栅格平均上行SINR")))
												? String.valueOf(map.get("栅格平均上行SINR")) : "");
								cell = row.createCell(23);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格平均上行SINR低于0比例") != null
										&& Common.judgeString(String.valueOf(map.get("栅格平均上行SINR低于0比例")))
												? String.valueOf(map.get("栅格平均上行SINR低于0比例")) : "");
								cell = row.createCell(24);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格内上行SINR低于0采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格内上行SINR低于0采样点数")))
												? String.valueOf(map.get("栅格内上行SINR低于0采样点数")) : "");
								cell = row.createCell(25);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格覆盖空洞比例") != null
										&& Common.judgeString(String.valueOf(map.get("栅格覆盖空洞比例")))
												? String.valueOf(map.get("栅格覆盖空洞比例")) : "");
								cell = row.createCell(26);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格重叠覆盖比例") != null
										&& Common.judgeString(String.valueOf(map.get("栅格重叠覆盖比例")))
												? String.valueOf(map.get("栅格重叠覆盖比例")) : "");
								cell = row.createCell(27);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区CGI") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区CGI")))
												? String.valueOf(map.get("主覆盖小区CGI")) : "");
								cell = row.createCell(28);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区号") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区号")))
												? String.valueOf(map.get("主覆盖小区号")) : "");
								cell = row.createCell(29);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区名") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区名")))
												? String.valueOf(map.get("主覆盖小区名")) : "");
								cell = row.createCell(30);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区采样点总数") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区采样点总数")))
												? String.valueOf(map.get("主覆盖小区采样点总数")) : "");
								cell = row.createCell(31);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区平均RSRP")))
												? String.valueOf(map.get("主覆盖小区平均RSRP")) : "");
								cell = row.createCell(32);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题小区号") != null && Common.judgeString(String.valueOf(map.get("问题小区号")))
												? String.valueOf(map.get("问题小区号")) : "");
								cell = row.createCell(33);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题小区名") != null && Common.judgeString(String.valueOf(map.get("问题小区名")))
												? String.valueOf(map.get("问题小区名")) : "");
								cell = row.createCell(34);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区覆盖率") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区覆盖率")))
												? String.valueOf(map.get("问题小区覆盖率")) : "");
								cell = row.createCell(35);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区弱覆盖采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区弱覆盖采样点数")))
												? String.valueOf(map.get("问题小区弱覆盖采样点数")) : "");
								cell = row.createCell(36);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区平均TA") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区平均TA")))
												? String.valueOf(map.get("问题小区平均TA")) : "");
								cell = row.createCell(37);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区平均PHR") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区平均PHR")))
												? String.valueOf(map.get("问题小区平均PHR")) : "");
								cell = row.createCell(38);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区覆盖空洞比例") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区覆盖空洞比例")))
												? String.valueOf(map.get("问题小区覆盖空洞比例")) : "");
								cell = row.createCell(39);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区重叠覆盖比例") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区重叠覆盖比例")))
												? String.valueOf(map.get("问题小区重叠覆盖比例")) : "");

							}
						} else {
							Row row = sheet.createRow(1);
							Cell cell = row.createCell(0);
							cell.setCellValue("本周暂无");
							cell.setCellStyle(style);
						}
					}
					if (sheetName.equals("道路干扰-信令")) {
						// 根据聚类编号查询道路干扰-信令的问题点
						criteria.put("vcplbtype", "信令_干扰");
						criteria.put("tableName2", "xl_xldlgr_grid_month");
						long number = queryXLpronumber(criteria);
						if (number > 0) {
							String exportXLSDFGSql = "";
							List<Map<String, Object>> XLlist = null;
							if(!dutygrid.equals("")&&null!=dutygrid){
								exportXLSDFGSql = sqlMap.get("exportHYXLGR").replace("${tableName1}",
										criteria.get("tableName1").toString());
								XLlist = task.selectMap(conn, exportXLSDFGSql,criteria.get("vcplbtype"),dutygrid);
							}else {
								exportXLSDFGSql = sqlMap.get("exportXLGR").replace("${tableName1}",
										criteria.get("tableName1").toString());
								XLlist = task.selectMap(conn, exportXLSDFGSql,criteria.get("vcplbtype"));
							}
							for (int j = 0; j < XLlist.size(); j++) {
								Row row = sheet.createRow((int) j + 1);
								// 第四步，创建单元格，并设置值
								Map map = XLlist.get(j);

								// 追加信令数据
								xlDatasList.add(map);

								Cell cell = row.createCell(0);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("聚类问题点编号") != null
										&& Common.judgeString(String.valueOf(map.get("聚类问题点编号")))
												? String.valueOf(map.get("聚类问题点编号")) : "");
								cell = row.createCell(1);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("编号ID") != null && Common.judgeString(String.valueOf(map.get("编号ID")))
												? String.valueOf(map.get("编号ID")) : "");
								cell = row.createCell(2);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("地市") != null && Common.judgeString(String.valueOf(map.get("地市")))
												? String.valueOf(map.get("地市")) : "");
								cell = row.createCell(3);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("经度") != null && Common.judgeString(String.valueOf(map.get("经度")))
												? String.valueOf(map.get("经度")) : "");
								cell = row.createCell(4);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("纬度") != null && Common.judgeString(String.valueOf(map.get("纬度")))
												? String.valueOf(map.get("纬度")) : "");
								cell = row.createCell(5);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("时间") != null && Common.judgeString(String.valueOf(map.get("时间")))
												? String.valueOf(map.get("时间")) : "");
								cell = row.createCell(6);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格A") != null && Common.judgeString(String.valueOf(map.get("网格A")))
												? String.valueOf(map.get("网格A")) : "");
								cell = row.createCell(7);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格B") != null && Common.judgeString(String.valueOf(map.get("网格B")))
												? String.valueOf(map.get("网格B")) : "");
								cell = row.createCell(8);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格C") != null && Common.judgeString(String.valueOf(map.get("网格C")))
												? String.valueOf(map.get("网格C")) : "");
								cell = row.createCell(9);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("道路名称") != null && Common.judgeString(String.valueOf(map.get("道路名称")))
												? String.valueOf(map.get("道路名称")) : "");
								cell = row.createCell(10);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题类型") != null && Common.judgeString(String.valueOf(map.get("问题类型")))
												? String.valueOf(map.get("问题类型")) : "");
								cell = row.createCell(11);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("道路栅格ID") != null
										&& Common.judgeString(String.valueOf(map.get("道路栅格ID")))
												? String.valueOf(map.get("道路栅格ID")) : "");
								cell = row.createCell(12);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格采样点总数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格采样点总数")))
												? String.valueOf(map.get("栅格采样点总数")) : "");
								cell = row.createCell(13);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("栅格用户数") != null && Common.judgeString(String.valueOf(map.get("栅格用户数")))
												? String.valueOf(map.get("栅格用户数")) : "");
								cell = row.createCell(14);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("栅格平均RSRP")))
												? String.valueOf(map.get("栅格平均RSRP")) : "");
								cell = row.createCell(15);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("栅格覆盖率") != null && Common.judgeString(String.valueOf(map.get("栅格覆盖率")))
												? String.valueOf(map.get("栅格覆盖率")) : "");
								cell = row.createCell(16);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格弱覆盖采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格弱覆盖采样点数")))
												? String.valueOf(map.get("栅格弱覆盖采样点数")) : "");
								cell = row.createCell(17);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格干扰概率") != null
										&& Common.judgeString(String.valueOf(map.get("栅格干扰概率")))
												? String.valueOf(map.get("栅格干扰概率")) : "");
								cell = row.createCell(18);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格模三干扰采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格模三干扰采样点数")))
												? String.valueOf(map.get("栅格模三干扰采样点数")) : "");
								cell = row.createCell(19);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区CGI") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区CGI")))
												? String.valueOf(map.get("主覆盖小区CGI")) : "");
								cell = row.createCell(20);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区号") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区号")))
												? String.valueOf(map.get("主覆盖小区号")) : "");
								cell = row.createCell(21);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区名") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区名")))
												? String.valueOf(map.get("主覆盖小区名")) : "");
								cell = row.createCell(22);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区采样点总数") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区采样点总数")))
												? String.valueOf(map.get("主覆盖小区采样点总数")) : "");
								cell = row.createCell(23);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区平均RSRP")))
												? String.valueOf(map.get("主覆盖小区平均RSRP")) : "");
								cell = row.createCell(24);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题小区号") != null && Common.judgeString(String.valueOf(map.get("问题小区号")))
												? String.valueOf(map.get("问题小区号")) : "");
								cell = row.createCell(25);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题小区名") != null && Common.judgeString(String.valueOf(map.get("问题小区名")))
												? String.valueOf(map.get("问题小区名")) : "");
								cell = row.createCell(26);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区干扰概率") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区干扰概率")))
												? String.valueOf(map.get("问题小区干扰概率")) : "");
								cell = row.createCell(27);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区模三干扰采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区模三干扰采样点数")))
												? String.valueOf(map.get("问题小区模三干扰采样点数")) : "");
								cell = row.createCell(28);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区PCI") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区PCI")))
												? String.valueOf(map.get("问题小区PCI")) : "");
								cell = row.createCell(29);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("干扰小区PCI") != null
										&& Common.judgeString(String.valueOf(map.get("干扰小区PCI")))
												? String.valueOf(map.get("干扰小区PCI")) : "");
								cell = row.createCell(30);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("干扰小区平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("干扰小区平均RSRP")))
												? String.valueOf(map.get("干扰小区平均RSRP")) : "");
								cell = row.createCell(31);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区平均RSRP")))
												? String.valueOf(map.get("问题小区平均RSRP")) : "");
								cell = row.createCell(32);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区覆盖率") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区覆盖率")))
												? String.valueOf(map.get("问题小区覆盖率")) : "");
								cell = row.createCell(33);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区平均TA") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区平均TA")))
												? String.valueOf(map.get("问题小区平均TA")) : "");
								cell = row.createCell(34);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区平均PHR") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区平均PHR")))
												? String.valueOf(map.get("问题小区平均PHR")) : "");
								cell = row.createCell(35);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区覆盖空洞比例") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区覆盖空洞比例")))
												? String.valueOf(map.get("问题小区覆盖空洞比例")) : "");
								cell = row.createCell(36);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区重叠覆盖比例") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区重叠覆盖比例")))
												? String.valueOf(map.get("问题小区重叠覆盖比例")) : "");

							}
						} else {
							Row row = sheet.createRow(1);
							Cell cell = row.createCell(0);
							cell.setCellValue("本周暂无");
							cell.setCellStyle(style);
						}

					}
					if (sheetName.equals("道路覆盖-信令")) {
						// 根据聚类编号查询道路覆盖-信令的问题点
						criteria.put("vcplbtype", "信令_弱覆盖");
						criteria.put("tableName2", "xl_xldlfg_grid_month");
						long number = queryXLpronumber(criteria);
						if (number > 0) {
							String exportXLSDFGSql = "";
							List<Map<String, Object>> XLlist = null;
							if(!dutygrid.equals("")&&null!=dutygrid){
								exportXLSDFGSql = sqlMap.get("exportHYXLRFG").replace("${tableName1}",
										criteria.get("tableName1").toString());
								XLlist = task.selectMap(conn, exportXLSDFGSql,criteria.get("vcplbtype"),dutygrid);
							}else {
								exportXLSDFGSql = sqlMap.get("exportXLRFG").replace("${tableName1}",
										criteria.get("tableName1").toString());
								XLlist = task.selectMap(conn, exportXLSDFGSql, criteria.get("vcplbtype"));
							}
							for (int j = 0; j < XLlist.size(); j++) {
								Row row = sheet.createRow((int) j + 1);
								// 第四步，创建单元格，并设置值
								Map map = XLlist.get(j);

								// 追加信令数据
								xlDatasList.add(map);

								Cell cell = row.createCell(0);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("聚类问题点编号") != null
										&& Common.judgeString(String.valueOf(map.get("聚类问题点编号")))
												? String.valueOf(map.get("聚类问题点编号")) : "");
								cell = row.createCell(1);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("编号ID") != null && Common.judgeString(String.valueOf(map.get("编号ID")))
												? String.valueOf(map.get("编号ID")) : "");
								cell = row.createCell(2);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("地市") != null && Common.judgeString(String.valueOf(map.get("地市")))
												? String.valueOf(map.get("地市")) : "");
								cell = row.createCell(3);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("经度") != null && Common.judgeString(String.valueOf(map.get("经度")))
												? String.valueOf(map.get("经度")) : "");
								cell = row.createCell(4);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("纬度") != null && Common.judgeString(String.valueOf(map.get("纬度")))
												? String.valueOf(map.get("纬度")) : "");
								cell = row.createCell(5);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("时间") != null && Common.judgeString(String.valueOf(map.get("时间")))
												? String.valueOf(map.get("时间")) : "");
								cell = row.createCell(6);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格A") != null && Common.judgeString(String.valueOf(map.get("网格A")))
												? String.valueOf(map.get("网格A")) : "");
								cell = row.createCell(7);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格B") != null && Common.judgeString(String.valueOf(map.get("网格B")))
												? String.valueOf(map.get("网格B")) : "");
								cell = row.createCell(8);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格C") != null && Common.judgeString(String.valueOf(map.get("网格C")))
												? String.valueOf(map.get("网格C")) : "");
								cell = row.createCell(9);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("道路名称") != null && Common.judgeString(String.valueOf(map.get("道路名称")))
												? String.valueOf(map.get("道路名称")) : "");
								cell = row.createCell(10);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题类型") != null && Common.judgeString(String.valueOf(map.get("问题类型")))
												? String.valueOf(map.get("问题类型")) : "");
								cell = row.createCell(11);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("道路栅格ID") != null
										&& Common.judgeString(String.valueOf(map.get("道路栅格ID")))
												? String.valueOf(map.get("道路栅格ID")) : "");
								cell = row.createCell(12);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格采样点总数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格采样点总数")))
												? String.valueOf(map.get("栅格采样点总数")) : "");
								cell = row.createCell(13);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("栅格用户数") != null && Common.judgeString(String.valueOf(map.get("栅格用户数")))
												? String.valueOf(map.get("栅格用户数")) : "");
								cell = row.createCell(14);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("栅格平均RSRP")))
												? String.valueOf(map.get("栅格平均RSRP")) : "");
								cell = row.createCell(15);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("栅格覆盖率") != null && Common.judgeString(String.valueOf(map.get("栅格覆盖率")))
												? String.valueOf(map.get("栅格覆盖率")) : "");
								cell = row.createCell(16);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格弱覆盖采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格弱覆盖采样点数")))
												? String.valueOf(map.get("栅格弱覆盖采样点数")) : "");
								cell = row.createCell(17);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区CGI") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区CGI")))
												? String.valueOf(map.get("主覆盖小区CGI")) : "");
								cell = row.createCell(18);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区号") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区号")))
												? String.valueOf(map.get("主覆盖小区号")) : "");
								cell = row.createCell(19);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区名") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区名")))
												? String.valueOf(map.get("主覆盖小区名")) : "");
								cell = row.createCell(20);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区采样点总数") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区采样点总数")))
												? String.valueOf(map.get("主覆盖小区采样点总数")) : "");
								cell = row.createCell(21);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区平均RSRP")))
												? String.valueOf(map.get("主覆盖小区平均RSRP")) : "");
								cell = row.createCell(22);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题小区号") != null && Common.judgeString(String.valueOf(map.get("问题小区号")))
												? String.valueOf(map.get("问题小区号")) : "");
								cell = row.createCell(23);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题小区名") != null && Common.judgeString(String.valueOf(map.get("问题小区名")))
												? String.valueOf(map.get("问题小区名")) : "");
								cell = row.createCell(24);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区覆盖率") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区覆盖率")))
												? String.valueOf(map.get("问题小区覆盖率")) : "");
								cell = row.createCell(25);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区弱覆盖采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区弱覆盖采样点数")))
												? String.valueOf(map.get("问题小区弱覆盖采样点数")) : "");
								cell = row.createCell(26);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区平均TA") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区平均TA")))
												? String.valueOf(map.get("问题小区平均TA")) : "");
								cell = row.createCell(27);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区平均PHR") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区平均PHR")))
												? String.valueOf(map.get("问题小区平均PHR")) : "");
								cell = row.createCell(28);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区覆盖空洞比例") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区覆盖空洞比例")))
												? String.valueOf(map.get("问题小区覆盖空洞比例")) : "");
								cell = row.createCell(29);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区重叠覆盖比例") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区重叠覆盖比例")))
												? String.valueOf(map.get("问题小区重叠覆盖比例")) : "");

							}
						} else {
							Row row = sheet.createRow(1);
							Cell cell = row.createCell(0);
							cell.setCellValue("本周暂无");
							cell.setCellStyle(style);
						}

					}
					if (sheetName.equals("高铁干扰-信令")) {
						// 根据聚类编号查询高铁干扰-信令的问题点
						criteria.put("vcplbtype", "信令_高铁_干扰");
						criteria.put("tableName2", "xl_gtgr_cell_month");
						long number = queryXLpronumber(criteria);
						if (number > 0) {
							String exportXLSDFGSql = "";
							List<Map<String, Object>> XLlist = null;
							if(!dutygrid.equals("")&&null!=dutygrid){
								exportXLSDFGSql = sqlMap.get("exportHYXLGTGR").replace("${tableName1}",
										criteria.get("tableName1").toString());
								XLlist = task.selectMap(conn, exportXLSDFGSql, criteria.get("vcplbtype"),dutygrid);
							}else {
								exportXLSDFGSql = sqlMap.get("exportXLGTGR").replace("${tableName1}",
										criteria.get("tableName1").toString());
								XLlist = task.selectMap(conn, exportXLSDFGSql, criteria.get("vcplbtype"));
							}

							for (int j = 0; j < XLlist.size(); j++) {
								Row row = sheet.createRow((int) j + 1);
								// 第四步，创建单元格，并设置值
								Map map = XLlist.get(j);

								// 追加信令数据
								xlDatasList.add(map);

								Cell cell = row.createCell(0);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("聚类问题点编号") != null
										&& Common.judgeString(String.valueOf(map.get("聚类问题点编号")))
												? String.valueOf(map.get("聚类问题点编号")) : "");
								cell = row.createCell(1);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("编号ID") != null && Common.judgeString(String.valueOf(map.get("编号ID")))
												? String.valueOf(map.get("编号ID")) : "");
								cell = row.createCell(2);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("地市") != null && Common.judgeString(String.valueOf(map.get("地市")))
												? String.valueOf(map.get("地市")) : "");
								cell = row.createCell(3);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("经度") != null && Common.judgeString(String.valueOf(map.get("经度")))
												? String.valueOf(map.get("经度")) : "");
								cell = row.createCell(4);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("纬度") != null && Common.judgeString(String.valueOf(map.get("纬度")))
												? String.valueOf(map.get("纬度")) : "");
								cell = row.createCell(5);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("时间") != null && Common.judgeString(String.valueOf(map.get("时间")))
												? String.valueOf(map.get("时间")) : "");
								cell = row.createCell(6);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("高铁名称") != null && Common.judgeString(String.valueOf(map.get("高铁名称")))
												? String.valueOf(map.get("高铁名称")) : "");
								cell = row.createCell(7);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("高铁方向") != null && Common.judgeString(String.valueOf(map.get("高铁方向")))
												? String.valueOf(map.get("高铁方向")) : "");
								cell = row.createCell(8);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("栅格名称") != null && Common.judgeString(String.valueOf(map.get("栅格名称")))
												? String.valueOf(map.get("栅格名称")) : "");
								cell = row.createCell(9);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题类型") != null && Common.judgeString(String.valueOf(map.get("问题类型")))
												? String.valueOf(map.get("问题类型")) : "");
								cell = row.createCell(10);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("栅格ID") != null && Common.judgeString(String.valueOf(map.get("栅格ID")))
												? String.valueOf(map.get("栅格ID")) : "");
								cell = row.createCell(11);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格采样点总数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格采样点总数")))
												? String.valueOf(map.get("栅格采样点总数")) : "");
								cell = row.createCell(12);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("栅格用户数") != null && Common.judgeString(String.valueOf(map.get("栅格用户数")))
												? String.valueOf(map.get("栅格用户数")) : "");
								cell = row.createCell(13);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("栅格平均RSRP")))
												? String.valueOf(map.get("栅格平均RSRP")) : "");
								cell = row.createCell(14);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("栅格覆盖率") != null && Common.judgeString(String.valueOf(map.get("栅格覆盖率")))
												? String.valueOf(map.get("栅格覆盖率")) : "");
								cell = row.createCell(15);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格弱覆盖采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格弱覆盖采样点数")))
												? String.valueOf(map.get("栅格弱覆盖采样点数")) : "");
								cell = row.createCell(16);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格平均RSRQ") != null
										&& Common.judgeString(String.valueOf(map.get("栅格平均RSRQ")))
												? String.valueOf(map.get("栅格平均RSRQ")) : "");
								cell = row.createCell(17);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格干扰概率") != null
										&& Common.judgeString(String.valueOf(map.get("栅格干扰概率")))
												? String.valueOf(map.get("栅格干扰概率")) : "");
								cell = row.createCell(18);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格模三干扰采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格模三干扰采样点数")))
												? String.valueOf(map.get("栅格模三干扰采样点数")) : "");
								cell = row.createCell(19);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格平均CQI") != null
										&& Common.judgeString(String.valueOf(map.get("栅格平均CQI")))
												? String.valueOf(map.get("栅格平均CQI")) : "");
								cell = row.createCell(20);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格CQI低于6比例") != null
										&& Common.judgeString(String.valueOf(map.get("栅格CQI低于6比例")))
												? String.valueOf(map.get("栅格CQI低于6比例")) : "");
								cell = row.createCell(21);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格CQI低于6的采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格CQI低于6的采样点数")))
												? String.valueOf(map.get("栅格CQI低于6的采样点数")) : "");
								cell = row.createCell(22);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格平均上行SINR") != null
										&& Common.judgeString(String.valueOf(map.get("栅格平均上行SINR")))
												? String.valueOf(map.get("栅格平均上行SINR")) : "");
								cell = row.createCell(23);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格平均上行SINR低于0比例") != null
										&& Common.judgeString(String.valueOf(map.get("栅格平均上行SINR低于0比例")))
												? String.valueOf(map.get("栅格平均上行SINR低于0比例")) : "");
								cell = row.createCell(24);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格内上行SINR低于0采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("栅格内上行SINR低于0采样点数")))
												? String.valueOf(map.get("栅格内上行SINR低于0采样点数")) : "");
								cell = row.createCell(25);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格覆盖空洞比例") != null
										&& Common.judgeString(String.valueOf(map.get("栅格覆盖空洞比例")))
												? String.valueOf(map.get("栅格覆盖空洞比例")) : "");
								cell = row.createCell(26);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("栅格重叠覆盖比例") != null
										&& Common.judgeString(String.valueOf(map.get("栅格重叠覆盖比例")))
												? String.valueOf(map.get("栅格重叠覆盖比例")) : "");
								cell = row.createCell(27);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区CGI") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区CGI")))
												? String.valueOf(map.get("主覆盖小区CGI")) : "");
								cell = row.createCell(28);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区号") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区号")))
												? String.valueOf(map.get("主覆盖小区号")) : "");
								cell = row.createCell(29);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区名") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区名")))
												? String.valueOf(map.get("主覆盖小区名")) : "");
								cell = row.createCell(30);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区采样点总数") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区采样点总数")))
												? String.valueOf(map.get("主覆盖小区采样点总数")) : "");
								cell = row.createCell(31);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("主覆盖小区平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("主覆盖小区平均RSRP")))
												? String.valueOf(map.get("主覆盖小区平均RSRP")) : "");
								cell = row.createCell(32);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题小区号") != null && Common.judgeString(String.valueOf(map.get("问题小区号")))
												? String.valueOf(map.get("问题小区号")) : "");
								cell = row.createCell(33);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题小区名") != null && Common.judgeString(String.valueOf(map.get("问题小区名")))
												? String.valueOf(map.get("问题小区名")) : "");
								cell = row.createCell(34);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区干扰概率") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区干扰概率")))
												? String.valueOf(map.get("问题小区干扰概率")) : "");
								cell = row.createCell(35);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区模三干扰采样点数") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区模三干扰采样点数")))
												? String.valueOf(map.get("问题小区模三干扰采样点数")) : "");
								cell = row.createCell(36);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区PCI") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区PCI")))
												? String.valueOf(map.get("问题小区PCI")) : "");
								cell = row.createCell(37);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("干扰小区PCI") != null
										&& Common.judgeString(String.valueOf(map.get("干扰小区PCI")))
												? String.valueOf(map.get("干扰小区PCI")) : "");
								cell = row.createCell(38);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("干扰小区平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("干扰小区平均RSRP")))
												? String.valueOf(map.get("干扰小区平均RSRP")) : "");
								cell = row.createCell(39);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区平均RSRP") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区平均RSRP")))
												? String.valueOf(map.get("问题小区平均RSRP")) : "");
								cell = row.createCell(40);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区覆盖率") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区覆盖率")))
												? String.valueOf(map.get("问题小区覆盖率")) : "");
								cell = row.createCell(41);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区平均TA") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区平均TA")))
												? String.valueOf(map.get("问题小区平均TA")) : "");
								cell = row.createCell(42);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区平均PHR") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区平均PHR")))
												? String.valueOf(map.get("问题小区平均PHR")) : "");
								cell = row.createCell(43);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区覆盖空洞比例") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区覆盖空洞比例")))
												? String.valueOf(map.get("问题小区覆盖空洞比例")) : "");
								cell = row.createCell(44);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("问题小区重叠覆盖比例") != null
										&& Common.judgeString(String.valueOf(map.get("问题小区重叠覆盖比例")))
												? String.valueOf(map.get("问题小区重叠覆盖比例")) : "");

							}
						} else {
							Row row = sheet.createRow(1);
							Cell cell = row.createCell(0);
							cell.setCellValue("本周暂无");
							cell.setCellStyle(style);
						}
					}
				}

			}
			book.write(outputStream);

			// 追加信令数据后,将信令问题点写入到外部表文件里面去
			String extFilePath = this.criteria.get("rkhcExtPath").toString();
			FileWriter fWriter = new FileWriter(new File(extFilePath), true);
			PrintWriter out = new PrintWriter(fWriter);
			for (Map map : xlDatasList) {
				String problemNum = map.get("编号ID") != null && Common.judgeString(String.valueOf(map.get("编号ID")))
						? String.valueOf(map.get("编号ID")) : "";
				out.println(problemNum + "|信令|0");
			}
			out.close();
			fWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} finally {
			System.gc();
			try {
				if (outputStream != null) {
					outputStream.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String tableName1 = criteria.get("tableName1").toString();
		String queryXLbyorderSql = sqlMap.get("queryXLbyorder").replace("${tableName1}", tableName1);

		return task.selectMap(conn, queryXLbyorderSql);
	}

	@SuppressWarnings("rawtypes")
	private List<Map<String, Object>> updateExcelbyTS(QueryCriteria criteria, String filePaths, String files,
													  String order_code) throws SQLException {
		org.apache.poi.ss.usermodel.Workbook book = null;
		org.apache.poi.ss.usermodel.Sheet sheet = null;
		criteria.put("order_code",order_code);
		String dutygrid = "";
		dutygrid = criteria.get("dutygrid").toString();
		conn.setAutoCommit(true);
		OutputStream outputStream = null;
		InputStream inputStream = null;
		// filePaths=filePaths.replace("/","\\");
		String newFile = filePaths + "/分析定位_" + files;
		try {
			inputStream = new FileInputStream(newFile.trim());
			book = WorkbookFactory.create(inputStream);
			int numberOfSheets = book.getNumberOfSheets();
			outputStream = new FileOutputStream(newFile);
			CellStyle style = book.createCellStyle();
			style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 居中
			style.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
			style.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
			style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
			String sheetName = "";
			// 20171009 跟踪表里面详细sheet缺少投诉数据,需要追加到外部表文件里面
			List<Map> tsDatasList = new ArrayList<Map>();
			logger.info("包含有" + numberOfSheets + "个shell");
			if (numberOfSheets > 0) {
				for (int i = 0; i < numberOfSheets; i++) {
					sheet = book.getSheetAt(i);
					sheetName = book.getSheetName(i);
					if (sheetName.equals("用户投诉")) {
						criteria.put("vcplbtype", "用户投诉");
						criteria.put("tableName2", "yy_complainproblem_month");
						long number = queryTousuList(criteria);

						logger.info(number + "");
						if (number > 0) {
							String exportYHTSSql = "";
							List<Map<String, Object>> XLlist = null;
							exportYHTSSql = sqlMap.get("exportYHTS");
							XLlist = task.selectMap(conn, exportYHTSSql,order_code,dutygrid);
							for (int j = 0; j < XLlist.size(); j++) {
								Row row = sheet.createRow((int) j + 1);
								// 第四步，创建单元格，并设置值
								Map map = XLlist.get(j);

								// 追加投诉数据
								tsDatasList.add(map);

								Cell cell = row.createCell(0);
								cell.setCellStyle(style);
								cell.setCellValue(map.get("聚类问题点编号") != null
										&& Common.judgeString(String.valueOf(map.get("聚类问题点编号")))
										? String.valueOf(map.get("聚类问题点编号")) : "");
								cell = row.createCell(1);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题点编号") != null && Common.judgeString(String.valueOf(map.get("问题点编号")))
												? String.valueOf(map.get("问题点编号")) : "");
								cell = row.createCell(2);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("流水号") != null && Common.judgeString(String.valueOf(map.get("流水号")))
												? String.valueOf(map.get("流水号")) : "");
								cell = row.createCell(3);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("投诉时间") != null && Common.judgeString(String.valueOf(map.get("投诉时间")))
												? String.valueOf(map.get("投诉时间")) : "");
								cell = row.createCell(4);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("地市") != null && Common.judgeString(String.valueOf(map.get("地市")))
												? String.valueOf(map.get("地市")) : "");
								cell = row.createCell(5);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("行政区") != null && Common.judgeString(String.valueOf(map.get("行政区")))
												? String.valueOf(map.get("行政区")) : "");
								cell = row.createCell(6);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("投诉业务") != null && Common.judgeString(String.valueOf(map.get("投诉业务")))
												? String.valueOf(map.get("投诉业务")) : "");
								cell = row.createCell(7);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("问题细项") != null && Common.judgeString(String.valueOf(map.get("问题细项")))
												? String.valueOf(map.get("问题细项")) : "");
								cell = row.createCell(8);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("地址") != null && Common.judgeString(String.valueOf(map.get("地址")))
												? String.valueOf(map.get("地址")) : "");
								cell = row.createCell(9);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格类型") != null && Common.judgeString(String.valueOf(map.get("网格类型")))
												? String.valueOf(map.get("网格类型")) : "");
								cell = row.createCell(10);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("经度") != null && Common.judgeString(String.valueOf(map.get("经度")))
												? String.valueOf(map.get("经度")) : "");
								cell = row.createCell(11);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("纬度") != null && Common.judgeString(String.valueOf(map.get("纬度")))
												? String.valueOf(map.get("纬度")) : "");
								cell = row.createCell(12);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格ID") != null && Common.judgeString(String.valueOf(map.get("网格ID")))
												? String.valueOf(map.get("网格ID")) : "");
								cell = row.createCell(13);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格中文名称") != null && Common.judgeString(String.valueOf(map.get("网格中文名称")))
												? String.valueOf(map.get("网格中文名称")) : "");
								cell = row.createCell(14);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("网格投诉量") != null && Common.judgeString(String.valueOf(map.get("网格投诉量")))
												? String.valueOf(map.get("网格投诉量")) : "");
								cell = row.createCell(15);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("影响范围") != null && Common.judgeString(String.valueOf(map.get("影响范围")))
												? String.valueOf(map.get("影响范围")) : "");
								cell = row.createCell(16);
								cell.setCellStyle(style);
								cell.setCellValue(
										map.get("归属网格名称") != null && Common.judgeString(String.valueOf(map.get("归属网格名称")))
												? String.valueOf(map.get("归属网格名称")) : "");
							}
						} else {
							Row row = sheet.createRow(1);
							Cell cell = row.createCell(0);
							cell.setCellValue("本周暂无");
							cell.setCellStyle(style);
						}

					}
				}
			}
			book.write(outputStream);

			// 追加投诉数据后,将投诉问题点写入到外部表文件里面去
			String extFilePath = this.criteria.get("rkhcExtPath").toString();
			FileWriter fWriter = new FileWriter(new File(extFilePath), true);
			PrintWriter out = new PrintWriter(fWriter);
			for (Map map : tsDatasList) {
				String problemNum = map.get("问题点编号") != null && Common.judgeString(String.valueOf(map.get("问题点编号")))
						? String.valueOf(map.get("问题点编号")) : "";
				out.println(problemNum + "|投诉|0");
			}
			out.close();
			fWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} finally {
			System.gc();
			try {
				if (outputStream != null) {
					outputStream.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String queryTSbyorderSql = sqlMap.get("queryTSbyorder");

		return task.selectMap(conn, queryTSbyorderSql,order_code);
	}

	/**
	 * 删除工单信息
	 * @param criteria
	 * @throws SQLException
	 */
	public void removeAllQuestionByCode(QueryCriteria criteria) throws SQLException {

		// 事务控制
		conn.setAutoCommit(false);

		try {
			// 更新已派工单的状态
			task.execute(logNum++, conn, sqlMap.get("updateCodestate"), criteria.get("order_code"));
			// 更新此工单的信令问题点状态
			String[] tablenames = new String[] { "xl_gtgr_cell_month", "xl_gtrfg_cell_month", "xl_lyrfg_cell_month",
					"xl_xldlfg_grid_month", "xl_xldlgr_grid_month" };
			criteria.put("xl_state", 0);

			for (int i = 0; i < tablenames.length; i++) {
				String updateXLproInfo2Sql = sqlMap.get("updateXLproInfo2").replace("${tableName2}", tablenames[i]);
				task.execute(logNum++, conn, updateXLproInfo2Sql, criteria.get("xl_state"), criteria.get("order_code"));
			}

			// 删除工单
			task.execute(logNum++, conn, sqlMap.get("removeAllQuestionByCode"), criteria.get("order_code"));
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			conn.rollback();
			logger.error("执行删除工单方法出错：", e);
			task.addDetaileLog(logNum++, "执行删除工单方法出错：" + e.getMessage());
			throw new SQLException(e);
		} finally {
			conn.setAutoCommit(true);
		}

	}

	@SuppressWarnings("unchecked")
	public void saveClusterQuestion(QueryCriteria criteria, String order_code) throws SQLException {
		HashSet<String> hs = (HashSet<String>) criteria.get("clusterCodeList");
		String datas = "";
		for (String data : hs) {
			datas += "'" + data + "',";
		}
		datas = datas.substring(0, datas.length() - 1);
		String saveClusterQuestionSql = sqlMap.get("saveClusterQuestion").replace("${datas}", datas)
				.replace("${order_code}", criteria.get("order_code").toString())
				.replace("${handle_state}", criteria.get("handle_state").toString());
		int number = task.execute(logNum++, conn, saveClusterQuestionSql);

		// 控制事务
		conn.setAutoCommit(false);
		try {
			updateDataSource(order_code);
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			conn.rollback();
			logger.error("更改来源出错：", e);
			task.addDetaileLog(logNum++, "更改来源出错：" + e.getMessage());
			throw new SQLException(e);
		} finally {
			conn.setAutoCommit(true);
		}

		logger.info("此工单共插入" + number + "条记录");
	}

	// 更改来源
	public void updateDataSource(String order_code) throws SQLException {

		String data = "";
		String source = "";
		data = "网管统计";
		source = "'PM','GHNL','GSNL','GHF','LGGR','GFHN','VQH','VDXSP','VDXYY','VJTSP','VJTYY','VSY','GDH','GJT','GZC','LDX','LJT','VYHQH','CZQH'";
		task.execute(logNum++, conn, sqlMap.get("updateDataSource").replace("${datas}", source), data, order_code);

		data = "工参数据";
		source = "'LCG','LCJ','LCY','CM'";
		task.execute(logNum++, conn, sqlMap.get("updateDataSource").replace("${datas}", source), data, order_code);

		data = "ATU";
		source = "'VAIMS','VAeSRVCCDelay','VADelay','VAeSRVCC','CD','XZ10','SC','XZ2','CSFB','GRQ','VARTP','VADrop','VAMOS','VABlock','S0','SF3','FG100','FG110'";
		task.execute(logNum++, conn, sqlMap.get("updateDataSource").replace("${datas}", source), data, order_code);

		//data = "北向MR";
        data = "MRO";
		source = "'LMR','MR'";
		task.execute(logNum++, conn, sqlMap.get("updateDataSource").replace("${datas}", source), data, order_code);

		data = "软硬采信令";
		source = "'XL','GTFGXL','LYFGXL','DLFGXL','DLGRXL','GTGRXL'";
		task.execute(logNum++, conn, sqlMap.get("updateDataSource").replace("${datas}", source), data, order_code);

	}

	/**
	 * 生成"1+N集中优化问题点跟踪模板"的excel
	 * @throws IOException 
	 */
	public String genTrackTemplate(QueryCriteria criteria, String files) throws Exception {

		OutputStream os = null;
		String fileName = "分析定位_1+N集中优化问题点跟踪_" + System.currentTimeMillis() + ".xls";
		try {
			String path = files + "/" + fileName;
			Common.mkdir(path);
			os = new FileOutputStream(path);
			this.exportDispatch(os, criteria);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("生成1+N集中优化问题点跟踪模板出错：" + e.getMessage());
			task.addDetaileLog(logNum++, "生成1+N集中优化问题点跟踪模板出错：" + e.getMessage());
		} finally {
			System.gc();
			if (os != null) {
				try {
					os.close();
				} catch (IOException e1) {
					logger.error("生成1+N集中优化问题点跟踪模板-关闭文件流出错：" + e1.getMessage());
					task.addDetaileLog(logNum++, "生成1+N集中优化问题点跟踪模板-关闭文件流出错：" + e1.getMessage());
				}
			}
		}

		logger.info("fileName>>>>>>>>>>>" + fileName);
		return fileName;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void exportDispatch(OutputStream os, QueryCriteria criteria) throws Exception {

		// 第一步，创建一个webbook，对应一个Excel文件
		Workbook wb = new HSSFWorkbook();
		// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
		org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("方案库派单附件表");
		// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
		Row row = sheet.createRow((int) 0);
		// 第四步，创建单元格，并设置值表头 设置表头居中
		CellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setFillBackgroundColor(HSSFColor.BLUE.index);

		org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
		cell.setCellValue("聚合问题点序号");
		sheet.setColumnWidth(0, ("聚合问题点序号".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("问题点数量");
		sheet.setColumnWidth(1, ("问题点数量".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(2);
		cell.setCellValue("地市");
		sheet.setColumnWidth(2, ("地市".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(3);
		cell.setCellValue("聚合问题点经度");
		sheet.setColumnWidth(3, ("聚合问题点经度".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(4);
		cell.setCellValue("聚合问题点纬度");
		sheet.setColumnWidth(4, ("聚合问题点纬度".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(5);
		cell.setCellValue("涉及问题小区");
		sheet.setColumnWidth(5, ("涉及问题小区".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(6);
		cell.setCellValue("日期");
		sheet.setColumnWidth(6, ("日期".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(7);
		cell.setCellValue("数据来源");
		sheet.setColumnWidth(7, ("数据来源".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(8);
		cell.setCellValue("专项标签");
		sheet.setColumnWidth(8, ("专项标签".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(9);
		cell.setCellValue("覆盖场景");
		sheet.setColumnWidth(9, ("覆盖场景".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(10);
		cell.setCellValue("价值标签");
		sheet.setColumnWidth(10, ("价值标签".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(11);
		cell.setCellValue("归属网格");
		sheet.setColumnWidth(11, ("归属网格".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(12);
		cell.setCellValue("涉及LOG/文件名称");
		sheet.setColumnWidth(12, ("涉及LOG/文件名称".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(13);
		cell.setCellValue("问题点类型");
		sheet.setColumnWidth(13, ("问题点类型".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(14);
		cell.setCellValue("初步原因分析");
		sheet.setColumnWidth(14, ("初步原因分析".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(15);
		cell.setCellValue("初步方案");
		sheet.setColumnWidth(15, ("初步方案".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(16);
		cell.setCellValue("原因归类");
		sheet.setColumnWidth(16, ("原因归类".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(17);
		cell.setCellValue("初步优化方案类别");
		sheet.setColumnWidth(17, ("初步优化方案类别".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(18);
		cell.setCellValue("详细原因分析");
		sheet.setColumnWidth(18, ("详细原因分析".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(19);
		cell.setCellValue("详细方案");
		sheet.setColumnWidth(19, ("详细方案".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(20);
		cell.setCellValue("详细原因归类");
		sheet.setColumnWidth(20, ("详细原因归类".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(21);
		cell.setCellValue("详细优化方案类别");
		sheet.setColumnWidth(21, ("详细优化方案类别".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(22);
		cell.setCellValue("调整小区/站点名称");
		sheet.setColumnWidth(22, ("调整小区/站点名称".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(23);
		cell.setCellValue("属性");
		sheet.setColumnWidth(23, ("属性".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(24);
		cell.setCellValue("目标值");
		sheet.setColumnWidth(24, ("目标值".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(25);
		cell.setCellValue("维护子工单状态");
		sheet.setColumnWidth(25, ("维护子工单状态".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(26);
		cell.setCellValue("评估问题是否已解决");
		sheet.setColumnWidth(26, ("评估问题是否已解决".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(27);
		cell.setCellValue("工单流水号");
		sheet.setColumnWidth(27, ("工单流水号".length() + 4) * 512);
		cell.setCellStyle(style);
		String sqlStr = getSqlStr(criteria);
		List<Map<String, Object>> mapList = task.selectMap(conn,
				sqlMap.get("exportClusterList").replace("${sqlStr}", sqlStr));
		Map dataMap = null;
		Date date = null;
		Date dateCurrent = null;
		String clusterCodes = "";
		int questionCount = 0;
		String cities = "";
		String order_code = "";
		String trim_village = "";
		String property = "";
		String target = "";
		String order_state = "";
		if (criteria.get("order_code") != null) {
			order_code = criteria.get("order_code").toString();
		}
		StringBuilder str = new StringBuilder();
		Object city = criteria.get("city");
		if (!isNull(city)) {
			str.append(" and city like '%").append(city.toString()).append("%'");
		}
		List<Map<String, Object>> mapList1 = task.selectMap(conn,
				sqlMap.get("findOldClusterList").replace("${sqlStr}", str.toString()),order_code,order_code);
		mapList.addAll(mapList1);
		for (int i = 0; i < mapList.size(); i++) {
			row = sheet.createRow((int) i + 1);
			// 第四步，创建单元格，并设置值
			Map map = mapList.get(i);

			cell = row.createCell(0);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("聚合问题点序号") != null && Common.judgeString(String.valueOf(map.get("聚合问题点序号")))
					? String.valueOf(map.get("聚合问题点序号")) : "");
			cell = row.createCell(1);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("问题点数量") != null && Common.judgeString(String.valueOf(map.get("问题点数量")))
					? String.valueOf(map.get("问题点数量")) : "");
			cell = row.createCell(2);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("地市") != null && Common.judgeString(String.valueOf(map.get("地市")))
					? String.valueOf(map.get("地市")) : "");
			cell = row.createCell(3);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("聚合问题点经度") != null && Common.judgeString(String.valueOf(map.get("聚合问题点经度")))
					? String.valueOf(map.get("聚合问题点经度")) : "");
			cell = row.createCell(4);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("聚合问题点纬度") != null && Common.judgeString(String.valueOf(map.get("聚合问题点纬度")))
					? String.valueOf(map.get("聚合问题点纬度")) : "");
			cell = row.createCell(5);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("涉及问题小区") != null && Common.judgeString(String.valueOf(map.get("涉及问题小区")))
					? String.valueOf(map.get("涉及问题小区")) : "");
			cell = row.createCell(6);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("日期") != null && Common.judgeString(String.valueOf(map.get("日期")))
					? String.valueOf(map.get("日期")) : "");
			cell = row.createCell(7);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("数据来源") != null && Common.judgeString(String.valueOf(map.get("数据来源")))
					? String.valueOf(map.get("数据来源")) : "");
			cell = row.createCell(8);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("专项标签") != null && Common.judgeString(String.valueOf(map.get("专项标签")))
					? String.valueOf(map.get("专项标签")) : "");
			cell = row.createCell(9);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("覆盖场景") != null && Common.judgeString(String.valueOf(map.get("覆盖场景")))
					? String.valueOf(map.get("覆盖场景")) : "");
			cell = row.createCell(10);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("价值标签") != null && Common.judgeString(String.valueOf(map.get("价值标签")))
					? String.valueOf(map.get("价值标签")) : "");
			cell = row.createCell(11);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("归属网格") != null && Common.judgeString(String.valueOf(map.get("归属网格")))
					? String.valueOf(map.get("归属网格")) : "");
			cell = row.createCell(12);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("涉及LOG/文件名称") != null && Common.judgeString(String.valueOf(map.get("涉及LOG/文件名称")))
					? String.valueOf(map.get("涉及LOG/文件名称")) : "");
			cell = row.createCell(13);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("问题点类型") != null && Common.judgeString(String.valueOf(map.get("问题点类型")))
					? String.valueOf(map.get("问题点类型")) : "");
			cell = row.createCell(14);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("初步原因分析") != null && Common.judgeString(String.valueOf(map.get("初步原因分析")))
					? String.valueOf(map.get("初步原因分析")) : "");
			cell = row.createCell(15);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("初步方案") != null && Common.judgeString(String.valueOf(map.get("初步方案")))
					? String.valueOf(map.get("初步方案")) : "");
			cell = row.createCell(16);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("原因归类") != null && Common.judgeString(String.valueOf(map.get("原因归类")))
					? String.valueOf(map.get("原因归类")) : "");
			cell = row.createCell(17);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("初步优化方案类别") != null && Common.judgeString(String.valueOf(map.get("初步优化方案类别")))
					? String.valueOf(map.get("初步优化方案类别")) : "");
			cell = row.createCell(18);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("详细原因分析") != null && Common.judgeString(String.valueOf(map.get("详细原因分析")))
					? String.valueOf(map.get("详细原因分析")) : "");
			cell = row.createCell(19);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("详细方案") != null && Common.judgeString(String.valueOf(map.get("详细方案")))
					? String.valueOf(map.get("详细方案")) : "");
			cell = row.createCell(20);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("详细原因归类") != null && Common.judgeString(String.valueOf(map.get("详细原因归类")))
					? String.valueOf(map.get("详细原因归类")) : "");
			cell = row.createCell(21);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("详细优化方案类别") != null && Common.judgeString(String.valueOf(map.get("详细优化方案类别")))
					? String.valueOf(map.get("详细优化方案类别")) : "");
			cell = row.createCell(22);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("调整小区/站点名称") != null && Common.judgeString(String.valueOf(map.get("调整小区/站点名称")))
					? String.valueOf(map.get("调整小区/站点名称")) : ""); // 调整小区/站点名称
			cell = row.createCell(23);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("属性") != null && Common.judgeString(String.valueOf(map.get("属性")))
					? String.valueOf(map.get("属性")) : ""); // 属性
			cell = row.createCell(24);
			cell.setCellStyle(style);
			cell.setCellValue(map.get("目标值") != null && Common.judgeString(String.valueOf(map.get("目标值")))
					? String.valueOf(map.get("目标值")) : ""); // 目标值
			cell = row.createCell(25);
			cell.setCellStyle(style);
			cell.setCellValue(order_state); // 维护子工单状态
			cell = row.createCell(26);
			cell.setCellStyle(style);
//			cell.setCellValue(map.get("评估问题是否已解决") != null && Common.judgeString(String.valueOf(map.get("评估问题是否已解决")))
//					? String.valueOf(map.get("评估问题是否已解决")) : "");
			cell.setCellValue("");
			cell = row.createCell(27);
			cell.setCellStyle(style);
			cell.setCellValue(order_code); // 工单流水号

			clusterCodes += "," + map.get("聚合问题点序号");
			questionCount += Common.judgeString(String.valueOf(map.get("问题点数量")))
					? Integer.parseInt(String.valueOf(map.get("问题点数量"))) : 0;
			if (dataMap != null) {
				date = Common.getDate(String.valueOf(dataMap.get("问题点首次发生时间")));
			}
			if (map != null) {
				dateCurrent = Common.getDate(String.valueOf(map.get("问题点首次发生时间")));
			}
			if (date == null) {
				dataMap = map;
			}
			if (date != null && dateCurrent != null) {
				if (date.after(dateCurrent)) {
					dataMap = map;
				}
			}
			if (!cities.contains(String.valueOf(map.get("地市")))) {
				cities += "," + String.valueOf(map.get("地市"));
			}
		}

		// 20170209在1+N跟踪表增加一个sheet,记录聚类问题点的详细问题点
		createClusterDetailSheet(wb);

		if (Common.judgeString(cities)) {
			cities = cities.substring(1);
		}
		if (dataMap != null) {
			if (Common.judgeString(clusterCodes)) {
				clusterCodes = clusterCodes.substring(1);
			}
			dataMap.put("clusterCodes", clusterCodes);
		}
		if (dataMap != null) {
			criteria.put("cluster_code", String.valueOf(dataMap.get("工单编号")));
			criteria.put("question_type", Common.judgeString(String.valueOf(dataMap.get("问题点类型")))
					? String.valueOf(dataMap.get("问题点类型")) : "");
			criteria.put("question_date", String.valueOf(dataMap.get("问题点首次发生时间")));
			criteria.put("cluster_longitude", String.valueOf(dataMap.get("聚合问题点经度")));
			criteria.put("cluster_latitude", String.valueOf(dataMap.get("聚合问题点纬度")));
			criteria.put("involve_site", String.valueOf(dataMap.get("涉及问题小区")));

			criteria.put("cities", cities);
			criteria.put("city", String.valueOf(dataMap.get("地市")));
			criteria.put("cluster_code", String.valueOf(dataMap.get("聚合问题点序号")));
			criteria.put("question_section", String.valueOf(dataMap.get("问题点路段")));
			criteria.put("clusterCount", clusterCodes.split(",").length);
			criteria.put("questionCount", questionCount);

		}
		wb.write(os);
	}

	/**
	 * 20170209在1+N跟踪表增加一个sheet,记录聚类问题点的详细问题点
	 * @param wb
	 * @throws SQLException 
	 */
	public void createClusterDetailSheet(Workbook wb) throws SQLException {

		// 创建方案库问题点详细附件表sheet
		org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("方案库问题点详细附件表");

		// 创建sheet标题行
		Row row = sheet.createRow(0);

		// 第四步，创建单元格，并设置值表头 设置表头居中
		CellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setFillBackgroundColor(HSSFColor.BLUE.index);

		// 设置标题
		org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
		cell.setCellValue("聚合问题点序号");
		sheet.setColumnWidth(0, ("聚合问题点序号".length() + 4) * 512);
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("问题点编号");
		sheet.setColumnWidth(1, ("问题点编号".length() + 4) * 512);
		cell.setCellStyle(style);

		// 获取问题点数据
		String querySql = sqlMap.get("queryDetailProblem").replace("${tableName}",
				criteria.get("rkhcTablename").toString());
		String order_code = criteria.get("orderCode").toString();
		List<Map<String, Object>> result = task.selectMap(conn, querySql, order_code, order_code);

		// 将问题点写入sheet中
		if (result != null && result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				row = sheet.createRow(i + 1);
				// 第四步，创建单元格，并设置值
				Map<String, Object> map = result.get(i);

				cell = row.createCell(0);
				cell.setCellStyle(style);
				cell.setCellValue(
						map.get("vcjlplbnum") != null && Common.judgeString(String.valueOf(map.get("vcjlplbnum")))
								? String.valueOf(map.get("vcjlplbnum")) : "");
				cell = row.createCell(1);
				cell.setCellStyle(style);
				cell.setCellValue(
						map.get("vcproblemnum") != null && Common.judgeString(String.valueOf(map.get("vcproblemnum")))
								? String.valueOf(map.get("vcproblemnum")) : "");
			}
		}

	}

	/**
	 * 根据条件拼接sql语句
	 * @param condition
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getSqlStr(QueryCriteria condition) {
		StringBuilder sqlStr = new StringBuilder();
		Object city = condition.get("city");
		if (!isNull(city)) {
			sqlStr.append(" and city like '%").append(city.toString()).append("%'");
		}
		Object question_type = condition.get("question_type");
		if (!isNull(question_type)) {
			sqlStr.append(" and question_type in ").append(question_type.toString());
		}
		Object data_source = condition.get("data_source");
		if (!isNull(data_source)) {
			sqlStr.append(" and data_source like '%").append(data_source.toString()).append("%'");
		}
		Object process_progress = condition.get("process_progress");
		if (!isNull(process_progress)) {
			sqlStr.append(" and process_progress like '%").append(process_progress.toString()).append("%'");
		}
		Object start_date = condition.get("start_date");
		if (!isNull(start_date)) {
			sqlStr.append(" and question_date >= '").append(start_date.toString()).append("'");
		}
		Object end_date = condition.get("end_date");
		if (!isNull(end_date)) {
			sqlStr.append(" and question_date <= '").append(end_date.toString()).append("'");
		}
		Object order_code = condition.get("order_code");
		if (!isNull(order_code)) {
			sqlStr.append(" and order_code = '").append(order_code.toString()).append("'");
		}
		Object isThird = condition.get("isThird");
		Object clusterCodeListObj = condition.get("clusterCodeList");
		HashSet<String> clusterCodeList = clusterCodeListObj == null ? null : (HashSet<String>) clusterCodeListObj;

		if (isNull(isThird) && clusterCodeList != null && clusterCodeList.size() > 0) {
			sqlStr.append(" and cluster_code in (");
			StringBuilder sbBuilder = new StringBuilder();
			for (String str : clusterCodeList) {
				sbBuilder.append("'").append(str).append("',");
			}
			sqlStr.append(sbBuilder.substring(0, sqlStr.length() - 1)).append(")");
		}

		if (clusterCodeList == null || clusterCodeList.size() < 1) {
			sqlStr.append(" and 1<>1 ");
		}
		return sqlStr.toString();
	}

	/**
	 * 判断是否为空
	 * @return
	 */
	public boolean isNull(Object str) {
		if (str == null) {
			return true;
		}
		if ("".equals(str.toString())) {
			return true;
		}
		if ("null".equals(str.toString())) {
			return true;
		}
		return false;
	}

	// 1+N推送文件
	private boolean pushAddAttachment(String orderCode, String resultMessage, String filePaths, long clusterCount,
			String realPath, QueryCriteria criteria) {

		boolean flag = true;
		try {
			FtpToolkit kit = new FtpToolkit(task.getParpams().get("ftpip"), task.getFtpUser(), task.getFtpPassword());
			String[] resultMessageArr = resultMessage.split("\\|");
			if (resultMessageArr != null && resultMessageArr.length == 3) {
				// 1+N的附件和另外两个附件都要上传到ftp上
				filePaths = ("分析定位_" + filePaths).replace(" ", "").replace(",", ",分析定位_");
				filePaths = resultMessageArr[0] + "," + filePaths;
				if (Common.judgeString(filePaths)) {
					String fileName = "";
					for (String filePath : filePaths.split(",")) {
						filePath = filePath.trim();
						logger.info(realPath + "---" + realPath.startsWith("/opt"));
						fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
						kit.upload(realPath + "/" + fileName, fileName, "/");
					}
				}
				Map<String, Object> query = new HashMap<String, Object>();
				query.put("order_code", orderCode);
				query.put("clusterCount", clusterCount);
				query.put("questionCount", this.criteria.get("problemCount"));

				// 1+N派单
				Map<String, Object> setAttachParams = setAttachParams(query, filePaths);
				flag = collectAttachment(setAttachParams, false);

				// 派单成功才进行高速派单
				Object gaosuCode = criteria.get("gaosuOrderCode");
				if (flag && gaosuCode != null) {

					// 20170301高速派单,不用更新工单状态,等派完一起更新
					setAttachParams.put("analy_result", "");
					setAttachParams.put("file_name", "");
					setAttachParams.put("eoms_ordernum", gaosuCode);
					flag = collectAttachment(setAttachParams, true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("1+N推送文件出错：" + e.getMessage());
			task.addDetaileLog(logNum++, "1+N推送文件出错：" + e.getMessage());
			flag = false;
		}
		return flag;
	}

	/**
	 * 组织调用接口参数
	 * @param query
	 * @param fileName
	 * @return
	 */
	public Map<String, Object> setAttachParams(Map<String, Object> query, String fileName) {
		String analyResult = "该工单的问题点数量为" + query.get("problemCount") + ",聚类问题点数量为" + query.get("clusterCount");
		query.put("user_name", "jk_LTE");
		query.put("password", "jk_LTE");
		query.put("command_type", "8");
		query.put("command_sub_type", "5");
		query.put("eoms_ordernum", query.get("order_code"));
		query.put("analy_result", analyResult);
		query.put("file_name", fileName);
		query.put("operator_city", "省公司");
		query.put("operator_department", "");
		query.put("operator_offices", "");
		query.put("operator", "方案库");
		query.put("telephone", "");

		return query;
	}

	/**
	 * 调用1+N派单接口
	 * @param query 参数对象
	 * @param isThirdFlow 是否更新工单状态,false更新,true不更新
	 * @return
	 */
	public boolean collectAttachment(Map<String, Object> query, boolean isThirdFlow) {
		boolean flag = true;
		String xmlString = "";
		String resultXml = "";
		String[] resultInfo = null;
		String eoms_ordernum = "";
		try {

			Element root = new Element("Item");
			root.setAttribute("name", "Param");
			root.setAttribute("typeName", "IDictionary");

			Document doc = new Document(root);

			Element elements = new Element("Item");
			elements.setAttribute("name", "UserName");
			elements.setAttribute("typeName", "String");
			root.addContent(elements
					.setText(null == query.get("user_name") || (String.valueOf(query.get("user_name")) == "null") ? ""
							: String.valueOf(query.get("user_name"))));

			Element elements2 = new Element("Item");
			elements2.setAttribute("name", "Password");
			elements2.setAttribute("typeName", "String");
			root.addContent(
					elements2.setText(null == query.get("password") || (String.valueOf(query.get("password")) == "null")
							? "" : String.valueOf(query.get("password"))));

			Element elements3 = new Element("Item");
			elements3.setAttribute("name", "CommandType");
			elements3.setAttribute("typeName", "Int32");
			root.addContent(elements3
					.setText(null == query.get("command_type") || (String.valueOf(query.get("command_type")) == "null")
							? "" : String.valueOf(query.get("command_type"))));

			Element elements4 = new Element("Item");
			elements4.setAttribute("name", "CommandSubType");
			elements4.setAttribute("typeName", "Int32");
			root.addContent(elements4.setText(
					null == query.get("command_sub_type") || (String.valueOf(query.get("command_sub_type")) == "null")
							? "" : String.valueOf(query.get("command_sub_type"))));

			Element elements5 = new Element("Item");
			elements5.setAttribute("name", "CommandParam");
			elements5.setAttribute("typeName", "IEnumerable");
			root.addContent(elements5);
			// EOMS的工单编号
			if (null == query.get("eoms_ordernum") || (String.valueOf(query.get("eoms_ordernum")) == "null")) {
			} else {
				eoms_ordernum = String.valueOf(query.get("eoms_ordernum"));
			}

			Element elements6 = new Element("Item");
			elements6.setAttribute("typeName", "String");
			elements5.addContent(elements6.setText(eoms_ordernum));

			// 工单字段信息
			Element elements11 = new Element("Item");
			elements11.setAttribute("typeName", "IDictionary");
			elements5.addContent(elements11);

			// 分析定位说明内容
			Element elements133 = new Element("Item");
			elements133.setAttribute("name", "Analy_result");
			elements133.setAttribute("typeName", "String");
			elements11.addContent(elements133
					.setText(null == query.get("analy_result") || (String.valueOf(query.get("analy_result")) == "null")
							? "" : String.valueOf(query.get("analy_result"))));

			// 附件列表
			Element elements134 = new Element("Item");
			elements134.setAttribute("name", "Flie_list");
			elements134.setAttribute("typeName", "String");
			elements11.addContent(elements134
					.setText(null == query.get("file_name") || (String.valueOf(query.get("file_name")) == "null") ? ""
							: String.valueOf(query.get("file_name"))));

			// 操作人地市
			Element elements135 = new Element("Item");
			elements135.setAttribute("name", "Operator_city");
			elements135.setAttribute("typeName", "String");
			elements11.addContent(elements135.setText(
					null == query.get("operator_city") || (String.valueOf(query.get("operator_city")) == "null") ? ""
							: String.valueOf(query.get("operator_city"))));

			// 操作人部门
			Element elements136 = new Element("Item");
			elements136.setAttribute("name", "Operator_Department");
			elements136.setAttribute("typeName", "String");
			elements11.addContent(elements136.setText(null == query.get("operator_department")
					|| (String.valueOf(query.get("operator_department")) == "null") ? ""
							: String.valueOf(query.get("operator_department"))));

			// 操作人科室
			Element elements137 = new Element("Item");
			elements137.setAttribute("name", "Operator_Offices");
			elements137.setAttribute("typeName", "String");
			elements11.addContent(elements137.setText(
					null == query.get("operator_offices") || (String.valueOf(query.get("operator_offices")) == "null")
							? "" : String.valueOf(query.get("operator_offices"))));

			// 操作人姓名
			Element elements138 = new Element("Item");
			elements138.setAttribute("name", "Operator_Username");
			elements138.setAttribute("typeName", "String");
			elements11.addContent(elements138
					.setText(null == query.get("operator") || (String.valueOf(query.get("operator")) == "null") ? ""
							: String.valueOf(query.get("operator"))));

			// 操作人联系电话
			Element elements139 = new Element("Item");
			elements139.setAttribute("name", "Operator_Tel");
			elements139.setAttribute("typeName", "String");
			elements11.addContent(elements139
					.setText(null == query.get("telephone") || (String.valueOf(query.get("telephone")) == "null") ? ""
							: String.valueOf(query.get("telephone"))));

			String path = task.getParpams().get("resultxmlpath");
			xmlString = Common.buildXmlFile(doc, path);
			logger.info("xmlString:" + xmlString);

			resultXml = ProblemUtil.executeMethod(task.getParpams().get("commandwsdl"), "Execute", xmlString);
			task.addDetaileLog(logNum++, "resultXml:" + resultXml);
			resultInfo = Common.convertStringToXml(resultXml);
			if (resultInfo == null || resultInfo.length < 3 || "False".equals(resultInfo[0])) {
				return false;
			}
			if (!isThirdFlow) {
				flag = updateClusterState(query, resultInfo[2]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
			logger.error("1+N派单出错：", e);
			task.addDetaileLog(logNum++, "1+N派单出错：" + e.getMessage());
		} finally {
			saveLog(xmlString, resultXml, resultInfo, eoms_ordernum, "");
		}
		return flag;
	}

	/**
	 * 保存日志
	 * @param xmlString
	 * @param resultXml
	 * @param resultInfo
	 * @param eoms_ordernum
	 * @param type
	 * @return
	 */
	private boolean saveLog(String xmlString, String resultXml, String[] resultInfo, String eoms_ordernum,
			String type) {

		boolean flag = true;
		try {
			if (resultInfo != null && resultInfo.length >= 3) {

				task.execute(logNum++, conn, sqlMap.get("saveLog"), resultInfo[0], xmlString, resultXml, resultInfo[2],
						(eoms_ordernum + type), new java.sql.Date(System.currentTimeMillis()), resultInfo[1]);
			} else {
				flag = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
			logger.error("保存日志出错：", e);
			task.addDetaileLog(logNum++, "保存日志出错：" + e.getMessage());
		}
		return flag;
	}

	/**
	 * 保存日志
	 * @param xmlData
	 * @param resultCode
	 * @param resultMessage
	 * @param resultInfo
	 * @param orderCode
	 * @return
	 */
	private boolean saveLog(String xmlData, String resultCode, String resultMessage, String resultInfo,
			String orderCode) {

		boolean flag = true;
		try {
			task.execute(logNum++, conn, sqlMap.get("saveLog"), resultCode, xmlData, resultInfo, orderCode, orderCode,
					new java.sql.Date(System.currentTimeMillis()), resultMessage);
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
			logger.error("保存日志出错：", e);
			task.addDetaileLog(logNum++, "保存日志出错：" + e.getMessage());
		}
		return flag;
	}

	private boolean updateClusterState(Map<String, Object> query, String eomsOrdernum) throws SQLException {

		// 事务控制
		conn.setAutoCommit(false);

		boolean flag = true;
		try {
			String clusterCodes = String.valueOf(query.get("clusterCodes"));
			if (Common.judgeString(clusterCodes)) {
				List<String> clusterCodeList = Arrays.asList(clusterCodes.split(","));
				StringBuilder sBuilder = new StringBuilder();
				for (String data : clusterCodeList) {
					sBuilder.append("'").append(data).append("',");
				}
				String datas = sBuilder.substring(0, sBuilder.length() - 1);
//				task.execute(logNum++, conn, sqlMap.get("updateNullClusterQuestion").replace("${datas}", datas));
				task.execute(logNum++, conn, sqlMap.get("updateClusterState").replace("${datas}", datas), "初步方案制定",
						eomsOrdernum);
			}
			conn.commit();
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
			conn.rollback();
		} finally {
			conn.setAutoCommit(true);
		}
		return flag;
	}

	/**
	 * 更新此工单的所有信令问题点
	 * @param criteria
	 * @throws SQLException
	 */
	public void updateXLproInfo(QueryCriteria criteria) throws SQLException {

		// 事务控制
		conn.setAutoCommit(false);

		String[] tablenames = new String[] { "xl_gtgr_cell_month", "xl_gtrfg_cell_month", "xl_lyrfg_cell_month",
				"xl_xldlfg_grid_month", "xl_xldlgr_grid_month" };
		criteria.put("xl_state", 1);
		try {
			String table1 = criteria.get("tableName1").toString();
			String sql = sqlMap.get("updateXLproInfo").replace("${tableName1}", table1);
			for (int i = 0; i < tablenames.length; i++) {
				criteria.put("tableName2", tablenames[i]);
				String newSql = sql.replace("${tableName2}", tablenames[i]);
				task.execute(logNum++, conn, newSql, 1, criteria.get("order_code"));
			}
			// 更新完就删除此工单临时表
			task.execute(logNum++, conn, sqlMap.get("delteTable1").replace("${tableName1}", table1));

			// 提交事务
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			conn.rollback();
			logger.error("更新此工单的所有信令问题点出错：", e);
			task.addDetaileLog(logNum++, "更新此工单的所有信令问题点出错：" + e.getMessage());
			throw new SQLException(e);
		} finally {
			conn.setAutoCommit(true);
		}

	}

	/**
	 * 判断标题第一列是否是问题点编号或者所属聚类编号
	 * @param sheet
	 * @return
	 */
	public String validQuestionCodeTitle(org.apache.poi.ss.usermodel.Sheet sheet) {
		String message = "";
		String title = String.valueOf(getCellValue(sheet, 0, 0));
		String title2 = String.valueOf(getCellValue(sheet, 0, 1));
		if (title.contains("问题点编号") || (title.contains("所属聚类编号") && title2.contains("问题点编号"))) {
		} else {
			message = "导入失败，excel表中‘" + sheet.getSheetName() + "‘的第一列的标题应为问题点编号或所属聚类编号!";
		}
		return message;
	}

	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	public Object getCellValue(org.apache.poi.ss.usermodel.Sheet sheet, int row, int column) {
		Object sheetTitle = null;
		if (sheet != null) {
			Row rows = sheet.getRow(row);
			if (rows != null) {
				org.apache.poi.ss.usermodel.Cell cell = rows.getCell(column);
				if (cell != null) {
					int cellType = cell.getCellType();
					if (cellType == XSSFCell.CELL_TYPE_STRING) {
						sheetTitle = cell.getStringCellValue();
						return sheetTitle;
					}
					if (cellType == XSSFCell.CELL_TYPE_NUMERIC) {
						try {
							if (DateUtil.isCellDateFormatted(cell)) {
								sheetTitle = format.format(cell.getDateCellValue());
							} else {
								sheetTitle = Common.convertInteger(String.valueOf(cell.getNumericCellValue()));
							}
						} catch (Exception e) {
							logger.error("获取cell内容出错row[" + row + "]cell[" + column + "]：", e);
						}
					}
				}
			}
		}
		return sheetTitle;
	}

	public List<String> updateExcel(org.apache.poi.ss.usermodel.Sheet sheet, String[] titleArr, Workbook book,
			String order_code) throws Exception {

		boolean flag = true;
		org.apache.poi.ss.usermodel.Cell cell = null;
		// 包括问题点编号和聚类问题点编号
		List<String> list = new ArrayList<String>();
		List<String> clusterCodes = new ArrayList<String>();
		List<String> questionCodes = new ArrayList<String>();

		CellStyle cellStyle = book.createCellStyle();
		cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		cellStyle.setBorderRight(CellStyle.BORDER_THIN);
		cellStyle.setBorderTop(CellStyle.BORDER_THIN);
		cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

		String title = String.valueOf(getCellValue(sheet, 0, 0));

		if (title.contains("问题点编号")) {
			// 添加一列
			// 每列的内容后移

			// 先判断第二行是否为无数据,加快遍历速度
			if (sheet.getPhysicalNumberOfRows() <= 1 || "本周暂无".equals(String.valueOf(getCellValue(sheet, 1, 0)))
					|| "本地暂无".equals(String.valueOf(getCellValue(sheet, 1, 0)))
					|| "暂无".equals(String.valueOf(getCellValue(sheet, 1, 0)))) {
				flag = false;
			} else {
				for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
					cell = insertCell(sheet, i, titleArr.length - 1);
					if (i == 0) {
						cell.setCellStyle(getCell(sheet, 0, 0).getCellStyle());
						sheet.setColumnWidth(titleArr.length - 1, 10 * 2 * 256);
					} else {
						cell.setCellStyle(cellStyle);

					}
					for (int j = titleArr.length - 1; j >= 0; j--) {
						cell = getCell(sheet, i, j);
						if (j == 0) {
							if (i == 0) {
								cell.setCellValue("所属聚类编号");
							} else {
								questionCodes.add(cell.getStringCellValue());
								cell.setCellValue("");
							}
						} else {
							Object obj = getCellValue(sheet, i, j - 1);
							if (cell != null && getCell(sheet, i, j - 1) != null) {
								cell.setCellType(getCell(sheet, i, j - 1).getCellType());
								cell.setCellValue(obj == null ? "" : String.valueOf(obj));
							}
						}
					}
				}
			}
		}
		if (title.contains("所属聚类编号")) {
			// 不用添加一列
			// 不用每列的内容后移
			String cellValue = String.valueOf(getCellValue(sheet, 1, 0));
			for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
				if ("本周暂无".equals(cellValue) || "本地暂无".equals(cellValue) || "暂无".equals(cellValue)) {
					flag = false;
					continue;
				}
				for (int j = titleArr.length - 1; j >= 0; j--) {
					cell = getCell(sheet, i, j);
					if (i != 0) {
						if (j == 1) {
							questionCodes.add(cell.getStringCellValue());
						}
					}
				}
			}
		}

		System.gc();

		if (flag) {
			if (questionCodes != null && !questionCodes.isEmpty()) {
				logger.info("总共要查询:" + questionCodes.size() + "条记录");

				// 通过外部表查询数据,sheet名称匹配
				String sheetName = sheet.getSheetName();
				String querySql = sqlMap.get("orderQueryProblem").replace("${orderTableName}",
						criteria.get("rkhcTablename").toString());
				List<Map<String, Object>> selectMap = task.selectMap(conn, querySql, sheetName);

				if (questionCodes.size() != selectMap.size()) {
					throw new Exception("sheet【" + sheetName + "】问题点数量【" + questionCodes.size() + "】与匹配出来的问题点数量【"
							+ selectMap.size() + "】不一致");
				}

				// 遍历结果集，插入聚类编号大屏sheet
				if (selectMap != null && selectMap.size() > 0) {

					for (Map<String, Object> map : selectMap) {

						// 判断是否历史派单
						Object intstatus = map.get("intstatus");
						intstatus = intstatus == null || "".equals(intstatus) ? 0 : intstatus;
						int status = Integer.valueOf(intstatus.toString());
						if (status != 3) {
							clusterCodes.add(map.get("vcjlplbnum2").toString());
						}
						int rownum = Integer.valueOf(map.get("rownum").toString());
						getCell(sheet, rownum, 0).setCellValue(map.get("vcjlplbnum").toString());
					}

				}

				// 组织返回结果集
				list.add(Common.ListToString(questionCodes, "0").replace(", ", ","));
				list.add(Common.ListToString(clusterCodes, "0").replace(", ", ","));
			}
		}

		/*
		 * criteria.put("tableName", "f_et_plb_vcpro_vcjl_test"); long number =
		 * task.selectOne(logNum++, conn, long.class, sqlMap.get("existTable"),
		 * "f_et_plb_vcpro_vcjl_test"); if (number == 0) {
		 * task.execute(logNum++, conn, sqlMap.get("createProTest")); }
		 */
		return list;
	}

	/**
	 * 根据excel生成csv外部表内容,并导入数据
	 * @param criteria
	 * @param order_code
	 * @param filename
	 * @return
	 * @throws SQLException 
	 */
	public String loadexcelByorder(QueryCriteria criteria, String order_code, String filename) throws SQLException {

		// 外部表建表部分
		String randomStr = "ext_update_" + order_code + "_" + (int) Math.random() * 10;
		// 防止误删除，固定写好要删除的表名开头
		String randomStr2 = order_code + "_" + (int) Math.random() * 10;

		// 先删除外部表
		String deleteTable = sqlMap.get("deleteExtCsv") + randomStr2;
		criteria.put("sql", deleteTable);
		task.execute(logNum++, conn, deleteTable);

		// 创建外部表
		String sql1 = sqlMap.get("createExtCsv").replace("${tableName}", randomStr).replace("${filename}", filename);
		criteria.put("sql", sql1);
		task.execute(logNum++, conn, sql1);

		// 更新外部表数据到物理表
		criteria.put("tablename", randomStr);
		task.execute(logNum++, conn, sqlMap.get("saveTempQuestionCode").replace("${tablename}", randomStr));

		// 执行完毕删除外部表
		criteria.put("sql", deleteTable);
		task.execute(logNum++, conn, deleteTable);

		return "";
	}

	private org.apache.poi.ss.usermodel.Cell insertCell(org.apache.poi.ss.usermodel.Sheet sheet, int row, int column) {
		org.apache.poi.ss.usermodel.Cell cell = null;
		if (sheet != null) {
			Row rows = sheet.getRow(row);
			if (rows != null) {
				cell = rows.createCell(column);
			}
		}
		return cell;
	}

	private org.apache.poi.ss.usermodel.Cell getCell(org.apache.poi.ss.usermodel.Sheet sheet, int row, int column) {
		org.apache.poi.ss.usermodel.Cell cell = null;
		if (sheet != null) {
			Row rows = sheet.getRow(row);
			if (rows != null) {
				cell = rows.getCell(column);
			}
		}
		return cell;
	}

	public void delteTable1(QueryCriteria criteria) throws SQLException {
		// 切换IQ数据库
		String tablename = criteria.get("tableName1").toString();
		logger.info(tablename);
		if (tablename.split("_")[0].equals("temp")) {
			task.execute(logNum++, conn, sqlMap.get("delteTable1").replace("${tableName1}", tablename));
		}
	}

	/**
	 * 根据问题点编号来辨别所属原始类
	 *
	 **/
	public String FindTableName(String code) {
		String tablename = "";
		String Num = "";
		// 获取code第一个'-'和第二个'-'之间字符
		Num = code.split("-")[1];
		if (Num.equals("GHNL")) {
			tablename = "mt_2ggllhznolte_cell"; // 1.2G高流量宏站无LTE覆盖小区
		} else if (Num.equals("GSNL")) {
			tablename = "mt_2ggllsfnolte_cell"; // 2.2G高流量室分无LTE覆盖小区
		} else if (Num.equals("GHF")) {
			tablename = "mt_gsmgllxq_cell"; // 3.GSM高流量小区（一周）
		} else if (Num.equals("LCG")) {
			tablename = "mt_ltecgxq_cell"; // 4.LTE超高站小区
		} else if (Num.equals("LCJ")) {
			tablename = "mt_ltecjxq_cell"; // 5.LTE超近站小区
		} else if (Num.equals("LCY")) {
			tablename = "mt_ltecyxq_cell"; // 6.LTE超远站小区
		} else if (Num.equals("TPGCDFGXQ")) {
			tablename = "mt_tpgcdfgxq_cell"; // 同频高重叠覆盖小区
		} else if (Num.equals("CD")) {
			tablename = "mt_cdfgxx_cell"; // 7.LTE高重叠覆盖路段详细信息
		} else if (Num.equals("GFHN")) {
			tablename = "mt_gfhdhrxq_cell"; // 8.高负荷待扩容小区
		} else if (Num.equals("XZ10") || Num.equals("SC") || Num.equals("XZ2")) {
			tablename = "mt_yyscxz_cell"; // 9.LTE上传低速率路段(<=512K)
											// ,LTE下载低速率路段(<=10M),LTE下载低速率路段(<=2M)
		} else if (Num.equals("CSFB")) {
			tablename = "mt_csfbcgl_cell"; // 10.CSFB事件列表
		} else if (Num.equals("GRQ")) {
			tablename = "mt_gsmlxcld_cell"; // 11.GSM连续质差路段
		} else if (Num.equals("VARTP")) {
			tablename = "mt_voltertodbsj_cell"; // 12.VOLTE_RTP丢包事件
		} else if (Num.equals("VADrop")) {
			tablename = "mt_voltedhsj_cell"; // 13.VOLTE掉话事件
		} else if (Num.equals("VAMOS")) {
			tablename = "mt_voltecxrmossj_cell"; // 14.VOLTE_持续弱MOS事件
		} else if (Num.equals("VABlock")) {
			tablename = "mt_voltenoconnection_cell"; // 15.VOLTE未接通事件
		} else if (Num.equals("S0") || Num.equals("SF3")) {
			tablename = "mt_lxzc_cell"; // 16.LTE连续质差路段(SINR≤0dB),LTE连续质差路段(SINR≤-3dB)
		} else if (Num.equals("FG100") || Num.equals("FG110")) {
			tablename = "mt_rfg_cell"; // 17.LTE弱覆盖路段(RSRP≤-100),LTE弱覆盖路段(RSRP≤-110)
		} else if (Num.equals("LMR")) {
			tablename = "mt_ltemrrfgxq_cell"; // 18.LTE MR弱覆盖小区
		} else if (Num.equals("LQWLYJZDSXQ")) {
			tablename = "mt_qwlyjzdsxq_cell"; // 全网劣于竞争对手小区
		} else if (Num.equals("GDH")) {
			tablename = "mt_gsmwxdhl_cell"; // 19. GSM无线掉话率（一周出现大于3次低于目标值）小区
		} else if (Num.equals("GJT")) {
			tablename = "mt_gsmwxjtl_cell"; // 20.GSM无线接通率（一周出现大于3次低于目标值）小区
		} else if (Num.equals("GZC")) {
			tablename = "mt_gsmzchwbl_cell"; // 21.GSM质差话务比例
		} else if (Num.equals("LDX")) {
			tablename = "mt_ltewxdxl_cell"; // 22.LTE掉线高小区
		} else if (Num.equals("LJT")) {
			tablename = "mt_ltewxjtl_cell"; // 23.LTE接通低小区
		} else if (Num.equals("LQH")) {
            tablename = "mt_lteqhcxq_cell"; // LTE切换差小区
        } else if (Num.equals("LGLL")) {
            tablename = "mt_4ggllwtyzxq_cell"; // 高流量问题严重小区
        } else if (Num.equals("GTDL")) {
			tablename = "mt_dlxqmx_cell"; // 24.倒流小区明细
		} else if (Num.equals("LGGR")) {
			tablename = "mt_ggrxq_cell"; // 25.高干扰小区
		} else if (Num.equals("LLLL")) {
			tablename = "mt_ltelllxq_cell"; // LTE零流量小区
		}
		// else if(Num.equals("")){
		// tablename="MT_CDFGGY_CELL"; //26.LTE高重叠覆盖路段概要信息
		// }
		else if (Num.equals("VQH")) {
			tablename = "mt_esvrccqhcxq_cell"; // 27.eSVRCC切换差小区
		} else if (Num.equals("VDXSP")) {
			tablename = "mt_volteerabdxgxqsp_cell"; // 28.VoLTE E-RAB掉线高小区(视频)
		} else if (Num.equals("VDXYY")) {
			tablename = "mt_volteerabdxgxqyy_cell"; // 29.VoLTE E-RAB掉线高小区(语音)
		} else if (Num.equals("VAeSRVCC")) {
			tablename = "mt_volteesrvccqhsbsj_cell"; // 30.VOLTE_eSRVCC切换失败事件
		} else if (Num.equals("VJTSP")) {
			tablename = "mt_voltewxjtcxqsp_cell"; // 31.VoLTE无线接通差小区(视频
		} else if (Num.equals("VJTYY")) {
			tablename = "mt_voltewxjtcxqyy_cell"; // 32.VoLTE无线接通差小区(语音
		} else if (Num.equals("VSY")) {
			tablename = "mt_voltexxgsyxq_cell"; // 33.VoLTE下行高时延小区
		} else if (Num.equals("VYHQH")) {
			tablename = "mt_volteyhqhcxq_cell"; // 34.VOLTE用户切换差小区
		} else if (Num.equals("CZQH")) {
			tablename = "mt_qci2czqhcxq_cell"; // 35.QCI2承载切换差小区
		} else if (Num.equals("VAIMS")) {
			tablename = "mt_volteimszcsbsj_cell"; // 36.volte_ims注册失败事件
		} else if (Num.equals("VAeSRVCCDelay")) {
			tablename = "mt_volteesrvccqhsyyhm_cell"; // 37.volte_esrvcc切换时延-用户面（ms）
		} else if (Num.equals("VADelay")) {
			tablename = "mt_voltehjjlsy_cell"; // 38.VOLTE呼叫建立时延
		} else {
			String ss = code.split("-")[2];
			if (ss.equals("DLGRXL")) {
				tablename = "yt_xldlgr_grid_cell"; // 39.信令道路干扰问题点
			} else if (ss.equals("DLFGXL")) {
				tablename = "yt_xldlfg_grid_cell"; // 40.信令道路覆盖问题点
			}
		}

		return tablename;
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
