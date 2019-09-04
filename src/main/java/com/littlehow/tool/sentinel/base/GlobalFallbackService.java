package com.littlehow.tool.sentinel.base;


import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.littlehow.tool.sentinel.exception.SentinelException;

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

    public static Object degradeFallback(Throwable t) throws Throwable {
        if (degradeFallback == null) {
            if (BlockException.isBlockException(t)) {
                throw new SentinelException("degrade fallback error", "666992");
            }
            throw t;
        }
        return degradeFallback.degradeFallback();
    }

    public static Object flowFallback() {
        if (flowFallback == null) {
            throw new SentinelException("flow fallback error", "666991");
        }
        return flowFallback.flowFallback();
    }

    public static void setDegradeFallback(GlobalDegradeFallback _degradeFallback) {
        degradeFallback = _degradeFallback;
    }

    public static void setFlowFallback(GlobalFlowFallback _flowFallback) {
        flowFallback = _flowFallback;
    }
}
