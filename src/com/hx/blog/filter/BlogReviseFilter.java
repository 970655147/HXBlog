/**
 * file name : BlogCheckCodeFilter.java
 * created at : 上午11:28:42 2016年8月5日
 * created by 970655147
 */

package com.hx.blog.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.hx.blog.bean.Blog;
import com.hx.blog.bean.ResponseMsg;
import com.hx.blog.bean.ValidateResult;
import com.hx.blog.business.BlogManager;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

public class BlogReviseFilter implements Filter {

	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
		ResponseMsg respMsg = new ResponseMsg();
		Integer id = Integer.parseInt((String) req.getAttribute(Constants.id) );
		String title = (String) req.getAttribute(Constants.title);
		String tags = (String) req.getAttribute(Constants.tags);
		String content = (String) req.getAttribute(Constants.content);
		
		boolean isValid = false;
		if(Tools.validateUserLogin(req, respMsg)) {
			if(Tools.validateCheckCode(req, respMsg)) {
				// fix "checkCode" in session's lifecycle [should be removed after checked !]		add at 2015.12.08
				Tools.removeAttrFromSession(req, Constants.checkCode);
				if(Tools.validateObjectBeNull(req, id, "blogId", respMsg) ) {
					if(Tools.validateTitle(req, title, "title", respMsg)) {
						if(Tools.validateTags(req, tags, respMsg)) {
							if(Tools.validateContent(req, content, respMsg) ) {
								Blog blogInServer = BlogManager.getBlog(id);
								// 对于修改这里, 需要使用副本 [因为 需要统计标签的增减]
								if(Tools.validateBlog(req, blogInServer, respMsg)) {
									isValid = true;
									req.setAttribute(Constants.blog, blogInServer);
									filterChain.doFilter(req, resp);
								}
							}
						}
					}
				}
			}
		}
		
		if(! isValid) {
			req.setAttribute(Constants.result, respMsg);
		}
		
		PrintWriter out = resp.getWriter();
		String respInfo = req.getAttribute(Constants.result).toString();
		out.write(respInfo );
		Tools.log(this, respInfo);
		out.close();
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		
	}

}
