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

// 获取blog配置的action
public class BlogListAction extends HttpServlet {

	// blogList, tagList, tag到tag对应的博客数的集合
	// 是否需要初始化, 同步用的对象
	private Map<Integer, Blog> blogList = new HashMap<>();
	private Map<String, List<Integer>> tagList = new HashMap<>();
	private Set<TagToBlogCnt> tagToBlogCnt = new TreeSet<>();
	private boolean needInit = true;
	private String ALL = "all";
	private Object lock = new Object();
	
	// 获取config.conf中的配置
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		initIfNeeded();
		
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
	
	// 通过tag获取响应的数据列表
	private JSONObject getResByTag(String tag) {
		JSONArray tagList = new JSONArray();
		Iterator<TagToBlogCnt> tagToBlogIt = this.tagToBlogCnt.iterator();
		while(tagToBlogIt.hasNext() ) {
			JSONObject obj = new JSONObject();
			tagToBlogIt.next().encapJSON(obj);
			tagList.add(obj);
		}
		
		JSONArray blogList = new JSONArray();
		List<Integer> blogIds = this.tagList.get(tag);
		Iterator<Integer> it = blogIds.iterator();
		while(it.hasNext() ) {
			JSONObject obj = new JSONObject();
			this.blogList.get(it.next()).encapJSON(obj);
			blogList.add(obj);
		}
		
		JSONObject res = new JSONObject();
		res.element("tagList", tagList.toString() );
		res.element("blogList", blogList.toString() );
		
		return res;
	}

	// 如果有必要的话, 初始化blogList, tagList
	private void initIfNeeded() {
		if(needInit) {
			synchronized (lock) {
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
	private void initLists() throws Exception {
		Connection con = Tools.getConnection(Tools.getProjectPath(this));
		blogList.clear();
		tagList.clear();
		tagToBlogCnt.clear();
		PreparedStatement blogPS = con.prepareStatement(Constants.blogListSql);
		PreparedStatement tagListPS = con.prepareStatement(Constants.tagListSql);
		
		// 初始化blogList, tagList, tagToBlogCnt
		List<Integer> allBlogIds = new ArrayList<>();
		ResultSet blogRs = blogPS.executeQuery();
		while(blogRs.next() ) {
			Blog blog = new Blog();
			blog.init(blogRs);
			blogList.put(blog.getId(), blog);
			allBlogIds.add(blog.getId() );
		}
		
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
		
		int size = 0;
		for(Entry<String, List<Integer>> entry : tagList.entrySet()) {
			size += entry.getValue().size();
			tagToBlogCnt.add(new TagToBlogCnt(entry.getKey(), entry.getValue().size()) );
		}
	}
	
}
