package com.hx.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hx.business.BlogManager;
import com.hx.business.CommentManager;

// ��ʼ��, ��������ʱ�����µ�ContextListener
public class InitAndCheckUpdateListener implements ServletContextListener {

	// ���ƶ�ʱ�����Timer
	Timer updateCheckTimer = new Timer();
	
	// context��ʼ����ʱ�����
	// ��ʱ��������
		// ��ʱ���BlogListAction���Ƿ���ڸ��µ�����, ����� ��ˢ�µ����ݿ�
		// ���ڼ����־, �������־����, ��ˢ�µ���־�ļ�
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
	
	// context�����ٵ�ʱ�����
	public void contextDestroyed(ServletContextEvent scv) {
		updateCheckTimer.cancel();
		Tools.log(this, "HXBlog destroyed !");
		checkUpdate(scv.getServletContext() );
	}
	
	// ���ڼ����µ�����
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
