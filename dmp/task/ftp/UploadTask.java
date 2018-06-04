/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.task.ftp;

import com.etone.daemon.util.XFiles;
import com.etone.daemon.util.XFtps;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.FtpEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Calendar;

/**
 * Created by Lanny on 2016-8-23.
 */
public class UploadTask extends FtpTask {
    private static final Logger logger = LoggerFactory.getLogger(UploadTask.class);

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

                        // upload
                        XFtps.FtpResult result = ftps.upload(remote, file.getAbsolutePath());
                        // 上传完成后，删除本地文件
                        if (delSource) file.delete();

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

}
