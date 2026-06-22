package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Collect;
import com.example.entity.Items;
import com.example.exception.CustomException;
import com.example.mapper.CollectMapper;
import com.example.mapper.ItemsMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemsService {
    private static final String STATUS_PENDING = "\u5f85\u5ba1\u6838";
    private static final String STATUS_APPROVED = "\u901a\u8fc7";

    @Resource
    private ItemsMapper itemsMapper;
    @Resource
    private CollectMapper collectMapper;

    public void add(Items items) {
        items.setTime(DateUtil.now());
        items.setStatus(false);
        items.setCheckStatus(STATUS_PENDING);
        if (items.getReason() == null) {
            items.setReason("");
        }
        itemsMapper.insert(items);
    }

    public void deleteById(Integer id) {
        itemsMapper.deleteById(id);
    }

    // Generic update used by admin approval and internal workflow.
    public void updateById(Items items) {
        itemsMapper.updateById(items);
    }

    // User edits must go back through admin review.
    public void updateByUser(Items items) {
        items.setStatus(false);
        items.setCheckStatus(STATUS_PENDING);
        items.setReason("");
        itemsMapper.updateById(items);
    }

    public void updateStatus(Items items) {
        Items dbItem = itemsMapper.selectById(items.getId());
        if (dbItem == null) {
            throw new CustomException("\u7269\u54c1\u4e0d\u5b58\u5728");
        }
        if (Boolean.TRUE.equals(items.getStatus()) && !STATUS_APPROVED.equals(dbItem.getCheckStatus())) {
            throw new CustomException("\u7269\u54c1\u5c1a\u672a\u901a\u8fc7\u5ba1\u6838\uff0c\u4e0d\u80fd\u4e0a\u67b6");
        }
        Items update = new Items();
        update.setId(items.getId());
        update.setStatus(items.getStatus());
        itemsMapper.updateById(update);
    }

    public Items selectById(Integer id) {
        return itemsMapper.selectById(id);
    }

    public List<Items> selectAll(Items items) {
        return itemsMapper.selectAll(items);
    }

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
