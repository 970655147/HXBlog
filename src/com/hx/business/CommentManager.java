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
import java.util.concurrent.ConcurrentHashMap;

import com.hx.action.BlogListAction;
import com.hx.bean.Comment;
import com.hx.util.Constants;
import com.hx.util.Tools;

// 管理评论
public class CommentManager {

	// 缓存的评论 [一定的个数]
	private static Queue<List<List<Comment>>> cachedComments = new PriorityQueue<>(Constants.cachedComments);
	private static Set<Integer> cachedCommentsBlogId = new HashSet<>();
	private static Map<Integer, Integer> blogGetFrequency = new ConcurrentHashMap<>();
	private static Object updateCacheLock = new Object();
	private static List<Comment> addComments = new ArrayList<>(Constants.addedCommentsListSize);
	private static Object updateAddCommentsLock = new Object();
	private static List<List<List<Comment>>> addBlogsComments = new ArrayList<>(Constants.addedCommentsListSize);
	private static Map<Integer, Integer> addBlogsId = new HashMap<>();
	private static Object updateAddBlogsCommentLock = new Object();
	private static Object dbLock = new Object();
	
	// 增加给定的播客的访问频率
	public static void incBlogGetFrequency(Integer blogId) {
		Integer frequncy = blogGetFrequency.get(blogId);
		if(frequncy == null) {
			blogGetFrequency.put(blogId, Constants.INTE_ZERO);
		} else {
			blogGetFrequency.put(blogId, frequncy + 1);
		}
	}
	
	// 获取给定的blogId对应的所有评论
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

	// 从数据库中获取对应播客的的评论
	private static List<List<Comment>> getBlogCommentsFromCache(Integer blogId) {
		List<List<Comment>> res = null;
		if(addBlogsId.containsKey(blogId)) {
			synchronized (updateAddBlogsCommentLock) {
				res = addBlogsComments.get(addBlogsId.get(blogId) );
			}
			
			checkIfNeedToPutResToCache(res, blogId);
		}
		
		if(res == null) {
			if(cachedCommentsBlogId.contains(blogId)) {
				synchronized (updateCacheLock) {
					Iterator<List<List<Comment>>> it = cachedComments.iterator();
					while(it.hasNext()) {
						List<List<Comment>> blogComments = it.next();
						Integer curBlogId = getBlogIdByBlogComements(blogComments);
						if(curBlogId.equals(blogId) ) {
							res = blogComments;
							break ;
						}
					}
				}
			}
		}
		
		return res;
	}
	
	// 从数据库中获取对应播客的的评论
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
		
		if(maxFloor != -1) {
			res = new ArrayList<>(maxFloor + 1);
			Tools.init(res, maxFloor+1, null);
			for(Comment comment : allComment) {
				List<Comment> curFloors = res.get(comment.getFloorIdx());
				if(curFloors == null) {
					curFloors = new ArrayList<>(Constants.defaultMaxCommentIdx);
					res.set(comment.getFloorIdx(), curFloors);
				}
				curFloors.add(comment);
			}
			
			// 如果可以的话, 更新缓存
			checkIfNeedToPutResToCache(res, blogId);
		}
		
		return res;
	}
	
	// 检查当前结果是否可以添加到缓存中
	private static void checkIfNeedToPutResToCache(List<List<Comment>> res, Integer blogId) {
		if(res != null) {
			int frequency = blogGetFrequency.get(blogId);
			int minFre = 0;
			if(cachedComments.size() > 0) {
				minFre = blogGetFrequency.get(getBlogIdByBlogComements(cachedComments.peek()) );
			}
			if(cachedComments.size() < Constants.cachedComments) {
				if(! cachedCommentsBlogId.contains(blogId)) {
					synchronized (updateCacheLock) {
						if(! cachedCommentsBlogId.contains(blogId)) {
							cachedComments.add(res);
							cachedCommentsBlogId.add(blogId );
						}
					}
				}
			}			
			if(frequency > (minFre + Constants.updateCachedCommentsOff) && (cachedComments.size() >= Constants.cachedComments) ) {
				if(! cachedCommentsBlogId.contains(blogId)) {
					synchronized (updateCacheLock) {
						if(! cachedCommentsBlogId.contains(blogId)) {
							Integer removedBlogId = getBlogIdByBlogComements(cachedComments.poll() );
							cachedCommentsBlogId.remove(removedBlogId);
							cachedComments.add(res);
							cachedCommentsBlogId.add(blogId );
						}
					}
				}
			}
		}
	}
	
	// 为blogId对应的播客, 添加一条评论
	public static void addComment(Integer blogId, Comment comment) {
		synchronized (updateAddCommentsLock) {
			addComments.add(comment);
		}
		if(getUpdated() > Constants.updateCommentsThreshold) {
			flushToDB();
		}
	}

	// 获取给定过的播客列表的播客id
	public static Integer getBlogIdByBlogComements(List<List<Comment>> comments) {
		if(comments != null) {
			return comments.get(0).get(0).getBlogIdx();
		}
		
		return null;
	}
	
	// 更新当前评论内容的commentIdx
	public static void updateCommentIdx(Comment comment) throws Exception {
		if(addBlogsId.containsKey(comment.getBlogIdx()) ) {
			List<List<Comment>> blogComments = addBlogsComments.get(addBlogsId.get(comment.getBlogIdx()) );
			updateCommentIdx0(blogComments, comment, false);
			return ;
		}
		if(cachedCommentsBlogId.contains(comment.getBlogIdx()) ) {
			updateCommentIdx0(getBlogCommentsFromCache(comment.getBlogIdx()), comment, true);
			return ;
		}
		
		List<List<Comment>> blogComments = getBlogCommentsFromDB(comment.getBlogIdx() );
		updateCommentIdx0(blogComments, comment, false);
	}
	
	// 根据已有的播客评论列表, 更新当前comment的commentIdx
	private static void updateCommentIdx0(List<List<Comment>> blogComments, Comment comment, boolean isInCachedComment) {
		if(isInCachedComment) {
			synchronized (updateCacheLock) {
				blogComments.get(comment.getFloorIdx()).add(comment);
				comment.setCommentIdx(blogComments.get(comment.getFloorIdx()).size() );
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
					blogComments.get(comment.getFloorIdx()).add(comment);
					comment.setCommentIdx(blogComments.get(comment.getFloorIdx()).size() );
				}
				if(! addBlogsId.containsKey(comment.getBlogIdx()) ) {
					addBlogsId.put(comment.getBlogIdx(), addBlogsComments.size());
					addBlogsComments.add(blogComments);
				}				
			}
		}
	}
	
	// 更新当前评论内容的floorIdx
	public static void updateFloorIdx(Comment comment) throws Exception {
		if(addBlogsId.containsKey(comment.getBlogIdx()) ) {
			List<List<Comment>> blogComments = addBlogsComments.get(addBlogsId.get(comment.getBlogIdx()) );
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
	
	// 根据已有的播客评论列表, 更新当前comment的commentIdx
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
					addBlogsComments.add(blogComments);
				}
			}
		}
	}
	
	// 获取更新的comment的个数
	public static int getUpdated() {
		return addComments.size();
	}
	
	// 刷新更新的数据到数据库
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
			
			synchronized (dbLock) {
				Connection con = null;
				try {
					con = Tools.getConnection(Tools.getProjectPath());			
					flushAddedRecords(con, addedCommentTmp);
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
	
	// 刷新更新的评论到数据库
	private static void flushAddedRecords(Connection con, List<Comment> addComments) throws Exception {
		int updatedRows = 0;
		if((addComments != null) && (addComments.size() > 0) ) {		
			String addSelectedSql = Tools.getAddSelectedCommentsSql(addComments);
			Tools.log(BlogManager.class, addSelectedSql);
			PreparedStatement delBlogPs = con.prepareStatement(addSelectedSql);
			updatedRows = delBlogPs.executeUpdate();
		}
		
		Tools.log(BlogListAction.class, "added " + updatedRows + " commentRecords to db !");
	}
	
	// 获取刷新数据到数据库的相关信息
	private static String getFlushInfo() {
		return "flush comments success !";
	}
	
	// 清理(blogId -> visitFrequency), 由ContextListener定期清理
	public static void clearFrequencyMap() {
		blogGetFrequency.clear();
	}
	
}
