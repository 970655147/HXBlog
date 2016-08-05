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

import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

import net.sf.json.JSONObject;

public class BlogGetFilter implements Filter {

	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
		filterChain.doFilter(req, resp);
		
		PrintWriter out = resp.getWriter();
		JSONObject res = (JSONObject) req.getAttribute(Constants.result);
		out.write(res.toString() );
		// 只记录部分内容
		JSONObject logObj = JSONObject.fromObject(res.getJSONObject("blog")).element("content", Tools.compressBlogContent(res.getJSONObject("blog").optString("content")) ); 
		Tools.log(this, logObj.toString() );
		out.close();
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		
	}

}
