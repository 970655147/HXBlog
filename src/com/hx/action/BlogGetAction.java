package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.hx.bean.Blog;
import com.hx.business.BlogManager;
import com.hx.util.Constants;
import com.hx.util.Log;
import com.hx.util.Tools;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

public class BlogGetAction extends HttpServlet {
	
	// 给定播客的名称
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		
		JSONObject injectorInfo = new JSONObject();
		Integer blogId = null;
		String tag = req.getParameter("tag");
		Blog blog = null;
		try {
			blogId = Integer.parseInt(req.getParameter("blogId") );
		} catch(Exception e) {
			blogId = null;
		}
		
		if(blogId == null) {
			blog = new Blog(Constants.defaultBlogId, "have no this blog !", Constants.defaultBlogPath, Constants.defaultBlogTag, null);
			blog.setContent("hi, you have been intercept ~ ~ ~ ~ !");
			injectorInfo.element("blogId", req.getParameter("blogId")).element("ip", Tools.getIPAddr(req));
		} else {
			blog = BlogManager.getBlog(blogId);
			if(blog == null) {
				blog = new Blog(Constants.defaultBlogId, "have no this blog !", Constants.defaultBlogPath, Constants.defaultBlogTag, null);
				blog.setContent("have no this blog !");
				injectorInfo.element("blogId", blogId).element("ip", Tools.getIPAddr(req)); 
			} else {
				blog = new Blog(blog);
				String blogPath = Tools.getBlogPath(Tools.getProjectPath(), (blog.getPath()) );
				if(! Tools.isFileExists(blogPath) ) {
					blog.setContent("this blog already be deleted, maybe by adminstrator !");
				} else {
					String content = Tools.getContent(blogPath, Tools.DEFAULT_CHARSET);
					blog.setContent(content);
				}
			}
		}
		
		JSONObject res = new JSONObject();
		boolean isLogin = Tools.isLogin(req);
		res.element("isLogin",  isLogin);
		res.element("blog", blog.toString() );
		if(isLogin) {
			res.element("reviseBtn", String.format(Constants.reviseBtn, blog.getId()) );
			res.element("deleteBtn", Constants.deleteBtn);
		}
		if(! Tools.isEmpty(tag)) {
			int curBlogIdx = BlogManager.getBlogIdxByIdAndTag(blogId, tag);
			if(curBlogIdx == Constants.HAVE_NO_THIS_TAG) {
				injectorInfo.element("tag", tag).element("ip", Tools.getIPAddr(req));
			} else {
				int prevBlogId = BlogManager.getBlogIdByIdxAndTag(curBlogIdx+1, tag);
				if(prevBlogId != Constants.HAVE_NO_PREV_IDX) {
					res.element("prevBlogId", prevBlogId);
				}
				int nextBlogId = BlogManager.getBlogIdByIdxAndTag(curBlogIdx-1, tag);
				if(nextBlogId != Constants.HAVE_NO_NEXT_IDX) {
					res.element("nextBlogId", nextBlogId);
				}
			}
		}
		
		PrintWriter out = resp.getWriter();
		String respInfo = res.toString();
		out.write(respInfo );
		Tools.log(this, respInfo );
		if(injectorInfo.size() > 0) {
			Tools.log(this, injectorInfo.toString() );
		}
		out.close();
	}
}
