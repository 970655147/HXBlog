package com.hx.blog.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.hx.blog.business.BlogManager;

// ÿһ��action�Ĺ����� [δ��]
public class InitBlogListFilter implements Filter {

	public void destroy() {
		
	}

	// ÿһ��action����֮ǰ, ��ʼ��BlogManager�� [�����ʼ����, ������]
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		BlogManager.initIfNeeded();
		chain.doFilter(req, resp);
	}

	public void init(FilterConfig config) throws ServletException {
		
	}

}
