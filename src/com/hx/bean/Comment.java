package com.hx.bean;

import com.hx.util.Tools;

import net.sf.json.JSONObject;

// 一条评论
public class Comment implements EncapJSON {
	
	// 播客id, 楼数, 在该楼的评论的索引
	// 用户的信息, 日期, 评论内容
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
