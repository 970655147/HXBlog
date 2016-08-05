/**
 * file name : BlogCheckCodeFilter.java
 * created at : ����11:28:42 2016��8��5��
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

import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

public class BlogListFilter implements Filter {

	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
		filterChain.doFilter(req, resp);
		
		PrintWriter out = resp.getWriter();
		String respInfo = req.getAttribute(Constants.result).toString();
		out.write(respInfo );
		// ����¼��־, �������ϴ�
		out.close();
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		
	}

}
