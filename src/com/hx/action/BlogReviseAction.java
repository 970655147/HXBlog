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
import com.hx.business.BlogManager;
import com.hx.util.Constants;
import com.hx.util.Tools;

public class BlogReviseAction extends HttpServlet {

	// 修改播客
	// 确保用户登录, 校验校验码
	// 校验id, title, tags, content
	// 获取blogId对应的blog, 校验该blog
	// 判断是否更新了播客的名称
		// 如果没有, 直接更新元数据, 以及播客文件
		// 否则  更新播客文件之后, 进行重命名, 更新成现在的文件名字
		// 将blog交给BlogManager处理更新blog的业务
	// 返回 响应结果, 记录日志
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		
		ResponseMsg respMsg = new ResponseMsg();
		if(Tools.validateUserLogin(req, respMsg) ) {
			if(Tools.validateCheckCode(req, respMsg) ) {
				Integer id = null;
				try {
					id = Integer.parseInt(req.getParameter("id") );
				} catch(Exception e) {
					id = null;
				}
				String title = req.getParameter("title");
				String tags = req.getParameter("tags");
				String content = req.getParameter("content");
				if(Tools.validateObjectBeNull(req, id, "blogId", respMsg) ) {
					if(Tools.validateTitle(req, title, "title", respMsg)) {
						if(Tools.validateTags(req, tags, respMsg)) {
							if(Tools.validateContent(req, content, respMsg) ) {
								Blog newBlog = BlogManager.getBlog(id);
								// 对于修改这里, 不使用副本 [因为, 使不使用副本对于结果来说, 并没有影响]
								if(Tools.validateBlog(req, newBlog, respMsg)) {
									String oldBlogName = newBlog.getPath();
									boolean isChangeName = ! Tools.equalsIgnorecase(oldBlogName.trim(), title.trim() );
									
									Date now = new Date();
//									String createTime = Constants.createDateFormat.format(now );
									String blogName = Tools.getBlogFileName(Constants.dateFormat.format(now), title);	
									newBlog.set(id, title, blogName, tags, null);
									
									if(! isChangeName) {
										Tools.save(content, Tools.getBlogPath(Tools.getProjectPath(), oldBlogName) );			
									} else {
										Tools.save(content, Tools.getBlogPath(Tools.getProjectPath(), oldBlogName) );
										Tools.renameTo(Tools.getBlogPath(Tools.getProjectPath(req.getServletContext()), oldBlogName), Tools.getBlogPath(Tools.getProjectPath(), blogName) );
									}
									BlogManager.reviseBlog(newBlog);
									respMsg = new ResponseMsg(true, Constants.defaultResponseCode, Tools.getPostSuccMsg(newBlog), null);
								}
							}
						}
					}
				}
			}
		}
		
		PrintWriter out = resp.getWriter();
		String respInfo = respMsg.toString();
		out.write(respInfo );
		Tools.log(this, respInfo);
		out.close();
	}
	
	// 处理掉id为list的style标签 [这里先暂时这样处理, 之后再回来完善]
//	private String prepare(String revised) {
//		StringBuilder sb = new StringBuilder(revised.length());
//		String startStr = "<style id=\"list\"";
//		String endStr = "</style>";
//		int liStyleIdxBefore = revised.indexOf(startStr);
//		int liStyleIdxAfter = revised.indexOf(">", liStyleIdxBefore + startStr.length());
//		sb.append(revised.substring(0, liStyleIdxBefore) );
//		sb.append(revised.substring(revised.indexOf(endStr, liStyleIdxAfter)) );
//		
//		return sb.toString();
//	}
	
}
