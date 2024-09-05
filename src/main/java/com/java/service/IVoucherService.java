package com.java.service;

import com.java.dto.Result;
import com.java.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 服务类
 */
public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
