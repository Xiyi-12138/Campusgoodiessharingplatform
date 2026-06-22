package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Collect;
import com.example.entity.Items;
import com.example.mapper.CollectMapper;
import com.example.mapper.ItemsMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemsService {

    @Resource
    private ItemsMapper itemsMapper;
    @Resource
    private CollectMapper collectMapper;

    public void add(Items items) {
        items.setTime(DateUtil.now());
        items.setStatus(items.getStatus() == null ? true : items.getStatus());
        items.setCheckStatus("通过");
        if (items.getReason() == null) {
            items.setReason("");
        }
        itemsMapper.insert(items);
    }

    public void deleteById(Integer id) { itemsMapper.deleteById(id); }

    public void updateById(Items items) {
        items.setCheckStatus("通过");
        itemsMapper.updateById(items);
    }

    public Items selectById(Integer id) { return itemsMapper.selectById(id); }

    public List<Items> selectAll(Items items) { return itemsMapper.selectAll(items); }

    public PageInfo<Items> selectPage(Items items, Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Items> list = itemsMapper.selectAll(items);
        if (userId != null) {
            for (Items item : list) {
                Collect collect = collectMapper.selectByItemIdAndUserId(item.getId(), userId);
                if (collect != null) {
                    item.setCollectId(collect.getId());
                }
            }
        }
        return PageInfo.of(list);
    }
}
