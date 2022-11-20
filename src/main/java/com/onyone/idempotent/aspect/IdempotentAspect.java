package com.onyone.idempotent.aspect;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.google.common.collect.ImmutableList;
import com.onyone.idempotent.annotation.IdempotentRule;
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
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;


@Aspect
@Component
@Slf4j
public class IdempotentAspect {

    @Autowired
    private RedisTemplate<String, Serializable> idempotentRedisTemplate;


    @Around("execution(public * *(..)) && @annotation(com.onyone.idempotent.annotation.IdempotentRule)")
    public Object limit(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();


        Object[] params = pjp.getArgs();
        String[] paramNames = signature.getParameterNames();

        Method method = signature.getMethod();
        IdempotentRule idempotentRule = method.getAnnotation(IdempotentRule.class);
        String key = idempotentRule.key();
        String prefix = idempotentRule.prefix();

        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable(paramNames[0], params[0]);
        String repeatKey = (String) parser.parseExpression(key).getValue(context);

        try {
            // 先在缓存中做个标记
            Boolean lockResult = idempotentRedisTemplate.opsForValue().setIfAbsent(prefix + repeatKey, "正在处理....", 10, TimeUnit.SECONDS);
            if (lockResult) {
                // 业务逻辑处理
                return pjp.proceed();
            } else {
                throw new Exception("重复提交..................");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            // 处理完成后，将标记删除
            idempotentRedisTemplate.delete(prefix + repeatKey);
        }

        return null;

    }


}
