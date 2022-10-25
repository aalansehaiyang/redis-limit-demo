package com.onyone.lock.controller;

import com.google.common.collect.ImmutableList;
import com.onyone.lock.configuration.RedisScriptConfig;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author Tom哥
 */
@RestController
public class LockController {

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;


    /**
     * 加锁
     */
    @RequestMapping(value = "/lock_case_1")
    public String lock_case_1() {

        String lockKey = "lock_case_1";
        String value = Thread.currentThread().getName();

        Boolean lockResult = redisTemplate.opsForValue().setIfAbsent(lockKey, value, 20, TimeUnit.SECONDS);

        System.out.println("加锁结果：" + lockResult);
        return lockResult + "";
    }


    /**
     * 释放锁
     */
    @RequestMapping(value = "/unlock_case_1")
    public String unlock_case_1() {
        String lockKey = "lock_case_1";

        Boolean lockResult = redisTemplate.delete(lockKey);
        System.out.println("释放锁结果：" + lockResult);
        return lockResult + "";
    }

    /**
     * 加锁、释放锁
     * 限制只能释放本线程加的锁
     */
    @SneakyThrows
    @RequestMapping(value = "/lock_case_2")
    public String lock_case_2() {

        String lockKey = "lock_case_2";
        String value = Thread.currentThread().getName();
        // 加锁
        Boolean lockResult = redisTemplate.opsForValue().setIfAbsent(lockKey, value, 20, TimeUnit.SECONDS);
        System.out.println("加锁结果：" + lockResult);

        // 模拟业务处理，休眠一段时间
        Thread.sleep(5000L);

        // 释放锁
        boolean unlockResult = false;
        ImmutableList<String> keys = ImmutableList.of(StringUtils.join(lockKey));
        Number count = redisTemplate.execute(RedisScriptConfig.getRedisScript(), keys, value);
        if (count != null && count.intValue() == 1) {
            unlockResult = true;
        }

        System.out.println("释放锁结果：" + unlockResult);
        return "Success";
    }


    /**
     * 加锁，具有可重入性
     */
    @SneakyThrows
    @RequestMapping(value = "/reentrant_lock_case_3")
    public String reentrant_lock_case_3() {
        String lockKey = "reentrant_lock_case_3";
        String value = Thread.currentThread().getName();
        ImmutableList<String> keys = ImmutableList.of(lockKey, lockKey + "_count");

        //  模拟当前线程 4次 加锁
        for (int i = 1; i <= 4; i++) {
            boolean lockResult = false;
            Number count = redisTemplate.execute(RedisScriptConfig.getReentrantScript(), keys, value, 30);
            if (count != null && count.intValue() == 1) {
                lockResult = true;
            }
            System.out.println("加锁结果：" + lockResult);

        }
        // 模拟业务处理，休眠一段时间
        Thread.sleep(5000L);

        //  模拟当前线程 4次 释放锁
        for (int i = 1; i <= 4; i++) {
            boolean unLockResult = false;
            Number unCount = redisTemplate.execute(RedisScriptConfig.getReentrantUnLockScript(), keys, value);
            if (unCount != null && unCount.intValue() == 1) {
                unLockResult = true;
            }
            System.out.println("释放锁结果：" + unLockResult);

        }

        return "Success";
    }


    /**
     * 非阻塞式获取锁
     */
    public boolean tryLock(List<String> keys, String value) throws Exception {

        boolean isLocked = false;
        Number count = redisTemplate.execute(RedisScriptConfig.getReentrantUnLockScript(), keys, value, 30);
        if (count != null && count.intValue() == 1) {
            isLocked = true;
        }
        if (!isLocked) {
            // 为了避免无限循环，我们可以设定重试次数
            for (; ; ) {
                count = redisTemplate.execute(RedisScriptConfig.getReentrantUnLockScript(), keys, value, 30);
                if (count != null && count.intValue() == 1) {
                    isLocked = true;
                    return isLocked;
                }
            }
        }
        return isLocked;
    }

}