package com.etone.universe.dmp.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 文件名过滤类
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年11月25日  上午11:36:06
 */
public class DirFilter implements FilenameFilter {

	private String type;

	public DirFilter(String tp) {
		this.type = tp;
	}

	public boolean accept(File fl, String path) {
		File file = new File(path);
		String filename = file.getName();
		return filename.indexOf(type) != -1;
	}
}
