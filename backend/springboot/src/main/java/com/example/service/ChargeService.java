package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Charge;
import com.example.entity.Items;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.ChargeMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChargeService {
    private static final String STATUS_PENDING = "\u5f85\u5ba1\u6838";
    private static final String STATUS_APPROVED = "\u901a\u8fc7";
    private static final String STATUS_REJECTED = "\u62d2\u7edd";

    @Resource
    private ChargeMapper chargeMapper;
    @Resource
    private ItemsService itemsService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private NotificationService notificationService;

    public void add(Charge charge) {
        Items item = itemsService.selectById(charge.getItemId());
        if (item == null) {
            throw new CustomException("\u7269\u54c1\u4e0d\u5b58\u5728");
        }
        if (item.getUserId() != null && item.getUserId().equals(charge.getUserId())) {
            throw new CustomException("\u4e0d\u80fd\u7533\u8bf7\u4ea4\u6362\u81ea\u5df1\u7684\u7269\u54c1");
        }
        requireText(charge.getContent(), "\u8bf7\u586b\u5199\u4ea4\u6362\u7269\u54c1");
        requireText(charge.getRemark(), "\u8bf7\u586b\u5199\u4ea4\u6362\u7406\u7531");
        charge.setTime(DateUtil.now());
        charge.setStatus(STATUS_PENDING);
        charge.setItemUserid(item.getUserId());
        chargeMapper.insert(charge);

        User actor = userMapper.selectById(charge.getUserId());
        String actorName = actor == null ? "\u6709\u4eba" : actor.getName();
        notificationService.addInteraction(item.getUserId(), charge.getUserId(), "exchange", "charge", charge.getId(),
                actorName + "\u7533\u8bf7\u4ea4\u6362\u4f60\u7684\u7269\u54c1\u300a" + item.getName() + "\u300b");
    }

    public void deleteById(Integer id) {
        chargeMapper.deleteById(id);
    }

    @Transactional
    public void updateById(Charge charge) {
        Charge dbCharge = chargeMapper.selectById(charge.getId());
        if (dbCharge == null) {
            throw new CustomException("\u7533\u8bf7\u4e0d\u5b58\u5728");
        }
        String status = charge.getStatus();
        if (STATUS_APPROVED.equals(status)) {
            requireText(charge.getLocation(), "\u8bf7\u586b\u5199\u4ea4\u6362\u5730\u70b9");
            requireText(charge.getShareTime(), "\u8bf7\u586b\u5199\u4ea4\u6362\u65f6\u95f4");
            Items items = itemsService.selectById(dbCharge.getItemId());
            if (items != null) {
                Items update = new Items();
                update.setId(items.getId());
                update.setStatus(false);
                itemsService.updateById(update);
            }
        }
        if (STATUS_REJECTED.equals(status)) {
            requireText(charge.getReason(), "\u8bf7\u586b\u5199\u62d2\u7edd\u7406\u7531");
        }
        chargeMapper.updateById(charge);
        notifyApplicant(dbCharge, charge);
    }

    public Charge selectById(Integer id) {
        return chargeMapper.selectById(id);
    }

    public List<Charge> selectAll(Charge charge) {
        return chargeMapper.selectAll(charge);
    }

    public PageInfo<Charge> selectPage(Charge charge, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(chargeMapper.selectAll(charge));
    }

    private void notifyApplicant(Charge dbCharge, Charge update) {
        if (!STATUS_APPROVED.equals(update.getStatus()) && !STATUS_REJECTED.equals(update.getStatus())) {
            return;
        }
        String content = STATUS_APPROVED.equals(update.getStatus())
                ? "\u4f60\u7684\u4ea4\u6362\u7533\u8bf7\u5df2\u901a\u8fc7\uff0c\u8bf7\u6309\u7ea6\u5b9a\u65f6\u95f4\u5730\u70b9\u4ea4\u6362"
                : "\u4f60\u7684\u4ea4\u6362\u7533\u8bf7\u88ab\u62d2\u7edd\uff1a" + update.getReason();
        notificationService.addInteraction(dbCharge.getUserId(), dbCharge.getItemUserid(), "exchange_result", "charge", dbCharge.getId(), content);
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new CustomException(message);
        }
    }
}
