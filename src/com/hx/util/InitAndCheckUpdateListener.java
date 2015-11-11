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
		Tools.init(scv.getServletContext() );
		BlogManager.initIfNeeded();
		updateCheckTimer.schedule(new TimerTask() {
			public void run() {
				checkUpdate(scv.getServletContext() );
			}
		}, Constants.checkUpdateInterval, Constants.checkUpdateInterval);
		
		Tools.log(this, "HXBlog initinized !");
	}
	
	// context被销毁的时候调用
	// 去掉定时任务, 刷新未刷新的数据
	// 释放BlogManager, Comment的资源, 注销driver
	public void contextDestroyed(ServletContextEvent scv) {
		updateCheckTimer.cancel();
		Tools.log(this, "HXBlog destroyed !");
		checkUpdate(scv.getServletContext() );
		BlogManager.clear();
		CommentManager.clear();
		try {
			Tools.closeDataSource(Tools.getProjectPath() );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 定期检查更新的任务
	// 定时检查对于blog, tag的更新
		// 如果存在更新将其刷新到数据库
		// 否则  检查是否存在, 顶踩, visite的更新, 如果有 将其刷新到数据库
	// 检查对于comment的更新
		// 如果存在更新将其刷新到数据库
	// 如果存在更新的日志, 将日志刷到日志文件
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
