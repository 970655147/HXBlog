package com.hx.bean;

import java.sql.ResultSet;
import java.util.List;

import net.sf.json.JSONObject;

import com.hx.action.BlogListAction;
import com.hx.util.Log;
import com.hx.util.Tools;

// ��Ӧ�����ݿ��е�blogList��ÿһ����¼
public class Blog implements EncapJSON {

	private Integer id;
	private String title;
	private String path;
	private List<String> tags;
	private String createTime;
	private String content;
	
	// ��ʼ��
	public Blog() {
		
	}
	public Blog(Integer id, String title, String path, String tags, String createTime) {
		set(id, title, path, tags, createTime);
		this.content = null;
	}
	public Blog(Blog blog) {
		this(blog.id, blog.title, blog.path, null, blog.createTime);
		this.tags = blog.tags;
	}
	
	// ���ø�����resultSet ��ʼ����ǰblog����
	public void init(ResultSet rs) throws Exception {
		String path = rs.getString("path");
		set(rs.getInt("id"), null, path, rs.getString("tag"), rs.getString("createTime"));
		this.title = Tools.getTitleFromBlogFileName(path);
	}
	
	// ��װ��ǰ�����е����ݵ�obj��
	public void encapJSON(JSONObject obj) {
		obj.element("id", id);
		obj.element("title", title);
		obj.element("path", path);
		obj.element("tags", Tools.tagsToString(tags));
		obj.element("date", createTime);
		obj.element("content", content);
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
		if(! this.tags.contains(BlogListAction.ALL)) {
			this.tags.add(BlogListAction.ALL);
		}
		if(! Tools.isEmpty(createTime) ) {
			this.createTime = createTime;
		}
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	// for debug & response
	public String toString() {
		JSONObject res = new JSONObject();
		encapJSON(res);
		
		return res.toString();
	}
}
