/**
 * file name : XSSFilter.java
 * created at : 10:25:50 2016-11-12
 * created by 970655147
 */

package com.hx.blog.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * XSSFilter
 * 
 * @author 970655147
 */
public class XSSFilter implements Filter {

	// XSSFilter排除在外的url[请求的contextPath的后缀作为判定条件]
	private String[] excludeUrls;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String excludeUrlStr = filterConfig.getInitParameter("excludeUrls");
		this.excludeUrls = excludeUrlStr.split("\\s*,\\s*");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(! (request instanceof HttpServletRequest) ) {
			chain.doFilter(request, response);
			return ;
		}
		
		chain.doFilter(new XSSServletRequestWrapper((HttpServletRequest) request, excludeUrls), response);
	}

	@Override
	public void destroy() {
		
	}
	
	// XSSServletRequestWrapper
	static class XSSServletRequestWrapper extends HttpServletRequestWrapper {
		boolean isExclude = false;
		
		public XSSServletRequestWrapper(HttpServletRequest request, String[] excludeUrls) {
			super(request);
			this.isExclude = isExclude(request.getRequestURI(), excludeUrls);
		}

		@Override
		public String getHeader(String name) {
			String value = super.getHeader(name);
			return cleanXSS(value);
		}
		@Override
		public String getParameter(String name) {
			String value = super.getParameter(name);
			return cleanXSS(value);
		}
		@Override
		public String[] getParameterValues(String name) {
			String[] vals = super.getParameterValues(name);
			// <code>null</code> if the parameter does not exist.
			if(vals == null) {
				return vals;
			}
			
			for(int i=0; i<vals.length; i++) {
				vals[i] = cleanXSS(vals[i]);
			}
			return vals;
		}

		/**
		 * @Name: cleanXSS 
		 * @Description: 对于没有排除在外的url的参数, 转义需要转义的符号
		 * @param value
		 * @return  
		 * @Create at 2016-11-12 10:55:31 by '970655147'
		 */
		private String cleanXSS(String value) {
			if(isExclude || value == null) {
				return value;
			}
			
			return value
//					.replaceAll("&", "&amp;")
					.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		}

		/**
		 * @Name: isExclude 
		 * @Description: 判定当前请求的uri是否需要被排除
		 * @param requestUri
		 * @return  
		 * @Create at 2016-11-12 10:50:34 by '970655147'
		 */
		private boolean isExclude(String requestUri, String[] excludeUrls) {
			for(String excludeUrl : excludeUrls) {
				if(requestUri.endsWith(excludeUrl) ) {
					return true;
				}
			}
			
			return false;
		}
		
	}
	
}
