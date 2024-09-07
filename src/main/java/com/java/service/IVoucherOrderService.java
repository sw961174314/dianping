package com.java.service;

import com.java.dto.Result;
import com.java.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 服务类
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);
}
