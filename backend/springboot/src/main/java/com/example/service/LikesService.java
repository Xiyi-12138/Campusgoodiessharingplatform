package com.example.service;

import com.example.entity.Article;
import com.example.entity.Likes;
import com.example.entity.User;
import com.example.mapper.ArticleMapper;
import com.example.mapper.LikesMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikesService {

    @Resource
    private LikesMapper likesMapper;
    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private NotificationService notificationService;

    public void add(Likes likes) {
        Likes old = likesMapper.selectByArticleIdAndUserId(likes.getArticleId(), likes.getUserId());
        if (old != null) {
            likes.setId(old.getId());
            return;
        }
        likesMapper.insert(likes);
        Article article = articleMapper.selectById(likes.getArticleId(), likes.getUserId());
        User actor = userMapper.selectById(likes.getUserId());
        String name = actor == null ? "有人" : actor.getName();
        if (article != null) {
            notificationService.addInteraction(article.getUserId(), likes.getUserId(), "like", "article", article.getId(), name + "点赞了你的帖子《" + article.getTitle() + "》");
        }
    }

    public void deleteById(Integer id) { likesMapper.deleteById(id); }
    public void updateById(Likes likes) { likesMapper.updateById(likes); }
    public Likes selectById(Integer id) { return likesMapper.selectById(id); }
    public List<Likes> selectAll(Likes likes) { return likesMapper.selectAll(likes); }
    public PageInfo<Likes> selectPage(Likes likes, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(likesMapper.selectAll(likes));
    }
}
