package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import com.hx.util.Constants;
import com.hx.util.Log;
import com.hx.util.Tools;

// ��ȡblog���õ�action
public class BlogLogoutAction extends HttpServlet {

	// ��ȫ�˳�ϵͳ
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		
		boolean isLogin = Tools.isLogin(req);
		StringBuilder sb = new StringBuilder();
		JSONObject userInfo = new JSONObject().element("userName", Tools.getStrFromSession(req, Constants.USER_NAME)).element("token", Tools.getStrFromSession(req, Constants.TOKEN)).element("ip", Tools.getIPAddr(req) );
		sb.append("an user attempt to login out : ");
		sb.append(userInfo.toString() );
		sb.append(", ");
		
		if(isLogin) {
			sb.append("this user is valid !");
			HttpSession session = req.getSession();
			session.removeAttribute(Constants.USER_NAME);
			session.removeAttribute(Constants.TOKEN);
		} else {
			sb.append("this user is invalid !");
		}
		
		Tools.log(this, sb.toString() );
		resp.sendRedirect("/HXBlog/");
	}
	
}