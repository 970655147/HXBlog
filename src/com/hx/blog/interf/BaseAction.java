/**
 * file name : BaseAction.java
 * created at : 10:28:21 AM Mar 20, 2016
 * created by 970655147
 */

package com.hx.blog.interf;

import javax.servlet.http.HttpServlet;

// 所有的action的基类
public abstract class BaseAction extends HttpServlet {

	// 校验用户输入的对象
	protected Validater validater = null;

}
