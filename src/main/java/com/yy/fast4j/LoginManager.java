package com.yy.fast4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpSession;

/**
 * 登陆管理器
 * @author yy
 *
 */
public class LoginManager {
	private final Map<Integer, HttpSession> webLoginUserMap = new ConcurrentHashMap<Integer, HttpSession>();
	
	private String userIdToTokenPre;
	private String tokenToUserIdPre;
	private long tokenExpirationTime;//token过期时间，单位：毫秒
	private Cache cache;

	public LoginManager(String userIdToTokenPre, String tokenToUserIdPre, long tokenExpirationTime, Cache cache) {
		this.userIdToTokenPre = userIdToTokenPre;
		this.tokenToUserIdPre = tokenToUserIdPre;
		this.tokenExpirationTime = tokenExpirationTime;
		this.cache = cache;
	}
	
	//app登陆
	public String appLogin(Integer userId) {
		//先检查此用户有没有app登陆过，如果有，则删除以前的token
		String token = cache.delete(userIdToTokenPre, userId.toString());
		if(token != null) { //删除以前的token缓存
			cache.delete(tokenToUserIdPre, token);
		}
		
		//再检查有没有web登陆过，如果有，清除session中的userId
		HttpSession oldSession = webLoginUserMap.remove(userId);
		if(oldSession != null) {
			oldSession.removeAttribute("userId");
		}
		
		//创建新的token，使用uuid作为token
		token = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
		
		//将新的当前用户token覆盖原来的token
		cache.set(userIdToTokenPre, userId.toString(), token, tokenExpirationTime);
		cache.set(tokenToUserIdPre, token, userId.toString(), tokenExpirationTime);
		return token;
	}
	
	//web登陆
	public void webLogin(Integer userId, HttpSession session) {
		//先检查此用户有没有app登陆过，如果有，则删除以前的token
		String token = cache.delete(userIdToTokenPre, userId.toString());
		if(token != null) { //删除以前的token缓存
			cache.delete(tokenToUserIdPre, token);
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
			String userId = cache.delete(tokenToUserIdPre, token);
			if(userId != null) {
				cache.delete(userIdToTokenPre, userId);
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
}