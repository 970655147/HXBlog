package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.hx.bean.Blog;
import com.hx.bean.TagToBlogCnt;
import com.hx.util.Constants;
import com.hx.util.Log;
import com.hx.util.Tools;

// ��ȡblog���õ�action
public class BlogListAction extends HttpServlet {

	// blogList, tagList, tag��tag��Ӧ�Ĳ������ļ���
	// �Ƿ���Ҫ��ʼ��, ͬ���õĶ���
	private static Map<Integer, Blog> blogList = new ConcurrentHashMap<>();
	private static Map<String, List<Integer>> tagList = new ConcurrentHashMap<>();
	private static Set<TagToBlogCnt> tagToBlogCnt = new TreeSet<>();
	private static boolean needInit = true;
	public static String ALL = "all";
	private static Object initLock = new Object();
	private static Object updateLock = new Object();
	private static TagToBlogCnt tagToBlogCntTmp = new TagToBlogCnt();
	private static AtomicInteger curBlogId = new AtomicInteger(0);
	
	// ��ȡconfig.conf�е�����
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		initIfNeeded(req);
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		PrintWriter out = resp.getWriter();
		
		String tag = req.getParameter("tag");
		if(tag == null) {
			tag = ALL;
		}
		JSONObject res = getResByTag(tag);
		out.write(res.toString() );
//		Log.log(res.toString() );
		
		out.close();
	}
	
	// ͨ��tag��ȡ��Ӧ�������б�
	private static JSONObject getResByTag(String tag) {
		JSONArray blogList_ = new JSONArray();
		List<Integer> blogIds = tagList.get(tag);
		Iterator<Integer> it = blogIds.iterator();
		while(it.hasNext() ) {
			JSONObject obj = new JSONObject();
			blogList.get(it.next()).encapJSON(obj);
			blogList_.add(obj);
		}
		
		JSONArray tagList_ = new JSONArray();
		Iterator<TagToBlogCnt> tagToBlogIt = tagToBlogCnt.iterator();
		while(tagToBlogIt.hasNext() ) {
			JSONObject obj = new JSONObject();
			tagToBlogIt.next().encapJSON(obj);
			tagList_.add(obj);
		}
		
		JSONObject res = new JSONObject();
		res.element("tagList", tagList_.toString() );
		res.element("blogList", blogList_.toString() );
		return res;
	}

	// ����б�Ҫ�Ļ�, ��ʼ��blogList, tagList
	public static void initIfNeeded(HttpServletRequest req) {
		if(needInit) {
			synchronized (initLock) {
				if(needInit) {
					try {
						initLists(req);
						needInit = false;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	// ��ʼ��blogList, tagList
	private static void initLists(HttpServletRequest req) throws Exception {
		Connection con = Tools.getConnection(Tools.getProjectPath(req));
		blogList.clear();
		tagList.clear();
		tagToBlogCnt.clear();
		PreparedStatement blogPS = con.prepareStatement(Constants.blogListSql);
		PreparedStatement tagListPS = con.prepareStatement(Constants.tagListSql);
		
		// ��ʼ��blogList, tagList, tagToBlogCnt
		List<Integer> allBlogIds = new ArrayList<>();
		ResultSet blogRs = blogPS.executeQuery();
		int maxBlogId = -1;
		while(blogRs.next() ) {
			Blog blog = new Blog();
			blog.init(blogRs);
			blogList.put(blog.getId(), blog);
			allBlogIds.add(blog.getId() );
			maxBlogId = Math.max(maxBlogId, blog.getId());
		}
		curBlogId = new AtomicInteger(maxBlogId);
		
		ResultSet tagListRs = tagListPS.executeQuery();
		while(tagListRs.next() ) {
			String tagId = tagListRs.getString("tag");
			Integer blogId = tagListRs.getInt("blogId");
			if(tagList.containsKey(tagId)) {
				tagList.get(tagId).add(blogId);
			} else {
				List<Integer> blogs = new ArrayList<>();
				blogs.add(blogId);
				tagList.put(tagId, blogs);
			}
		}
		tagList.put(ALL, allBlogIds);
		
		for(Entry<String, List<Integer>> entry : tagList.entrySet()) {
			tagToBlogCnt.add(new TagToBlogCnt(entry.getKey(), entry.getValue().size()) );
		}
	}
	
	// ��ȡ��һ��blog��id
	public static int nextBlogId() {
		return curBlogId.incrementAndGet();
	}

	// �����һ������
	public static void publishBlog(Blog newBlog) {
		blogList.put(newBlog.getId(), newBlog);
		for(String tag : newBlog.getTags()) {
			List<Integer> listOfTag = tagList.get(tag);
			if(listOfTag == null) {
				listOfTag = new ArrayList<>();
				tagList.put(tag, listOfTag);
			}
			listOfTag.add(newBlog.getId() );
		}
		synchronized (updateLock) {
			for(String tag : newBlog.getTags()) {
				tagToBlogCntTmp.setTag(tag);
				tagToBlogCntTmp.setBlogCnt(tagList.get(tag).size()-1);
				tagToBlogCnt.remove(tagToBlogCntTmp );
				tagToBlogCntTmp.incTagCnt();
				tagToBlogCnt.add(new TagToBlogCnt(tagToBlogCntTmp) );
			}
		}
	}
	
}
