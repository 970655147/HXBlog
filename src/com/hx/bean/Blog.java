package com.hx.bean;

import java.sql.ResultSet;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.hx.util.Tools;

// 对应于数据库中的blogList的每一条记录
public class Blog implements EncapJSON {

	private Integer id;
	private String title;
	private String path;
	private List<String> tags;
	private String createTime;
	
	// 初始化
	public Blog() {
		
	}
	public Blog(Integer id, String title, String path, String tags, String createTime) {
		this.id = id;
		this.title = title;
		this.path = path;
		this.tags = Tools.getTagListFromString(tags);
		this.createTime = createTime;
		
	}
	
	// 利用给定的resultSet 初始化当前blog对象
	public void init(ResultSet rs) throws Exception {
		this.id = rs.getInt("id");
		this.title = rs.getString("title");
		this.path = rs.getString("path");
		this.tags = Tools.getTagListFromString(rs.getString("tag") );
		this.createTime = rs.getString("createTime");
	}
	
	// 封装当前对象中的数据到obj中
	public void encapJSON(JSONObject obj) {
		obj.element("id", id);
		obj.element("title", title);
		JSONArray tags = new JSONArray();
		for(String tag : this.tags) {
			tags.add(tag);
		}
		obj.element("tag", tags.toString());
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
	public List<String> getTag() {
		return tags;
	}
	public String getCreateTime() {
		return createTime;
	}
	
}
