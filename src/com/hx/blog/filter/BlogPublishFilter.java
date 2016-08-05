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

import com.hx.blog.bean.ResponseMsg;
import com.hx.blog.bean.ValidateResult;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

public class BlogPublishFilter implements Filter {

	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
		ResponseMsg respMsg = new ResponseMsg();
		
		boolean isValid = false;
		if(Tools.validateUserLogin(req, respMsg)) {
			if(Tools.validateCheckCode(req, respMsg)) {
				// fix "checkCode" in session's lifecycle [should be removed after checked !]		add at 2015.12.08
				Tools.removeAttrFromSession(req, Constants.checkCode);
				String title = Tools.replaceMultiSpacesAsOne((String) req.getAttribute(Constants.title) );
				String tags = Tools.replaceMultiSpacesAsOne((String) req.getAttribute(Constants.tags) );
				String content = (String) req.getAttribute(Constants.content);
				if(Tools.validateTitle(req, title, "title", respMsg)) {
					if(Tools.validateTags(req, tags, respMsg)) {
						if(Tools.validateContent(req, content, respMsg) ) {
							isValid = true;
							req.setAttribute(Constants.title, title);
							req.setAttribute(Constants.tags, tags);
							req.setAttribute(Constants.content, content);
							filterChain.doFilter(req, resp);
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
