package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.hx.bean.Blog;
import com.hx.bean.Comment;
import com.hx.bean.ResponseMsg;
import com.hx.bean.UserInfo;
import com.hx.bean.ValidateResult;
import com.hx.business.BlogManager;
import com.hx.business.CommentManager;
import com.hx.interf.BaseAction;
import com.hx.interf.Validater;
import com.hx.util.Constants;
import com.hx.util.Tools;

public class BlogCommentAction extends BaseAction {
	
	// 处理评论相关业务
	// 获取blogIdx, floorIdx, imageIdx  如果发生异常, 将imageIdx置空 [必然过不了校验]
	// 校验blogIdx, floorIdx, imageIdx, userInfo, commentBody
	// 再校验这个播客是否存在
	// 校验通过后, 设置用户的userInfo
		// 如果comment是新的楼层信息, 则确定他的楼层数[index]
			// 否则  便是回复, 确定它在该楼层的回复数[index]
		// 将comment信息添加到CommentManager中
	// 返回 响应结果, 记录日志
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);
		
		ResponseMsg respMsg = new ResponseMsg();
		Integer blogIdx = null;
		Integer floorIdx = null;
		Integer imageIdx = null;
		try {
			blogIdx = Integer.parseInt((String) req.getAttribute("blogIdx") );
			floorIdx = Integer.parseInt((String) req.getAttribute("floorIdx") );
			imageIdx = Integer.parseInt((String) req.getAttribute("imageIdx") );
		} catch (Exception e) {
			blogIdx = null;
		}
		String to = (String) req.getAttribute("to");
		if(Tools.isEmpty(to)) {
			to = Constants.defaultTo;
		}
		String commentBody = (String) req.getAttribute("comment");
		Comment comment = null;
		ValidateResult vRes = validater.validate(req, respMsg, blogIdx, floorIdx, imageIdx, to, commentBody);
		if(vRes.isSucc) {
			UserInfo userInfo = (UserInfo) vRes.attachments[0];
			req.getSession().setAttribute(Constants.preferInfo, userInfo);
			comment = new Comment(blogIdx, floorIdx, Constants.defaultCommentIdx, userInfo, Constants.createDateFormat.format(new Date()), to, Tools.replaceCommentBody(commentBody, Constants.scriptCharacterMap) );
			try {
				CommentManager.addComment(blogIdx, comment);
				
				if(! Tools.isReply(commentBody) ) {
					CommentManager.updateFloorIdx(comment);
					BlogManager.addComment(blogIdx);
				} else {
					comment.setComment(Tools.getReplyComment(commentBody));
					CommentManager.updateCommentIdx(comment);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			respMsg.set(Constants.respSucc, Constants.defaultResponseCode, "comment success !", Tools.getIPAddr(req) );
		}
		
		JSONObject res = new JSONObject();
		res.element("respMsg", respMsg.toString() );
		res.element("comment", String.valueOf(comment) );
		
		PrintWriter out = resp.getWriter();
		String respInfo = res.toString();
		out.write(respInfo );
		Tools.log(this, respInfo);
		out.close();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.validater = new Validater() {
			@Override
			public ValidateResult validate(HttpServletRequest req, ResponseMsg respMsg, Object... others) {
				Integer blogIdx = (Integer) others[0];
				Integer floorIdx = (Integer) others[1];
				Integer imageIdx = (Integer) others[2];
				String to = (String) others[3];
				String commentBody = (String) others[4];
				if(Tools.validateObjectBeNull(req, blogIdx, "blogIdx", respMsg) ) {
					if(Tools.validateObjectBeNull(req, floorIdx, "floorIdx", respMsg) ) {
						if(Tools.validateObjectBeNull(req, imageIdx, "imageIdx", respMsg) ) {
							UserInfo userInfo = new UserInfo((String) req.getAttribute("userName"), (String) req.getAttribute("email"), imageIdx, Tools.getPrivilege(Tools.isLogin(req)) );
							if(Tools.validateTitle(req, to, "to's userName", respMsg) ) {					
								if(Tools.validateUserInfo(req, userInfo, respMsg) ) {
									if(Tools.validateCommentBody(req, commentBody, respMsg) ) {
										Blog blogInServer = BlogManager.getBlog(blogIdx);
										if(Tools.validateBlog(req, blogInServer, respMsg)) {
											return new ValidateResult(true, new Object[]{userInfo } );
										}
									}
								}
							}
						}
					}
				}
				
				return Constants.validateResultFalse;
			}
		};
	}
	
}
