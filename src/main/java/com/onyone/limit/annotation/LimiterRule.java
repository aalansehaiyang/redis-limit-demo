package com.onyone.limit.annotation;

import java.lang.annotation.*;


@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LimiterRule {

    String key();

    // 限流上限阈值
    int maxCount();

    // 过期时间
    int limitPeriod();

}
