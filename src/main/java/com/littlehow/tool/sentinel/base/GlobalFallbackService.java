package com.littlehow.tool.sentinel.base;


/**
 * 全局fallback处理服务
 */
public class GlobalFallbackService {
    /**
     * 默认实现
     * @see com.littlehow.tool.sentinel.feign.SentinelFeignAutoConfiguration#degradeFallback()
     */
    private static GlobalDegradeFallback degradeFallback;

    /**
     * 默认实现
     * @see com.littlehow.tool.sentinel.feign.SentinelFeignAutoConfiguration#flowFallback()
     */
    private static GlobalFlowFallback flowFallback;

    public static Object degradeFallback() {
        return degradeFallback.degradeFallback();
    }

    public static Object flowFallback() {
        return flowFallback.flowFallback();
    }

    public static void setDegradeFallback(GlobalDegradeFallback _degradeFallback) {
        degradeFallback = _degradeFallback;
    }

    public static void setFlowFallback(GlobalFlowFallback _flowFallback) {
        flowFallback = _flowFallback;
    }
}
