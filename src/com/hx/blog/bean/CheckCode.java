package com.hx.blog.bean;

import java.awt.image.BufferedImage;

// 验证码bean
public class CheckCode {

	// 验证码, 以及验证图片 
	public String checkCode;
	public BufferedImage checkCodeImage;
	
	// 初始化
	public CheckCode() {
		super();
	}
	public CheckCode(String checkCode, BufferedImage checkCodeImage) {
		super();
		this.checkCode = checkCode;
		this.checkCodeImage = checkCodeImage;
	}
	
}
