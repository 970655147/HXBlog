package com.hx.bean;

import net.sf.json.JSONObject;

// 用于排序的bean
public class TagToBlogCnt implements Comparable<TagToBlogCnt>, EncapJSON {

	// 标签的id, 标签对应的播客的个数
	private String tag;
	private Integer blogCnt;

	// 初始化
	public TagToBlogCnt() {
		
	}
	public TagToBlogCnt(String tag, Integer blogCnt) {
		this.tag = tag;
		this.blogCnt = blogCnt;
	}
	public TagToBlogCnt(TagToBlogCnt tagToBlogCnt) {
		this.tag = tagToBlogCnt.tag;
		this.blogCnt = tagToBlogCnt.blogCnt;
	}
	
	// for TreeSet
	public int compareTo(TagToBlogCnt o) {
		int cntDelta =  o.blogCnt - blogCnt;
		
		if(cntDelta != 0) {
			return cntDelta;
		} else {
			return this.tag.compareTo(o.tag);
		}
	}

	// blogCnt 自增
	public void incTagCnt() {
		blogCnt ++;
	}
	public void decTagCnt() {
		blogCnt --;
	}
	
	// 封装当前对象中的数据到obj中
	public void encapJSON(JSONObject obj) {
		obj.element("text", tag);
		obj.element("cnt", blogCnt);
	}
	// setter & getter
	public String getTag() {
		return tag;
	}
	public Integer getBlogCnt() {
		return blogCnt;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public void setBlogCnt(Integer blogCnt) {
		this.blogCnt = blogCnt;
	}	

	// for debug & response
	public String toString() {
		JSONObject res = new JSONObject();
		encapJSON(res);
		
		return res.toString();
	}
	
}
