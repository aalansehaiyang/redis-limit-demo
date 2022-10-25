package com.onyone.limit.aspect;

import com.google.common.collect.ImmutableList;
import com.onyone.limit.annotation.LimiterRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;


@Aspect
@Component
@Slf4j
public class RedisLimitAspect {
    private static final String LIMIT_LUA_PATH = "limit.lua";

    @Autowired
    private RedisTemplate<String, Serializable> limitRedisTemplate;
    private DefaultRedisScript<Number> redisScript;

    @PostConstruct
    public void init() {

        redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Number.class);
        ClassPathResource classPathResource = new ClassPathResource(LIMIT_LUA_PATH);
        try {

            classPathResource.getInputStream();//探测资源是否存在
            redisScript.setScriptSource(new ResourceScriptSource(classPathResource));
        } catch (IOException e) {
            log.error("未找到文件：{}", LIMIT_LUA_PATH);
        }
    }

    @Around("execution(public * *(..)) && @annotation(com.onyone.limit.annotation.LimiterRule)")
    public Object limit(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        LimiterRule limiterRule = method.getAnnotation(LimiterRule.class);

        String key = limiterRule.key();
        int maxCount = limiterRule.maxCount();
        int limitPeriod = limiterRule.limitPeriod();

        key = key + System.currentTimeMillis() / 1000;

        ImmutableList<String> keys = ImmutableList.of(StringUtils.join(key));
        try {
            Number count = limitRedisTemplate.execute(redisScript, keys, maxCount, limitPeriod);
            System.out.println("limit script result ，count =" + count);
            if (count != null && count.intValue() == 1) {
                return pjp.proceed();
            } else {
                throw new RuntimeException("被限流啦........");
            }
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw new RuntimeException(e.getLocalizedMessage());
            }
            throw new RuntimeException("服务器出现异常，请稍后再试");
        }

    }


}
