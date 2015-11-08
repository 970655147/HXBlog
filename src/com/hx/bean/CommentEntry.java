package com.hx.bean;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.hx.util.EncapJSON;
import com.hx.util.Tools;

// 一个blog缓存的comment的条目
public class CommentEntry implements Comparable<CommentEntry>, EncapJSON {

	// 播客的id, 所有的评论, 所有bolg访问的频率映射 [static]
	private Integer blogId;
	private List<List<Comment>> blogComments;
	private static Map<Integer, Integer> blogGetFrequency;
	
	// 初始化
	public CommentEntry() {
		super();
	}
	public CommentEntry(Integer blogId, List<List<Comment>> blogComments) {
		super();
		this.blogId = blogId;
		this.blogComments = blogComments;
	}
	
	// setter & getter
	public List<List<Comment>> getBlogComments() {
		return blogComments;
	}
	public void setBlogComments(List<List<Comment>> blogComments) {
		this.blogComments = blogComments;
	}
	public Integer getBlogId() {
		return blogId;
	}
	public static void setBlogGetFrequency(Map<Integer, Integer> blogGetFrequency) {
		CommentEntry.blogGetFrequency = blogGetFrequency;
	}
	
	// for priorityQueue
	@Override
	public int compareTo(CommentEntry o) {
		return blogGetFrequency.get(blogId) - blogGetFrequency.get(blogId);
	}
	
	// 封装当前对象中的数据到obj中
	@Override
	public void encapJSON(JSONObject obj) {
		obj.element("blogId", blogId);
		obj.element("blogComments", Tools.encapBlogComments(blogComments) );
	}
	
	// for debug ...
	public String toString() {
		JSONObject res = new JSONObject();
		encapJSON(res);
		
		return res.toString();
	}
	
}
