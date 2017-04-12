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
	
	// 是系统常亮[不会进入睡眠状态]
	public static void main(String []args) {
		
		alwaysOn();
		
	}

	// robot 模拟用户操作
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
	
	// 是系统常亮[不会进入睡眠状态]
	public static void alwaysOn() {
		while(true ) {
			Log.log("program run now : " + getCurrentTime() );
			Point pos = getNextPoint();
			robot.mouseMove(pos.x, pos.y);
			sleep(SLEEP_INTERVAL);
		}
	}

	// 获取下一个鼠标的位置
	private static Point getNextPoint() {
		return pos;
	}
	
	// 获取当前时间的字符串表示
	private static String getCurrentTime() {
		return new Date().toString();
	}

	// 是当前线程睡眠sleep秒
	public static void sleep(int sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
