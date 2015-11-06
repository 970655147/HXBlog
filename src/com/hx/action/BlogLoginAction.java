package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import com.hx.bean.UserInfo;
import com.hx.util.Constants;
import com.hx.util.Tools;

// 获取blog配置的action
public class BlogLoginAction extends HttpServlet {

	// 登录校验
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		
		String userName = req.getParameter("userName");
		String pwd = req.getParameter("pwd");
		JSONObject userInfo = new JSONObject().element("userName", userName).element("pwd", pwd).element("ip", Tools.getIPAddr(req) );
		StringBuilder sb = new StringBuilder();
		sb.append("an user attempt to login with : ");
		sb.append(userInfo.toString() );
		sb.append(", ");
		
		boolean isLogin = false;
		if(userName.equals(Constants.account) && pwd.equals(Constants.pwd)) {
			HttpSession session = req.getSession();
			session.setAttribute(Constants.ACCOUNT_NAME, Constants.account);
			session.setAttribute(Constants.TOKEN, Constants.token);
			session.setAttribute(Constants.preferInfo, new UserInfo(Constants.adminUserName, Constants.adminEmail, Constants.adminImageIdx) );
			sb.append("login success !");
			isLogin = true;
		} else {
			sb.append("login failed !");
		}
		
		Tools.log(this, sb.toString() );
		if(isLogin) {
			resp.sendRedirect("/HXBlog/");
		} else {
			resp.sendRedirect("/HXBlog/login.html");
		}
	}
	
}
