package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Article;
import com.example.entity.Comments;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.ArticleMapper;
import com.example.mapper.CommentsMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentsService {

    @Resource
    private CommentsMapper commentsMapper;
    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private NotificationService notificationService;

    public void add(Comments comments) {
        if (comments.getContent() == null || comments.getContent().trim().length() < 2) {
            throw new CustomException("\u8bc4\u8bba\u81f3\u5c11\u9700\u89812\u4e2a\u5b57");
        }
        comments.setTime(DateUtil.now());
        commentsMapper.insert(comments);
        Article article = articleMapper.selectById(comments.getArticleId(), comments.getUserId());
        User actor = userMapper.selectById(comments.getUserId());
        String name = actor == null ? "有人" : actor.getName();
        if (article != null) {
            notificationService.addInteraction(article.getUserId(), comments.getUserId(), "comment", "article", article.getId(), name + "评论了你的帖子《" + article.getTitle() + "》");
        }
    }

    public void deleteById(Integer id) { commentsMapper.deleteById(id); }
    public void updateById(Comments comments) { commentsMapper.updateById(comments); }
    public Comments selectById(Integer id) { return commentsMapper.selectById(id); }
    public List<Comments> selectAll(Comments comments) { return commentsMapper.selectAll(comments); }
    public PageInfo<Comments> selectPage(Comments comments, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(commentsMapper.selectAll(comments));
    }
}
