package com.hx.bean;

import net.sf.json.JSONObject;

// ���������bean
public class TagToBlogCnt implements Comparable<TagToBlogCnt>, EncapJSON {

	// ��ǩ��id, ��ǩ��Ӧ�Ĳ��͵ĸ���
	private String tag;
	private Integer blogCnt;

	// ��ʼ��
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

	// blogCnt ����
	public void incTagCnt() {
		blogCnt ++;
	}
	public void decTagCnt() {
		blogCnt --;
	}
	
	// ��װ��ǰ�����е����ݵ�obj��
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
