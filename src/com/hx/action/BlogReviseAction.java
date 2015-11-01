package com.hx.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.util.Log;
import com.hx.util.Tools;

public class BlogReviseAction extends HttpServlet {

	// 修改播客
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String revisedContent = req.getParameter("revised");
		String blogName = req.getParameter("path") + Tools.HTML;
		Tools.save(prepare(revisedContent), Tools.getBlogPath(Tools.getProjectPath(this), blogName) );
//		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
//		String blogPath = Tools.getBlogPath(Tools.getProjectPath(this), blogId);
//		PrintWriter out = resp.getWriter();
//		String ulEditorIdx = Tools.getProjectPath(this) + Constants.ulEditorIdx;
//		Log.log(ulEditorIdx);
//		out.write(Tools.getContent(ulEditorIdx, Tools.DEFAULT_CHARSET) );
//		out.close();
	}
	
	// 处理掉id为list的style标签 [这里先暂时这样处理, 之后再回来完善]
	private String prepare(String revised) {
		StringBuilder sb = new StringBuilder(revised.length());
		String startStr = "<style id=\"list\"";
		String endStr = "</style>";
		int liStyleIdxBefore = revised.indexOf(startStr);
		int liStyleIdxAfter = revised.indexOf(">", liStyleIdxBefore + startStr.length());
		sb.append(revised.substring(0, liStyleIdxBefore) );
		sb.append(revised.substring(revised.indexOf(endStr, liStyleIdxAfter)) );
		
		return sb.toString();
	}
	
}
