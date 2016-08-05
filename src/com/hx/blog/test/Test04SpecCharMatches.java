/**
 * file name : Test04SpecCharMatchesa.java
 * created at : 9:48:46 PM Dec 22, 2015
 * created by 970655147
 */

package com.hx.blog.test;

import java.util.regex.Matcher;

import com.hx.blog.action.BlogPublishAction;
import com.hx.blog.action.BlogReviseAction;
import com.hx.blog.filter.InitAndCheckUpdateListener;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Log;
import com.hx.blog.util.Tools;

public class Test04SpecCharMatches {
	
	// specChars’˝‘Ú∆•≈‰
	public static void main(String []args) {
		
		Matcher matcher = Constants.specCharPattern.matcher("df:ghffgh");
		Log.log(matcher.matches() );
		
//		new Tools();
		
//		new BlogReviseAction();
//		new BlogPublishAction();
//		new InitAndCheckUpdateListener();
		
	}

}
