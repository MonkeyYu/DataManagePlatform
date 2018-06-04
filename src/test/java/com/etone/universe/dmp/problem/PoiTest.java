package com.etone.universe.dmp.problem;


import com.etone.daemon.db.DB;
import com.etone.daemon.support.Env;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.sql.Connection;

/**
 * User: yuxj
 * Date: 18-2-2
 * Time: 上午10:36
 */
public class PoiTest{
    public static void main(String[] args) throws Exception {
        Workbook wb= WorkbookFactory.create(new File("E:\\etone\\data_20180103\\查询站点.xlsx"));
        Integer sheetCount=wb.getNumberOfSheets();
        StringBuffer buffer=new StringBuffer();
        Connection conn;
        try {
            conn= DB.getConnection(Env.getProperties().getValue("scheme.ws.datasource.gp"));
            for(int k=0;k<sheetCount;k++){
                Sheet sheet=wb.getSheetAt(k);
                String sheetName=sheet.getSheetName();
                System.out.println("The SheetName is : "+sheetName);
                Integer rowNum=sheet.getLastRowNum();
                for(int i=0;i<rowNum;i++){
                    Row row=sheet.getRow(i);
                    short cellNum=row.getLastCellNum();
                    for(int j=0;j<cellNum;j++){
                        Cell cell=row.getCell(j);
//                    System.out.println(cell.toString());
                        buffer.append(",").append(cell.toString());
                    }
                    buffer.deleteCharAt(0);
//                buffer.append(System.getProperty("line.separator", "\n"));
                    System.out.println(buffer.toString());
                    buffer.setLength(0);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
