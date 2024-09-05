package com.java.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.dto.Result;
import com.java.entity.User;
import com.java.mapper.UserMapper;
import com.java.service.IUserService;
import com.java.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * 服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    /**
     * 发送验证码
     * @param phone
     * @param session
     * @return
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.校验手机号
        boolean phoneInvalid = RegexUtils.isPhoneInvalid(phone);
        // 2.如果不符合 返回错误信息
        if (phoneInvalid) {
            return Result.fail("手机号格式错误");
        }
        // 3.符合 生成验证码
        String code = RandomUtil.randomString(6);
        // 4.保存验证码到session
        session.setAttribute("code", code);
        // todo 5.发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);
        // 返回
        return Result.ok();
    }
}
