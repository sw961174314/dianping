package com.java.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.dto.LoginFormDTO;
import com.java.dto.Result;
import com.java.entity.User;
import com.java.mapper.UserMapper;
import com.java.service.IUserService;
import com.java.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.java.utils.SystemConstants.USER_NICK_NAME_PREFIX;

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

    /**
     * 登录
     * @param loginForm
     * @param session
     * @return
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1.校验手机号
        String phone = loginForm.getPhone();
        boolean phoneInvalid = RegexUtils.isPhoneInvalid(phone);
        if (phoneInvalid) {
            return Result.fail("手机号格式错误");
        }
        // 2.校验验证码
        String cacheCode = (String) session.getAttribute("code");
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 3.不一致，报错
            return Result.fail("验证码错误");
        }
        // 4.一致，根据手机号查询用户
        User user = query().eq("phone", phone).one();
        // 5.判断用户是否存在
        if (user == null) {
            // 6.不存在，创建新用户并保存
            createUserWithPhone(phone);
        }
        // 7.保存用户信息到session中
        session.setAttribute("user", user);
        return Result.ok();
    }

    /**
     * 创建用户
     * @param phone
     * @return
     */
    private User createUserWithPhone(String phone) {
        // 1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 2.保存用户
        save(user);
        return user;
    }
}
