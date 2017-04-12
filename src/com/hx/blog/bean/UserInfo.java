package com.hx.blog.bean;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.hx.blog.util.Constants;
import com.hx.blog.util.EncapJSON;
import com.hx.blog.util.Tools;

import net.sf.json.JSONObject;

// һ���û���Ϣ
public class UserInfo implements EncapJSON {

	// �ǳ�, email, ͷ��, ͷ��
	private String name;
	private String email;
	private int imageIdx;
	private String privilege;

	// ��ʼ��
	public UserInfo() {
	}
	public UserInfo(String name, String email, int imageIdx, String privilege) {
		super();
		set(name, email, imageIdx, privilege);
	}

	//���ݸ�����rs��ʼ��Comment
	public void init(ResultSet rs) throws Exception {
		set(rs.getString("name"), rs.getString("email"), rs.getInt("headImgIdx"), rs.getString("privilege") );
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
	public String getPrivilege() {
		return privilege;
	}
	public void set(String name, String email, int imageIdx, String privilege) {
		this.name = name;
		this.email = email;
		this.imageIdx = imageIdx;
		this.privilege = privilege;
	}
	
	@Override
	public void encapJSON(JSONObject obj) {
		Tools.addIfNotEmpty(obj, "name", name);
		Tools.addIfNotEmpty(obj, "email", email);
		obj.element("imageIdx", imageIdx);
		Tools.addIfNotEmpty(obj, "privilege", privilege);
	}
	
	// for debug & IO
	public String toString() {
		JSONObject res = new JSONObject();
		encapJSON(res);
		
		return res.toString();
	}
	
}
