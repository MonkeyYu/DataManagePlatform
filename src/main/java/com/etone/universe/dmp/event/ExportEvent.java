/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.event;

import com.etone.daemon.db.DB;
import com.etone.daemon.db.helper.QueryHelper;
import com.etone.daemon.db.sql.SqlBuilder;
import com.etone.daemon.db.sql.SqlColumn;
import com.etone.daemon.db.sql.SqlTable;

import java.sql.Connection;
import java.util.Date;

/**
 * Created by Lanny on 2016-8-25.
 */
@SqlTable(name = "d_exportLog")
public class ExportEvent extends BaseEvent {

    @SqlColumn(name = "vcPath")
    private String path = "";

    @SqlColumn(name = "douSize")
    private String size = "";

    @SqlColumn(name = "vcSql")
    private String sql = "";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
