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

	// ɾ�����͵�ҵ��
	// ��ȡ�����id, ȷ����Ϊ����
	// ȷ���û���¼
	// ��ȡblogId��Ӧ��blog, ȷ�������
		// ��� blog��Ӧ���ļ�����, ��ɾ����Ӧ�Ĳ����ļ�
		// ֪ͨBlogManagerɾ����Ӧ����blog
	// ���� ��Ӧ���, ��¼��־
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
//		respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "sorry, delete blog is not a good children��!", Tools.getIPAddr(req) );
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
