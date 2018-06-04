package com.etone.universe.dmp.problem;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

//import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Rock on 2017/10/12.
 */
public class Testsss {
//
//    @SuppressWarnings("unchecked")
//    public static void main(String[] args) {
//        String paramStr =
//                "<?xml version=\"1.0\"?>"+
//                        "<Item name=\"Param\" typeName=\"IDictionary\">"+
//                        "<Item name=\"Return\" typeName=\"Boolean\">True</Item>"+	//执行是否成功
//                        "<Item name=\"Info\" typeName=\"String\">XXX</Item>"+	//返回成功的描述，或者失败的原因
//                        "<Item name=\"Result\" typeName=\"IEnumerable\">"+ //返回的命令参数，包含以下参数
//                        "<Item typeName=\"IDictionary\">"+
//                        "<Item name=\"TokenInfo\" typeName=\"String\">"+
//                        "{\"TokenInfo\":{\"LoginName\":\"XXX\",\"TokenURL\":\"http://rrc.gmcc.net/mtnoh/page/mtnoh.html?uname=XXXXXXX&pwd=XXXXXXX&hidelogo=1&hideframe=0&viewmode=1\"}}</Item>"+  //令牌Json信息(已经包含URL前缀)
//                        "</Item>"+
//                        "</Item>"+
//                        "</Item>";
//
//
//        // XML字符串转doc对象
//        try {
//
//            String resultStr = executeMethod("http://rrc.gmcc.net/mtnoh/Service/Command.asmx?wsdl","Execute",paramStr);
//            // XML字符串转doc对象
//            org.dom4j.Document doc = DocumentHelper.parseText(resultStr);
//            List<Element> elements = doc.getRootElement().elements();
//            // 是否执行成功
//            if(!"True".equals(elements.get(0).getText())){
//                return;
//            }
//            List<Element> IDictionary = elements.get(2).elements();
//            List<Element> TokenInfo = IDictionary.get(0).elements();
//            String tokenJson = TokenInfo.get(0).getText();
//            System.out.println(tokenJson);
//
//            // json转对象
//            /*JSONObject jb = JSONObject.fromObject(tokenJson);
//            String tokenURL = jb.get("TokenURL").toString();*/
//
//            // 存放进会话信息里面
//            //System.out.println(tokenURL);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//
//
//    /**
//     * 调用wsdl接口
//     * @param url 接口描述地址
//     * @param method 调用的接口方法
//     * @param parameters 调用的参数
//     * @return
//     * @throws Exception
//     */
//    public static String executeMethod(String url, String method,
//                                       String parameters) throws Exception {
//
//        JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
//        if (!url.endsWith("wsdl")) {
//            url += "?wsdl";
//        }
//
//        org.apache.cxf.endpoint.Client client = dcf.createClient(url);
//        return client.invoke(method, parameters)[0].toString();
//    }
}
