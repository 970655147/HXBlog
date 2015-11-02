package com.hx.bean;

import java.sql.ResultSet;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.hx.action.BlogListAction;
import com.hx.util.Tools;

// ��Ӧ�����ݿ��е�blogList��ÿһ����¼
public class Blog implements EncapJSON {

	private Integer id;
	private String title;
	private String path;
	private List<String> tags;
	private String createTime;
	
	// ��ʼ��
	public Blog() {
		
	}
	public Blog(Integer id, String title, String path, String tags, String createTime) {
		set(id, title, path, tags, createTime);
	}
	
	// ���ø�����resultSet ��ʼ����ǰblog����
	public void init(ResultSet rs) throws Exception {
		set(rs.getInt("id"), rs.getString("title"), rs.getString("path"), rs.getString("tag"), rs.getString("createTime"));
	}
	
	// ��װ��ǰ�����е����ݵ�obj��
	public void encapJSON(JSONObject obj) {
		obj.element("id", id);
		obj.element("title", title);
		obj.element("path", path);
		JSONArray tags = new JSONArray();
		for(String tag : this.tags) {
			tags.add(tag);
		}
		obj.element("tags", tags.toString());
		obj.element("date", createTime);
	}
	
	// setter & getter
	public Integer getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public String getPath() {
		return path;
	}
	public List<String> getTags() {
		return tags;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void set(Integer id, String title, String path, String tags, String createTime) {
		this.id = id;
		this.title = title;
		this.path = path;
		this.tags = Tools.getTagListFromString(tags);
		this.tags.add(BlogListAction.ALL);
		this.createTime = createTime;
	}
	
	// for debug & response
	public String toString() {
		JSONObject res = new JSONObject();
		res.element("id", id);
		Tools.addIfNotEmpty(res, "title", title);
		Tools.addIfNotEmpty(res, "path", path);
		res.element("tags", tags.toString());
		Tools.addIfNotEmpty(res, "createTime", createTime);
		
		return res.toString();
	}
}
