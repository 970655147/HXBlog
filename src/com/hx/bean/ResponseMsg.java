package com.hx.bean;

import com.hx.util.Tools;

import net.sf.json.JSONObject;

// 响应消息
public class ResponseMsg {

	// 是否成功, 响应码, 响应消息
	private boolean isSuccess;
	private int respCode;
	private String msg;
	private String ip;
	private String others;
	
	// 初始化
	public ResponseMsg() {
		super();
	}
	public ResponseMsg(boolean isSuccess, int respCode, String msg, String ip) {
		super();
		set(isSuccess, respCode, msg, ip);
	}
	
	// setter & getter
	public boolean isSuccess() {
		return isSuccess;
	}
	public int getRespCode() {
		return respCode;
	}
	public String getMsg() {
		return msg;
	}
	public String getIp() {
		return ip;
	}
	public String getOthers() {
		return others;
	}
	public void setOthers(String others) {
		this.others = others;
	}
	public void set(boolean isSuccess, int respCode, String msg, String ip) {
		this.isSuccess = isSuccess;
		this.respCode = respCode;
		this.msg = msg;
		this.ip = ip;
	}
	
	// for debug & response
	public String toString() {
		JSONObject res = new JSONObject();
		res.element("isSuccess", isSuccess);
		res.element("respCode", respCode);
		Tools.addIfNotEmpty(res, "msg", msg);
		Tools.addIfNotEmpty(res, "ip", ip);
		Tools.addIfNotEmpty(res, "others", others);
		
		return res.toString();
	}
	
}
