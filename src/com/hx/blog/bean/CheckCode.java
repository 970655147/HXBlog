package com.hx.blog.bean;

import java.awt.image.BufferedImage;

// ��֤��bean
public class CheckCode {

	// ��֤��, �Լ���֤ͼƬ 
	public String checkCode;
	public BufferedImage checkCodeImage;
	
	// ��ʼ��
	public CheckCode() {
		super();
	}
	public CheckCode(String checkCode, BufferedImage checkCodeImage) {
		super();
		this.checkCode = checkCode;
		this.checkCodeImage = checkCodeImage;
	}
	
}
