/**
 * file name : EncodingFilter.java
 * created at : 下午9:29:10 2016年8月3日
 * created by 970655147
 */

package com.hx.blog.filter;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.hx.blog.util.Tools;

// 解决编码问题的filter[当前项目的编码为gbk]
public class EncodingFilter implements Filter {

	// 目标编码[iso8859-1 -> dstEncoding]
	private String encodingInClient;
	private String decodingInServer;
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc) throws IOException, ServletException {
		// set default param
		Enumeration<String> paramNames = req.getParameterNames();
		while(paramNames.hasMoreElements() ) {
			String paramName = paramNames.nextElement();
			req.setAttribute(paramName, req.getParameter(paramName) );
		}
		
		if(! ("get".equalsIgnoreCase(((HttpServletRequest) req).getMethod())) ) {
			fc.doFilter(req, resp);
			return ;
		}
		try {
			// update encoding
			if(( !Tools.isEmpty(encodingInClient)) && (! Tools.isEmpty(decodingInServer)) ) {
				paramNames = req.getParameterNames();
				while(paramNames.hasMoreElements() ) {
					String paramName = paramNames.nextElement();
					req.setAttribute(paramName, new String(req.getParameter(paramName).getBytes(decodingInServer), encodingInClient) );
				}
			}
		} finally {
			fc.doFilter(req, resp);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		encodingInClient = filterConfig.getInitParameter("encodingInClient");
		decodingInServer = filterConfig.getInitParameter("decodingInServer");
	}

}
