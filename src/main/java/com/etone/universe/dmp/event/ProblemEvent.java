/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.event;

import java.util.Calendar;

import com.etone.daemon.db.sql.SqlColumn;
import com.etone.daemon.db.sql.SqlTable;
import com.etone.universe.dmp.task.problem.ProblemTask;

/**
 * Created by Lanny on 2016-8-25.
 */
@SqlTable(name = "d_problemLog")
public class ProblemEvent extends BaseEvent {

	@SqlColumn(name = "vclogid")
	private String vclogid = "";

	@SqlColumn(name = "intnum")
	private int num = 0;

	@SqlColumn(name = "vcstatus")
	private String vcstatus = "1";

	public void initialize(ProblemTask task) {
		super.initialize(task);
		setStart(Calendar.getInstance().getTime());
		setNum(task.getNum());
	}

	/**
	 * @return the num
	 */
	public int getNum() {
		return num;
	}

	/**
	 * @param num the num to set
	 */
	public void setNum(int num) {
		this.num = num;
	}

	/**
	 * @return the vcstatus
	 */
	public String getVcstatus() {
		return vcstatus;
	}

	/**
	 * @param vcstatus the vcstatus to set
	 */
	public void setVcstatus(String vcstatus) {
		this.vcstatus = vcstatus;
	}

	/**
	 * @return the vclogid
	 */
	public String getVclogid() {
		return vclogid;
	}

	/**
	 * @param vclogid the vclogid to set
	 */
	public void setVclogid(String vclogid) {
		this.vclogid = vclogid;
	}

}
