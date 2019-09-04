package com.littlehow.tool.sentinel.base;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.littlehow.tool.sentinel.config.FeignSentinelConfig;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public class FeignRuleManager {

    /**
     * 添加规则
     * @param resource  -- 资源名称
     * @param method    -- 方法
     */
    public static void addRule(String resource, Method method) {
        //判断该资源是否已有规则
        Annotation[] annotations = method.getAnnotations();
        List<FlowRule> flowRules = getFlowRule(resource, annotations);
        if (!RuleManager.hasFlowRule(resource) && !CollectionUtils.isEmpty(flowRules)) {
            RuleManager.updateFlowRule(resource, flowRules);
        }

        List<DegradeRule> degradeRules = getDegradeRule(resource, annotations);
        if (!RuleManager.hasDegradeRule(resource) && !CollectionUtils.isEmpty(degradeRules)) {
            RuleManager.updateDegradeRule(resource, degradeRules);
        }
    }

    /**
     * 加上限流规则
     * @param resource     -- 资源名
     * @param annotations  -- 注解
     * @return
     */
    private static List<FlowRule> getFlowRule(String resource, Annotation[] annotations) {
        return AnnotationRuleManager.getFlowRule(resource, annotations);
    }

    /**
     * 加上降级规则
     * @param resource     -- 资源名
     * @param annotations  -- 注解
     * @return
     */
    private static List<DegradeRule> getDegradeRule(String resource, Annotation[] annotations) {
        Pair<Class, List<DegradeRule>> pair = AnnotationRuleManager.getDegradeRule(resource, annotations);
        List<DegradeRule> degradeRules = pair.getV();
        if (degradeRules.isEmpty()) {//此处加上默认降级规则
            return FeignSentinelConfig.getDefaultDegrade(resource);
        }
        return degradeRules;
    }
}
