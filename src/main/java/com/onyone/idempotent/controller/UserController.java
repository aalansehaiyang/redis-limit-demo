package com.onyone.idempotent.controller;

import com.onyone.idempotent.annotation.IdempotentRule;
import com.onyone.idempotent.param.UserParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/user")
public class UserController {


    /**
     * 创建一个新的用户
     */
    @RequestMapping(value = "/create_user")
    @IdempotentRule(key = "#userParam.cardNumber", prefix = "repeat_")
    public String createUser(@RequestBody UserParam userParam) {

        // 模拟业务处理

        return "创建用户成功！";
    }
}
