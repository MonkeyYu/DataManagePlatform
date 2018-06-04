/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.task.file;

import com.etone.daemon.support.Env;
import com.etone.daemon.util.Mixeds;
import com.etone.daemon.util.PropertiesKit;
import com.etone.daemon.util.XFiles;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.ChmodEvent;
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
public class ChmodTask extends BaseTask {
    private static final Logger logger = LoggerFactory.getLogger(ChmodTask.class);

    private String path = "";

    private int code = 666;

    private String prefix = "";

    private String suffix = "";

    private String newSuffix = "";

    @Override
    public void execute() {

        ChmodEvent event = new ChmodEvent();
        event.initialize(this);
        event.setCode(code);

        // do something
        logger.info("Start chmod task : {}", this.getName());

        try {

            // read configure from file.properties
            readConf();

            // merge file
            logger.info("chmod.path = {}", getPath());
            logger.info("chmod.prefix = {}", getPrefix());
            logger.info("chmod.suffix = {}", getSuffix());
            logger.info("chmod.newSuffix = {}", getNewSuffix());


            // java 命令行不支持修改整个目录下所有文件的权限，需要遍历实现
            File[] files = XFiles.scan(path, prefix, suffix);
            if (files.length == 0) {
                logger.error("scan no file");
                throw new Exception("scan no file in dir " + path);
            }

            for (File file : files) {
                event.setStart(Calendar.getInstance().getTime());
                event.setSize(file.length());

                String filePath = file.getAbsolutePath();
                // 修改文件名的后缀
                if (!suffix.equals(newSuffix) && !"".equals(newSuffix)) {
                    filePath = FileOperator.modifySuffix(file, newSuffix);
                }
                logger.debug("chmod {} {}", code, filePath);
                XFiles.chmod(filePath, code);

                event.setPath(filePath);
                event.setEnd(Calendar.getInstance().getTime());
                Duration d = new Duration(new DateTime(event.getStart()), new DateTime(event.getEnd()));
                event.setSpend(d.getMillis());

                EventService.getInstance().post(event);
            }
        } catch (Exception e) {
            logger.error("Chmod exception : ", e);
            setException("Chmod exception : " + e.getMessage());
            event.setEnd(Calendar.getInstance().getTime());
            event.setException(e.getMessage());
            EventService.getInstance().post(event);
        }
    }

    public void readConf() throws Exception {

        // read configure from ${this.name}.properties
        String cfg = Env.getPath("conf") + File.separator + "fop" + File.separator + this.getName();
        if (Mixeds.isNullOrEmpty(cfg)) {
            throw new Exception("file not found : " + cfg);
        }
        PropertiesKit properties = PropertiesKit.getInstance(new FileInputStream(cfg));
        String key = "chmod.";

        // path
        String path = properties.getString(key + "path", "");
        if (Mixeds.isNullOrEmpty(path)) {
            throw new Exception("Properties " + key + "path is not set on file.properties");
        }
        setPath(Variables.parse(path, getTime()));

        setCode(properties.getInt(key + "code", 666));
        setPrefix(Variables.parse(properties.getString(key + "prefix", ""), getTime()));
        setSuffix(Variables.parse(properties.getString(key + "suffix", ""), getTime()));
        setNewSuffix(Variables.parse(properties.getString(key + "modify.suffix", ""), getTime()));
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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

    public String getNewSuffix() {
        return newSuffix;
    }

    public void setNewSuffix(String newSuffix) {
        this.newSuffix = newSuffix;
    }
}
