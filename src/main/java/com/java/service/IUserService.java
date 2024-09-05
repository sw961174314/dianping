package com.java.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.dto.Result;
import com.java.entity.User;

import javax.servlet.http.HttpSession;

/**
 * 服务类
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);
}
