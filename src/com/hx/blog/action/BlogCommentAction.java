package com.hx.blog.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.hx.blog.bean.Blog;
import com.hx.blog.bean.Comment;
import com.hx.blog.bean.ResponseMsg;
import com.hx.blog.bean.UserInfo;
import com.hx.blog.bean.ValidateResult;
import com.hx.blog.business.BlogManager;
import com.hx.blog.business.CommentManager;
import com.hx.blog.interf.BaseAction;
import com.hx.blog.interf.Validater;
import com.hx.blog.util.Constants;
import com.hx.blog.util.Tools;

public class BlogCommentAction extends BaseAction {

    // �����������ҵ��
    // ��ȡblogIdx, floorIdx, imageIdx  ��������쳣, ��imageIdx�ÿ� [��Ȼ������У��]
    // У��blogIdx, floorIdx, imageIdx, userInfo, commentBody
    // ��У����������Ƿ����
    // У��ͨ����, �����û���userInfo
    // ���comment���µ�¥����Ϣ, ��ȷ������¥����[index]
    // ����  ���ǻظ�, ȷ�����ڸ�¥��Ļظ���[index]
    // ��comment��Ϣ��ӵ�CommentManager��
    // ���� ��Ӧ���, ��¼��־
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding(Constants.DEFAULT_CHARSET);
        resp.setHeader("Content-Type", "text/html;charset=" + Constants.DEFAULT_CHARSET);

        ResponseMsg respMsg = new ResponseMsg();
        Integer blogIdx = Integer.parseInt((String) req.getAttribute(Constants.blogIdx));
        Integer floorIdx = Integer.parseInt((String) req.getAttribute(Constants.floorIdx));
        Integer imageIdx = Integer.parseInt((String) req.getAttribute(Constants.imageIdx));
        String to = (String) req.getAttribute(Constants.to);
        if (Tools.isEmpty(to)) {
            to = Constants.defaultTo;
        }
        String commentBody = (String) req.getAttribute(Constants.comment);
        Comment comment = null;

        UserInfo userInfo = (UserInfo) req.getAttribute(Constants.userInfo);
        req.getSession().setAttribute(Constants.preferInfo, userInfo);
        comment = new Comment(blogIdx, floorIdx, Constants.defaultCommentIdx, userInfo, Constants.createDateFormat.format(new Date()), to, commentBody);
        try {
            addComment(blogIdx, comment, commentBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
        respMsg.set(Constants.respSucc, Constants.defaultResponseCode, "comment success !", Tools.getIPAddr(req));

        JSONObject res = new JSONObject();
        res.element(Constants.respMsg, respMsg.toString());
        res.element(Constants.comment, String.valueOf(comment));
        req.setAttribute(Constants.result, res);

    }

    /**
     * �־û�����
     *
     * @param blogIdx     ��Ӧ�Ĳ��͵�id
     * @param comment     ��װ�����۶���
     * @param commentBody �û����۵�����
     * @return void
     * @author Jerry.X.He
     * @date 4/11/2017 6:00 PM
     * @since 1.0
     */
    private void addComment(Integer blogIdx, Comment comment, String commentBody) throws Exception {
        CommentManager.addComment(blogIdx, comment);

        if (!Tools.isReply(commentBody)) {
            CommentManager.updateFloorIdx(comment);
            BlogManager.addComment(blogIdx);
        } else {
            comment.setComment(Tools.getReplyComment(commentBody));
            CommentManager.updateCommentIdx(comment);
        }
    }

}
