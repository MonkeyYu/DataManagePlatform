package com.etone.universe.daemon;

/*
 * Copyright 2010 Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */

import com.etone.daemon.DaemonStartup;
import com.etone.daemon.util.Mixeds;

/**
 * 使用示例项目<br/>
 * 建议所有程序，非特殊情况，都以该com.etone.universe.daemon.Startup作为程序入口<br/>
 * 以统一bin目录中的启动脚本，达到复用的目的
 *
 * @author <a href="mailto:maxid@qq.com">ZengHan</a>
 * @since $$Id$$
 */
public class Startup {

	/**
	 * 经典程序入口
	 *
	 * @param args
	 *            参数表
	 */
	public static void main(String[] args) {

        args=new String[]{"DemoServer","gpexport_local.xml"};

		// 启动插件系统
		if (args == null || args.length != 2) {
			System.out.println("Usage : run.sh CrontabServer crontab-demo.xml");
			System.exit(0);
		}

		if (args.length >= 2 && !Mixeds.isNullOrEmpty(args[1])) {
			System.setProperty("xml", args[1]);
		}

		DaemonStartup.getInstance(args).startup();
		// 初始化子系统环境变量

	}


}
