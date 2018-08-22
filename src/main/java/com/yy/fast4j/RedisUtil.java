package com.yy.fast4j;

import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * redis工具类
 * @author 49803
 *
 */
public class RedisUtil {
	private RedisUtil() {}

	//缓存一个值
	public static void set(RedisTemplate<String, Object> redisTemplate, String pre, String key, Object value) {
		key = pre + ":" + key;
		redisTemplate.opsForValue().set(key, value);
	}
	
	//缓存一个值，并设置过期时间
	public static void set(RedisTemplate<String, Object> redisTemplate, String pre, String key, Object value, long timeout, TimeUnit unit) {
		key = pre + ":" + key;
		redisTemplate.opsForValue().set(key, value);
		redisTemplate.expire(key, timeout, unit);
	}
	
	//缓存一个值，并设置过期时间以秒为单位
	public static void set(RedisTemplate<String, Object> redisTemplate, String pre, String key, Object value, long seconds) {
		set(redisTemplate, pre, key, value, seconds, TimeUnit.SECONDS);
	}

	//从redis中取出一个Object值
	public static Object getObject(RedisTemplate<String, Object> redisTemplate, String pre, String key) {
		key = pre + ":" + key;
		return redisTemplate.opsForValue().get(key);
	}
	public static String getString(RedisTemplate<String, Object> redisTemplate, String pre, String key) {
		return (String)getObject(redisTemplate, pre, key);
	}
	public static Integer getInteger(RedisTemplate<String, Object> redisTemplate, String pre, String key) {
		return (Integer)getObject(redisTemplate, pre, key);
	}
	public static Long getLong(RedisTemplate<String, Object> redisTemplate, String pre, String key) {
		return (Long)getObject(redisTemplate, pre, key);
	}
	public static Float getFloat(RedisTemplate<String, Object> redisTemplate, String pre, String key) {
		return (Float)getObject(redisTemplate, pre, key);
	}
	public static Double getDouble(RedisTemplate<String, Object> redisTemplate, String pre, String key) {
		return (Double)getObject(redisTemplate, pre, key);
	}
	
	//删除key以及值
	public static Boolean delete(RedisTemplate<String, Object> redisTemplate, String pre, String key) {
		key = pre + ":" + key;
		return redisTemplate.delete(key);
	}
	
	//删除key，并返回值
	public static Object deleteAndReturnValue(RedisTemplate<String, Object> redisTemplate, String pre, String key) {
		key = pre + ":" + key;
		Object retVal = redisTemplate.opsForValue().get(key);
		redisTemplate.delete(key);
		return retVal;
	}

	//取出一个Object值，从ServletContext中取出RedisTemplate
	@SuppressWarnings("unchecked")
	public static Object getObject(ServletContext sc, String pre, String key) {
		return getObject((RedisTemplate<String, Object>)Fast4jUtils.getBean("redisTemplate", sc), pre, key);
	}
}