package com.littlehow.tool.sentinel.feign;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import feign.Feign;
import feign.InvocationHandlerFactory.MethodHandler;
import feign.MethodMetadata;
import feign.Target;
import feign.hystrix.FallbackFactory;
import com.littlehow.tool.sentinel.base.*;
import com.littlehow.tool.sentinel.config.FeignSentinelConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import static feign.Util.checkNotNull;

/**
 * {@link InvocationHandler} handle invocation that protected by Sentinel
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class SentinelInvocationHandler implements InvocationHandler {

    private final Target<?> target;
    private final Map<Method, MethodHandler> dispatch;

    private FallbackFactory fallbackFactory;
    private Map<Method, Method> fallbackMethodMap;

    SentinelInvocationHandler(Target<?> target, Map<Method, MethodHandler> dispatch,
                              FallbackFactory fallbackFactory) {
        this.target = checkNotNull(target, "target");
        this.dispatch = checkNotNull(dispatch, "dispatch");
        this.fallbackFactory = fallbackFactory;
        this.fallbackMethodMap = toFallbackMethod(dispatch);
    }

    SentinelInvocationHandler(Target<?> target, Map<Method, MethodHandler> dispatch) {
        this.target = checkNotNull(target, "target");
        this.dispatch = checkNotNull(dispatch, "dispatch");
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable {
        if ("equals".equals(method.getName())) {
            try {
                Object otherHandler = args.length > 0 && args[0] != null
                        ? Proxy.getInvocationHandler(args[0])
                        : null;
                return equals(otherHandler);
            }
            catch (IllegalArgumentException e) {
                return false;
            }
        }
        else if ("hashCode".equals(method.getName())) {
            return hashCode();
        }
        else if ("toString".equals(method.getName())) {
            return toString();
        }
        Object result;
        MethodHandler methodHandler = this.dispatch.get(method);
        // only handle by HardCodedTarget
        if (target instanceof Target.HardCodedTarget) {
            Target.HardCodedTarget hardCodedTarget = (Target.HardCodedTarget) target;
            MethodMetadata methodMetadata = SentinelContractHolder.METADATA_MAP
                    .get(hardCodedTarget.type().getName()
                            + Feign.configKey(hardCodedTarget.type(), method));
            // resource default is HttpMethod:protocol://url
            String resourceName = methodMetadata.template().method().toUpperCase() + ":"
                    + hardCodedTarget.url() + methodMetadata.template().url();
            //添加熔断限流规则
            FeignRuleManager.addRule(resourceName, method);
            Entry entry = null;
            try {
                ContextUtil.enter(resourceName);
                entry = SphU.entry(resourceName, EntryType.OUT, 1, args);
                ResourceContext.addResource(resourceName);
                result = methodHandler.invoke(args);
            } catch (DegradeException e) {
                result = getDegradeFallbackResult(method, args, e);
            } catch (FlowException e) {
                result = GlobalFallbackService.flowFallback();
            } catch (Throwable ex) {
                if (ex instanceof InvocationTargetException) {
                    ex = ((InvocationTargetException) ex).getTargetException();
                }
                if (ExceptionHandler.traceException(ex)) {
                    Tracer.trace(ex);
                    if (FeignSentinelConfig.getGlobalFallback()) {
                        return getDegradeFallbackResult(method, args, ex);
                    }
                }
                throw ex;
            } finally {
                if (entry != null) {
                    entry.exit(1, args);
                }
                ContextUtil.exit();
            }
        } else {
            // other target type using default strategy
            result = methodHandler.invoke(args);
        }
        return result;
    }

    /**
     * 获取fallback
     * @param method
     * @param args
     * @param ex
     * @return
     * @throws Throwable
     */
    private Object getDegradeFallbackResult(Method method, final Object[] args, Throwable ex) throws Throwable {
        if (fallbackFactory != null && fallbackMethodMap.containsKey(method)) {
            try {
                return fallbackMethodMap.get(method)
                        .invoke(fallbackFactory.create(ex), args);
            } catch (IllegalAccessException e) {
                return GlobalFallbackService.degradeFallback(ex);
            } catch (InvocationTargetException e) {
                if (ExceptionHandler.traceException(e.getTargetException())) {
                    return GlobalFallbackService.degradeFallback(ex);
                } else {
                    throw e.getTargetException();
                }
            }
        } else {
            return GlobalFallbackService.degradeFallback(ex);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SentinelInvocationHandler) {
            SentinelInvocationHandler other = (SentinelInvocationHandler) obj;
            return target.equals(other.target);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public String toString() {
        return target.toString();
    }

    static Map<Method, Method> toFallbackMethod(Map<Method, MethodHandler> dispatch) {
        Map<Method, Method> result = new LinkedHashMap<>();
        for (Method method : dispatch.keySet()) {
            method.setAccessible(true);
            result.put(method, method);
        }
        return result;
    }
}
