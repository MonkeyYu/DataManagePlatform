package com.etone.universe.dmp.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 高速问题点处理类
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年2月28日  下午3:22:40
 */
public class GaosuTest {

	public static void main(String[] args) throws IOException {
		excelPro("F:/test/2G-4G高速无线网络指标-派单模板2-15.xlsx");
	}

	public static void excelPro(String path) throws IOException {

		InputStream is = null;

		// 创建唯一的表名
		String tableName = ProblemUtil.EXT_EDMP_TABLENAME
				+ System.currentTimeMillis();
		String extFilePath = null;
		// 写文件流
		FileOutputStream fw = null;
		BufferedWriter bw = null;

		String txtName = ProblemUtil.EXT_EDMP_TABLENAME
				+ System.currentTimeMillis() + ProblemUtil.FILE_TXT;
		File txtFile = new File("/data1/load/" + txtName);
		try {

			// 文件路径
			File jobFile = new File(path);

			// 创建work对象
			is = new FileInputStream(jobFile);
			Workbook hssfWorkbook = new XSSFWorkbook(is);

			// 写入外部表数据文件
			fw = new FileOutputStream(txtFile);
			bw = new BufferedWriter(new OutputStreamWriter(fw, "UTF-8"));

			// 将汇聚表数据写入到外部表文件
			writeExtTxt(bw, hssfWorkbook, "LTE高速问题点汇聚", "LTE");
			writeExtTxt(bw, hssfWorkbook, "GSM高速问题点汇聚", "GSM");

			// 创建外部表
			createExtTable(txtFile.getName(), tableName);

			// 核查入库表数据

			// 匹配成功,将匹配上的入库数据写入到聚类结果跟踪表lte_cluster_question

		} catch (Exception e) {
			e.printStackTrace();
			// 出错删除外部表文件
			if (txtFile != null) {
				txtFile.delete();
			}
			throw new IOException("处理高速工单出错：", e);
		} finally {

			// 关闭文件流
			if (is != null) {
				is.close();
			}
			if (bw != null) {
				bw.close();
			}
			if (fw != null) {
				fw.close();
			}

			// 删除外部表及外部表文件
			if (tableName != null) {

			}
			if (extFilePath != null) {
				new File(extFilePath).delete();
			}
		}

	}

	/**
	 * 创建外部表
	 * @param name
	 * @param tableName
	 */
	private static void createExtTable(String name, String tableName) {
		
		// 组织创建sql语句
		/*String extPath = getExternalPath() + name;
		String createTbaleSql = sqlMap.get("createExtTable")
				.replace("${tableName}", tableName)
				.replace("${externalPathStr}", extPath);*/

	}

	/**
	 * 将汇聚表数据写入到外部表文件
	 * @param bw 外部表数据文件流
	 * @param hssfWorkbook work对象
	 * @param sheetName sheet名称
	 * @param sheetType sheet类型
	 * @throws IOException
	 */
	private static void writeExtTxt(BufferedWriter bw, Workbook hssfWorkbook,
			String sheetName, String sheetType) throws IOException {

		// 取出里面两个跟踪sheet的聚类编号,数据写入文件
		Sheet lteSheet = hssfWorkbook.getSheet(sheetName);
		// 行数小于2说明没有数据
		if (lteSheet.getPhysicalNumberOfRows() > 2) {

			Iterator<Row> rowIterator = lteSheet.rowIterator();
			while (rowIterator.hasNext()) {
				Row next = rowIterator.next();
				// 第一行是标题
				if (next.getRowNum() == 0) {
					continue;
				}
				// 若聚类编号为空,跳过
				Cell cell = next.getCell(0);
				if (cell == null) {
					continue;
				}
				String jlbh = cell.getStringCellValue();
				bw.write(jlbh + "|" + sheetType);
				bw.newLine();
			}
		}
	}

}
