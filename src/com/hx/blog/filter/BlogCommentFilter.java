/**
 * file name : BlogCheckCodeFilter.java
 * created at : 上午11:28:42 2016年8月5日
 * created by 970655147
 */

package com.hx.blog.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.hx.blog.bean.Blog;
import com.hx.blog.bean.Comment;
import com.hx.blog.bean.ResponseMsg;
import com.hx.blog.bean.UserInfo;
import com.hx.blog.business.BlogManager;
import com.hx.blog.business.CommentManager;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

import net.sf.json.JSONObject;

public class BlogCommentFilter implements Filter {

	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
		ResponseMsg respMsg = new ResponseMsg();
		Integer blogIdx = null;
		Integer floorIdx = null;
		Integer imageIdx = null;
		try {
			blogIdx = Integer.parseInt((String) req.getAttribute(Constants.blogIdx) );
			floorIdx = Integer.parseInt((String) req.getAttribute(Constants.floorIdx) );
			imageIdx = Integer.parseInt((String) req.getAttribute(Constants.imageIdx) );
		} catch (Exception e) {
			blogIdx = null;
		}
		String to = (String) req.getAttribute(Constants.to);
		if(Tools.isEmpty(to)) {
			to = Constants.defaultTo;
		}
		String commentBody = (String) req.getAttribute(Constants.comment);
		Comment comment = null;
		
		boolean isValid = false;
		if(Tools.validateObjectBeNull(req, blogIdx, "blogIdx", respMsg) ) {
			if(Tools.validateObjectBeNull(req, floorIdx, "floorIdx", respMsg) ) {
				if(Tools.validateObjectBeNull(req, imageIdx, "imageIdx", respMsg) ) {
					UserInfo userInfo = new UserInfo((String) req.getAttribute("userName"), (String) req.getAttribute("email"), imageIdx, Tools.getPrivilege(Tools.isLogin(req)) );
					if(Tools.validateTitle(req, to, "to's userName", respMsg) ) {					
						if(Tools.validateUserInfo(req, userInfo, respMsg) ) {
							if(Tools.validateCommentBody(req, commentBody, respMsg) ) {
								Blog blogInServer = BlogManager.getBlog(blogIdx);
								if(Tools.validateBlog(req, blogInServer, respMsg)) {
//									if(validateFloorIdx(req, blogIdx, floorIdx, respMsg) ) {
										isValid = true;
										req.setAttribute(Constants.userInfo, userInfo);
										filterChain.doFilter(req, resp);
//									}
								}
							}
						}
					}
				}
			}
		}
		
		if(! isValid) {
			JSONObject res = new JSONObject();
			res.element(Constants.respMsg, respMsg.toString() );
			res.element(Constants.comment, String.valueOf(comment) );
			req.setAttribute(Constants.result, res);
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

//	// 校验floorIdx [检查了一下, 可以不用校验floorIdx..] 			add at 2016.08.25
//	private boolean validateFloorIdx(ServletRequest req, int blogIdx, int floorIdx, ResponseMsg respMsg) {
//		List<List<Comment>> comments = CommentManager.getCommentByBlogId(blogIdx);
//		if((floorIdx<0) || (floorIdx>comments.size()) ) {
//			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "floorIdx not valid　!", Tools.getIPAddr(req) );
//			return false;
//		}
//		
//		return true;
//	}
	
}
