package com.etone.universe.dmp.problem.ws.impl;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.etone.daemon.db.DB;
import com.etone.daemon.db.helper.QueryHelper;
import com.etone.daemon.support.Env;

/**
 * webservice处理类
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年3月23日  上午10:41:34
 */
public class LteSchemeUnifyProcessor {

	public static final Logger logger = LoggerFactory
			.getLogger(LteSchemeUnifyManagerImpl.class);

	/**
	 * 根据省工单号更新数据表的地市工单号字段
	 * @param gpConn
	 * @param dataMap
	 * @throws SQLException
	 */
	public static void updateCityCode(Map<String, String> dataMap)
			throws SQLException {

		Connection gpConn = null;
		try {
			String datasource = Env.getProperties().getValue(
					"scheme.ws.datasource.gp");
			gpConn = DB.getDataSource(datasource).getConnection();
			QueryHelper
					.execute(
							gpConn,
							" update lte_cluster_question set city_order_code=? where order_code=?",
							dataMap.get("ccode"), dataMap.get("pcode"));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("更新地市转派号失败：", e);
			throw new SQLException("更新地市转派号失败！");
		} finally {
			if (gpConn != null) {
				gpConn.close();
			}
		}
	}

	/**
	 * 记录日志信息
	 * @param xmlData
	 * @param gpConn
	 * @return
	 * @throws SQLException
	 */
	public static void saveLog(String xmlData) throws SQLException {

		Connection mysqlConn = null;
		try {
			String datasource = Env.getProperties().getValue(
					"scheme.ws.datasource.mysql");
			mysqlConn = DB.getDataSource(datasource).getConnection();
			QueryHelper
					.execute(
							mysqlConn,
							" insert into t_scheme_ws_log(vcwsname,vcuser,vcinfo) values(?,?,?)",
							"地市工单编号回填(LteSchemeUnifyManagerImpl.CityWorkNum)",
							"明通", xmlData);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("插入日志操作失败：", e);
			throw new SQLException("插入日志操作失败！");
		} finally {
			if (mysqlConn != null) {
				mysqlConn.close();
			}
		}
	}

	/**
	 * 检查用户信息
	 * @param dataMap
	 * @return
	 */
	public static String checkUser(Map<String, String> dataMap) {
		String user = Env.getProperties().getValue("scheme.ws.user");
		String pwd = Env.getProperties().getValue("scheme.ws.pwd");
		if (user == null || pwd == null) {
			logger.error("请先配置webservice用户名密码");
			return "<info><status>003</status><message>地市转派号更新失败！</message></info>";
		}

		if (!user.equals(dataMap.get("user"))
				|| !pwd.equals(dataMap.get("pwd"))) {
			logger.error("用户名或密码不正确！");
			return "<info><status>002</status><message>用户名或密码不正确！</message></info>";
		}
		return "";
	}

	// 解析xml,获取对应字段数据
	public static String parseXml(String protocolXML,
			Map<String, String> dataMap) {

		try {
			// XML字符串转doc对象
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(
					protocolXML)));
			Element root = doc.getDocumentElement();

			// 要处理的字段
			String[] fields = { "ccode", "pcode", "user", "pwd" };
			for (String fieldName : fields) {
				String messageStr = getField(dataMap, root, fieldName);
				if (messageStr.length() > 0) {
					return messageStr;
				}
			}
			return "";
		} catch (Exception e) {
			e.printStackTrace();
			return "参数格式不符合规范！";
		}
	}

	// 根据字段名获取信息
	private static String getField(Map<String, String> dataMap, Element root,
			String fieldName) {
		org.w3c.dom.NodeList pnode = root.getElementsByTagName(fieldName);
		if (pnode.getLength() < 1) {
			return "未找到到" + fieldName + "字段！";
		}
		dataMap.put(fieldName, pnode.item(0).getTextContent());
		return "";
	}

}
