/**
 * file name : ValidateResult.java
 * created at : 10:27:13 AM Mar 20, 2016
 * created by 970655147
 */

package com.hx.bean;

// У��Ľ��
public class ValidateResult {
	// �Ƿ�ɹ��ı�־λ, �Լ���Ҫ���صĶ���
	public boolean isSucc;
	public Object[] attachments;
	
	// ��ʼ��
	public ValidateResult(boolean isSucc, Object[] attachments) {
		super();
		this.isSucc = isSucc;
		this.attachments = attachments;
	}
	
}
