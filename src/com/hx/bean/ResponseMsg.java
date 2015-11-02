package com.hx.bean;

import com.hx.util.Tools;

import net.sf.json.JSONObject;

// ��Ӧ��Ϣ
public class ResponseMsg {

	// �Ƿ�ɹ�, ��Ӧ��, ��Ӧ��Ϣ
	private boolean isSuccess;
	private int respCode;
	private String msg;
	
	// ��ʼ��
	public ResponseMsg() {
		super();
	}
	public ResponseMsg(boolean isSuccess, int respCode, String msg) {
		super();
		this.isSuccess = isSuccess;
		this.respCode = respCode;
		this.msg = msg;
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
	
	// for debug & response
	public String toString() {
		JSONObject res = new JSONObject();
		res.element("isSuccess", isSuccess);
		res.element("respCode", respCode);
		if(! Tools.isEmpty(msg)) {
			res.element("msg", msg);
		}
		
		return res.toString();
	}
	
}
