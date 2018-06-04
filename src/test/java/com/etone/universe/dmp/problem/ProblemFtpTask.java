package com.etone.universe.dmp.problem;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.etone.daemon.util.XFtps;
import com.etone.daemon.util.XFtps.FtpResult;
import com.etone.universe.dmp.task.Variables;
import com.etone.universe.dmp.util.ProblemFtpUtil;

/**
 * 
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年3月27日  下午2:28:28
 */
public class ProblemFtpTask {
	
	
	public static void main3(String[] args) {
		StringBuilder sql = new StringBuilder();
		sql.append("update ").append("xxx").append(" set ");
		String[] fields = "test1,test2".split(",");
		for (String field : fields) {
			sql.append(field).append("=null,");
		}
		
		String sqlStr = sql.substring(0, sql.length()-1);
		System.out.println(sqlStr);
	}
	
	public static void main(String[] args) {
		String plusDay = Variables.plusDay("select  test_znytb20170405('${hiveconf:yyyyMMdd-63}','${hiveconf:yyyyMMdd-3}');", new Date());
		System.out.println(plusDay);
	}
	
	public static void main1(String[] args) throws IOException {
		 XFtps ftps = XFtps.getInstance("127.0.0.1", 21, "Administrator", "111111");
         if (ftps.connect()) {

             List<XFtps.FtpResult> result = ftps.download("", "F:/test/", "", "xx.txt");

             for (int i = 0; i < result.size(); i++) {
            	 FtpResult x = result.get(0);
				System.out.println(x.getPath());
             }
         }
	}
	
	public static void main2(String[] args) throws IOException {
		
		
		ProblemFtpUtil ftpUtil = new ProblemFtpUtil("127.0.0.1", "Administrator", "111111");
		
		FTPClient ftpClient = ftpUtil.connect();
		
		// 设置被动模式
		ftpClient.enterLocalPassiveMode();
		String remotePath = "测试目录";
		FTPFile fs1= null;
		for(FTPFile fs : ftpClient.listFiles()){
			//System.out.println(fs.getName());
			if(remotePath.equals(fs.getName())){
				fs1 = fs;
				remotePath = fs.getName();
				break;
			}
		}
		String encoding1 = System.getProperty("file.encoding");
		System.out.println(encoding1);
		ftpClient.setControlEncoding(encoding1);
		String pathname = new String("测试目录".getBytes("GBK"),"iso-8859-1");
		FTPFile[] listFiles = ftpClient.listFiles(pathname);
		for(FTPFile fs : listFiles){
			System.out.println("#########"+fs.getName());
		}
		
		String controlEncoding = ftpClient.getControlEncoding();
		System.out.println(controlEncoding);
		String strNew = new String("测试目录".getBytes(),"iso-8859-1");
		
		boolean changeWorkingDirectory = ftpClient.changeWorkingDirectory(pathname);
		System.out.println(changeWorkingDirectory);
		//如果切换目录成功
		if (ftpUtil.changeDirectory("/"+pathname+"/")) {
			System.out.println("进入。。。");
		}
		
		
		
		
	}

}
