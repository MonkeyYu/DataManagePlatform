package com.etone.universe.dmp.task.problem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.etone.universe.dmp.event.EventService;
import com.etone.universe.dmp.event.FtpEvent;
import com.etone.universe.dmp.util.ProblemFtpUtil;
import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 告警数据下载，需要特殊定制
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年2月13日  上午10:14:10
 */
public class ProblemFtpGjTask extends ProblemTask {

	/**
	 * 文件匹配格式集合
	 */
	private Map<String, String> typeMap = null;

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
	 * ftp切换目录
	 */
	private String remotePath = "";

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

			//如果切换目录成功
			if (ftpUtil.changeDirectory(remotePath)) {

				//获取  remotePath 目录下所有文件夹
				FTPFile[] fs2 = ftpClient.listFiles();

				// 遍历目录文件,下载未下载过的文件并记录日志
				loadFiles(ftpUtil, ftpClient, fs2);

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
	 * 遍历目录文件,下载未下载过的文件并记录日志
	 * @param ftpUtil
	 * @param ftpClient
	 * @param fs2
	 * @throws IOException
	 * @throws SQLException
	 * @throws FileNotFoundException
	 */
	private void loadFiles(ProblemFtpUtil ftpUtil, FTPClient ftpClient,
			FTPFile[] fs2) throws IOException, SQLException,
			FileNotFoundException {

		// 一次性查出所有的告警下载记录，进行匹配
		List<Map<String, Object>> gjMap = selectMap(conn,
				sqlMap.get("queryAllGaojingData"));
		Map<String, String> gjallMap = new HashMap<String, String>();
		for (Map<String, Object> map : gjMap) {
			String filename = map.get("vcfilename").toString();
			gjallMap.put(filename, filename);
		}

		for (int i = 0; i < fs2.length; i++) {
			//判断是文件夹还是文件，返回完整路径路径、例如 /FMS_ALARMDATA/GZ/20161011/czxxx.csv.gz
			String remotePath1 = "/" + fs2[i].getName(); ///FMS_ALARMDATA/GZ
			//进入下一层目录FMS_ALARMDATA/GZ
			ftpUtil.changeDirectory(remotePath1 + "/");
			FTPFile[] fsCity = ftpClient.listFiles();///FMS_ALARMDATA/GZ
			for (int j = 0; j < fsCity.length; j++) {
				String remotePath2 = "/" + fsCity[j].getName() + "/"; ///FMS_ALARMDATA/GZ/20160912
				ftpUtil.changeDirectory(remotePath2);
				FTPFile[] fsFile = ftpClient.listFiles();
				for (int r = 0; r < fsFile.length; r++) {
					//适配程序修改名称
					String fileName = new StringBuffer(fsFile[r].getName())
							.replace(13, 14, "-").toString().toLowerCase();
					java.sql.Timestamp fileTime = new java.sql.Timestamp(
							fsFile[r].getTimestamp().getTime().getTime());
					double fileSize = fsFile[r].getSize() > 0 ? fsFile[r]
							.getSize() : 0.00;
					//查询这个文件是否已经下载
					/*String sql = sqlMap.get("queryFtpTransmissionFile");
					long flg = QueryHelper.selectOne(conn, long.class, sql, "%" + fileName
							+ "%");*/
					if (gjallMap.get(fileName.toLowerCase()) == null
							&& fileName.toLowerCase().endsWith(".gz")) { //如果没有下载过则下载
						File localFile = new File(loadpath + fileName);
						OutputStream is = new FileOutputStream(localFile);
						ftpClient.retrieveFile(fsFile[r].getName(), is);
						System.out.println("开始下载文件：" + fileName + ",大小为："
								+ fsFile[r].getSize() * 1.00 / 1024 / 1024
								/ 1024 + "G");
						is.close();
						String csvName = fileName.replace(".gz", "");
						//下载完解压成.csv文件
						ProblemUtil.excuteLiuxOrde3r("gzip -d " + loadpath
								+ fileName, this);
						//删除第一行表头
						String[] cmds = { "/bin/sh", "-c",
								"sed -i '1d' " + loadpath + csvName };
						ProblemUtil.excuteLiuxOrde3r(cmds, this);
						//替换 为
						String[] cmdsk = { "/bin/sh", "-c",
								"sed -i 's/ //g' " + loadpath + csvName };
						ProblemUtil.excuteLiuxOrde3r(cmdsk, this);
						//替换# 为空
						String[] cmdst = { "/bin/sh", "-c",
								"sed -i 's/#//g' " + loadpath + csvName };
						ProblemUtil.excuteLiuxOrde3r(cmdst, this);
						//替换? 为空
						String[] cmdst7 = { "/bin/sh", "-c",
								"sed -i 's/?//g' " + loadpath + csvName };
						ProblemUtil.excuteLiuxOrde3r(cmdst7, this);
						//替换文件中的|为@
						String[] cmds1 = { "/bin/sh", "-c",
								"sed -i 's/|/@/g' " + loadpath + csvName };
						ProblemUtil.excuteLiuxOrde3r(cmds1, this);
						//替换文件中的'为"
						String[] cmds8 = { "/bin/sh", "-c",
								"sed -i \"s/'//g\" " + loadpath + csvName };
						ProblemUtil.excuteLiuxOrde3r(cmds8, this);
						//替换|^% 为 |
						String[] cmds2 = { "/bin/sh", "-c",
								"sed -i 's/@^%/|/g' " + loadpath + csvName };
						ProblemUtil.excuteLiuxOrde3r(cmds2, this);

						//记录下载情况
						execute(conn, sqlMap.get("saveFtpTransmissionLog"),
								fileTime, fileSize, fileName, dataType,
								loadpath);
					}
				}
				ftpClient.cdup();
			}
			ftpClient.cdup();
		}
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

		// 文件类型
		dataType = parpams.get("datatype");
		if (dataType == null || "".equals(dataType)) {
			logger.info("请先配置文件类型【datatype】...");
			return;
		}

		// ftp切换目录
		remotePath = parpams.get("remotepath");
		if (remotePath == null || "".equals(remotePath)) {
			logger.info("请先配置ftp切换目录【remotepath】...");
			return;
		}
	}

}
