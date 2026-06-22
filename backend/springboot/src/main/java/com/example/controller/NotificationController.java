package com.example.controller;

import com.example.common.Result;
import com.example.entity.Notification;
import com.example.service.NotificationService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @GetMapping("/selectAll")
    public Result selectAll(Notification notification) {
        return Result.success(notificationService.selectAll(notification));
    }

    @GetMapping("/selectPage")
    public Result selectPage(Notification notification,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(notificationService.selectPage(notification, pageNum, pageSize));
    }

    @PutMapping("/read/{id}")
    public Result read(@PathVariable Integer id) {
        notificationService.markRead(id);
        return Result.success();
    }

    @PutMapping("/readAll")
    public Result readAll(@RequestParam Integer userId) {
        notificationService.markAllRead(userId);
        return Result.success();
    }
}
