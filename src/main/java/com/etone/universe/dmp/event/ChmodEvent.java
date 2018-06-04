package com.etone.universe.dmp.event;

import com.etone.daemon.db.sql.SqlColumn;
import com.etone.daemon.db.sql.SqlTable;

import java.util.Date;

/**
 * Created by Macro on 2016/9/20.
 * Copyright 2016 Guangdong Eastone Century Technology Co.,Ltd.
 * All rights reserved.
 */
@SqlTable(name = "d_fileLog")
public class ChmodEvent extends BaseEvent {

    @SqlColumn(name = "intCode")
    private int code = 0;

    @SqlColumn(name = "vcPath")
    private String path = "";

    @SqlColumn(name = "douSize")
    private long size = 0;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
