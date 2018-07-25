package com.yy.fast4j;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class UserWebLoginListener implements HttpSessionListener {
	private LoginManager loginManager;

	@Override
	public void sessionCreated(HttpSessionEvent e) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent e) {
		HttpSession session = e.getSession();
		if(loginManager == null) {
			loginManager = Fast4jUtils.getBean(LoginManager.class, session.getServletContext());
		}
		loginManager.webLogout(session);
	}
}