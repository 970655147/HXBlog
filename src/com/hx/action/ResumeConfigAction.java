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

// 获取blog配置的action
public class ResumeConfigAction extends HttpServlet {

	// 获取resume.conf中的配置
	// 由于简历的内容比较多, 所以写日志就不写内容了
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Tools.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);

		String content = null;
		String logInfo = null;
		if(Tools.isFileExists(Tools.getPackagePath(Tools.getProjectPath(), Constants.configPath)) ) {
			content = Constants.defaultResume;
			logInfo = "your resume be seen by : " + Tools.getIPAddr(req) + ", but resultFile not exists !";
		} else {
			content = Tools.getContent(Tools.getPackagePath(Tools.getProjectPath(), Constants.resumePath), Tools.DEFAULT_CHARSET );
			logInfo = "your resume be seen by : " + Tools.getIPAddr(req);
		}
		
		PrintWriter out = resp.getWriter();
		out.write(content);
		Tools.log(this, logInfo);
		out.close();
	}
	
}
