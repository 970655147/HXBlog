package com.hx.util;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
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
	public final static String addBlogListSql = "insert into blogList (id, path, tag, createTime, good, notGood, visited) values ('%d', '%s', '%s', '%s', %d, %d, %d);";
	public final static String addMultiBlogListSql = "insert into blogList (id, path, tag, createTime, good, notGood, visited) %s;";
	public final static String addTagListSql = "insert into tagToBlog (tag, blogId) values ('%s', '%d');";
	public final static String addMultiTagListSql = "insert into tagToBlog (tag, blogId) %s;";
	public final static String deleteBlogListSql = "delete from blogList where id = '%d'";
	public final static String deleteMultiBlogListSql = "delete from blogList where id in (%s)";
	public final static String deleteTagListSql = "delete from tagToBlog where blogId = '%d'";
	public final static String deleteMultiTagListSql = "delete from tagToBlog where blogId = '%d' and tag in (%s)";
	public final static String updateBlogListSql = "update blogList set path='%s', tag='%s', good=%d, notGood=%d, visited=%d where id = %d";
	public final static String deleteByTagAndIdTagListSql = "delete from tagToBlog where tag = '%s' and blogId = '%d'";	
	// 帖子相对于项目路径的路径, uEditor的index.html的位置
	public final static String blogFolder = "WEB-INF/post";
	public final static String uEditorIdx = "ueditorLib/index.html";
	public final static String logFolder = "WEB-INF/log";
	
	// 默认的blogId [未用], 默认的blogPath, 默认的blogTag, 默认的good的数量, notGood的数量, defaultVisited的数量
	// 默认的cookieValue
	public final static Integer defaultBlogId = -1;
	public final static String defaultBlogPath = "unknown";
	public final static String defaultBlogTag = "unknown";
	public final static int defaultGood = 0;
	public final static int defaultNotGood = 0;
	public final static int defaultVisited = 0;
	public final static String defaultCookieValue = Tools.NULL;
	
	// 默认的配置, 默认的建立数据, 格式化日期的dateFormat [前者用于显示, 后者用于构造文件]
	public final static String defaultConfig = "{ \"title\" : \"蓝风970655147\", \"subTitle\" : \"好记性不如烂笔头\", \"quickLinks\" : [ {\"text\" : \"Blog\", \"href\" : \"/HXBlog/#!/tag/all\"}, {\"text\" : \"Resume\", \"href\" : \"/HXBlog/#!/resume\"}, {\"text\" : \"Github\", \"href\" : \"https://github.com/970655147\"}, {\"text\" : \"CSDNBlog\", \"href\" : \"http://blog.csdn.net/u011039332\"}, ]}";
	public final static String defaultResume = "{ \"basicInfo\" : { \"name\" : \"Jerry He\", \"description\" : \"One programmer who enjoys programming & expects challenges\", \"mail\" : \"970655147@qq.com\" }, \"education\" : [ { \"period\" : \"2012/09 - 2016/06\", \"university\" : \"cduestc\", \"college\" : \"cduestc\", \"description\" : \"Bachelor Degree in Mechanical Engineering and Automation\" } ], \"workExperience\" : [ { \"period\" : \"2012/06 – Now\", \"company\" : \"Nokia MP\", \"title\" : \"Senior Software Engineer\", \"description\" : [ \"Responsible for new concept handwriting Chinese input method on new s40 platform\", \"Demo version had been completed in two weeks and shown in department all-hands meeting\", \"Final version has been accepted by product (Asha 501), and no bugs reported after development finished\" ] }, { \"period\" : \"2010/07 – 2012/05\", \"company\" : \"Nokia MP\", \"title\" : \"Software Engineer\", \"description\" : [ \"Responsible for Chinese input development in S40 full-touch product\", \"Achieved 0 bugs target two month ahead\" ] }, { \"period\" : \"2008/01 – 2010/06\", \"company\" : \"Dayang Technology Development Inc\", \"title\" : \"Software Engineer\", \"description\" : [ \"Responsible for Edit-Net related product & project development\", \"Finished BTV project development by myself, praised by client\", \"Leading new staff in several projects\" ] } ], \"skills\" : [ { \"category\" : \"Programming language\", \"items\" : [ {\"name\" : \"Java\", \"proficiency\" : \"proficient\"}, {\"name\" : \"C++\", \"proficiency\" : \"familiar\"}, {\"name\" : \"C\", \"proficiency\" : \"familiar\"}, {\"name\" : \"Scheme\", \"proficiency\" : \"familiar\"}, {\"name\" : \"Python\", \"proficiency\" : \"familiar\"}, {\"name\" : \"C#\", \"proficiency\" : \"familiar\"}, {\"name\" : \"C#\", \"proficiency\" : \"familiar\"}, {\"name\" : \"Javascript\", \"proficiency\" : \"familiar\"} ] }, { \"category\" : \"Version Control\", \"items\" : [ {\"name\" : \"Git\", \"proficiency\" : \"proficient\"}, {\"name\" : \"SVN\", \"proficiency\" : \"familiar\"}, {\"name\" : \"Synergy\", \"proficiency\" : \"familiar\"} ] } ], \"projects\" : [ {\"name\" : \"HXBlog\", \"description\" : \"A static blog on github. Using JavaScript to load blog article in Markdown format and render it dynamically.\"} ]}";
	public final static SimpleDateFormat createDateFormat = new SimpleDateFormat("yyyy-MM-dd HH : mm");
	public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
	
	// 响应成功, 失败的标志, 默认的响应码
	public final static Boolean respSucc = true;
	public final static Boolean respFailed = false;
	public final static Integer defaultResponseCode = 0;
	
	// 检查刷新到数据库的周期, 更新个数的阈值 [超过阈值, 直接刷新到数据库], 更新tag的时候, 删除所有标签的阈值 [未用]
//	public final static long checkUpdateInterval = 60 * 1000;
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
	public final static String ACCOUNT_NAME = "userName";
	public final static String TOKEN = "token";
	public final static String account = "970655";
	public final static String token = "970655";
	public final static String pwd = "202cb962ac59075b964b07152d234b70";
	
	public final static String preferInfo = "preferInfo";
	public final static String adminUserName = "蓝风970655147";
	public final static String adminEmail = "970655147@qq.com";
	public final static int adminImageIdx = 0;
	
	// 修改按钮, 删除按钮
	public final static String reviseBtn = "<a href='/HXBlog/#!/blogPublishAction?blogId=%d&revise=true'>修改</a>";
	public final static String deleteBtn = "<a id='deleteAction' href='javascript:void(0)'>删除</a>";
	public final static JSONObject publishBlogConf = new JSONObject().element("text", "Publish").element("href", "/HXBlog/#!/blogPublishAction");
	public final static JSONObject logoutBlogConf = new JSONObject().element("text", "Logout").element("href", "/HXBlog/action/blogLogoutAction");
	public final static JSONObject loginBlogConf = new JSONObject().element("text", "Login").element("href", "/HXBlog/login.html");
	public final static JSONArray defaultTagList = new JSONArray().element(new JSONObject().element("text", Constants.defaultBlogTag).element("cnt", 0));
	public final static JSONObject defaultTagListAndBlogList = new JSONObject().element("tagList", Constants.defaultTagList).element("blogList", new JSONObject() );
	
	// 是否还存在上一页, 下一篇文章的标志
	public final static int HAVE_NO_THIS_TAG = -1;
	public final static int HAVE_NO_PREV_IDX = -1;
	public final static int HAVE_NO_NEXT_IDX = -2;
	
	// 0 整数
	public final static Integer INTE_ZERO = new Integer(0);
	public final static int ZERO = 0;

	// 校验相关的常量
	public final static int titleMaxLength = 30;
	public final static Pattern specCharPattern = Pattern.compile("[!-/ | :-@ | {-~ | \\[ /\\]^—、]");
	
	// 是否点击过顶踩的cookieName
	public final static String visitedCookieName = "isVisited";
	public final static String visitedMap = "visitedMap";
	public final static String visitedCookieValue = "visited";
	public final static String senseCookieName = "isSensed";
	public final static String senseCookieValue = "sensed";
	public final static String senseGood = "good";
	public final static String senseNotGood = "notGood";
	
	
}
