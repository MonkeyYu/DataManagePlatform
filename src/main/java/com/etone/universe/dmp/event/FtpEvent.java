/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.event;

import com.etone.daemon.db.sql.SqlColumn;
import com.etone.daemon.db.sql.SqlTable;

import java.util.Date;

/**
 * Created by Lanny on 2016-8-25.
 */
@SqlTable(name = "d_ftpLog")
public class FtpEvent extends BaseEvent {

    @SqlColumn(name = "vcType")
    private String type = null;

    @SqlColumn(name = "vcServer")
    private String server = "";

    @SqlColumn(name = "vcLocal")
    private String local = "";

    @SqlColumn(name = "vcRemote")
    private String remote = "";

    @SqlColumn(name = "douSize")
    private long size = 0;

    @SqlColumn(name = "douSpeed")
    private String rate = "0";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
