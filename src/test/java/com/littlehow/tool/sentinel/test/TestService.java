package com.littlehow.tool.sentinel.test;

import com.littlehow.tool.sentinel.annotation.Degrade;
import com.littlehow.tool.sentinel.annotation.Fallback;
import com.littlehow.tool.sentinel.annotation.Flow;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TestService implements IService {
    private static AtomicInteger integer = new AtomicInteger();

    @Degrade
    @Fallback(FallbackService.class)
    public String getHello(String name, Integer age) {
        if (integer.getAndIncrement() % 2 == 0) {
            throw new IllegalArgumentException("这是自己定义的异常哦");
        }
        return "hello " + name + ", 已经" + age + "岁了";
    }

    @Flow(count = 10)
    public String getUUID() {
        return UUID.randomUUID().toString();
    }
}
