package com.hx.util;

import net.sf.json.JSONArray;

public class Test01JSONArrayToString {
	
	// ����jsonArray��toString
	public static void main(String []args) {
		
//		JSONArray arr = new JSONArray().element("df").element("DFfd");
		JSONArray arr = new JSONArray().element(1).element(2);
		String str = arr.toString();
		Log.log(str);
		String str02 = str.substring(1, str.length()-1 );
		Log.log(str02);
		
		StringBuilder sb = new StringBuilder();
		// �����ַ�����
		sb.append("df��");
		Log.log(sb.length());
		
		Log.log(System.getProperty("user.dir") );
		
	}

}
