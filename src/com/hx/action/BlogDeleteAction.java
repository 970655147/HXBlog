package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.bean.Blog;
import com.hx.bean.ResponseMsg;
import com.hx.bean.ValidateResult;
import com.hx.business.BlogManager;
import com.hx.interf.BaseAction;
import com.hx.interf.Validater;
import com.hx.util.Constants;
import com.hx.util.Tools;

public class BlogDeleteAction extends BaseAction {

	// 删除播客的业务
	// 获取传入的id, 确保其为整数
	// 确保用户登录
	// 获取blogId对应的blog, 确保其存在
		// 如果 blog对应的文件存在, 则删除对应的播客文件
		// 通知BlogManager删除对应过的blog
	// 返回 响应结果, 记录日志
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);

		Integer blogId = null;
		try {
			blogId = Integer.parseInt(req.getParameter("id") );
		} catch(Exception e) {
			blogId = null;
		}
		ResponseMsg respMsg = new ResponseMsg();
		ValidateResult vRes = validater.validate(req, respMsg, blogId);
		if(vRes.isSucc) {
			Blog oldBlog = (Blog) vRes.attachments[0];
			if(Tools.isFileExists(Tools.getBlogPath(Tools.getProjectPath(), (oldBlog.getPath()) )) ) {	
				Tools.delete(Tools.getBlogPath(Tools.getProjectPath(), oldBlog.getPath()) );
			}
			BlogManager.deleteBlog(oldBlog);
			respMsg.set(Constants.respSucc, Constants.defaultResponseCode, Tools.getDeleteSuccMsg(oldBlog), null);
		}
		
		PrintWriter out = resp.getWriter();
		String respInfo = respMsg.toString();
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
				Integer blogId = (Integer) others[0];
//				respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "sorry, delete blog is not a good children　!", Tools.getIPAddr(req) );
				if(Tools.validateObjectBeNull(req, blogId, "blogId", respMsg) ) {
					if(Tools.validateUserLogin(req, respMsg)) {
						Blog oldBlog = BlogManager.getBlog(blogId);
						if(Tools.validateBlog(req, oldBlog, respMsg) ) {
							return new ValidateResult(true, new Object[]{oldBlog } );
						}
					}
				}
				
				return Constants.validateResultFalse;
			}
		};
	}
	
}
