/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.task.export;

import com.etone.daemon.support.Env;
import com.etone.daemon.util.Mixeds;
import com.etone.daemon.util.PropertiesKit;
import com.etone.daemon.util.XFiles;
import com.etone.daemon.util.XFtps;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.FtpEvent;
import com.etone.universe.dmp.task.Variables;
import com.etone.universe.dmp.task.ftp.FtpInfo;
import com.etone.universe.dmp.task.ftp.FtpTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: yuxj
 * Date: 17-12-19
 * Time: 下午5:11
 * To change this template use File | Settings | File Templates.
 */
public class GpUploadTask extends FtpTask {
    private static final Logger logger = LoggerFactory.getLogger(GpUploadTask.class);

    @Override
    public void execute() {

        FtpEvent event = new FtpEvent();
        event.initialize(this);
        event.setType("upload");

        // do something
        logger.info("Start upload task : {}", this.getName());

        try {
            // read configure from ftp.xml
            readConf();

            logger.info("ftp.local.path = {}", getLocal());
            logger.info("ftp.remote.path = {}", getRemote());
            logger.info("ftp.prefix= {}", getPrefix());
            logger.info("ftp.suffix = {}", getSuffix());
            logger.info("ftp.server.name = {}", getServer());
            logger.info("ftp.server.ip = {}", info.getIp());
            logger.info("ftp.server.port = {}", info.getPort());
            logger.info("ftp.server.user = {}", info.getUser());
            logger.info("ftp.server.password = {}", info.getPassword());

            // upload file by ftp
            XFtps ftps = XFtps.getInstance(info.getIp(), info.getPort(), info.getUser(), info.getPassword());
            if (ftps.connect()) {
                File[] files = XFiles.scan(local, prefix, suffix);
                logger.info("scan path : {}, prefix: {}, suffix: {}, and found {} files to upload", local, prefix, suffix, files.length);
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (!file.isDirectory()) {
                        event.setStart(Calendar.getInstance().getTime());
                        logger.info("Start to upload");
                        // upload
                        XFtps.FtpResult result = ftps.upload(remote, file.getAbsolutePath());
                        // 上传完成后，删除本地文件
                        if (delSource) file.delete();
                        logger.info("Size:  "+result.getSize());
                        logger.info("Rate:  "+result.getRate());
                        // 记录ftp的上传文件大小及速率
                        event.setSize(result.getSize());
                        event.setRate(result.getRate());
                        event.setLocal(file.getAbsolutePath());
                        event.setRemote(getRemote());
                        event.setEnd(Calendar.getInstance().getTime());
                        event.setSpend(result.getCost());

                        EventService.getInstance().post(event);
                    }
                }

                // disconnect ftp
                ftps.disconnect();
                logger.info("finish upload : {} files uploaded", files.length);
            }
        } catch (Exception e) {
            logger.error("Upload exception : ", e);
            setException("Upload exception : " + e.getMessage());
            event.setEnd(Calendar.getInstance().getTime());
            event.setException(e.getMessage());
            EventService.getInstance().post(event);
        }
    }

    /**
     * 从file.propertites 读取配置信息
     *
     * @throws Exception
     */
    @Override
    public void readConf() throws Exception {
        String configName="ftp.nokia.gp.";
        // read configure from ${this.name}.properties
        String cfg = Env.getPath("conf") + File.separator + this.getName();
        if (Mixeds.isNullOrEmpty(cfg)) {
            throw new Exception("file not found : " + cfg);
        }
        PropertiesKit properties = PropertiesKit.getInstance(new FileInputStream(cfg));
        // ftp local
        String localPath = properties.getString(configName+"local.path", "");
        if (Mixeds.isNullOrEmpty(localPath)) {
            throw new Exception("Properties ftp.nokia.local.path is not set on file.properties");
        }
        setLocal(Variables.parse(localPath, getTime()));
        // ftp remote
        String remotePath = properties.getString(configName+"remote.path", "");
        if (Mixeds.isNullOrEmpty(remotePath)) {
            throw new Exception("Properties ftp.nokia.remote.path is not set on file.properties");
        }
        setRemote(Variables.parse(remotePath, getTime()));

        // ftp prefix
        String prefix = properties.getString(configName+"prefix", "");
        setPrefix(Variables.parse(prefix, getTime()));

        // ftp suffix
        String suffix = properties.getString(configName+"suffix", "");
        setSuffix(Variables.parse(suffix, getTime()));

        // ftp delSource
        setDelSource(properties.getBoolean(configName+"delSource", true));

        // ftp server
        String server = properties.getString(configName+"server", "");
        if (Mixeds.isNullOrEmpty(server)) {
            throw new Exception("Properties ftp.nokia.server is not set on application.properties");
        }
        setServer(server);

        info = new FtpInfo();
        info.setServer(server);
        String key =configName + server + ".";

        // 读取文件ftp server配置信息


        String serverIp = Env.getProperties().getString(key + "ip", "");
        if (Mixeds.isNullOrEmpty(serverIp)) {
            throw new Exception("Properties " + key + "ip is not set on application.properties");
        }
        info.setIp(serverIp);

        // ftp port default : 21
        int serverPort = Env.getProperties().getInt(key + "port", 21);
        info.setPort(serverPort);

        // ftp connect user
        String serverUser = Env.getProperties().getString(key + "user", "");
        if (Mixeds.isNullOrEmpty(serverUser)) {
            throw new Exception("Properties " + key + "user is not set on application.properties");
        }
        info.setUser(serverUser);

        // ftp connect password
        String passwd = Env.getProperties().getString(key + "passwd", "");
        if (Mixeds.isNullOrEmpty(passwd)) {
            throw new Exception("Properties " + key + "passwd is not set on application.properties");
        }
        info.setPassword(passwd);

    }
}
