package com.etone.universe.dmp.task.problem;

import java.util.Calendar;

import javax.xml.ws.Endpoint;

import com.etone.universe.dmp.event.EventService;

/**
 * 方案库转派地市工单编号回填webservice
 * @author <a href="mailto:azpao@qq.com">jiangwei</a>
 * @version $Revision: 14169 $
 * @date 2017年2月13日  上午10:14:10
 */
public class ProblemWsTask extends ProblemTask {

	@Override
	public void execute() {

		try {

			// 是否发布webservice
			schemeWsPublish();

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Download exception : ", e);
			setException(e.getMessage());
			event.setEnd(Calendar.getInstance().getTime());
			event.setException(e.getMessage());
			EventService.getInstance().post(event);
		}

	}

	/**
	 * 方案库地市工单ws接口
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void schemeWsPublish() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		String url = parpams.get("wsurl");
		String classname = parpams.get("classname");
		Endpoint.publish(url, Class.forName(classname).newInstance());
	}

}
