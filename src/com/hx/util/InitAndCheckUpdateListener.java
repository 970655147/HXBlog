package com.hx.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hx.action.BlogListAction;

// ��ʼ��, ��������ʱ�����µ�ContextListener
public class InitAndCheckUpdateListener implements ServletContextListener {

	// ���ƶ�ʱ�����Timer
	Timer updateCheckTimer = new Timer();
	
	// context��ʼ����ʱ�����
	// ��ʱ��������
		// ��ʱ���BlogListAction���Ƿ���ڸ��µ�����, ����� ��ˢ�µ����ݿ�
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
	
	// context�����ٵ�ʱ�����
	public void contextDestroyed(ServletContextEvent scv) {
		updateCheckTimer.cancel();
	}
	

}
