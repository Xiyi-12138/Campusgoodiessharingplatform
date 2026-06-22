package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Article;
import com.example.mapper.ArticleMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleService {
    private static final String STATUS_PENDING = "\u5f85\u5ba1\u6838";

    @Resource
    private ArticleMapper articleMapper;

    public void add(Article article) {
        article.setTime(DateUtil.now());
        article.setStatus(STATUS_PENDING);
        if (article.getReason() == null) {
            article.setReason("");
        }
        articleMapper.insert(article);
    }

    public void deleteById(Integer id) {
        articleMapper.deleteById(id);
    }

    // Keep the status supplied by the caller. The web admin uses this for approval.
    public void updateById(Article article) {
        articleMapper.updateById(article);
    }

    public Article selectById(Integer id, Integer loginUserId) {
        return articleMapper.selectById(id, loginUserId);
    }

    public List<Article> selectAll(Article article) {
        return articleMapper.selectAll(article);
    }

    public PageInfo<Article> selectPage(Article article, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(articleMapper.selectAll(article));
    }
}
