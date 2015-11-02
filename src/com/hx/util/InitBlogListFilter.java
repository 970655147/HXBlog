package com.hx.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.hx.action.BlogListAction;

// ≥ı ºªØBlogListAction
public class InitBlogListFilter implements Filter {

	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		BlogListAction.initIfNeeded((HttpServletRequest) req);
		chain.doFilter(req, resp);
	}

	public void init(FilterConfig config) throws ServletException {
		
	}

}
