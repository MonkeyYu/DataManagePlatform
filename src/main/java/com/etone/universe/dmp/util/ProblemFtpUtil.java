package com.etone.universe.dmp.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.daemon.support.Env;
import com.etone.universe.dmp.task.problem.ProblemTask;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 16-10-10
 * Time: 下午5:18
 * To change this template use File | Settings | File Templates.
 */
public class ProblemFtpUtil {

	public static final Logger log = LoggerFactory.getLogger(ProblemTask.class);
	private FTPClient ftpClient;
	private String ip;
	private String user;
	private String pwd;
	public static String dir;

	public ProblemFtpUtil() {
		this.ip = Env.getProperties().getValue("ftp.server.ip");
		this.user = Env.getProperties().getValue("ftp.server.user");
		this.pwd = Env.getProperties().getValue("ftp.server.passwd");
	}

	public ProblemFtpUtil(String ip, String user, String pwd) {
		this.ip = ip;
		this.user = user;
		this.pwd = pwd;
	}

	/**
	 * 连接FTP
	 *
	 * @return
	 */
	public FTPClient connect() {
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(ip);
			if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				ftpClient.setControlEncoding("GBK");
				if (ftpClient.login(user, pwd)) {
					//连上
					System.out.println("连接上FTP！");
					return ftpClient;
				}
			}
			disconnect();
			//没连上
			return null;
		} catch (Exception e) {
			log.error("", this, e);
			//没连上
			return null;
		}
	}

	/**
	 * 断开与远程服务器的连接
	 */
	public void disconnect() {
		try {
			if (ftpClient.isConnected()) {
				ftpClient.disconnect();
			}
		} catch (Exception e) {
			log.error("", this, e);
		}
	}

	/**
	 * 是否空文件
	 *
	 * @return
	 */
	public boolean isNotEmptyFile(String remotefile) {
		try {
			FTPFile[] ftpFiles = ftpClient.listFiles(remotefile);
			if (ftpFiles.length != 1 || ftpFiles[0].getSize() == 0) {
				return false;
			}
			return true;
		} catch (Exception e) {
			log.error("", this, e);
			return false;
		}
	}

	/**
	 * 切换多级目录，如果目录不存在则创建
	 *
	 * @param directory
	 * @return
	 */
	public boolean changeDirectory(String directory) {
		try {
			int start = directory.startsWith("/") ? 1 : 0;
			int end = directory.indexOf("/", start);
			while (true) {
				String subDirectory = directory.substring(start, end);
				if (!ftpClient.changeWorkingDirectory(subDirectory)) {
					log.info("创建目录:" + subDirectory + "失败");
					return false;
				}
				start = end + 1;
				end = directory.indexOf("/", start);

				// 检查所有目录是否创建完毕
				if (end <= start) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("", this, e);
		}
		return true;
	}
}
