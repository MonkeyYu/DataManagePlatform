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
@SqlTable(name = "d_sqlLog")
public class SqlEvent extends BaseEvent {

    @SqlColumn(name = "vcDataSource")
    private String dataSource = null;

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
