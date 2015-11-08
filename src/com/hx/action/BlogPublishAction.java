package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

public class BlogPublishAction extends HttpServlet {

	// 修改播客
	// 确保用户登录, 校验校验码
	// 校验title, tags, content
	// 创建Blog对象, 持久化blog文件, 将blog保存到BlogManager中
	// 返回 响应结果, 记录日志		
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		
		ResponseMsg respMsg = new ResponseMsg();
		if(Tools.validateUserLogin(req, respMsg)) {
			if(Tools.validateCheckCode(req, respMsg)) {
				String title = Tools.replaceMultiSpacesAsOne(req.getParameter("title") );
				String tags = Tools.replaceMultiSpacesAsOne(req.getParameter("tags") );
				String content = req.getParameter("content");
				if(Tools.validateTitle(req, title, "title", respMsg)) {
					if(Tools.validateTags(req, tags, respMsg)) {
						if(Tools.validateContent(req, content, respMsg) ) {
							Date now = new Date();
							String createTime = Constants.createDateFormat.format(now );
							String blogName = Tools.getBlogFileName(Constants.dateFormat.format(now), title);
							
							Blog newBlog = new Blog(BlogManager.nextBlogId(), title, blogName, tags, createTime, new AtomicInteger(0), new AtomicInteger(0), 0);
							Tools.save(content, Tools.getBlogPath(Tools.getProjectPath(), blogName) );
							BlogManager.publishBlog(newBlog);
							respMsg = new ResponseMsg(true, Constants.defaultResponseCode, Tools.getPostSuccMsg(newBlog), null);
						}
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
