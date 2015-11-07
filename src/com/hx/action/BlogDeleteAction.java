package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.bean.Blog;
import com.hx.bean.ResponseMsg;
import com.hx.business.BlogManager;
import com.hx.util.Constants;
import com.hx.util.Log;
import com.hx.util.Tools;

public class BlogDeleteAction extends HttpServlet {

	// É¾³ý²¥¿Í
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		
		ResponseMsg respMsg = new ResponseMsg();
		if(Tools.validateUserLogin(req, respMsg)) {
			Integer blogId = Integer.parseInt(req.getParameter("id") );
			Blog oldBLog = BlogManager.getBlog(blogId);
			
			if(Tools.validateBlog(req, oldBLog, respMsg) ) {
				if(Tools.isFileExists(Tools.getBlogPath(Tools.getProjectPath(), (oldBLog.getPath()) )) ) {	
					Tools.delete(Tools.getBlogPath(Tools.getProjectPath(), oldBLog.getPath()) );
				}
				BlogManager.deleteBlog(oldBLog);
				respMsg.set(true, Constants.defaultResponseCode, Tools.getDeleteSuccMsg(oldBLog), null);
			}
		}
		
		PrintWriter out = resp.getWriter();
		String respInfo = respMsg.toString();
		out.write(respInfo );
		Tools.log(this, respInfo);
		out.close();
	}
	
}
