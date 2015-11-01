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
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServlet;

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
	public static String getProjectPath(HttpServlet servlet) {
		return servlet.getServletContext().getRealPath("/");
	}
	
	// ����tag�ķָ���
	static String tagsSep = ",";
	
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
		String[] splits = tagListStr.split(tagsSep);
		List<String> res = new ArrayList<>(splits.length);
		for(String tag : splits) {
			res.add(tag);
		}
		
		return res;
	}
	
	// ��ȡ�����Ĳ��͵ĵ�ַ
	public static String getBlogPath(String basePath, String path) {
		return basePath + Constants.blogFolder + Tools.SLASH + path;
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
	
}