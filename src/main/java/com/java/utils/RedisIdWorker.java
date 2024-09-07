package com.java.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 全局唯一ID工具类
 */
@Component
public class RedisIdWorker {

    // 开始时间戳
    private static final long BEGIN_TIMESTAMP = 1704067200L;

    // 序列号长度
    private static final int COUNT_BITS = 32;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 由时间戳+序列号组成id
     * @param keyprefix
     * @return
     */
    public long nextId(String keyprefix) {
        // 1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        // 2.生成序列号
        // 2.1.获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2.自增长
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyprefix + ":" + date);
        // 3.拼接并返回
        return timestamp << COUNT_BITS | count;
    }
}