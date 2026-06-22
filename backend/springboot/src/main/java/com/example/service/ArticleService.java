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

    @Resource
    private ArticleMapper articleMapper;

    public void add(Article article) {
        article.setTime(DateUtil.now());
        article.setStatus("通过");
        if (article.getReason() == null) {
            article.setReason("");
        }
        articleMapper.insert(article);
    }

    public void deleteById(Integer id) { articleMapper.deleteById(id); }

    public void updateById(Article article) {
        article.setStatus("通过");
        articleMapper.updateById(article);
    }

    public Article selectById(Integer id, Integer loginUserId) {
        return articleMapper.selectById(id, loginUserId);
    }

    public List<Article> selectAll(Article article) { return articleMapper.selectAll(article); }

    public PageInfo<Article> selectPage(Article article, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(articleMapper.selectAll(article));
    }
}
