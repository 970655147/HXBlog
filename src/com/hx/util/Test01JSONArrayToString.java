package com.hx.util;

import net.sf.json.JSONArray;

public class Test01JSONArrayToString {
	
	// ����jsonArray��toString
	public static void main(String []args) {
		
		JSONArray arr = new JSONArray().element("df").element("DFfd");
		Log.log(arr.toString() );
		
	}

}
