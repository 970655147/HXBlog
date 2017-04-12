/**
 * file name : Test06WindowsAlwaysOn.java
 * created at : 9:42:46 PM Aug 22, 2015
 * created by 970655147
 */

package com.hx.blog.test;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.util.Date;

import com.hx.blog.util.Log;

public class Test06WindowsAlwaysOn {
	
	// ��ϵͳ����[�������˯��״̬]
	public static void main(String []args) {
		
		alwaysOn();
		
	}

	// robot ģ���û�����
	static Robot robot;
	static int SLEEP_INTERVAL = 20 * 1000;
	static Point pos;
	static {
		try {
			robot = new Robot();
			pos = new Point(100, 100);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	// ��ϵͳ����[�������˯��״̬]
	public static void alwaysOn() {
		while(true ) {
			Log.log("program run now : " + getCurrentTime() );
			Point pos = getNextPoint();
			robot.mouseMove(pos.x, pos.y);
			sleep(SLEEP_INTERVAL);
		}
	}

	// ��ȡ��һ������λ��
	private static Point getNextPoint() {
		return pos;
	}
	
	// ��ȡ��ǰʱ����ַ�����ʾ
	private static String getCurrentTime() {
		return new Date().toString();
	}

	// �ǵ�ǰ�߳�˯��sleep��
	public static void sleep(int sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
