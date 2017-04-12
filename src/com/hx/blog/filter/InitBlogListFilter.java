package com.hx.blog.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.hx.blog.business.BlogManager;

// 每一个action的过滤器 [未用]
public class InitBlogListFilter implements Filter {

	public void destroy() {
		
	}

	// 每一个action调用之前, 初始化BlogManager的 [如果初始化了, 则不用了]
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		BlogManager.initIfNeeded();
		chain.doFilter(req, resp);
	}

	public void init(FilterConfig config) throws ServletException {
		
	}

}
