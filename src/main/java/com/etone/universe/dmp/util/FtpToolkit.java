package com.etone.universe.dmp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.daemon.support.Env;

public class FtpToolkit {
	private static final int BUFFER_SIZE = 16 * 1024;
	private final Logger log = LoggerFactory.getLogger(FtpToolkit.class);
	private FTPClient ftpClient;
	private String ip;
	private String user;
	private String pwd;

	public FtpToolkit(String ip, String user, String pwd) {
		this.ip = ip;
		this.user = user;
		this.pwd = pwd;
	}

	/**
	 * 连接FTP
	 * 
	 * @return
	 */
	public boolean connect() {
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(ip);
			if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				ftpClient.setControlEncoding("GBK");
				if (ftpClient.login(user, pwd)) {
					return true;
				}
			}
			disconnect();
			return false;
		} catch (Exception e) {
			log.error("", e);
			return false;
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
			log.error("", e);
		}
	}

	/**
	 * 上传文件
	 *
	 * @param name
	 * @param path
	 * @throws Exception
	 */
	public void upload(String filepath, String name, String path)
			throws Exception {
		this.upload(new FileInputStream(filepath), name, path);
	}

	/**
	 * 上传文件
	 * 
	 * @param file
	 * @param name
	 * @param path
	 * @throws Exception
	 */
	public void upload(File file, String name, String path) throws Exception {
		this.upload(new FileInputStream(file), name, path);
	}

	/**
	 * 上传文件
	 *
	 * @param name
	 * @param path
	 * @throws Exception
	 */
	public void upload(InputStream in, String name, String path)
			throws Exception {
		try {
			if (!this.connect()) {
				throw new Exception();
			}
			//			String ftppath = path;
			//            this.changeDirectory(ftppath);
			ftpClient.setBufferSize(BUFFER_SIZE); // 设置缓冲大小
			ftpClient.setControlEncoding("GBK"); // 设置编码
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 设置文件类型（二进制）
			System.out.println("5:" + name);
			ftpClient.enterLocalPassiveMode();
			ftpClient.storeFile(new String(name.getBytes("GBK"), "iso-8859-1"),
					in);
			//            ftpClient.storeFile(name, in);

			log.info("FTP executeUpload success");

			ftpClient.logout();
		} catch (Exception e) {
			log.error("", e);
			throw e;
		} finally {
			IOUtils.closeQuietly(in);
			disconnect();
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
			log.error("", e);
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
					if (ftpClient.makeDirectory(subDirectory)) {
						ftpClient.changeWorkingDirectory(subDirectory);
					} else {
						log.info("创建目录失败");
						return false;
					}
				}
				start = end + 1;
				end = directory.indexOf("/", start);

				// 检查所有目录是否创建完毕
				if (end <= start) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return true;
	}

	/**
	 * 下载文件
	 *
	 * @param remotePath
	 *            远程文件地址
	 */
	public String download(String remotePath) throws Exception {
		try {
			return this.download(remotePath, this.createFilePath(remotePath));
		} catch (IOException e) {
			log.error("", e);
			throw new RuntimeException("FTP客户端出错！", e);
		}
	}

	/**
	 * 下载文件
	 *
	 * @param remotePath
	 *            远程文件地址
	 * @param downpath
	 *            下载目录
	 * @return
	 * @throws Exception
	 */
	public String download(String remotePath, String downpath) throws Exception {
		FileOutputStream fos = null;
		try {
			if (!this.connect()) {
				throw new Exception();
			}
			int index = remotePath.lastIndexOf("/");
			String filename = (index > -1) ? remotePath.substring(index + 1)
					: remotePath;

			File tmpFile = new File(downpath);// 创建下载目录
			if (!tmpFile.exists()) {
				tmpFile.mkdirs();
			}
			if (!downpath.endsWith("/")) {
				downpath += File.separator;
			}

			String newFile = downpath + filename; // 下载文件路径
			fos = new FileOutputStream(newFile);
			ftpClient.setBufferSize(BUFFER_SIZE); // 设置缓冲大小
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 设置文件类型（二进制）
			System.out.println(newFile + "--" + remotePath + "--"
					+ new String(remotePath.getBytes("GBK"), "iso-8859-1"));
			ftpClient.enterLocalPassiveMode();
			ftpClient.retrieveFile(new String(remotePath.getBytes("GBK"),
					"iso-8859-1"), fos);
			return newFile;
		} catch (IOException e) {
			log.error("", e);
			throw new RuntimeException("FTP Client error！", e);
		} finally {
			IOUtils.closeQuietly(fos);
			this.disconnect();
		}
	}

	/**
	 * 创建存储路径
	 *
	 * @param remotePath
	 * @return
	 */
	private String createFilePath(String remotePath) {
		//String temppath = PropertiesUtil.getInstance().getProperty("tmppath");
		String temppath = Env.getProperties().getValue("tmppath");
		String relatePath = remotePath
				.substring(0, remotePath.lastIndexOf("/"));
		String path = temppath + relatePath;
		path += (path.endsWith("/") ? "" : "/") + new Date().getTime();
		File tmpFile = new File(path);
		if (!tmpFile.exists()) {
			tmpFile.mkdirs();
		}
		return path;
	}

}
