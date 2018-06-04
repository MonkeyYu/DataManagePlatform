package com.etone.universe.dmp.task.file;

import com.etone.daemon.support.Env;
import com.etone.daemon.util.Mixeds;
import com.etone.daemon.util.PropertiesKit;
import com.etone.daemon.util.XFiles;
import com.etone.universe.dmp.SplitEvent;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.task.BaseTask;
import com.etone.universe.dmp.task.Variables;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Lanny on 2016-9-1.
 */
public class SplitTask extends BaseTask {
    private static final Logger logger = LoggerFactory.getLogger(MergeTask.class);

    private String sourceFile = "";

    private String targetFile = "";

    private int size = 1024 * 1024 * 10; // 默认分割文件的大小为10M

    private String sourceCode = "utf-8";

    private String targetCode = "utf-8";

    private boolean delSource = true;

    @Override
    public void execute() {

        SplitEvent event = new SplitEvent();
//        event.initialize(this);
//        event.setStart(Calendar.getInstance().getTime());


        // do something
        logger.info("Start split task : {}", this.getName());

        try {

            // read configure from file.properties
            readConf();

            // split file
            logger.info("split sourceFile={}, targetFile={}, size={}", sourceFile, targetFile, size);
            logger.info("split.sourceFile = {}", getSourceFile());
            logger.info("split.targetFile = {}", getTargetFile());
            logger.info("split.sourceCode = {}", getSourceCode());
            logger.info("split.targetCode = {}", getTargetCode());
            logger.info("split.size = {}", getSize());

            ArrayList<String> list = XFiles.cut(sourceFile, targetFile, size, sourceCode, targetCode, delSource);

            // 遍历cut后的子文件
            for (String filePath : list){
                File file = new File(filePath);
//                event.setSize(file.length());
//                event.setTargetCode(file.getAbsolutePath());
//                event.setEnd(Calendar.getInstance().getTime());
//                Duration d = new Duration(new DateTime(event.getStart()), new DateTime(event.getEnd()));
//                event.setSpend(d.getMillis());

                EventService.getInstance().post(event);
            }

        } catch (Exception e) {
            logger.error("Split exception : ", e);
            setException("Split exception : " + e.getMessage());
//            event.setEnd(Calendar.getInstance().getTime());
//            event.setException(e.getMessage());
            EventService.getInstance().post(event);
        }
    }


    /**
     * 从split.properties中读取配置
     * @return
     */
    public void readConf() throws Exception {

        // read configure from conf/fop/${this.name}.properties
        String cfg = Env.getPath("conf") + File.separator + "fop" + File.separator + this.getName();
        if (Mixeds.isNullOrEmpty(cfg)) {
            throw new Exception("file not found : " + cfg);
        }
        PropertiesKit properties = PropertiesKit.getInstance(new FileInputStream(cfg));

        // 读取文件切割配置信息
        // source path
        String sourcePath = properties.getString("cut.source.file", "");
        if (Mixeds.isNullOrEmpty(sourcePath)) {
            throw new Exception("Properties cut.source.file is not set on file.properties");
        }
        setSourceFile(Variables.parse(sourcePath, getTime()));

        // target path
        String targetPath = properties.getString("cut.target.file", "");
        if (Mixeds.isNullOrEmpty(targetPath)) {
            throw new Exception("Properties cut.target.file is not set on file.properties");
        }
        setTargetFile(Variables.parse(targetPath, getTime()));

        // source code
        String sourceCode = properties.getString("cut.source.code", "UTF-8");
        if (Mixeds.isNullOrEmpty(sourceCode)) {
            sourceCode = "UTF-8";
        }
        setSourceCode(sourceCode);

        // target code
        String targetCode = properties.getString("cut.target.code", "UTF-8");
        if (Mixeds.isNullOrEmpty(targetCode)) {
            targetCode = "UTF-8";
        }
        setTargetCode(targetCode);

        // target file size
        setSize(properties.getInt("cut.split.size", 1024 * 1024 * 10));

        // delete source file
        setDelSource(properties.getBoolean("cut.delete.source", true));
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getTargetCode() {
        return targetCode;
    }

    public void setTargetCode(String targetCode) {
        this.targetCode = targetCode;
    }

    public boolean isDelSource() {
        return delSource;
    }

    public void setDelSource(boolean delSource) {
        this.delSource = delSource;
    }
}
