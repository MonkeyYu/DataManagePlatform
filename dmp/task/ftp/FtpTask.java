package com.etone.universe.dmp.task.ftp;

import com.etone.daemon.support.Env;
import com.etone.daemon.util.Mixeds;
import com.etone.daemon.util.PropertiesKit;
import com.etone.universe.dmp.task.BaseTask;
import com.etone.universe.dmp.task.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by Macro on 2016/9/12.
 * Copyright 2016 Guangdong Eastone Century Technology Co.,Ltd.
 * All rights reserved.
 */
public abstract class FtpTask extends BaseTask {
    protected static final Logger logger = LoggerFactory.getLogger(FtpTask.class);

    protected String server = "";

    protected String local = "";

    protected String remote = "";

    protected String prefix = "";

    protected String suffix = "";

    protected FtpInfo info = null;

    public boolean isDelSource() {
        return delSource;
    }

    public void setDelSource(boolean delSource) {
        this.delSource = delSource;
    }

    protected boolean delSource = true;

    /**
     * 从file.propertites 读取配置信息
     *
     * @throws Exception
     */
    public void readConf() throws Exception {

        // read configure from ${this.name}.properties
        String cfg = Env.getPath("conf") + File.separator + "fop" + File.separator + this.getName();
        if (Mixeds.isNullOrEmpty(cfg)) {
            throw new Exception("file not found : " + cfg);
        }
        PropertiesKit properties = PropertiesKit.getInstance(new FileInputStream(cfg));

        // ftp local
        String localPath = properties.getString("ftp.local.path", "");
        if (Mixeds.isNullOrEmpty(localPath)) {
            throw new Exception("Properties ftp.local.path is not set on file.properties");
        }
        setLocal(Variables.parse(localPath, getTime()));

        // ftp remote
        String remotePath = properties.getString("ftp.remote.path", "");
        if (Mixeds.isNullOrEmpty(remotePath)) {
            throw new Exception("Properties ftp.remote.path is not set on file.properties");
        }
        setRemote(Variables.parse(remotePath, getTime()));

        // ftp prefix
        String prefix = properties.getString("ftp.prefix", "");
        setPrefix(Variables.parse(prefix, getTime()));

        // ftp suffix
        String suffix = properties.getString("ftp.suffix", "");
        setSuffix(Variables.parse(suffix, getTime()));

        // ftp delSource
        setDelSource(properties.getBoolean("ftp.delSource", true));

        // ftp server
        String server = properties.getString("ftp.server", "");
        if (Mixeds.isNullOrEmpty(server)) {
            throw new Exception("Properties ftp.erver is not set on file.properties");
        }
        setServer(server);

        info = new FtpInfo();
        info.setServer(server);
        String key = "ftp." + server + ".";

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

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
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

    public FtpInfo getInfo() {
        return info;
    }

    public void setInfo(FtpInfo info) {
        this.info = info;
    }


}
