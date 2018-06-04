package com.etone.universe.dmp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 拷贝文件工具类
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年11月25日  下午12:41:05
 */
public class CopyFileUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(CopyFileUtil.class);

	private static String MESSAGE = "";

	/** 
	 * 复制单个文件 
	 *  
	 * @param srcFile 
	 *            待复制的文件名
	 * @param descFileName 
	 *            目标文件名 
	 * @param overlay 
	 *            如果目标文件存在，是否覆盖 
	 * @return 如果复制成功返回true，否则返回false 
	 */
	public static boolean copyFile(File srcFile, String destFileName,
			boolean overlay) {

		if (!srcFile.isFile()) {
			MESSAGE = "复制文件失败，源文件：" + srcFile.getName() + "不是一个文件！";
			logger.error(MESSAGE);
			return false;
		}

		// 判断目标文件是否存在
		File destFile = new File(destFileName);
		if (destFile.exists()) {
			// 如果目标文件存在并允许覆盖
			if (overlay) {
				// 删除已经存在的目标文件，无论目标文件是目录还是单个文件
				new File(destFileName).delete();
			}
		} else {
			// 如果目标文件所在目录不存在，则创建目录
			if (!destFile.getParentFile().exists()) {
				// 目标文件所在目录不存在
				if (!destFile.getParentFile().mkdirs()) {
					// 复制文件失败：创建目标文件所在目录失败
					return false;
				}
			}
		}

		// 使用文件通道的方式复制文件
		return fileChannelCopy(srcFile, destFile);

	}

	/** 
	 * 复制整个目录的内容 
	 *  
	 * @param srcDirName 
	 *            待复制目录的目录名 
	 * @param destDirName 
	 *            目标目录名 
	 * @param overlay 
	 *            如果目标目录存在，是否覆盖 
	 * @return 如果复制成功返回true，否则返回false 
	 */
	public static boolean copyDirectory(String srcDirName, String destDirName,
			boolean overlay) {
		// 判断源目录是否存在
		File srcDir = new File(srcDirName);
		if (!srcDir.exists()) {
			MESSAGE = "复制目录失败：源目录" + srcDirName + "不存在！";
			logger.error(MESSAGE);
			return false;
		} else if (!srcDir.isDirectory()) {
			MESSAGE = "复制目录失败：" + srcDirName + "不是目录！";
			logger.error(MESSAGE);
			return false;
		}

		// 如果目标目录名不是以文件分隔符结尾，则加上文件分隔符
		if (!destDirName.endsWith(File.separator)) {
			destDirName = destDirName + File.separator;
		}
		File destDir = new File(destDirName);
		// 如果目标文件夹存在
		if (destDir.exists()) {
			// 如果允许覆盖则删除已存在的目标目录
			if (overlay) {
				new File(destDirName).delete();
			} else {
				MESSAGE = "复制目录失败：目的目录" + destDirName + "已存在！";
				logger.error(MESSAGE);
				return false;
			}
		} else {
			// 创建目的目录
			logger.info("目的目录不存在，准备创建。。。");
			if (!destDir.mkdirs()) {
				logger.info("复制目录失败：创建目的目录失败！");
				return false;
			}
		}

		boolean flag = true;
		File[] files = srcDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 复制文件
			if (files[i].isFile()) {
				flag = CopyFileUtil.copyFile(files[i],
						destDirName + files[i].getName(), overlay);
				if (!flag)
					break;
			} else if (files[i].isDirectory()) {
				flag = CopyFileUtil.copyDirectory(files[i].getAbsolutePath(),
						destDirName + files[i].getName(), overlay);
				if (!flag)
					break;
			}
		}
		if (!flag) {
			MESSAGE = "复制目录" + srcDirName + "至" + destDirName + "失败！";
			logger.error(MESSAGE);
			return false;
		} else {
			return true;
		}
	}

	public static boolean fileChannelCopy(File s, File t) {

		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;

		try {

			fi = new FileInputStream(s);
			fo = new FileOutputStream(t);
			in = fi.getChannel();// 得到对应的文件通道
			out = fo.getChannel();// 得到对应的文件通道
			in.transferTo(0, in.size(), out);// 连接两个通道，并且从in通道读取，然后写入out通道
			logger.info("file:{} copy to {}",s.getAbsolutePath(),t.getAbsolutePath());
			
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {

				fi.close();
				in.close();
				fo.close();
				out.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		String srcDirName = "F:/test/test1/";
		String destDirName = "F:/test/test2";
		CopyFileUtil.copyDirectory(srcDirName, destDirName, true);
	}

}
