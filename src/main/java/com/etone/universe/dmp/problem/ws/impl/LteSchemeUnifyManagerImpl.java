package com.etone.universe.dmp.problem.ws.impl;

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.universe.dmp.problem.ws.LteSchemeUnifyManager;

/**
 * 方案库统一呈现-地市工单转派号接口
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 17-3-21
 * Time: 下午3:53
 * To change this template use File | Settings | File Templates.
 */

@WebService
@SOAPBinding(style = Style.RPC)
public class LteSchemeUnifyManagerImpl implements LteSchemeUnifyManager {

	public static final Logger logger = LoggerFactory
			.getLogger(LteSchemeUnifyManagerImpl.class);

	@WebMethod
	public String Test(String xmlData) {
		// 测试方法,字符串原样放回
		return xmlData;
	}

	// 地市工单编号回填
	@WebMethod
	public String CityWorkNum(String xmlData) {

		// 基本参数
		Map<String, String> dataMap = new HashMap<String, String>();

		try {

			// 1.解析xml信息获取:地市工单号,省工单号,用户名,密码
			String messageStr = LteSchemeUnifyProcessor.parseXml(xmlData,
					dataMap);
			if (messageStr.length() > 0) {
				return "<info><status>001</status><message>" + messageStr
						+ "</message></info>";
			}

			// 2.判断用户名密码是否正确 (确定只有一个厂商调用,先简化配置，在代码上定死用户名密码)
			messageStr = LteSchemeUnifyProcessor.checkUser(dataMap);
			if (messageStr.length() > 0) {
				return messageStr;
			}

			// 3.先记录本次调用日志
			LteSchemeUnifyProcessor.saveLog(xmlData);

			// 4.根据省工单号更新数据表的地市工单号字段
			LteSchemeUnifyProcessor.updateCityCode(dataMap);

			// 5.更新成功返回状态000
			return "<info><status>000</status><message>地市转派号更新成功！</message></info>";

		} catch (Exception e) {

			e.printStackTrace();
			return "<info><status>003</status><message>地市转派号更新失败！</message></info>";

		}
	}

}
