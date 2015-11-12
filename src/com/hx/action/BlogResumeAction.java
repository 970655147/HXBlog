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

// 获取blog配置的action
public class BlogResumeAction extends HttpServlet {
	
	// 是否是第一次访问
	private static boolean isFirst = true;
	
	// 获取resume.conf中的配置
	// 由于简历的内容比较多, 所以写日志就不写内容了
	// 如果resumePath对应的文件不存在, 则获取内存中缓存的config
		// 否则  直接获取对应的文件的内容, 缓存到defaultConfig, 之后取数据, 从defaultConfig中取
	// 返回 响应结果, 记录日志	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);

		String content = null;
		String logInfo = null;
		if((! isFirst) || (! Tools.isFileExists(Tools.getPackagePath(Tools.getProjectPath(), Constants.configPath))) ) {
			content = Constants.defaultResume.toString();
			logInfo = "your resume be seen by : " + Tools.getIPAddr(req) + ", but read from defaultResume !";
		} else {
			content = Tools.getContent(Tools.getPackagePath(Tools.getProjectPath(), Constants.resumePath), Constants.DEFAULT_CHARSET );
			Constants.defaultResume = JSONObject.fromObject(content);
			logInfo = "your resume be seen by : " + Tools.getIPAddr(req);
		}
		isFirst = false;
		
		PrintWriter out = resp.getWriter();
		out.write(content);
		Tools.log(this, logInfo);
		out.close();
	}
	
}
