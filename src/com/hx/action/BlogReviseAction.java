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

	// �޸Ĳ���
	// ȷ���û���¼, У��У����
	// У��id, title, tags, content
	// ��ȡblogId��Ӧ��blog, У���blog
	// �ж��Ƿ�����˲��͵�����
		// ���û��, ֱ�Ӹ���Ԫ����, �Լ������ļ�
		// ����  ���²����ļ�֮��, ����������, ���³����ڵ��ļ�����
		// ��blog����BlogManager�������blog��ҵ��
	// ���� ��Ӧ���, ��¼��־
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);
		
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
								Blog blogInServer = BlogManager.getBlog(id);
								// �����޸�����, ��Ҫʹ�ø��� [��Ϊ ��Ҫͳ�Ʊ�ǩ������]
								if(Tools.validateBlog(req, blogInServer, respMsg)) {
									Blog newBlog = new Blog(blogInServer);
									String oldBlogName = blogInServer.getPath();
									boolean isChangeName = ! Tools.equalsIgnorecase(oldBlogName.trim(), title.trim() );
									
									Date now = new Date();
//									String createTime = Constants.createDateFormat.format(now );
									String blogName = Tools.getBlogFileName(Constants.dateFormat.format(now), title);	
									newBlog.set(id, title, blogName, tags, null);
									
									Tools.save(content, Tools.getBlogPath(Tools.getProjectPath(), oldBlogName) );			
									if(isChangeName) {
										Tools.renameTo(Tools.getBlogPath(Tools.getProjectPath(req.getServletContext()), oldBlogName), Tools.getBlogPath(Tools.getProjectPath(), blogName) );
									}
									BlogManager.reviseBlog(newBlog);
									respMsg = new ResponseMsg(Constants.respSucc, Constants.defaultResponseCode, Tools.getPostSuccMsg(newBlog), null);
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
	
	// �����idΪlist��style��ǩ [��������ʱ��������, ֮���ٻ�������]
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
