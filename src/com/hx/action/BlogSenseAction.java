package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.bean.Blog;
import com.hx.bean.ResponseMsg;
import com.hx.bean.ValidateResult;
import com.hx.business.BlogManager;
import com.hx.interf.BaseAction;
import com.hx.interf.Validater;
import com.hx.util.Constants;
import com.hx.util.Tools;

public class BlogSenseAction extends BaseAction {

	// ���Ͷ�/ �ȴ����action
	// ��ȡblogId
	// У��blogId, sense, ȷ��senseΪConstants.senseGood / Constants.senseNotGood ����֮һ
	// У��blog
	// ���û��sense��  �����sense���¶�Ӧ��good / notGood, ����"sense��"��ǰsense
		// ����  ���senseΪ�����sense, ��ȡ��"sense��ǰ"sense
		// ���� ȡ��֮ǰ����sense, ����"sense��"��ǰsense
	// �������blog����visitedSensedBlogList, ��ˢ�µ����ݿ�
	// ���� ��Ӧ���, ��¼��־
	// ע : response.addCookie, ������ڸ��������ֵ�cookie�Ļ�, ���߻��滻��ǰ��.. �����ҵ��ĵĵط�, ���Ǿ�������һ������, �Ͳ��õ�����
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
		resp.setHeader("Content-Type","text/html;charset=" + Constants.DEFAULT_CHARSET);
		
		Integer blogId = null;
		ResponseMsg respMsg = new ResponseMsg();
		try {
			blogId = Integer.parseInt((String) req.getAttribute("blogId") );
		} catch(Exception e) {
			blogId = null;
		}
		String sense = (String) req.getAttribute("sense");
		ValidateResult vRes = validater.validate(req, respMsg, blogId, sense);
		if(vRes.isSucc ) {
			Blog blog = (Blog) vRes.attachments[0];
			BlogManager.addVisitSense(blog);					
			Cookie sensedToBlog = Tools.getCookieByName(req.getCookies(), Tools.getSensedCookieName(blogId) );
			boolean isSensed = (sensedToBlog != null) && (! Constants.defaultCookieValue.equals(sensedToBlog.getValue()) );
			if(! isSensed) {
				respMsg.set(Constants.respSucc, Constants.defaultResponseCode, "sense to : " + sense, Tools.getIPAddr(req) );
				if(sense.equals(Constants.senseGood) ) {
					blog.incGood();
				} else {
					blog.incNotGood();
				}
				resp.addCookie(new Cookie(Tools.getSensedCookieName(blog.getId() ), sense) );
			} else {
				sensedToBlog.setMaxAge(-1);
				if(sense.equals(Constants.senseGood) ) {
					if(! sensedToBlog.getValue().equals(sense) ) {
						respMsg.set(Constants.respSucc, Constants.defaultResponseCode, "sense to : " + sense, Tools.getIPAddr(req) );
						blog.incGood();
						blog.decNotGood();
						resp.addCookie(new Cookie(Tools.getSensedCookieName(blog.getId() ), Constants.senseGood) );
					} else {
						respMsg.set(Constants.respSucc, Constants.defaultResponseCode, "cancel sense to : " + sense, Tools.getIPAddr(req) );
						blog.decGood();
						resp.addCookie(new Cookie(Tools.getSensedCookieName(blog.getId() ), Constants.defaultCookieValue) );
					}
				} else {
					if(! sensedToBlog.getValue().equals(sense) ) {
						respMsg.set(Constants.respSucc, Constants.defaultResponseCode, "sense to : " + sense, Tools.getIPAddr(req) );
						blog.incNotGood();
						blog.decGood();
						resp.addCookie(new Cookie(Tools.getSensedCookieName(blog.getId() ), Constants.senseNotGood) );
					} else {
						respMsg.set(Constants.respSucc, Constants.defaultResponseCode, "cancel sense to : " + sense, Tools.getIPAddr(req) );
						blog.decNotGood();
						resp.addCookie(new Cookie(Tools.getSensedCookieName(blog.getId() ), Constants.defaultCookieValue) );
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

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.validater = new Validater() {
			@Override
			public ValidateResult validate(HttpServletRequest req, ResponseMsg respMsg, Object... others) {
				Integer blogId = (Integer) others[0];
				String sense = (String) others[1];
				if(Tools.validateObjectBeNull(req, blogId, "blogId", respMsg)) {
					if(Tools.validateStringBeNull(req, sense, "sense", respMsg) ) {
						respMsg.setOthers(sense);
						Blog blog = BlogManager.getBlog(blogId);
						if((Tools.equalsIgnorecase(sense, Constants.senseGood)) || (Tools.equalsIgnorecase(sense, Constants.senseNotGood)) ) {
							if(Tools.validateBlog(req, blog, respMsg) ) {
								return new ValidateResult(true, new Object[]{blog } );
							}
						}
					}
				}
				
				return Constants.validateResultFalse;
			}
		};
	}
	
}
