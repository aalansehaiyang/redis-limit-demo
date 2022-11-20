package com.onyone.limit.controller;

import com.onyone.limit.annotation.LimiterRule;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class OrderController {

    /**
     * 限流规则：5秒内允许通过4个请求
     */
    @LimiterRule(key = "create_order_service_", maxCount = 4, limitPeriod = 20)
    @RequestMapping(value = "/create_order")
    public String createOrder() {
        // 模拟创建订单的业务流程

        return "success";
    }


}
