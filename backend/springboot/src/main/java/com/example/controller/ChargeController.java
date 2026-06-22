package com.example.controller;

import com.example.common.Result;
import com.example.entity.Charge;
import com.example.service.ChargeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员前端操作接口
 **/
@RestController
@RequestMapping("/charge")
public class ChargeController {

    @Resource
    private ChargeService chargeService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Charge charge) {
        chargeService.add(charge);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        chargeService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Charge charge) {
        chargeService.updateById(charge);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Charge charge = chargeService.selectById(id);
        return Result.success(charge);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Charge charge) {
        List<Charge> list = chargeService.selectAll(charge);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Charge charge,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Charge> page = chargeService.selectPage(charge, pageNum, pageSize);
        return Result.success(page);
    }

}