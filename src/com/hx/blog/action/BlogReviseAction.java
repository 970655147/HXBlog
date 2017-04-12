package com.hx.blog.action;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.blog.bean.Blog;
import com.hx.blog.bean.ResponseMsg;
import com.hx.blog.bean.ValidateResult;
import com.hx.blog.business.BlogManager;
import com.hx.blog.interf.BaseAction;
import com.hx.blog.interf.Validater;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

public class BlogReviseAction extends BaseAction {

    // �޸Ĳ���
    // ȷ���û���¼, У��У����
    // У��id, title, tags, content
    // ��ȡblogId��Ӧ��blog, У���blog
    // �ж��Ƿ�����˲��͵�����
    // ���û��, ֱ�Ӹ���Ԫ����, �Լ������ļ�
    // ����  ���²����ļ�֮��, ����������, ���³����ڵ��ļ�����
    // ��blog����BlogManager�������blog��ҵ��
    // ���� ��Ӧ���, ��¼��־
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
        resp.setHeader("Content-Type", "text/html;charset=" + Constants.DEFAULT_CHARSET);

        ResponseMsg respMsg = new ResponseMsg();
        Blog blogInServer = (Blog) req.getAttribute(Constants.blog);
        Blog newBlog = new Blog(blogInServer);
        doReviseBlog(blogInServer, newBlog, req);

        respMsg = new ResponseMsg(Constants.respSucc, Constants.defaultResponseCode, Tools.getPostSuccMsg(newBlog), null);
        req.setAttribute(Constants.result, respMsg);
    }

    /**
     * �����޸Ĳ�����Ϣ�����
     *
     * @param blogInServer �������˵�blog
     * @param newBlog      �޸�֮���blog
     * @param req          �ͻ��˵�request
     * @return void
     * @author Jerry.X.He
     * @date 4/11/2017 6:19 PM
     * @since 1.0
     */
    private void doReviseBlog(Blog blogInServer, Blog newBlog, HttpServletRequest req) throws IOException {
        Integer id = Integer.parseInt((String) req.getAttribute(Constants.id));
        String title = (String) req.getAttribute(Constants.title);
        String tags = (String) req.getAttribute(Constants.tags);
        String content = (String) req.getAttribute(Constants.content);
        String oldBlogPath = blogInServer.getPath();

        // Ԥ����content
        content = Tools.prepareContent(content);
        boolean isChangeName = !Tools.equalsIgnorecase(blogInServer.getTitle().trim(), title.trim());
        String blogPath = Tools.getBlogFileName(Tools.getDateFromBlogFileName(blogInServer.getPath()), title);
        newBlog.set(id, title, blogPath, tags, null);

        Tools.save(content, Tools.getBlogPath(Tools.getProjectPath(), oldBlogPath));
        if (isChangeName) {
            Tools.renameTo(Tools.getBlogPath(Tools.getProjectPath(), oldBlogPath), Tools.getBlogPath(Tools.getProjectPath(), blogPath));
        }
        BlogManager.reviseBlog(newBlog);
    }

}
