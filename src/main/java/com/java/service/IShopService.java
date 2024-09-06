package com.java.service;

import com.java.dto.Result;
import com.java.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 服务类
 */
public interface IShopService extends IService<Shop> {

    Result queryById(Long id);

    Result update(Shop shop);
}
