package com.example.controller;

import com.example.common.Result;
import com.example.entity.Comments;
import com.example.service.CommentsService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员前端操作接口
 **/
@RestController
@RequestMapping("/comments")
public class CommentsController {

    @Resource
    private CommentsService commentsService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Comments comments) {
        commentsService.add(comments);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        commentsService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Comments comments) {
        commentsService.updateById(comments);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Comments comments = commentsService.selectById(id);
        return Result.success(comments);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Comments comments) {
        List<Comments> list = commentsService.selectAll(comments);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Comments comments,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Comments> page = commentsService.selectPage(comments, pageNum, pageSize);
        return Result.success(page);
    }

}