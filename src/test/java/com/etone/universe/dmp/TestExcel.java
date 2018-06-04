package com.etone.universe.dmp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.apache.derby.tools.sysinfo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年3月25日  下午1:20:54
 */
public class TestExcel {
	
	public static void main(String[] args) throws Exception {
		
		
		InputStream is = new FileInputStream("F:/test/sendwork/guangzhou.xlsx");
		XSSFWorkbook hssfWorkbook = new XSSFWorkbook(is);
		
		Map<String,String> nameMap = ProblemUtil.getIdNames("problem/sheetNames.xml", "sheeNames");
		
		
		for (String nameKey : nameMap.keySet()) {
			String sheetName = nameMap.get(nameKey);
			System.out.println(sheetName);
			XSSFSheet sheet = hssfWorkbook.getSheet(nameKey);
			int wtdhh = 0;
			// 第一列包含聚类编号
			if (sheet.getRow(0).getCell(0).getStringCellValue().contains("聚类")) {
				wtdhh = 1;
			}

			// 判断第二行第二列是否为空
			if ("本周暂无".equals(sheet.getRow(1).getCell(0).getStringCellValue())
					&& sheet.getRow(1).getCell(1) == null) {
				//System.out.println(sheetName+":null");
				continue;
			}

			for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
				Row next = sheet.getRow(i);
				int rowNum = next.getRowNum();
				String valStr = next.getCell(wtdhh).getStringCellValue();
				//System.out.println(valStr + "|" + sheet.getSheetName() + "|" + rowNum);
			}
		}
	}

}
