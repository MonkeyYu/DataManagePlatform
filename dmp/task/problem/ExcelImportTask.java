package com.etone.universe.dmp.task.problem;

import com.etone.daemon.db.DB;
import com.etone.daemon.support.Env;
import com.etone.daemon.util.Mixeds;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.SqlEvent;
import com.etone.universe.dmp.task.BaseTask;
import com.etone.universe.dmp.util.ProblemUtil;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * 解析Excel数据，并导入到数据库；该Task会自动根据数据日期自动给表创建分区，所以不需要sql文件
 * 只匹配特定的excel名里面的特定sheet名，其他的excel文件和sheet不做处理
 * Date: 18-2-5
 * Time: 上午11:17
 * Author: yuxj
 */
    public class ExcelImportTask extends BaseTask{

    public static final Logger logger = LoggerFactory.getLogger(ExcelImportTask.class);

    private static Map<String,String> sheetTableMap=new HashMap<String,String>();

    private static List<String> regArr=new ArrayList<String>();

    //待扫描的目录
    protected String rootPath="";

    static {
        sheetTableMap.put("回落TAC-LAC不一致TOP小区","yt_csfbcelltaclac_cell_month");
        sheetTableMap.put("回落失败TOP小区","yt_csfbcellhlsbtopcell_cell_month");
        sheetTableMap.put("回落失败TOP小区矩阵","yt_csfbcellhlsbtopcelljz_cell_month");
        sheetTableMap.put("寻呼失败TOP小区","yt_csfbcellxhsbtopcell_cell_month");
        sheetTableMap.put("MVQ普通语音告警小区","yt_mvqptyygjxq_cell_month");
        sheetTableMap.put("soc-8e52275b-a235-4701-bdd8-9e1","yt_socgjcell_cell_month");
        sheetTableMap.put("esrvcc无效","yt_voltecellesrvccwx_cell_month");
        sheetTableMap.put("esrvcc拥塞","yt_voltecellesrvccys_cell_month");
        sheetTableMap.put("ASR掉话","yt_voltecellasr_cell_month");
        regArr.add("[0-9.]+soc[0-9a-z-.]+");
        regArr.add("[0-9.]+VOLTE告警小区[0-9a-z_.]+");
        regArr.add("[0-9.]+CSFB告警小区[0-9a-z_.]+");
        regArr.add("[0-9.]+MVQ普通语音告警小区[0-9a-z_.]+");
    }

    public void execute(){
        //记录日志
        SqlEvent event = new SqlEvent();
        event.setStart(Calendar.getInstance().getTime());
        event.setPriority(getPriority());
        event.setName(getName());
        logger.info("Start Greenplum sql file task : {}", getName());

        Connection conn=null;
        PreparedStatement st=null;
        try {
            File root=new File(getRootPath());
            File[] files=root.listFiles();
            //如果/data2/orderdata_bak目录不存在，则创建目录
            ProblemUtil.excuteLiuxOrde3r("ls "+getRootPath()+"/../orderdata_bak || mkdir "+getRootPath()+"/../orderdata_bak");
            for(File file:files){
                if(file.getName().matches(regArr.get(0))||file.getName().matches(regArr.get(1))||file.getName().matches(regArr.get(2))||file.getName().matches(regArr.get(3))){
                    logger.info("Begin to read excel,the excel's name is "+file.getName());
                    Workbook wb= WorkbookFactory.create(file);
//                    Workbook wb= WorkbookFactory.create(new File("E:\\etone\\data_20180202\\CSFB告警小区_20180111_762.xls"));
                    Integer sheetCount=wb.getNumberOfSheets();
                    conn= DB.getConnection(Env.getProperties().getValue("scheme.ws.datasource.gp"));
                    //将变量定义在循环外，减少系统的压力
                    StringBuffer sql=new StringBuffer();
                    String partitionSql;
                    short cellNum;
                    Sheet sheet;
                    Integer rowNum;
                    Row row;
                    Cell cell;
                    for(int k=0;k<sheetCount;k++){
                        sheet=wb.getSheetAt(k);
                        logger.info("The SheetName is : "+sheet.getSheetName());
                        //如果存在不在sheetTableMap中的sheet名称，跳过改sheet
                        if(!sheetTableMap.containsKey(sheet.getSheetName())){
                            logger.info(sheet.getSheetName()+"不在处理范围内，跳过该sheet");
                            continue;
                        }
                        rowNum=sheet.getLastRowNum();
                        for(int i=0;i<rowNum;i++){
                            row=sheet.getRow(i);
                            //如果第一个cell为空，跳过该行
                            if(row.getCell(0)==null){
                                continue;
                            }
                            //过滤掉不符合条件的数据,示例数据里面所有数据行第一个数据都是日期，所以用日期来做正则表达式匹配:[0-9]{8}
                            if(!row.getCell(0).toString().matches("20[0-9]{6}")){
                                continue;
                            }
                            //分区不存在则创建分区
                            if(file.getName().matches(regArr.get(0))){
                                partitionSql="select create_partition_if_not_exists('yt_socgjcell_cell_month','"+row.getCell(0)+"');";
                            }else{
                                partitionSql="select create_partition_if_not_exists('"+sheetTableMap.get(sheet.getSheetName())+"','"+row.getCell(0)+"');";
                            }
                            st=conn.prepareStatement(partitionSql.toString());
                            st.execute();
                            //往分区中插数据
                            if(file.getName().matches(regArr.get(0))){
                                sql.append("insert into yt_socgjcell_cell_month ").append(" values ('"+file.getName()+"',");
                            }else{
                                sql.append("insert into ").append(sheetTableMap.get(sheet.getSheetName())).append(" values ('"+file.getName()+"',");
                            }
                            cellNum=row.getLastCellNum();
                            for(int j=0;j<cellNum;j++){
                                cell=row.getCell(j);
                                //当cell为空值时，插入null
                                if(cell.toString().trim().isEmpty()){
                                    sql.append("null,");
                                }else{
                                    sql.append("'").append(cell.toString()).append("',");
                                }
                            }
                            sql.deleteCharAt(sql.length()-1).append(");");
                            st=conn.prepareStatement(sql.toString());
                            st.executeUpdate();
                            sql.setLength(0);
                        }
                    }
                    //数据入完库后，将Excel移到另一个文件夹，避免下次扫描时再次入库
                    String linuxOrder="mv " +getRootPath()+file.getName()+" "+getRootPath()+"../orderdata_bak ";
                    ProblemUtil.excuteLiuxOrde3r(linuxOrder);
                }
            }
        }catch (Exception e){
            logger.error("SQLException : ", e);
            event.setException(e.getMessage());
            this.setException(e.getMessage());
        }finally {
            try {
                st.close();
                conn.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }

        event.setEnd(Calendar.getInstance().getTime());
        Duration d = new Duration(new DateTime(event.getStart()), new DateTime(
                event.getEnd()));
        event.setSpend(d.getMillis());
        EventService.getInstance().post(event);
    }

    /**
     * 从xml中初始化任务的属性，该方法程序自动执行，无需手动调用
     * @param task
     */
    @Override
    public void fromXml(Element task) throws Exception {
        super.fromXml(task);

        // 存放导出文件的根目录
        String rootPath = task.attr("rootPath");
        if (Mixeds.isNullOrEmpty(rootPath)) {
            throw new Exception("Attribute rootPath is not set on task[name=" + getName() + "]");
        }
        setRootPath(rootPath);
    }

    /**
    *  删除文件及文件夹，如果文件夹下有文件，调用delete()方法无法删除文件夹，要将文件夹下所有文件删掉才行
    * */
    public static  void deleteAll(File path) {
        if (!path.exists())   //路径存在
            return;
        if (path.isFile()) {  //是文件
            path.delete();
            return;
        }
        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            deleteAll(files[i]);
        }
        path.delete();
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
}
