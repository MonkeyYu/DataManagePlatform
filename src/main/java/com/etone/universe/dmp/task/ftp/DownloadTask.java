package com.etone.universe.dmp.task.ftp;

import com.etone.daemon.util.XFiles;
import com.etone.daemon.util.XFtps;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.FtpEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Macro on 2016/9/12.
 * Copyright 2016 Guangdong Eastone Century Technology Co.,Ltd.
 * All rights reserved.
 */
public class DownloadTask extends FtpTask {
    private static final Logger logger = LoggerFactory.getLogger(DownloadTask.class);

    @Override
    public void execute() {

        FtpEvent event = new FtpEvent();
        event.setPriority(getPriority());
        event.setName(getName());
        event.setType("download");
        event.setStart(Calendar.getInstance().getTime());

        // do something
        logger.info("Start ftp task : {}", this.getName());

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

            // 先创建本地目录，如果目录存在，则不做任何处理
            XFiles.getInstance(local).createDir();

            // download file by ftp
            XFtps ftps = XFtps.getInstance(info.getIp(), info.getPort(), info.getUser(), info.getPassword());
            if (ftps.connect()) {
                logger.info("download path : {}, prefix: {}, suffix: {}", remote, prefix, suffix);

                List<XFtps.FtpResult> result = ftps.download(remote, local, prefix, suffix);

                for (int i = 0; i < result.size(); i++) {
                    XFtps.FtpResult file = result.get(i);
                    // 记录ftp的上传文件大小及速率
                    event.setSize(file.getSize());
                    event.setRate(file.getRate());
                    event.setLocal(getLocal());
                    event.setRemote(file.getPath());
                    event.setEnd(Calendar.getInstance().getTime());
                    event.setSpend(file.getCost());

                    EventService.getInstance().post(event);
                }

                // disconnect ftp
                ftps.disconnect();
                logger.info("finish download : {} files downloaded", result.size());
            }
        } catch (Exception e) {
            logger.error("Download exception : ", e);
            setException(e.getMessage());
            event.setEnd(Calendar.getInstance().getTime());
            event.setException(e.getMessage());
            EventService.getInstance().post(event);
        }
    }

}
