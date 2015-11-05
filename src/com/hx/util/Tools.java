package com.hx.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.hx.action.BlogListAction;
import com.hx.bean.Blog;

// 工具函数
public class Tools {
	
	// 常量
	public final static String EMPTY_STR = "";
	public final static String NULL = "null";
	public static final Character SLASH = '\\';
	public static final Character INV_SLASH = '/';
	public static final Character DOT = '.';
	public static final Character SPACE = ' ';
	public static final Character TAB = '\t';
	public static final Character CR = '\r';
	public static final Character LF = '\n';
	public static final Character QUESTION = '?';
	public static final String CRLF = "\r\n";
	public final static Random ran = new Random();
	public final static String DEFAULT_CHARSET = "GBK";
	
	// 项目的日志buffer, 阈值[128kb], 缓冲区大小[(128 + 16)kb], 日志文件
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
	public static String TMP_DIR = null;
	public static String TMP_NAME = "log";
	public static String SUFFIX = LOG;
	
	// 加载jdbc
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
	
	// ---------------日志文件相关---------------
	// 获取临时路径的下一个路径[返回文件路径]
	public static void setTmpIdx(int idx) {
		TMP_IDX = new AtomicInteger(idx );
	}
	public static String getNextTmpPath() {
		return TMP_DIR + "\\" + getNextTmpName();
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
	private static String getNextTmpName(String suffix) {
		return TMP_NAME + (TMP_IDX.getAndIncrement() ) + suffix;
	}
	
	// 获取给定的输入流中的字符内容
	public static String getContent(InputStream is, String charset) throws IOException {
		StringBuilder sb = new StringBuilder(is.available() );
		BufferedReader br = new BufferedReader(new InputStreamReader(is, charset) );

		String line = null;
		while((line = br.readLine()) != null) {
			sb.append(line );
		}
		br.close();
		
		return sb.toString();
	}
	public static String getContent(String path, String charset) throws IOException {
		return getContent(new File(path), charset);
	}
	public static String getContent(File file, String charset) throws IOException {
		return getContent(new FileInputStream(file), charset);
	}
	public static String getContent(String path) throws IOException {
		return getContent(new File(path), DEFAULT_CHARSET);
	}
	public static String getContent(File file) throws IOException {
		return getContent(file, DEFAULT_CHARSET);
	}
	public static String getContent(InputStream is) throws IOException {
		return getContent(is, DEFAULT_CHARSET);
	}
	
	// 获取包中的路径对应的文件的路径
	public static String getPackagePath(String basePath, String packagePath) {
		return basePath + "WEB-INF/classes" + Tools.SLASH + packagePath;
	}
	// 获取给定的播客的地址
	public static String getBlogPath(String basePath, String blogFileName) {
		return basePath + Constants.blogFolder + Tools.SLASH + blogFileName + HTML;
	}	
	
	// 获取到数据库的连接
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
	
	// 获取当前项目的地址
	public static String getProjectPath() {
		return projectPath;
	}
	public static String getProjectPath(ServletContext servletContext) {
		return servletContext.getRealPath("/");
	}	
	public static void setProjectPath(ServletContext servletContext) {
		projectPath = servletContext.getRealPath("/");
		TMP_DIR = projectPath + Constants.logFolder;
		logFile = getNextTmpPath();
	}
	
	// 各个tag的分隔符
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
		
		String[] splits = tagListStr.split(tagsSep);
		List<String> res = new ArrayList<>(splits.length);
		for(String tag : splits) {
			res.add(Tools.replaceMultiSpacesAsOne(tag) );
		}
		
		return res;
	}
	
	// 根据给定的日期, 博客title, 获取其应该存储的文件名
	public static String getBlogFileName(String date, String title) {
		return date + dateBlogSep + title;
	}
	public static String[] getDateAndBlogTitleFromFileName(String fileName) {
		String[] dateAndBlogTitle = new String[0];
		int sepIdx = fileName.indexOf(dateBlogSep);
		if(sepIdx >= 0) {
			dateAndBlogTitle = new String[2];
			dateAndBlogTitle[0] = fileName.substring(0, sepIdx);
			dateAndBlogTitle[1] = fileName.substring(sepIdx + dateBlogSep.length());
		}
		
		return dateAndBlogTitle;
	}
	public static String getTitleFromBlogFileName(String fileName) {
		String title = null;
		int sepIdx = fileName.indexOf(dateBlogSep);
		if(sepIdx >= 0) {
			title = fileName.substring(sepIdx + dateBlogSep.length());
		}
		
		return title;
	}
	
	// 将html字符串保存到指定的文件中
	public static void save(String html, String nextTmpName) throws IOException {
		save(html, new File(nextTmpName) );
	}
	public static void save(String html, File nextTmpFile) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(nextTmpFile) );
		bos.write(html.getBytes(DEFAULT_CHARSET) );
		bos.close();
		
		Log.log("save content to \" " + nextTmpFile.getAbsolutePath() + " \" success ...");
	}	
	public static void append(String html, String nextTmpName) throws IOException {
		append(html, new File(nextTmpName) );
	}
	public static void append(String html, File nextTmpFile) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(nextTmpFile, true) );
		bos.write(html.getBytes(DEFAULT_CHARSET) );
		bos.close();
		
		Log.log("append content to \" " + nextTmpFile.getAbsolutePath() + " \" success ...");
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
	
	// 判断给定的字符串是否为空
	public static boolean isEmpty(String str) {
		return (str == null) || (EMPTY_STR.equals(str.trim()) ) || NULL.equals(str.trim()); 
	}
	public static void addIfNotEmpty(JSONObject obj, String key, String val) {
		if(! isEmpty(val)) {
			obj.put(key, val);
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
		tagFilter.add(BlogListAction.ALL);
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
	public static String tagsToStringTripBracket(List<String> arr) {
		if(arr.size() == 0) {
			return EMPTY_STR;
		}
		
		StringBuilder sb = new StringBuilder();
		int idx = 0;
		while(idx < arr.size() && tagFilter.contains(arr.get(idx)) ) {
			idx ++;
		}
		sb.append(arr.get(idx) );
		for(int i=idx+1; i<arr.size(); i++) {
			String tag = arr.get(i);
			if(! tagFilter.contains(tag)) {
				sb.append(tagsSep);		sb.append(tag);
			}
		}
		return sb.toString();
	}
	
	// 获取删除给定的blog, tags的sql
	public static String getDeleteSelectedBlogsSql(Map<Integer, Blog> deletedBlog) {
		JSONArray arr = new JSONArray();
		for(Entry<Integer, Blog> entry : deletedBlog.entrySet()) {
			arr.element(entry.getKey() );
		}
		String blogIds = arr.toString();
		String in = blogIds.substring(1, blogIds.length()-1 );
		return String.format(Constants.deleteMultiBlogListSql, in);
	}
	public static String getDeleteSelectedTagsSql(Integer blogId, List<String> deletedTag) {
		String in = tagsToStringTripBracket(deletedTag);
		return String.format(Constants.deleteMultiTagListSql, blogId, in);
	}
	
	// 获取添加给定的blog, tags的sql
	public static String getAddSelectedBlogsSql(Map<Integer, Blog> addedBlog) {
		if(addedBlog.size() < 1) {
			return EMPTY_STR;
		}
		
		StringBuilder sb = new StringBuilder();
		for(Entry<Integer, Blog> entry : addedBlog.entrySet()) {
			Blog blog = entry.getValue();
			sb.append(" select ");	sb.append(blog.getId());	sb.append(" , '");
			sb.append(blog.getPath());	sb.append("' , '");
			sb.append(tagsToStringTripBracket(blog.getTags()) );	sb.append("' , '");
			sb.append(blog.getCreateTime());	sb.append("' union all ");
		}
		
		String unionAll = sb.substring(0, sb.lastIndexOf("union"));
		return String.format(Constants.addMultiBlogListSql, unionAll);
	}
	public static String getAddSelectedTagsSql(Integer blogId, List<String> tags) {
		if(tags.size() < 1) {
			return EMPTY_STR;
		}
		
		StringBuilder sb = new StringBuilder();
		for(String tag : tags) {
			if(! tagFilter.contains(tag)) {
				sb.append(" select '");	sb.append(tag);	sb.append("', ");
				sb.append(blogId);	sb.append(" union all ");
			}
		}
		
		String unionAll = sb.substring(0, sb.lastIndexOf("union"));
		return String.format(Constants.addMultiTagListSql, unionAll);
	}
	public static String getUpdateBlogListSql(Integer blogId, Blog blog) {
		return String.format(Constants.updateBlogListSql, blog.getPath(), tagsToStringTripBracket(blog.getTags()), blog.getId());
	}
	
	// 判断给定的两个字符串是否相同
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
		String flushInfo = "flushed log : " + getKByteByByte(logLength) + " kb !"; 
		logBuffer.append(flushInfo);
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
			logFile = getNextTmpPath();
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
	public static boolean isLogin(HttpServletRequest req) {
		HttpSession session = req.getSession();
		if(session == null) {
			return false;
		}
		if((! Constants.userName.equals(session.getAttribute("userName"))) || (! Constants.token.equals(session.getAttribute("token"))) ) {
			return false;
		}
		
		return true;
	}
	
	// 通过request获取ip
	public static String getIPAddr(HttpServletRequest request) {
			String ip = request.getHeader("x-forwarded-for");
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}

			return ip;
		}
	
}