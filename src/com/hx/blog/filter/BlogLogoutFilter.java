/**
 * file name : BlogCheckCodeFilter.java
 * created at : ����11:28:42 2016��8��5��
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

public class BlogLogoutFilter implements Filter {

	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
		filterChain.doFilter(req, resp);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		
	}

}
