package com.hx.blog.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.hx.blog.interf.BaseAction;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

// ��ȡblog���õ�action
public class BlogConfigAction extends BaseAction {

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
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);
		
		JSONObject config = null;
		if((isFirst) && (Tools.isFileExists(Tools.getPackagePath(Tools.getProjectPath(), Constants.configPath))) ) {
			String content = Tools.getContent(Tools.getPackagePath(Tools.getProjectPath(), Constants.configPath), Constants.DEFAULT_CHARSET );
			Constants.defaultConfig = JSONObject.fromObject(content);
		}
		config = JSONObject.fromObject(Constants.defaultConfig);
		isFirst = false;
		boolean isLogin = Tools.isLogin(req);
		if(isLogin) {
			config.getJSONArray("quickLinks").add(Constants.publishBlogConf);
			config.getJSONArray("quickLinks").add(Constants.logoutBlogConf);
		} else {
			config.getJSONArray("quickLinks").add(Constants.loginBlogConf);
		}
		
		req.setAttribute(Constants.result, config);
	}
	
}