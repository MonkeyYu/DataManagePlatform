package com.etone.universe.dmp.problem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.derby.tools.sysinfo;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;

import com.etone.universe.dmp.util.Common;
import com.etone.universe.dmp.util.FileCharsetUtil;
import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年4月13日  下午3:32:04
 */
public class Test {
	public static final String[] mt_voltewxjtcxqyy_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属ENODEB", "小区英文名", "小区中文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "设备厂商", "功率", "2016-07-20", "2016-07-21", "2016-07-22", "2016-07-23", "2016-07-24", "2016-07-25", "2016-07-26", "有指标天数", "一周出现大于2次低于目标值", "QCI1的E-RAB建立请求数", "QCI1的E-RAB建立成功数", "RRC连接建立成功次数", "RRC连接建立请求次数", "是否优先优化小区", "最近四周出现次数"};
	public static void main(String[] args) throws Exception {
		FileInputStream inputStream = new FileInputStream("G:\\方案库替换文件\\20170616174246374.2G-4G无线网络指标-20170614-深圳.xlsx");
		try {
			Workbook book = WorkbookFactory.create(inputStream);
			testx(book.getSheet("VoLTE无线接通差小区(语音)"),mt_voltewxjtcxqyy_week,book);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally {
			inputStream.close();
		}
		
	}
	
	
	public static void testx(Sheet sheet,String[] titleArr, Workbook book){

		//        添加一列
		//        每列的内容后移
		org.apache.poi.ss.usermodel.Cell cell = null;
		//先判断第二行是否为无数据,加快遍历速度
		if (sheet.getPhysicalNumberOfRows() <= 1
				|| "本周暂无".equals(String.valueOf(getCellValue(sheet, 1, 0)))
				|| "本地暂无".equals(String.valueOf(getCellValue(sheet, 1, 0)))
				|| "暂无".equals(String.valueOf(getCellValue(sheet, 1, 0)))) {
		} else {
			for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
				cell = insertCell(sheet, i, titleArr.length - 1);
				if (i == 0) {
					cell.setCellStyle(getCell(sheet, 0, 0).getCellStyle());
					sheet.setColumnWidth(titleArr.length - 1, 10 * 2 * 256);
				} else {
					//cell.setCellStyle(cellStyle);

				}
				for (int j = titleArr.length - 1; j >= 0; j--) {
					cell = getCell(sheet, i, j);
					if (j == 0) {
						if (i == 0) {
							cell.setCellValue("所属聚类编号");
						} else {
							//questionCodes.add(cell.getStringCellValue());
							cell.setCellValue("");
						}
					} else {
						Object obj = getCellValue(sheet, i, j - 1);
						if (cell != null
								&& getCell(sheet, i, j - 1) != null) {
							cell.setCellType(getCell(sheet, i, j - 1)
									.getCellType());
							cell.setCellValue(obj == null ? "" : String
									.valueOf(obj));
						}
					}
				}
			}
		}
	
	}
	
	private static org.apache.poi.ss.usermodel.Cell insertCell(
			org.apache.poi.ss.usermodel.Sheet sheet, int row, int column) {
		org.apache.poi.ss.usermodel.Cell cell = null;
		if (sheet != null) {
			Row rows = sheet.getRow(row);
			if (rows != null) {
				cell = rows.createCell(column);
			}
		}
		return cell;
	}
	
	private static org.apache.poi.ss.usermodel.Cell getCell(
			org.apache.poi.ss.usermodel.Sheet sheet, int row, int column) {
		org.apache.poi.ss.usermodel.Cell cell = null;
		if (sheet != null) {
			Row rows = sheet.getRow(row);
			if (rows != null) {
				cell = rows.getCell(column);
			}
		}
		return cell;
	}
	
	public static Object getCellValue(org.apache.poi.ss.usermodel.Sheet sheet,
			int row, int column) {
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
								sheetTitle = Common.convertInteger(String
										.valueOf(cell.getNumericCellValue()));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return sheetTitle;
	}
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	
	
	public static void maixxn(String[] args) {
		System.out.println(System.currentTimeMillis());
		
	}
	
	
	
	public static void mainxx(String[] args) throws Exception {
		
		List<Map<String,Object>> fields = new ArrayList<Map<String,Object>>();
		int fieldSize =fields.size();
		String data = "yy_ltesource_antennalte_20170509.csv|-1542175206|460-00-174530-12|2017-05-09|20170509|2017|05|09|广东省|86020|||1___2___1___1|摩比|0||Enabled|460-00-174530-12|6|270|35|1|1|1|MOBIT045215001|摩比内置合路器-8通天线";
		
		FileReader in=new FileReader("F:/test/111.csv");
	    LineNumberReader line=new LineNumberReader(in);
	    
	    OutputStreamWriter pw = new OutputStreamWriter(new FileOutputStream("F:/test.txt"),"GBK");//确认流的输出文件和编码格式，此过程创建了“test.txt”实例
	   
	    for(int i=0;i<5;i++){
	    	line.setLineNumber(i);
	    	String readLine = line.readLine();
	    	if(readLine!= null && !"".equals(readLine)){
	    		// 把最后的逗号去掉
	    		String delStr = readLine.substring(0, readLine.length()-1);
				String[] datas = delStr.split("|");
	    		
	    		// 判断字符切割后字段数是否足够
	    		if(datas.length>=fieldSize){
	    			pw.write(delStr);//将要写入文件的内容，可以多次write
	    			
	    			
	    			
	    			int indexOf = delStr.indexOf("|", fieldSize);
	    			System.out.println(delStr);
	    		}
	    		
	    	}
	    	//System.out.println(readLine);
	    }
	    
	    line.close();
	    in.close();
	    pw.close();//关闭流
	    
	}
	

	
	// 文件转码
	public static void mainzm(String[] args) {
		FileCharsetUtil.transfer2("F:/20170511172930.csv", "F:/20170511172930_1.csv");
	}
	
	public static String CheckInfo(int number,String info) {
        if(number==1){
            if(!info.equals("")){
               if(!info.equals("故障")&&!info.equals("覆盖")&&!info.equals("结构")&&!info.equals("干扰")&&!info.equals("容量")&&!info.equals("参数")&&!info.equals("其他")&&!info.equals("其它")&&!info.equals("其他（核心侧、传输等）")&&!info.equals("其它（核心侧、传输等）")){
                   info="";
               }
            }
            }else if(number==2){
            if(!info.equals("")){
                if(!info.equals("故障")&&!info.equals("覆盖")&&!info.equals("结构")&&!info.equals("干扰")&&!info.equals("容量")&&!info.equals("参数")&&!info.equals("其他")&&!info.equals("其它")&&!info.equals("其他（核心侧、传输等）")&&!info.equals("其它（核心侧、传输等）")){
                    info="";
                }
            }
            }else if(number==3){
            if(!info.equals("")){
                if(!info.equals("新增规划")&&!info.equals("工程建设")&&!info.equals("工程整改")&&!info.equals("天面整改")&&!info.equals("天线调整")&&!info.equals("参数优化")&&!info.equals("维护")&&!info.equals("非无线网络原因")){
                    info="";
                }
            }
            }else {
            if(!info.equals("")){
                if(!info.equals("新增规划")&&!info.equals("工程建设")&&!info.equals("工程整改")&&!info.equals("天面整改")&&!info.equals("天线调整")&&!info.equals("参数优化")&&!info.equals("维护")&&!info.equals("其它")&&!info.equals("市公司现场排查")){
                    info="";
                }
            }
        }
        return info;
    }
	
	public static void mainddd(String[] args) {
		Map<String,Integer> idNames = ProblemUtil.getIdNames("/problem/cluster.xml", "jhlbqz");
		
		for(int i=1;i<=idNames.size();i++){
			for(String key : idNames.keySet()){
				if(idNames.get(key)==i){
					if(i>10){
					System.out.println("<name key=\""+key+"\">"+(i+2)+"</name>");
					}else{
						System.out.println("<name key=\""+key+"\">"+(i)+"</name>");
					}
				}
			}
		}
	}

	
	public static void mainx(String[] args) throws IOException, SQLException {
		FileInputStream is = new FileInputStream("F:/test/jlgz.xls");
		Workbook wb = new HSSFWorkbook(is);
		
		Sheet sheet = wb.getSheet("Sheet3");
		Map<String, String> map = new HashMap<String, String>();
		
		
		for(int i =1;i<sheet.getPhysicalNumberOfRows();i++){
			Row row = sheet.getRow(i);
			String tablename = row.getCell(0).getStringCellValue();
			String field = row.getCell(1).getStringCellValue();
			map.put(tablename, field);
			field = field.replace("\"", "").replace("“", "").replace("”", "").replace("；", "||'；").replace("：", "：'||");
			
			//System.out.println("<name key=\""+tablename+"\">'"+field+"</name>");
		}
		processorProblemTarget();
		
	}
	
	private static void processorProblemTarget() throws SQLException {
		Map<String, Integer> map1 = ProblemUtil.getIdNames(
				"/problem/cluster.xml", "jhlbqz");
		
		Map<String, Integer> map2 = ProblemUtil.getIdNames(
				"/problem/cluster.xml", "intjlType");
		
		String sql = "case ";
		for(String key : map2.keySet()){
			Integer integer = map2.get(key);
			sql+= " when intplbtype="+integer+" then "+map1.get(key);
		}
		System.out.println(sql+" else 0 end ");
	}
}
