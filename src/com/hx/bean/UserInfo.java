package com.hx.bean;

import com.hx.util.Tools;

import net.sf.json.JSONObject;

// 一个用户信息
public class UserInfo implements EncapJSON {
	
	// 昵称, email, 头像
	private String name;
	private String email;
	private int imageIdx;

	// 初始化
	public UserInfo() {
	}
	public UserInfo(String name, String email, int imageIdx) {
		super();
		this.name = name;
		this.email = email;
		this.imageIdx = imageIdx;
	}

	// setter & getter
	public String getName() {
		return name;
	}
	public String getEmail() {
		return email;
	}
	public int getImageIdx() {
		return imageIdx;
	}
	
	@Override
	public void encapJSON(JSONObject obj) {
		Tools.addIfNotEmpty(obj, "name", name);
		Tools.addIfNotEmpty(obj, "email", email);
		obj.element("imageIdx", imageIdx);
	}
	
	// for debug & IO
	public String toString() {
		JSONObject res = new JSONObject();
		encapJSON(res);
		
		return res.toString();
	}
	
}
