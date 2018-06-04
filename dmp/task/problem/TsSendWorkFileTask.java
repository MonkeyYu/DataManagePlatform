package com.etone.universe.dmp.task.problem;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.etone.universe.dmp.problem.QueryCriteria;
import com.etone.universe.dmp.problem.TousuProcessor;
import org.jdom.Document;
import org.jdom.Element;

import com.etone.universe.dmp.problem.GaosuProcessor;
import com.etone.universe.dmp.util.Common;
import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 集中投诉工单文件派单
 * @author <a href="mailto:azpao@qq.com">xiejialin</a>
 * @version $Revision: 14169 $
 * @date 2017年9月5日  上午10:10:46
 */
public class TsSendWorkFileTask extends ProblemTask {

	// 日志步骤号,用于线程里面,防止日志错乱
	private int logNum = 200;

	@Override
	public void execute() {

		// 初始化日志信息
		event.initialize(this);

		// 设置日志信息
		addDetaileLog("本次投诉数据处理环节开始...");

		// 设置参数信息
		QueryCriteria criteria = new QueryCriteria();

		ExecutorService executor = Executors.newFixedThreadPool(1);

		//创建投诉数据处理对象
		TousuProcessor tousu = new TousuProcessor(logNum,conn,sqlMap,this,criteria);
		executor.execute(tousu);

	}
}
