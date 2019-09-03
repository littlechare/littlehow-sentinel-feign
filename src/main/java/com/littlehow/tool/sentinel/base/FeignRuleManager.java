package com.littlehow.tool.sentinel.base;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.littlehow.tool.sentinel.annotation.Degrade;
import com.littlehow.tool.sentinel.annotation.Flow;
import com.littlehow.tool.sentinel.annotation.Rule;
import com.littlehow.tool.sentinel.config.FeignSentinelConfig;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
        List<FlowRule> flowRules = new ArrayList<>();
        Stream.of(annotations).forEach(o -> {
            if (o instanceof Rule) {
                Flow[] flows = ((Rule)o).flows();
                Stream.of(flows).forEach(f ->
                    flowRules.add(RuleManager.createFlowRule(resource, f.count(), f.grade()))
                );
            } else if (o instanceof Flow) {
                Flow flow = (Flow)o;
                flowRules.add(RuleManager.createFlowRule(resource, flow.count(), flow.grade()));
            }
        });
        //如果需要加上默认限流，可以在此处判定
        return flowRules;
    }

    /**
     * 加上降级规则
     * @param resource     -- 资源名
     * @param annotations  -- 注解
     * @return
     */
    private static List<DegradeRule> getDegradeRule(String resource, Annotation[] annotations) {
        List<DegradeRule> degradeRules = new ArrayList<>();
        Stream.of(annotations).forEach(o -> {
            if (o instanceof Rule) {
                Degrade[] degrades = ((Rule)o).degrades();
                Stream.of(degrades).forEach(d ->
                        degradeRules.add(RuleManager.createDegradeRule(resource, d.count(), d.timeWindow(), d.grade()))
                );
            } else if (o instanceof Degrade) {
                Degrade degrade = (Degrade)o;
                degradeRules.add(RuleManager.createDegradeRule(resource, degrade.count(), degrade.timeWindow(), degrade.grade()));
            }
        });
        if (degradeRules.isEmpty()) {//此处加上默认降级规则
            return FeignSentinelConfig.getDefaultDegrade(resource);
        }
        return degradeRules;
    }
}
