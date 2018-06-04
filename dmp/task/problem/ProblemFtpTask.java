package com.etone.universe.dmp.task.problem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.etone.daemon.db.DB;
import com.etone.daemon.db.helper.QueryHelper;
import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.FtpEvent;
import com.etone.universe.dmp.util.ProblemFtpUtil;
import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年2月13日  上午10:14:10
 */
public class ProblemFtpTask extends ProblemTask {

	/**
	 * 文件匹配格式集合
	 */
	private Map<String, String> typeMap = null;

	/**
	 * 是否下载所有文件,若没有配置通配格式,则需要下载所有文件
	 */
	private boolean isAll = true;

	/**
	 * 文件名切割后第几个进行匹配
	 */
	private int num = 1;

	/**
	 * 文件名切割字符串
	 */
	private String fileSplit = "";

	/**
	 * 本地存放地址
	 */
	private String loadpath = "";

	/**
	 * 数据类型
	 */
	private String dataType = "";

	/**
	 * 是否遍历所有日期文件夹,否则遍历当前文件夹
	 */
	private String loadAll = "0";

	/**
	 * 文件后缀
	 */
	private String suffix = "";

	/**
	 * 文件前缀
	 */
	private String prefix = "";

	@Override
	public void execute() {

		FtpEvent event = new FtpEvent();
		event.setPriority(getPriority());
		event.setName(getName());
		event.setType("download");
		event.setStart(Calendar.getInstance().getTime());

		// do something
		logger.info("Start ftp task : {}", this.getName());

		try {

			// 初始化参数
			initParam();

			// 调用下载文件方法
			downloadFileFtp();

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Download exception : ", e);
			setException(e.getMessage());
			event.setEnd(Calendar.getInstance().getTime());
			event.setException(e.getMessage());
			EventService.getInstance().post(event);
		}

		// 调度指定job
		super.execute();
	}

	/**
	 * 下载文件
	 */
	public void downloadFileFtp() {

		// 创建problem ftp对象
		String ip = parpams.get("url");
		String user = parpams.get("user");
		String pwd = parpams.get("pwd");
		ProblemFtpUtil ftpUtil = new ProblemFtpUtil(ip, user, pwd);

		//连接ftp
		FTPClient ftpClient = ftpUtil.connect();

		try {

			// 设置文件类型（二进制）
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

			// 设置被动模式
			ftpClient.enterLocalPassiveMode();
			ftpClient.setRemoteVerificationEnabled(false);

			// 获取ftp当前目录所有文件以及文件名
			FTPFile[] ftpFiles = ftpClient.listFiles();
			System.out.println("进入被动模式，获取文件数量为：【"+ftpFiles.length+"】");

			if ("0".equals(loadAll)) {
				//遍历当前目录
				loadFiles(ftpUtil, ftpClient, ftpFiles);
			} else {
				// 遍历所有日期文件夹
				loadFilesAll(ftpUtil, ftpClient, ftpFiles);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("下载文件出错：", e);
		} finally {
			try {
				ftpClient.disconnect();
				logger.info("ftp关闭成功");
			} catch (IOException e) {
				logger.info("ftp关闭失败");
				e.printStackTrace();
			}
		}
		//断开与远程服务器的连接
		ftpUtil.disconnect();
	}

	/**
	 * 初始化必要参数
	 */
	public void initParam() {

		// 处理文件匹配格式
		String fileTypes = parpams.get("filetypes");
		if (fileTypes == null || "".equals(fileTypes)) {
			logger.info("请先配置下载文件格式【filetypes】...");
			return;
		}

		typeMap = new HashMap<String, String>();
		for (String type : fileTypes.split(ProblemUtil.FILE_S)) {
			typeMap.put(type, type);
		}

		if (typeMap.size() > 0) {
			isAll = false;
		}

		// 匹配第几个字符
		String typeNum = parpams.get("typenum");
		if (typeNum == null || "".equals(typeNum)) {
			logger.info("请先配置下载文件格式【typenum】...");
			return;
		}
		num = Integer.valueOf(typeNum);

		// 文件名切割字符
		fileSplit = parpams.get("filesplit");
		if (fileSplit == null || "".equals(fileSplit)) {
			logger.info("请先配置下载文件格式【filesplit】...");
			return;
		}

		// 本地目录
		loadpath = parpams.get("loadpath");
		if (loadpath == null || "".equals(loadpath)) {
			logger.info("请先配置本地存放目录【loadpath】...");
			return;
		}

		// 本地目录
		dataType = parpams.get("datatype");
		if (dataType == null || "".equals(dataType)) {
			logger.info("请先配置本地存放目录【datatype】...");
			return;
		}

		// 是否遍历所有日期文件夹,否则只遍历当前文件夹
		loadAll = parpams.get("loadall");
		if (loadAll == null || "".equals(loadAll)) {
			loadAll = "0";
		}

		// 文件后缀
		suffix = parpams.get("suffix");

		// 文件后缀
		prefix = parpams.get("prefix");
	}

	/**
	 * 遍历当前目录文件,下载未下载过的文件并记录日志
	 * @param ftpUtil
	 * @param ftpClient
	 * @param ftpFiles
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws SQLException 
	 */
	private void loadFiles(ProblemFtpUtil ftpUtil, FTPClient ftpClient,
			FTPFile[] ftpFiles) throws IOException, FileNotFoundException,
			SQLException {

		// 判断条件
		boolean flag = false;

		// 循环当前目录文件，并且把数据下载到本地服务器
		for (int i = 0; i < ftpFiles.length; i++) {

			// 目录不下载
			if (ftpFiles[i].isDirectory()) {
				continue;
			}

			// 文件名称
			String fileName = ftpFiles[i].getName();

			// 辨别文件名前后缀
			if (!checkFileName(fileName)) {
				continue;
			}

			// 文件名匹配符
			String fileType = fileName.split(fileSplit)[num].toLowerCase();

			//查询这个文件是否已经下载过
			String sql = sqlMap.get("queryFtpTransmissionFile");
			long flg = QueryHelper.selectOne(conn, long.class, sql, "%"
					+ fileName + "%");
			if (flg > 0) {
				continue;
			}

			// 迎合问题点入库文件格式,匹配上的才进行下载
			flag = isAll;
			if (!isAll) {
				flag = typeMap.get(fileType) != null;
			}
			if (flag) {
				java.sql.Timestamp fileTime = new java.sql.Timestamp(
						ftpFiles[i].getTimestamp().getTime().getTime());
				double fileSize = ftpFiles[i].getSize() > 0 ? ftpFiles[i]
						.getSize() : 0.00;
				double size = ftpFiles[i].getSize() * 1.00 / 1024 / 1024 / 1024;
				logger.info("开始下载文件：" + fileName + ",大小为：[" + size + "G]");

				// 文件太大(超过1G)，需要很长时间下载完,先释放掉链接
				if (size > 1) {
					if (conn != null && !conn.isClosed()) {
						conn.close();
					}
				}

				// 开始下载文件
				File localFile = new File(loadpath + fileName);
				OutputStream is = new FileOutputStream(localFile);
				ftpClient.retrieveFile(ftpFiles[i].getName(), is);
				is.close();
				//设置权限为644
				ProblemUtil
						.excuteLiuxOrde3r("chmod 644 " + loadpath + fileName);

				// 记录下载情况，文件太大(超过1G)，需要很长时间下载完,重新获取链接
				if (conn.isClosed()) {
					conn = DB.getDataSource(getDataSource()).getConnection();
				}
				execute(conn, sqlMap.get("saveFtpTransmissionLog"), fileTime,
						fileSize, fileName, dataType, loadpath);
			}
		}
	}

	/**
	 * 遍历目录下所有日期文件,下载未下载过的文件并记录日志
	 * @param ftpUtil
	 * @param ftpClient
	 * @param ftpFiles
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws SQLException 
	 */
	private void loadFilesAll(ProblemFtpUtil ftpUtil, FTPClient ftpClient,
			FTPFile[] ftpFiles) throws IOException, FileNotFoundException,
			SQLException {

		// 判断条件
		boolean flag = false;

		// 20170215若为MRO文件,由于文件太多太大,则只保留30天内的文件
		if ("MRO,MR".equals(dataType)) {
			processorMroFile();
		}

		for (int i = 0; i < ftpFiles.length; i++) {

			//判断是文件还是文件夹和是否是日期目录
			if (ftpFiles[i].isDirectory() && isNumber(ftpFiles[i].getName())) {

				String remotePath = ftpFiles[i].getName();

				// 进入目录
				ftpUtil.changeDirectory(remotePath + "/");

				//获取当前目录所有文件
				FTPFile[] ftpFiles1 = ftpClient.listFiles();

				//循环当前目录文件，并且把数据下载到本地服务器
				for (int j = 0; j < ftpFiles1.length; j++) {

					// 文件名称
					String fileName = ftpFiles1[j].getName();

					// 辨别文件名前后缀
					if (!checkFileName(fileName)) {
						continue;
					}

					// 文件名匹配符
					String fileType = fileName.split(fileSplit)[num]
							.toLowerCase();

					// 查询这个文件是否已经下载过
					String sql = sqlMap.get("queryFtpTransmissionFile");
					long flg = QueryHelper.selectOne(conn, long.class, sql, "%"
							+ fileName + "%");
					if (flg > 0) {
						continue;
					}
					try {
						// 迎合问题点入库文件格式,匹配上的才进行下载
						flag = isAll;
						if (!isAll) {
							flag = typeMap.get(fileType) != null;
						}
						if (flag) {

							java.sql.Timestamp fileTime = new java.sql.Timestamp(
									ftpFiles1[j].getTimestamp().getTime()
											.getTime());
							double fileSize = ftpFiles1[j].getSize() > 0 ? ftpFiles1[j]
									.getSize() : 0.00;
							double size = ftpFiles1[j].getSize() * 1.00 / 1024
									/ 1024 / 1024;
							logger.info("开始下载文件：" + fileName + ",大小为：[" + size
									+ "G]");

							// 文件太大(超过1G)，需要很长时间下载完,先释放掉链接
							if (size > 1) {
								if (conn != null && !conn.isClosed()) {
									conn.close();
								}
							}

							// 开始下载文件
							File localFile = new File(loadpath + fileName);
							OutputStream is = new FileOutputStream(localFile);
							ftpClient.retrieveFile(ftpFiles1[j].getName(), is);
							is.close();

							//设置权限为644###鉴于老是报空，先不执行赋予权限命令
							//ProblemUtil.excuteLiuxOrde3r("chmod 644 " + loadpath + fileName);

							// 记录下载情况，文件太大(超过1G)，需要很长时间下载完,重新获取链接
							if (conn.isClosed()) {
								conn = DB.getDataSource(getDataSource())
										.getConnection();
							}

							//记录下载情况
							execute(conn, sqlMap.get("saveFtpTransmissionLog"),
									fileTime, fileSize, fileName, dataType,
									loadpath);
						}
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("下载文件出错！", e);
					}
				}
				ftpClient.cdup();
			}
		}
	}

	/**
	 * 20170215若为MRO文件,由于文件太多太大,则只保留1个月的文件
	 * @throws SQLException 
	 */
	private void processorMroFile() throws SQLException {

		// 1.获取30天前的日期
		Calendar calendar = Calendar.getInstance();
		// 因为数据一般有延迟3天,因此保留33天内
		calendar.add(Calendar.DATE, -33);
		Date time2 = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

		// 2.查询30天前并且没有处理过的MRO文件记录
		List<Map<String, Object>> selectMap = selectMap(conn,
				sqlMap.get("queryHistory"), format.format(time2));

		// 3.删除MRO文件
		if (selectMap != null && selectMap.size() > 0) {

			String removeStr = "rm -rf " + loadpath;
			String updateSql = sqlMap.get("updateStatus");
			for (Map<String, Object> map : selectMap) {

				try {

					// 3.1.删除MRO文件
					ProblemUtil.excuteLiuxOrde3r(removeStr
							+ map.get("vcfilename").toString());

					// 3.2.回写日志状态
					execute(conn, updateSql, map.get("intid"));

				} catch (Exception e) {
					e.printStackTrace();
					addDetaileLog("移除MRO文件【" + map.get("vcfilename").toString()
							+ "】出错:" + e.getMessage());
				}
			}
		}

	}

	/**
	 * 检查文件名前后缀
	 * @param fileName 文件名
	 * @return true符合,false不符合
	 */
	public boolean checkFileName(String fileName) {

		// 前缀
		if (prefix != null && !"".equals(prefix)) {
			if (!fileName.startsWith(prefix)) {
				return false;
			}
		}
		// 后缀
		if (suffix != null && !"".equals(suffix)) {
			if (!fileName.endsWith(suffix)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 判断字符串是否是纯数字,识别日期目录
	 * @param str
	 * @return
	 */
	private boolean isNumber(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
