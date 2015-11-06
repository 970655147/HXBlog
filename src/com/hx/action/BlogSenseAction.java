package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.bean.Blog;
import com.hx.bean.ResponseMsg;
import com.hx.business.BlogManager;
import com.hx.util.Constants;
import com.hx.util.Log;
import com.hx.util.Tools;

public class BlogSenseAction extends HttpServlet {

	// 播客顶/ 踩处理的action
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		
		Integer blogId = null;
		ResponseMsg respMsg = new ResponseMsg();
		try {
			blogId = Integer.parseInt(req.getParameter("blogId") );
		} catch(Exception e) {
			blogId = null;
		}
		String sense = req.getParameter("sense");
		if(Tools.validateObjectBeNull(req, blogId, "blogId", respMsg)) {
			if(Tools.validateStringBeNull(req, sense, "sense", respMsg) ) {
				Blog blog = BlogManager.getBlog(blogId);
				respMsg.setOthers(sense);
				if(Tools.validateBlog(req, blog, respMsg) ) {
					if(Tools.validateIsSensed(req, blog.getId(), respMsg) ) {
						respMsg.set(true, Constants.defaultResponseCode, "sense to : " + sense, Tools.getIPAddr(req) );
						if(sense.equals(Constants.senseGood) ) {
							blog.incGood();
						} else {
							blog.incNotGood();
						}
						BlogManager.addVisitSense(blog);
						resp.addCookie(new Cookie(Tools.getSensedCookieName(blog.getId() ), Constants.senseCookieValue) );
					}
				}
			}
		}
		
		PrintWriter out = resp.getWriter();
		String respInfo = respMsg.toString();
		out.write(respInfo );
		Tools.log(this, respInfo);
		out.close();
	}
	
}
