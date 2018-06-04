package com.etone.universe.dmp.problem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;


/**
 * Mr预统计模块
 *
 * @author <a href="mailto:88453013@qq.com">Guojian</a>
 * @version $$Revision: 14169 $$
 * @date 2013-10-10 上午9:57:59
 */

public class LteDispatchManager {
	
	public static void main(String[] args) throws Exception {
		org.apache.poi.ss.usermodel.Workbook book = WorkbookFactory.create(new FileInputStream("F:\\123123.xlsx"));
        org.apache.poi.ss.usermodel.Sheet sheet = null;
        QueryCriteria criteria = new QueryCriteria();
        sheet = book.getSheetAt(0);

        int rows = sheet.getPhysicalNumberOfRows();

        String msg=new LteDispatchManager().getExcelMsg("全网指标", rows, sheet, criteria);
        System.out.println(msg);
	}
	

    public String getExcelMsg(String ordertype,int rows,org.apache.poi.ss.usermodel.Sheet sheet,QueryCriteria criteria){
        org.apache.poi.ss.usermodel.Workbook book = null;
        org.apache.poi.ss.usermodel.Sheet sheet2=null;
        List<String> clusterCodeList = new ArrayList<String>();
        String WYId="";
        String cluster_code="";
        String first_analysis="";
        String first_proposal="";
        String reason_classify="";
        String first_proposal_type="";
        String detail_analysis="";
        String detail_proposal="";
        String detail_reason="";
        String detail_proposal_type="";
        String handle_state="";
        String is_solved="";
        String state="";
        String trim_village="";
        String property="";
        String target="";
        String order_state="";
        String order_code="";
        List<String> order_codes = new ArrayList<String>();
        OutputStream outputStream = null;
        InputStream inputStream = null;
        File file=null;
        //String newFile="/data1/load/" + msgid+".xls";
        String msgid="";
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            String lineSeparator=System.getProperty("line.separator","\n");//从当前系统中获取换行符，默认是“\n”
            byte[] buff=null;
            StringBuffer strBuffer=new StringBuffer();
            System.out.println("共计有"+rows+"行");
            if(rows>5000){
                System.out.println("更新异常,停止更新");
                state="更新失败";
                return state;
            }
            if(criteria.get("order_type")!=null&&criteria.get("order_type").equals("高速")){
                String numbers=null;
                int number= sheet.getRow(0).getPhysicalNumberOfCells();//得到首行所有列
                for(int i=0;i<number;i++){
                    String name=sheet.getRow(0).getCell(i).getStringCellValue();
                    if(name.equals("聚类编号")){
                       numbers=i+",";
                    }
                    if(name.equals("初步原因分析")){
                        numbers+=i+",";
                    }
                    if(name.equals("初步方案")){
                        numbers+=i+",";
                    }
                    if(name.equals("原因归类")){
                        numbers+=i+",";
                    }
                    if(name.equals("初步优化方案类别")){
                        numbers+=i+",";
                    }
                    if(name.equals("详细原因分析")){
                        numbers+=i+",";
                    }
                    if(name.equals("详细方案")){
                        numbers+=i+",";
                    }
                    if(name.equals("详细原因归类")){
                        numbers+=i+",";
                    }
                    if(name.equals("详细优化方案类别")){
                        numbers+=i+",";
                    }
                    if(name.equals("调整小区/站点名称")){
                        numbers+=i+",";
                    }
                    if(name.equals("属性")){
                        numbers+=i+",";
                    }
                    if(name.equals("目标值")){
                        numbers+=i+",";
                    }
                    if(name.equals("维护子工单状态")){
                        numbers+=i+",";
                    }
                    if(name.equals("评估问题是否已解决")){
                        numbers+=i+"";
                    }
//                    if(name.equals("工单流水号")){
//                        numbers+=i+"";
//                    }
                }
//                String[] numbervalue=numbers.split(",");
                //得到第一列的工单号
//                WYId=sheet.getRow(1).getCell(Integer.valueOf(numbervalue[14])).getStringCellValue();
                WYId=criteria.get("WYId").toString();
                msgid=WYId+"_"+System.currentTimeMillis();
                if("详细方案".equals(ordertype)) {
                    for (int row = 1; row < rows; row++) {
                        criteria = getExcelDetailData2(row + 1, sheet,numbers);
                        cluster_code=  Common.replaceBlank(String.valueOf(criteria.get("cluster_code")));
                        first_analysis= Common.replaceBlank(String.valueOf(criteria.get("first_analysis")));
                        first_proposal= Common.replaceBlank(String.valueOf(criteria.get("first_proposal")));
                        reason_classify= Common.CheckInfo(1,Common.replaceBlank(String.valueOf(criteria.get("reason_classify"))));
                        first_proposal_type= Common.CheckInfo(4,Common.replaceBlank(String.valueOf(criteria.get("first_proposal_type"))));
                        detail_analysis= Common.replaceBlank(String.valueOf(criteria.get("detail_analysis")));
                        detail_proposal= Common.replaceBlank(String.valueOf(criteria.get("detail_proposal")));
                        detail_reason= Common.CheckInfo(2,Common.replaceBlank(String.valueOf(criteria.get("detail_reason"))));
                        detail_proposal_type= Common.CheckInfo(3,Common.replaceBlank(String.valueOf(criteria.get("detail_proposal_type"))));
                        is_solved=Common.replaceBlank(String.valueOf(criteria.get("is_solved")));
                        trim_village=Common.replaceBlank(String.valueOf(criteria.get("trim_village")));
                        property=Common.replaceBlank(String.valueOf(criteria.get("property")));
                        target=Common.replaceBlank(String.valueOf(criteria.get("target")));
                        order_state=Common.replaceBlank(String.valueOf(criteria.get("order_state")));
                        order_code=WYId;
                        //System.out.println(is_solved+"行"+row);
                        handle_state="待评估";
                        order_codes.add(order_code);
                        //插入数据
                        strBuffer.append(cluster_code+"|"+first_analysis+"|"+first_proposal+"|"+reason_classify+"|"+first_proposal_type+"|"+detail_analysis+"|"+detail_proposal+"|"+detail_reason+"|"+detail_proposal_type+"|"+is_solved+"|"+handle_state+"|"+trim_village+"|"+property+"|"+target+"|"+order_state+"|"+order_code+"|"+msgid);
                        strBuffer.append(lineSeparator);

                    }
                    state="详细方案";

                }else {
                    for (int row = 1; row < rows; row++) {
//                         System.out.println("共计有"+rows+"行,正在执行"+(row+1)+"行");
                        criteria = getExcelDetailData2(row + 1, sheet,numbers);
                        cluster_code= Common.replaceBlank(String.valueOf(criteria.get("cluster_code")));
                        first_analysis= Common.replaceBlank(String.valueOf(criteria.get("first_analysis")));
                        first_proposal= Common.replaceBlank(String.valueOf(criteria.get("first_proposal")));
                        reason_classify= Common.CheckInfo(1,Common.replaceBlank(String.valueOf(criteria.get("reason_classify"))));
                        first_proposal_type= Common.CheckInfo(4,Common.replaceBlank(String.valueOf(criteria.get("first_proposal_type"))));
                        order_code=WYId;
                        detail_analysis= "";
                        detail_proposal= "";
                        detail_reason= "";
                        detail_proposal_type= "";
                        is_solved="";
                        handle_state="详细方案制定";
                        trim_village="";
                        property="";
                        target="";
                        order_state="";
                        order_codes.add(order_code);
                        //插入数据
                        strBuffer.append(cluster_code+"|"+first_analysis+"|"+first_proposal+"|"+reason_classify+"|"+first_proposal_type+"|"+detail_analysis+"|"+detail_proposal+"|"+detail_reason+"|"+detail_proposal_type+"|"+is_solved+"|"+handle_state+"|"+trim_village+"|"+property+"|"+target+"|"+order_state+"|"+order_code+"|"+msgid);
                        strBuffer.append(lineSeparator);

                    }
                    state="初步方案";
                }
            }else {
                //得到第一列工单号作为更新标识
                WYId=sheet.getRow(1).getCell(24).getStringCellValue();
                msgid=WYId+"_"+System.currentTimeMillis();
                if("详细方案".equals(ordertype)) {
                    for (int row = 1; row < rows; row++) {
//                        System.out.println("共计有"+rows+"行,正在执行"+(row+1)+"行");
                        criteria = getExcelDetailData(row + 1, sheet);
                        cluster_code=  Common.replaceBlank(String.valueOf(criteria.get("cluster_code")));
                        first_analysis= Common.replaceBlank(String.valueOf(criteria.get("first_analysis")));
                        first_proposal= Common.replaceBlank(String.valueOf(criteria.get("first_proposal")));
                        reason_classify= Common.CheckInfo(1,Common.replaceBlank(String.valueOf(criteria.get("reason_classify"))));
                        first_proposal_type= Common.CheckInfo(4,Common.replaceBlank(String.valueOf(criteria.get("first_proposal_type"))));
                        detail_analysis= Common.replaceBlank(String.valueOf(criteria.get("detail_analysis")));
                        detail_proposal= Common.replaceBlank(String.valueOf(criteria.get("detail_proposal")));
                        detail_reason= Common.CheckInfo(2,Common.replaceBlank(String.valueOf(criteria.get("detail_reason"))));
                        detail_proposal_type= Common.CheckInfo(3,Common.replaceBlank(String.valueOf(criteria.get("detail_proposal_type"))));
                        is_solved=Common.replaceBlank(String.valueOf(criteria.get("is_solved")));
                        trim_village=Common.replaceBlank(String.valueOf(criteria.get("trim_village")));
                        property=Common.replaceBlank(String.valueOf(criteria.get("property")));
                        target=Common.replaceBlank(String.valueOf(criteria.get("target")));
                        order_state=Common.replaceBlank(String.valueOf(criteria.get("order_state")));
                        order_code=Common.replaceBlank(String.valueOf(criteria.get("order_code")));
                        handle_state="待评估";
                        order_codes.add(order_code);
                        //插入数据
                        strBuffer.append(cluster_code+"|"+first_analysis+"|"+first_proposal+"|"+reason_classify+"|"+first_proposal_type+"|"+detail_analysis+"|"+detail_proposal+"|"+detail_reason+"|"+detail_proposal_type+"|"+is_solved+"|"+handle_state+"|"+trim_village+"|"+property+"|"+target+"|"+order_state+"|"+order_code+"|"+msgid);
                        strBuffer.append(lineSeparator);

                    }
                    state="详细方案";

                }else {
                    for (int row = 1; row < rows; row++) {
                        criteria = getExcelDetailData(row + 1, sheet);
                        cluster_code= Common.replaceBlank(String.valueOf(criteria.get("cluster_code")));
                        first_analysis= Common.replaceBlank(String.valueOf(criteria.get("first_analysis")));
                        first_proposal= Common.replaceBlank(String.valueOf(criteria.get("first_proposal")));
                        reason_classify= Common.CheckInfo(1,Common.replaceBlank(String.valueOf(criteria.get("reason_classify"))));
                        first_proposal_type= Common.CheckInfo(4,Common.replaceBlank(String.valueOf(criteria.get("first_proposal_type"))));
                        order_code=Common.replaceBlank(String.valueOf(criteria.get("order_code")));
                        detail_analysis= "";
                        detail_proposal= "";
                        detail_reason= "";
                        detail_proposal_type= "";
                        is_solved="";
                        handle_state="详细方案制定";
                        trim_village="";
                        property="";
                        target="";
                        order_state="";
                        order_codes.add(order_code);
                        //插入数据
                        strBuffer.append(cluster_code+"|"+first_analysis+"|"+first_proposal+"|"+reason_classify+"|"+first_proposal_type+"|"+detail_analysis+"|"+detail_proposal+"|"+detail_reason+"|"+detail_proposal_type+"|"+is_solved+"|"+handle_state+"|"+trim_village+"|"+property+"|"+target+"|"+order_state+"|"+order_code+"|"+msgid);
                        strBuffer.append(lineSeparator);

                    }
                    state="初步方案";
                }
            }
            String filename=msgid+".csv";
            //生产环境地址
            String address="F:/test/";
            String newFile=address+"/" +filename;
            //本地环境地址
//       String address="D:\\Workspaces\\LteMr\\projects\\trunk\\ltemr-web-hgp\\target\\ltemr\\file\\upload";
//        String newFile=address+"\\" +filename;
            /// Common.mkdir(newFile);
//        criteria.put("WYId",WYId);
        outputStream = new FileOutputStream(newFile,true);
        buff=strBuffer.toString().getBytes("UTF-8");
        outputStream.write(buff);
        file = new File(newFile);
         //外部表导入数据, 更新外部表数据到物理表
        criteria.put("tableName","lte_cluster_question_new");
        criteria.put("msgid",msgid);
        criteria.put("order_code",WYId);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 路径为文件则进行删除
            if (file.exists()) {
                file.delete();
            }
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
       HashSet<String> oc = new HashSet<String>(order_codes);
       //更新非法枚举值
       String reason_classify_value = "'故障', '覆盖', '结构', '干扰', '容量', '参数', '其他','其它','其他（核心侧、传输等）','其它（核心侧、传输等）'";
       String detail_reason_value = "'故障', '覆盖', '结构', '干扰', '容量', '参数', '其他','其它','其他（核心侧、传输等）','其它（核心侧、传输等）'";
       String detail_proposal_type_value = "'新增规划', '工程建设', '工程整改', '天面整改', '天线调整', '参数优化', '维护', '非无线网络原因'";
       String first_proposal_type_value = "'新增规划', '工程建设', '工程整改', '天面整改', '天线调整', '参数优化', '维护','市公司现场排查'";
       criteria.put("reason_classify",reason_classify_value);
       criteria.put("detail_reason",detail_reason_value);
       criteria.put("detail_proposal_type",detail_proposal_type_value);
       criteria.put("first_proposal_type",first_proposal_type_value);

       if(oc != null ){
           criteria.put("order_codeList", oc);
       }

//       updateClusterbycode(criteria);
       if(state.equals("详细方案")){
           criteria.put("handle_state","待评估");
       }else {
           criteria.put("handle_state","详细方案制定");
       }
        return "更新"+state+"成功";
    }
   
    
    public QueryCriteria getExcelDetailData2(int row, org.apache.poi.ss.usermodel.Sheet sheet,String numbers) {

        QueryCriteria criteria = new QueryCriteria();
        String cellContent = "";
        org.apache.poi.ss.usermodel.Cell cell =null;
        Row rows = sheet.getRow(row - 1);
        String[] numbervalue=numbers.split(",");
        cell = rows.getCell(Integer.valueOf(numbervalue[0]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("cluster_code", cellContent);// 聚合问题点序号

        cell = rows.getCell(Integer.valueOf(numbervalue[1]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }

        criteria.put("first_analysis", cellContent);// 初步原因分析

        cell = rows.getCell(Integer.valueOf(numbervalue[2]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
            if(cellContent.length()>30002){
                cellContent=cellContent.substring(0,30000);
            }
        }else {
            cellContent="";
        }
        criteria.put("first_proposal", cellContent);// 初步方案

        cell = rows.getCell(Integer.valueOf(numbervalue[3]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("reason_classify", cellContent);// 原因归类

        cell = rows.getCell(Integer.valueOf(numbervalue[4]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("first_proposal_type", cellContent);// 初步优化方案类别

        cell = rows.getCell(Integer.valueOf(numbervalue[5]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
            if(cellContent.length()>30000){
                cellContent=cellContent.substring(0,30000);
            }
        }else {
            cellContent="";
        }
        criteria.put("detail_analysis", cellContent);// 详细原因分析

        cell = rows.getCell(Integer.valueOf(numbervalue[6]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("detail_proposal", cellContent);// 详细方案


        cell = rows.getCell(Integer.valueOf(numbervalue[7]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("detail_reason", cellContent);// 详细原因归类


        cell = rows.getCell(Integer.valueOf(numbervalue[8]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("detail_proposal_type", cellContent);// 详细优化方案类别

        cell = rows.getCell(Integer.valueOf(numbervalue[9]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("trim_village",cellContent);//调整小区/站点名称

        cell = rows.getCell(Integer.valueOf(numbervalue[10]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("property",cellContent);//属性


        cell = rows.getCell(Integer.valueOf(numbervalue[11]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("target",cellContent);//目标值

        cell = rows.getCell(Integer.valueOf(numbervalue[12]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("order_state",cellContent);//维护子工单状态

        cell = rows.getCell(Integer.valueOf(numbervalue[13]));
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("is_solved",cellContent);//评估问题是否已解决

//        cell = rows.getCell(Integer.valueOf(numbervalue[14]));
//        if(cell!=null){
//            cell.setCellType(cell.CELL_TYPE_STRING);
//            cellContent=String.valueOf(cell.getStringCellValue());
//        }else {
//            cellContent="";
//        }
//        criteria.put("order_code",cellContent);//工单编号


        return criteria;
    }
    
    public QueryCriteria getExcelDetailData(int row, org.apache.poi.ss.usermodel.Sheet sheet) {

        QueryCriteria criteria = new QueryCriteria();
        String cellContent = "";
        org.apache.poi.ss.usermodel.Cell cell =null;
        Row rows = sheet.getRow(row - 1);
        int column = 0;
        cell = rows.getCell(column++);
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("cluster_code", cellContent);// 聚合问题点序号

        column=11;
        cell = rows.getCell(column++);
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }

        criteria.put("first_analysis", cellContent);// 初步原因分析

        cell = rows.getCell(column++);
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
            if(cellContent.length()>30002){
                cellContent=cellContent.substring(0,30000);
            }
        }else {
            cellContent="";
        }
        criteria.put("first_proposal", cellContent);// 初步方案

        cell = rows.getCell(column++);
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        String reason= cellContent;
        criteria.put("reason_classify", cellContent);// 原因归类

        cell = rows.getCell(column++);
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("first_proposal_type", cellContent);// 初步优化方案类别

        column = 15;
        cell = rows.getCell(column++);
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
            if(cellContent.length()>30000){
                cellContent=cellContent.substring(0,30000);
            }
        }else {
            cellContent="";
        }
        criteria.put("detail_analysis", cellContent);// 详细原因分析
        if (rows.getPhysicalNumberOfCells() <= column) {
            return criteria;
        }
        cell = rows.getCell(column++);
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("detail_proposal", cellContent);// 详细方案
        if (rows.getPhysicalNumberOfCells() <= column) {
            return criteria;
        }
        int cellrow= column++;
        String title=sheet.getRow(0).getCell(cellrow).getStringCellValue();  //得到第一列标题内容
        cell = rows.getCell(cellrow);
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        if(title.equals("详细原因归类")){
            criteria.put("detail_reason", cellContent);// 详细原因归类
            cell = rows.getCell(column++);
            if(cell!=null){
                cell.setCellType(cell.CELL_TYPE_STRING);
                cellContent=String.valueOf(cell.getStringCellValue());
            }else {
                cellContent="";
            }
            criteria.put("detail_proposal_type", cellContent);// 详细优化方案类别
        }else {
            criteria.put("detail_reason",reason);// 详细原因归类
            criteria.put("detail_proposal_type", cellContent);// 详细优化方案类别
        }
        if(sheet.getRow(0).getPhysicalNumberOfCells()>18){
            String title2=sheet.getRow(0).getCell(22).getStringCellValue();
            if(title2.equals("维护子工单状态")){
                //站点
                cell = rows.getCell(19);
                if(cell!=null){
                    cell.setCellType(cell.CELL_TYPE_STRING);
                    cellContent=String.valueOf(cell.getStringCellValue());
                }else {
                    cellContent="";
                }
                criteria.put("trim_village",cellContent);
                //属性
                cell = rows.getCell(20);
                if(cell!=null){
                    cell.setCellType(cell.CELL_TYPE_STRING);
                    cellContent=String.valueOf(cell.getStringCellValue());
                }else {
                    cellContent="";
                }
                criteria.put("property",cellContent);
                //目标值
                cell = rows.getCell(21);
                if(cell!=null){
                    cell.setCellType(cell.CELL_TYPE_STRING);
                    cellContent=String.valueOf(cell.getStringCellValue());
                }else {
                    cellContent="";
                }
                criteria.put("target",cellContent);
                //工单状态
                cell = rows.getCell(22);
                if(cell!=null){
                    cell.setCellType(cell.CELL_TYPE_STRING);
                    cellContent=String.valueOf(cell.getStringCellValue());
                }else {
                    cellContent="";
                }
                criteria.put("order_state",cellContent);
            }

        }else {
            criteria.put("trim_village","");
            criteria.put("property","");
            criteria.put("target","");
            criteria.put("order_state","");

        }

        String title2=sheet.getRow(0).getCell(18).getStringCellValue();
        //评估问题状态
        if(title2.equals("评估问题是否已解决")){
            cell = rows.getCell(18);
        }else {
            cell = rows.getCell(23);
        }
        if(cell!=null){
            cell.setCellType(cell.CELL_TYPE_STRING);
            cellContent=String.valueOf(cell.getStringCellValue());
        }else {
            cellContent="";
        }
        criteria.put("is_solved",cellContent);

        title2=sheet.getRow(0).getCell(24).getStringCellValue();
        //工单流水号
        if(title2.equals("工单流水号")){
            cell = rows.getCell(24);
            if(cell!=null){
                cell.setCellType(cell.CELL_TYPE_STRING);
                cellContent=String.valueOf(cell.getStringCellValue());
                criteria.put("order_code",cellContent);
            }
        }


        return criteria;
    }


}