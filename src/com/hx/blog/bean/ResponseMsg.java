package com.hx.blog.bean;

import com.hx.blog.util.EncapJSON;
import com.hx.blog.util.Tools;

import net.sf.json.JSONObject;

// ��Ӧ��Ϣ
public class ResponseMsg implements EncapJSON {

	// �Ƿ�ɹ�, ��Ӧ��, ��Ӧ��Ϣ
	private boolean isSuccess;
	private int respCode;
	private String msg;
	private String ip;
	private String others;
	
	// ��ʼ��
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
	
	// ��װ��ǰ�����е����ݵ�obj��	
	@Override
	public void encapJSON(JSONObject res) {
		res.element("isSuccess", isSuccess);
		res.element("respCode", respCode);
		Tools.addIfNotEmpty(res, "msg", msg);
		Tools.addIfNotEmpty(res, "ip", ip);
		Tools.addIfNotEmpty(res, "others", others);
	}
	
	// for debug & response
	public String toString() {
		JSONObject res = new JSONObject();
		encapJSON(res);
		
		return res.toString();
	}
	
}
