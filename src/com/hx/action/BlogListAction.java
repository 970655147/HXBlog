package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.hx.business.BlogManager;
import com.hx.util.Constants;
import com.hx.util.Tools;

// ��ȡblog���õ�action
public class BlogListAction extends HttpServlet {

	// ��ȡconfig.conf�е�����
	// ��ȡtag, �����Ϊ��, ��Ĭ��ֵΪ"all"
	// ��ȡ��tag��ص�����blog, �Լ����е�tag
	// ���� ��Ӧ���, ��¼��־		
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//		BlogManager.initIfNeeded();
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);		
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);
		
		String tag = req.getParameter("tag");
		int pageNo = 0;
		try {
			pageNo = Integer.parseInt(req.getParameter("pageNo") );
		} catch (Exception e) {
			pageNo = 0;
		}
		if(pageNo == 0) {
			pageNo = 1;
		}
		
		if(tag == null) {
			tag = BlogManager.ALL;
		}
		JSONObject res = BlogManager.getResByTag(tag, pageNo);
		
		PrintWriter out = resp.getWriter();
		String respInfo = res.toString();
		out.write(respInfo );
		Tools.log(this, respInfo);
		out.close();
	}
	
}
