package com.etone.universe.dmp.problem;

import java.util.HashMap;
import java.util.Map;

/**
 * 公用条件查询类<br>
 * 也可以用于MVC层之间的参数传递
 * 
 * @author <a href="mailto:88453013@qq.com">Guojian</a>
 * @version $$Revision: 14169 $$
 * @date 2013-9-6
 */

public class QueryCriteria {
	/**
	 * 存放条件查询值
	 */
	private Map<String, Object> condition;

	/**
	 * 是否相异
	 */
	protected boolean distinct;

	/**
	 * 排序字段
	 */
	protected String orderByClause;
	/**
	 * 排序类型
	 */
	protected String orderType = "ASC";

	private Integer rowStart = 0;
	private Integer rowEnd = 100;

	protected QueryCriteria(QueryCriteria example) {
		this.orderByClause = example.orderByClause;
		this.condition = example.condition;
		this.distinct = example.distinct;
		this.rowStart = example.rowStart;
		this.rowEnd = example.rowEnd;
	}

	public QueryCriteria() {
		condition = new HashMap<String, Object>();
	}

	public void clear() {
		this.condition.clear();
		this.orderByClause = null;
		this.distinct = false;
		this.rowStart = null;
		this.rowEnd = null;
	}

	/**
	 * @param condition
	 *            查询的条件名称
	 * @param value
	 *            查询的值
	 */
	public QueryCriteria put(String condition, Object value) {
		this.condition.put(condition, value);
		return (QueryCriteria) this;
	}

	/**
	 * 得到键值，C层和S层的参数传递时取值所用<br>
	 * 自行转换对象
	 * 
	 * @param key
	 *            键值
	 * @return 返回指定键所映射的值
	 */
	public Object get(String key) {
		return this.condition.get(key);
	}

}