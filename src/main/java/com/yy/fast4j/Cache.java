package com.yy.fast4j;

public interface Cache {
	//缓存一个数据，永久有效
	void set(String pre, String key, String value);
	
	//缓存一个数据，并设置有效时间，单位：毫秒
	void set(String pre, String key, String value, long milliseconds);
	
	//返回一个String
	String getString(String pre, String key);
	
	//返回一个Integer
	default Integer getInt(String pre, String key) {
		String value = getString(pre, key);
		if(value == null) {
			return null;
		} else {
			return Integer.valueOf(value);
		}
	}
	
	//返回一个Double
	default Double getDouble(String pre, String key) {
		String value = getString(pre, key);
		if(value == null) {
			return null;
		} else {
			return Double.valueOf(value);
		}
	}
	
	//删除一个值，并返回删除的值
	String delete(String pre, String key);
		
	//清除所有缓存
	void clear();
}