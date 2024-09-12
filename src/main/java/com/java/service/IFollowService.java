package com.java.service;

import com.java.dto.Result;
import com.java.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 服务类
 */
public interface IFollowService extends IService<Follow> {

    Result follow(Long followUserId, Boolean isFollow);

    Result isFollow(Long followUserId);

    Result followCommons(Long id);
}
