package com.etone.universe.dmp.problem;

import java.sql.Timestamp;

/**
 * 问题点聚类详细日志
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年12月12日  下午3:20:09
 */
public class DetailLog {

	/**
	 * 流程日志id
	 */
	private String vclogid = "";

	/**
	 * 日志信息
	 */
	private String vcinfo = "";

	/**
	 * 日志序号
	 */
	private int intnum = 0;

	/**
	 * 当前时间
	 */
	private Timestamp logtime = new Timestamp(System.currentTimeMillis());

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

	/**
	 * @return the vcinfo
	 */
	public String getVcinfo() {
		return vcinfo;
	}

	/**
	 * @param vcinfo the vcinfo to set
	 */
	public void setVcinfo(String vcinfo) {
		this.vcinfo = vcinfo;
	}

	/**
	 * @return the intnum
	 */
	public int getIntnum() {
		return intnum;
	}

	/**
	 * @param intnum the intnum to set
	 */
	public void setIntnum(int intnum) {
		this.intnum = intnum;
	}

	/**
	 * @return the logtime
	 */
	public Timestamp getLogtime() {
		return logtime;
	}

	/**
	 * @param logtime the logtime to set
	 */
	public void setLogtime(Timestamp logtime) {
		this.logtime = logtime;
	}

}
