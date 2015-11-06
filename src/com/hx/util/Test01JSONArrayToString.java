package com.hx.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Test01JSONArrayToString {
	
	// 测试jsonArray的toString
	public static void main(String []args) throws IOException {
		
//		JSONArray arr = new JSONArray().element("df").element("DFfd");
		JSONArray arr = new JSONArray().element(1).element(2);
		String str = arr.toString();
		Log.log(str);
		String str02 = str.substring(1, str.length()-1 );
		Log.log(str02);
		
		StringBuilder sb = new StringBuilder();
		// 三个字符长度
		sb.append("df中");
		Log.log(sb.length());
		
		Log.log(System.getProperty("user.dir") );
		
		Log.log(Tools.replaceMultiSpacesAsOne(Tools.getContent(Tools.getPackagePath(System.getProperty("user.dir") + Tools.SLASH + "WebRoot" + Tools.SLASH, Constants.configPath))) );
		Log.log(Tools.replaceMultiSpacesAsOne(Tools.getContent(Tools.getPackagePath(System.getProperty("user.dir") + Tools.SLASH + "WebRoot" + Tools.SLASH, Constants.resumePath))) );
		
		List<List<String>> list = new ArrayList<>();
		List<String> ls01 = new ArrayList<>();
		List<String> ls02 = new ArrayList<>();
		list.add(ls01);
		list.add(ls02);
		ls01.add("sdf");
		ls01.add("dd");
		ls02.add("fff");
		ls02.add("ff");
		Log.log(list.toString() );
		
		List<JSONArray> jList = new ArrayList<>();
		JSONArray jls01 = new JSONArray();
		JSONArray jls02 = new JSONArray();
		jList.add(jls01);
		jList.add(jls02);
		jls01.add("sdf");
		jls01.add("dd");
		jls02.add("fff");
		jls02.add("ff");
		Log.log(jList.toString() );
		
		JSONArray jjList = new JSONArray();
		JSONArray jjls01 = new JSONArray();
		JSONArray jjls02 = new JSONArray();
		jjList.add(jjls01);
		jjList.add(jjls02);
		jjls01.add("sdf");
		jjls01.add("dd");
		jjls02.add("fff");
		jjls02.add("ff");
		Log.log(jjList.toString() );
		
		JSONObject obj = new JSONObject();
		
	}

}
