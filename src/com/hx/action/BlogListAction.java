package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.hx.business.BlogManager;
import com.hx.util.Constants;
import com.hx.util.Tools;

// 获取blog配置的action
public class BlogListAction extends HttpServlet {

	// 获取config.conf中的配置
	// 获取tag, 如果其为空, 其默认值为"all"
	// 获取该tag相关的所有blog, 以及所有的tag
	// 返回 响应结果, 记录日志		
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//		BlogManager.initIfNeeded();
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);		
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);
		
		String tag = req.getParameter("tag");
		int pageNo = 0;
		try {
			pageNo = Integer.parseInt(req.getParameter("pageNo") );
		} catch (Exception e) {
			pageNo = 0;
		}
		if(pageNo == 0) {
			pageNo = 1;
		}
		
		if(tag == null) {
			tag = BlogManager.ALL;
		}
		JSONObject res = BlogManager.getResByTag(tag, pageNo);
		
		PrintWriter out = resp.getWriter();
		String respInfo = res.toString();
		out.write(respInfo );
		Tools.log(this, respInfo);
		out.close();
	}
	
}
