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
import com.hx.blog.bean.ResponseMsg;
import com.hx.blog.business.BlogManager;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

public class BlogSenseFilter implements Filter {

	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
		ResponseMsg respMsg = new ResponseMsg();
		Integer blogId = Integer.parseInt((String) req.getAttribute(Constants.blogId) );
		String sense = (String) req.getAttribute(Constants.sense);
		
		boolean isValid = false;
		if(Tools.validateObjectBeNull(req, blogId, "blogId", respMsg)) {
			if(Tools.validateStringBeNull(req, sense, "sense", respMsg) ) {
				respMsg.setOthers(sense);
				Blog blog = BlogManager.getBlog(blogId);
				if((Tools.equalsIgnorecase(sense, Constants.senseGood)) || (Tools.equalsIgnorecase(sense, Constants.senseNotGood)) ) {
					if(Tools.validateBlog(req, blog, respMsg) ) {
						isValid = true;
						req.setAttribute(Constants.blog, blog);
						filterChain.doFilter(req, resp);
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
