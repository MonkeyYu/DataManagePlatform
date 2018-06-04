package com.etone.universe.dmp.task.problem;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import com.etone.universe.dmp.problem.GaosuProcessor;
import com.etone.universe.dmp.util.Common;
import com.etone.universe.dmp.util.ProblemUtil;

/**
 * 高速工单文件派单
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2016年12月5日  上午10:10:46
 */
public class GsSendWorkFileTask extends ProblemTask {

	@Override
	public void execute() {

		// 初始化日志信息
		event.initialize(this);

		// 设置日志信息
		addDetaileLog("本次高速工单派单环节开始...");

		try {

			// 1.先查询出是否有高速工单;
			List<Map<String, Object>> gsList = selectMap(conn,
					sqlMap.get("queryGaosuWorkJob"));

			// 2.若无,直接退出; 
			if (gsList != null && gsList.size() > 0) {

				// 若有,则要核查汇聚表数据
				for (Map<String, Object> next : gsList) {
					String tableName = "";
					File extFile = null;
					try {

						// 获取工单编号
						String gaosuOrderCode = next.get("order_code")
								.toString();
						// 获取工单文件路径
						String[] files = next.get("file_path").toString()
								.split(",");
						String workName = null;
						for (String fileStr : files) {
							if (fileStr.endsWith(".xlsx")) {
								workName = fileStr;
								break;
							}
						}
						String path = getFilePath() + "/" + workName;
						// 创建唯一的表名
						tableName = ProblemUtil.EXT_EDMP_TABLENAME
								+ System.currentTimeMillis();

						addDetaileLog("高速工单【" + gaosuOrderCode + "】开始派发...");

						// 创建高速工单处理类对象
						GaosuProcessor gaosu = new GaosuProcessor(conn, sqlMap,
								this);

						// 3.若有,则要先核查汇聚表数据是否完整;
						extFile = gaosu.checkGaosuData(path, gaosuOrderCode,
								tableName);

						// 4.核查通过,将汇聚表数据插入到跟踪表;
						gaosu.insertGaosuClusterData(tableName, gaosuOrderCode);

						// 5.调用21个地市的方案库接口,修改工单附件状态;
						Map<String, Object> setAttachParams = setAttachParams(gaosuOrderCode);

						boolean flag = collectAttachment(setAttachParams, false);

						// 6.修改高速工单状态;
						if (flag) {

							// 获取统计数据
							List<Map<String, Object>> selectCount = selectMap(
									conn, sqlMap.get("queryCount"),
									gaosuOrderCode);
							if (selectCount != null && selectCount.size() > 0) {
								Map<String, Object> next2 = selectCount
										.iterator().next();

								// 更新工单状态信息
								execute(conn, sqlMap.get("updateGaosuWork"),
										next2.get("cluster_count"),
										next2.get("question_count"),
										gaosuOrderCode);
							}
						}

						addDetaileLog("高速工单【" + gaosuOrderCode + "】派发结束...");
					} catch (Exception e) {
						logger.error("核查高速跟踪表数据出错：", e);
					} finally {
						// 删除外部表及外部表文件
						if (tableName != null) {
							String dropTableSql = sqlMap.get("deleteExtTable")
									+ " " + tableName;
							execute(conn, dropTableSql);
						}
						if (extFile != null) {
							extFile.delete();
						}
					}

				}

			}

		} catch (Exception e) {

			e.printStackTrace();
			addDetaileLog("高速工单派单出错：" + e.getMessage());
			event.setException(e.getMessage());
			event.setVcstatus(ProblemUtil.STATUS_ER);
			this.setException(e.getMessage());
		}

		// 设置日志信息
		addDetaileLog("本次高速工单派单环节结束...");

		// 派单完成
		super.execute();
	}

	/**
	 * 调用1+N派单接口
	 * @param query 参数对象
	 * @param isThirdFlow 是否更新工单状态,false更新,true不更新
	 * @return
	 */
	public boolean collectAttachment(Map<String, Object> query,
			boolean isThirdFlow) {
		boolean flag = true;
		String xmlString = "";
		String resultXml = "";
		String[] resultInfo = null;
		String eoms_ordernum = "";
		try {

			// 组织调用接口xml信息
			Document doc = createXml(query, eoms_ordernum);

			String path = getParpams().get("resultxmlpath");
			xmlString = Common.buildXmlFile(doc, path);
			logger.info("xmlString:" + xmlString);

			resultXml = ProblemUtil.executeMethod(
					getParpams().get("commandwsdl"), "Execute", xmlString);
			logger.info("调用接口返回信息:" + resultXml);
			addDetaileLog("resultXml:" + resultXml);
			resultInfo = Common.convertStringToXml(resultXml);
			if (resultInfo == null || resultInfo.length < 3
					|| "False".equals(resultInfo[0])) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
			logger.error("高速派单出错：", e);
			addDetaileLog("高速派单出错：" + e.getMessage());
		}
		return flag;
	}

	/**
	 * 组织调用接口xml信息
	 * @param query
	 * @param eoms_ordernum
	 * @return
	 */
	private Document createXml(Map<String, Object> query, String eoms_ordernum) {
		Element root = new Element("Item");
		root.setAttribute("name", "Param");
		root.setAttribute("typeName", "IDictionary");

		Document doc = new Document(root);

		Element elements = new Element("Item");
		elements.setAttribute("name", "UserName");
		elements.setAttribute("typeName", "String");
		root.addContent(elements.setText(null == query.get("user_name")
				|| (String.valueOf(query.get("user_name")) == "null") ? ""
				: String.valueOf(query.get("user_name"))));

		Element elements2 = new Element("Item");
		elements2.setAttribute("name", "Password");
		elements2.setAttribute("typeName", "String");
		root.addContent(elements2.setText(null == query.get("password")
				|| (String.valueOf(query.get("password")) == "null") ? ""
				: String.valueOf(query.get("password"))));

		Element elements3 = new Element("Item");
		elements3.setAttribute("name", "CommandType");
		elements3.setAttribute("typeName", "Int32");
		root.addContent(elements3.setText(null == query.get("command_type")
				|| (String.valueOf(query.get("command_type")) == "null") ? ""
				: String.valueOf(query.get("command_type"))));

		Element elements4 = new Element("Item");
		elements4.setAttribute("name", "CommandSubType");
		elements4.setAttribute("typeName", "Int32");
		root.addContent(elements4.setText(null == query.get("command_sub_type")
				|| (String.valueOf(query.get("command_sub_type")) == "null") ? ""
				: String.valueOf(query.get("command_sub_type"))));

		Element elements5 = new Element("Item");
		elements5.setAttribute("name", "CommandParam");
		elements5.setAttribute("typeName", "IEnumerable");
		root.addContent(elements5);
		//            EOMS的工单编号
		if (null == query.get("eoms_ordernum")
				|| (String.valueOf(query.get("eoms_ordernum")) == "null")) {
		} else {
			eoms_ordernum = String.valueOf(query.get("eoms_ordernum"));
		}

		Element elements6 = new Element("Item");
		elements6.setAttribute("typeName", "String");
		elements5.addContent(elements6.setText(eoms_ordernum));

		//            工单字段信息
		Element elements11 = new Element("Item");
		elements11.setAttribute("typeName", "IDictionary");
		elements5.addContent(elements11);

		//            分析定位说明内容
		Element elements133 = new Element("Item");
		elements133.setAttribute("name", "Analy_result");
		elements133.setAttribute("typeName", "String");
		elements11.addContent(elements133.setText(null == query
				.get("analy_result")
				|| (String.valueOf(query.get("analy_result")) == "null") ? ""
				: String.valueOf(query.get("analy_result"))));

		//            附件列表
		Element elements134 = new Element("Item");
		elements134.setAttribute("name", "Flie_list");
		elements134.setAttribute("typeName", "String");
		elements11.addContent(elements134.setText(null == query
				.get("file_name")
				|| (String.valueOf(query.get("file_name")) == "null") ? ""
				: String.valueOf(query.get("file_name"))));

		//            操作人地市
		Element elements135 = new Element("Item");
		elements135.setAttribute("name", "Operator_city");
		elements135.setAttribute("typeName", "String");
		elements11.addContent(elements135.setText(null == query
				.get("operator_city")
				|| (String.valueOf(query.get("operator_city")) == "null") ? ""
				: String.valueOf(query.get("operator_city"))));

		//            操作人部门
		Element elements136 = new Element("Item");
		elements136.setAttribute("name", "Operator_Department");
		elements136.setAttribute("typeName", "String");
		elements11
				.addContent(elements136.setText(null == query
						.get("operator_department")
						|| (String.valueOf(query.get("operator_department")) == "null") ? ""
						: String.valueOf(query.get("operator_department"))));

		//            操作人科室
		Element elements137 = new Element("Item");
		elements137.setAttribute("name", "Operator_Offices");
		elements137.setAttribute("typeName", "String");
		elements11
				.addContent(elements137.setText(null == query
						.get("operator_offices")
						|| (String.valueOf(query.get("operator_offices")) == "null") ? ""
						: String.valueOf(query.get("operator_offices"))));

		//            操作人姓名
		Element elements138 = new Element("Item");
		elements138.setAttribute("name", "Operator_Username");
		elements138.setAttribute("typeName", "String");
		elements11.addContent(elements138.setText(null == query.get("operator")
				|| (String.valueOf(query.get("operator")) == "null") ? ""
				: String.valueOf(query.get("operator"))));

		//            操作人联系电话
		Element elements139 = new Element("Item");
		elements139.setAttribute("name", "Operator_Tel");
		elements139.setAttribute("typeName", "String");
		elements11.addContent(elements139.setText(null == query
				.get("telephone")
				|| (String.valueOf(query.get("telephone")) == "null") ? ""
				: String.valueOf(query.get("telephone"))));
		return doc;
	}

	/**
	 * 组织调用接口参数
	 * @param gaosuOrderCode
	 * @return
	 */
	public Map<String, Object> setAttachParams(String gaosuOrderCode) {

		Map<String, Object> query = new HashMap<String, Object>();
		query.put("user_name", "jk_LTE");
		query.put("password", "jk_LTE");
		query.put("command_type", "8");
		query.put("command_sub_type", "5");
		query.put("eoms_ordernum", gaosuOrderCode);
		query.put("analy_result", "");
		query.put("file_name", "");
		query.put("operator_city", "省公司");
		query.put("operator_department", "");
		query.put("operator_offices", "");
		query.put("operator", "方案库");
		query.put("telephone", "");

		return query;
	}

}
