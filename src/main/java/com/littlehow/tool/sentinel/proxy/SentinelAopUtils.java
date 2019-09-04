package com.littlehow.tool.sentinel.proxy;

import com.littlehow.tool.sentinel.annotation.Degrade;
import com.littlehow.tool.sentinel.annotation.Flow;
import com.littlehow.tool.sentinel.annotation.Rule;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SentinelAopUtils {

    private static Map<Class, List<Method>> classMethodCache = new ConcurrentHashMap<>();

    /**
     * 获取代理类
     * @param clazz  -- 获取到代理类
     * @param <T>
     * @return
     */
    public static <T> T getProxyObject(Class<T> clazz) {
        try {
            T obj = clazz.newInstance();
            return getProxyObject(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取代理类
     * @param target  -- 目标
     * @return  -- 代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProxyObject(T target) {
        if (target == null) return null;
        //获取Rule,Degrade,Flow注解
        Class clazz = target.getClass();
        if (!classMethodCache.containsKey(clazz)) {
            List<Method> methods = getTargetMethod(clazz);
            if (CollectionUtils.isEmpty(methods)) {
                return target;
            } else {
                methods.forEach(o -> SentinelExecute.setRule(o, clazz));
            }
        } else if (CollectionUtils.isEmpty(classMethodCache.get(clazz))) {
            return target;
        }
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addAdvice((MethodInterceptor) SentinelExecute::invoke);
        return (T)proxyFactory.getProxy();
    }

    /**
     * @param clazz  -- 类型
     * @return
     */
    private static List<Method> getTargetMethod(Class clazz) {
        synchronized (clazz) {
            List<Method> methods = classMethodCache.get(clazz);
            if (methods == null) {
                List<Method> methodTmp = new ArrayList<>();
                ReflectionUtils.doWithMethods(clazz, (method) -> {
                    if (method.isAnnotationPresent(Degrade.class)
                            || method.isAnnotationPresent(Flow.class)
                            || method.isAnnotationPresent(Rule.class)) {
                        methodTmp.add(method);
                    }
                });
                classMethodCache.put(clazz, methodTmp);
                methods = methodTmp;
            }
            return methods;
        }
    }
}
