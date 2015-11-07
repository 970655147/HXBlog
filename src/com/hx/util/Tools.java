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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.hx.bean.Blog;
import com.hx.bean.Comment;
import com.hx.bean.ResponseMsg;
import com.hx.business.BlogManager;

// ���ߺ���
public class Tools {
	
	// ����
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
	public static final String UNDER_LINE = "_";
	public static final String CRLF = "\r\n";
	public final static Random ran = new Random();
	public final static String DEFAULT_CHARSET = "GBK";
	
	// ��Ŀ����־buffer, ��ֵ[128kb], ��������С[(128 + 16)kb], ��־�ļ�
	public final static int logThreshold = 128 << 10;
	public final static int logBuffSize = logThreshold + (logThreshold >> 3);
	public static StringBuffer logBuffer = null;
	public static String projectPath = null;
	public static String logFile = null;
	
	// ��׺���
	public final static String HTML = ".html";
	public final static String JAVA = ".java";
	public final static String TXT = ".txt";
	public final static String PNG = ".png";
	public final static String JPEG = ".jpeg";
	public final static String JS = ".js";
	public final static String MAP = ".map";
	public final static String LOG = ".log";
	
	// ��־�ļ����
	public static AtomicInteger TMP_IDX = new AtomicInteger(0);
	public static String TMP_DIR = null;
	public static String TMP_NAME = "log";
	public static String SUFFIX = LOG;
	
	// ����jdbc
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
	
	// ---------------��־�ļ����---------------
	// ��ȡ��ʱ·������һ��·��[�����ļ�·��]
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
	
	// ��ȡ��ʱ�ļ�����һ������[�����ļ�����]
	private static String getNextTmpName() {
		return TMP_NAME + (TMP_IDX.getAndIncrement() ) + SUFFIX;
	}
	private static String getNextTmpName(String suffix) {
		return TMP_NAME + (TMP_IDX.getAndIncrement() ) + suffix;
	}
	
	// ��ȡ�������������е��ַ�����
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
	
	// �жϸ�����·�����ļ��Ƿ����
	public static boolean isFileExists(String path) {	
		File file = new File(path);
		return file.exists();
	}
	
	// ��ȡ���е�·����Ӧ���ļ���·��
	public static String getPackagePath(String basePath, String packagePath) {
		return basePath + "WEB-INF/classes" + Tools.SLASH + packagePath;
	}
	// ��ȡ�����Ĳ��͵ĵ�ַ
	public static String getBlogPath(String basePath, String blogFileName) {
		return basePath + Constants.blogFolder + Tools.SLASH + blogFileName + HTML;
	}	
	
	// ��ȡ�����ݿ������
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
	
	// ��ȡ��ǰ��Ŀ�ĵ�ַ
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
	
	// ����tag�ķָ���
	static String tagsSep = ",";
	static String dateBlogSep = "__dateBlogSep__";
	
	// ��ȡ������tagList���ַ�����ʾ / ��tagList�ַ�����ȡ������tagList
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
			if(! Tools.isEmpty(tag) ) {
				res.add(tag );
			}
		}
		
		return res;
	}
	
	// ���ݸ���������, ����title, ��ȡ��Ӧ�ô洢���ļ���
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
	
	// ��html�ַ������浽ָ�����ļ���
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
	
	// �������������ļ�
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
	
	// ɾ���������ļ�
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
	
	// �жϸ������ַ����Ƿ�Ϊ��
	public static boolean isEmpty(String str) {
		return (str == null) || (EMPTY_STR.equals(str.trim()) ) || NULL.equals(str.trim()); 
	}
	public static boolean isEmpty(Collection coll) {
		return (coll == null) || (coll.size() == 0); 
	}	
	public static boolean isEmpty(JSONObject coll) {
		return (coll == null) || (coll.size() == 0); 
	}	
	
	// ��������Ķ���Ϊ��, ������������JSONObject��
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
	
	// ��ȡ�������ͳɹ�֮�����Ӧ��Ϣ
	public static String getPostSuccMsg(Blog newBlog) {
		return "post \"" + newBlog.getTitle() + "\" success ...";
	}
	public static String getPostFailedMsg(Blog newBlog) {
		return "post \"" + newBlog.getTitle() + "\" failed ...";
	}
	// ��ȡɾ�����ͳɹ�֮�����Ӧ��Ϣ
	public static String getDeleteSuccMsg(Blog newBlog) {
		return "delete \"" + newBlog.getTitle() + "\" success ...";
	}
	public static String getDeleteFailedMsg(Blog newBlog) {
		return "delete \"" + newBlog.getTitle() + "\" failed ...";
	}
	
	// ��Ҫ���˵�tag
	private static Set<String> tagFilter = new HashSet<>();
	static {
		tagFilter.add(BlogManager.ALL);
	}
	
	// ��ȡJSONArray���ַ�����ʾ [�ַ�����˫����]
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
	
	// ��ȡJSONArray���ַ�����ʾ [�ַ�������˫����]
		// ��һ��whileѭ����Ϊ�˶�λ����һ����ΪtagFilter�еı�ǩ
		// Ȼ��  ��֮��Ĳ���tagFilter�е�tag��ӵ�sb��
	public static String tagsToStringTripBracket(List<String> arr) {
		if(arr.size() == 0) {
			return EMPTY_STR;
		}
		
		StringBuilder sb = new StringBuilder();
		int idx = 0;
		while(idx < arr.size() && tagFilter.contains(arr.get(idx)) ) {
			idx ++;
		}
		if(idx < arr.size() ) {
			sb.append(arr.get(idx) );
		}
		for(int i=idx+1; i<arr.size(); i++) {
			String tag = arr.get(i);
			if(! tagFilter.contains(tag)) {
				sb.append(tagsSep);		sb.append(tag);
			}
		}
		return sb.toString();
	}
	
	// ��ȡɾ��������blog, tags��sql
	public static String getDeleteSelectedBlogsSql(List<Blog> deletedBlog) {
		JSONArray arr = new JSONArray();
		for(Blog blog : deletedBlog) {
			arr.element(blog.getId() );
		}
		String blogIds = arr.toString();
		String in = blogIds.substring(1, blogIds.length()-1 );
		return String.format(Constants.deleteMultiBlogListSql, in);
	}
	public static String getDeleteSelectedTagsSql(Integer blogId, List<String> deletedTag) {
		String in = tagsToStringTripBracket(deletedTag);
		return String.format(Constants.deleteMultiTagListSql, blogId, in);
	}
	
	// ��ȡ��Ӹ�����blog, tags��sql
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
			sb.append(tagsToStringTripBracket(blog.getTags()) );	sb.append("' , '");
			sb.append(blog.getCreateTime());	sb.append("' , '");
			sb.append(blog.getGood());	sb.append("' , '");
			sb.append(blog.getNotGood());	sb.append("' , '");
			sb.append(blog.getVisited());	
			sb.append("' union all ");
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
				sb.append(" select '");	
				sb.append(tag);	sb.append("', ");
				sb.append(blogId);	
				sb.append(" union all ");
			}
		}
		
		String unionAll = sb.substring(0, sb.lastIndexOf("union"));
		return String.format(Constants.addMultiTagListSql, unionAll);
	}
	public static String getUpdateBlogListSql(Integer blogId, Blog blog) {
		return String.format(Constants.updateBlogListSql, blog.getPath(), tagsToStringTripBracket(blog.getTags()), blog.getGood(), blog.getNotGood(), blog.getVisited(), blog.getId());
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
			sb.append(comment.getComment());	
			sb.append("' union all ");
		}
		
		String unionAll = sb.substring(0, sb.lastIndexOf("union"));
		return String.format(Constants.addMultiCommentListSql, unionAll);
	}	
	
	// �жϸ����������ַ����Ƿ���ͬ
	public static boolean equalsIgnorecase(String str01, String str02) {
		return str01.toUpperCase().equals(str02.toUpperCase() );
	}
	
	// ��ӡ��־ [�������logBuffer����ֵ, ��ˢ����־��������logFile]
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
	
	// ��ȡ�������־��С
	public static int getLogBufferSize() {
		return logBuffer.length();
	}
	
	// ˢ����־�ļ� [�����Ҫ�л���־�ļ�, ���л�]
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
			logFile = getNextTmpPath();
		}
		
		return logLength;
	}
	
	// �Ƿ�Ӧ���л���־�ļ���
	private static boolean shouldChangeLogFile(String logFile) {
		File log = new File(logFile);
		if(log.length() >= Constants.logFileThreshold) {
			return true;
		}
		
		return false;
	}

	// �ո����ַ�
	static Set<Character> spaces = new HashSet<>();
	static {
		spaces.add(SPACE);
		spaces.add(TAB);
		spaces.add(CR);
		spaces.add(LF);
	}
	
	// ���ַ����Ķ�������Ŀո�ת��Ϊһ���ո�
	// ˼· : ���strΪnull  ֱ�ӷ���null
	// ��str�ж�����ڵĿո��滻Ϊһ���ո�[SPACE]
	// ���������ַ�������Ϊ1 ���Ҹ��ַ�Ϊ�ո�, ��ֱ�ӷ��ؿ��ַ���
	// ����  ȥ��ǰ��Ŀո�, ����֮������ַ���
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
	
	// ͨ���ֽ���, ��ȡǧ�ֽ���
	public static long getKByteByByte(int bytes) {
		return bytes >> 10;
	}
	
	// �жϸ������û������Ƿ��¼
	public static boolean isLogin(HttpServletRequest req) {
		HttpSession session = req.getSession();
		if(session == null) {
			return false;
		}
		if((! Constants.account.equals(session.getAttribute(Constants.ACCOUNT_NAME))) || (! Constants.token.equals(session.getAttribute(Constants.TOKEN))) ) {
			return false;
		}
		
		return true;
	}
	
	// ͨ��request��ȡip
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
	
	// ��req.session�л�ȡattr��Ӧ����ֵ
	public static String getStrFromSession(HttpServletRequest req, String attr) {
		HttpSession session = req.getSession(false);
		String res = NULL;
		if(session != null) {
			res = String.valueOf(session.getAttribute(attr) );
		}
		
		return res;
	}
	public static Object getAttrFromSession(HttpServletRequest req, String attr) {
		HttpSession session = req.getSession(false);
		Object res = null;
		if(session != null) {
			res = session.getAttribute(attr);
		}
		
		return res;
	}
	
	// У���Ƿ��û���¼
	public static boolean validateUserLogin(HttpServletRequest req, ResponseMsg respMsg) {
		boolean isLogin = Tools.isLogin(req);
		if(! isLogin) {
			respMsg.set(false, Constants.defaultResponseCode, "sorry, you haven't login, please login first��!", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}
	
	// У�鲩���Ƿ�Ϸ�
	public static boolean validateBlog(HttpServletRequest req, Blog blog, ResponseMsg respMsg) {
		if(blog == null) {
			respMsg.set(false, Constants.defaultResponseCode, "sorry, have no this blog��!", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}
	public static boolean validateObjectBeNull(HttpServletRequest req, Object obj, String key, ResponseMsg respMsg) {
		if(obj == null) {
			respMsg.set(false, Constants.defaultResponseCode, "sorry, " + key + " can't be null��!", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}
	public static boolean validateStringBeNull(HttpServletRequest req, String str, String key, ResponseMsg respMsg) {
		if(isEmpty(str)) {
			respMsg.set(false, Constants.defaultResponseCode, "sorry, " + key + " can't be null��!", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}	
	
	// У���û����������title
	public static boolean validateTitle(HttpServletRequest req, String title, ResponseMsg respMsg) {
		if(Tools.isEmpty(title) ) {
			respMsg.set(false, Constants.defaultResponseCode, "please input title��!", Tools.getIPAddr(req) );
			return false;
		}
		if(title.length() > Constants.titleMaxLength) {
			respMsg.set(false, Constants.defaultResponseCode, "your title is to long [0 - 30], please check it !", Tools.getIPAddr(req) );
			return false;
		}
		Matcher matcher = Constants.specCharPattern.matcher(title);
		if(matcher.matches() ) {
			respMsg.set(false, Constants.defaultResponseCode, "your title contains special character [eg : ! - /], please check it !", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}
	
	// У��tags
	public static boolean validateTags(HttpServletRequest req, String tag, ResponseMsg respMsg) {
		Matcher matcher = Constants.specCharPattern.matcher(tag);
		if(matcher.matches() ) {
			respMsg.set(false, Constants.defaultResponseCode, "your tag contains special character [eg : ! - /], please check it !", Tools.getIPAddr(req) );
			return false;
		}
		
		return true;
	}
	
	// У��content
	public static boolean validateContent(HttpServletRequest req, String tag, ResponseMsg respMsg) {
		return true;
	}
	
	// �ж��Ƿ������ �� /��
//	public static boolean validateIsSensed(HttpServletRequest req, Integer blogId, ResponseMsg respMsg) {
//		boolean isSensed = Constants.senseCookieValue.equals(Tools.getCookieByName(req.getCookies(), Tools.getSensedCookieName(blogId)) );
//		if(isSensed) {
//			respMsg.set(false, Constants.defaultResponseCode, "your have already clicked good/ notGood !", Tools.getIPAddr(req) );
//			return false;
//		}
//		
//		return true;
//	}
	
	// �ҵ���ΪcookieName��Cookie��Ӧ��ֵ
	public static String getCookieValByName(Cookie[] cookies, String cookieName) {
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
		Cookie res = null;
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals(cookieName) ) {
				res = cookie;
				break ;
			}
		}
		
		return res;
	}	
	
	// ��ȡ������blogId�ķ���session������
	public static String getVisitedCookieName(Integer blogId) {
		return Constants.visitedCookieName + UNDER_LINE + blogId.toString();
	}
	
	// ��ȡ������ȵ�cookie������
	public static String getSensedCookieName(Integer blogId) {
		return Constants.senseCookieName + UNDER_LINE + blogId.toString();
	}
	
	// �����Ƿ��¼, ��ȡͷ��
	public static String getPrivilege(boolean isLogin) {
		if(isLogin) {
			return Constants.admin;
		} else {
			return Constants.guest;
		}
	}
	
	// ��ȡ������resultSet�ļ�¼��
		// java.sql.SQLException: ResultSet is TYPE_FORWARD_ONLY
	public static int getRows(ResultSet rs) throws Exception {
		rs.last();
		int rows = rs.getRow();
		rs.beforeFirst();
		return rows;
	}
	
	// У�������comment�Ƿ��ǻظ����˵�comment
	public static boolean isReply(String commentBody) {
		int startIdx = commentBody.indexOf(Constants.replyStart);
		int endIdx = commentBody.indexOf(Constants.replyEnd, startIdx + Constants.replyStart.length() );
		if((startIdx == -1) || (endIdx == -1) ) {
			return false;
		}
		
		return true;
	}
	
	// ��ȡ������reply��comment����������
	public static String getReplyComment(String commentBody) {
		int endIdx = commentBody.indexOf(Constants.replyEnd);
		if(endIdx > 0) {
			return commentBody.substring(endIdx + Constants.replyEnd.length() );
		}
		
		return EMPTY_STR;
	}
	
	// ��װ���͵�����
	public static String encapBlogComments(List<List<Comment>> blogComments) {
		JSONObject res = new JSONObject();
		for(int i=1; i<blogComments.size(); i++) {
			List<Comment> curFloor = blogComments.get(i);
			JSONObject curFloorRes = new JSONObject();
			// .. NullPointer �������˲��Գ���, ��Ϊ1¥����ҳ����, û�д�����ݿ�
			if(curFloor != null) {
				for(int j=0; j<curFloor.size(); j++) {
					curFloorRes.element(String.valueOf(j), curFloor.get(j).toString() );
				}
			}
			res.element(String.valueOf(i), curFloorRes.toString() );
		}
		
		return res.toString();
	}
	
	// ��ʼ��res
	public static <T> void init(List<T> res, int size, T initVal) {
		for(int i=0; i<size; i++) {
			res.add(initVal);
		}
	}
	
	
}