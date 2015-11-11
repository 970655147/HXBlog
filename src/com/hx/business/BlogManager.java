package com.hx.business;

import java.io.IOException;
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
import com.hx.util.Tools;

public class BlogManager {

	// blogList, tagList, tag��tag��Ӧ�Ĳ������ļ���
	// ���ӵĲ��͵ļ��� , ���ӵĲ��͵�˳��[��Ҫά��], ���µĲ��͵ļ���, ����sense, visited�Ĳ��ͼ���,  ɾ���Ĳ��͵ļ���
	// ���ӵ�(blogId -> [tag])��ӳ��, ɾ����(blogId -> [tag])��ӳ��
	// �Ƿ���Ҫ��ʼ��, "all", ͬ���õĶ���
	private static Map<Integer, Blog> blogList = new ConcurrentHashMap<>();
	private static Map<String, List<Integer>> tagList = new ConcurrentHashMap<>();
	private static Set<TagToBlogCnt> tagToBlogCnt = new TreeSet<>();
	private static List<Blog> addedList = new ArrayList<>();
//	private static Map<Integer, Blog> addedList = new ConcurrentHashMap<>();
//	private static List<Integer> addOrder = new ArrayList<>(Constants.addedBlogsListSize);
	private static Map<Integer, Blog> updatedList = new ConcurrentHashMap<>();
	private static Map<Integer, Blog> visitSenseUpdatedList = new ConcurrentHashMap<>();
	private static List<Blog> deletedList = new ArrayList<>();
//	private static Map<Integer, Blog> deletedList = new ConcurrentHashMap<>();
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
	public static int[] updated = new int[5];
	
	// ͨ��tag��ȡ��Ӧ�������б�
	// ��ȡtag��Ӧ����blogId�б�
	// ���blogId�б�Ϊnull  �򷵻ؿյ�result 
	// �����ȡ��ǰtag��Ӧ����blog��Ϊ��ʹ���µĲ�����ʾ��ǰ�� [���� ֮����ϸ����]
		// Ȼ�� ��ȡ��ǩ�б�
		// ��󽫱�ǩ�б�, �Ͳ����б��װ����, ����
	public static JSONObject getResByTag(String tag, int pageNo) throws IOException {
		List<Integer> blogIds = tagList.get(tag);
		JSONArray blogList_ = new JSONArray();
		int pageSum = 0;
		if(! Tools.isEmpty(blogIds) ) {
			pageSum = Tools.calcPageNums(blogIds.size(), Constants.blogPerPage);
			if(pageNo >=0 && pageNo <= pageSum) {
				int end = blogIds.size()-1 - ((pageNo-1) * Constants.blogPerPage);
				int startTmp = end - Constants.blogPerPage + 1;
				int start =  startTmp >= 0 ? startTmp : 0;
				for(int i=end; i>=start; i--) {
					JSONObject obj = new JSONObject();
					Blog blog = blogList.get(blogIds.get(i));
					if(blog != null) {
						blog.encapJSON(obj);
						blogList_.add(obj);
					}
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
		res.element("pageSum", pageSum );
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
	
	// ��ȡ��һ��blog��id [BlogPublishAction]
	public static int nextBlogId() {
		return curBlogId.incrementAndGet();
	}

	// ��������, ���²���, ɾ������
	public static void publishBlog(Blog newBlog) {
		publishBlog0(newBlog);
		checkIfNeedFlush();
	}
	public static void reviseBlog(Blog newBlog) {
		reviseBlog0(newBlog);
		checkIfNeedFlush();
	}
	public static void deleteBlog(Blog newBlog) {
		deleteBlog0(newBlog);
		checkIfNeedFlush();
	}
	
	// ���blog�� visitSenseUpdatedList [�����˶���, ����visited]
	public static void addVisitSense(Blog blog) {
		visitSenseUpdatedList.put(blog.getId(), blog);
	}
	
	// ͨ��id��ȡ��Ӧ��blog [�����¸�blog��ʹ��Ƶ��]
	public static Blog getBlog(Integer id) {
		CommentManager.incBlogGetFrequency(id);
		return blogList.get(id);
	}
	
	// ��ȡ���µ�Ԫ�صĸ��� [����, ���ȸ��µĲ���]
	public static int getUpdated() {
		return addedList.size() + updatedList.size() + deletedList.size();
	}
	
	// ��ȡ���µĶ��� ����visited��blog�ĸ���
	public static int getVistitedSensedUpdate() {
		return visitSenseUpdatedList.size();
	}	
	
	// �����µ�����ˢ�µ����ݿ�
		// ˢ�����ݿ��ڼ�, ����������ݿ���в���
		// �����������µĵ�Ԫ�صĸ���, Ȼ�������Ÿ���Ԫ�ص����� [�Ա��ھ���ظ�ҵ��]
		// ֮ǰ��ʹ��addList, reviseList, ... �ȴ�ˢ�µ����ݿ�������ͷ�updateLock
		// ���ڸ���Ϊ����addList, reviseList, ..�Ļ���, ����֮��, ֱ���ͷ�updateLock 
	public static void flushToDB() {
		int updated = getUpdated();
		if(updated > 0) {
//			Map<Integer, Blog> addedListTmp = new HashMap<>(); 
//			Map<Integer, Blog> deletedListTmp = new HashMap<>(); 
			List<Blog> addedListTmp = new ArrayList<>(); 
			List<Blog> deletedListTmp = new ArrayList<>(); 
			Map<Integer, Blog> updatedListTmp = new HashMap<>();
			Map<Integer, Blog> visitSenseUpdatedListTmp = new HashMap<>();
//			List<Integer> addOrderTmp = new ArrayList<>();
			Map<Integer, List<String>> addedBlogIdToTagMapTmp = new HashMap<>();
			Map<Integer, List<String>> deletedBlogIdToTagMapTmp = new HashMap<>();
//			addedListTmp.putAll(addedList);
//			deletedListTmp.putAll(deletedList);
			updatedListTmp.putAll(updatedList);
			visitSenseUpdatedListTmp.putAll(visitSenseUpdatedList);
//			addedList.clear();
//			deletedList.clear();	
			updatedList.clear();
			visitSenseUpdatedList.clear();
			synchronized (updateLock) {
//				addOrderTmp.addAll(addOrder);
				addedListTmp.addAll(addedList);
				deletedListTmp.addAll(deletedList);
				addedBlogIdToTagMapTmp.putAll(addedBlogIdToTagMap);
				deletedBlogIdToTagMapTmp.putAll(deletedBlogIdToTagMap);
				
//				addOrder.clear();
				addedList.clear();
				deletedList.clear();
				addedBlogIdToTagMap.clear();
				deletedBlogIdToTagMap.clear();
			}
			
			synchronized (initLock) {
				Connection con = null;
				try {
					con = Tools.getConnection(Tools.getProjectPath());			
//					flushAddedRecords(con, addedListTmp, addOrderTmp, addedBlogIdToTagMapTmp);
					flushAddedRecords(con, addedListTmp, addedBlogIdToTagMapTmp);
					flushRevisedRecords(con, updatedListTmp, visitSenseUpdatedListTmp);
//					flushDeletedRecords(con, deletedListTmp, deletedBlogIdToTagMapTmp);
					flushDeletedRecords(con, deletedListTmp, deletedBlogIdToTagMapTmp);
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
	
	// ˢ�¸����˷�����, ���ȵĲ��� �����ݵ����ݿ�
		// ����һ������, ����ֻ������flushToDB����������ˢ��updatedList�޸ĵ�ʱ��, ɾ��visitSenseUpdatedList�ж�Ӧ�Ĳ��� [�����ζ�ͬһ��blog�����޸� [����sql][Ч��]] [�μ�flushRevisedRecords]
		// ����û�п���flushToDBForVisitedSensed���������, ��û��ɾ��updatedList�ж�Ӧ���Ĳ��� [���ܻ��һ�����ͽ��ж����������޸�]
	public static void flushToDBForVisitedSensed(ServletContext servletContext) {
		int updated = getVistitedSensedUpdate();
		if(updated > 0) {
			Map<Integer, Blog> visitSenseUpdatedListTmp = new HashMap<>();
			visitSenseUpdatedListTmp.putAll(visitSenseUpdatedList);
			visitSenseUpdatedList.clear();
			
			synchronized (initLock) {
				Connection con = null;
				try {
					con = Tools.getConnection(Tools.getProjectPath(servletContext));			
					flushRevisedRecords(con, null, visitSenseUpdatedListTmp);
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
		}		
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
	
	// context destory��ʱ������ռ�õ��ڴ�
	public static void clear() {
		blogList.clear();
		tagList.clear();
		updatedList.clear();
		visitSenseUpdatedList.clear();
		blogList = null;
		tagList = null;
		updatedList = null;
		visitSenseUpdatedList = null;
		
		synchronized (updateLock) {
			tagToBlogCnt.clear();
			addedList.clear();
			deletedList.clear();
			addedBlogIdToTagMap.clear();
			deletedBlogIdToTagMap.clear();
			tagToBlogCnt = null;
			addedList = null;
			deletedList = null;
			addedBlogIdToTagMap = null;
			deletedBlogIdToTagMap = null;
			initLock = null;
			tagToBlogCntTmp = null;
			curBlogId = null;
			updated = null;
		}
		updateLock = null;
	}	
	
	// ��ʼ��blogList, tagList, �Լ�tagToBlogCnt [TreeSet ά��˳��]
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
	
	// �����һ������
	// ��������ӵ�blogList
	// addedList, ��Ӹ��µı�ǩӳ��addedBlogIdToTagMap [����֮��ˢ�µ����ݿ�]
		// ����tagToBlogCnt�и���  [ҵ��]
	// ��ӵ�ǰ���͵����еı�ǩ�����Եı�ǩ����[tagList]
	private static void publishBlog0(Blog newBlog) {
		blogList.put(newBlog.getId(), newBlog);
		synchronized (updateLock) {
//			addOrder.add(newBlog.getId() );
//			addedList.put(newBlog.getId(), newBlog);
			addedList.add(newBlog);
			if(newBlog.getTags().size() > 0) {
				List<String> addedTagsOfBlog = addedBlogIdToTagMap.get(newBlog.getId());
				if(addedTagsOfBlog == null) {
					addedTagsOfBlog = new ArrayList<>();
					addedBlogIdToTagMap.put(newBlog.getId(), addedTagsOfBlog);
				}
				
				for(String tag : newBlog.getTags()) {
					addedTagsOfBlog.add(tag);
					updateTagCntInTagToBlogCnt(tag, true);
				}
			}
		}
		
		for(String tag : newBlog.getTags()) {
			updateBlogIdToTagList(newBlog, tag, true);
		}
		
		// ---------------------------------------------------
	}
	// ������һ��blog������
	// ��ȡ�����˵ı�ǩ, �Լ�ɾ���˵ı�ǩ
	// �����ӵı�ǩ��ӵ���ӵı�ǩӳ�� addedBlogIdToTagMap [����֮��ˢ�µ����ݿ�]
		// ����tagToBlogCnt�и��� 	[ҵ��]
	// ��ɾ���ı�ǩ��ӵ���ӵı�ǩӳ�� deletedBlogIdToTagMap [����֮��ˢ�µ����ݿ�]
		// ����tagToBlogCnt�и��� 	[ҵ��]
	// ��ӵ�ǰ���͵����е����ӱ�ǩ�����Եı�ǩ����[tagList]
	// ɾ����ǰ���͵����е�ɾ����ǩ�����Եı�ǩ����[tagList]
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
					addedTagsOfBlog.add(tag);
					updateTagCntInTagToBlogCnt(tag, true);
				}
				for(String tag : removedTags) {
					deletedTagsOfBlog.add(tag);
					updateTagCntInTagToBlogCnt(tag, false);
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
	// �����ʹ�blogList��ɾ��
	// deletedList, ���ɾ���ı�ǩӳ��deletedBlogIdToTagMap [����֮��ˢ�µ����ݿ�]
		// ����tagToBlogCnt�и���  [ҵ��]
	// ɾ����ǰ���͵����еı�ǩ�����Եı�ǩ����[tagList]	
	private static void deleteBlog0(Blog newBlog) {
		blogList.remove(newBlog.getId());
		if(newBlog.getTags().size() > 0) {
			synchronized (updateLock) {
//				deletedList.put(newBlog.getId(), newBlog);
				deletedList.add(newBlog);
				List<String> deletedTagsOfBlog = deletedBlogIdToTagMap.get(newBlog.getId());
				if(deletedTagsOfBlog == null) {
					deletedTagsOfBlog = new ArrayList<>();
					deletedBlogIdToTagMap.put(newBlog.getId(), deletedTagsOfBlog);
				}	
				
				for(String tag : newBlog.getTags()) {
					deletedTagsOfBlog.add(tag);
					updateTagCntInTagToBlogCnt(tag, false);
				}
			}
			
			for(String tag : newBlog.getTags()) {
				updateBlogIdToTagList(newBlog, tag, false);
			}
		}
		
		CommentManager.delete(newBlog);
		// ---------------------------------------------------
	}
	
	// ����Ƿ���Ҫˢ����������ݵ����ݿ�
		// ��������˸���blog����ֵ, �򽫵�ǰ����ĸ���ˢ�µ����ݿ�
	private static void checkIfNeedFlush() {
//		Log.log("updated : " + getUpdated() );
		if(getUpdated() >= Constants.updateBlogThreashold) {
			flushToDB();
		}
	}	
	
	// ��newBlog ��� / ɾ�� ��tag��Ӧ��tagList��
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
	private static void updateTagCntInTagToBlogCnt (String tag, boolean isAdd) {
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
	
	// ˢ����ӵļ�¼��db [blogList, tagList]
	private static void flushAddedRecords(Connection con, List<Blog> addedList, Map<Integer, List<String>> addedBlogIdToTagMap) throws Exception {
		updated[Constants.addBlogCnt] = 0;
		updated[Constants.addTagCnt] = 0;
		if((addedList != null) && (addedList.size() > 0) ) {		
//			String addSelectedSql = Tools.getAddSelectedBlogsSql(addedList, addOrder);
			String addSelectedSql = Tools.getAddSelectedBlogsSql(addedList);
			Tools.log(BlogManager.class, addSelectedSql);
			PreparedStatement delBlogPs = con.prepareStatement(addSelectedSql);
			updated[Constants.addBlogCnt] = delBlogPs.executeUpdate();
		}
		
		if((addedBlogIdToTagMap != null) && (addedBlogIdToTagMap.size() > 0) ) {
			String addSelectedSql = null;
			PreparedStatement addTagsPs = null;
			for(Entry<Integer, List<String>> entry : addedBlogIdToTagMap.entrySet()) {
				if(entry.getValue().size() > 0) {
					addSelectedSql = Tools.getAddSelectedTagsSql(entry.getKey(), entry.getValue());
					Tools.log(BlogManager.class, addSelectedSql);
					addTagsPs = con.prepareStatement(addSelectedSql);
					updated[Constants.addTagCnt] += addTagsPs.executeUpdate();
				}
			}
		}
		
		Tools.log(BlogListAction.class, "added " + updated[Constants.addBlogCnt] + " blogRecoreds to db , added " + updated[Constants.addTagCnt] + " tagRecoreds to db !");
	}	
	
	// ˢ��ɾ���ļ�¼��db [blogList, tagList]
	private static void flushDeletedRecords(Connection con, List<Blog> deletedList, Map<Integer, List<String>> deletedBlogIdToTagMap) throws Exception {
		updated[Constants.deletedBlogCnt] = 0;
		updated[Constants.deletedTagCnt] = 0;
		if((deletedList != null) && (deletedList.size() > 0) ) {
			String deleteSelectedSql = Tools.getDeleteSelectedBlogsSql(deletedList);
			Tools.log(BlogManager.class, deleteSelectedSql);
			PreparedStatement delBlogPs = con.prepareStatement(deleteSelectedSql);
			updated[Constants.deletedBlogCnt] = delBlogPs.executeUpdate();
		}
		
		if((deletedBlogIdToTagMap != null) && (deletedBlogIdToTagMap.size() > 0) ) {
			String deleteSelectedSql = null;
			PreparedStatement delTagsPs = null;
			for(Entry<Integer, List<String>> entry : deletedBlogIdToTagMap.entrySet()) {
				if(entry.getValue().size() > 0) {
					deleteSelectedSql = Tools.getDeleteSelectedTagsSql(entry.getKey(), entry.getValue());
					Tools.log(BlogManager.class, deleteSelectedSql);
					delTagsPs = con.prepareStatement(deleteSelectedSql);
					updated[Constants.deletedTagCnt] += delTagsPs.executeUpdate();
				}
			}
		}
		
		Tools.log(BlogListAction.class, "deleted " + updated[Constants.deletedBlogCnt] + " blogRecoreds int db , deleted " + updated[Constants.deletedTagCnt] + " tagRecoreds int db !");
	}
	
	// ˢ�¸��µļ�¼��db [blogList]
	private static void flushRevisedRecords(Connection con, Map<Integer, Blog> updatedList, Map<Integer, Blog> visitSenseUpdatedListTmp) throws Exception {
		updated[Constants.revisedBlogCnt] = 0;
		if((updatedList != null) && (updatedList.size() > 0) ) {
			String updateSelectedSql = null;
			PreparedStatement updateTagsPs = null;
			for(Entry<Integer, Blog> entry : updatedList.entrySet()) {
				visitSenseUpdatedListTmp.remove(entry.getKey() );
				updateSelectedSql = Tools.getUpdateBlogListSql(entry.getKey(), entry.getValue());
				Tools.log(BlogManager.class, updateSelectedSql);
				updateTagsPs = con.prepareStatement(updateSelectedSql);
				updated[Constants.revisedBlogCnt] += updateTagsPs.executeUpdate();
			}
		}
		
		if((visitSenseUpdatedListTmp != null) && (visitSenseUpdatedListTmp.size() > 0) ) {
			String updateSelectedSql = null;
			PreparedStatement updateTagsPs = null;
			for(Entry<Integer, Blog> entry : visitSenseUpdatedListTmp.entrySet()) {
				updateSelectedSql = Tools.getUpdateBlogListSql(entry.getKey(), entry.getValue());
				Tools.log(BlogManager.class, updateSelectedSql);
				updateTagsPs = con.prepareStatement(updateSelectedSql);
				updated[Constants.revisedBlogCnt] += updateTagsPs.executeUpdate();
			}
		}
		
		Tools.log(BlogListAction.class, "updated " + updated[Constants.revisedBlogCnt] + " blogRecoreds to db !");
	}
	
	// ��ȡ���µļ�¼��Ϣ [��־]
	private static String getFlushInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("addBlog : ");	sb.append(updated[Constants.addBlogCnt] );
		sb.append(", addTag : ");	sb.append(updated[Constants.addTagCnt] );
		sb.append(", revisedBlog : ");	sb.append(updated[Constants.revisedBlogCnt] );
		sb.append(", deletedBlog : ");	sb.append(updated[Constants.deletedBlogCnt] );
		sb.append(", deletedTag : ");	sb.append(updated[Constants.deletedTagCnt] );
		
		return sb.toString();
	}
	
}
