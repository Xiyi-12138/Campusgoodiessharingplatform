package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Charge;
import com.example.entity.Items;
import com.example.mapper.ChargeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 业务处理
 **/
@Service
public class ChargeService {

    @Resource
    private ChargeMapper chargeMapper;
    @Resource
    ItemsService itemsService;

    /**
     * 新增
     */
    public void add(Charge charge) {
        charge.setTime(DateUtil.now());
        charge.setStatus("待审核");
        chargeMapper.insert(charge);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        chargeMapper.deleteById(id);
    }

    /**
     * 修改
     */
    @Transactional
    public void updateById(Charge charge) {
        if ("通过".equals(charge.getStatus())) {
            Items items = itemsService.selectById(charge.getItemId());
            items.setStatus(false);
            itemsService.updateById(items);
        }
        chargeMapper.updateById(charge);
    }

    /**
     * 根据ID查询
     */
    public Charge selectById(Integer id) {
        return chargeMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Charge> selectAll(Charge charge) {
        return chargeMapper.selectAll(charge);
    }

    /**
     * 分页查询
     */
    public PageInfo<Charge> selectPage(Charge charge, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Charge> list = chargeMapper.selectAll(charge);
        return PageInfo.of(list);
    }

}