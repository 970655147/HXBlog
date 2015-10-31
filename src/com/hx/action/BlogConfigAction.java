package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.util.Constants;
import com.hx.util.Log;
import com.hx.util.Tools;

// ��ȡblog���õ�action
public class BlogConfigAction extends HttpServlet {

	// ��ȡconfig.conf�е�����
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		PrintWriter out = resp.getWriter();
		String content = Tools.getContent(Tools.getPackagePath(Tools.getProjectPath(this), Constants.configPath), Tools.DEFAULT_CHARSET );
		out.write(content);
		out.close();
	}
	
}
