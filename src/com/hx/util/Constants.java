package com.hx.util;

import java.text.SimpleDateFormat;

// ����
public class Constants {

	// ���ݿ��λ��, �����ļ���λ��, ���������ļ���λ��
	public final static String dbPath = "com/hx/config/HXBlog.db";
	public final static String configPath = "com/hx/config/config.json";
	public final static String resumePath = "com/hx/config/resume.json";
	// ����blogList, tagList�� ��sql
	// ����blogList, tagList��Ԫ�ص�sql [���Ӳ��͵ĳ���]
	// ɾ��blogList, tagList��Ԫ�ص�sql [ɾ�����͵ĳ���]
	// ����blogList, tagList��Ԫ�ص�sql [���²��͵ĳ���]
	public final static String blogListSql = "select * from blogList";
	public final static String tagListSql = "select * from tagToBlog";
	public final static String addBlogListSql = "insert into blogList (id, title, path, tag, createTime) values ('%d', '%s', '%s', '%s', '%s');";
	public final static String addTagListSql = "insert into tagToBlog (tag, blogId) values ('%s', '%d');";
	public final static String deleteBlogListSql = "delete from blogList where id = '%d'";
	public final static String deleteTagListSql = "delete from tagToBlog where blogId = '%d'";
	public final static String updateBlogListSql = "update blogList set title='%s', path='%s', tag='%s' where id = '%d'";
	public final static String deleteByTagAndIdTagListSql = "delete from tagToBlog where tag = '%s' and blogId = '%d'";	
	// �����������Ŀ·����·��, uEditor��index.html��λ��
	public final static String blogFolder = "post";
	public final static String uEditorIdx = "ueditorLib/index.html";
	// Ĭ�ϵ�blogId [δ��], ��ʽ�����ڵ�dateFormat [ǰ��������ʾ, �������ڹ����ļ�]
	public final static Integer defaultBlogId = -1;
	public final static SimpleDateFormat createDateFormat = new SimpleDateFormat("yyyy-MM-dd HH : mm");
	public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
	
	// ��Ӧ�ɹ�, ʧ�ܵı�־, Ĭ�ϵ���Ӧ��
	public final static Boolean respSucc = true;
	public final static Boolean respFailed = false;
	public final static Integer defaultResponseCode = 0;
	
	// ���ˢ�µ����ݿ������, ���¸�������ֵ [������ֵ, ֱ��ˢ�µ����ݿ�], ����tag��ʱ��, ɾ�����б�ǩ����ֵ
	public final static long checkUpdateInterval = 10 * 60 * 1000;
	public final static int updateThreashold = 10;
	public static int getDeleteAllTagThreshold(int allTag) {
		return allTag >> 1;
	}
	
	
}
