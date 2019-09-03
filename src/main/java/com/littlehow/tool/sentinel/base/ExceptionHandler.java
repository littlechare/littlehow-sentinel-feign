package com.littlehow.tool.sentinel.base;

import java.util.HashSet;
import java.util.Set;

public class ExceptionHandler {
    private static final Set<String> white = new HashSet<>();
    static {
        white.add("BizException");
        white.add("AssertException");
        white.add("InnerArgumentException");
        white.add("SentinelException");
    }
    /**
     * 判定是否是需要追踪的exception
     * @param t  -- 异常
     * @return  true表示需要追踪
     */
    public static boolean traceException(Throwable t) {
        return traceException(t, 10);
    }

    /**
     * 指定深度，防止死递归
     * @param t    -- 异常
     * @param deep -- 深度
     * @return
     */
    public static boolean traceException(Throwable t, int deep) {
        if (white.contains(exceptionName(t))) {
            return false;
        }
        if (deep <= 0) {//深度用完还没找到白名单，则需要追踪
            return true;
        }
        if (t.getCause() != null) {
            return traceException(t.getCause(), deep - 1);
        }
        return true;
    }

    private static String exceptionName(Throwable t) {
        String name = t.getClass().getName();
        if (name.contains(".")) {
            name = name.substring(name.lastIndexOf(".") + 1);
        }
        if (name.contains("$")) {
            name = name.substring(name.lastIndexOf("$") + 1);
        }
        return name;
    }
}
