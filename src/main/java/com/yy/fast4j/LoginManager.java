package com.yy.fast4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 登陆管理器
 * @author yy
 *
 */
public class LoginManager {
	private final Map<Integer, HttpSession> webLoginUserMap;
	
	private String userIdToTokenPre;
	private String tokenToUserIdPre;
	private long tokenExpirationTime;//token过期时间，单位：毫秒
	private RedisTemplate<String, Object> redisTemplate;

	public LoginManager(String userIdToTokenPre, String tokenToUserIdPre, long tokenExpirationTime, RedisTemplate<String, Object> redisTemplate) {
		this.userIdToTokenPre = userIdToTokenPre;
		this.tokenToUserIdPre = tokenToUserIdPre;
		this.tokenExpirationTime = tokenExpirationTime;
		this.redisTemplate = redisTemplate;
		this.webLoginUserMap = new ConcurrentHashMap<Integer, HttpSession>();
	}
	
	//app登陆
	public String appLogin(Integer userId) {
		//先检查此用户有没有app登陆过，如果有，则删除以前的token
		String token = (String)RedisUtil.deleteAndReturnValue(redisTemplate, userIdToTokenPre, userId.toString());
		if(token != null) { //删除以前的token缓存
			RedisUtil.delete(redisTemplate, tokenToUserIdPre, token);
		}
		
		//再检查有没有web登陆过，如果有，清除session中的userId
		HttpSession oldSession = webLoginUserMap.remove(userId);
		if(oldSession != null) {
			oldSession.removeAttribute("userId");
		}
		
		//创建新的token，使用uuid作为token
		token = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
		
		//将新的当前用户token覆盖原来的token
		RedisUtil.set(redisTemplate, userIdToTokenPre, userId.toString(), token, tokenExpirationTime, TimeUnit.MILLISECONDS);
		RedisUtil.set(redisTemplate, tokenToUserIdPre, token, userId, tokenExpirationTime, TimeUnit.MILLISECONDS);
		return token;
	}
	
	//web登陆
	public void webLogin(Integer userId, HttpSession session) {
		//先检查此用户有没有app登陆过，如果有，则删除以前的token
		String token = (String)RedisUtil.deleteAndReturnValue(redisTemplate, userIdToTokenPre, userId.toString());
		if(token != null) { //删除以前的token缓存
			RedisUtil.delete(redisTemplate, tokenToUserIdPre, token);
		}
		
		//先检查此用户有没有web登陆过，如果有，清除session中的userId
		HttpSession oldSession = webLoginUserMap.remove(userId);
		if(oldSession != null) {
			oldSession.removeAttribute("userId");
		}
		
		session.setAttribute("userId", userId);
		webLoginUserMap.put(userId, session);
	}
	
	//app退出登陆
	public void appLogout(String token) {
		if(token != null) {
			Integer userId = (Integer)RedisUtil.deleteAndReturnValue(redisTemplate, tokenToUserIdPre, token);
			if(userId != null) {
				RedisUtil.delete(redisTemplate, userIdToTokenPre, userId.toString());
			}
		}
	}

	//web退出登陆
	public void webLogout(HttpSession session) {
		Integer userId = (Integer)session.getAttribute("userId");
		if(userId != null) {
			session.removeAttribute("userId");
			session = webLoginUserMap.remove(userId);
			if(session != null) session.removeAttribute("userId");
		}
	}
	
	//判断是否登陆
	public boolean isLogin(String token) {
		if(token != null) {
			Integer userId = RedisUtil.getInteger(redisTemplate, tokenToUserIdPre, token);
			return userId != null;
		} else {
			return false;
		}
	}
	public boolean isLogin(HttpSession session) {
		Object userId = session.getAttribute("userId");
		return userId != null;
	}
	
	
	//从session中，以及redis中获取登陆用户的userId, 如果用户没有登陆则返回null
	public Integer getUserId(HttpServletRequest req) {
		Integer userId = (Integer)req.getSession().getAttribute("userId");
		if(userId != null) {
			return userId;
		}

		//session中没有再从redis中读取userId
		String token = req.getParameter("token");
		if(token != null) {
			return RedisUtil.getInteger(redisTemplate, tokenToUserIdPre, token);
		} else {
			return null;
		}
	}
}