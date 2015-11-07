package com.hx.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hx.business.BlogManager;
import com.hx.business.CommentManager;

// 初始化, 并启动定时检查更新的ContextListener
public class InitAndCheckUpdateListener implements ServletContextListener {

	// 控制定时任务的Timer
	Timer updateCheckTimer = new Timer();
	
	// context初始化的时候调用
	// 定时检查的任务
		// 定时检查BlogListAction中是否存在更新的数据, 如果有 则刷新到数据库
		// 定期检查日志, 如果有日志缓存, 则刷新到日志文件
	public void contextInitialized(final ServletContextEvent scv) {
		Tools.setProjectPath(scv.getServletContext() );
		BlogManager.initIfNeeded();
		updateCheckTimer.schedule(new TimerTask() {
			public void run() {
				checkUpdate(scv.getServletContext() );
			}
		}, Constants.checkUpdateInterval, Constants.checkUpdateInterval);
		
		Tools.log(this, "HXBlog initinized !");
	}
	
	// context被销毁的时候调用
	public void contextDestroyed(ServletContextEvent scv) {
		updateCheckTimer.cancel();
		Tools.log(this, "HXBlog destroyed !");
		checkUpdate(scv.getServletContext() );
	}
	
	// 定期检查更新的任务
	private void checkUpdate(ServletContext servletContext) {
		int updated = BlogManager.getUpdated();
		if(updated > 0) {
			BlogManager.flushToDB();
			Tools.log(this, "checkUpdateBlog, updated : " + updated);
		} else {
			updated = BlogManager.getVistitedSensedUpdate();
			if(updated > 0) {
				BlogManager.flushToDBForVisitedSensed(servletContext);
				Tools.log(this, "checkVistitedSensedUpdateBlog, updated : " + updated);
			}
		}
		
		updated = CommentManager.getUpdated();
		if(updated > 0) {
			CommentManager.flushToDB();
			Tools.log(this, "checkUpdateComment, updated : " + updated);
		}
		CommentManager.clearFrequencyMap();
		
		if(Tools.getLogBufferSize() > 0) {
			Tools.flushLog(Constants.dateFormat.format(new Date()) );
		}
	}
	

}
