package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.hx.bean.Blog;
import com.hx.bean.Comment;
import com.hx.business.BlogManager;
import com.hx.business.CommentManager;
import com.hx.util.Constants;
import com.hx.util.Tools;

public class BlogGetAction extends HttpServlet {
	
	// 给定播客的名称
	// 获取blogId, 确保其为整数
	// 获取blogId对应的blog, 确保该blog存在
	// 确保blog对应的文件是否存在
		// 按情况 更新该播客的visited, 并返回该博客的信息
	// 添加登录信息, 播客信息
	// 如果blogId不为null, 获取顶踩信息, 以及该博客的评论信息, 以及用户的偏好信息 [姓名, 邮箱]
		// 如果登录, 则获取修改, 删除按钮信息, 在获取当前播客的上一页, 以及下一页的播客id
	// 返回 响应结果, 记录日志	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);
		
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
			blog = new Blog(Constants.defaultBlogId, "have no this blog !", Constants.defaultBlogPath, Constants.defaultBlogTag, null, new AtomicInteger(Constants.defaultGood), new AtomicInteger(Constants.defaultNotGood), Constants.defaultVisited);
			blog.setContent("hi, you have been intercept ~ ~ ~ ~ !");
			injectorInfo.element("blogId", req.getParameter("blogId")).element("ip", Tools.getIPAddr(req));
		} else {
			Blog blogInServer = BlogManager.getBlog(blogId);
			if(blogInServer == null) {
				blog = new Blog(Constants.defaultBlogId, "have no this blog !", Constants.defaultBlogPath, Constants.defaultBlogTag, null, new AtomicInteger(Constants.defaultGood), new AtomicInteger(Constants.defaultNotGood), Constants.defaultVisited);
				blog.setContent("have no this blog !");
				injectorInfo.element("blogId", blogId).element("ip", Tools.getIPAddr(req)); 
			} else {
				blog = new Blog(blogInServer);
				String blogPath = Tools.getBlogPath(Tools.getProjectPath(), (blogInServer.getPath()) );
				if(! Tools.isFileExists(blogPath) ) {
					blog.setContent("this blog already be deleted, maybe by adminstrator !");
				} else {
					if(! Constants.visitedCookieValue.equals(Tools.getCookieValByName(req.getCookies(), Tools.getVisitedCookieName(blog.getId()))) ) {
						blogInServer.incVisited();
						blog.incVisited();
						BlogManager.addVisitSense(blog);
						resp.addCookie(new Cookie(Tools.getVisitedCookieName(blog.getId()), Constants.visitedCookieValue));
					}
					String content = Tools.getContent(blogPath, Constants.DEFAULT_CHARSET);
					blog.setContent(content);
				}
			}
		}
		
		JSONObject res = new JSONObject();
		boolean isLogin = Tools.isLogin(req);
		res.element("isLogin",  isLogin);
		res.element("blog", blog.toString() );
		if(blogId != null) {
			res.element("sense", Tools.getCookieValByName(req.getCookies(), Tools.getSensedCookieName(blogId)) );
			List<List<Comment>> blogComments = CommentManager.getCommentByBlogId(blogId);
			if(blogComments != null) {
				res.element("comments", Tools.encapBlogComments(blogComments) );
			}
			Tools.addIfNotEmpty(res, "user", Tools.getStrFromSession(req, Constants.preferInfo) );
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
		}
		
		PrintWriter out = resp.getWriter();
		String respInfo = res.toString();
		out.write(respInfo );
		res.getJSONObject("blog").element("content", Tools.compressBlogContent(blog.getContent()) );
		Tools.log(this, res.toString() );
		if(injectorInfo.size() > 0) {
			Tools.log(this, injectorInfo.toString() );
		}
		out.close();
	}
}
