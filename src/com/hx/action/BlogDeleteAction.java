package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.bean.Blog;
import com.hx.bean.ResponseMsg;
import com.hx.util.Constants;
import com.hx.util.Log;
import com.hx.util.Tools;

public class BlogDeleteAction extends HttpServlet {

	// �޸Ĳ���
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Integer blogId = Integer.parseInt(req.getParameter("id") );
		Blog oldBLog = BlogListAction.getBlog(blogId);
		
		Tools.delete(Tools.getBlogPath(Tools.getProjectPath(), oldBLog.getPath()) );
		BlogListAction.deleteBlog(oldBLog, req.getServletContext());
		
		// -----------------------------------------
		ResponseMsg respMsg = new ResponseMsg(true, Constants.defaultResponseCode, Tools.getDeleteSuccMsg(oldBLog));
		
		PrintWriter out = resp.getWriter();
		String respInfo = respMsg.toString();
		out.write(respInfo );
		Tools.log(this, respInfo);
		out.close();
	}
	
}