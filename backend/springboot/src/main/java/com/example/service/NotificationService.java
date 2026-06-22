package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Notification;
import com.example.mapper.NotificationMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Resource
    private NotificationMapper notificationMapper;

    public void addInteraction(Integer userId, Integer actorId, String type, String targetType, Integer targetId, String content) {
        if (userId == null || actorId == null || userId.equals(actorId)) {
            return;
        }
        notificationMapper.insertInteraction(userId, actorId, type, targetType, targetId, content, DateUtil.now());
    }

    public PageInfo<Notification> selectPage(Notification notification, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Notification> list = notificationMapper.selectAll(notification);
        return PageInfo.of(list);
    }

    public List<Notification> selectAll(Notification notification) {
        return notificationMapper.selectAll(notification);
    }

    public void markRead(Integer id) {
        notificationMapper.markRead(id);
    }

    public void markAllRead(Integer userId) {
        notificationMapper.markAllRead(userId);
    }
}
