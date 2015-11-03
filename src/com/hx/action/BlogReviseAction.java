package com.hx.action;

import java.io.File;
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

public class BlogReviseAction extends HttpServlet {

	// 修改播客
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Integer id = Integer.parseInt(req.getParameter("id") );
		String title = req.getParameter("title");
		String tags = req.getParameter("tags");
		String content = req.getParameter("content");
		Blog newBlog = new Blog(BlogListAction.getBlog(id) );
		String oldBlogName = newBlog.getPath();
		boolean isChangeName = ! Tools.equalsIgnorecase(oldBlogName.trim(), title.trim() );
		
		Date now = new Date();
		String createTime = Constants.createDateFormat.format(now );
		String blogName = Tools.getBlogFileName(Constants.dateFormat.format(now), title);	
		newBlog.set(id, title, blogName, tags, null);
		
		if(! isChangeName) {
			Tools.save(content, Tools.getBlogPath(Tools.getProjectPath(req.getServletContext()), oldBlogName) );			
		} else {
			Tools.save(content, Tools.getBlogPath(Tools.getProjectPath(req.getServletContext()), oldBlogName) );
			Tools.renameTo(Tools.getBlogPath(Tools.getProjectPath(req.getServletContext()), oldBlogName), Tools.getBlogPath(Tools.getProjectPath(req.getServletContext()), blogName) );
		}

		BlogListAction.reviseBlog(newBlog);
		
		// -----------------------------------------
		PrintWriter out = resp.getWriter();
		ResponseMsg respMsg = new ResponseMsg(true, Constants.defaultResponseCode, Tools.getPostSuccMsg(newBlog));
		out.write(respMsg.toString() );
		Tools.log(this, respMsg);
		out.close();
	}
	
	// 处理掉id为list的style标签 [这里先暂时这样处理, 之后再回来完善]
	private String prepare(String revised) {
		StringBuilder sb = new StringBuilder(revised.length());
		String startStr = "<style id=\"list\"";
		String endStr = "</style>";
		int liStyleIdxBefore = revised.indexOf(startStr);
		int liStyleIdxAfter = revised.indexOf(">", liStyleIdxBefore + startStr.length());
		sb.append(revised.substring(0, liStyleIdxBefore) );
		sb.append(revised.substring(revised.indexOf(endStr, liStyleIdxAfter)) );
		
		return sb.toString();
	}
	
}
