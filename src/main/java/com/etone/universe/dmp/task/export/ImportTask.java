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
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: yuxj
 * Date: 17-12-15
 * Time: 下午5:11
 * To change this template use File | Settings | File Templates.
 */
public class ImportTask extends ProblemTask {

    public static final Logger logger = LoggerFactory.getLogger(ImportTask.class);

    protected String dirPath="";

    @Override
    public void execute(){
        SqlEvent event = new SqlEvent();
        event.setStart(Calendar.getInstance().getTime());
        event.setPriority(getPriority());
        event.setName(getName());
        logger.info("Start Greenplum sql file task : {}", this.getName());
        Connection conn=null;
        try {
            conn=DB.getConnection(Env.getProperties().getValue("scheme.ws.datasource." + getDataSource()));
            String[] sqlArr=readSQL(getName());
            for (String sql : sqlArr) {
                sql = Variables.parse(sql, new Date());
                logger.info(sql);
                // 若出现$linux$特殊字符，则是linux命令
                if (sql.contains("$linux$")){
                    sql = sql.replace("$linux$", "");
                    excuteLiuxOrde3r(sql);
                    continue;
                }
                if (sql != null){
                    execute(conn, sql);
                }
            }
            //执行成功就删除文件及文件夹
            File file=new File(dirPath);
            if(file.exists()){
                file.delete();
            }
        }catch (Exception e){
            logger.error("SQLException : ", e);
            event.setException(e.getMessage());
            this.setException(e.getMessage());
        }finally {
            try {
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

    @Override
    public void fromXml(Element task) throws Exception {
        // 初始化父类属性
        super.fromXml(task);

        // 存放导出文件文件夹
        String dirPath = task.attr("dirPath");
        if (Mixeds.isNullOrEmpty(dirPath)) {
            throw new Exception("Attribute dirPath is not set on task[name=" + getName() + "]");
        }
        setDirPath(dirPath);
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }
}
