package com.yy.fast4j;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class QueryCondition {
	private Map<String, Condition> conditionMap = new LinkedHashMap<String, Condition>(); //条件
	private Set<String> sortSet = new LinkedHashSet<String>();  //排序
	private Page page;
	
	/**
	 * 添加查询条件
	 * @param field 要添加查询的字段名
	 * @param operator 操作符，如：=、>、< 之类的sql查询操作符
	 * @param value 字段值
	 * @param join 条件连接符 “and”“ or”(一般都为and)
	 * @return
	 */
	public QueryCondition addCondition(String field, String operator, Object value, ConditionJoin join) {
		String key = join.toString() + field + operator;
		this.conditionMap.put(key, new Condition(field, operator, value, join));
		return this;
	}
	//添加查询条件，默认连接符为and
	public QueryCondition addCondition(String field, String operator, Object value) {
		return this.addCondition(field, operator, value, ConditionJoin.AND);
	}
	
	/**
	 * 添加分页
	 * @param pageSize 一页多少条记录
	 * @param pageNo 第几页(从第1页开始)
	 * @return
	 */
	public QueryCondition setPage(Page page) {
		this.page = page;
		return this;
	}
	public Page getPage() {
		return this.page;
	}
	
	//删除查询条件
	public QueryCondition removeCondition(String field, String operator, ConditionJoin join) {
		String key = join.toString() + field + operator;
		this.conditionMap.remove(key);
		return this;
	}
	//删除查询条件，默认连接符为and
	public QueryCondition removeCondition(String field, String operator) {
		return this.removeCondition(field, operator, ConditionJoin.AND);
	}
	
	/**
	 * 添加排序
	 * @param 要排序的字段
	 * @param 顺序还是倒序，asc、desc
	 * @return
	 */
	public QueryCondition addSort(String field, SortType sortType) {
		this.sortSet.add(field + " " + sortType.toString());
		return this;
	}
	//删除排序
	public QueryCondition removeSort(String field, SortType sortType) {
		this.sortSet.remove(field + " " + sortType.toString());
		return this;
	}

	public boolean isUseWhere() {
		return this.conditionMap.size() > 0 ? true : false;
	}
	public Collection<Condition> getConditions() {
		return this.conditionMap.values();
	}
	public boolean isUseSort() {
		return this.sortSet.size() > 0 ? true : false;
	}
	public Set<String> getSorts() {
		return this.sortSet;
	}

	//条件类
	public class Condition {
		private String field;
		private String operator; //运算符
		private Object value;
		private ConditionJoin join; //条件连符 and or
		public Condition(String field, String operator, Object value, ConditionJoin join) {
			this.field = field;
			this.operator = operator;
			this.value = value;
			this.join = join;
		}
		public String getField() {
			return field;
		}
		public String getOperator() {
			return operator;
		}
		public Object getValue() {
			return value;
		}
		public ConditionJoin getJoin() {
			return join;
		}
		public void setField(String field) {
			this.field = field;
		}
		public void setOperator(String operator) {
			this.operator = operator;
		}
		public void setValue(Object value) {
			this.value = value;
		}
		public void setJoin(ConditionJoin join) {
			this.join = join;
		}
	}

	public enum ConditionJoin{
		AND, OR
	}
	
	public enum SortType {
		ASC, DESC
	}
}
