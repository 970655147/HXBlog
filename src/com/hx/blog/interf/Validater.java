/**
 * file name : Validter.java
 * created at : 10:20:14 AM Mar 20, 2016
 * created by 970655147
 */

package com.hx.blog.interf;

import javax.servlet.http.HttpServletRequest;

import com.hx.blog.bean.ResponseMsg;
import com.hx.blog.bean.ValidateResult;

// У���û�������
public interface Validater {

	// У������Ŀͻ��˵�����
	public ValidateResult validate(HttpServletRequest req, ResponseMsg respMsg, Object... others);
	
}
