package com.hx.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hx.action.BlogListAction;

// 初始化, 并启动定时检查更新的ContextListener
public class InitAndCheckUpdateListener implements ServletContextListener {

	// 控制定时任务的Timer
	Timer updateCheckTimer = new Timer();
	
	// context初始化的时候调用
	// 定时检查的任务
		// 定时检查BlogListAction中是否存在更新的数据, 如果有 则刷新到数据库
	public void contextInitialized(final ServletContextEvent scv) {
		Tools.setProjectPath(scv.getServletContext() );
		BlogListAction.initIfNeeded();
		updateCheckTimer.schedule(new TimerTask() {
			public void run() {
				int updated = BlogListAction.getUpdated();
				if(updated > 0) {
					BlogListAction.flushToDB(scv.getServletContext() );
					Tools.log(this, "checkUpdate, updated : " + updated);
				}
				
				if(Tools.getLogBufferSize() > 0) {
					Tools.flushLog(Constants.dateFormat.format(new Date()) );
				}
			}
		}, Constants.checkUpdateInterval, Constants.checkUpdateInterval);
		
		Tools.log(this, "HXBlog initinized !");
	}
	
	// context被销毁的时候调用
	public void contextDestroyed(ServletContextEvent scv) {
		updateCheckTimer.cancel();
	}
	

}
