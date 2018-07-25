package com.yy.fast4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class CheckUserLoginInterceptor implements HandlerInterceptor {
	private Cache cache;
	private String tokenToUserIdPre;
	public CheckUserLoginInterceptor(Cache cache, String tokenToUserIdPre) {
		this.cache = cache;
		this.tokenToUserIdPre = tokenToUserIdPre;
	}
	
	//进入handler方法之前执行
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		Integer userId = (Integer)request.getSession().getAttribute("userId");
		if(userId != null) {//先从session中读取userId
			request.setAttribute("userId", userId);
			return true;
		}
		
		//session中没有再从redis中读取userId
		String token = request.getParameter("token");
		if(token != null) {
			userId = cache.getInt(tokenToUserIdPre, token);
			if(userId != null) {
				request.setAttribute("userId", userId);
				return true;
			} else {
				response.setContentType("application/json;charset=utf-8");
				response.getWriter().write(Fast4jUtils.ObjecttoJson(new ResponseObject(200, "您还未登陆，或登陆已过期，请重新登陆")));
				return false;
			}
		} else {
			response.setContentType("application/json;charset=utf-8");
			response.getWriter().write(Fast4jUtils.ObjecttoJson(new ResponseObject(200, "您还未登陆，或登陆已过期，请重新登陆")));
			return false;
		}
	}
	
	//进入handler方法之后，返回modelAndView之前执行
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}
	
	//执行handler完成执行此方法
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}
}
