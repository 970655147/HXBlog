package com.hx.bean;

import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.json.JSONObject;

import com.hx.business.BlogManager;
import com.hx.util.Tools;

// 对应于数据库中的blogList的每一条记录
public class Blog {
	
	// id, title, path, tags, createTime, 顶踩值, 访问数, 当前播客的内容, 评论[未用]
	private Integer id;
	private String title;
	private String path;
	private List<String> tags;
	private String createTime;
	private AtomicInteger good;
	private AtomicInteger notGood;
	private AtomicInteger visited;
	private AtomicInteger commentsNum;
	private String content;
	private JSONObject comments;
	
	// 初始化
	public Blog() {
		
	}
	public Blog(Integer id, String title, String path, String tags, String createTime, AtomicInteger good, AtomicInteger notGood, AtomicInteger visited, AtomicInteger commentsNum) {
		set(id, title, path, tags, createTime, good, notGood, visited, commentsNum);
		this.content = null;
	}
	public Blog(Blog blog) {
		this(blog.id, blog.title, blog.path, null, blog.createTime, blog.good, blog.notGood, blog.visited, blog.commentsNum);
		this.tags = blog.tags;
	}
	
	// 利用给定的resultSet 初始化当前blog对象
	public void init(ResultSet rs) throws Exception {
		String path = rs.getString("path");
		set(rs.getInt("id"), null, path, rs.getString("tag"), rs.getString("createTime"), new AtomicInteger(rs.getInt("good")), new AtomicInteger(rs.getInt("notGood") ), new AtomicInteger(rs.getInt("visited")), new AtomicInteger(rs.getInt("commentsNum")) );
		this.title = Tools.getTitleFromBlogFileName(path);
	}
	
	// 封装当前对象中的数据到obj中
	public void encapJSON(JSONObject obj) {
		obj.element("id", id);
		obj.element("title", title);
//		Tools.addIfNotEmpty(obj, "path", path);
		obj.element("tags", Tools.tagsToString(tags));
		Tools.addIfNotEmpty(obj, "date", createTime);
		Tools.addIfNotEmpty(obj, "content", content);
		obj.element("good", good);
		obj.element("notGood", notGood);
		obj.element("visited", visited);
		obj.element("commentsNum", commentsNum);
		Tools.addIfNotEmpty(obj, "comments", comments);
	}
	
	// setter & getter
	public Integer getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public String getPath() {
		return path;
	}
	public List<String> getTags() {
		return tags;
	}
	public String getCreateTime() {
		return createTime;
	}
	public int getGood() {
		return good.intValue();
	}
	public int getNotGood() {
		return notGood.intValue();
	}
	public Integer getVisited() {
		return visited.intValue();
	}
	public Integer getCommentsNum() {
		return commentsNum.intValue();
	}
	public void incGood() {
		good.incrementAndGet();
	}
	public void decGood() {
		good.decrementAndGet();
	}
	public void incNotGood() {
		notGood.incrementAndGet();
	}
	public void decNotGood() {
		notGood.decrementAndGet();
	}
	public void incVisited() {
		visited.incrementAndGet();
	}
	public void incCommentsNum() {
		commentsNum.incrementAndGet();
	}
	public void set(Integer id, String title, String path, String tags, String createTime) {
		this.id = id;
		this.title = title;
		this.path = path;
		this.tags = Tools.getTagListFromString(tags);
		if(! this.tags.contains(BlogManager.ALL)) {
			this.tags.add(BlogManager.ALL);
		}
		if(! Tools.isEmpty(createTime) ) {
			this.createTime = createTime;
		}
	}
	public void set(Integer id, String title, String path, String tags, String createTime, AtomicInteger good, AtomicInteger notGood, AtomicInteger visited, AtomicInteger commentsNum) {
		set(id, title, path, tags, createTime);
		this.good = good;
		this.notGood = notGood;
		this.visited = visited;
		this.commentsNum = commentsNum;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public JSONObject getComments() {
		return comments;
	}
	public void setComments(JSONObject comments) {
		this.comments = comments;
	}
	
	// for debug & response
	public String toString() {
		JSONObject res = new JSONObject();
		encapJSON(res);
		
		return res.toString();
	}
}
