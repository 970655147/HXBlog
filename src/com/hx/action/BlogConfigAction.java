package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.hx.util.Constants;
import com.hx.util.Log;
import com.hx.util.Tools;

// ��ȡblog���õ�action
public class BlogConfigAction extends HttpServlet {

	// ��ȡconfig.conf�е�����
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		String content = Tools.getContent(Tools.getPackagePath(Tools.getProjectPath(), Constants.configPath), Tools.DEFAULT_CHARSET );
		
		JSONObject config = JSONObject.fromObject(content);
		boolean isLogin = Tools.isLogin(req);
		if(isLogin) {
			config.getJSONArray("quickLinks").add(Constants.publishBlogConf);
		}
		
		PrintWriter out = resp.getWriter();
		String res = config.toString();
		out.write(res);
		Tools.log(this, res);
		out.close();
	}
	
}
