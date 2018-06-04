package com.etone.universe.dmp.problem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.etone.universe.dmp.util.Common;

/**
 * 
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年3月30日  上午9:57:04
 */
public class MrTest {
	
	public static void main(String[] args) {
		String string = "lte_rsrp_day_cell|yy_lte_rsrp_day_cell";
		String[] split = string.split("\\|");
		System.out.println(split[0]+split[1]);
	}

	public static void maixxn(String[] args) throws Exception {
		/*for (int i = 0; i < 1; i++) {
			System.out.println("2017-01-01".substring(0, 4) + "  "
					+ "2017-01-01".substring(5, 7) + "  "
					+ "2017-01-01".substring(8));
			List dateStr = getDateStr("2017-01-01", "2017-02-01");
		}*/
		//QueryCriteria criteria = new QueryCriteria();
		//OutputStream os = new FileOutputStream("F:/test/111.xlsx");
		//exportDispatch(os,criteria);
		
		List<Runnable> listRun = new ArrayList<Runnable>();
		for(int i=0;i<1;i++){
			
			listRun.add(new Runnable() {
				
				@Override
				public void run() {
					QueryCriteria criteria = new QueryCriteria();
					OutputStream os;
					try {
						os = new FileOutputStream("F:/test/111.xlsx");
						// TODO Auto-generated method stub
						//MrTest.exportDispatch(os,criteria);
						MrTest.exportDispatchWrit(os,criteria);
						os.flush();
						os.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			});
		}
		
		for(Runnable runnable : listRun){
			new Thread(runnable).start();
		}
		
		Thread.sleep(30000);
		
	}

	/**
	 * 获取两个时间点之间的日期
	 */
	@SuppressWarnings({ "unchecked", "static-access", "unused", "rawtypes" })
	private static List getDateStr(String timeStart, String timeEnd) {
		List dateList = new ArrayList();
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.set(Integer.parseInt(timeStart.substring(0, 4)),
				Integer.parseInt(timeStart.substring(5, 7)) - 1,
				Integer.parseInt(timeStart.substring(8)));
		calEnd.set(Integer.parseInt(timeEnd.substring(0, 4)),
				Integer.parseInt(timeEnd.substring(5, 7)) - 1,
				Integer.parseInt(timeEnd.substring(8)));
		long dayCount = (calEnd.getTimeInMillis() - calStart.getTimeInMillis())
				/ (1000 * 60 * 60 * 24L);
		String monthStr, dayStr;
		Integer year, month, day;
		for (int i = 0; i < dayCount; i++) {
			//System.out.println("#########进入:【"+i+"】");
			year = calStart.get(Calendar.YEAR);
			month = calStart.get(Calendar.MONTH) + 1;
			if (month < 10) {
				monthStr = "0" + month;
			} else {
				monthStr = month.toString();
			}
			day = calStart.get(calStart.DAY_OF_MONTH);
			if (day < 10) {
				dayStr = "0" + day;
			} else {
				dayStr = day.toString();
			}
			System.out.println(year + monthStr + dayStr);
			dateList.add(year + monthStr + dayStr);
			calStart.add(Calendar.DAY_OF_MONTH, 1);
		}
		return dateList;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void exportDispatchWrit(OutputStream os, QueryCriteria criteria)

			throws Exception {

		// 第一步，创建一个webbook，对应一个Excel文件
		Workbook wb = new XSSFWorkbook(new FileInputStream("F:/test/222.xlsx"));
		// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
		org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheet("方案库派单附件表");
		// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
		Row row = sheet.getRow((int) 0);
		// 第四步，创建单元格，并设置值表头 设置表头居中
		CellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setFillBackgroundColor(HSSFColor.BLUE.index);

		org.apache.poi.ss.usermodel.Cell cell = row.getCell(0);
		cell.setCellValue("聚合问题点序号");
		sheet.setColumnWidth(0, ("聚合问题点序号".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(1);
		cell.setCellValue("问题点数量");
		sheet.setColumnWidth(1, ("问题点数量".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(2);
		cell.setCellValue("地市");
		sheet.setColumnWidth(2, ("地市".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(3);
		cell.setCellValue("聚合问题点经度");
		sheet.setColumnWidth(3, ("聚合问题点经度".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(4);
		cell.setCellValue("聚合问题点纬度");
		sheet.setColumnWidth(4, ("聚合问题点纬度".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(5);
		cell.setCellValue("涉及问题小区");
		sheet.setColumnWidth(5, ("涉及问题小区".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(6);
		cell.setCellValue("日期");
		sheet.setColumnWidth(6, ("日期".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(7);
		cell.setCellValue("数据来源");
		sheet.setColumnWidth(7, ("数据来源".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(8);
		cell.setCellValue("归属网格");
		sheet.setColumnWidth(8, ("归属网格".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(9);
		cell.setCellValue("涉及LOG/文件名称");
		sheet.setColumnWidth(9, ("涉及LOG/文件名称".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(10);
		cell.setCellValue("问题点类型");
		sheet.setColumnWidth(10, ("问题点类型".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(11);
		cell.setCellValue("初步原因分析");
		sheet.setColumnWidth(11, ("初步原因分析".length() + 10) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(12);
		cell.setCellValue("初步方案");
		sheet.setColumnWidth(12, ("初步方案".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(13);
		cell.setCellValue("原因归类");
		sheet.setColumnWidth(13, ("原因归类".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(14);
		cell.setCellValue("初步优化方案类别");
		sheet.setColumnWidth(14, ("初步优化方案类别".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(15);
		cell.setCellValue("详细原因分析");
		sheet.setColumnWidth(15, ("详细原因分析".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(16);
		cell.setCellValue("详细方案");
		sheet.setColumnWidth(16, ("详细方案".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(17);
		cell.setCellValue("详细原因归类");
		sheet.setColumnWidth(17, ("详细原因归类".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(18);
		cell.setCellValue("详细优化方案类别");
		sheet.setColumnWidth(18, ("详细优化方案类别".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(19);
		cell.setCellValue("调整小区/站点名称");
		sheet.setColumnWidth(19, ("调整小区/站点名称".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(20);
		cell.setCellValue("属性");
		sheet.setColumnWidth(20, ("属性".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(21);
		cell.setCellValue("目标值");
		sheet.setColumnWidth(21, ("目标值".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(22);
		cell.setCellValue("维护子工单状态");
		sheet.setColumnWidth(22, ("维护子工单状态".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(23);
		cell.setCellValue("评估问题是否已解决");
		sheet.setColumnWidth(23, ("评估问题是否已解决".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.getCell(24);
		cell.setCellValue("工单流水号");
		sheet.setColumnWidth(24, ("工单流水号".length() + 4) * 512);
		//cell.setCellStyle(style);
		
		
		List<Map> mapList = mapList = exportClusterList2(criteria);

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
		for (int i = 0; i < mapList.size(); i++) {
			System.out.println(System.currentTimeMillis()+"  "+i);
			row = sheet.createRow((int) i + 1);
			// 第四步，创建单元格，并设置值
			Map map = mapList.get(i);

			//String reason=map.get("初步原因分析").toString();

			// String type=map.get("问题点类型").toString();

			cell = row.createCell(0);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("聚合问题点序号") != null
					&& Common.judgeString(String.valueOf(map.get("聚合问题点序号"))) ? String
					.valueOf(map.get("聚合问题点序号")) : "");
			cell = row.createCell(1);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("问题点数量") != null
					&& Common.judgeString(String.valueOf(map.get("问题点数量"))) ? String
					.valueOf(map.get("问题点数量")) : "");
			cell = row.createCell(2);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("地市") != null
					&& Common.judgeString(String.valueOf(map.get("地市"))) ? String
					.valueOf(map.get("地市")) : "");
			cell = row.createCell(3);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("聚合问题点经度") != null
					&& Common.judgeString(String.valueOf(map.get("聚合问题点经度"))) ? String
					.valueOf(map.get("聚合问题点经度")) : "");
			cell = row.createCell(4);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("聚合问题点纬度") != null
					&& Common.judgeString(String.valueOf(map.get("聚合问题点纬度"))) ? String
					.valueOf(map.get("聚合问题点纬度")) : "");
			cell = row.createCell(5);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("涉及问题小区") != null
					&& Common.judgeString(String.valueOf(map.get("涉及问题小区"))) ? String
					.valueOf(map.get("涉及问题小区")) : "");
			cell = row.createCell(6);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("日期") != null
					&& Common.judgeString(String.valueOf(map.get("日期"))) ? String
					.valueOf(map.get("日期")) : "");
			cell = row.createCell(7);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("数据来源") != null
					&& Common.judgeString(String.valueOf(map.get("数据来源"))) ? String
					.valueOf(map.get("数据来源")) : "");
			cell = row.createCell(8);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("归属网格") != null
					&& Common.judgeString(String.valueOf(map.get("归属网格"))) ? String
					.valueOf(map.get("归属网格")) : "");
			cell = row.createCell(9);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("涉及LOG/文件名称") != null
					&& Common.judgeString(String.valueOf(map.get("涉及LOG/文件名称"))) ? String
					.valueOf(map.get("涉及LOG/文件名称")) : "");
			cell = row.createCell(10);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("问题点类型") != null
					&& Common.judgeString(String.valueOf(map.get("问题点类型"))) ? String
					.valueOf(map.get("问题点类型")) : "");
			cell = row.createCell(11);
			//cell.setCellStyle(style);
			if (map.get("初步原因分析") != null
					&& map.get("初步原因分析").toString().length() > 32766) {
				System.out.println(map.get("聚合问题点序号") + "长度为:"
						+ map.get("初步原因分析").toString().length());
				//超过excel单元格最大长度，只截取前面30000长度部分
				String maxstring = map.get("初步原因分析").toString()
						.substring(0, 30000);
				cell.setCellValue(maxstring);

			} else {
				cell.setCellValue(map.get("初步原因分析") != null
						&& Common.judgeString(String.valueOf(map.get("初步原因分析"))) ? String
						.valueOf(map.get("初步原因分析")) : "");
			}
			cell = row.createCell(12);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("初步方案") != null
					&& Common.judgeString(String.valueOf(map.get("初步方案"))) ? String
					.valueOf(map.get("初步方案")) : "");
			cell = row.createCell(13);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("原因归类") != null
					&& Common.judgeString(String.valueOf(map.get("原因归类"))) ? String
					.valueOf(map.get("原因归类")) : "");
			cell = row.createCell(14);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("初步优化方案类别") != null
					&& Common.judgeString(String.valueOf(map.get("初步优化方案类别"))) ? String
					.valueOf(map.get("初步优化方案类别")) : "");
			cell = row.createCell(15);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("详细原因分析") != null
					&& Common.judgeString(String.valueOf(map.get("详细原因分析"))) ? String
					.valueOf(map.get("详细原因分析")) : "");
			cell = row.createCell(16);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("详细方案") != null
					&& Common.judgeString(String.valueOf(map.get("详细方案"))) ? String
					.valueOf(map.get("详细方案")) : "");
			cell = row.createCell(17);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("详细方案归类") != null
					&& Common.judgeString(String.valueOf(map.get("详细方案归类"))) ? String
					.valueOf(map.get("详细方案归类")) : "");
			cell = row.createCell(18);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("详细优化方案类别") != null
					&& Common.judgeString(String.valueOf(map.get("详细优化方案类别"))) ? String
					.valueOf(map.get("详细优化方案类别")) : "");
			cell = row.createCell(19);
			//cell.setCellStyle(style);
			cell.setCellValue(trim_village); //调整小区/站点名称
			cell = row.createCell(20);
			//cell.setCellStyle(style);
			cell.setCellValue(property); //属性
			cell = row.createCell(21);
			//cell.setCellStyle(style);
			cell.setCellValue(target); //目标值
			cell = row.createCell(22);
			//cell.setCellStyle(style);
			cell.setCellValue(order_state); //维护子工单状态
			cell = row.createCell(23);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("评估问题是否已解决") != null
					&& Common.judgeString(String.valueOf(map.get("评估问题是否已解决"))) ? String
					.valueOf(map.get("评估问题是否已解决")) : "");
			cell = row.createCell(24);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("工单流水号") != null
					&& Common.judgeString(String.valueOf(map.get("工单流水号"))) ? String
					.valueOf(map.get("工单流水号")) : order_code); //工单流水号
			clusterCodes += "," + map.get("聚合问题点序号");
			questionCount += Common
					.judgeString(String.valueOf(map.get("问题点数量"))) ? Integer
					.parseInt(String.valueOf(map.get("问题点数量"))) : 0;
			if (dataMap != null) {
				date = Common.getDate(String.valueOf(dataMap.get("问题点首次发生时间")));
			}
			if (map != null) {
				dateCurrent = Common.getDate(String.valueOf(map
						.get("问题点首次发生时间")));
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
			criteria.put(
					"question_type",
					Common.judgeString(String.valueOf(dataMap.get("问题点类型"))) ? String
							.valueOf(dataMap.get("问题点类型")) : "");
			criteria.put("question_date",
					String.valueOf(dataMap.get("问题点首次发生时间")));
			criteria.put("cluster_longitude",
					String.valueOf(dataMap.get("聚合问题点经度")));
			criteria.put("cluster_latitude",
					String.valueOf(dataMap.get("聚合问题点纬度")));
			criteria.put("involve_site", String.valueOf(dataMap.get("涉及问题小区")));

			criteria.put("cities", cities);
			criteria.put("city", String.valueOf(dataMap.get("地市")));
			criteria.put("cluster_code", String.valueOf(dataMap.get("聚合问题点序号")));
			criteria.put("question_section",
					String.valueOf(dataMap.get("问题点路段")));
			criteria.put("clusterCount", clusterCodes.split(",").length);
			criteria.put("questionCount", questionCount);

		}
		wb.write(os);
	}

	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void exportDispatch2(OutputStream os, QueryCriteria criteria)

			throws Exception {

		// 第一步，创建一个webbook，对应一个Excel文件
		Workbook wb = new XSSFWorkbook();
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
		//cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("问题点数量");
		sheet.setColumnWidth(1, ("问题点数量".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(2);
		cell.setCellValue("地市");
		sheet.setColumnWidth(2, ("地市".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(3);
		cell.setCellValue("聚合问题点经度");
		sheet.setColumnWidth(3, ("聚合问题点经度".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(4);
		cell.setCellValue("聚合问题点纬度");
		sheet.setColumnWidth(4, ("聚合问题点纬度".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(5);
		cell.setCellValue("涉及问题小区");
		sheet.setColumnWidth(5, ("涉及问题小区".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(6);
		cell.setCellValue("日期");
		sheet.setColumnWidth(6, ("日期".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(7);
		cell.setCellValue("数据来源");
		sheet.setColumnWidth(7, ("数据来源".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(8);
		cell.setCellValue("归属网格");
		sheet.setColumnWidth(8, ("归属网格".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(9);
		cell.setCellValue("涉及LOG/文件名称");
		sheet.setColumnWidth(9, ("涉及LOG/文件名称".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(10);
		cell.setCellValue("问题点类型");
		sheet.setColumnWidth(10, ("问题点类型".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(11);
		cell.setCellValue("初步原因分析");
		sheet.setColumnWidth(11, ("初步原因分析".length() + 10) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(12);
		cell.setCellValue("初步方案");
		sheet.setColumnWidth(12, ("初步方案".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(13);
		cell.setCellValue("原因归类");
		sheet.setColumnWidth(13, ("原因归类".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(14);
		cell.setCellValue("初步优化方案类别");
		sheet.setColumnWidth(14, ("初步优化方案类别".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(15);
		cell.setCellValue("详细原因分析");
		sheet.setColumnWidth(15, ("详细原因分析".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(16);
		cell.setCellValue("详细方案");
		sheet.setColumnWidth(16, ("详细方案".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(17);
		cell.setCellValue("详细原因归类");
		sheet.setColumnWidth(17, ("详细原因归类".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(18);
		cell.setCellValue("详细优化方案类别");
		sheet.setColumnWidth(18, ("详细优化方案类别".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(19);
		cell.setCellValue("调整小区/站点名称");
		sheet.setColumnWidth(19, ("调整小区/站点名称".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(20);
		cell.setCellValue("属性");
		sheet.setColumnWidth(20, ("属性".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(21);
		cell.setCellValue("目标值");
		sheet.setColumnWidth(21, ("目标值".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(22);
		cell.setCellValue("维护子工单状态");
		sheet.setColumnWidth(22, ("维护子工单状态".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(23);
		cell.setCellValue("评估问题是否已解决");
		sheet.setColumnWidth(23, ("评估问题是否已解决".length() + 4) * 512);
		//cell.setCellStyle(style);
		cell = row.createCell(24);
		cell.setCellValue("工单流水号");
		sheet.setColumnWidth(24, ("工单流水号".length() + 4) * 512);
		//cell.setCellStyle(style);
		
		
		List<Map> mapList = mapList = exportClusterList2(criteria);

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
		for (int i = 0; i < mapList.size(); i++) {
			System.out.println(System.currentTimeMillis()+"  "+i);
			row = sheet.createRow((int) i + 1);
			// 第四步，创建单元格，并设置值
			Map map = mapList.get(i);

			//String reason=map.get("初步原因分析").toString();

			// String type=map.get("问题点类型").toString();

			cell = row.createCell(0);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("聚合问题点序号") != null
					&& Common.judgeString(String.valueOf(map.get("聚合问题点序号"))) ? String
					.valueOf(map.get("聚合问题点序号")) : "");
			cell = row.createCell(1);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("问题点数量") != null
					&& Common.judgeString(String.valueOf(map.get("问题点数量"))) ? String
					.valueOf(map.get("问题点数量")) : "");
			cell = row.createCell(2);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("地市") != null
					&& Common.judgeString(String.valueOf(map.get("地市"))) ? String
					.valueOf(map.get("地市")) : "");
			cell = row.createCell(3);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("聚合问题点经度") != null
					&& Common.judgeString(String.valueOf(map.get("聚合问题点经度"))) ? String
					.valueOf(map.get("聚合问题点经度")) : "");
			cell = row.createCell(4);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("聚合问题点纬度") != null
					&& Common.judgeString(String.valueOf(map.get("聚合问题点纬度"))) ? String
					.valueOf(map.get("聚合问题点纬度")) : "");
			cell = row.createCell(5);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("涉及问题小区") != null
					&& Common.judgeString(String.valueOf(map.get("涉及问题小区"))) ? String
					.valueOf(map.get("涉及问题小区")) : "");
			cell = row.createCell(6);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("日期") != null
					&& Common.judgeString(String.valueOf(map.get("日期"))) ? String
					.valueOf(map.get("日期")) : "");
			cell = row.createCell(7);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("数据来源") != null
					&& Common.judgeString(String.valueOf(map.get("数据来源"))) ? String
					.valueOf(map.get("数据来源")) : "");
			cell = row.createCell(8);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("归属网格") != null
					&& Common.judgeString(String.valueOf(map.get("归属网格"))) ? String
					.valueOf(map.get("归属网格")) : "");
			cell = row.createCell(9);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("涉及LOG/文件名称") != null
					&& Common.judgeString(String.valueOf(map.get("涉及LOG/文件名称"))) ? String
					.valueOf(map.get("涉及LOG/文件名称")) : "");
			cell = row.createCell(10);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("问题点类型") != null
					&& Common.judgeString(String.valueOf(map.get("问题点类型"))) ? String
					.valueOf(map.get("问题点类型")) : "");
			cell = row.createCell(11);
			//cell.setCellStyle(style);
			if (map.get("初步原因分析") != null
					&& map.get("初步原因分析").toString().length() > 32766) {
				System.out.println(map.get("聚合问题点序号") + "长度为:"
						+ map.get("初步原因分析").toString().length());
				//超过excel单元格最大长度，只截取前面30000长度部分
				String maxstring = map.get("初步原因分析").toString()
						.substring(0, 30000);
				cell.setCellValue(maxstring);

			} else {
				cell.setCellValue(map.get("初步原因分析") != null
						&& Common.judgeString(String.valueOf(map.get("初步原因分析"))) ? String
						.valueOf(map.get("初步原因分析")) : "");
			}
			cell = row.createCell(12);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("初步方案") != null
					&& Common.judgeString(String.valueOf(map.get("初步方案"))) ? String
					.valueOf(map.get("初步方案")) : "");
			cell = row.createCell(13);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("原因归类") != null
					&& Common.judgeString(String.valueOf(map.get("原因归类"))) ? String
					.valueOf(map.get("原因归类")) : "");
			cell = row.createCell(14);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("初步优化方案类别") != null
					&& Common.judgeString(String.valueOf(map.get("初步优化方案类别"))) ? String
					.valueOf(map.get("初步优化方案类别")) : "");
			cell = row.createCell(15);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("详细原因分析") != null
					&& Common.judgeString(String.valueOf(map.get("详细原因分析"))) ? String
					.valueOf(map.get("详细原因分析")) : "");
			cell = row.createCell(16);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("详细方案") != null
					&& Common.judgeString(String.valueOf(map.get("详细方案"))) ? String
					.valueOf(map.get("详细方案")) : "");
			cell = row.createCell(17);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("详细方案归类") != null
					&& Common.judgeString(String.valueOf(map.get("详细方案归类"))) ? String
					.valueOf(map.get("详细方案归类")) : "");
			cell = row.createCell(18);
			//cell.setCellStyle(style);
			cell.setCellValue(map.get("详细优化方案类别") != null
					&& Common.judgeString(String.valueOf(map.get("详细优化方案类别"))) ? String
					.valueOf(map.get("详细优化方案类别")) : "");
			cell = row.createCell(19);
			//cell.setCellStyle(style);
			cell.setCellValue(trim_village); //调整小区/站点名称
			cell = row.createCell(20);
			//cell.setCellStyle(style);
			cell.setCellValue(property); //属性
			cell = row.createCell(21);
			//cell.setCellStyle(style);
			cell.setCellValue(target); //目标值
			cell = row.createCell(22);
			//cell.setCellStyle(style);
			cell.setCellValue(order_state); //维护子工单状态
			cell = row.createCell(23);
			//cell.setCellStyle(style);
			/*cell.setCellValue(map.get("评估问题是否已解决") != null
					&& Common.judgeString(String.valueOf(map.get("评估问题是否已解决"))) ? String
					.valueOf(map.get("评估问题是否已解决")) : "");*/
			cell = row.createCell(24);
			//cell.setCellStyle(style);
			/*cell.setCellValue(map.get("工单流水号") != null
					&& Common.judgeString(String.valueOf(map.get("工单流水号"))) ? String
					.valueOf(map.get("工单流水号")) : order_code); //工单流水号
			clusterCodes += "," + map.get("聚合问题点序号");
			questionCount += Common
					.judgeString(String.valueOf(map.get("问题点数量"))) ? Integer
					.parseInt(String.valueOf(map.get("问题点数量"))) : 0;*/
			/*if (dataMap != null) {
				date = Common.getDate(String.valueOf(dataMap.get("问题点首次发生时间")));
			}
			if (map != null) {
				dateCurrent = Common.getDate(String.valueOf(map
						.get("问题点首次发生时间")));
			}
			if (date == null) {
				dataMap = map;
			}
			if (date != null && dateCurrent != null) {
				if (date.after(dateCurrent)) {
					dataMap = map;
				}
			}*/
			if (!cities.contains(String.valueOf(map.get("地市")))) {
				cities += "," + String.valueOf(map.get("地市"));
			}
		}
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
			criteria.put(
					"question_type",
					Common.judgeString(String.valueOf(dataMap.get("问题点类型"))) ? String
							.valueOf(dataMap.get("问题点类型")) : "");
			criteria.put("question_date",
					String.valueOf(dataMap.get("问题点首次发生时间")));
			criteria.put("cluster_longitude",
					String.valueOf(dataMap.get("聚合问题点经度")));
			criteria.put("cluster_latitude",
					String.valueOf(dataMap.get("聚合问题点纬度")));
			criteria.put("involve_site", String.valueOf(dataMap.get("涉及问题小区")));

			criteria.put("cities", cities);
			criteria.put("city", String.valueOf(dataMap.get("地市")));
			criteria.put("cluster_code", String.valueOf(dataMap.get("聚合问题点序号")));
			criteria.put("question_section",
					String.valueOf(dataMap.get("问题点路段")));
			criteria.put("clusterCount", clusterCodes.split(",").length);
			criteria.put("questionCount", questionCount);

		}
		wb.write(os);
	}

	/**
	 * @param criteria
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static List<Map> exportClusterList2(QueryCriteria criteria) {
		List<Map> mapList = new ArrayList<Map>();
		for (int i = 0; i < 90000; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			mapList.add(map);
		}
		return mapList;
	}
	
}
