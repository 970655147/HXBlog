package com.hx.bean;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.hx.util.EncapJSON;
import com.hx.util.Tools;

// һ��blog�����comment����Ŀ
public class CommentEntry implements Comparable<CommentEntry>, EncapJSON {

	// ���͵�id, ���е�����, ����bolg���ʵ�Ƶ��ӳ�� [static]
	private Integer blogId;
	private List<List<Comment>> blogComments;
	private static Map<Integer, Integer> blogGetFrequency;
	
	// ��ʼ��
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
	
	// ��װ��ǰ�����е����ݵ�obj��
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
