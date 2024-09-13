package com.java.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.java.dto.Result;
import com.java.entity.Shop;
import com.java.mapper.ShopMapper;
import com.java.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.java.utils.CacheClient;
import com.java.utils.RedisData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.java.utils.RedisConstants.*;

/**
 * 服务实现类
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    // 创建线程池
    public static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CacheClient cacheClient;

    /**
     * 根据id查询商铺信息
     * @param id
     * @return
     */
    @Override
    public Result queryById(Long id) {
        // 1.缓存穿透
         Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        // 2.利用互斥锁解决缓存击穿
        // Shop shop = queryWithMutex(id);
        // if (shop == null) {
        //     return Result.fail("店铺不存在");
        // }
        // 3.利用过期时间解决缓存击穿(前提是要先进行数据预热，即需要先对热点数据进行存储，否则无法查询数据)
//        Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        // 7.返回
        return Result.ok(shop);
    }

    /**
     * 缓存穿透
     * @param id
     * @return
     */
    public Shop queryWithPassThrough(Long id) {
        String key = CACHE_SHOP_KEY + id;
        // 1.从Redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 3.存在，直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 判断命中的是否是空值
        if (shopJson != null) {
            return null;
        }
        // 4.不存在，根据id查询数据库
        Shop shop = getById(id);
        // 5.不存在，返回错误
        if (shop == null) {
            // 将空值写入到Redis中
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            // 返回错误信息
            return null;
        }
        // 6.存在，写入Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        // 7.返回
        return shop;
    }

    /**
     * 利用互斥锁解决缓存击穿
     * @param id
     * @return
     */
//    public Shop queryWithMutex(Long id) {
//        String key = CACHE_SHOP_KEY + id;
//        // 1.从Redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        // 2.判断是否存在
//        if (StrUtil.isNotBlank(shopJson)) {
//            // 3.存在，直接返回
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//        // 判断命中的是否是空值
//        if (shopJson != null) {
//            return null;
//        }
//        // 4.实现缓存重建
//        // 4.1.获取互斥锁
//        String lockKey = LOCK_SHOP_KEY + id;
//        Shop shop = null;
//        try {
//            boolean isLock = tryLock(lockKey);
//            // 4.2.判断是否获取成功
//            if (!isLock) {
//                // 4.3.失败，则休眠并重试
//                Thread.sleep(50);
//                return queryWithMutex(id);
//            }
//            // 4.4.成功，根据id查询数据库
//            shop = getById(id);
//            // 模拟重建延时
//            Thread.sleep(200);
//            // 5.不存在，返回错误
//            if (shop == null) {
//                // 将空值写入到Redis中
//                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
//                // 返回错误信息
//                return null;
//            }
//            // 6.存在，写入Redis
//            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }finally {
//            // 7.释放互斥锁
//            unlock(lockKey);
//        }
//        // 8.返回
//        return shop;
//    }

    /**
     * 利用过期时间解决缓存击穿
     * @param id
     * @return
     */
//    public Shop queryWithLogicalExpire(Long id) {
//        String key = CACHE_SHOP_KEY + id;
//        // 1.从Redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        // 2.判断是否存在
//        if (StrUtil.isBlank(shopJson)) {
//            // 3.不存在，直接返回
//            return null;
//        }
//        // 4.存在，需要先把json反序列化为对象
//        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
//        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
//        LocalDateTime expireTime = redisData.getExpireTime();
//        // 5.判断是否过期
//        if (expireTime.isAfter(LocalDateTime.now())) {
//            // 5.1.未过期，直接返回店铺信息
//            return shop;
//        }
//        // 5.2.已过期，需要缓存重建
//        // 6.缓存重建
//        // 6.1.获取互斥锁
//        String lockKey = LOCK_SHOP_KEY + id;
//        boolean isLock = tryLock(lockKey);
//        // 6.2.判断是否获取锁成功
//        if (isLock) {
//            // 6.3.成功，开启独立线程，实现缓存创建
//            CACHE_REBUILD_EXECUTOR.submit(() -> {
//                try {
//                    // 重建缓存
//                    saveShop2Redis(id, 30L);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    // 释放锁
//                    unlock(lockKey);
//                }
//            });
//        }
//        // 6.4.返回商铺信息
//        return shop;
//    }

    /**
     * 更新商铺信息
     * @param shop
     * @return
     */
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        // 1.更新数据库
        updateById(shop);
        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    /**
     * 将商户经纬度导入Redis
     * @return
     */
    @Override
    public Result loadShopData() {
        // 1.查询店铺信息
        List<Shop> list = list();
        // 2.把店铺分组 按照typeId分组 id一致放到一个集合中
        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        // 3.分批存储
        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
            // 3.1.获取类型id
            Long typeId = entry.getKey();
            String key = SHOP_GEO_KEY + typeId;
            // 3.2.获取同类型店铺的id
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
            // 3.3.写入Redis GEOADD key 经度 纬度 member
            for (Shop shop : value) {
                // 单个导入
                // stringRedisTemplate.opsForGeo().add(key, new Point(shop.getX(), shop.getY()), shop.getId().toString());
                // 批量导入
                locations.add(new RedisGeoCommands.GeoLocation<>(shop.getId().toString(), new Point(shop.getX(), shop.getY())));
            }
            stringRedisTemplate.opsForGeo().add(key, locations);
        }
        return Result.ok();
    }
}
