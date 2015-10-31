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
	
	// for TreeSet
	public int compareTo(TagToBlogCnt o) {
		int cntDelta = blogCnt - o.blogCnt;
		
		if(cntDelta != 0) {
			return cntDelta;
		} else {
			return this.tag.compareTo(o.tag);
		}
	}

	// ��װ��ǰ�����е����ݵ�obj��
	public void encapJSON(JSONObject obj) {
		obj.element("text", tag);
		obj.element("cnt", blogCnt);
	}
	// setter & getter
	public String getTagId() {
		return tag;
	}
	public Integer getBlogCnt() {
		return blogCnt;
	}

	
}
