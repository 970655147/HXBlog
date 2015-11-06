package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.bean.ResponseMsg;
import com.hx.bean.UserInfo;
import com.hx.util.Constants;
import com.hx.util.Log;
import com.hx.util.Tools;

public class BlogCommentAction extends HttpServlet {
	
	// 处理评论相关业务
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		
		ResponseMsg responseMsg = new ResponseMsg();
		UserInfo uInfo = new UserInfo(req.getParameter("userName"), req.getParameter("email"), Integer.parseInt(req.getParameter("imageIdx")) );
		String comment = req.getParameter("comment");
		Log.log(uInfo.toString() );
		Log.log(comment );
		responseMsg.set(true, Constants.defaultResponseCode, "comment success !", Tools.getIPAddr(req) );
		
		PrintWriter out = resp.getWriter();
		String respInfo = responseMsg.toString();
		out.write(respInfo );
		Tools.log(this, respInfo);
		out.close();
	}
	
}
