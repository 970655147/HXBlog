package com.hx.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.hx.action.BlogListAction;
import com.hx.bean.Blog;
import com.hx.bean.TagToBlogCnt;
import com.hx.util.Constants;
import com.hx.util.Log;
import com.hx.util.Tools;

public class BlogManager {

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
	// ��ǰblogId�ļ����� [unsafe �̰߳�ȫ]
	// �յ�JSONObject
	private static TagToBlogCnt tagToBlogCntTmp = new TagToBlogCnt();
	private static AtomicInteger curBlogId = new AtomicInteger(0);
		
	// �������µ����� 
	// [0] ��ʾ��ӵĲ��ͼ�¼��, [1] ��ʾ��ӵ�(��ǩ -> ����)�ļ�¼��
	// [2] ��ʾɾ���Ĳ��ͼ�¼��, [3] ��ʾɾ����(��ǩ -> ����)�ļ�¼��
	// [4] ��ʾ���µĲ��͵ļ�¼��
	public final static int[] updated = new int[5];
	
	// ͨ��tag��ȡ��Ӧ�������б�
	// ��ȡtag��Ӧ����blogId�б�
		// ���blogId�б�Ϊnull  �򷵻ؿյ�result 
		// �����ȡ��ǰtag��Ӧ����blog��Ϊ��ʹ��ǰ�Ĳ�����ʾ��ǰ�� [���� ֮����ϸ����]
			// Ȼ�� ��ȡ��ǩ�б�
			// ��󽫱�ǩ�б�, �Ͳ����б��װ����, ����
	public static JSONObject getResByTag(String tag) {
		List<Integer> blogIds = tagList.get(tag);
		JSONArray blogList_ = new JSONArray();
		if(! Tools.isEmpty(blogIds) ) {
			for(int i=blogIds.size()-1; i>=0; i--) {
				JSONObject obj = new JSONObject();
				Blog blog = blogList.get(blogIds.get(i));
				if(blog != null) {
					blog.encapJSON(obj);
					blogList_.add(obj);
				}
			}
		}
		
		boolean foundTag = false;
		JSONArray tagList_ = new JSONArray();
		Iterator<TagToBlogCnt> tagToBlogIt = tagToBlogCnt.iterator();
		while(tagToBlogIt.hasNext() ) {
			JSONObject obj = new JSONObject();
			TagToBlogCnt tagToBlog = tagToBlogIt.next();
			tagToBlog.encapJSON(obj);
			if(tagToBlog.getTag().equals(tag) ) {
				foundTag = true;
			}
			tagList_.add(obj);
		}
		
		String curTag = foundTag ? tag : Constants.defaultBlogTag;
		
		JSONObject res = new JSONObject();
		res.element("tagList", tagList_.toString() );
		res.element("blogList", blogList_.toString() );
		res.element("curTag", curTag);
		return res;
	}

	// ����б�Ҫ�Ļ�, ��ʼ��blogList, tagList
	public static void initIfNeeded() {
		if(needInit) {
			synchronized (initLock) {
				if(needInit) {
					try {
						initLists();
						needInit = false;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	// ��ʼ��blogList, tagList
	private static void initLists() throws Exception {
		Connection con = null;
		try {
			con = Tools.getConnection(Tools.getProjectPath());
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
		} finally {
			if(con != null) {
				con.close();
			}
		}
		
		for(Entry<String, List<Integer>> entry : tagList.entrySet()) {
			tagToBlogCnt.add(new TagToBlogCnt(entry.getKey(), entry.getValue().size()) );
		}
	}
	
	// ��ȡ��һ��blog��id
	public static int nextBlogId() {
		return curBlogId.incrementAndGet();
	}

	public static void publishBlog(Blog newBlog, ServletContext servletContext) {
		publishBlog0(newBlog);
		checkIfNeedFlush(servletContext);
	}
	public static void reviseBlog(Blog newBlog, ServletContext servletContext) {
		reviseBlog0(newBlog);
		checkIfNeedFlush(servletContext);
	}
	public static void deleteBlog(Blog newBlog, ServletContext servletContext) {
		deleteBlog0(newBlog);
		checkIfNeedFlush(servletContext);
	}
	
	// �����һ������
	private static void publishBlog0(Blog newBlog) {
		blogList.put(newBlog.getId(), newBlog);
		if(newBlog.getTags().size() > 0) {
			synchronized (updateLock) {
				List<String> addedTagsOfBlog = addedBlogIdToTagMap.get(newBlog.getId());
				if(addedTagsOfBlog == null) {
					addedTagsOfBlog = new ArrayList<>();
					addedBlogIdToTagMap.put(newBlog.getId(), addedTagsOfBlog);
				}
				
				for(String tag : newBlog.getTags()) {
					addedTagsOfBlog.add(tag);
					updateTagCntInTagToBlogCnt(tag, true);
				}
				addedList.put(newBlog.getId(), newBlog);
			}
			
			for(String tag : newBlog.getTags()) {
				updateBlogIdToTagList(newBlog, tag, true);
			}
		}
		
		// ---------------------------------------------------
	}
	// ������һ��blog������
	private static void reviseBlog0(Blog newBlog) {
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
					deletedBlogIdToTagMap.put(newBlog.getId(), deletedTagsOfBlog);
				}				
				
				for(String tag : addedTags) {
					updateTagCntInTagToBlogCnt(tag, true);
					addedTagsOfBlog.add(tag);
				}
				for(String tag : removedTags) {
					updateTagCntInTagToBlogCnt(tag, false);
					deletedTagsOfBlog.add(tag);
				}	
				updatedList.put(newBlog.getId(), newBlog);
			}
			
			for(String tag : addedTags) {
				updateBlogIdToTagList(newBlog, tag, true);
			}
			for(String tag : removedTags) {
				updateBlogIdToTagList(newBlog, tag, false);
			}			
		}
		
		// ---------------------------------------------------
	}
	// ɾ����һ������
	private static void deleteBlog0(Blog newBlog) {
		blogList.remove(newBlog.getId());
		if(newBlog.getTags().size() > 0) {
			synchronized (updateLock) {
				List<String> deletedTagsOfBlog = deletedBlogIdToTagMap.get(newBlog.getId());
				if(deletedTagsOfBlog == null) {
					deletedTagsOfBlog = new ArrayList<>();
					deletedBlogIdToTagMap.put(newBlog.getId(), deletedTagsOfBlog);
				}	
				
				for(String tag : newBlog.getTags()) {
					deletedTagsOfBlog.add(tag);
					updateTagCntInTagToBlogCnt(tag, false);
				}
				deletedList.put(newBlog.getId(), newBlog);
			}
			
			for(String tag : newBlog.getTags()) {
				updateBlogIdToTagList(newBlog, tag, false);
			}
		}
		
		// ---------------------------------------------------
	}
	private static void checkIfNeedFlush(ServletContext servletContext) {
//		Log.log("updated : " + getUpdated() );
		if(getUpdated() >= Constants.updateThreashold) {
			flushToDB(servletContext);
		}
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
			if(listOfTag.size() == Constants.ZERO) {
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
		if(! tagToBlogCntTmp.getBlogCnt().equals(Constants.INTE_ZERO) ) {
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
		// ֮ǰ��ʹ��addList, reviseList, ... �ȴ�ˢ�µ����ݿ�������ͷ�updateLock
		// ���ڸ���Ϊ����addList, reviseList, ..�Ļ���, ����֮��, ֱ���ͷ�updateLock 
	public static void flushToDB(ServletContext servletContext) {
		int updated = getUpdated();
		if(updated > 0) {
			Map<Integer, Blog> addedListTmp = new HashMap<>(); 
			Map<Integer, Blog> deletedListTmp = new HashMap<>(); 
			Map<Integer, Blog> updatedListTmp = new HashMap<>();
			Map<Integer, List<String>> addedBlogIdToTagMapTmp = new HashMap<Integer, List<String>>();
			Map<Integer, List<String>> deletedBlogIdToTagMapTmp = new HashMap<Integer, List<String>>();
			addedListTmp.putAll(addedList);
			deletedListTmp.putAll(deletedList);
			updatedListTmp.putAll(updatedList);
			addedList.clear();
			updatedList.clear();
			deletedList.clear();			
			synchronized (updateLock) {
				addedBlogIdToTagMapTmp.putAll(addedBlogIdToTagMap);
				deletedBlogIdToTagMapTmp.putAll(deletedBlogIdToTagMap);
				addedBlogIdToTagMap.clear();
				deletedBlogIdToTagMap.clear();
			}
			
			synchronized (initLock) {
				Connection con = null;
				try {
					con = Tools.getConnection(Tools.getProjectPath(servletContext));			
					flushAddedRecords(con, addedListTmp, addedBlogIdToTagMapTmp);
					flushDeletedRecords(con, deletedListTmp, deletedBlogIdToTagMapTmp);
					flushRevisedRecords(con, updatedListTmp);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if(con != null) {
						try {
							con.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}					
				}
			}
			
			Tools.log(BlogListAction.class, getFlushInfo() );
		}		
	}

	// ˢ����ӵļ�¼��db
	private static void flushAddedRecords(Connection con, Map<Integer, Blog> addedList, Map<Integer, List<String>> addedBlogIdToTagMap) throws Exception {
		updated[Constants.addBlogCnt] = 0;
		updated[Constants.addTagCnt] = 0;
		if(addedList.size() > 0) {		
			String addSelectedSql = Tools.getAddSelectedBlogsSql(addedList);
			Log.log(addSelectedSql);
			PreparedStatement delBlogPs = con.prepareStatement(addSelectedSql);
			updated[Constants.addBlogCnt] = delBlogPs.executeUpdate();
		}
		
		if(addedBlogIdToTagMap.size() > 0) {
			String addSelectedSql = null;
			PreparedStatement addTagsPs = null;
			for(Entry<Integer, List<String>> entry : addedBlogIdToTagMap.entrySet()) {
				if(entry.getValue().size() > 0) {
					addSelectedSql = Tools.getAddSelectedTagsSql(entry.getKey(), entry.getValue());
					Log.log(addSelectedSql);
					addTagsPs = con.prepareStatement(addSelectedSql);
					updated[Constants.addTagCnt] += addTagsPs.executeUpdate();
				}
			}
		}
		
		Tools.log(BlogListAction.class, "added " + updated[Constants.addBlogCnt] + " blogRecoreds to db , added " + updated[Constants.addTagCnt] + " tagRecoreds to db !");
	}	
	
	// ˢ��ɾ���ļ�¼��db
	private static void flushDeletedRecords(Connection con, Map<Integer, Blog> deletedList, Map<Integer, List<String>> deletedBlogIdToTagMap) throws Exception {
		updated[Constants.deletedBlogCnt] = 0;
		updated[Constants.deletedTagCnt] = 0;
		if(deletedList.size() > 0) {
			String deleteSelectedSql = Tools.getDeleteSelectedBlogsSql(deletedList);
			Log.log(deleteSelectedSql);
			PreparedStatement delBlogPs = con.prepareStatement(deleteSelectedSql);
			updated[Constants.deletedBlogCnt] = delBlogPs.executeUpdate();
		}
		
		if(deletedBlogIdToTagMap.size() > 0) {
			String deleteSelectedSql = null;
			PreparedStatement delTagsPs = null;
			for(Entry<Integer, List<String>> entry : deletedBlogIdToTagMap.entrySet()) {
				if(entry.getValue().size() > 0) {
					deleteSelectedSql = Tools.getDeleteSelectedTagsSql(entry.getKey(), entry.getValue());
					Log.log(deleteSelectedSql);
					delTagsPs = con.prepareStatement(deleteSelectedSql);
					updated[Constants.deletedTagCnt] += delTagsPs.executeUpdate();
				}
			}
		}
		
		Tools.log(BlogListAction.class, "deleted " + updated[Constants.deletedBlogCnt] + " blogRecoreds int db , deleted " + updated[Constants.deletedTagCnt] + " tagRecoreds int db !");
	}
	
	// ˢ�¸��µļ�¼��db
	private static void flushRevisedRecords(Connection con, Map<Integer, Blog> updatedList) throws Exception {
		updated[Constants.revisedBlogCnt] = 0;
		if(updatedList.size() > 0) {
			String updateSelectedSql = null;
			PreparedStatement updateTagsPs = null;
			for(Entry<Integer, Blog> entry : updatedList.entrySet()) {
				updateSelectedSql = Tools.getUpdateBlogListSql(entry.getKey(), entry.getValue());
				Log.log(updateSelectedSql);
				updateTagsPs = con.prepareStatement(updateSelectedSql);
				updated[Constants.revisedBlogCnt] += updateTagsPs.executeUpdate();
			}
		}
		
		Tools.log(BlogListAction.class, "updated " + updated[Constants.revisedBlogCnt] + " blogRecoreds to db !");
	}
	
	// ��ȡ���µļ�¼��Ϣ
	private static String getFlushInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("addBlog : ");	sb.append(updated[Constants.addBlogCnt] );
		sb.append(", addTag : ");	sb.append(updated[Constants.addTagCnt] );
		sb.append(", revisedBlog : ");	sb.append(updated[Constants.revisedBlogCnt] );
		sb.append(", deletedBlog : ");	sb.append(updated[Constants.deletedBlogCnt] );
		sb.append(", deletedTag : ");	sb.append(updated[Constants.deletedTagCnt] );
		
		return sb.toString();
	}
	
	// ���ݲ��͵�id��tag��ȡ���͵�����
	public static int getBlogIdxByIdAndTag(Integer id, String tag) {
		List<Integer> blogIds = tagList.get(tag);
		if(blogIds == null) {
			return Constants.HAVE_NO_THIS_TAG;
		}
		
		return blogIds.indexOf(id);
	}
	
	// ͨ��idx ��tag��ȡ��Ӧ��blogId
	public static int getBlogIdByIdxAndTag(int idx, String tag) {
		List<Integer> blogIds = tagList.get(tag);
		if(idx < 0) {
			return Constants.HAVE_NO_NEXT_IDX;
		}
		if(idx >= blogIds.size()) {
			return Constants.HAVE_NO_PREV_IDX;
		}
		
		return blogIds.get(idx).intValue();
	}
	
}
