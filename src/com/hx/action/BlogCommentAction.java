package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.hx.bean.Comment;
import com.hx.bean.ResponseMsg;
import com.hx.bean.UserInfo;
import com.hx.business.CommentManager;
import com.hx.util.Constants;
import com.hx.util.Tools;

public class BlogCommentAction extends HttpServlet {
	
	// 处理评论相关业务
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		
		ResponseMsg respMsg = new ResponseMsg();
		Integer blogIdx = Integer.parseInt(req.getParameter("blogIdx") );
		Integer floorIdx = Integer.parseInt(req.getParameter("floorIdx") );
		UserInfo userInfo = new UserInfo(req.getParameter("userName"), req.getParameter("email"), Integer.parseInt(req.getParameter("imageIdx")), Tools.getPrivilege(Tools.isLogin(req)) );
		req.getSession().setAttribute(Constants.preferInfo, userInfo);
		String to = req.getParameter("to");
		String commentBody = req.getParameter("comment");
		Comment comment = new Comment(blogIdx, floorIdx, Constants.defaultCommentIdx, userInfo, Constants.dateFormat.format(new Date()), to, commentBody);
		try {
			if(! Tools.isReply(commentBody) ) {
				CommentManager.updateCommentIdx(comment);
			} else {
				comment.setComment(Tools.getReplyComment(commentBody));
				CommentManager.updateFloorIdx(comment);
			}
			CommentManager.addComment(blogIdx, comment);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		respMsg.set(true, Constants.defaultResponseCode, "comment success !", Tools.getIPAddr(req) );
		
		JSONObject res = new JSONObject();
		res.element("respMsg", respMsg.toString() );
		res.element("comment", comment.toString() );
		
		PrintWriter out = resp.getWriter();
		String respInfo = res.toString();
		out.write(respInfo );
		Tools.log(this, respInfo);
		out.close();
	}
	
}
