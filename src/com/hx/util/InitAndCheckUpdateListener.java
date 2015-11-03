package com.hx.util;

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
	public void contextInitialized(ServletContextEvent scv) {
		BlogListAction.initIfNeeded(scv.getServletContext() );
		updateCheckTimer.schedule(new CheckUpdateTask(), Constants.checkUpdateInterval, Constants.checkUpdateInterval);
	}
	
	// context�����ٵ�ʱ�����
	public void contextDestroyed(ServletContextEvent scv) {
		updateCheckTimer.cancel();
	}
	
	// ��ʱ��������
		// ��ʱ���BlogListAction���Ƿ���ڸ��µ�����, ����� ��ˢ�µ����ݿ�
	static class CheckUpdateTask extends TimerTask {

		public void run() {
			int updated = BlogListAction.getUpdated();
			if(updated > 0) {
				BlogListAction.flushToDB();
			}
		}
		
	}

}
