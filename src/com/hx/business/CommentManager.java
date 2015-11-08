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
import com.hx.bean.CommentEntry;
import com.hx.util.Constants;
import com.hx.util.Tools;

// ��������
public class CommentManager {

	// ��������� [һ���ĸ���], ����Ĳ������۵�id�Ľ��
	// ���͵�id�����ʴ�����ӳ��
	// ������������ӵ�����, ������������۵Ĳ��͵����۵ļ���, �������۵ĸ�������id�ļ��� 
	// ͬ���õĶ���
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
	
	// ��ʼ�� [��ʼ��CommentEntry.blogGetFrequency]
	static {
		CommentEntry.setBlogGetFrequency(blogGetFrequency);
	}
	
	// ���Ӹ����Ĳ��͵ķ���Ƶ�� [BlogManager.getBlog �е���]
	public static void incBlogGetFrequency(Integer blogId) {
		Integer frequncy = blogGetFrequency.get(blogId);
		if(frequncy == null) {
			blogGetFrequency.put(blogId, Constants.INTE_ZERO);
		} else {
			blogGetFrequency.put(blogId, frequncy + 1);
		}
	}
	
	// ��ȡ������blogId��Ӧ����������
	// �ȴӻ����л�ȡ [cachedComments, addBlogsComments]
	// �ڴ����ݿ��л�ȡ
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
	
	// ΪblogId��Ӧ�Ĳ���, ���һ������
	// ������µ����۸������ӵ�һ������ֵ, ����ˢ�µ����ݿ�
	public static void addComment(Integer blogId, Comment comment) {
		synchronized (updateAddCommentsLock) {
			addComments.add(comment);
		}
		if(getUpdated() > Constants.updateCommentsThreshold) {
			flushToDB();
		}
	}

	// ��ȡ�������Ĳ����б�Ĳ���id
	public static Integer getBlogIdByCommentsEntry(CommentEntry entry) {
		List<List<Comment>> comments = entry.getBlogComments();
		if(comments != null) {
			return comments.get(0).get(0).getBlogIdx();
		}
		
		return null;
	}
	
	// ���µ�ǰ�������ݵ�commentIdx
	// ��Ϊ��addBlogsComments, cachedComments, ���ݿ��л�ȡ
	// Ȼ�� ���µ�ǰcomment��commentIdx
		// �����ǰblog�����۲���addBlogsComments �������addBlogsComments
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
	
	// ���µ�ǰ�������ݵ�floorIdx
	// ��Ϊ��addBlogsComments, cachedComments, ���ݿ��л�ȡ
	// Ȼ�� ���µ�ǰcomment��floorIdx
		// �����ǰblog�����۲���addBlogsComments �������addBlogsComments	
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
	
	// ��ȡ���µ�comment�ĸ���
	public static int getUpdated() {
		return addComments.size();
	}
	
	// ˢ�¸��µ����ݵ����ݿ�
	// ��addComments�е����� ���浽��ʱ�ռ���, ������addComments, addBlogsComments, addBlogsId
	// Ȼ�󽫸��µ�commentˢ�µ����ݿ���
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

	
	// ����(blogId -> visitFrequency), ��ContextListener��������
	public static void clearFrequencyMap() {
		blogGetFrequency.clear();
	}
	
	// context destory��ʱ������ռ�õ��ڴ�
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

	// �ӻ����л�ȡblogId��Ӧ������ [cachedComments, addBlogsComments]
	// �ȴ�addBlogsComments�л�ȡ [��һ�� map��ȡ����, ֱ�������ȡ] [�����ǰblogId����װ�뻺����, ����װ�뻺��]
	// Ȼ���ٴ� cachedComments�л�ȡ
	// ���ؽ��
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
	
	// �����ݿ��л�ȡ��Ӧ���͵ĵ�����
	// ��ȡ�����е�����
	// Ȼ���ڸ��¸���¥����з�װ������¥��ļ����� [�����ǰblogId����װ�뻺����, ����װ�뻺��]
	// ���ؽ��
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
			
			// ������ԵĻ�, ���»���
			checkIfNeedToPutResToCache(res, blogId);
		}
		
		return res;
	}
	
	// ��鵱ǰ����Ƿ������ӵ�������
	// �������ļ��ϲ���, ��ֱ�ӽ���¼��Ž�ȥ
		// ����  �жϵ�ǰblog�Ļ�ȡƵ��, �Ƿ��cachedComments����С��Ƶ�ʴﵽһ���̶� 
			// �����, ��ʹ��Ƶ����С��blog��������, ����ǰblog��ӵ�����
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
	
	// ˢ�¸��µ����۵����ݿ�
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
	
	// ��ȡˢ�����ݵ����ݿ�������Ϣ
	private static String getFlushInfo() {
		return "flush comments success !";
	}
	
	// �������еĲ��������б�, ���µ�ǰcomment��commentIdx
	// �����ǰ���͵����ۻ�������cachedComments��
		// ��ֱ�ӽ�comment��ӵ���Ӧ��blogComments��, ������commentIdx
	// ����  �����ǰblog��û������, ���½�List<List<Comment>>
		// ��ֱ�ӽ�comment��ӵ���Ӧ��blogComments��, ������commentIdx
		// ���addBlogsComments��û�ж�Ӧ��blog�ļ���, �������addBlogsComments
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
	
	// �������еĲ��������б�, ���µ�ǰcomment��commentIdx
	// �����ǰ���͵����ۻ�������cachedComments��
		// ��ֱ�ӽ�comment��ӵ���Ӧ��blogComments��, ������commentIdx
	// ����  �����ǰblog��û������, ���½�List<List<Comment>>
		// ��ֱ�ӽ�comment��ӵ���Ӧ��blogComments��, ������commentIdx
		// ���addBlogsComments��û�ж�Ӧ��blog�ļ���, �������addBlogsComments	
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
