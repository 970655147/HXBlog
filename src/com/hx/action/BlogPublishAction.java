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
import com.hx.util.Log;
import com.hx.util.Tools;

public class BlogPublishAction extends HttpServlet {

	// ÐÞ¸Ä²¥¿Í
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String title = req.getParameter("title");
		String tags = req.getParameter("tags");
		String content = req.getParameter("content");
		Date now = new Date();
		String createTime = Constants.createDateFormat.format(now );
		String blogName = Tools.getBlogFileName(Constants.dateFormat.format(now), title);
		
		Blog newBlog = new Blog(BlogListAction.nextBlogId(), title, blogName, tags, createTime);
		Tools.save(content, Tools.getBlogPath(Tools.getProjectPath(), blogName) );
		BlogListAction.publishBlog(newBlog, req.getServletContext());
		
		// -----------------------------------------
		ResponseMsg respMsg = new ResponseMsg(true, Constants.defaultResponseCode, Tools.getPostSuccMsg(newBlog));
		
		PrintWriter out = resp.getWriter();
		String respInfo = respMsg.toString();
		out.write(respInfo );
		Tools.log(this, respInfo);
		out.close();
	}
	
}
