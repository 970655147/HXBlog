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
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletContext;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.hx.action.BlogListAction;
import com.hx.bean.Blog;

// ���ߺ���
public class Tools {
	
	// ����
	public static String EMPTY_STR = "";
	public static String NULL = "null";
	public static final Character SLASH = '\\';
	public static final Character INV_SLASH = '/';
	private static final Character DOT = '.';
	private static final Character SPACE = ' ';
	private static final Character TAB = '\t';
	private static final Character CR = '\r';
	private static final Character LF = '\n';
	private static final Character QUESTION = '?';
	public static final String CRLF = "\r\n";
	public static Random ran = new Random();
	public static String DEFAULT_CHARSET = "GBK";
	
	// ��׺���
	public static String HTML = ".html";
	public static String JAVA = ".java";
	public static String TXT = ".txt";
	public static String PNG = ".png";
	public static String JPEG = ".jpeg";
	public static String JS = ".js";
	public static String MAP = ".map";
	
	// ����jdbc
	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
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
	public static String getProjectPath(ServletContext servletContext) {
		return servletContext.getRealPath("/");
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
		
		String[] splits = tagListStr.split(tagsSep);
		List<String> res = new ArrayList<>(splits.length);
		for(String tag : splits) {
			res.add(tag);
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
	public static void addIfNotEmpty(JSONObject obj, String key, String val) {
		if(! isEmpty(val)) {
			obj.put(key, val);
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
		tagFilter.add(BlogListAction.ALL);
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
	
	// �жϸ����������ַ����Ƿ���ͬ
	public static boolean equalsIgnorecase(String str01, String str02) {
		return str01.toUpperCase().equals(str02.toUpperCase() );
	}
	
	// ��ӡ��־
	public static void log(Object obj, Object content) {
		Log.log(obj.getClass().getName() + " -> " + Constants.dateFormat.format(new Date()) + " : " + content.toString() );
	}
	
}