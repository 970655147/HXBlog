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
import com.hx.util.Constants;
import com.hx.util.Tools;

public class BlogPublishAction extends HttpServlet {

	// ÐÞ¸Ä²¥¿Í
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String title = req.getParameter("title");
		String tags = req.getParameter("tags");
		String content = req.getParameter("content");
		String createTime = Constants.dateFormat.format(new Date() );
		String blogName = Tools.getBlogFileName(createTime, title);
		
		Blog newBlog = new Blog(BlogListAction.nextBlogId(), title, blogName, tags, createTime);
		Tools.save(content, Tools.getBlogPath(Tools.getProjectPath(req), blogName + Tools.HTML) );
		BlogListAction.publishBlog(newBlog);
		
		// -----------------------------------------
		PrintWriter out = resp.getWriter();
		out.write(new ResponseMsg(true, Constants.defaultResponseCode, Tools.getSuccMsg(newBlog)).toString() );
		out.close();
	}
	
}
