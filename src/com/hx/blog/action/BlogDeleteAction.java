package com.hx.blog.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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

public class BlogDeleteAction extends BaseAction {

	// ɾ�����͵�ҵ��
	// ��ȡ�����id, ȷ����Ϊ����
	// ȷ���û���¼
	// ��ȡblogId��Ӧ��blog, ȷ�������
		// ��� blog��Ӧ���ļ�����, ��ɾ����Ӧ�Ĳ����ļ�
		// ֪ͨBlogManagerɾ����Ӧ����blog
	// ���� ��Ӧ���, ��¼��־
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);

		ResponseMsg respMsg = new ResponseMsg();
		Integer blogId = Integer.parseInt((String) req.getAttribute(Constants.id) );
		Blog oldBlog = (Blog) req.getAttribute(Constants.oldBlog);
		if(Tools.isFileExists(Tools.getBlogPath(Tools.getProjectPath(), (oldBlog.getPath()) )) ) {
			Tools.delete(Tools.getBlogPath(Tools.getProjectPath(), oldBlog.getPath()) );
		}
		BlogManager.deleteBlog(oldBlog);
		respMsg.set(Constants.respSucc, Constants.defaultResponseCode, Tools.getDeleteSuccMsg(oldBlog), null);
		
		req.setAttribute(Constants.result, respMsg);
	}
	
}
