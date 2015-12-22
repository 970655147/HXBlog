package com.hx.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.hx.util.Constants;
import com.hx.util.Tools;

// ��ʼ��, ��������ʱ�����µ�ContextListener
public class InitAndCheckUpdateListener implements ServletContextListener {

	// ���ƶ�ʱ�����Timer
	Timer updateCheckTimer = new Timer();
	
	// context��ʼ����ʱ�����
	// ��ʱ��������
		// ��ʱ���BlogListAction���Ƿ���ڸ��µ�����, ����� ��ˢ�µ����ݿ�
		// ���ڼ����־, �������־����, ��ˢ�µ���־�ļ�
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
	
	// context�����ٵ�ʱ�����
	// ȥ����ʱ����, ˢ��δˢ�µ�����
	// �ͷ�BlogManager, Comment����Դ, ע��driver
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
	
	// ���ڼ����µ�����
	// ��ʱ������blog, tag�ĸ���
		// ������ڸ��½���ˢ�µ����ݿ�
		// ����  ����Ƿ����, ����, visite�ĸ���, ����� ����ˢ�µ����ݿ�
	// ������comment�ĸ���
		// ������ڸ��½���ˢ�µ����ݿ�
	// ������ڸ��µ���־, ����־ˢ����־�ļ�
	// ������ڸ���, ���б���
	public void checkUpdate(ServletContext servletContext) {
		int updated = BlogManager.getUpdated();
		boolean isUpdated = false;
		if(updated > 0) {
			isUpdated = true;
			BlogManager.flushToDB();
			Tools.log(this, "checkUpdateBlog, updated : " + updated);
		} else {
			updated = BlogManager.getVistitedSensedUpdate();
			if(updated > 0) {
				isUpdated = true;
				BlogManager.flushToDBForVisitedSensed(servletContext);
				Tools.log(this, "checkVistitedSensedUpdateBlog, updated : " + updated);
			}
		}
		
		updated = CommentManager.getUpdated();
		if(updated > 0) {
			isUpdated = true;
			CommentManager.flushToDB();
			Tools.log(this, "checkUpdateComment, updated : " + updated);
		}
		CommentManager.clearFrequencyMap();
		
		if(Tools.getLogBufferSize() > 0) {
			Tools.flushLog(Constants.dateFormat.format(new Date()) );
		}
		
		if(isUpdated) {
			try {
				doBackup(getBackupConf() ) ;
			} catch (Exception e) {
				Tools.log(this, "error while backup ! Exception : " + e.getMessage() );
				e.printStackTrace();
			}
		}
	}

	// ��������
	public void doBackup(JSONObject backupConf) throws Exception {
		String backupDir = backupConf.getString("backupDir");
		JSONArray backupPathes = backupConf.getJSONArray("backupPathes");
		if((backupPathes == null) || (backupDir == null) ) {
			return ;
		}
		
		for(int i=0; i<backupPathes.size(); i++) {
			String subPath = backupPathes.getString(i);
			File backupFile = new File(Tools.getPath(Tools.getProjectPath(), subPath) );
			if(backupFile.exists() ) {
				File targetFile = new File(Tools.getPath(backupDir, subPath) );
				copyIfModified(backupFile, targetFile, true);
			} else {
				Tools.log(this, "target backupFile : " + backupFile.getAbsolutePath() + " does not exists !" );
			}
		}
	}

	// ��ȡ���ݵ���Ϣ
	private JSONObject getBackupConf() throws Exception {
		if(! Tools.isFileExists(Tools.getPackagePath(Tools.getProjectPath(), Constants.backupConfPath)) ) {
			return JSONObject.fromObject(Constants.defaultBackupConf);
		} else {
			String content = Tools.getContent(Tools.getPackagePath(Tools.getProjectPath(), Constants.backupConfPath), Constants.DEFAULT_CHARSET );
			return JSONObject.fromObject(content);
		}
	}

	// ����������ļ�������, ����и���
    public static void copyIfModified(File src, File dst, boolean isOverride) throws IOException {
        if(! src.exists() ) {
            Tools.log(Tools.class, "srcFile \" " + src.getAbsolutePath() + " \" do not exists ...");
            return ;
        }
//        if(dst.exists() ) {
//        	if(! isOverride) {
//        		Tools.log(Tools.class, "dstFile \" " + dst.getAbsolutePath() + " \" does exists, please check it ...");
//        		return ;
//        	} else {
//        		Tools.log(Tools.class, "dstFile \" " + dst.getAbsolutePath() + " \" does exists, override it ...");
//        	}
//        }

        if(src.isDirectory() && ((! dst.exists()) || dst.isDirectory()) ) {
        	if(! dst.exists() ) {
        		dst.mkdirs();
        	}
            File[] childs = src.listFiles();
            for(File child : childs) {
            	File dstChild = new File(dst, child.getName() );
            	copyIfModified(child, dstChild, isOverride);
            }
            Tools.log(Tools.class, "copy folder \" " + src.getAbsolutePath() + " \" -> \" " + dst.getAbsolutePath() + " \" success ...");
        } else if(src.isFile() && ((! dst.exists()) || dst.isFile()) ) {
        	if(! dst.exists() ) {
        		dst.createNewFile();
        	}
        	if(src.length() != dst.length() ) {
		        FileInputStream fis = new FileInputStream(src);
		        FileOutputStream fos = new FileOutputStream(dst);
		        Tools.copy(fis, fos);
		        Tools.log(Tools.class, "copy file \" " + src.getAbsolutePath() + " \" -> \" " + dst.getAbsolutePath() + " \" success ...");
        	} else {
        		Tools.log(Tools.class, "keep file \" " + dst.getAbsolutePath() + " \" , cause it not be modified ...");
        	}
        } else {
        	Tools.log(Tools.class, "src & dst must be both 'File' or 'Folder' ! ");
        }
    }
    public static void copyIfModified(File src, File dst) throws IOException {
    	copyIfModified(src, dst, false);
    }
    
}
