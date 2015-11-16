package com.hx.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.hx.action.BlogListAction;
import com.hx.bean.Blog;
import com.hx.bean.Comment;
import com.hx.bean.CommentEntry;
import com.hx.util.Constants;
import com.hx.util.Tools;

// 管理评论
public class CommentManager {

	// 缓存的评论 [一定的个数], 缓存的播客评论的id的结合
	// 播客的id到访问次数的映射
	// 缓存的所有增加的评论, 缓存的增加评论的播客的评论的集合, 增加评论的各个播客id的集合 
	// 同步用的对象
	private static Queue<CommentEntry> cachedComments = new PriorityQueue<>(Constants.cachedComments);
	private static Set<Integer> cachedCommentsBlogId = new HashSet<>();
	private static Map<Integer, Integer> blogGetFrequency = new ConcurrentHashMap<>();
	private static Object updateCacheLock = new Object();
	private static List<Comment> addComments = new ArrayList<>(Constants.addedCommentsListSize);
	private static Object updateAddCommentsLock = new Object();
	private static List<CommentEntry> addBlogsComments = new ArrayList<>(Constants.addedCommentsListSize);
	private static Map<Integer, Integer> addBlogsId = new HashMap<>();
	private static Object updateAddBlogsCommentLock = new Object();
	private static Object dbLock = new Object();
	private static List<Integer> deleteCommentsByBlogIdx = new ArrayList<>(Constants.addedCommentsListSize);
	private static Map<Integer, List<Integer>> deleteCommentsByFloorIdx = new HashMap<>(Constants.addedCommentsListSize);
	private static Map<Integer, List<Integer>> deleteCommentsByCommentIdx = new HashMap<>(Constants.addedCommentsListSize);
	// 这里为了简单, 就将这三个更新的对象关联在一起了..
	private static Object deleteCommentsLock = new Object();
	
	// 各个更新的数据 
	// [0] 表示添加的评论记录数, [1] 表示根据blogIdx删除的记录数
	// [2] 表示根据floorIdx删除的记录数, [3] 表示根据commentIdx删除的记录数
	private static int[] updated = new int[4];
	
	// 初始化 [初始化CommentEntry.blogGetFrequency]
	static {
		CommentEntry.setBlogGetFrequency(blogGetFrequency);
	}
	
	// 增加给定的播客的访问频率 [BlogManager.getBlog 中调用]
	public static void incBlogGetFrequency(Integer blogId) {
		Integer frequncy = blogGetFrequency.get(blogId);
		if(frequncy == null) {
			blogGetFrequency.put(blogId, Constants.INTE_ZERO);
		} else {
			blogGetFrequency.put(blogId, frequncy + 1);
		}
	}
	
	// 获取给定的blogId对应的所有评论
	// 先从缓存中获取 [cachedComments, addBlogsComments]
	// 在从数据库中获取
	public static List<List<Comment>> getCommentByBlogId(Integer blogId) {
		List<List<Comment>> res = getBlogCommentsFromCache(blogId);
		
		if(res == null) {
			try {
				res = getBlogCommentsFromDB(blogId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(res == null) {
			
		}
		
		return res;
	}
	
	// 为blogId对应的播客, 添加一条评论
	// 如果更新的评论个数增加到一定的阈值, 将其刷新到数据库
	public static void addComment(Integer blogId, Comment comment) {
		synchronized (updateAddCommentsLock) {
			addComments.add(comment);
		}
		if(getUpdated() > Constants.updateCommentsThreshold) {
			flushToDB();
		}
	}

	// 获取给定过的播客列表的播客id
	public static Integer getBlogIdByCommentsEntry(CommentEntry entry) {
		List<List<Comment>> comments = entry.getBlogComments();
		if(comments != null) {
			return comments.get(0).get(0).getBlogIdx();
		}
		
		return null;
	}
	
	// 删除给定的blog的评论
	public static void delete(Blog blog) {
		if(cachedCommentsBlogId.contains(blog.getId()) ) {
			synchronized (updateCacheLock) {
				if(cachedCommentsBlogId.contains(blog.getId()) ) {
					Iterator<CommentEntry> it = cachedComments.iterator();
					while(it.hasNext()) {
						CommentEntry blogComments = it.next();
						Integer curBlogId = getBlogIdByCommentsEntry(blogComments);
						if(curBlogId.equals(blog.getId()) ) {
							cachedComments.remove(blogComments);
							cachedCommentsBlogId.remove(blog.getId());
							break ;
						}
					}
				}
			}
		}
		
		if(addBlogsId.containsKey(blog.getId()) ) {
			synchronized (updateAddBlogsCommentLock) {
				if(addBlogsId.containsKey(blog.getId()) ) {
					addBlogsComments.remove(addBlogsId.get(blog.getId()) );
					addBlogsId.remove(blog.getId() );
				}
			}
		}
		
		synchronized (deleteCommentsLock) {
			deleteCommentsByBlogIdx.add(blog.getId() );
		}
		
	}
	
	// 更新当前评论内容的commentIdx
	// 分为在addBlogsComments, cachedComments, 数据库中获取
	// 然后 更新当前comment的commentIdx
		// 如果当前blog的评论不在addBlogsComments 将其加入addBlogsComments
	public static void updateCommentIdx(Comment comment) throws Exception {
		if(addBlogsId.containsKey(comment.getBlogIdx()) ) {
			List<List<Comment>> blogComments = addBlogsComments.get(addBlogsId.get(comment.getBlogIdx()) ).getBlogComments();
			updateCommentIdx0(blogComments, comment, false);
			return ;
		}
		if(cachedCommentsBlogId.contains(comment.getBlogIdx()) ) {
			List<List<Comment>> blogComments = getBlogCommentsFromCache(comment.getBlogIdx());
			updateCommentIdx0(blogComments, comment, true);
			return ;
		}
		
		List<List<Comment>> blogComments = getBlogCommentsFromDB(comment.getBlogIdx() );
		updateCommentIdx0(blogComments, comment, false);
	}
	
	// 更新当前评论内容的floorIdx
	// 分为在addBlogsComments, cachedComments, 数据库中获取
	// 然后 更新当前comment的floorIdx
		// 如果当前blog的评论不在addBlogsComments 将其加入addBlogsComments	
	public static void updateFloorIdx(Comment comment) throws Exception {
		if(addBlogsId.containsKey(comment.getBlogIdx()) ) {
			List<List<Comment>> blogComments = addBlogsComments.get(addBlogsId.get(comment.getBlogIdx()) ).getBlogComments();
			updateFloorIdx0(blogComments, comment, false);
			return ;
		}
		if(cachedCommentsBlogId.contains(comment.getBlogIdx()) ) {
			updateFloorIdx0(getBlogCommentsFromCache(comment.getBlogIdx()), comment, true);
			return ;
		}
		
		List<List<Comment>> blogComments = getBlogCommentsFromDB(comment.getBlogIdx() );
		updateFloorIdx0(blogComments, comment, false);
	}
	
	// 获取更新的comment的个数 加上删除的blog的个数
	public static int getUpdated() {
		return addComments.size() + deleteCommentsByBlogIdx.size();
	}
	
	// 刷新更新的数据到数据库
	// 将addComments中的数据 缓存到临时空间中, 并清理addComments, addBlogsComments, addBlogsId
	// 然后将更新的comment刷新到数据库中
	public static void flushToDB() {
		int updated = getUpdated();
		if(updated > 0) {
			List<Comment> addedCommentTmp = new ArrayList<>(); 
			synchronized (updateAddCommentsLock) {
				addedCommentTmp.addAll(addComments);
				addComments.clear();
			}
			synchronized (updateAddBlogsCommentLock) {
				addBlogsComments.clear();
				addBlogsId.clear();
			}
			List<Integer> deleteCommentsByBlogIdxTmp = new ArrayList<>();
			Map<Integer, List<Integer>> deleteCommentsByFloorIdxTmp = new HashMap<>();
			Map<Integer, List<Integer>> deleteCommentsByCommentIdxTmp = new HashMap<>();
			synchronized (deleteCommentsLock) {
				deleteCommentsByBlogIdxTmp.addAll(deleteCommentsByBlogIdx);
				deleteCommentsByFloorIdxTmp.putAll(deleteCommentsByFloorIdx);
				deleteCommentsByCommentIdxTmp.putAll(deleteCommentsByCommentIdx);
				deleteCommentsByBlogIdx.clear();
				deleteCommentsByFloorIdx.clear();
				deleteCommentsByCommentIdx.clear();
			}
			
			synchronized (dbLock) {
				Connection con = null;
				try {
					con = Tools.getConnection(Tools.getProjectPath());			
					flushAddedRecords(con, addedCommentTmp);
					flushDeletedRecords(con, deleteCommentsByBlogIdxTmp, deleteCommentsByFloorIdxTmp, deleteCommentsByCommentIdxTmp);
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

	// 清理(blogId -> visitFrequency), 由ContextListener定期清理
	public static void clearFrequencyMap() {
		blogGetFrequency.clear();
	}
	
	// context destory的时候清理占用的内存
	public static void clear() {
		blogGetFrequency.clear();
		blogGetFrequency = null;
		synchronized (updateCacheLock) {
			cachedComments.clear();
			cachedCommentsBlogId.clear();
			cachedComments = null;
			cachedCommentsBlogId = null;
		}
		updateCacheLock = null;
		synchronized (updateAddCommentsLock) {
			addComments.clear();
			addComments = null;
		}
		updateAddCommentsLock = null;
		synchronized (updateAddBlogsCommentLock) {
			addBlogsComments.clear();
			addBlogsId.clear();
			addBlogsComments = null;
			addBlogsId = null;
		}
		updateAddBlogsCommentLock = null;
		dbLock = null;
	}

	// 从缓存中获取blogId对应的评论 [cachedComments, addBlogsComments]
	// 先从addBlogsComments中获取 [快一点 map获取索引, 直接随机存取] [如果当前blogId可以装入缓存中, 则将其装入缓存]
	// 然后再从 cachedComments中获取
	// 返回结果
	private static List<List<Comment>> getBlogCommentsFromCache(Integer blogId) {
		List<List<Comment>> res = null;
		if(addBlogsId.containsKey(blogId)) {
			synchronized (updateAddBlogsCommentLock) {
				res = addBlogsComments.get(addBlogsId.get(blogId) ).getBlogComments();
			}
			
			checkIfNeedToPutResToCache(res, blogId);
		}
		
		if(res == null) {
			if(cachedCommentsBlogId.contains(blogId)) {
				synchronized (updateCacheLock) {
					if(cachedCommentsBlogId.contains(blogId)) {
						Iterator<CommentEntry> it = cachedComments.iterator();
						while(it.hasNext()) {
							CommentEntry blogComments = it.next();
							Integer curBlogId = getBlogIdByCommentsEntry(blogComments);
							if(curBlogId.equals(blogId) ) {
								res = blogComments.getBlogComments();
								break ;
							}
						}
					}
				}
			}
		}
		
		return res;
	}
	
	// 从数据库中获取对应播客的的评论
	// 获取到所有的评论
	// 然后在更新各个楼层进行封装到各个楼层的集合中 [如果当前blogId可以装入缓存中, 则将其装入缓存]
	// 返回结果
	private static List<List<Comment>> getBlogCommentsFromDB(Integer blogId) throws Exception {
		Connection con = null;
		ArrayList<List<Comment>> res = null;
		List<Comment> allComment = null;
		int maxFloor = -1;
		try {
			synchronized (dbLock) {
				con = Tools.getConnection(Tools.getProjectPath());
				PreparedStatement ps = con.prepareStatement(String.format(Constants.getBlogCommentByBlogIdSql, blogId) );
				ResultSet rs = ps.executeQuery();
				
//				allComment = new ArrayList<>(Tools.getRows(rs) );
				allComment = new ArrayList<>( );
				maxFloor = -1;
				while(rs.next() ) {
					Comment comment = new Comment();
					comment.init(rs);
					maxFloor = Math.max(maxFloor, comment.getFloorIdx() );
					allComment.add(comment);
				}
			}
		} finally {
			if(con != null) {
				con.close();
			}
		}
		
		if(maxFloor > 0) {
			res = new ArrayList<>(maxFloor);
			Tools.init(res, maxFloor, null);
			for(Comment comment : allComment) {
				List<Comment> curFloors = res.get(comment.getFloorIdx()-1);
				if(curFloors == null) {
					curFloors = new ArrayList<>(Constants.defaultMaxCommentIdx);
					res.set(comment.getFloorIdx()-1, curFloors);
				}
				curFloors.add(comment);
			}
			
			// 如果可以的话, 更新缓存
			checkIfNeedToPutResToCache(res, blogId);
		}
		
		return res;
	}
	
	// 检查当前结果是否可以添加到缓存中
	// 如果缓存的集合不满, 则直接将记录存放进去
		// 否则  判断当前blog的获取频率, 是否比cachedComments重最小的频率达到一定程度 
			// 如果是, 将使用频率最小的blog挤出缓存, 将当前blog添加到缓存
	private static void checkIfNeedToPutResToCache(List<List<Comment>> res, Integer blogId) {
		if(res != null) {
			int frequency = blogGetFrequency.get(blogId);
			int minFre = 0;
			if(cachedComments.size() > 0) {
				minFre = blogGetFrequency.get(getBlogIdByCommentsEntry(cachedComments.peek()) );
			}
			if(cachedComments.size() < Constants.cachedComments) {
				if(! cachedCommentsBlogId.contains(blogId)) {
					synchronized (updateCacheLock) {
						if(! cachedCommentsBlogId.contains(blogId)) {
							cachedComments.add(new CommentEntry(blogId, res) );
							cachedCommentsBlogId.add(blogId );
						}
					}
				}
			}			
			if(frequency > (minFre + Constants.updateCachedCommentsOff) && (cachedComments.size() >= Constants.cachedComments) ) {
				if(! cachedCommentsBlogId.contains(blogId)) {
					synchronized (updateCacheLock) {
						if(! cachedCommentsBlogId.contains(blogId)) {
							Integer removedBlogId = getBlogIdByCommentsEntry(cachedComments.poll() );
							cachedCommentsBlogId.remove(removedBlogId);
							cachedComments.add(new CommentEntry(blogId, res) );
							cachedCommentsBlogId.add(blogId );
						}
					}
				}
			}
		}
	}	
	
	// 刷新更新的评论到数据库
	private static void flushAddedRecords(Connection con, List<Comment> addComments) throws Exception {
		updated[Constants.addCommentCnt] = 0;
		if((addComments != null) && (addComments.size() > 0) ) {		
			String addSelectedSql = Tools.getAddSelectedCommentsSql(addComments);
			Tools.log(BlogManager.class, addSelectedSql);
			PreparedStatement delBlogPs = con.prepareStatement(addSelectedSql);
			updated[Constants.addCommentCnt] += delBlogPs.executeUpdate();
		}
		
		Tools.log(BlogListAction.class, "added " + updated[Constants.addCommentCnt] + " commentRecords to db !");
	}
	
	// 根据对应的blogIdx, 或者floorIdx, commentIdx 删除对应的comment
	private static void flushDeletedRecords(Connection con, List<Integer> deleteCommentsByBlogIdxTmp, Map<Integer, List<Integer>> deleteCommentsByFloorIdxTmp, Map<Integer, List<Integer>> deleteCommentsByCommentIdxTmp) throws Exception {
		updated[Constants.deleteCommentByBlogIdxCnt] = 0;
		updated[Constants.deleteCommentByFloorIdxCnt] = 0;
		updated[Constants.deleteCommentByCommentIdxCnt] = 0;
		if((deleteCommentsByBlogIdxTmp != null) && (deleteCommentsByBlogIdxTmp.size() > 0) ) {
			for(Integer blogIdx : deleteCommentsByBlogIdxTmp) {
				String deleteSelectedSql = Tools.getDeleteCommentByBlogIdxSql(blogIdx);
				Tools.log(CommentManager.class, deleteSelectedSql);
				PreparedStatement delCommentPs = con.prepareStatement(deleteSelectedSql);
				updated[Constants.deleteCommentByBlogIdxCnt] += delCommentPs.executeUpdate();
			}
		}
		
		if((deleteCommentsByFloorIdxTmp != null) && (deleteCommentsByFloorIdxTmp.size() > 0) ) {
			String deleteSelectedSql = null;
			PreparedStatement delTagsPs = null;
			for(Entry<Integer, List<Integer>> entry : deleteCommentsByFloorIdxTmp.entrySet()) {
				if(entry.getValue().size() > 0) {
					deleteSelectedSql = Tools.getDeleteCommentByFloorIdxSql(entry.getKey(), entry.getValue());
					Tools.log(BlogManager.class, deleteSelectedSql);
					delTagsPs = con.prepareStatement(deleteSelectedSql);
					updated[Constants.deleteCommentByFloorIdxCnt] += delTagsPs.executeUpdate();
				}
			}
		}

		if((deleteCommentsByCommentIdxTmp != null) && (deleteCommentsByCommentIdxTmp.size() > 0) ) {
			String deleteSelectedSql = null;
			PreparedStatement delTagsPs = null;
			for(Entry<Integer, List<Integer>> entry : deleteCommentsByCommentIdxTmp.entrySet()) {
				if(entry.getValue().size() > 0) {
					deleteSelectedSql = Tools.getDeleteCommentByFloorIdxSql(entry.getKey(), entry.getValue());
					Tools.log(BlogManager.class, deleteSelectedSql);
					delTagsPs = con.prepareStatement(deleteSelectedSql);
					updated[Constants.deleteCommentByCommentIdxCnt] += delTagsPs.executeUpdate();
				}
			}
		}
		
		Tools.log(BlogListAction.class, "delete byBlogIdx " + updated[Constants.deleteCommentByBlogIdxCnt] + " commentRecoreds , delete byFloorIdx " + updated[Constants.deleteCommentByFloorIdxCnt] + " commentRecoreds, delete byCommentIdx " + updated[Constants.deleteCommentByCommentIdxCnt] + " commentRecoreds" );
	}
	
	// 获取更新的记录信息 [日志]
	private static String getFlushInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("addComment : ");	sb.append(updated[Constants.addCommentCnt] );
		sb.append(", deleteCommentByBlogIdx : ");	sb.append(updated[Constants.deleteCommentByBlogIdxCnt] );
		sb.append(", deleteCommentByFloorIdx : ");	sb.append(updated[Constants.deleteCommentByFloorIdxCnt] );
		sb.append(", deleteCommentByCommentIdx : ");	sb.append(updated[Constants.deleteCommentByCommentIdxCnt] );
		
		return sb.toString();
	}
	
	// 根据已有的播客评论列表, 更新当前comment的commentIdx
	// 如果当前播客的评论缓存中在cachedComments中
		// 则直接将comment添加到对应的blogComments中, 并更新commentIdx
	// 否则  如果当前blog还没有评论, 则新建List<List<Comment>>
		// 则直接将comment添加到对应的blogComments中, 并更新commentIdx
		// 如果addBlogsComments中没有对应的blog的集合, 则将其加入addBlogsComments
	private static void updateCommentIdx0(List<List<Comment>> blogComments, Comment comment, boolean isInCachedComment) {
		if(isInCachedComment) {
			synchronized (updateCacheLock) {
				blogComments.get(comment.getFloorIdx()-1).add(comment);
				comment.setCommentIdx(blogComments.get(comment.getFloorIdx()-1).size() );
			}
		} else {
			synchronized (updateAddBlogsCommentLock) {
				if(blogComments == null) {
					blogComments = new ArrayList<>();
					List<Comment> curFloor = new ArrayList<>();
					curFloor.add(comment);
					blogComments.add(curFloor);
					comment.setCommentIdx(curFloor.size() );
				} else {
					blogComments.get(comment.getFloorIdx()-1).add(comment);
					comment.setCommentIdx(blogComments.get(comment.getFloorIdx()-1).size() );
				}
				if(! addBlogsId.containsKey(comment.getBlogIdx()) ) {
					addBlogsId.put(comment.getBlogIdx(), addBlogsComments.size());
					addBlogsComments.add(new CommentEntry(comment.getBlogIdx(), blogComments) );
				}				
			}
		}
	}
	
	// 根据已有的播客评论列表, 更新当前comment的commentIdx
	// 如果当前播客的评论缓存中在cachedComments中
		// 则直接将comment添加到对应的blogComments中, 并更新commentIdx
	// 否则  如果当前blog还没有评论, 则新建List<List<Comment>>
		// 则直接将comment添加到对应的blogComments中, 并更新commentIdx
		// 如果addBlogsComments中没有对应的blog的集合, 则将其加入addBlogsComments	
	private static void updateFloorIdx0(List<List<Comment>> blogComments, Comment comment, boolean isInCachedComment) {
		if(isInCachedComment) {
			synchronized (updateCacheLock) {
				List<Comment> curFloor = new ArrayList<>();
				curFloor.add(comment);
				blogComments.add(curFloor);
				comment.setFloorId(blogComments.size() );
			}
		} else {
			synchronized (updateAddBlogsCommentLock) {
				if(blogComments == null) {
					blogComments = new ArrayList<>();
				}
				List<Comment> curFloor = new ArrayList<>();
				curFloor.add(comment);
				blogComments.add(curFloor);
				comment.setFloorId(blogComments.size() );
				if(! addBlogsId.containsKey(comment.getBlogIdx()) ) {
					addBlogsId.put(comment.getBlogIdx(), addBlogsComments.size());
					addBlogsComments.add(new CommentEntry(comment.getBlogIdx(), blogComments) );
				}
			}
		}
	}	
	
}
