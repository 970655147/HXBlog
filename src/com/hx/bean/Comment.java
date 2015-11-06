package com.hx.bean;

import com.hx.util.Tools;

import net.sf.json.JSONObject;

// һ������
public class Comment implements EncapJSON {
	
	// ����id, ¥��, �ڸ�¥�����۵�����
	// �û�����Ϣ, ����, ��������
	private int blogId;
	private int floorId;
	private int commentIdx;
	private UserInfo userInfo;
	private String date;
	private String comment;
	
	// setter & getter
	public int getBlogId() {
		return blogId;
	}
	public int getFloorId() {
		return floorId;
	}
	public int getCommentIdx() {
		return commentIdx;
	}
	public UserInfo getUserInfo() {
		return userInfo;
	}
	public String getDate() {
		return date;
	}
	public String getComment() {
		return comment;
	}
	
	@Override
	public void encapJSON(JSONObject obj) {
		obj.element("blogId", blogId);
		obj.element("floorId", floorId);
		obj.element("commentIdx", commentIdx);
		obj.element("userInfo", userInfo.toString());
		Tools.addIfNotEmpty(obj, "date", date);
		Tools.addIfNotEmpty(obj, "comment", comment);
	}
	
	// for debug & IO
	public String toString() {
		JSONObject res = new JSONObject();
		encapJSON(res);
		
		return res.toString();
	}
	
}
