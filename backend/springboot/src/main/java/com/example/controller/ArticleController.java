package com.example.controller;

import com.example.common.Result;
import com.example.entity.Article;
import com.example.service.ArticleService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @PostMapping("/add")
    public Result add(@RequestBody Article article) {
        articleService.add(article);
        return Result.success(article);
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        articleService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody Article article) {
        articleService.updateById(article);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id,
                             @RequestParam(required = false) Integer loginUserId) {
        return Result.success(articleService.selectById(id, loginUserId));
    }

    @GetMapping("/selectAll")
    public Result selectAll(Article article) {
        return Result.success(articleService.selectAll(article));
    }

    @GetMapping("/selectPage")
    public Result selectPage(Article article,
                             @RequestParam(required = false) Integer loginUserId,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        article.setLoginUserId(loginUserId);
        PageInfo<Article> page = articleService.selectPage(article, pageNum, pageSize);
        return Result.success(page);
    }
}
