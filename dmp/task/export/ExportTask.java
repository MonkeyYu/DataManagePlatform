package com.etone.universe.dmp.task.export;

import com.etone.daemon.db.DB;
import com.etone.daemon.support.Env;
import com.etone.daemon.util.FileAccessor;
import com.etone.daemon.util.Mixeds;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.SqlEvent;
import com.etone.universe.dmp.task.BaseTask;
import com.etone.universe.dmp.task.Variables;
import com.etone.universe.dmp.util.ProblemUtil;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;

/**
 * JDBC数据导出
 * Date: 17-12-20
 * Time: 下午2:49
 * Auther: yuxj
 */
public class ExportTask extends BaseTask{

    public static final Logger logger = LoggerFactory.getLogger(GpExportTask.class);

    protected String rootPath="";

    protected String fileName="";

    protected Boolean splitFile=false;

    protected Boolean surround=false;

    protected String surroundStr="";

    protected String delimiter="";

    protected String suffix="";

    protected Long size=0L;

    protected String source="";

    public void execute(){
        /*记录日志*/
        SqlEvent event = new SqlEvent();
        event.setStart(Calendar.getInstance().getTime());
        event.setPriority(getPriority());
        event.setName(getName());
        logger.info("Start Greenplum sql file task : {}", getName());

        if(getSplitFile()){
        /*删除原文件并重新创建文件*/
            File dir=new File(getRootPath()+getFileName());
            if(dir.exists()){
                dir.delete();
            }
            dir.mkdir();
        }

        /*JDBC查询数据库*/
        Connection conn=null;
        Statement st=null;
        ResultSet rs=null;
        try {
            conn= DB.getConnection(Env.getProperties().getValue("scheme.ws.datasource." + getSource()));
            st=conn.createStatement();
            String[] sqlArr= readSQL(getName());
            for(String sql:sqlArr){
                logger.info(sql);
                // 若出现$linux$特殊字符，则是linux命令
                if (sql.contains("$linux$")){
                    sql = sql.replace("$linux$", "");
                    ProblemUtil.excuteLiuxOrde3r(sql);
                    continue;
                }
                if (sql != null&& sql.trim().toLowerCase().startsWith("select")){
                    rs=st.executeQuery(sql);
                    /*是否需要切割文件*/
                    if (getSplitFile()){
                        convertToLittleFiles(rs,event);
                    }else{
                        downloadFile(rs,event);
                    }
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

    /*
    * 将ResultSet中文件输出到指定文件中
    * */
    protected void downloadFile(ResultSet rs,SqlEvent event) throws SQLException{
        FileOutputStream fos=null;
        OutputStreamWriter writer=null;
        StringBuffer buffer=new StringBuffer();
        Integer columnCount=0;
        try{
            fos=new FileOutputStream(getRootPath()+getFileName());
            writer=new OutputStreamWriter(fos);
            while(rs.next()){
                //读取一行记录，拼接成字符串，写入到文件中
                buffer.delete(0,buffer.length());
                columnCount=rs.getMetaData().getColumnCount();
                for(int i=0;i<columnCount;i++){
                    buffer.append(getDelimiter());
                    if(getSurround()){
                        buffer.append(getSurroundStr());
                    }
                    buffer.append(rs.getObject(i + 1));
                    if(getSurround()){
                        buffer.append(getSurroundStr());
                    }
                }
                buffer.append(System.getProperty("line.separator", "\n"));
                buffer.deleteCharAt(0);
                writer.write(buffer.toString());
            }
        }catch (Exception e){
            logger.error("SQLException : ", e);
            event.setException(e.getMessage());
            this.setException(e.getMessage());
        }finally {
            try {
                fos.close();
                writer.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /*
    * 将读取到的数据用文件输出流输出到不同小文件
    * */
    protected void convertToLittleFiles(ResultSet rs,SqlEvent event) throws SQLException{
        Long rowIndex=1L;
        Integer fileIndex=0;
        FileOutputStream fos=null;
        OutputStreamWriter writer=null;
        StringBuffer content=new StringBuffer();
        StringBuffer result=new StringBuffer();
        Integer columnCount=0;
        try {
            while (rs.next()){
                //每当读取到size的倍数行数时，关闭输出流，新建输出流写入到新的文件
                if(rowIndex%size==1){
                    //如果输出流已存在则关闭
                    if(writer!=null){
                        writer.flush();
                        if(fos!=null){
                            fos.close();
                        }
                        writer.close();
                    }
                    if(fos!=null){
                        fos.close();
                    }
                    String filename=lpad(fileIndex,6,0)+getSuffix();
                    fos=new FileOutputStream(getRootPath()+getFileName()+File.separator+filename);
                    writer=new OutputStreamWriter(fos);
                    fileIndex++;
                }
                //读取一行记录，拼接成字符串，写入到文件中
                if(content.length()>0){
                    content.delete(0,content.length());
                }
                columnCount=rs.getMetaData().getColumnCount();
                for(int i=0;i<columnCount;i++){
                    content.append(getDelimiter());
                    if(getSurround()){
                        content.append(getSurroundStr());
                    }
                    content.append(rs.getObject(i + 1));//ResultSet.getObject()方法索引从1开始
                    if(getSurround()){
                        content.append(getSurroundStr());
                    }
                }
                content.append(System.getProperty("line.separator", "\n"));
                content.deleteCharAt(0);
                result.append(content);
                //每读取100行数据写入一次数据，刷新一次缓存区
                if(rowIndex%100==0){
                    writer.write(result.toString());
                    writer.flush();
                    result.delete(0,result.length());
                }
                rowIndex++;
            }
            writer.write(result.toString());
            writer.flush();
        }catch (Exception e){
            logger.error("SQLException : ", e);
            event.setException(e.getMessage());
            this.setException(e.getMessage());
        }finally {
            try {
                fos.close();
                writer.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
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

        // 文件夹名称
        String fileName = task.attr("fileName");
        if (Mixeds.isNullOrEmpty(fileName)) {
            throw new Exception("Attribute fileName is not set on task[name=" + getName() + "]");
        }
        //替换文件名中的${hiveconf:yyyyMMdd}
        fileName= Variables.parse(fileName,new Date());
        setFileName(fileName);

        //是否创建文件夹并切割文件
        String splitFile=task.attr("splitFile");
        if (Mixeds.isNullOrEmpty(fileName)) {
            logger.info("Attribute splitFile is not set on task[name="+getName()+"],use default value false");
            setSplitFile(false);
        }else{
            if("true".equals(splitFile)){
                setSplitFile(true);
            }else{
                setSplitFile(false);
            }
        }

        //导出字段是否需要字符包围，如用双引号包围："189724-1","XXX",...
        String surround=task.attr("surround");
        if(Mixeds.isNullOrEmpty(surround)){
            logger.info("Attribute surround is not set on task[name=" + getName() + "],default nothing surrounded");
            setSurround(false);
        }else{
            if("true".equals(surround)){
                setSurround(true);
            }else{
                setSurround(false);
            }
        }

        //包围字段的字符
        String surroundStr=task.attr("surroundStr");
        if(Mixeds.isNullOrEmpty(surroundStr)){
            logger.info("Attribute surroundStr is not set on task[name=" + getName() + "],default nothing surrounded");
        }else {
            setSurroundStr(surroundStr);
        }

        // 字段分割符
        String delimiter = task.attr("delimiter");
        if (Mixeds.isNullOrEmpty(delimiter)) {
            logger.info("Attribute delimiter is not set on task[name=" + getName() + "],use default delimiter '|'");
            setDelimiter("|");
        }else{
            setDelimiter(delimiter);
        }

        // 每个切割后小文件所允许的行数
        String size = task.attr("size");
        if (Mixeds.isNullOrEmpty(size)) {
            logger.info("Attribute size is not set on task[name=" + getName() + "],use default size 1000000");
            setSize(1000000L);
        }else{
            setSize(Long.parseLong(size));
        }

        // 小文件后缀
        String suffix = task.attr("suffix");
        if (Mixeds.isNullOrEmpty(suffix)) {
            logger.info("Attribute suffix is not set on task[name=" + getName() + "],use no suffix instead");
            setSuffix("");
        }else{
            setSuffix(suffix);
        }

        // 数据源名称
        String source = task.attr("source");
        if (Mixeds.isNullOrEmpty(source)) {
            throw new Exception("Attribute source is not set on task[name=" + getName() + "]");
        }
        setSource(source);
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getSplitFile() {
        return splitFile;
    }

    public void setSplitFile(Boolean splitFile) {
        this.splitFile = splitFile;
    }

    public Boolean getSurround() {
        return surround;
    }

    public void setSurround(Boolean surround) {
        this.surround = surround;
    }

    public String getSurroundStr() {
        return surroundStr;
    }

    public void setSurroundStr(String surroundStr) {
        this.surroundStr = surroundStr;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    /*
    * 获取切割后文件的文件名：000001
    * */
    public static String lpad(Object str,int num,Object pad){
        String n_str=str.toString();
        if(str==null)
            n_str= " ";
        for(int i=str.toString().length();i <num;i++){
            n_str=pad.toString()+n_str;
        }
        return n_str;
    }
}
