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

// 获取blog配置的action
public class BlogConfigAction extends HttpServlet {

	// 是否是第一次访问
	private static boolean isFirst = true;
	
	// 获取config.conf中的配置
	// 如果configPath对应的文件不存在, 则获取内存中缓存的config
		// 否则  直接获取对应的文件的内容, 缓存到defaultConfig, 之后取数据, 从defaultConfig中取
	// 然后 判断是否登录
		// 如果登录  则发送发布博客, logout的按钮信息
		// 否则  发布login按钮信息
	// 返回 响应结果, 记录日志
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
