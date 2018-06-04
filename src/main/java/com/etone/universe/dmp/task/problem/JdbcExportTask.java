package com.etone.universe.dmp.task.problem;

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

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

/**
 * JDBC数据导出
 * Date: 17-12-20
 * Time: 下午2:49
 * Author: yuxj
 */
    public class JdbcExportTask extends BaseTask{

    public static final Logger logger = LoggerFactory.getLogger(JdbcExportTask.class);

    //导出文件所在根目录（注意最后要带文件分隔符：/，如：/data1/load/）
    protected String rootPath="";

    //导出文件名称，或者是文件夹名称（根据splitFile字段来区分）
    protected String fileName="";

    //是否切割导出文件，是则在根目录下创建文件夹并将切割后文件放在该文件夹中，否则将文件放在根目录下
    protected Boolean splitFile=false;

    //导出的数据是否需要用字符包围（如："189210-1","XXX",...），有的导出字段需要用"包围，故添加此字段来标识
    protected Boolean surround=false;

    //包围字段的字符串，一般为双引号
    protected String surroundStr="";

    //导出文件中字段分隔符
    protected String delimiter="";

    //导出文件的后缀
    protected String suffix="";

    //每个文件中数据的最大记录数，切割文件时根据记录数来切割，避免根据大小切割而造成一条记录被切断的情况
    protected Long size=0L;

    //sql文件名称
    protected String sqlFileName="";

    //数据源
    protected String source="";

    public void execute(){
        //记录日志
        SqlEvent event = new SqlEvent();
        event.setStart(Calendar.getInstance().getTime());
        event.setPriority(getPriority());
        event.setName(getName());
        logger.info("Start Greenplum sql file task : {}", getName());


        //JDBC查询数据库
        Connection conn=null;
        PreparedStatement st=null;
        ResultSet rs=null;
        try {
            conn= DB.getConnection(Env.getProperties().getValue("scheme.ws.datasource." + getSource()));
            conn.setAutoCommit(false);
            String[] sqlArr= readSQL(getSqlFileName());
            if(sqlArr.length>0){
                if(getSplitFile()){
                    //删除原文件夹并重新创建文件
                    File dir=new File(getRootPath()+getFileName());
                    deleteAll(dir);
                    dir.mkdir();
                }else {
                    //如果有同名文件夹，则删除原文件夹
                    File file=new File(getRootPath()+getFileName()+"."+getSuffix());
                    deleteAll(file);
                }
            }
            for(String sql:sqlArr){
                sql = Variables.parse(sql, new Date());
                logger.info(sql);
                // 若出现$linux$特殊字符，则是linux命令
                if (sql.contains("$linux$")){
                    sql = sql.replace("$linux$", "");
                    ProblemUtil.excuteLiuxOrde3r(sql);
                    continue;
                }
                if (sql != null&& sql.trim().toLowerCase().startsWith("select")){
                    st=conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY );
                    st.setFetchSize(5000);
                    rs=st.executeQuery();
                    //是否需要切割文件
//                    testDownload(rs,event);
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
            fos=new FileOutputStream(getRootPath()+getFileName()+"."+getSuffix());
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

    /*
    * 将读取到的数据用文件输出流输出到不同小文件
    * */
    protected void convertToLittleFiles(ResultSet rs,SqlEvent event) throws SQLException{
        Long rowIndex=1L;
//        Integer fileIndex=0;
        FileOutputStream fos=null;
        OutputStreamWriter writer=null;
        StringBuffer content=new StringBuffer();
//        StringBuffer result=new StringBuffer();
        Integer columnCount=rs.getMetaData().getColumnCount();
        Integer i=0;
        try {
            fos=new FileOutputStream(getRootPath()+getFileName()+File.separator+"000000");
            writer=new OutputStreamWriter(fos);
            while (rs.next()){
                //读取一行记录，拼接成字符串，写入到文件中
                for(i=0;i<columnCount;i++){
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
                writer.write(content.toString());
                //每写10000行数据，刷新一次缓存
                if(rowIndex%10000==0 && rowIndex > 0){
                    writer.flush();
                }
                content.setLength(0);
                rowIndex++;
            }
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

    public void testDownload(ResultSet rs,SqlEvent event) throws SQLException{
        logger.info("function testDownload already began---------------------");
        FileWriter fileWriter=null;
        BufferedWriter writer=null;
        StringBuffer content=new StringBuffer();
        Integer columnCount=0;
        Integer i=0;
        Integer count=1;
        try {
            logger.info("start to download file------------------------------------------");
            System.out.println("开始导出文件------------------------------------------");
            fileWriter=new FileWriter(getRootPath()+getFileName()+File.separator+"000000.csv");
            writer=new BufferedWriter(fileWriter);
            while(rs.next()){
                columnCount=rs.getMetaData().getColumnCount();
                for(i=0;i<columnCount;i++){
                    content.append(getDelimiter());
                    if(getSurround()){
                        content.append(getSurroundStr());
                    }
                    content.append(rs.getObject(i + 1));//ResultSet.getObject()方法索引从1开始
                    if(getSurround()){
                        content.append(getSurroundStr());
                    }
                }
                writer.write(content.toString());
                writer.flush();
                if(count%10000==0){
                    logger.info("已导出"+count+++"条数据");
                    System.out.println("已导出"+count+++"条数据");
                }
            }
            writer.flush();
            logger.info("导出完成-------------------------------------------------");
            System.out.println("导出完成-------------------------------------------------");
        }catch (Exception e){
            logger.error("SQLException : ", e);
            event.setException(e.getMessage());
            this.setException(e.getMessage());
        }finally {
            try {
                fileWriter.close();
                writer.close();
            }catch (Exception e){
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

        // 文件后缀
        String suffix = task.attr("suffix");
        if (Mixeds.isNullOrEmpty(suffix)) {
            logger.info("Attribute suffix is not set on task[name=" + getName() + "],use no suffix instead");
            setSuffix("");
        }else{
            setSuffix(suffix);
        }

        // sql文件名称
        String sqlFileName = task.attr("sqlFileName");
        if (Mixeds.isNullOrEmpty(sqlFileName)) {
            throw new Exception("Attribute sqlFileName is not set on task[name=" + getName() + "]");
        }
        setSqlFileName(sqlFileName);

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

    public String getSqlFileName() {
        return sqlFileName;
    }

    public void setSqlFileName(String sqlFileName) {
        this.sqlFileName = sqlFileName;
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
        String path = Env.getPath("conf") + File.separator + "sql" + File.separator + name;
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

    /*
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
}
