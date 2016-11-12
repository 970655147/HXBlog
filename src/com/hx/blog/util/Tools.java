package com.hx.blog.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileLock;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.hx.blog.bean.Blog;
import com.hx.blog.bean.CheckCode;
import com.hx.blog.bean.Comment;
import com.hx.blog.bean.ResponseMsg;
import com.hx.blog.bean.UserInfo;
import com.hx.blog.business.BlogManager;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

// 工具函数
public final class Tools {
	
	// disable constructor
	private Tools() {
		Tools.assert0("can't instantiate !");
	}
	
	// 常量
	public final static String EMPTY_STR = "";
	public final static String NULL = "null";
	public static final Character SLASH = '\\';
	public static final Character INV_SLASH = '/';
	static Character DOUBLE_QUOTION = '"';
	static Character QUOTION = '\'';
	static Character TRANSFER_CHAR = SLASH;
	public static final Character DOT = '.';
	public static final Character SPACE = ' ';
	public static final Character TAB = '\t';
	public static final Character CR = '\r';
	public static final Character LF = '\n';
	public static final Character QUESTION = '?';
	public static final String UNDER_LINE = "_";
	public static final String CRLF = "\r\n";
	public final static Random ran = new Random();
	public static String DEFAULT_CHARSET = "gbk";
	
	// 项目的日志buffer, 阈值[128kb], 缓冲区大小[(128 + 16)kb], 项目路径, 日志文件
	public final static int logThreshold = 128 << 10;
	public final static int logBuffSize = logThreshold + (logThreshold >> 3);
	public static StringBuffer logBuffer = null;
	public static String projectPath = null;
	public static String logFile = null;
	
	// 后缀相关
	public final static String HTML = ".html";
	public final static String JAVA = ".java";
	public final static String TXT = ".txt";
	public final static String PNG = ".png";
	public final static String JPEG = ".jpeg";
	public final static String JS = ".js";
	public final static String MAP = ".map";
	public final static String LOG = ".log";
	
	// 日志文件相关
	public static AtomicInteger TMP_IDX = new AtomicInteger(0);
	public static AtomicInteger LOG_TMP_IDX = new AtomicInteger(0);
	public static String TMP_DIR = null;
	public static String LOG_TMP_DIR = null;
	public static int BUFF_SIZE = 2048;
	public static String TMP_NAME = "tmp";
	public static String LOG_TMP_NAME = "log";
	public static String SUFFIX = HTML;
	public static String LOG_SUFFIX = LOG;
	
	// 加载jdbc, 初始化日志文件
	static {
		if(Constants.logEnable) {
			 logBuffer = new StringBuffer(logBuffSize);
		}
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// 初始化
	public static void init(ServletContext servletContext) {
		projectPath = servletContext.getRealPath("/");
		LOG_TMP_DIR = projectPath + Constants.logFolder;
		TMP_DIR = projectPath + Constants.tmpFolder;
		logFile = getNextLogTmpPath();
		while (shouldChangeLogFile(logFile) ) {
			logFile = getNextLogTmpPath();
		}
	}
	
	// ---------------日志文件相关---------------
	// 获取临时路径的下一个路径[返回文件路径]
	public static void setTmpIdx(int idx) {
		TMP_IDX = new AtomicInteger(idx );
	}
	public static String getNextTmpPath() {
		return TMP_DIR + "\\" + getNextTmpName();
	}
	public static String getNextLogTmpPath() {
		return LOG_TMP_DIR + "\\" + getNextLogTmpName();
	}
	public static String getNextTmpPath(String suffix) {
		return TMP_DIR + "\\" + getNextTmpName(suffix);
	}
	public static String getNextTmpPath(String fileName, String suffix) {
		return TMP_DIR + "\\" + fileName + suffix;
	}
	public static String getTmpPath(int idx) {
		return TMP_DIR + "\\" + TMP_NAME + idx + SUFFIX;
	}
	public static String getTmpPath(int idx, String suffix) {
		return TMP_DIR + "\\" + TMP_NAME + idx + suffix;
	}
	
	// 获取临时文件的下一个索引[生成文件名称]
	private static String getNextTmpName() {
		return TMP_NAME + (TMP_IDX.getAndIncrement() ) + SUFFIX;
	}
	private static String getNextLogTmpName() {
		return LOG_TMP_NAME + (LOG_TMP_IDX.getAndIncrement() ) + LOG_SUFFIX;
	}
	private static String getNextTmpName(String suffix) {
		return TMP_NAME + (TMP_IDX.getAndIncrement() ) + suffix;
	}
	
	// 获取给定的输入流中的字符内容
	public static String getContent(InputStream is, String charset) throws IOException {
		StringBuilder sb = new StringBuilder(is.available() );
		BufferedReader br = null;

		try {
			// .. forget initialize "br" !			2015.12.08
			br = new BufferedReader(new InputStreamReader(is, charset) );
			String line = null;
			while((line = br.readLine()) != null) {
				sb.append(line );
			}
		} finally {
			if(br != null) {
				br.close();
			}
		}
		
		return sb.toString();
	}
	public static String getContent(String path, String charset) throws IOException {
		return getContent(new File(path), charset);
	}
	public static String getContent(File file, String charset) throws IOException {
		return getContent(new FileInputStream(file), charset);
	}
	public static String getContent(String path) throws IOException {
		return getContent(new File(path), Constants.DEFAULT_CHARSET);
	}
	public static String getContent(File file) throws IOException {
		return getContent(file, Constants.DEFAULT_CHARSET);
	}
	public static String getContent(InputStream is) throws IOException {
		return getContent(is, Constants.DEFAULT_CHARSET);
	}
	
	// 判断给定的路径的文件是否存在
	public static boolean isFileExists(String path) {	
		File file = new File(path);
		return file.exists();
	}
	
	// 获取包中的路径对应的文件的路径
	public static String getPackagePath(String basePath, String packagePath) {
		return Tools.appendIfNotEndsWith(basePath, "/") + "WEB-INF/classes" + Tools.SLASH + removeIfStartsWithSlash(packagePath);
	}
	// 获取给定的播客的地址
	public static String getBlogPath(String basePath, String blogFileName) {
		return Tools.appendIfNotEndsWith(basePath, "/") + Constants.blogFolder + Tools.SLASH + removeIfStartsWithSlash(blogFileName) + HTML;
	}
	// 获取给定的基路径 和相对路径对应的路径
	public static String getPath(String basePath, String subPath) {
		return Tools.appendIfNotEndsWith(basePath, "/") + removeIfStartsWithSlash(subPath);
	}
	// 如果path以斜杠, 反斜杠开头, 则删除
	private static String removeIfStartsWithSlash(String path) {
		return Tools.removeIfStartsWith(Tools.removeIfStartsWith(path, "/"), "\\");
	}
	
	// 如果给定的字符串以startsWith, 则移除startsWith
	public static String removeIfStartsWith(String str, String startsWith) {
		if(str.startsWith(startsWith) ) {
			return str.substring(startsWith.length() );
		}
		
		return str;
	}
	// 如果给定的字符串以endsWith, 则移除endsWith
	public static String removeIfEndsWith(String str, String endsWith) {
		if(str.endsWith(endsWith) ) {
			return str.substring(0, str.length() - endsWith.length());
		}
		
		return str;
	}
	// 如果不是给定的字符串以startsWith, 则添加startsWith
	public static String appendIfNotStartsWith(String str, String startsWith) {
		if(! str.startsWith(startsWith) ) {
			return startsWith + str;
		}
		
		return str;
	}
	public static String appendIfNotEndsWith(String str, String endsWith) {
		if(! str.endsWith(endsWith) ) {
			return str + endsWith;
		}
		
		return str;
	}
	
	// 获取 / 关闭 到数据库的连接
	public static Connection getConnection(String basePath) throws Exception {
		String dbPath = Tools.getPackagePath(basePath, Constants.dbPath);
		String conUrl  = "jdbc:sqlite://" + dbPath;
		Connection con = DriverManager.getConnection(conUrl );
		
		return con;
	}
	public static void close(Connection con) {
		if(con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	// 注销driver
	// The web application [/HXBlog] registered the JDBC driver [org.sqlite.JDBC] but failed to unregister it when the web application was stopped. To prevent a memory leak, the JDBC Driver has been forcibly unregistered.
	// site : http://blog.sina.com.cn/s/blog_4550f3ca0101byg1.html
	public static void closeDataSource(String basePath) throws Exception {
		String dbPath = Tools.getPackagePath(basePath, Constants.dbPath);
		String conUrl  = "jdbc:sqlite://" + dbPath;
		DriverManager.deregisterDriver(DriverManager.getDriver(conUrl) );
	}
	
	// 获取当前项目的地址
	public static String getProjectPath() {
		return projectPath;
	}
	public static String getProjectPath(ServletContext servletContext) {
		return servletContext.getRealPath("/");
	}	
	
	// 各个tag的分隔符, 日期到播客名字的分隔符
	static String tagsSep = ",";
	static String dateBlogSep = "__dateBlogSep__";
	
	// 获取给定的tagList的字符串表示 / 从tagList字符串获取给定的tagList
	public static String getTagListString(List<String> tagList) {
		StringBuilder sb = new StringBuilder(tagList.size() << 2);
		Iterator<String> tagsIt = tagList.iterator();
		while(tagsIt.hasNext() ) {
			sb.append(tagsIt.next() );
			sb.append(tagsSep);
		}
		
		return sb.toString();
	}
	public static List<String> getTagListFromString(String tagListStr) {
		if(Tools.isEmpty(tagListStr) ) {
			return new ArrayList<>();
		}
		
		tagListStr = Tools.replaceMultiSpacesAsOne(tagListStr);
		String[] splits = tagListStr.split(tagsSep);
		List<String> res = new ArrayList<>(splits.length);
		for(String tag : splits) {
			if((validteTag(tag)) && (! res.contains(tag)) ) {
				res.add(tag );
				if(res.size() >= Constants.maxTagSize) {
					break ;
				}
			}
		}
		
		return res;
	}
	
	// 校验tag
	public static boolean validteTag(String tag) {
		return (! isEmpty(tag)) && (tag.length() < Constants.maxTagLength);
	}
	
	
	// 根据给定的日期  博客title 获取其应该存储的文件名, 根据存储的文件名  获取日期 播客title
	public static String getBlogFileName(String date, String title) {
		return date + dateBlogSep + title;
	}
	public static String[] getDateAndBlogTitleFromFileName(String fileName) {
		String[] dateAndBlogTitle = null;
		int sepIdx = fileName.indexOf(dateBlogSep);
		if(sepIdx >= 0) {
			dateAndBlogTitle = new String[2];
			dateAndBlogTitle[0] = fileName.substring(0, sepIdx);
			dateAndBlogTitle[1] = fileName.substring(sepIdx + dateBlogSep.length());
		}
		
		return dateAndBlogTitle;
	}
	public static String getTitleFromBlogFileName(String fileName) {
		String[] dateAndTitle = getDateAndBlogTitleFromFileName(fileName);
		if(dateAndTitle != null) {
			return dateAndTitle[1];
		}
		
		return null;
	}
	public static String getDateFromBlogFileName(String fileName) {
		String[] dateAndTitle = getDateAndBlogTitleFromFileName(fileName);
		if(dateAndTitle != null) {
			return dateAndTitle[0];
		}
		
		return null;
	}
	
	// 将html字符串保存到指定的文件中
	public static void save(String html, String nextTmpName) throws IOException {
		save(html, new File(nextTmpName) );
	}
	public static void save(String html, File nextTmpFile) throws IOException {
		write0(html, nextTmpFile, DEFAULT_CHARSET, false);
		Log.log("save content to \" " + nextTmpFile.getAbsolutePath() + " \" success ...");
	}	
	public static void append(String html, String nextTmpName) throws IOException {
		append(html, new File(nextTmpName) );
	}
	public static void append(String html, File nextTmpFile) throws IOException {
		write0(html, nextTmpFile, DEFAULT_CHARSET, true);
		Log.log("append content to \" " + nextTmpFile.getAbsolutePath() + " \" success ...");
	}
	private static void write0(String html, File nextTmpFile, String charset, boolean isAppend) throws IOException {
		// add fileLock while publish or revise blog !		2015.12.10
		BufferedOutputStream bos = null;
		FileLock lock = null;
		try {
			FileOutputStream fos = new FileOutputStream(nextTmpFile, isAppend);
			lock = fos.getChannel().lock();
			bos = new BufferedOutputStream(fos );
			bos.write(html.getBytes(charset) );
		} finally {
			if(lock != null) {
				lock.release();
			}
			if(bos != null) {
				bos.close();
			}
		}
	}
	
	// 重命名给定的文件
	public static boolean renameTo(File src, File dst) {
		if(! src.exists() ) {
			Log.log("src file isn't exists !");
			return false;
		}
		if(dst.exists() ) {
			Log.log("dst file is exists !");
			return false;
		}
		
		return src.renameTo(dst);
	}	
	public static boolean renameTo(String src, String dst) {
		return renameTo(new File(src), new File(dst) );
	}
	
	// 删除给定的文件
	public static boolean delete(File src) {
		if(! src.exists() ) {
			Log.log("src file isn't exists !");
			return false;
		}
		
		return src.delete();
	}
	public static boolean delete(String src) {
		return delete(new File(src) );
	}
	
	// 判断给定的字符串 / 集合 / JSONObject 是否为空
	public static boolean isEmpty(String str) {
		return (str == null) || (EMPTY_STR.equals(str.trim()) ) || NULL.equals(str.trim()); 
	}
	public static boolean isEmpty(Collection coll) {
		return (coll == null) || (coll.size() == 0); 
	}	
	public static boolean isEmpty(JSONObject coll) {
		return (coll == null) || (coll.size() == 0); 
	}	
	
	// 如果给定的对象不为空, 则将其加入给定的JSONObject中
	public static void addIfNotEmpty(JSONObject obj, String key, String val) {
		if(! isEmpty(val)) {
			obj.put(key, val);
		}
	}
	public static void addIfNotEmpty(JSONObject obj, String key, JSONObject jsonObj) {
		if(! isEmpty(jsonObj)) {
			obj.put(key, jsonObj.toString());
		}
	}	
	public static void addIfNotEmpty(JSONObject obj, String key, Object valObj) {
		if(valObj != null) {
			obj.put(key, valObj.toString());
		}
	}	
	
	// 获取发布博客成功之后的响应消息
	public static String getPostSuccMsg(Blog newBlog) {
		return "post \"" + newBlog.getTitle() + "\" success ...";
	}
	public static String getPostFailedMsg(Blog newBlog) {
		return "post \"" + newBlog.getTitle() + "\" failed ...";
	}
	
	// 获取删除博客成功之后的响应消息
	public static String getDeleteSuccMsg(Blog newBlog) {
		return "delete \"" + newBlog.getTitle() + "\" success ...";
	}
	public static String getDeleteFailedMsg(Blog newBlog) {
		return "delete \"" + newBlog.getTitle() + "\" failed ...";
	}
	
	// 需要过滤的tag
	private static Set<String> tagFilter = new HashSet<>();
	static {
		tagFilter.add(BlogManager.ALL);
	}
	
	// 获取JSONArray的字符串表示 [字符串带双引号]
	public static String tagsToString(List<String> arr) {
		if(arr.size() == 0) {
			return "[]";
		}
		
		JSONArray tags = new JSONArray();
		for(String tag : arr) {
			if(! tagFilter.contains(tag)) {
				tags.add(tag);
			}
		}
		return tags.toString();
	}
	
	// 获取JSONArray的字符串表示 [字符串不带双引号]
		// 第一个while循环是为了定位到第一个不为tagFilter中的标签
		// 然后  将之后的不在tagFilter中的tag添加到sb中
	public static <T> String tagsToStringTripBracket(List<T> arr, boolean isAppendSingleQuotion) {
		if(arr.size() == 0) {
			return EMPTY_STR;
		}
		
		StringBuilder sb = new StringBuilder();
		int idx = 0;
		while(idx < arr.size() && tagFilter.contains(arr.get(idx)) ) {
			idx ++;
		}
		if(idx < arr.size() ) {
			if(isAppendSingleQuotion) {
				sb.append(Tools.QUOTION);
			}
			sb.append(arr.get(idx).toString() );
			if(isAppendSingleQuotion) {
				sb.append(Tools.QUOTION);
			}
		}
		for(int i=idx+1; i<arr.size(); i++) {
			String tag = arr.get(i).toString();
			if(! tagFilter.contains(tag)) {
				sb.append(tagsSep);
				if(isAppendSingleQuotion) {
					sb.append(Tools.QUOTION);
				}
				sb.append(tag);
				if(isAppendSingleQuotion) {
					sb.append(Tools.QUOTION);
				}
			}
		}
		return sb.toString();
	}
	
	// 获取删除给定的blog, tags的sql
	public static String getDeleteSelectedBlogsSql(List<Blog> deletedBlog) {
		JSONArray arr = new JSONArray();
		for(Blog blog : deletedBlog) {
			arr.element(blog.getId() );
		}
		String blogIds = arr.toString();
		String in = blogIds.substring(1, blogIds.length()-1 );
		return String.format(Constants.deleteMultiBlogListSql, in);
	}
//	public static String getDeleteSelectedTagsSql(Integer blogId, List<String> deletedTag) {
//		String in = tagsToStringTripBracket(deletedTag, true);
//		return String.format(Constants.deleteMultiTagListSql, blogId, in);
//	}
	// 删除评论 by blogIdx, floorIdx, commentIdx
	public static String getDeleteCommentByBlogIdxSql(Integer blogId) {
		return String.format(Constants.deleteCommentByBlogIdxSql, blogId);
	}	
	public static String getDeleteCommentByFloorIdxSql(Integer blogId, List<Integer> floorIdxes) {
		String in = tagsToStringTripBracket(floorIdxes, false);
		return String.format(Constants.deleteCommentByFloorIdxSql, blogId, in);
	}	
	public static String getDeleteCommentByCommentIdxSql(Integer blogId, List<Integer> commentIdxes) {
		String in = tagsToStringTripBracket(commentIdxes, false);
		return String.format(Constants.deleteCommentByCommentIdxSql, blogId, in);
	}		
	
	// 获取添加给定的blog, tags的sql
//	public static String getAddSelectedBlogsSql(Map<Integer, Blog> addedBlog, List<Integer> addOrder) {
//		if(addedBlog.size() < 1) {
//			return EMPTY_STR;
//		}
//		
//		StringBuilder sb = new StringBuilder();
//		for(Integer blogId : addOrder) {
//			Blog blog = addedBlog.get(blogId);
//			sb.append(" select ");	
//			sb.append(blog.getId());	sb.append(" , '");
//			sb.append(blog.getPath());	sb.append("' , '");
//			sb.append(tagsToStringTripBracket(blog.getTags()) );	sb.append("' , '");
//			sb.append(blog.getCreateTime());	sb.append("' , '");
//			sb.append(blog.getGood());	sb.append("' , '");
//			sb.append(blog.getNotGood());	sb.append("' , '");
//			sb.append(blog.getVisited());	
//			sb.append("' union all ");
//		}
//		
//		String unionAll = sb.substring(0, sb.lastIndexOf("union"));
//		return String.format(Constants.addMultiBlogListSql, unionAll);
//	}
	public static String getAddSelectedBlogsSql(List<Blog> addedBlog) {
		if(addedBlog.size() < 1) {
			return EMPTY_STR;
		}
		
		StringBuilder sb = new StringBuilder();
		for(Blog blog : addedBlog) {
			sb.append(" select ");	
			sb.append(blog.getId());	sb.append(" , '");
			sb.append(blog.getPath());	sb.append("' , '");
			sb.append(tagsToStringTripBracket(blog.getTags(), false) );	sb.append("' , '");
			sb.append(blog.getCreateTime());	sb.append("' , '");
			sb.append(blog.getGood());	sb.append("' , '");
			sb.append(blog.getNotGood());	sb.append("' , '");
			sb.append(blog.getVisited());	sb.append("' , '");
			sb.append(blog.getCommentsNum());
			sb.append("' union all ");
		}
		
		String unionAll = sb.substring(0, sb.lastIndexOf("union"));
		return String.format(Constants.addMultiBlogListSql, unionAll);
	}	
//	public static String getAddSelectedTagsSql(Integer blogId, List<String> tags) {
//		if(tags.size() < 1) {
//			return EMPTY_STR;
//		}
//		
//		StringBuilder sb = new StringBuilder();
//		for(String tag : tags) {
//			if(! tagFilter.contains(tag)) {
//				sb.append(" select '");	
//				sb.append(tag);	sb.append("', ");
//				sb.append(blogId);	
//				sb.append(" union all ");
//			}
//		}
//		
//		String unionAll = sb.substring(0, sb.lastIndexOf("union"));
//		return String.format(Constants.addMultiTagListSql, unionAll);
//	}
	public static String getUpdateBlogListSql(Integer blogId, Blog blog) {
		return String.format(Constants.updateBlogListSql, blog.getPath(), 
				tagsToStringTripBracket(blog.getTags(), false), blog.getGood(), blog.getNotGood(),
				blog.getVisited(), blog.getCommentsNum(), blog.getId() );
	}
	public static String getAddSelectedCommentsSql(List<Comment> addedComments) {
		if(addedComments.size() < 1) {
			return EMPTY_STR;
		}
		
		StringBuilder sb = new StringBuilder();
		for(Comment comment : addedComments) {
			sb.append(" select ");	
			sb.append(comment.getBlogIdx());	sb.append(" , ");
			sb.append(comment.getFloorIdx());	sb.append(" , ");
			sb.append(comment.getCommentIdx());	sb.append(" , '");
			sb.append(comment.getUserInfo().getName());	sb.append("' , '");
			sb.append(comment.getUserInfo().getEmail());	sb.append("' , ");
			sb.append(comment.getUserInfo().getImageIdx());	sb.append(" , '");
			sb.append(comment.getDate());	sb.append("' , '");
			sb.append(comment.getTo());	sb.append("' , '");
			sb.append(comment.getUserInfo().getPrivilege());	sb.append("' , '");
			sb.append(Tools.transfer(comment.getComment(), Constants.needToBeFormatMap) );	
			sb.append("' union all ");
		}
		
		String unionAll = sb.substring(0, sb.lastIndexOf("union"));
		return String.format(Constants.addMultiCommentListSql, unionAll);
	}	
	
	// 判断给定的两个字符串是否相同 [忽略大小写]
	public static boolean equalsIgnorecase(String str01, String str02) {
		return str01.toUpperCase().equals(str02.toUpperCase() );
	}
	
	// 打印日志 [如果到达logBuffer的阈值, 则刷出日志到给定的logFile]
	public static void log(Object obj, Object content) {
		if(Constants.debugEnable || Constants.logEnable) {
			String timeNow = Constants.dateFormat.format(new Date());
			String info = obj.getClass().getName() + " -> " + timeNow + " : " + content.toString();
			
			if(Constants.debugEnable) {
				Log.log(info );
			}
			
			if(Constants.logEnable) {
				logBuffer.append(info);
				logBuffer.append(CRLF);
				if(logBuffer.length() > logThreshold) {
					flushLog(timeNow);
				}
			}
		}
	}
	
	// 获取缓存的日志大小
	public static int getLogBufferSize() {
		return logBuffer.length();
	}
	
	// 刷新日志文件 [如果需要切换日志文件, 则切换]
	public static int flushLog(String timeNow) {
		int logLength = logBuffer.length();
		String flushInfo = Tools.class.getName() + " -> " + timeNow + " : flushed log : " + getKByteByByte(logLength) + " kb !"; 
		logBuffer.append(flushInfo);
		logBuffer.append(CRLF);
		Log.log(flushInfo);
		String appendLog = logBuffer.toString();
		logBuffer.setLength(0);
		
		try {
			Tools.append(appendLog, logFile);
		} catch (IOException e) {
			e.printStackTrace();
			Log.err("write log error at : " + timeNow);
		}
		if(shouldChangeLogFile(logFile)) {
			logFile = getNextLogTmpPath();
		}
		
		return logLength;
	}
	
	// 是否应该切换日志文件了
	private static boolean shouldChangeLogFile(String logFile) {
		File log = new File(logFile);
		if(log.length() >= Constants.logFileThreshold) {
			return true;
		}
		
		return false;
	}

	// 空格类字符
	static Set<Character> spaces = new HashSet<>();
	static {
		spaces.add(SPACE);
		spaces.add(TAB);
		spaces.add(CR);
		spaces.add(LF);
	}
	
	// 将字符串的多个连续的空格转换为一个空格
	// 思路 : 如果str为null  直接返回null
	// 将str中多个相邻的空格替换为一个空格[SPACE]
	// 如果结果的字符串长度为1 并且该字符为空格, 则直接返回空字符串
	// 否则  去掉前后的空格, 返回之间的子字符串
	public static String replaceMultiSpacesAsOne(String str) {
		if(str == null) {
			return null;
		}
		
		char[] chars = str.toCharArray();
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<chars.length; i++) {
			if(spaces.contains(chars[i])) {
				sb.append(SPACE);
				int nextI = i+1;
				while((nextI < chars.length) && spaces.contains(chars[nextI])) nextI++ ;
				i = nextI - 1;
				continue ;
			}
			sb.append(chars[i]);
		}
		
		if((sb.length() == 0) || ((sb.length() == 1) && spaces.contains(sb.charAt(0))) ) {
			return EMPTY_STR;
		} else {
			int start = 0, end = sb.length();
			if(spaces.contains(sb.charAt(start)) ) {
				start ++;
			}
			if(spaces.contains(sb.charAt(end-1)) ) {
				end --;
			}
			
			return sb.substring(start, end);
		}
	}
	
	// 通过字节数, 获取千字节数
	public static long getKByteByByte(int bytes) {
		return bytes >> 10;
	}
	
	// 判断给定的用户请求是否登录
	public static boolean isLogin(ServletRequest req) {
		
		HttpSession session = ((HttpServletRequest) req).getSession();
		if(session == null) {
			return false;
		}
		if((! Constants.ACCOUNT.equals(session.getAttribute(Constants._ACCOUNT_NAME))) || (! Constants.TOKEN.equals(session.getAttribute(Constants._TOKEN))) ) {
			return false;
		}
		
		return true;
	}
	
	// 通过request获取ip
	public static String getIPAddr(ServletRequest request) {
		HttpServletRequest req = (HttpServletRequest) request;
		String ip = req.getHeader("x-forwarded-for");
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("WL-Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getRemoteAddr();
		}

		return ip;
	}
	
	// 从req.session中获取attr对应过的值
	public static String getStrFromSession(ServletRequest req, String attr) {
		HttpSession session = ((HttpServletRequest) req).getSession(false);
		String res = EMPTY_STR;
		if(session != null) {
			res = String.valueOf(session.getAttribute(attr) );
		}
		
		return res;
	}
	public static Object getAttrFromSession(ServletRequest req, String attr) {
		HttpSession session = ((HttpServletRequest) req).getSession(false);
		Object res = null;
		if(session != null) {
			res = session.getAttribute(attr);
		}
		
		return res;
	}
	// 移除session中给定的属性
	public static void removeAttrFromSession(ServletRequest req, String attr) {
		HttpSession session = ((HttpServletRequest) req).getSession();
		if(session != null) {
			session.removeAttribute(attr);
		}
	}
	
	
	// 校验是否用户登录
	public static boolean validateUserLogin(ServletRequest req, ResponseMsg respMsg) {
		boolean isLogin = Tools.isLogin(req);
		if(! isLogin) {
			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "sorry, you haven't login, please login first　!", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}
	
	// 校验校验码
	public static boolean validateCheckCode(ServletRequest req, ResponseMsg respMsg) {
		String checkCode = (String) req.getAttribute("checkCode");
		if(Tools.isEmpty(checkCode)) {
			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "sorry, please input checkCode　!", Tools.getIPAddr(req) );
			return false;
		}
		String realCheckCode = Tools.getStrFromSession(req, Constants.checkCode);
		respMsg.setOthers(checkCode);
		if(Tools.isEmpty(realCheckCode) ) {
			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "sorry, you didn't even have a checkCode store here　!", Tools.getIPAddr(req) );
			return false;
		}
		if(! equalsIgnorecase(checkCode, realCheckCode) ) {
			Log.log(checkCode, realCheckCode);
			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "sorry, your checkCode is not right　!", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}
	
	// 校验博客是否合法
	public static boolean validateBlog(ServletRequest req, Blog blog, ResponseMsg respMsg) {
		if(blog == null) {
			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "sorry, have no this blog　!", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}
	
	// 确保给定的Object, String 不为空
	public static boolean validateObjectBeNull(ServletRequest req, Object obj, String key, ResponseMsg respMsg) {
		if(obj == null) {
			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "sorry, " + key + " can't be null　!", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}
	public static boolean validateStringBeNull(ServletRequest req, String str, String key, ResponseMsg respMsg) {
		if(isEmpty(str)) {
			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "sorry, " + key + " can't be null　!", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}	
	
	// 校验用户传输过来的title
	public static boolean validateTitle(ServletRequest req, String title, String key, ResponseMsg respMsg) {
		if(Tools.isEmpty(title) ) {
			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "please input " + key + "　!", Tools.getIPAddr(req) );
			return false;
		}
		if(title.length() > Constants.titleMaxLength) {
			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "your " + key + " is to long [0 - 30], please check it !", Tools.getIPAddr(req) );
			return false;
		}
		Matcher matcher = Constants.specCharPattern.matcher(title);
		if(matcher.matches() ) {
			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "your " + key + " contains special character [eg : '\\/:*?\"<>|'], please check it !", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}
	
	// 校验tags
	public static boolean validateTags(ServletRequest req, String tag, ResponseMsg respMsg) {
		Matcher matcher = Constants.specCharPattern.matcher(tag);
		if(matcher.matches() ) {
			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "your tag contains special character [eg : ! - /], please check it !", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}
	
	// 校验content
	public static boolean validateContent(ServletRequest req, String tag, ResponseMsg respMsg) {
		
		return true;
	}
	
	// 校验userInfo
	public static boolean validateUserInfo(ServletRequest req, UserInfo userInfo, ResponseMsg respMsg) {
		if(! validateTitle(req, userInfo.getName(), "userName", respMsg) ) {
			return false;
		}
		if(! completeMatch(Constants.emailPattern, userInfo.getEmail()) ) {
			respMsg.set(Constants.respFailed, Constants.defaultResponseCode, "userEmail not valid　!", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}
	
	// 校验评论内容
	public static boolean validateCommentBody(ServletRequest req, String commentBody, ResponseMsg respMsg) {
		
		return true;
	}
	
	// 判断给定的字符串是否完全匹配给定的Pattern
	public static boolean completeMatch(Pattern pat, String str) {
		Matcher matcher = pat.matcher(str);
		if(matcher.matches() ) {
			if(str.length() == matcher.group(0).length()) {
				return true;
			}
		}
		
		return false;
	}
	
	// 判断是否评点过 顶 /踩
//	public static boolean validateIsSensed(HttpServletRequest req, Integer blogId, ResponseMsg respMsg) {
//		boolean isSensed = Constants.senseCookieValue.equals(Tools.getCookieByName(req.getCookies(), Tools.getSensedCookieName(blogId)) );
//		if(isSensed) {
//			respMsg.set(false, Constants.defaultResponseCode, "your have already clicked good/ notGood !", Tools.getIPAddr(req) );
//			return false;
//		}
//		
//		return true;
//	}
	
	// 找到名为cookieName的Cookie对应的值
	public static String getCookieValByName(Cookie[] cookies, String cookieName) {
		// fix if there is not exists "cookies" 		2015.12.08
		if((cookies == null) || (cookieName == null) ) {
			return Constants.defaultCookieValue;
		}
		
		String res = Constants.defaultCookieValue;
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals(cookieName) ) {
				res = cookie.getValue();
				break ;
			}
		}
		
		return res;
	}
	public static Cookie getCookieByName(Cookie[] cookies, String cookieName) {
		if((cookies == null) || (cookieName == null) ) {
			return null;
		}
		
		Cookie res = null;
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals(cookieName) ) {
				res = cookie;
				break ;
			}
		}
		
		return res;
	}	
	
	// 获取给定的blogId的访问session的名称
	public static String getVisitedCookieName(Integer blogId) {
		return Constants.visitedCookieName + UNDER_LINE + blogId.toString();
	}
	// 获取点过顶踩的cookie的名称
	public static String getSensedCookieName(Integer blogId) {
		return Constants.senseCookieName + UNDER_LINE + blogId.toString();
	}
	
	// 根据是否登录, 获取头衔
	public static String getPrivilege(boolean isLogin) {
		if(isLogin) {
			return Constants.admin;
		} else {
			return Constants.guest;
		}
	}
	
	// 获取给定的resultSet的记录数 [未用]
		// java.sql.SQLException: ResultSet is TYPE_FORWARD_ONLY
	public static int getRows(ResultSet rs) throws Exception {
		rs.last();
		int rows = rs.getRow();
		rs.beforeFirst();
		return rows;
	}
	
	// 校验给定的comment是否是回复[reply]别人的comment
	public static boolean isReply(String commentBody) {
		int startIdx = commentBody.indexOf(Constants.replyStart);
		int endIdx = commentBody.indexOf(Constants.replyEnd, startIdx + Constants.replyStart.length() );
		if((startIdx == -1) || (endIdx == -1) ) {
			return false;
		}
		
		return true;
	}
	
	// 获取给定的reply的comment的主体内容
	public static String getReplyComment(String commentBody) {
		int endIdx = commentBody.indexOf(Constants.replyEnd);
		if(endIdx > 0) {
			return commentBody.substring(endIdx + Constants.replyEnd.length() );
		}
		
		return EMPTY_STR;
	}
	
	// 封装播客的评论
	public static String encapBlogComments(List<List<Comment>> blogComments) {
		JSONObject res = new JSONObject();
		int idx = 0;
		for(int i=0; i<blogComments.size(); i++) {
			List<Comment> curFloor = blogComments.get(i);
			JSONObject curFloorRes = new JSONObject();
			// .. NullPointer 出现在了测试场景, 因为1楼是在页面上, 没有存放数据库
			if(curFloor != null) {
				for(int j=0; j<curFloor.size(); j++) {
					curFloorRes.element(String.valueOf(j), curFloor.get(j).toString() );
				}
				res.element(String.valueOf(idx ++), curFloorRes.toString() );
			}
		}
		
		return res.toString();
	}
	
	// 初始化res
	public static <T> void init(List<T> res, int size, T initVal) {
		for(int i=0; i<size; i++) {
			res.add(initVal);
		}
	}
	
	// 常量, 以及需要转义的字符
	static Set<Character> needToBeFormat = new HashSet<Character>();
	static {
		needToBeFormat.add(DOUBLE_QUOTION);
		needToBeFormat.add(QUOTION);
		needToBeFormat.add(SLASH);
	}
	
	// 格式化字符串   为每一个'"', '\' 前面加上一个转义字符['\']
	public static String transfer(String xPath01) {
		return transfer(xPath01, TRANSFER_CHAR);
	}	
	public static String transfer(String xPath01, Character transferChar) {
		return transfer(xPath01, needToBeFormat, transferChar);
	}		
	public static String transfer(String xPath01, Set<Character> needToBeFormat, Character transferChar) {
		if(Tools.isEmpty(xPath01)) {
			return EMPTY_STR;
		}
		
		StringBuilder sb = new StringBuilder(xPath01.length());
		for(int i=0; i<xPath01.length(); i++) {
			if(needToBeFormat.contains(xPath01.charAt(i)) ) {
				sb.append(transferChar);
			}
			sb.append(xPath01.charAt(i));
		}
		
//		Log.log(sb.toString());
		return sb.toString();
	}	
	public static String transfer(String xPath01, Map<Character, Character> needToBeFormat) {
		if(Tools.isEmpty(xPath01)) {
			return EMPTY_STR;
		}
		
		StringBuilder sb = new StringBuilder(xPath01.length());
		for(int i=0; i<xPath01.length(); i++) {
			if(needToBeFormat.containsKey(xPath01.charAt(i)) ) {
				sb.append(needToBeFormat.get(xPath01.charAt(i)) );
			}
			sb.append(xPath01.charAt(i));
		}
		
//		Log.log(sb.toString());
		return sb.toString();
	}
	
	// 去除格式化字符串   为每一个'"', '\' 前面加上一个转义字符['\']
	public static String detransfer(String xPath01, Set<Character> needToBeDeformat) {
		if(Tools.isEmpty(xPath01)) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder(xPath01.length());
		for(int i=0; i<xPath01.length(); i++) {
			if(needToBeDeformat.contains(xPath01.charAt(i)) ) {
				sb.append(xPath01.charAt(++ i) );
				continue ;
			}
			sb.append(xPath01.charAt(i));
		}
		
//		Log.log(sb.toString());
		return sb.toString();
	}
	
	// 将commentBody中包含脚本的部分转义掉
	public static String replaceCommentBody(String commentBody, Map<String, String> needToBeFormat) {
		if(Tools.isEmpty(commentBody)) {
			return EMPTY_STR;
		}
		
		StringBuilder sb = new StringBuilder(commentBody.length());
		for(int i=0; i<commentBody.length(); i++) {
			// 为了效率, 这里就仅仅写"替换key"长度为1的数据了, 以后再来更新吧
			String nextCh = String.valueOf(commentBody.charAt(i) );
			if(! needToBeFormat.containsKey(nextCh) ) {
				sb.append(nextCh);
			} else {
				sb.append(needToBeFormat.get(nextCh) );
			}
//			sb.append(commentBody.charAt(i));
//			if(needToBeFormat.containsKey(commentBody.charAt(i)) && ((i+1 >= commentBody.length()) || isAlphaOrInvSlash(commentBody.charAt(i+1))) ) {
//				sb.append(needToBeFormat.get(commentBody.charAt(i)) );
//			}
		}
		
		return sb.toString();
	}
	
	// 判断给定的字符是否是字母
	public static boolean isAlphaOrInvSlash(char ch) {
		return (ch == INV_SLASH || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch<= 'Z') );
	}
	
	// 获取简单的验证码
	public static CheckCode getCheckCode() {
		BufferedImage checkCodeImg = new BufferedImage(80, 30, BufferedImage.TYPE_INT_RGB);
		Graphics g = checkCodeImg.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 80, 30);
		g.setColor(Color.BLACK);
		g.setFont(new Font(null, Font.BOLD, 20));
		
		String checkCodeStr = Tools.makeCheckCode(Constants.checkCodeLength);
		g.drawString(checkCodeStr, 0, 20);
		
		CheckCode checkCode = new CheckCode(checkCodeStr, checkCodeImg);
		return checkCode;
	}
	// 生成验证码
	public static String makeCheckCode(int length) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<length; i++){
			sb.append(Constants.checkCodes.get(ran.nextInt(Constants.checkCodeBackUpSize)) );
		}
		
		return sb.toString();	
	}
	
	// 生成验证码图片
	public static CheckCode getCheckCode(int width, int height, Color bgColor, Font font, int validateCodeLength, List<Character> checkCodes, int minInterference, int interferenceOff) {
		int checkCodeY = height - (height >> 2);
		int checkCodeXOff = width / (validateCodeLength+2);
		
		BufferedImage checkCodeImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = checkCodeImg.getGraphics();
		g.setColor(bgColor);
		g.fillRect(0, 0, width, height);
		g.setFont(font);
		StringBuilder checkCodeStr = new StringBuilder();
		while(checkCodeStr.length() < validateCodeLength){
			g.setColor(randomColor() );
			Character curChar = checkCodes.get(random(checkCodes.size()) );
			g.drawString(curChar.toString(), (checkCodeStr.length() + 1) * checkCodeXOff, checkCodeY);
			checkCodeStr.append(curChar );
		}
		
		//draw the interference line
		int interferenceSize = (ran.nextInt(interferenceOff) + minInterference);
		for(int i = 0; i < interferenceSize; i++){
			g.setColor(randomColor() );
			g.drawLine(random(width), random(height), random(width), random(height));
		}	
		
		CheckCode checkCode = new CheckCode(checkCodeStr.toString(), checkCodeImg);
		return checkCode;
	}
	// 获取给定范围内的随机数
	public static int random(int range) {
		return ran.nextInt(range);
	}
	// 获取一个随机的颜色
	public static Color randomColor() {
		return new Color(random(255)+1, random(255)+1, random(255)+1);
	}	
	
	// 压缩播客的内容
	public static String compressBlogContent(String content) {
		if(content.length() > Constants.logBlogContentMaxLength) {
			return content.substring(0, Constants.logBlogContentMaxLength) + " ...";
		} else {
			return content ;
		}
	}
	
	// 通过产品的数目, 以及每一页显示的产品的数目, 计算页数
	public static int calcPageNums(int productNum, int numPerPage) {
		return ((productNum-1) / numPerPage) + 1;
	}
	
    // 复制指定的文件(夹)
    public static void copy(File src, File dst, boolean isOverride) throws IOException {
        if(! src.exists() ) {
            Log.log(Tools.class, "srcFile \" " + src.getAbsolutePath() + " \" do not exists ...");
            return ;
        }
        if(dst.exists() ) {
        	if(! isOverride) {
        		Log.log(Tools.class, "dstFile \" " + dst.getAbsolutePath() + " \" does exists, please check it ...");
        		return ;
        	} else {
        		Log.log(Tools.class, "dstFile \" " + dst.getAbsolutePath() + " \" does exists, override it ...");
        	}
        }

        if(src.isDirectory() && ((! dst.exists()) || dst.isDirectory()) ) {
        	if(! dst.exists() ) {
        		dst.mkdirs();
        	}
            File[] childs = src.listFiles();
            for(File child : childs) {
            	File dstChild = new File(dst, child.getName() );
            	copy(child, dstChild, isOverride);
            }
            Log.log(Tools.class, "copy folder \" " + src.getAbsolutePath() + " \" -> \" " + dst.getAbsolutePath() + " \" success ...");
        } else if(src.isFile() && ((! dst.exists()) || dst.isFile()) ) {
	        FileInputStream fis = new FileInputStream(src);
	        FileOutputStream fos = new FileOutputStream(dst);
	        copy(fis, fos);
	        Log.log(Tools.class, "copy file \" " + src.getAbsolutePath() + " \" -> \" " + dst.getAbsolutePath() + " \" success ...");
        } else {
        	Log.log(Tools.class, "src & dst must be both 'File' or 'Folder' ! ");
        }
    }
    public static void copy(String src, String dst, boolean isOverride) throws IOException {
        copy(new File(src), new File(dst), isOverride);
    }
    public static void copy(File src, File dst) throws Exception {
    	copy(src, dst, false);
    }
    public static void copy(String src, String dst) throws Exception {
    	copy(src, dst, false);
    }
    
	// 将输入流中的数据 复制到输出流
	public static void copy(InputStream is, OutputStream os, boolean isCloseStream) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		
		try {
			bis = new BufferedInputStream(is);
			bos = new BufferedOutputStream(os);
			int len = 0;
			byte[] buf = new byte[BUFF_SIZE];
			while((len = bis.read(buf)) != -1) {
				bos.write(buf, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(isCloseStream) {
				if(bos != null) {
					try {
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(bis != null) {
					try {
						bis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	public static void copy(InputStream is, OutputStream os) {
		copy(is, os, true);
	}
	
	// ------------ assert相关 ------- 2016.03.22 -------------
	// 工具方法
	// 确保boo为true, 否则 抛出异常
	public static void assert0(String msg) {
		assert0(false, msg);
	}
	public static void assert0(boolean boo, String msg) {
		if(msg == null) {
			Log.err("'msg' can't be null ");
			return ;
		}
		if(! boo) {
			throw new RuntimeException("assert0Exception : " + msg);
		}
	}
	// add at 2016.05.02
	public static void assert0(Exception e) {
		assert0(false, e);
	}
	public static void assert0(boolean boo, Exception e) {
		Tools.assert0(e != null, "'e' can't be null ");
		if(! boo) {
			throw new RuntimeException(e);
		}
	}
	// 确保val 和expected相同, 否则 抛出异常
	public static void assert0(int val, int expect, String errorMsg) {
		assert0(val, expect, true, errorMsg);
	}
	public static void assert0(int val, int expect, boolean isEquals, String errorMsg) {
		if(isEquals ^ (val == expect)) {
			String symbol = null;
			if(isEquals) {
				symbol = "!=";
			} else {
				symbol = "==";
			}
			assert0("assert0Exception : " + val + " " + symbol + ", expected : " + expect + ", MSG : " + errorMsg);
		}
	}
	public static <T> void assert0(T val, T expect, String errorMsg) {
		assert0(val, expect, true, errorMsg);
	}
	public static <T> void assert0(T val, T expect, boolean isEquals, String errorMsg) {
		if(val == null) {
			if(expect != null) {
				assert0("assert0Exception : " + val + " == null, expected : " + expect + ", MSG : " + errorMsg);
			}
		}
		if(isEquals ^ (val.equals(expect)) ) {
			String symbol = null;
			if(isEquals) {
				symbol = "!=";
			} else {
				symbol = "==";
			}
			assert0("assert0Exception : " + String.valueOf(val) + " " + symbol + " " + String.valueOf(expect) + ", expected : " + String.valueOf(expect) + ", MSG : " + errorMsg );
		}
	}
	
	/**
	 * @Name: prepareContent 
	 * @Description: 预处理content
	 *  1. 去掉script
	 *  2. 去掉a.href, iframe.src
	 *  3. 去掉各个事件
	 *  因为html大小写不敏感, 因此这里统一以小写作为标准进行比较[toLowerCase]
	 * @param content
	 * @return  
	 * @Create at 2016-11-12 13:11:36 by '970655147'
	 */
	public static String prepareContent(String content) {
		Document doc = Jsoup.parse(content);
		prepareContent0(doc);
		return doc.toString();
	}
	
	// prepareContent0
	private static void prepareContent0(Element ele) {
		String lowerTagName = ele.tagName().toLowerCase();
		if(Constants.sensetiveTags.contains(lowerTagName) ) {
			ele.remove();
			return ;
		}
		
		// remove specified tag's specified attribute if that contains sensetiveWords
		if(Constants.sensetiveTag2Attr.containsKey(lowerTagName) ) {
			Map<String, List<String>> attr2SensetiveWords = Constants.sensetiveTag2Attr.get(lowerTagName);
			// remove all sensetiveAttrs
			Iterator<Attribute> attrIts = ele.attributes().iterator();
			while(attrIts.hasNext() ) {
				Attribute attr = attrIts.next();
				String lowerAttrKey = attr.getKey().toLowerCase();
				// href = "javascript:alert(1)"
				if(attr2SensetiveWords.containsKey(lowerAttrKey) ) {
					String lowerAttrValue = attr.getValue().toLowerCase();
					for(String sensetiveWord : attr2SensetiveWords.get(lowerAttrKey) ) {
						if(lowerAttrValue.contains(sensetiveWord) ) {
							ele.removeAttr(attr.getKey() );
							break ;
						}
					}
				}
			}
		}
		// remove all sensetiveAttrs
		Iterator<Attribute> attrIts = ele.attributes().iterator();
		while(attrIts.hasNext() ) {
			Attribute attr = attrIts.next();
			if(Constants.sensetiveAttrs.contains(attr.getKey().toLowerCase()) ) {
				ele.removeAttr(attr.getKey() );
			}
		}
		
		for(Element child : ele.children() ) {
			prepareContent0(child);
		}
		// there must be an "#root" node
//		Element nextSib = ele.nextElementSibling();
//		if(nextSib != null) {
//			prepareContent0(nextSib);
//		}
	}
	
	
}