package com.hx.util;

import java.text.SimpleDateFormat;

import net.sf.json.JSONObject;

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
	public final static String addBlogListSql = "insert into blogList (id, path, tag, createTime) values ('%d', '%s', '%s', '%s');";
	public final static String addMultiBlogListSql = "insert into blogList (id, path, tag, createTime) %s;";
	public final static String addTagListSql = "insert into tagToBlog (tag, blogId) values ('%s', '%d');";
	public final static String addMultiTagListSql = "insert into tagToBlog (tag, blogId) %s;";
	public final static String deleteBlogListSql = "delete from blogList where id = '%d'";
	public final static String deleteMultiBlogListSql = "delete from blogList where id in (%s)";
	public final static String deleteTagListSql = "delete from tagToBlog where blogId = '%d'";
	public final static String deleteMultiTagListSql = "delete from tagToBlog where blogId = '%d' and tag in (%s)";
	public final static String updateBlogListSql = "update blogList set path='%s', tag='%s' where id = %d";
	public final static String deleteByTagAndIdTagListSql = "delete from tagToBlog where tag = '%s' and blogId = '%d'";	
	// �����������Ŀ·����·��, uEditor��index.html��λ��
	public final static String blogFolder = "WEB-INF/post";
	public final static String uEditorIdx = "ueditorLib/index.html";
	public final static String logFolder = "WEB-INF/log";
	
	// Ĭ�ϵ�blogId [δ��], ��ʽ�����ڵ�dateFormat [ǰ��������ʾ, �������ڹ����ļ�]
	public final static Integer defaultBlogId = -1;
	public final static SimpleDateFormat createDateFormat = new SimpleDateFormat("yyyy-MM-dd HH : mm");
	public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
	
	// ��Ӧ�ɹ�, ʧ�ܵı�־, Ĭ�ϵ���Ӧ��
	public final static Boolean respSucc = true;
	public final static Boolean respFailed = false;
	public final static Integer defaultResponseCode = 0;
	
	// ���ˢ�µ����ݿ������, ���¸�������ֵ [������ֵ, ֱ��ˢ�µ����ݿ�], ����tag��ʱ��, ɾ�����б�ǩ����ֵ [δ��]
//	public final static long checkUpdateInterval = 10 * 1000;
	public final static long checkUpdateInterval = 10 * 60 * 1000;
	public final static int updateThreashold = 10;
	public static int getDeleteAllTagThreshold(int allTag) {
		return allTag >> 1;
	}
	
	// ��ʾ���µ����ݵ����� [���, ɾ��, �޸�]
	public final static int addBlogCnt = 0;
	public final static int addTagCnt = 1;
	public final static int deletedBlogCnt = 2;
	public final static int deletedTagCnt = 3;
	public final static int revisedBlogCnt = 4;
	
	// �Ƿ�����debug
	public final static boolean debugEnable = true;
	public final static boolean logEnable = true;
//	public final static long logFileThreshold = 2 << 10;
	public final static long logFileThreshold = 512 << 10;
	
	// �˺���Ϣ
	public final static String USER_NAME = "userName";
	public final static String TOKEN = "token";
	public final static String userName = "970655";
	public final static String token = "970655";
	public final static String pwd = "202cb962ac59075b964b07152d234b70";
	
	// �޸İ�ť, ɾ����ť
	public final static String reviseBtn = "<a href='/HXBlog/#!/blogPublishAction?blogId={{postId}}&revise=true'>�޸�</a>";
	public final static String deleteBtn = "<a id='deleteAction' href='javascript:void(0)'>ɾ��</a>'";
	public final static JSONObject publishBlogConf = new JSONObject().element("text", "Publish").element("href", "/HXBlog/#!/blogPublishAction");
	
	
}
