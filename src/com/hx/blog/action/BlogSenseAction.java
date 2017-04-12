package com.hx.blog.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.blog.bean.Blog;
import com.hx.blog.bean.ResponseMsg;
import com.hx.blog.bean.ValidateResult;
import com.hx.blog.business.BlogManager;
import com.hx.blog.interf.BaseAction;
import com.hx.blog.interf.Validater;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

public class BlogSenseAction extends BaseAction {

	// 播客顶/ 踩处理的action
	// 获取blogId
	// 校验blogId, sense, 确保sense为Constants.senseGood / Constants.senseNotGood 二者之一
	// 校验blog
	// 如果没有sense过  则根据sense更新对应的good / notGood, 并且"sense到"当前sense
		// 否则  如果sense为保存的sense, 则取消"sense当前"sense
		// 否则 取消之前缓存sense, 并且"sense到"当前sense
	// 并将其该blog加入visitedSensedBlogList, 待刷新到数据库
	// 返回 响应结果, 记录日志
	// 注 : response.addCookie, 如果存在给定的名字的cookie的话, 后者会替换掉前者.. 这是我担心的地方, 但是经过这样一个测试, 就不用担心鸟
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);
		
		ResponseMsg respMsg = new ResponseMsg();
		Integer blogId = Integer.parseInt((String) req.getAttribute(Constants.blogId) );
		String sense = (String) req.getAttribute(Constants.sense);

		Blog blog = (Blog) req.getAttribute(Constants.blog);
		BlogManager.addVisitSense(blog);
		Cookie sensedToBlog = Tools.getCookieByName(req.getCookies(), Tools.getSensedCookieName(blogId) );
		boolean isSensed = (sensedToBlog != null) && (! Constants.defaultCookieValue.equals(sensedToBlog.getValue()) );
		if(! isSensed) {
			respMsg.set(Constants.respSucc, Constants.defaultResponseCode, "sense to : " + sense, Tools.getIPAddr(req) );
			if(sense.equals(Constants.senseGood) ) {
				blog.incGood();
			} else {
				blog.incNotGood();
			}
			resp.addCookie(new Cookie(Tools.getSensedCookieName(blog.getId() ), sense) );
		} else {
			sensedToBlog.setMaxAge(-1);
			if(sense.equals(Constants.senseGood) ) {
				if(! sensedToBlog.getValue().equals(sense) ) {
					respMsg.set(Constants.respSucc, Constants.defaultResponseCode, "sense to : " + sense, Tools.getIPAddr(req) );
					blog.incGood();
					blog.decNotGood();
					resp.addCookie(new Cookie(Tools.getSensedCookieName(blog.getId() ), Constants.senseGood) );
				} else {
					respMsg.set(Constants.respSucc, Constants.defaultResponseCode, "cancel sense to : " + sense, Tools.getIPAddr(req) );
					blog.decGood();
					resp.addCookie(new Cookie(Tools.getSensedCookieName(blog.getId() ), Constants.defaultCookieValue) );
				}
			} else {
				if(! sensedToBlog.getValue().equals(sense) ) {
					respMsg.set(Constants.respSucc, Constants.defaultResponseCode, "sense to : " + sense, Tools.getIPAddr(req) );
					blog.incNotGood();
					blog.decGood();
					resp.addCookie(new Cookie(Tools.getSensedCookieName(blog.getId() ), Constants.senseNotGood) );
				} else {
					respMsg.set(Constants.respSucc, Constants.defaultResponseCode, "cancel sense to : " + sense, Tools.getIPAddr(req) );
					blog.decNotGood();
					resp.addCookie(new Cookie(Tools.getSensedCookieName(blog.getId() ), Constants.defaultCookieValue) );
				}
			}
		}
		
		req.setAttribute(Constants.result, respMsg);
	}

}
