package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.hx.util.Constants;
import com.hx.util.Tools;

// ��ȡblog���õ�action
public class BlogConfigAction extends HttpServlet {

	// �Ƿ��ǵ�һ�η���
	private static boolean isFirst = true;
	
	// ��ȡconfig.conf�е�����
	// ���configPath��Ӧ���ļ�������, ���ȡ�ڴ��л����config
		// ����  ֱ�ӻ�ȡ��Ӧ���ļ�������, ���浽defaultConfig, ֮��ȡ����, ��defaultConfig��ȡ
	// Ȼ�� �ж��Ƿ��¼
		// �����¼  ���ͷ�������, logout�İ�ť��Ϣ
		// ����  ����login��ť��Ϣ
	// ���� ��Ӧ���, ��¼��־
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		
		JSONObject config = null;
		if((! isFirst) || (! Tools.isFileExists(Tools.getPackagePath(Tools.getProjectPath(), Constants.configPath))) ) {
			config = JSONObject.fromObject(Constants.defaultConfig);
		} else {
			String content = Tools.getContent(Tools.getPackagePath(Tools.getProjectPath(), Constants.configPath), Tools.DEFAULT_CHARSET );
			Constants.defaultConfig = JSONObject.fromObject(content);
			config = JSONObject.fromObject(Constants.defaultConfig);
			isFirst = false;
		}
		boolean isLogin = Tools.isLogin(req);
		if(isLogin) {
			config.getJSONArray("quickLinks").add(Constants.publishBlogConf);
			config.getJSONArray("quickLinks").add(Constants.logoutBlogConf);
		} else {
			config.getJSONArray("quickLinks").add(Constants.loginBlogConf);
		}
		
		PrintWriter out = resp.getWriter();
		String res = config.toString();
		out.write(res);
		Tools.log(this, res);
		out.close();
	}
	
}
