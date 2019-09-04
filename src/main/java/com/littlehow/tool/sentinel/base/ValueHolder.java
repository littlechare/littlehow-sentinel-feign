package com.littlehow.tool.sentinel.base;

import lombok.Data;

/**
 * 单值处理
 * 线程不安全
 */
@Data
public class ValueHolder<T> {
    private T value;
}
