package com.hx.blog.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.hx.blog.business.BlogManager;
import com.hx.blog.interf.BaseAction;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

// 获取blog配置的action
public class BlogListAction extends BaseAction {

	// 获取config.conf中的配置
	// 获取tag, 如果其为空, 其默认值为"all"
	// 获取该tag相关的所有blog, 以及所有的tag
	// 返回 响应结果, 记录日志		
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//		BlogManager.initIfNeeded();
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);		
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);
		
		int pageNo = 0;
		try {
			pageNo = Integer.parseInt((String) req.getAttribute(Constants.pageNo) );
		} catch (Exception e) {
			pageNo = 0;
		}
		if(pageNo == 0) {
			pageNo = 1;
		}
		
		String tag = (String) req.getAttribute(Constants.tag);
		if(tag == null) {
			tag = BlogManager.ALL;
		}
		JSONObject res = BlogManager.getResByTag(tag, pageNo);
		
		req.setAttribute(Constants.result, res);
		Tools.log(this, "getBlogList for tag : " + tag + ", pageNo : " + pageNo);
	}
	
}
