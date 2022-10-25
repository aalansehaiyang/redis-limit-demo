package com.onyone.lock.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author Tom哥
 */

@Slf4j
@Component
public class RedisScriptConfig {

    private static final String LOCK_LUA_PATH = "";

    private static DefaultRedisScript<Number> redisScript;

    private static DefaultRedisScript<Number> reentrantScript;

    private static DefaultRedisScript<Number> reentrantUnLockScript;


    @PostConstruct
    public void init() {

        redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Number.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("unlock1.lua")));

        reentrantScript = new DefaultRedisScript<>();
        reentrantScript.setResultType(Number.class);
        reentrantScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("reentrant_lock_3.lua")));

        reentrantUnLockScript = new DefaultRedisScript<>();
        reentrantUnLockScript.setResultType(Number.class);
        reentrantUnLockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("reentrant_unlock_3.lua")));
    }

    public static DefaultRedisScript<Number> getRedisScript() {
        return redisScript;
    }

    public static DefaultRedisScript<Number> getReentrantScript() {
        return reentrantScript;
    }

    public static DefaultRedisScript<Number> getReentrantUnLockScript() {
        return reentrantUnLockScript;
    }
}
