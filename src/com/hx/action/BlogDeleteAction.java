package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.bean.Blog;
import com.hx.bean.ResponseMsg;
import com.hx.business.BlogManager;
import com.hx.util.Constants;
import com.hx.util.Tools;

public class BlogDeleteAction extends HttpServlet {

	// 删除播客的业务
	// 获取传入的id, 确保其为整数
	// 确保用户登录
	// 获取blogId对应的blog, 确保其存在
		// 如果 blog对应的文件存在, 则删除对应的播客文件
		// 通知BlogManager删除对应过的blog
	// 返回 响应结果, 记录日志
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);

		Integer blogId = null;
		try {
			blogId = Integer.parseInt(req.getParameter("id") );
		} catch(Exception e) {
			blogId = null;
		}
		ResponseMsg respMsg = new ResponseMsg();
//		respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "sorry, delete blog is not a good children　!", Tools.getIPAddr(req) );
		if(Tools.validateObjectBeNull(req, blogId, "blogId", respMsg) ) {
			if(Tools.validateUserLogin(req, respMsg)) {
				Blog oldBLog = BlogManager.getBlog(blogId);
				
				if(Tools.validateBlog(req, oldBLog, respMsg) ) {
					if(Tools.isFileExists(Tools.getBlogPath(Tools.getProjectPath(), (oldBLog.getPath()) )) ) {	
						Tools.delete(Tools.getBlogPath(Tools.getProjectPath(), oldBLog.getPath()) );
					}
					BlogManager.deleteBlog(oldBLog);
					respMsg.set(Constants.respSucc, Constants.defaultResponseCode, Tools.getDeleteSuccMsg(oldBLog), null);
				}
			}
		}
		
		PrintWriter out = resp.getWriter();
		String respInfo = respMsg.toString();
		out.write(respInfo );
		Tools.log(this, respInfo);
		out.close();
	}
	
}
