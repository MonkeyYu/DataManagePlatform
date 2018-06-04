package com.etone.universe.dmp.task.ftp;

/**
 * Created by Lanny on 2016-8-30.
 */
public class FtpInfo {

    private String user = "";

    private String ip = "";

    private int port = 21;

    private String password = "";

    private String server = "";

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
