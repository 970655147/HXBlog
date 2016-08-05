package com.hx.blog.action;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.blog.bean.Blog;
import com.hx.blog.bean.Comment;
import com.hx.blog.business.BlogManager;
import com.hx.blog.business.CommentManager;
import com.hx.blog.interf.BaseAction;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

import net.sf.json.JSONObject;

public class BlogGetAction extends BaseAction {
	
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
		String tag = (String) req.getAttribute(Constants.tag);
		Blog blog = null;
		try {
			blogId = Integer.parseInt((String) req.getAttribute(Constants.blogId) );
		} catch(Exception e) {
			blogId = null;
		}
		
		if(blogId == null) {
			blog = new Blog(Constants.defaultBlogId, "have no this blog !", Constants.defaultBlogPath, Constants.defaultBlogTag, null, Constants.defaultGood,Constants.defaultNotGood, Constants.defaultVisited, Constants.defaultCommentsNum);
			blog.setContent("hi, you have been intercept ~ ~ ~ ~ !");
			injectorInfo.element("blogId", req.getAttribute("blogId")).element("ip", Tools.getIPAddr(req));
		} else {
			Blog blogInServer = BlogManager.getBlog(blogId);
			if(blogInServer == null) {
				blog = new Blog(Constants.defaultBlogId, "have no this blog !", Constants.defaultBlogPath, Constants.defaultBlogTag, null, Constants.defaultGood,Constants.defaultNotGood, Constants.defaultVisited, Constants.defaultCommentsNum);
				blog.setContent("have no this blog !");
				injectorInfo.element("blogId", blogId).element("ip", Tools.getIPAddr(req)); 
			} else {
				blog = new Blog(blogInServer);
				String blogPath = Tools.getBlogPath(Tools.getProjectPath(), (blogInServer.getPath()) );
//				Log.log(blogPath);
				if(! Tools.isFileExists(blogPath) ) {
					blog.setContent("this blog already be deleted, maybe by adminstrator !");
				} else {
					if(! Constants.visitedCookieValue.equals(Tools.getCookieValByName(req.getCookies(), Tools.getVisitedCookieName(blog.getId()))) ) {
						BlogManager.addVisited(blogId);
						resp.addCookie(new Cookie(Tools.getVisitedCookieName(blog.getId()), Constants.visitedCookieValue));
					}
					
					// add try-catch in case of disk get fileContent while admin is revise blog !		2015.12.10
					String content = null;
					try {
						content = Tools.getContent(blogPath, Constants.DEFAULT_CHARSET);
					} catch (Exception e) {
						content = "file maybe be locked now, please try again later !";
					}
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

		req.setAttribute(Constants.result, res);
		if(injectorInfo.size() > 0) {
			Tools.log(this, injectorInfo.toString() );
		}
		injectorInfo = null;
	}
	
}
