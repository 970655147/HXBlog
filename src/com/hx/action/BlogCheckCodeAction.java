package com.hx.action;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.bean.CheckCode;
import com.hx.util.Constants;
import com.hx.util.Tools;

public class BlogCheckCodeAction extends HttpServlet {
	
	// 获取验证码的action
		// 取到验证码后, 将其放入session中
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html;charset=utf-8");
		resp.setDateHeader("Expires", -1);
		resp.setHeader("Cache-Control", "no-cache");
		resp.setHeader("Pragma", "no-cache");
		resp.setHeader("Content-Type", "image");
		
//		CheckCode checkCode = Tools.getCheckCode();
		CheckCode checkCode = Tools.getCheckCode(Constants.checkCodeWidth, Constants.checkCodeHeight, Constants.checkCodeBgColor, Constants.checkCodeFont, Constants.checkCodeLength, Constants.checkCodes, Constants.checkCodeMinInterference, Constants.checkCodeInterferenceOff);
		req.getSession().setAttribute(Constants.checkCode, checkCode.checkCode);
		
		ImageIO.write(checkCode.checkCodeImage, "jpg", resp.getOutputStream());
		Tools.log(this, "get checkCode : " + checkCode.checkCode + ", from ip : " + Tools.getIPAddr(req) );
	}
	
}
