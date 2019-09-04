package com.littlehow.tool.sentinel.test;

import com.littlehow.tool.sentinel.exception.SentinelException;

import java.util.concurrent.atomic.AtomicInteger;

public class FallbackService {
    private static AtomicInteger integer = new AtomicInteger();
    public String getHello(String name, Integer age, Throwable t) {
        if (integer.getAndIncrement() % 2 == 0) {
            throw new SentinelException("降级fallback处理", "888888");
        }
        return "fallback hello " + name + ",  t = " + t.getClass().getName();
    }
}
