package com.etone.universe.dmp.problem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.ws.addressing.WSAContextUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.etone.universe.dmp.util.Common;
import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年4月10日  下午3:58:23
 */
public class TestExcel {
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		Workbook wb = new XSSFWorkbook(new FileInputStream("F:\\test\\sendwork\\20170303\\佛山\\分析定位_20170302195250306.2G-4G无线网络指标-20170301-佛山.xlsx"));
		//SXSSFWorkbook wb = new SXSSFWorkbook(500);
		// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
		Map<String,String> idNames = ProblemUtil.getIdNames("/problem/sheetNames.xml", "sheeNames");
		for(int i=0;i<wb.getNumberOfSheets();i++){
			String sheetName = wb.getSheetAt(i).getSheetName();
			if(idNames.get(sheetName)!=null){
				System.out.println("sum(case when sheetname='"+sheetName+"' then 1 esle 0 end) ");
			}
		}
		if(1==1)return;
		org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("方案库派单附件表111");
		
		Row row1 = sheet.createRow(0);
		for(int i=0;i<24;i++){
			row1.createCell(i).setCellValue("TEST"+i);
		}
		
		int count = 0;
		for(int j=1;j<90000;j++){
			count = 0;
			Row createRow = sheet.createRow(j);
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			createRow.createCell(count++).setCellValue("TEST123");
			System.out.println(j);
			//每当行数达到设置的值就刷新数据到硬盘,以清理内存
            /*if(j%100==0){
               ((SXSSFSheet)sheet).flushRows();
            }*/
		}
		
		
	}
	
	public static void main1(String[] args) throws Exception {
		exportDispatch();
		//main1(null);
		//voidtestWorkBook();
	}
	
	
	public static void voidtestWorkBook() {

	       try{
	           long curr_time=System.currentTimeMillis();
	           int rowaccess=100;//内存中缓存记录行数
	           /*keep 100 rowsin memory,exceeding rows will be flushed to disk*/
	           SXSSFWorkbook wb = new SXSSFWorkbook(rowaccess); 
	           int sheet_num=3;//生成3个SHEET

	           for(int i=0;i<sheet_num;i++){
	              Sheet sh = wb.createSheet();
	              //每个SHEET有60000ROW
	              for(int rownum = 0; rownum < 90000; rownum++) {
	                  Row row = sh.createRow(rownum);
	                  //每行有10个CELL
	                  for(int cellnum = 0; cellnum < 10; cellnum++) {
	                     Cell cell = row.createCell(cellnum);
	                     String address = new CellReference(cell).formatAsString();
	                     cell.setCellValue(address);
	                     System.out.println(rownum);
	                  }
	                  //每当行数达到设置的值就刷新数据到硬盘,以清理内存
	                  if(rownum%rowaccess==0){
	                     ((SXSSFSheet)sh).flushRows();
	                  }
	              }
	           }

	           /*写数据到文件中*/
	           FileOutputStream os = new FileOutputStream("F:/test/biggrid.xlsx");    
	           wb.write(os);
	           os.close();
	           /*计算耗时*/
	           System.out.println("耗时:"+(System.currentTimeMillis()-curr_time)/1000);
	       } catch(Exception e) {
	           e.printStackTrace();
	       }
	    }
	
	private static void exportDispatch() throws Exception
	  {


	    long longcurr_time = System.currentTimeMillis();
	    int introwaccess = 500;
	    SXSSFWorkbook wb = new SXSSFWorkbook(introwaccess);

	    Sheet sheet = wb.createSheet("方案库派单附件表");

	    Row row = sheet.createRow(0);

	    CellStyle style = wb.createCellStyle();
	    style.setAlignment((short) 2);
	    style.setBorderLeft((short) 1);
	    style.setBorderRight((short) 1);
	    style.setBorderTop((short) 1);
	    style.setBorderBottom((short) 1);
	    style.setFillBackgroundColor((short) 12);

	    Cell cell = row.createCell(0);
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
	    cell.setCellValue("归属网格");
	    sheet.setColumnWidth(8, ("归属网格".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(9);
	    cell.setCellValue("涉及LOG/文件名称");
	    sheet.setColumnWidth(9, ("涉及LOG/文件名称".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(10);
	    cell.setCellValue("问题点类型");
	    sheet.setColumnWidth(10, ("问题点类型".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(11);
	    cell.setCellValue("初步原因分析");
	    sheet.setColumnWidth(11, ("初步原因分析".length() + 10) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(12);
	    cell.setCellValue("初步方案");
	    sheet.setColumnWidth(12, ("初步方案".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(13);
	    cell.setCellValue("原因归类");
	    sheet.setColumnWidth(13, ("原因归类".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(14);
	    cell.setCellValue("初步优化方案类别");
	    sheet.setColumnWidth(14, ("初步优化方案类别".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(15);
	    cell.setCellValue("详细原因分析");
	    sheet.setColumnWidth(15, ("详细原因分析".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(16);
	    cell.setCellValue("详细方案");
	    sheet.setColumnWidth(16, ("详细方案".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(17);
	    cell.setCellValue("详细原因归类");
	    sheet.setColumnWidth(17, ("详细原因归类".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(18);
	    cell.setCellValue("详细优化方案类别");
	    sheet.setColumnWidth(18, ("详细优化方案类别".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(19);
	    cell.setCellValue("调整小区/站点名称");
	    sheet.setColumnWidth(19, ("调整小区/站点名称".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(20);
	    cell.setCellValue("属性");
	    sheet.setColumnWidth(20, ("属性".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(21);
	    cell.setCellValue("目标值");
	    sheet.setColumnWidth(21, ("目标值".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(22);
	    cell.setCellValue("维护子工单状态");
	    sheet.setColumnWidth(22, ("维护子工单状态".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(23);
	    cell.setCellValue("评估问题是否已解决");
	    sheet.setColumnWidth(23, ("评估问题是否已解决".length() + 4) * 512);
	    cell.setCellStyle(style);
	    cell = row.createCell(24);
	    cell.setCellValue("工单流水号");
	    sheet.setColumnWidth(24, ("工单流水号".length() + 4) * 512);
	    cell.setCellStyle(style);
	    String type = String.valueOf("");
	    List mapList = new ArrayList();
	      mapList = exportClusterList2();

	    Map dataMap = null;
	    Date date = null;
	    Date dateCurrent = null;
	    String clusterCodes = "";
	    int questionCount = 0;
	    String cities = "";
	    String handleState = "";
	    String order_code = "";
	    String trim_village = "";
	    String property = "";
	    String target = "";
	    String order_state = "";
	    for (int i = 0; i < mapList.size(); i++) {
	      row = sheet.createRow(i + 1);

	      Map map = (Map)mapList.get(i);

	      target = (map.get("目标值") != null) && (Common.judgeString(String.valueOf(map.get("目标值")))) ? String.valueOf(map.get("目标值")) : "";
	      property = (map.get("属性") != null) && (Common.judgeString(String.valueOf(map.get("属性")))) ? String.valueOf(map.get("属性")) : "";
	      trim_village = (map.get("站点名称") != null) && (Common.judgeString(String.valueOf(map.get("站点名称")))) ? String.valueOf(map.get("站点名称")) : "";
	      order_state = (map.get("子工单") != null) && (Common.judgeString(String.valueOf(map.get("子工单")))) ? String.valueOf(map.get("子工单")) : "";
	      cell = row.createCell(0);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("聚合问题点序号") != null) && (Common.judgeString(String.valueOf(map.get("聚合问题点序号")))) ? String.valueOf(map.get("聚合问题点序号")) : "");
	      cell = row.createCell(1);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("问题点数量") != null) && (Common.judgeString(String.valueOf(map.get("问题点数量")))) ? String.valueOf(map.get("问题点数量")) : "");
	      cell = row.createCell(2);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("地市") != null) && (Common.judgeString(String.valueOf(map.get("地市")))) ? String.valueOf(map.get("地市")) : "");
	      cell = row.createCell(3);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("聚合问题点经度") != null) && (Common.judgeString(String.valueOf(map.get("聚合问题点经度")))) ? String.valueOf(map.get("聚合问题点经度")) : "");
	      cell = row.createCell(4);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("聚合问题点纬度") != null) && (Common.judgeString(String.valueOf(map.get("聚合问题点纬度")))) ? String.valueOf(map.get("聚合问题点纬度")) : "");
	      cell = row.createCell(5);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("涉及问题小区") != null) && (Common.judgeString(String.valueOf(map.get("涉及问题小区")))) ? String.valueOf(map.get("涉及问题小区")) : "");
	      cell = row.createCell(6);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("日期") != null) && (Common.judgeString(String.valueOf(map.get("日期")))) ? String.valueOf(map.get("日期")) : "");
	      cell = row.createCell(7);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("数据来源") != null) && (Common.judgeString(String.valueOf(map.get("数据来源")))) ? String.valueOf(map.get("数据来源")) : "");
	      cell = row.createCell(8);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("归属网格") != null) && (Common.judgeString(String.valueOf(map.get("归属网格")))) ? String.valueOf(map.get("归属网格")) : "");
	      cell = row.createCell(9);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("涉及LOG/文件名称") != null) && (Common.judgeString(String.valueOf(map.get("涉及LOG/文件名称")))) ? String.valueOf(map.get("涉及LOG/文件名称")) : "");
	      cell = row.createCell(10);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("问题点类型") != null) && (Common.judgeString(String.valueOf(map.get("问题点类型")))) ? String.valueOf(map.get("问题点类型")) : "");
	      cell = row.createCell(11);
	      cell.setCellStyle(style);
	      if ((map.get("初步原因分析") != null) && (map.get("初步原因分析").toString().length() > 32766)) {
	        System.out.println(map.get("聚合问题点序号") + "长度为:" + map.get("初步原因分析").toString().length());

	        String maxstring = map.get("初步原因分析").toString().substring(0, 30000);
	        cell.setCellValue(maxstring);
	      }
	      else {
	        cell.setCellValue((map.get("初步原因分析") != null) && (Common.judgeString(String.valueOf(map.get("初步原因分析")))) ? String.valueOf(map.get("初步原因分析")) : "");
	      }
	      cell = row.createCell(12);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("初步方案") != null) && (Common.judgeString(String.valueOf(map.get("初步方案")))) ? String.valueOf(map.get("初步方案")) : "");
	      cell = row.createCell(13);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("原因归类") != null) && (Common.judgeString(String.valueOf(map.get("原因归类")))) ? String.valueOf(map.get("原因归类")) : "");
	      cell = row.createCell(14);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("初步优化方案类别") != null) && (Common.judgeString(String.valueOf(map.get("初步优化方案类别")))) ? String.valueOf(map.get("初步优化方案类别")) : "");
	      cell = row.createCell(15);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("详细原因分析") != null) && (Common.judgeString(String.valueOf(map.get("详细原因分析")))) ? String.valueOf(map.get("详细原因分析")) : "");
	      cell = row.createCell(16);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("详细方案") != null) && (Common.judgeString(String.valueOf(map.get("详细方案")))) ? String.valueOf(map.get("详细方案")) : "");
	      cell = row.createCell(17);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("详细方案归类") != null) && (Common.judgeString(String.valueOf(map.get("详细方案归类")))) ? String.valueOf(map.get("详细方案归类")) : "");
	      cell = row.createCell(18);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("详细优化方案类别") != null) && (Common.judgeString(String.valueOf(map.get("详细优化方案类别")))) ? String.valueOf(map.get("详细优化方案类别")) : "");
	      cell = row.createCell(19);
	      cell.setCellStyle(style);
	      cell.setCellValue(trim_village);
	      cell = row.createCell(20);
	      cell.setCellStyle(style);
	      cell.setCellValue(property);
	      cell = row.createCell(21);
	      cell.setCellStyle(style);
	      cell.setCellValue(target);
	      cell = row.createCell(22);
	      cell.setCellStyle(style);
	      cell.setCellValue(order_state);
	      cell = row.createCell(23);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("评估问题是否已解决") != null) && (Common.judgeString(String.valueOf(map.get("评估问题是否已解决")))) ? String.valueOf(map.get("评估问题是否已解决")) : "");
	      cell = row.createCell(24);
	      cell.setCellStyle(style);
	      cell.setCellValue((map.get("工单流水号") != null) && (Common.judgeString(String.valueOf(map.get("工单流水号")))) ? String.valueOf(map.get("工单流水号")) : order_code);
	      clusterCodes = clusterCodes + "," + map.get("聚合问题点序号");
	      questionCount += (Common.judgeString(String.valueOf(map.get("问题点数量"))) ? Integer.parseInt(String.valueOf(map.get("问题点数量"))) : 0);
	      if (dataMap != null) {
	        date = Common.getDate(String.valueOf(dataMap.get("问题点首次发生时间")));
	      }
	      if (map != null) {
	        dateCurrent = Common.getDate(String.valueOf(map.get("问题点首次发生时间")));
	      }
	      if (date == null) {
	        dataMap = map;
	      }
	      if ((date != null) && (dateCurrent != null) && 
	        (date.after(dateCurrent))) {
	        dataMap = map;
	      }

	      if (!cities.contains(String.valueOf(map.get("地市")))) {
	        cities = cities + "," + String.valueOf(map.get("地市"));
	      }

	      if (i % introwaccess == 0) {
	    	  System.out.println(i);
	        ((SXSSFSheet)sheet).flushRows();
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
	  }
	
	/**
	 * @param criteria
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static List<Map> exportClusterList2() {
		List<Map> mapList = new ArrayList<Map>();
		for (int i = 0; i < 90000; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			mapList.add(map);
		}
		return mapList;
	}

}
