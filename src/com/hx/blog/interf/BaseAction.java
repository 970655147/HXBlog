/**
 * file name : BaseAction.java
 * created at : 10:28:21 AM Mar 20, 2016
 * created by 970655147
 */

package com.hx.blog.interf;

import javax.servlet.http.HttpServlet;

// ���е�action�Ļ���
public abstract class BaseAction extends HttpServlet {

	// У���û�����Ķ���
	protected Validater validater = null;

}
