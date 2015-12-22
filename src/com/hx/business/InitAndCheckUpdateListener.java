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

// 初始化, 并启动定时检查更新的ContextListener
public class InitAndCheckUpdateListener implements ServletContextListener {

	// 控制定时任务的Timer
	Timer updateCheckTimer = new Timer();
	
	// context初始化的时候调用
	// 定时检查的任务
		// 定时检查BlogListAction中是否存在更新的数据, 如果有 则刷新到数据库
		// 定期检查日志, 如果有日志缓存, 则刷新到日志文件
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
	
	// context被销毁的时候调用
	// 去掉定时任务, 刷新未刷新的数据
	// 释放BlogManager, Comment的资源, 注销driver
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
	
	// 定期检查更新的任务
	// 定时检查对于blog, tag的更新
		// 如果存在更新将其刷新到数据库
		// 否则  检查是否存在, 顶踩, visite的更新, 如果有 将其刷新到数据库
	// 检查对于comment的更新
		// 如果存在更新将其刷新到数据库
	// 如果存在更新的日志, 将日志刷到日志文件
	// 如果存在更新, 进行备份
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

	// 备份数据
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

	// 获取备份的信息
	private JSONObject getBackupConf() throws Exception {
		if(! Tools.isFileExists(Tools.getPackagePath(Tools.getProjectPath(), Constants.backupConfPath)) ) {
			return JSONObject.fromObject(Constants.defaultBackupConf);
		} else {
			String content = Tools.getContent(Tools.getPackagePath(Tools.getProjectPath(), Constants.backupConfPath), Constants.DEFAULT_CHARSET );
			return JSONObject.fromObject(content);
		}
	}

	// 如果给定的文件更新了, 则进行复制
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
