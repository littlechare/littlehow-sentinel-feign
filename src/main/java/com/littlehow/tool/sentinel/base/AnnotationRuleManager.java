package com.littlehow.tool.sentinel.base;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.littlehow.tool.sentinel.annotation.Degrade;
import com.littlehow.tool.sentinel.annotation.Fallback;
import com.littlehow.tool.sentinel.annotation.Flow;
import com.littlehow.tool.sentinel.annotation.Rule;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AnnotationRuleManager {
    /**
     * 加上限流规则
     * @param resource     -- 资源名
     * @param annotations  -- 注解
     * @return
     */
    public static List<FlowRule> getFlowRule(String resource, Annotation[] annotations) {
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
    public static Pair<Class, List<DegradeRule>> getDegradeRule(String resource, Annotation[] annotations) {
        List<DegradeRule> degradeRules = new ArrayList<>();
        ValueHolder<Class> valueHolder = new ValueHolder<>();
        Stream.of(annotations).forEach(o -> {
            if (o instanceof Rule) {
                Degrade[] degrades = ((Rule)o).degrades();
                Stream.of(degrades).forEach(d ->
                    degradeRules.add(RuleManager.createDegradeRule(resource, d.count(), d.timeWindow(), d.grade()))
                );
            } else if (o instanceof Degrade) {
                Degrade degrade = (Degrade)o;
                degradeRules.add(RuleManager.createDegradeRule(resource, degrade.count(), degrade.timeWindow(), degrade.grade()));
            } else if (o instanceof Fallback) {
                valueHolder.setValue(((Fallback)o).value());
            }
        });
        return new Pair<>(valueHolder.getValue(), degradeRules);
    }
}
