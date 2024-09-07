package com.java.service.impl;

import com.java.dto.Result;
import com.java.entity.SeckillVoucher;
import com.java.entity.VoucherOrder;
import com.java.mapper.VoucherOrderMapper;
import com.java.service.ISeckillVoucherService;
import com.java.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.utils.RedisIdWorker;
import com.java.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 服务实现类
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    /**
     * 优惠券秒杀
     * @param voucherId
     * @return
     */
    @Override
    @Transactional
    public Result seckillVoucher(Long voucherId) {
        // 1.查询优惠券
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        // 2.判断秒杀是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            // 还没开始
            return Result.fail("秒杀活动还没开始");
        }
        // 3.判断秒杀是否结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 已经结束
            return Result.fail("秒杀活动已经结束");
        }
        // 4.判断库存是否充足
        if (voucher.getStock() < 1) {
            // 库存不足
            return Result.fail("库存不足");
        }
        // 5.扣减库存(添加乐观锁 用库存来防止超卖)
        boolean success = seckillVoucherService.update().setSql("stock = stock - 1").eq("voucher_id", voucherId).gt("stock",0).update();
        if (!success) {
            // 扣减失败
            return Result.fail("库存不足");
        }
        // 6.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 6.1.订单ID
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        // 6.2.用户ID
        Long userId = UserHolder.getUser().getId();
        voucherOrder.setUserId(userId);
        // 6.3.优惠券ID
        voucherOrder.setVoucherId(voucherId);
        // 6.4.保存订单
        save(voucherOrder);
        // 7.返回订单id
        return Result.ok(orderId);
    }
}
