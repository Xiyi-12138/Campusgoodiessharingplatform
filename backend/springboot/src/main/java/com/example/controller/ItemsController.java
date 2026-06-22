package com.example.controller;

import com.example.common.Result;
import com.example.entity.Items;
import com.example.service.ItemsService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
public class ItemsController {

    @Resource
    private ItemsService itemsService;

    @PostMapping("/add")
    public Result add(@RequestBody Items items) {
        itemsService.add(items);
        return Result.success(items);
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        itemsService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/updateStatus")
    public Result updateStatus(@RequestBody Items items) {
        itemsService.updateById(items);
        return Result.success();
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody Items items) {
        itemsService.updateById(items);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        return Result.success(itemsService.selectById(id));
    }

    @GetMapping("/selectAll")
    public Result selectAll(Items items) {
        return Result.success(itemsService.selectAll(items));
    }

    @GetMapping("/selectPage")
    public Result selectPage(Items items,
                             @RequestParam(required = false) Integer loginUserId,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Items> page = itemsService.selectPage(items, loginUserId, pageNum, pageSize);
        return Result.success(page);
    }
}
