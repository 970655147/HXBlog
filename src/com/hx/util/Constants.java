package com.hx.util;

import java.text.SimpleDateFormat;

import net.sf.json.JSONObject;

// 常量
public class Constants {

	// 数据库的位置, 配置文件的位置, 简历配置文件的位置
	public final static String dbPath = "com/hx/config/HXBlog.db";
	public final static String configPath = "com/hx/config/config.json";
	public final static String resumePath = "com/hx/config/resume.json";
	// 查找blogList, tagList表 的sql
	// 增加blogList, tagList表元素的sql [增加播客的场景]
	// 删除blogList, tagList表元素的sql [删除播客的场景]
	// 更新blogList, tagList表元素的sql [更新播客的场景]
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
	// 帖子相对于项目路径的路径, uEditor的index.html的位置
	public final static String blogFolder = "WEB-INF/post";
	public final static String uEditorIdx = "ueditorLib/index.html";
	public final static String logFolder = "WEB-INF/log";
	
	// 默认的blogId [未用], 格式化日期的dateFormat [前者用于显示, 后者用于构造文件]
	public final static Integer defaultBlogId = -1;
	public final static SimpleDateFormat createDateFormat = new SimpleDateFormat("yyyy-MM-dd HH : mm");
	public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
	
	// 响应成功, 失败的标志, 默认的响应码
	public final static Boolean respSucc = true;
	public final static Boolean respFailed = false;
	public final static Integer defaultResponseCode = 0;
	
	// 检查刷新到数据库的周期, 更新个数的阈值 [超过阈值, 直接刷新到数据库], 更新tag的时候, 删除所有标签的阈值 [未用]
//	public final static long checkUpdateInterval = 10 * 1000;
	public final static long checkUpdateInterval = 10 * 60 * 1000;
	public final static int updateThreashold = 10;
	public static int getDeleteAllTagThreshold(int allTag) {
		return allTag >> 1;
	}
	
	// 表示更新的数据的索引 [添加, 删除, 修改]
	public final static int addBlogCnt = 0;
	public final static int addTagCnt = 1;
	public final static int deletedBlogCnt = 2;
	public final static int deletedTagCnt = 3;
	public final static int revisedBlogCnt = 4;
	
	// 是否启动debug
	public final static boolean debugEnable = true;
	public final static boolean logEnable = true;
//	public final static long logFileThreshold = 2 << 10;
	public final static long logFileThreshold = 512 << 10;
	
	// 账号信息
	public final static String USER_NAME = "userName";
	public final static String TOKEN = "token";
	public final static String userName = "970655";
	public final static String token = "970655";
	public final static String pwd = "202cb962ac59075b964b07152d234b70";
	
	// 修改按钮, 删除按钮
	public final static String reviseBtn = "<a href='/HXBlog/#!/blogPublishAction?blogId={{postId}}&revise=true'>修改</a>";
	public final static String deleteBtn = "<a id='deleteAction' href='javascript:void(0)'>删除</a>'";
	public final static JSONObject publishBlogConf = new JSONObject().element("text", "Publish").element("href", "/HXBlog/#!/blogPublishAction");
	
	
}
