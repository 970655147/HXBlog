/**
 * file name : ValidateResult.java
 * created at : 10:27:13 AM Mar 20, 2016
 * created by 970655147
 */

package com.hx.bean;

// 校验的结果
public class ValidateResult {
	// 是否成功的标志位, 以及需要返回的对象
	public boolean isSucc;
	public Object[] attachments;
	
	// 初始化
	public ValidateResult(boolean isSucc, Object[] attachments) {
		super();
		this.isSucc = isSucc;
		this.attachments = attachments;
	}
	
}
