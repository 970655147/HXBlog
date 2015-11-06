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

	// blogList, tagList, tag到tag对应的博客数的集合
	// 增加的播客的集合, 更新的播客的集合, 删除的播客的集合
	// 增加的(blogId -> [tag])的映射, 删除的(blogId -> [tag])的映射
	// 是否需要初始化, 同步用的对象
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
	// TagToBlogCnt临时对象 [所有的使用场景均为同一个对象的同步快中, 线程安全]
	// 当前blogId的计数器 [unsafe 线程安全]
	// 空的JSONObject
	private static TagToBlogCnt tagToBlogCntTmp = new TagToBlogCnt();
	private static AtomicInteger curBlogId = new AtomicInteger(0);
		
	// 各个更新的数据 
	// [0] 表示添加的播客记录数, [1] 表示添加的(标签 -> 播客)的记录数
	// [2] 表示删除的播客记录数, [3] 表示删除的(标签 -> 播客)的记录数
	// [4] 表示更新的播客的记录数
	public final static int[] updated = new int[5];
	
	// 通过tag获取响应的数据列表
	// 获取tag对应过的blogId列表
		// 如果blogId列表为null  则返回空的result 
		// 倒序获取当前tag对应过的blog是为了使靠前的播客显示在前面 [这里 之后详细介绍]
			// 然后 获取标签列表
			// 最后将标签列表, 和播客列表封装起来, 返回
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

	// 如果有必要的话, 初始化blogList, tagList
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
	
	// 初始化blogList, tagList
	private static void initLists() throws Exception {
		Connection con = null;
		try {
			con = Tools.getConnection(Tools.getProjectPath());
			blogList.clear();
			tagList.clear();
			tagToBlogCnt.clear();
			PreparedStatement blogPS = con.prepareStatement(Constants.blogListSql);
			PreparedStatement tagListPS = con.prepareStatement(Constants.tagListSql);
			
			// 初始化blogList, tagList, tagToBlogCnt
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
	
	// 获取下一个blog的id
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
	
	// 添加了一个博客
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
	// 更新了一个blog的内容
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
	// 删除了一个博客
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
	
	// 将newBlog添加到tag对应的tagList中
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
	
	// 为给定的tag的播客数量+1 / -1
		// 注意 tagToBlogCntTmp均是同一个对象的同步块中调用的, 所以是线程安全的
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
	
	// 通过id获取对应的blog
	public static Blog getBlog(Integer id) {
		return blogList.get(id);
	}
	
	// 获取更新的元素的个数
	public static int getUpdated() {
		return addedList.size() + updatedList.size() + deletedList.size();
	}
	
	// 将更新的数据刷新到数据库
		// 刷新数据库期间, 不允许对数据库进行操作
		// 之前是使用addList, reviseList, ... 等待刷新到数据库完毕再释放updateLock
		// 现在更新为建立addList, reviseList, ..的缓存, 缓存之后, 直接释放updateLock 
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

	// 刷新添加的记录到db
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
	
	// 刷新删除的记录到db
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
	
	// 刷新更新的记录到db
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
	
	// 获取更新的记录信息
	private static String getFlushInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("addBlog : ");	sb.append(updated[Constants.addBlogCnt] );
		sb.append(", addTag : ");	sb.append(updated[Constants.addTagCnt] );
		sb.append(", revisedBlog : ");	sb.append(updated[Constants.revisedBlogCnt] );
		sb.append(", deletedBlog : ");	sb.append(updated[Constants.deletedBlogCnt] );
		sb.append(", deletedTag : ");	sb.append(updated[Constants.deletedTagCnt] );
		
		return sb.toString();
	}
	
	// 根据播客的id和tag获取博客的索引
	public static int getBlogIdxByIdAndTag(Integer id, String tag) {
		List<Integer> blogIds = tagList.get(tag);
		if(blogIds == null) {
			return Constants.HAVE_NO_THIS_TAG;
		}
		
		return blogIds.indexOf(id);
	}
	
	// 通过idx 和tag获取对应的blogId
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
