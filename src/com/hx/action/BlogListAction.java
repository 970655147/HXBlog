package com.hx.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.hx.bean.Blog;
import com.hx.bean.TagToBlogCnt;
import com.hx.util.Constants;
import com.hx.util.Tools;

// ��ȡblog���õ�action
public class BlogListAction extends HttpServlet {

	// blogList, tagList, tag��tag��Ӧ�Ĳ������ļ���
	// ���ӵĲ��͵ļ���, ���µĲ��͵ļ���, ɾ���Ĳ��͵ļ���
	// ���ӵ�(blogId -> [tag])��ӳ��, ɾ����(blogId -> [tag])��ӳ��
	// �Ƿ���Ҫ��ʼ��, ͬ���õĶ���
	private static Map<Integer, Blog> blogList = new ConcurrentHashMap<>();
	private static Map<String, List<Integer>> tagList = new ConcurrentHashMap<>();
	private static Set<TagToBlogCnt> tagToBlogCnt = new TreeSet<>();
	private static Map<Integer, Blog> addedList = new ConcurrentHashMap<>();
	private static Map<Integer, Blog> updatedList = new ConcurrentHashMap<>();
	private static Map<Integer, Blog> deletedList = new ConcurrentHashMap<>();
	private static Map<Integer, List<String>> addedBlogIdToTagMap = new HashMap<>();
	private static Map<Integer, List<String>> deletedBlogIdToTagMap = new HashMap<>();
	private static boolean needInit = true;
	public final static String ALL = "all";
	public static Object initLock = new Object();
	public static Object updateLock = new Object();
	// TagToBlogCnt��ʱ���� [���е�ʹ�ó�����Ϊͬһ�������ͬ������, �̰߳�ȫ]
	// ��ǰblogId�ļ����� [unsafe �̰߳�ȫ], 0 ����
	// �յ�JSONObject
	private static TagToBlogCnt tagToBlogCntTmp = new TagToBlogCnt();
	private static AtomicInteger curBlogId = new AtomicInteger(0);
	public final static Integer INTE_ZERO = new Integer(0);
	public final static int ZERO = 0;
	public final static JSONObject DEFAULT_RES = new JSONObject().element("tagList", new JSONObject()).element("blogList", new JSONObject() );
	
	// ��ȡconfig.conf�е�����
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		initIfNeeded(req.getServletContext() );
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		PrintWriter out = resp.getWriter();
		
		String tag = req.getParameter("tag");
		if(tag == null) {
			tag = ALL;
		}
		JSONObject res = getResByTag(tag);
		out.write(res.toString() );
		Tools.log(this, res);
		
		out.close();
	}
	
	// ͨ��tag��ȡ��Ӧ�������б�
	private static JSONObject getResByTag(String tag) {
		JSONArray blogList_ = new JSONArray();
		List<Integer> blogIds = tagList.get(tag);
		if(blogIds == null) {
			return DEFAULT_RES;
		}
		
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
	public static void initIfNeeded(ServletContext servletContext) {
		if(needInit) {
			synchronized (initLock) {
				if(needInit) {
					try {
						initLists(servletContext);
						needInit = false;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	// ��ʼ��blogList, tagList
	private static void initLists(ServletContext servletContext) throws Exception {
		Connection con = Tools.getConnection(Tools.getProjectPath(servletContext));
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
		if(newBlog.getTags().size() > 0) {
			synchronized (updateLock) {
				for(String tag : newBlog.getTags()) {
					updateTagCntInTagToBlogCnt(tag, true);
				}
			}
			
			for(String tag : newBlog.getTags()) {
				updateBlogIdToTagList(newBlog, tag, true);
			}
		}
		
		// ---------------------------------------------------
		addedList.put(newBlog.getId(), newBlog);
	}
	// ������һ��blog������
	public static void reviseBlog(Blog newBlog) {
		Blog oldBlog = blogList.put(newBlog.getId(), newBlog);
		
		List<String> addedTags = new ArrayList<>(newBlog.getTags() );
		addedTags.removeAll(oldBlog.getTags() );
		List<String> removedTags = new ArrayList<>(oldBlog.getTags() );
		removedTags.removeAll(newBlog.getTags() );
		if((addedTags.size() > 0) || (removedTags.size() > 0) ) {
			synchronized (updateLock) {
				List<String> addedTagsOfBlog = addedBlogIdToTagMap.get(newBlog.getId());
				List<String> deletedTagsOfBlog = deletedBlogIdToTagMap.get(newBlog.getId());
				if(addedTagsOfBlog == null) {
					addedTagsOfBlog = new ArrayList<>();
					addedBlogIdToTagMap.put(newBlog.getId(), addedTagsOfBlog);
				}
				if(deletedTagsOfBlog == null) {
					deletedTagsOfBlog = new ArrayList<>();
					addedBlogIdToTagMap.put(newBlog.getId(), deletedTagsOfBlog);
				}				
				
				for(String tag : addedTags) {
					updateTagCntInTagToBlogCnt(tag, true);
					addedTagsOfBlog.add(tag);
				}
				for(String tag : removedTags) {
					updateTagCntInTagToBlogCnt(tag, false);
					deletedTagsOfBlog.add(tag);
				}			
			}
			
			for(String tag : addedTags) {
				updateBlogIdToTagList(newBlog, tag, true);
			}
			for(String tag : removedTags) {
				updateBlogIdToTagList(newBlog, tag, false);
			}			
		}
		
		// ---------------------------------------------------
		updatedList.put(newBlog.getId(), newBlog);
	}
	// ɾ����һ������
	public static void deleteBlog(Blog newBlog) {
		blogList.remove(newBlog.getId());
		if(newBlog.getTags().size() > 0) {
			synchronized (updateLock) {
				for(String tag : newBlog.getTags()) {
					updateTagCntInTagToBlogCnt(tag, false);
				}
			}
			
			for(String tag : newBlog.getTags()) {
				updateBlogIdToTagList(newBlog, tag, false);
			}
		}
		
		// ---------------------------------------------------
		deletedList.put(newBlog.getId(), newBlog);
	}
	
	// ��newBlog��ӵ�tag��Ӧ��tagList��
	private static void updateBlogIdToTagList(Blog newBlog, String tag, boolean isAdd) {
		List<Integer> listOfTag = tagList.get(tag);
		if(listOfTag == null) {
			listOfTag = new ArrayList<>();
			tagList.put(tag, listOfTag);
		}
		
		if(isAdd) {
			listOfTag.add(newBlog.getId() );
		} else {
			listOfTag.remove(newBlog.getId() );
			if(listOfTag.size() == ZERO) {
				tagList.remove(tag);
			}
		}
	}
	
	// Ϊ������tag�Ĳ�������+1 / -1
		// ע�� tagToBlogCntTmp����ͬһ�������ͬ�����е��õ�, �������̰߳�ȫ��
	public static void updateTagCntInTagToBlogCnt (String tag, boolean isAdd) {
		List<Integer> listOfTag = tagList.get(tag);
		if(listOfTag == null) {
			listOfTag = new ArrayList<>();
			tagList.put(tag, listOfTag);
		}
		
		tagToBlogCntTmp.setTag(tag);
		tagToBlogCntTmp.setBlogCnt(listOfTag.size());
		tagToBlogCnt.remove(tagToBlogCntTmp );
		if(isAdd) {
			tagToBlogCntTmp.incTagCnt();
		} else {
			tagToBlogCntTmp.decTagCnt();
		}
		if(! tagToBlogCntTmp.getBlogCnt().equals(INTE_ZERO) ) {
			tagToBlogCnt.add(new TagToBlogCnt(tagToBlogCntTmp) );
		}
	}
	
	// ͨ��id��ȡ��Ӧ��blog
	public static Blog getBlog(Integer id) {
		return blogList.get(id);
	}
	
	// ��ȡ���µ�Ԫ�صĸ���
	public static int getUpdated() {
		return addedList.size() + updatedList.size() + deletedList.size();
	}
	
	// �����µ�����ˢ�µ����ݿ�
		// ˢ�����ݿ��ڼ�, ����������ݿ���в���
	public static void flushToDB() {
		synchronized (initLock) {
			
		}
		
		Tools.log(BlogListAction.class, "flush " + getUpdated() + " recoreds to db !");
	}
	
	
}
