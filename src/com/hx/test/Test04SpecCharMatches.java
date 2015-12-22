/**
 * file name : Test04SpecCharMatchesa.java
 * created at : 9:48:46 PM Dec 22, 2015
 * created by 970655147
 */

package com.hx.test;

import java.util.regex.Matcher;

import com.hx.util.Constants;
import com.hx.util.Log;

public class Test04SpecCharMatches {
	
	// specChars’˝‘Ú∆•≈‰
	public static void main(String []args) {
		
		Matcher matcher = Constants.specCharPattern.matcher("dfghf?fgh");
		Log.log(matcher.matches() );
		
	}

}
