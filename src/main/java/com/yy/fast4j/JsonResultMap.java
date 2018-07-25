package com.yy.fast4j;

import java.util.HashMap;

public class JsonResultMap extends HashMap<String, Object> {
	private static final long serialVersionUID = 8148208679743524317L;
	
	public JsonResultMap set(String key, Object value) {
		put(key, value);
		return this;
	}
}