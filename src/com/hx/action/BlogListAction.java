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

	// blogList
	private Map<Integer, Blog> blogList = new HashMap<>();
	private Map<String, List<Integer>> tagList = new HashMap<>();
	private Set<TagToBlogCnt> tagToBlogCnt = new TreeSet<>();
	private boolean needInit = true;
	
	// 获取config.conf中的配置
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if(needInit) {
			try {
				initLists();
				needInit = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		resp.setHeader("Content-Type","text/html;charset=" + Tools.DEFAULT_CHARSET);
		PrintWriter out = resp.getWriter();
		
		JSONArray tagList = new JSONArray();
		Iterator<TagToBlogCnt> tagToBlogIt = this.tagToBlogCnt.iterator();
		while(tagToBlogIt.hasNext() ) {
			JSONObject obj = new JSONObject();
			tagToBlogIt.next().encapJSON(obj);
			tagList.add(obj);
		}
		
		JSONArray blogList = new JSONArray();
		Iterator<Entry<Integer, Blog>> it = this.blogList.entrySet().iterator();
		while(it.hasNext() ) {
			JSONObject obj = new JSONObject();
			it.next().getValue().encapJSON(obj);
			blogList.add(obj);
		}
		
		JSONObject res = new JSONObject();
		res.element("tagList", tagList.toString() );
		res.element("blogList", blogList.toString() );
		out.write(res.toString() );
		Log.log(res.toString() );
		
		out.close();
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
		ResultSet blogRs = blogPS.executeQuery();
		while(blogRs.next() ) {
			Blog blog = new Blog();
			blog.init(blogRs);
			blogList.put(blog.getId(), blog);
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
		
		int size = 0;
		for(Entry<String, List<Integer>> entry : tagList.entrySet()) {
			size += entry.getValue().size();
			tagToBlogCnt.add(new TagToBlogCnt(entry.getKey(), entry.getValue().size()) );
		}
		tagToBlogCnt.add(new TagToBlogCnt("All", size) );
	}
	
}
