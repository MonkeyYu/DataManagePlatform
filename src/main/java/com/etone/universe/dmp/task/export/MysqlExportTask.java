package com.etone.universe.dmp.task.export;

import com.etone.daemon.db.DB;
import com.etone.daemon.support.Env;
import com.etone.daemon.util.FileAccessor;
import com.etone.daemon.util.Mixeds;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.SqlEvent;
import com.etone.universe.dmp.task.Variables;
import com.etone.universe.dmp.task.problem.ProblemTask;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: yuxj
 * Date: 17-12-15
 * Time: 下午5:11
 * To change this template use File | Settings | File Templates.
 */
public class MysqlExportTask extends ProblemTask {

    public static final Logger logger = LoggerFactory.getLogger(MysqlExportTask.class);

    @Override
    public void execute(){
        String filePath=getFilePath();
        //get yesterday's date and replace the 'Date' in the path
        SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd");
        Date nowaday=new Date();
        String date=format.format(nowaday.getTime()-1000*60*60*24);
        filePath=filePath.replaceAll("Date",date);
//        logger.info("Hello World!");
        SqlEvent event = new SqlEvent();
        event.setStart(Calendar.getInstance().getTime());
        event.setPriority(getPriority());
        event.setName(getName());
        logger.info("Start Mysql sql file task : {}", this.getName());
        Connection conn=null;
        FileOutputStream fos=null;
        OutputStreamWriter writer=null;
        Statement st=null;
        ResultSet rs=null;
        try {
            conn=DB.getConnection(Env.getProperties().getValue("scheme.ws.datasource." + getDataSource()));
            fos=new FileOutputStream(filePath);
            writer=new OutputStreamWriter(fos);
            st=conn.createStatement();
            String[] sqlArr=readSQL(getName());
//            String[] sqlArr=new String[]{"select * from tbUser;"};
            Integer columnCount=0;
            String content="";
            for (String sql : sqlArr) {
                sql = Variables.plusDay(sql, new Date());
                logger.info(sql);
                // 若出现$linux$特殊字符，则是linux命令
                if (sql.contains("$linux$")) {
                    sql = sql.replace("$linux$", "");
                    excuteLiuxOrde3r(sql);
                    continue;
                }
                if (sql != null
                        && sql.trim().toLowerCase().startsWith("select")){
                    rs=st.executeQuery(sql);
                    while(rs.next()){
                        //读取一行记录，拼接成字符串，写入到文件中
                        content="";
                        columnCount=rs.getMetaData().getColumnCount();
                        for(int i=0;i<columnCount;i++){
                            content=content+"|"+"\""+rs.getObject(i+1)+"\"";//ResultSet.getObject()方法索引从1开始
                        }
                        content+=System.getProperty("line.separator", "\n");
                        content=content.substring(1);
                        writer.write(content);
                    }
                    //为防止数据不能写入到文件中，数据库查询后将所有在内存中的数据刷新到文件中
                    writer.flush();
                } else {
                    execute(conn, sql);
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
            try {
                fos.close();
                writer.close();
            }catch (IOException e){
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
     * 从文件中读取SQL脚本
     *
     * @param name
     * @return
     */
    public String[] readSQL(String name) throws Exception {
        StringBuilder content = new StringBuilder();
        // read configure from conf/fop/${this.name}
        String path = Env.getPath("conf") + File.separator + "sql" + File.separator + this.getName();
        if (Mixeds.isNullOrEmpty(path)) {
            throw new Exception("file not found : " + path);
        }
        logger.debug("read sql from file : " + path);
        FileAccessor accessor = FileAccessor.getInstance(path);
        for (String line : accessor) {
            line = line.trim();
            // 跳过注解行或者空行；
            if (!Mixeds.isNullOrEmpty(line) && !line.startsWith("--") && !line.startsWith("#")) {
                content.append("\n");
                content.append(line);
            }
        }
        // 对SQL内容进行分隔，分隔符为英文;
        String[] sql = content.toString().split(";");
        return sql;
    }

    /**
     * 执行linux命令方法
     * @param command
     * @return
     */
    public boolean excuteLiuxOrde3r(String command) {
        addDetaileLog("执行命令：" + command);
        boolean returnFlag = false;
        StringBuilder sb = new StringBuilder();
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            returnFlag = true;
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String lo;
            while ((lo = bufferedReader.readLine()) != null) {
                sb.append(lo);
                sb.append("\n");
            }
            process.waitFor();
            // 等待n秒后destory进程
        } catch (Exception e) {
            addDetaileLog("执行命令[" + command + "]出错:" + e.getMessage());
        }
        process.destroy();
        return returnFlag;
    }
}
