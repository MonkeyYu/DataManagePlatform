/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.task.file;

import com.etone.daemon.support.Env;
import com.etone.daemon.util.Mixeds;
import com.etone.daemon.util.PropertiesKit;
import com.etone.universe.dmp.MergeEvent;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.task.BaseTask;
import com.etone.universe.dmp.task.Variables;
import com.etone.universe.dmp.util.FileOperator;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;

/**
 * Hive任务
 *
 * @author <a href="mailto:lanny@qq.com">LiangYonghua</a>
 * @since $$Id$$
 */
public class MergeTask extends BaseTask {
    private static final Logger logger = LoggerFactory.getLogger(MergeTask.class);

    private String path = "";

    private String targetFile = "";

    private String sourceCode = "";

    private String targetCode = "";

    private String prefix = "";

    private String suffix = "";

    private String head = "";

    private boolean delSource = true;

    @Override
    public void execute() {

        MergeEvent event = new MergeEvent();
        event.initialize(this);
        event.setStart(Calendar.getInstance().getTime());

        // do something
        logger.info("Start merge task : {}", this.getName());

        try {

            // read configure from file.properties
            readConf();

            // merge file
            logger.info("merge.path = {}", getPath());
            logger.info("merge.target = {}", getTargetFile());
            logger.info("merge.prefix = {}", getPrefix());
            logger.info("merge.suffix = {}", getSuffix());
            logger.info("merge.head = {}", getHead());
            logger.info("merge.delSource = {}", isDelSource());

//            event.setSourcePath(path);
//            event.setTargetFile(targetFile);
//            event.setPrefix(prefix);
//            event.setSuffix(suffix);

            FileOperator.merge(path, targetFile, sourceCode, targetCode, prefix, suffix, delSource, head);

            // insert db log
            File file = new File(targetFile);
            if (file.exists()) {
//                event.setSize(file.length());
            } else {
                throw new Exception("merge exception : no file found");
            }
        } catch (Exception e) {
            logger.error("exception : ", e);
            setException("exception : " + e);
            event.setException("exception : " + e);
        }

        event.setEnd(Calendar.getInstance().getTime());
        Duration d = new Duration(new DateTime(event.getStart()), new DateTime(event.getEnd()));
        event.setSpend(d.getMillis());

        EventService.getInstance().post(event);
    }

    /**
     * 读取合并配置信息
     */
    public void readConf() throws Exception {

        // read configure from ${this.name}.properties
        String cfg = Env.getPath("conf") + File.separator + "fop" + File.separator + this.getName();
        if (Mixeds.isNullOrEmpty(cfg)) {
            throw new Exception("file not found : " + cfg);
        }
        PropertiesKit properties = PropertiesKit.getInstance(new FileInputStream(cfg));

        // 读取文件切割配置信息
        // source path
        String sourcePath = properties.getString("merge.source.file", "");
        if (Mixeds.isNullOrEmpty(sourcePath)) {
            throw new Exception("Properties merge.source.file is not set on " + cfg);
        }
        setPath(Variables.parse(sourcePath, getTime()));

        // target path
        String targetPath = properties.getString("merge.target.file", "");
        if (Mixeds.isNullOrEmpty(targetPath)) {
            throw new Exception("Properties merge target.file is not set on " + cfg);
        }
        setTargetFile(Variables.parse(targetPath, getTime()));

        // source prefix
        setPrefix(properties.getString("merge.prefix", ""));

        // source suffix
        setSuffix(properties.getString("merge.suffix", ""));

        // source code
        String sourceCode = properties.getString("merge.source.code", "UTF-8");
        if (Mixeds.isNullOrEmpty(sourceCode)) {
            sourceCode = "UTF-8";
        }
        setSourceCode(sourceCode);

        // target code
        String targetCode = properties.getString("merge.target.code", "UTF-8");
        if (Mixeds.isNullOrEmpty(targetCode)) {
            targetCode = "UTF-8";
        }
        setTargetCode(targetCode);

        // file head
        setHead(properties.getString("merge.head", ""));

        // delete source file
        setDelSource(properties.getBoolean("merge.delete.source", true));
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
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
