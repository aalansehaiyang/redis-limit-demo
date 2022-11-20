package com.onyone.idempotent.annotation;

import java.lang.annotation.*;

/**
 * @Author Tom哥
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented

public @interface IdempotentRule {

    /**
     * 业务自定义前缀
     */
    String prefix() default "";

    /**
     * 业务重复标识
     */
    String key() default "";
}
