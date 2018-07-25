package com.yy.fast4j;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisCache implements Cache {

	private JedisPool jedisPool;
	
	public RedisCache(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	@Override
	public void set(String pre, String key, String value) {
		key = pre + ":" + key;
		try(Jedis jedis = jedisPool.getResource()) {
			jedis.set(key, value);
		}
	}

	@Override
	public void set(String pre, String key, String value, long milliseconds) {
		key = pre + ":" + key;
		try(Jedis jedis = jedisPool.getResource()) {
			jedis.set(key, value);
			jedis.pexpire(key, milliseconds);
		}
	}

	@Override
	public String getString(String pre, String key) {
		key = pre + ":" + key;
		try(Jedis jedis = jedisPool.getResource()) {
			return jedis.get(key);
		}
	}

	@Override
	public String delete(String pre, String key) {
		key = pre + ":" + key;
		try(Jedis jedis = jedisPool.getResource()) {
			String value = jedis.get(key);
			jedis.del(key);
			return value;
		}
	}

	@Override
	public void clear() {
		try(Jedis jedis = jedisPool.getResource()) {
			jedis.flushAll();
		}
	}
}