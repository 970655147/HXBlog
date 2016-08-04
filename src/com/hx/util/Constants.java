package com.hx.util;

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.hx.bean.ValidateResult;

// ����
public class Constants {

	// Ĭ�ϵ��ַ���
	public final static String DEFAULT_CHARSET = "GBK";
	
	// ���ݿ��λ��, �����ļ���λ��, ���������ļ���λ��
	public final static String dbPath = "com/hx/config/HXBlog.db";
	public final static String configPath = "com/hx/config/config.json";
	public final static String resumePath = "com/hx/config/resume.json";
	public final static String backupConfPath = "com/hx/config/backup.json";
	
	// ����blogList, tagList�� ��sql
	// ����blogList, tagList��Ԫ�ص�sql [���Ӳ��͵ĳ���]
	// ɾ��blogList, tagList��Ԫ�ص�sql [ɾ�����͵ĳ���]
	// ����blogList, tagList��Ԫ�ص�sql [���²��͵ĳ���]
	// ����comment��sql
	public final static String blogListSql = "select * from blogList";
	public final static String tagListSql = "select * from tagToBlog";
	public final static String addBlogListSql = "insert into blogList (id, path, tag, createTime, good, notGood, visited, commentsNum) values ('%d', '%s', '%s', '%s', %d, %d, %d, %d);";
	public final static String addMultiBlogListSql = "insert into blogList (id, path, tag, createTime, good, notGood, visited, commentsNum) %s;";
//	public final static String addTagListSql = "insert into tagToBlog (tag, blogId) values ('%s', '%d');";
//	public final static String addMultiTagListSql = "insert into tagToBlog (tag, blogId) %s;";
	public final static String addMultiCommentListSql = "insert into commentList (blogIdx, floorIdx, commentIdx, name, email, headImgIdx, date, toUser, privilege, comment) %s;";	
	public final static String deleteBlogListSql = "delete from blogList where id = '%d'";
	public final static String deleteMultiBlogListSql = "delete from blogList where id in (%s)";
//	public final static String deleteTagListSql = "delete from tagToBlog where blogId = '%d'";
//	public final static String deleteMultiTagListSql = "delete from tagToBlog where blogId = '%d' and tag in (%s)";
	public final static String updateBlogListSql = "update blogList set path='%s', tag='%s', good=%d, notGood=%d, visited=%d, commentsNum=%d where id = %d";
//	public final static String deleteByTagAndIdTagListSql = "delete from tagToBlog where tag = '%s' and blogId = '%d'";
	public final static String deleteCommentByBlogIdxSql = "delete from commentList where blogIdx=%d";	
	public final static String deleteCommentByFloorIdxSql = "delete from commentList where blogIdx=%d and floorIdx in (%s)";	
	public final static String deleteCommentByCommentIdxSql = "delete from commentList where blogIdx=%d and commentIdx in (%s)";	

	// ��ȡ�����Ĳ��͵���������
	public final static String getBlogCommentByBlogIdSql = "select * from commentList where blogIdx = %d";
	
	// �����������Ŀ·����·��, uEditor��index.html��λ��, ��־�ļ���
	public final static String blogFolder = "WEB-INF/post";
	public final static String uEditorIdx = "ueditorLib/index.html";
	public final static String tmpFolder = "WEB-INF/tmp";
	public final static String logFolder = "WEB-INF/log";
	
	// Ĭ�ϵ�blogId [δ��], Ĭ�ϵ�blogPath, Ĭ�ϵ�blogTag, Ĭ�ϵ�good������, notGood������, defaultVisited������
	// Ĭ�ϵ�cookieValue
	public final static Integer defaultBlogId = -1;
	public final static String defaultBlogPath = "unknown";
	public final static String defaultBlogTag = "unknown";
	public final static AtomicInteger defaultGood =  new AtomicInteger(0);
	public final static AtomicInteger defaultNotGood = defaultGood;
	public final static AtomicInteger defaultVisited = defaultGood;
	public final static AtomicInteger defaultCommentsNum = defaultGood;
	public final static String defaultCookieValue = Tools.NULL;
	
	// Ĭ�ϵ�����, Ĭ�ϵĽ�������, ��ʽ�����ڵ�dateFormat [ǰ��������ʾ, �������ڹ����ļ�]
	public static JSONObject defaultConfig = JSONObject.fromObject("{ \"title\" : \"����970655147\", \"subTitle\" : \"�ü��Բ����ñ�ͷ\", \"quickLinks\" : [ {\"text\" : \"Blog\", \"href\" : \"/HXBlog/#!/tag/all\"}, {\"text\" : \"Resume\", \"href\" : \"/HXBlog/#!/resume\"}, {\"text\" : \"Github\", \"href\" : \"https://github.com/970655147\"}, {\"text\" : \"CSDNBlog\", \"href\" : \"http://blog.csdn.net/u011039332\"}, ]}");
	public static JSONObject defaultResume = JSONObject.fromObject("{\"basicInfo\":{\"name\":\"JerryHe\",\"description\":\"Oneprogrammerwhoenjoysprogramming&expectschallenges\",\"mail\":\"970655147@qq.com\"},\"education\":[{\"period\":\"2012/09-2016/06\",\"university\":\"cduestc\",\"college\":\"cduestc\",\"description\":\"BachelorDegreeincduestc\"}],\"workExperience\":[{\"period\":\"2015/06�C2015/09\",\"company\":\"XXX\",\"title\":\"javapracticeengineer\",\"description\":[\"crawlertasks\"]}],\"skills\":[{\"category\":\"Programminglanguage\",\"items\":[{\"name\":\"Java\",\"proficiency\":\"proficient\"},{\"name\":\"C++\",\"proficiency\":\"familiar\"},{\"name\":\"C\",\"proficiency\":\"familiar\"},{\"name\":\"Scheme\",\"proficiency\":\"familiar\"},{\"name\":\"Python\",\"proficiency\":\"familiar\"},{\"name\":\"C#\",\"proficiency\":\"familiar\"},{\"name\":\"C#\",\"proficiency\":\"familiar\"},{\"name\":\"Javascript\",\"proficiency\":\"familiar\"}]},{\"category\":\"VersionControl\",\"items\":[{\"name\":\"Git\",\"proficiency\":\"proficient\"},{\"name\":\"SVN\",\"proficiency\":\"familiar\"},{\"name\":\"Synergy\",\"proficiency\":\"familiar\"}]}],\"projects\":[{\"name\":\"HXSyntaxTree\",\"href\":\"\",\"description\":\"analysisthespecified'.java''sAST.\"},{\"name\":\"HXServer\",\"href\":\"http://blog.csdn.net/u011039332/article/details/50075125\",\"description\":\"asimple'Server'likeasimple'Tomcat',implements'Servlet','Filter',andsoon.\"},{\"name\":\"HXBlog\",\"href\":\"http://blog.csdn.net/u011039332/article/details/49869169\",\"description\":\"Astaticblogongithub.usingjavascriptcomminicationwithserver.\"},{\"name\":\"HXCrawler\",\"href\":\"http://blog.csdn.net/u011039332/article/details/48860929\",\"description\":\"alightweight'Crawler'framework.\"}]}");
	public static JSONObject defaultBackupConf = JSONObject.fromObject("{\"backupDir\":\"D:\\Assist\\bak\\HXBlog\",\"backupPathes\":[\"upload\",\"WEB-INF/post\",\"WEB-INF/log\",\"WEB-INF/classes/com/hx/config\"]}");
	public final static SimpleDateFormat createDateFormat = new SimpleDateFormat("yyyy-MM-dd HH : mm");
	public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
	
	// ��Ӧ�ɹ�, ʧ�ܵı�־, Ĭ�ϵ���Ӧ��
	public final static Boolean respSucc = true;
	public final static Boolean respFailed = false;
	public final static Integer defaultResponseCode = 0;
	
	// ���ˢ�µ����ݿ������, ���¸�������ֵ [������ֵ, ֱ��ˢ�µ����ݿ�], ����tag��ʱ��, ɾ�����б�ǩ����ֵ [δ��]
	// �������������, ˢ�¸������۵���ֵ, addCommentList�ĳ�ʼ����
//	public final static long checkUpdateInterval = 60 * 1000;
	public final static long checkUpdateInterval = 10 * 60 * 1000;
	public final static int updateBlogThreashold = 10;
	public final static int concurrencyOff = 10;
	public final static int updateCachedCommentsOff = 4;
	public final static int addedBlogsListSize = updateBlogThreashold >> 1 + concurrencyOff;
	public static int getDeleteAllTagThreshold(int allTag) {
		return allTag >> 1;
	}
	public final static int cachedComments = 20;
	public final static int updateCommentsThreshold = 20;
	public final static int addedCommentsListSize = updateCommentsThreshold + concurrencyOff;
	
	// ��ʾ���µ����ݵ����� [���, ɾ��, �޸�]
	public final static int addBlogCnt = 0;
	public final static int addTagCnt = 1;
	public final static int deletedBlogCnt = 2;
	public final static int deletedTagCnt = 3;
	public final static int revisedBlogCnt = 4;
	public final static int addCommentCnt = 0;
	public final static int deleteCommentByBlogIdxCnt = 1;
	public final static int deleteCommentByFloorIdxCnt = 2;
	public final static int deleteCommentByCommentIdxCnt = 3;
	
	// �Ƿ�����debug, �Ƿ��ӡ��־, ��־�ļ�����ֵ[����һ���̶�, �л���־�ļ�]
	public final static boolean debugEnable = true;
	public final static boolean logEnable = true;
//	public final static long logFileThreshold = 2 << 10;
	public final static long logFileThreshold = 512 << 10;
	
	// "userName", "toekn"
	// �û���, token, ����md5 [123]
	public final static String ACCOUNT_NAME = "userName";
	public final static String TOKEN = "token";
	public final static String account = "970655";
	public final static String token = "970655";
	public final static String pwd = "202cb962ac59075b964b07152d234b70";
	
	// "preferInfo", ����Ա����, ����, �Լ�Ĭ��ʹ�õ�ͷ��, ����
	public final static String preferInfo = "preferInfo";
	public final static String adminUserName = "����970655147";
	public final static String adminEmail = "970655147@qq.com";
	public final static int adminImageIdx = 0;
	
	// �޸İ�ť, ɾ����ť
	// �������µ�Ԫ��, ��¼, �ǳ���Ԫ��
	// Ĭ�ϵı�ǩ�б�, Ĭ�ϵ�blogList����ֵ
	public final static String reviseBtn = "<a href='/HXBlog/#!/blogPublishAction?blogId=%d&revise=true'>�޸�</a>";
	public final static String deleteBtn = "<a id='deleteAction' href='javascript:void(0)'>ɾ��</a>";
	public final static JSONObject publishBlogConf = new JSONObject().element("text", "Publish").element("href", "/HXBlog/#!/blogPublishAction");
	public final static JSONObject logoutBlogConf = new JSONObject().element("text", "Logout").element("href", "/HXBlog/action/blogLogoutAction");
	public final static JSONObject loginBlogConf = new JSONObject().element("text", "Login").element("href", "/HXBlog/login.html");
	public final static JSONArray defaultTagList = new JSONArray().element(new JSONObject().element("text", Constants.defaultBlogTag).element("cnt", 0));
	public final static JSONObject defaultTagListAndBlogList = new JSONObject().element("tagList", Constants.defaultTagList).element("blogList", new JSONObject() );
	
	// �Ƿ񻹴�����һҳ, ��һƪ���µı�־
	// �Ƿ�������tag�ı�־
	public final static int HAVE_NO_THIS_TAG = -1;
	public final static int HAVE_NO_PREV_IDX = -1;
	public final static int HAVE_NO_NEXT_IDX = -2;
	
	// 0 ����
	public final static Integer INTE_ZERO = new Integer(0);
	public final static int ZERO = 0;

	// У����صĳ���, title�������, �����ַ���У��Pattern
	public final static int titleMaxLength = 30;
//	public final static Pattern specCharPattern = Pattern.compile(".*[!-/|:-@|{-~|\\[/\\]^����].*");
	public final static Pattern specCharPattern = Pattern.compile(".*[\\|/|:|\\*|\\?|\\\"|<|>|\\|].*");
	
	// �Ƿ��������ȵ�cookieName, һϵ�е�cookie��س���, [visitedMap, senseCookieValue δ��]
	public final static String visitedCookieName = "isVisited";
	public final static String visitedMap = "visitedMap";
	public final static String visitedCookieValue = "visited";
	public final static String senseCookieName = "isSensed";
	public final static String senseCookieValue = "sensed";
	public final static String senseGood = "good";
	public final static String senseNotGood = "notGood";
	
	// ���۵�privilege, Ĭ�ϵ�floorIdx, commentIdx, Ĭ�ϵ�toUser
	public final static String admin = "admin";
	public final static String guest = "guest";
	public final static int defaultFloorIdx = 1;
	public final static int defaultCommentIdx = 1;
	public final static String defaultTo = account;
	public final static int defaultMaxCommentIdx = 2;
	public final static Pattern emailPattern = Pattern.compile("\\w{7,11}@\\w+(\\.\\w+)+");
	
	// reply�Ŀ�ʼ����, �������� , sqlite����Ҫת����ַ�, �ű���ʼ, �������ַ�
	public final static String replyStart = "[re]";
	public final static String replyEnd = "[/re] : ";
	public final static Map<Character, Character> needToBeFormatMap = new HashMap<>();
	public final static Set<Character> needToBeDeformat = new HashSet<>();
	public final static Map<String, String> scriptCharacterMap = new HashMap<>();
	
	// "checkCode", ��֤��ĳ���, ��֤�뱸ѡ���ַ�����, ��ѡ���ַ�
	// ��֤��Ŀ��, ��֤��ĸ��������ٵĸ���, �����ߵĸ���ƫ��
	// ��֤��ı���ɫ, ��֤�������
	public final static String checkCode = "checkCode";
	public final static int checkCodeLength = 4;
	public final static int checkCodeBackUpSize = 36;
	public final static List<Character> checkCodes = new ArrayList<>(checkCodeBackUpSize);
	public final static int checkCodeWidth = 200;
	public final static int checkCodeHeight = 100;
	public final static int checkCodeMinInterference = 10;
	public final static int checkCodeInterferenceOff = 5;
	public final static Color checkCodeBgColor = Color.WHITE;
	public final static Font checkCodeFont = new Font("΢���ź�", Font.ITALIC, 40);
	
	// ��ʼ����ؼ���
	static {
		needToBeFormatMap.put(Tools.INV_SLASH, Tools.INV_SLASH);
		needToBeFormatMap.put(Tools.QUOTION, Tools.QUOTION);
		needToBeFormatMap.put('[', Tools.INV_SLASH);
		needToBeFormatMap.put(']', Tools.INV_SLASH);
		needToBeFormatMap.put('%', Tools.INV_SLASH);
		needToBeFormatMap.put('&', Tools.INV_SLASH);
		needToBeFormatMap.put('_', Tools.INV_SLASH);
		needToBeFormatMap.put('(', Tools.INV_SLASH);
		needToBeFormatMap.put(')', Tools.INV_SLASH);
		
		needToBeDeformat.add(Tools.INV_SLASH);
		needToBeDeformat.add(Tools.QUOTION);
		
		scriptCharacterMap.put("<", "&lt;");
		scriptCharacterMap.put(">", "&gt;");
		scriptCharacterMap.put("&", "&amp;");
		
		for(char i='0'; i<'9'; i++) {
			checkCodes.add(i);
		}
		for(char i='a'; i<'z'; i++) {
			checkCodes.add(i);
		}
		for(char i='A'; i<'Z'; i++) {
			checkCodes.add(i);
		}
	}
	
	// ��־��Ҫ��¼��blog�����ݵĳ���
	public final static int logBlogContentMaxLength = 100;
	public final static int blogPerPage = 20;
	public final static int maxTagLength = 10;
	public final static int maxTagSize = 10;
	
	// Ĭ�ϵ�ValidateResult
	public final static ValidateResult validateResultTrue = new ValidateResult(false, null);
	public final static ValidateResult validateResultFalse = new ValidateResult(false, null);
	public final static ValidateResult defaultValidateResult = validateResultTrue;
	
}
