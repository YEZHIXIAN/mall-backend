package com.zhixian.mall.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class R extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public R() {
		put("code", 0);
		put("msg", "success");
	}

	public static R error() {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}

	public static R error(String msg) {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
	}

	public static R error(int code, String msg) {
		R r = new R();
		r.put("code", code);
		r.put("msg", msg);
		return r;
	}

	public static R ok(String msg) {
		R r = new R();
		r.put("msg", msg);
		return r;
	}

	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}

	public static R ok() {
		return new R();
	}

	public static <T> R ok(T data){
		R r = new R();
		r.put("data", data);
		return r;
	}

	public <T> T getData(String key, TypeReference<T> typeReference){
		Object data = get(key);
		return objectMapper.convertValue(data, typeReference);
	}

	public <T> T getData(TypeReference<T> typeReference){
		Object data = get("data");
		return objectMapper.convertValue(data, typeReference);
	}

	@Override
	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public Integer getCode() {
		return (Integer) this.get("code");
	}
}
