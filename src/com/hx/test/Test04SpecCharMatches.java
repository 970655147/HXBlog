/**
 * file name : Test04SpecCharMatchesa.java
 * created at : 9:48:46 PM Dec 22, 2015
 * created by 970655147
 */

package com.hx.test;

import java.util.regex.Matcher;

import com.hx.action.BlogPublishAction;
import com.hx.action.BlogReviseAction;
import com.hx.business.InitAndCheckUpdateListener;
import com.hx.util.Constants;
import com.hx.util.Log;
import com.hx.util.Tools;

public class Test04SpecCharMatches {
	
	// specChars����ƥ��
	public static void main(String []args) {
		
		Matcher matcher = Constants.specCharPattern.matcher("df:ghffgh");
		Log.log(matcher.matches() );
		
//		new Tools();
		
//		new BlogReviseAction();
//		new BlogPublishAction();
//		new InitAndCheckUpdateListener();
		
	}

}