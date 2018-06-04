package com.etone.universe.dmp.problem.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

/**
 * 方案库统一呈现-地市工单转派号接口
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 17-3-21
 * Time: 下午4:03
 * To change this template use File | Settings | File Templates.
 */
@WebService
@SOAPBinding(style = Style.RPC)
public interface LteSchemeUnifyManager {

	@WebMethod
	public String Test(String xmlData);

	@WebMethod
	public String CityWorkNum(String xmlData);

}
