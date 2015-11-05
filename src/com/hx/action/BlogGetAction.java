package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.hx.bean.Blog;
import com.hx.util.Constants;
import com.hx.util.Log;
import com.hx.util.Tools;

public class BlogGetAction extends HttpServlet {
	
	// 给定播客的名称
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		
		Integer blogId = Integer.parseInt(req.getParameter("blogId") );
		Blog blog = new Blog(BlogListAction.getBlog(blogId) );
		String content = Tools.getContent(Tools.getBlogPath(Tools.getProjectPath(), (blog.getPath()) ), Tools.DEFAULT_CHARSET);
		blog.setContent(content);
		
		JSONObject res = new JSONObject();
		boolean isLogin = Tools.isLogin(req);
		res.element("isLogin",  isLogin);
		res.element("blog", blog.toString() );
		if(isLogin) {
			res.element("reviseBtn", Constants.reviseBtn);
			res.element("deleteBtn", Constants.deleteBtn);
		}
		
		PrintWriter out = resp.getWriter();
		String respInfo = res.toString();
		out.write(respInfo );
		Tools.log(this, respInfo );
		out.close();
	}
}
