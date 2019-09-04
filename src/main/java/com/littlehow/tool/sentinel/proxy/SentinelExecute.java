package com.littlehow.tool.sentinel.proxy;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.littlehow.tool.sentinel.base.*;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SentinelExecute {
    private static Map<Method, MethodInfo> fallbackMethod = new ConcurrentHashMap<>();

    private static Map<Method, String> resourceNames = new ConcurrentHashMap<>();

    /**
     * 设置规则
     * @param method  -- 方法
     */
    static void setRule(Method method, Class clazz) {
        String resourceName = getResourceName(method, clazz);
        //获取限流规则
        List<FlowRule> flowRules = AnnotationRuleManager.getFlowRule(resourceName, method.getAnnotations());
        RuleManager.updateFlowRule(resourceName, flowRules);
        //获取降级规则
        Pair<Class, List<DegradeRule>> pair = AnnotationRuleManager.getDegradeRule(resourceName, method.getAnnotations());
        RuleManager.updateDegradeRule(resourceName, pair.getV());
        //如果有设置降级class，则获取其降级信息
        setFallbackMethod(method, pair.getK(), clazz);
    }

    static Object invoke(MethodInvocation invocation) throws Throwable {
        Object result;
        Entry entry = null;
        final Object[] args = invocation.getArguments();
        try {
            String resourceName = getResourceName(invocation.getMethod(), invocation.getClass());
            ContextUtil.enter(resourceName);
            entry = SphU.entry(resourceName, EntryType.OUT, 1, args);
            ResourceContext.addResource(resourceName);
            result = invocation.proceed();
        } catch (DegradeException e) {
            result = getDegradeFallbackResult(invocation.getMethod(), args, e);
        } catch (FlowException e) {
            result = GlobalFallbackService.flowFallback();
        } catch (Throwable ex) {
            if (ex instanceof InvocationTargetException) {
                ex = ((InvocationTargetException) ex).getTargetException();
            }
            if (ExceptionHandler.traceException(ex)) {
                Tracer.trace(ex);
            }
            throw ex;
        } finally {
            if (entry != null) {
                entry.exit(1, args);
            }
            ContextUtil.exit();
        }
        return result;
    }

    /**
     * 获取fallback值
     * @param method  -- 方法
     * @param args    -- 参数
     * @param t       -- 异常
     * @return
     */
    private static Object getDegradeFallbackResult(Method method, Object[] args, Throwable t) throws Throwable {
        MethodInfo degradeMethod = fallbackMethod.get(method);
        if (degradeMethod != null) {
            try {
                return degradeMethod.invoke(args, t);
            } catch (IllegalAccessException e) {
                return GlobalFallbackService.degradeFallback(t);
            } catch (InvocationTargetException e) {
                if (ExceptionHandler.traceException(e.getTargetException())) {
                    return GlobalFallbackService.degradeFallback(t);
                } else {
                    throw e.getTargetException();
                }
            }
        } else {
            return GlobalFallbackService.degradeFallback(t);
        }
    }

    /**
     * 获取资源名称
     * @param method
     * @return
     */
    private static String getResourceName(Method method, Class clazz) {
        String resourceName = resourceNames.get(method);
        if (resourceName == null) {//资源名称为方法签名(除返回值和修饰符)
            method = getRealMethod(method, clazz);
            String[] info = method.toString().split(" ");
            resourceName = info[info.length - 1];
            resourceNames.put(method, resourceName);
        }
        return resourceName;
    }

    /**
     * 设置降级方法
     * @param method  -- 源方法
     * @param clazz   -- 降级class
     */
    private static void setFallbackMethod(Method method, Class clazz, Class origClass) {
        if (clazz == null || clazz == Void.class) return;
        method = getRealMethod(method, origClass);
        Class[] paramTypes = method.getParameterTypes();
        Method degradeMethod = ReflectionUtils.findMethod(clazz, method.getName(), paramTypes);
        if (degradeMethod == null) {//在参数后面追加一个异常
            paramTypes = ArrayUtils.add(paramTypes, paramTypes.length, Throwable.class);
            degradeMethod = ReflectionUtils.findMethod(clazz, method.getName(), paramTypes);
        }
        fallbackMethod.put(method, MethodInfo.getInstance(degradeMethod, paramTypes, clazz));
    }

    /**
     * 获取接口方法
     * @param method
     * @param clazz
     * @return
     */
    private static Method getRealMethod(Method method, Class clazz) {
        Class[] classes = clazz.getInterfaces();
        if (!ArrayUtils.isEmpty(classes)) {//获取接口方法
            Method tmp;
            for (Class c : classes) {
                tmp = ReflectionUtils.findMethod(c, method.getName(), method.getParameterTypes());
                if (tmp != null) {
                    method = tmp;
                    break;
                }
            }
        }
        return method;
    }

    /**
     * 处理自定义fallback
     */
    final static class MethodInfo {
        Method method;
        Object instance;
        Class[] arguments;
        static MethodInfo getInstance(Method method, Class[] arguments, Class clazz) {
            MethodInfo info = new MethodInfo();
            info.method = method;
            info.arguments = arguments;
            try {
                info.instance = clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return info;
        }

        Object invoke(Object[] args, Throwable t) throws InvocationTargetException, IllegalAccessException {
            Object result;
            if (args.length == arguments.length) {
                result = method.invoke(instance, args);
            } else {
                args = ArrayUtils.add(args, args.length, t);
                result = method.invoke(instance, args);
            }
            return result;
        }
    }
}
