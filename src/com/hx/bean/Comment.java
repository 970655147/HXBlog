package com.hx.bean;

import java.sql.ResultSet;

import net.sf.json.JSONObject;

import com.hx.util.Constants;
import com.hx.util.EncapJSON;
import com.hx.util.Tools;

// һ������
public class Comment implements EncapJSON {

	// ����id, ¥��, �ڸ�¥�����۵�����
	// �û�����Ϣ, ����, ���۵Ķ���, ��������
	private Integer blogIdx;
	private int floorIdx;
	private int commentIdx;
	private UserInfo userInfo;
	private String date;
	private String to;
	private String comment;
	
	// ��ʼ��
	public Comment() {
		super();
	}
	public Comment(Integer blogIdx, int floorIdx, int commentIdx, UserInfo userInfo, String date, String to, String comment) {
		set(blogIdx, floorIdx, commentIdx, userInfo, date, to, comment);
	}
	
	// ���ݸ�����rs��ʼ��Comment
	public void init(ResultSet rs) throws Exception {
		UserInfo userInfo = new UserInfo();
		userInfo.init(rs);
		set(rs.getInt("blogIdx"), rs.getInt("floorIdx"), rs.getInt("commentIdx"), userInfo, rs.getString("date"), rs.getString("toUser"), Tools.detransfer(rs.getString("comment"), Constants.needToBeDeformat) );
	}
	
	// setter & getter
	public Integer getBlogIdx() {
		return blogIdx;
	}
	public int getFloorIdx() {
		return floorIdx;
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
	public String getTo() {
		return to;
	}
	public void setFloorId(int floorIdx) {
		this.floorIdx = floorIdx;
	}
	public void setCommentIdx(int commentIdx) {
		this.commentIdx = commentIdx;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}	
	public void set(Integer blogIdx, int floorIdx, int commentIdx, UserInfo userInfo, String date, String to, String comment) {
		this.blogIdx = blogIdx;
		this.floorIdx = floorIdx;
		this.commentIdx = commentIdx;
		this.userInfo = userInfo;
		this.date = date;
		this.to = to;
		this.comment = comment;
	}
	
	// ��װ��ǰ�����е����ݵ�obj��
	@Override
	public void encapJSON(JSONObject obj) {
		obj.element("blogIdx", blogIdx);
		obj.element("floorIdx", floorIdx);
		obj.element("commentIdx", commentIdx);
		obj.element("userInfo", userInfo.toString());
		Tools.addIfNotEmpty(obj, "to", to);
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
