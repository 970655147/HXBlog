package com.hx.blog.action;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hx.blog.bean.Blog;
import com.hx.blog.bean.Comment;
import com.hx.blog.business.BlogManager;
import com.hx.blog.business.CommentManager;
import com.hx.blog.interf.BaseAction;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

import net.sf.json.JSONObject;

public class BlogGetAction extends BaseAction {

    // �������͵�����
    // ��ȡblogId, ȷ����Ϊ����
    // ��ȡblogId��Ӧ��blog, ȷ����blog����
    // ȷ��blog��Ӧ���ļ��Ƿ����
    // ����� ���¸ò��͵�visited, �����ظò��͵���Ϣ
    // ��ӵ�¼��Ϣ, ������Ϣ
    // ���blogId��Ϊnull, ��ȡ������Ϣ, �Լ��ò��͵�������Ϣ, �Լ��û���ƫ����Ϣ [����, ����]
    // �����¼, ���ȡ�޸�, ɾ����ť��Ϣ, �ڻ�ȡ��ǰ���͵���һҳ, �Լ���һҳ�Ĳ���id
    // ���� ��Ӧ���, ��¼��־
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
        resp.setHeader("Content-Type", "text/html;charset=" + Constants.DEFAULT_CHARSET);

        JSONObject injectorInfo = new JSONObject();
        Integer blogId = null;
        String tag = (String) req.getAttribute(Constants.tag);
        Blog blog = null;
        try {
            blogId = Integer.parseInt((String) req.getAttribute(Constants.blogId));
        } catch (Exception e) {
            blogId = null;
        }

        if (blogId == null) {
            blog = new Blog(Constants.defaultBlogId, "have no this blog !", Constants.defaultBlogPath, Constants.defaultBlogTag, null, Constants.defaultGood, Constants.defaultNotGood, Constants.defaultVisited, Constants.defaultCommentsNum);
            blog.setContent("hi, you have been intercept ~ ~ ~ ~ !");
            injectorInfo.element("blogId", req.getAttribute("blogId")).element("ip", Tools.getIPAddr(req));
        } else {
            Blog blogInServer = BlogManager.getBlog(blogId);
            if (blogInServer == null) {
                blog = new Blog(Constants.defaultBlogId, "have no this blog !", Constants.defaultBlogPath, Constants.defaultBlogTag, null, Constants.defaultGood, Constants.defaultNotGood, Constants.defaultVisited, Constants.defaultCommentsNum);
                blog.setContent("have no this blog !");
                injectorInfo.element("blogId", blogId).element("ip", Tools.getIPAddr(req));
            } else {
                blog = doGetBlogInfo(blogInServer, req, resp);
            }
        }

        JSONObject res = new JSONObject();
        boolean isLogin = Tools.isLogin(req);
        res.element("isLogin", isLogin);
        res.element("blog", blog.toString());
        if (blogId != null) {
            putOtherInfo(res, req, blog, tag, isLogin, injectorInfo);
        }

        req.setAttribute(Constants.result, res);
        if (injectorInfo.size() > 0) {
            Tools.log(this, injectorInfo.toString());
        }
        injectorInfo = null;
    }

    /**
     * ��ȡ��ǰ���͵�������Ϣ, ������ڷ��صĽ��Blog��
     *
     * @param blogInServer ��ǰ��Ҫ��ȡ�Ĳ�����Ϣ
     * @param req          �ͻ��˵�reqest
     * @param resp         �ͻ��˵�response
     * @return com.hx.blog.bean.Blog
     * @author Jerry.X.He
     * @date 4/11/2017 6:09 PM
     * @since 1.0
     */
    private Blog doGetBlogInfo(Blog blogInServer, HttpServletRequest req, HttpServletResponse resp) {
        Blog blog = new Blog(blogInServer);
        Integer blogId = blogInServer.getId();

        String blogPath = Tools.getBlogPath(Tools.getProjectPath(), (blogInServer.getPath()));
//				Log.log(blogPath);
        if (!Tools.isFileExists(blogPath)) {
            blog.setContent("this blog already be deleted, maybe by adminstrator !");
        } else {
            if (!Constants.visitedCookieValue.equals(Tools.getCookieValByName(req.getCookies(), Tools.getVisitedCookieName(blog.getId())))) {
                BlogManager.addVisited(blogId);
                resp.addCookie(new Cookie(Tools.getVisitedCookieName(blog.getId()), Constants.visitedCookieValue));
            }

            // add try-catch in case of disk get fileContent while admin is revise blog !		2015.12.10
            String content = null;
            try {
                content = Tools.getContent(blogPath, Constants.DEFAULT_CHARSET);
            } catch (Exception e) {
                content = "file maybe be locked now, please try again later !";
            }
            blog.setContent(content);
        }

        return blog;
    }

    /**
     * ������������Ʒ��, �޸�, ɾ����ť, ��һƪ, ��һƪ������Ϣ
     *
     * @param res          ��Ҫ���ظ��ͻ��˵Ľ������
     * @param req          �ͻ��˵�����
     * @param blog         ��ǰ������Ϣ
     * @param tag          ��ǰ��ǩ��Ϣ[�п����Ǵ�ĳ��tag�б�������ת������]
     * @param isLogin      ��ǰ�û��Ƿ��¼
     * @param injectorInfo ע�����Ϣ
     * @return void
     * @author Jerry.X.He
     * @date 4/11/2017 6:05 PM
     * @since 1.0
     */
    private void putOtherInfo(JSONObject res, HttpServletRequest req, Blog blog, String tag, boolean isLogin, JSONObject injectorInfo) {
        Integer blogId = blog.getId();
        res.element("sense", Tools.getCookieValByName(req.getCookies(), Tools.getSensedCookieName(blogId)));
        List<List<Comment>> blogComments = CommentManager.getCommentByBlogId(blogId);
        if (blogComments != null) {
            res.element("comments", Tools.encapBlogComments(blogComments));
        }
        Tools.addIfNotEmpty(res, "user", Tools.getStrFromSession(req, Constants.preferInfo));
        if (isLogin) {
            res.element("reviseBtn", String.format(Constants.reviseBtn, blog.getId()));
            res.element("deleteBtn", Constants.deleteBtn);
        }
        if (!Tools.isEmpty(tag)) {
            int curBlogIdx = BlogManager.getBlogIdxByIdAndTag(blogId, tag);
            if (curBlogIdx == Constants.HAVE_NO_THIS_TAG) {
                injectorInfo.element("tag", tag).element("ip", Tools.getIPAddr(req));
            } else {
                int prevBlogId = BlogManager.getBlogIdByIdxAndTag(curBlogIdx + 1, tag);
                if (prevBlogId != Constants.HAVE_NO_PREV_IDX) {
                    res.element("prevBlogId", prevBlogId);
                }
                int nextBlogId = BlogManager.getBlogIdByIdxAndTag(curBlogIdx - 1, tag);
                if (nextBlogId != Constants.HAVE_NO_NEXT_IDX) {
                    res.element("nextBlogId", nextBlogId);
                }
            }
        }
    }

}
