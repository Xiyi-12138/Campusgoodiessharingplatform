package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.service.AdminService;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebController {
    private static final String ROLE_ADMIN = "\u7ba1\u7406\u5458";
    private static final String ROLE_USER = "\u666e\u901a\u7528\u6237";

    @Resource
    private AdminService adminService;
    @Resource
    private UserService userService;

    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }

    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        if (ROLE_ADMIN.equals(account.getRole())) {
            return Result.success(adminService.login(account));
        }
        account.setRole(ROLE_USER);
        return Result.success(userService.login(account));
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.add(user);
        return Result.success();
    }

    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if (ROLE_ADMIN.equals(account.getRole())) {
            adminService.updatePassword(account);
        } else {
            userService.updatePassword(account);
        }
        return Result.success();
    }
}
