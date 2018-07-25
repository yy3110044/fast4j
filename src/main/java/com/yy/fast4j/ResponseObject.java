package com.yy.fast4j;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseObject {
	private int code;
	private String msg;
	private Object result;
	public ResponseObject() {}
	
	public ResponseObject(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public ResponseObject(int code, String msg, Object result) {
		this(code, msg);
		this.result = result;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
}