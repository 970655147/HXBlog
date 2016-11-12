package com.hx.blog.action;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.hx.blog.bean.Blog;
import com.hx.blog.bean.ResponseMsg;
import com.hx.blog.business.BlogManager;
import com.hx.blog.interf.BaseAction;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

public class BlogPublishAction extends BaseAction {

	// 修改播客
	// 确保用户登录, 校验校验码
	// 校验title, tags, content
	// 创建Blog对象, 持久化blog文件, 将blog保存到BlogManager中
	// 返回 响应结果, 记录日志		
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);
		
//		ResponseMsg respMsg = new ResponseMsg();
//		ValidateResult vRes = validater.validate(req, respMsg);
//		if(vRes.isSucc) {
//			String title = (String) vRes.attachments[0];
//			String tags = (String) vRes.attachments[1];
//			String content = (String) vRes.attachments[2];
			String title = (String) req.getAttribute(Constants.title);
			String tags = (String) req.getAttribute(Constants.tags);
			String content = (String) req.getAttribute(Constants.content);
			
			// 预处理content
			content = Tools.prepareContent(content);
			
			Date now = new Date();
			String createTime = Constants.createDateFormat.format(now );
			String blogName = Tools.getBlogFileName(Constants.dateFormat.format(now), title);
			
			Blog newBlog = new Blog(BlogManager.nextBlogId(), title, blogName, tags, createTime, new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0) );
			Tools.save(content, Tools.getBlogPath(Tools.getProjectPath(), blogName) );
			BlogManager.publishBlog(newBlog);
			ResponseMsg respMsg = new ResponseMsg(Constants.respSucc, Constants.defaultResponseCode, Tools.getPostSuccMsg(newBlog), null);
//		}
		
		req.setAttribute(Constants.result, respMsg);
	}
	
//	@Override
//	public void init(ServletConfig config) throws ServletException {
//		super.init(config);
//		this.validater = new Validater() {
//			@Override
//			public ValidateResult validate(HttpServletRequest req, ResponseMsg respMsg, Object... others) {
//				if(Tools.validateUserLogin(req, respMsg)) {
//					if(Tools.validateCheckCode(req, respMsg)) {
//						// fix "checkCode" in session's lifecycle [should be removed after checked !]		add at 2015.12.08
//						Tools.removeAttrFromSession(req, Constants.checkCode);
//						String title = Tools.replaceMultiSpacesAsOne((String) req.getAttribute(Constants.title) );
//						String tags = Tools.replaceMultiSpacesAsOne((String) req.getAttribute(Constants.tags) );
//						String content = (String) req.getAttribute(Constants.content);
//						if(Tools.validateTitle(req, title, "title", respMsg)) {
//							if(Tools.validateTags(req, tags, respMsg)) {
//								if(Tools.validateContent(req, content, respMsg) ) {
//									return new ValidateResult(true, new Object[]{title, tags, content } );
//								}
//							}
//						}
//					}
//				}
//				
//				return Constants.validateResultFalse;
//			}
//		};
//	}
	
	
}
