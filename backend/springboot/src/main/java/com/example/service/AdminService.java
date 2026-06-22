package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomException;
import com.example.mapper.AdminMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    private static final String ROLE_ADMIN = "\u7ba1\u7406\u5458";

    @Resource
    private AdminMapper adminMapper;

    public void add(Admin admin) {
        Admin dbAdmin = adminMapper.selectByUsername(admin.getUsername());
        if (ObjectUtil.isNotNull(dbAdmin)) {
            throw new CustomException("\u7528\u6237\u5df2\u5b58\u5728");
        }
        if (ObjectUtil.isEmpty(admin.getPassword())) {
            admin.setPassword("admin");
        }
        if (ObjectUtil.isEmpty(admin.getName())) {
            admin.setName(admin.getUsername());
        }
        admin.setRole(ROLE_ADMIN);
        adminMapper.insert(admin);
    }

    public void deleteById(Integer id) { adminMapper.deleteById(id); }
    public void updateById(Admin admin) { adminMapper.updateById(admin); }
    public Admin selectById(Integer id) { return adminMapper.selectById(id); }
    public List<Admin> selectAll(Admin admin) { return adminMapper.selectAll(admin); }

    public PageInfo<Admin> selectPage(Admin admin, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(adminMapper.selectAll(admin));
    }

    public Account login(Account account) {
        Account dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException("\u7528\u6237\u4e0d\u5b58\u5728");
        }
        if (!account.getPassword().equals(dbAdmin.getPassword())) {
            throw new CustomException("\u8d26\u53f7\u6216\u5bc6\u7801\u9519\u8bef");
        }
        return dbAdmin;
    }

    public void updatePassword(Account account) {
        Admin dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException("\u7528\u6237\u4e0d\u5b58\u5728");
        }
        if (!account.getPassword().equals(dbAdmin.getPassword())) {
            throw new CustomException("\u539f\u5bc6\u7801\u9519\u8bef");
        }
        dbAdmin.setPassword(account.getNewPassword());
        adminMapper.updateById(dbAdmin);
    }
}
