package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Collect;
import com.example.entity.Items;
import com.example.entity.User;
import com.example.mapper.CollectMapper;
import com.example.mapper.ItemsMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectService {

    @Resource
    private CollectMapper collectMapper;
    @Resource
    private ItemsMapper itemsMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private NotificationService notificationService;

    public void add(Collect collect) {
        Collect old = collectMapper.selectByItemIdAndUserId(collect.getItemId(), collect.getUserId());
        if (old != null) {
            collect.setId(old.getId());
            return;
        }
        collect.setTime(DateUtil.now());
        collectMapper.insert(collect);
        Items item = itemsMapper.selectById(collect.getItemId());
        User actor = userMapper.selectById(collect.getUserId());
        String name = actor == null ? "有人" : actor.getName();
        if (item != null) {
            notificationService.addInteraction(item.getUserId(), collect.getUserId(), "collect", "item", item.getId(), name + "收藏了你的物品《" + item.getName() + "》");
        }
    }

    public void deleteById(Integer id) { collectMapper.deleteById(id); }
    public void updateById(Collect collect) { collectMapper.updateById(collect); }
    public Collect selectById(Integer id) { return collectMapper.selectById(id); }
    public List<Collect> selectAll(Collect collect) { return collectMapper.selectAll(collect); }
    public PageInfo<Collect> selectPage(Collect collect, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(collectMapper.selectAll(collect));
    }
}
