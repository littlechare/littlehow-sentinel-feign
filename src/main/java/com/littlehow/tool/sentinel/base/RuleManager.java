package com.littlehow.tool.sentinel.base;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RuleManager {

    private static final Map<String, List<FlowRule>> flowRuleMap = new ConcurrentHashMap<>();

    private static final Map<String, List<DegradeRule>> degradeRuleMap = new ConcurrentHashMap<>();

    /**
     * 判断是否有限流规则
     * @param resource  -- 资源名称
     * @return  true表示有
     */
    public static boolean hasFlowRule(String resource) {
        return flowRuleMap.containsKey(resource);
    }

    /**
     * 更新规则
     * @param flowRule  -- 限流规则
     */
    public static void updateFlowRule(FlowRule flowRule) {
        updateFlowRule(flowRule.getResource(), newArrayList(flowRule));
    }

    /**
     * 更新规则
     * @param resource   -- 资源名称
     * @param flowRules  -- 限流规则
     */
    public static void updateFlowRule(String resource, List<FlowRule> flowRules) {
        if (CollectionUtils.isEmpty(flowRules)) {
            return;
        }
        //限流规则，可以多个flow rule,该规则支持QPS和并发线程数的限流
        //FlowRuleManager.getRules()可以获取到已经设置的限流规则
        //重新加载限流规则，此处将覆盖原有的限流，所以如果想要不覆盖
        //请使用FlowRuleManager.getRules()获取到的加入到rules中
        List<FlowRule> old = FlowRuleManager.getRules();
        if (CollectionUtils.isEmpty(old)) {
            flowRuleMap.put(resource, flowRules);
            FlowRuleManager.loadRules(flowRules);
            return;
        }
        if (flowRuleMap.containsKey(resource)) {//需要将原有删除
            old = old.stream().filter(o -> !resource.equals(o.getResource())).collect(Collectors.toList());
        }
        old.addAll(flowRules);
        flowRuleMap.put(resource, flowRules);
        FlowRuleManager.loadRules(old);
    }

    /**
     * 删除对应资源名称的限流规则
     * @param resource  -- 资源名称
     */
    public static void removeFLowRule(String resource) {
        List<FlowRule> flowRules = FlowRuleManager.getRules();
        if (CollectionUtils.isEmpty(flowRules)) {
            return;
        }
        List<FlowRule> update = flowRules.stream().filter(o -> !resource.equals(o.getResource())).collect(Collectors.toList());
        if (flowRules.size() == update.size()) {
            return;//之前就没有该资源的
        }
        FlowRuleManager.loadRules(update);
        flowRuleMap.remove(resource);
    }


    /**
     * 生成限流规则
     * QPS限流
     * @see com.alibaba.csp.sentinel.slots.block.RuleConstant#FLOW_GRADE_QPS
     * 线程数限流
     * @see com.alibaba.csp.sentinel.slots.block.RuleConstant#FLOW_GRADE_THREAD
     * @param name   -- 资源名称
     * @param count  -- 数量
     * @param grade  -- 级别
     */
    public static FlowRule createFlowRule(String name, double count, int grade) {

        FlowRule rule = new FlowRule();
        //设置资源名称，sentinel限流都是以资源为单位进行
        rule.setResource(name);
        //使用grade限流
        rule.setGrade(grade);
        //grade对应达到的数量阈值
        rule.setCount(count);
        return rule;
    }

    /**
     * 判断资源是否已有熔断规则
     * @param resource  -- 资源名称
     * @return  true表示有
     */
    public static boolean hasDegradeRule(String resource) {
        return degradeRuleMap.containsKey(resource);
    }

    /**
     * 更新规则
     * @param degradeRule  -- 熔断规则
     */
    public static void updateDegradeRule(DegradeRule degradeRule) {
        updateDegradeRule(degradeRule.getResource(), newArrayList(degradeRule));
    }

    /**
     * 更新规则
     * @param resource      -- 资源名称
     * @param degradeRules  -- 熔断规则
     */
    public static void updateDegradeRule(String resource, List<DegradeRule> degradeRules) {
        if (CollectionUtils.isEmpty(degradeRules)) {
            return;
        }
        List<DegradeRule> old = DegradeRuleManager.getRules();
        if (CollectionUtils.isEmpty(old)) {
            degradeRuleMap.put(resource, degradeRules);
            DegradeRuleManager.loadRules(degradeRules);
            return;
        }
        if (degradeRuleMap.containsKey(resource)) {//需要将原有删除
            old = old.stream().filter(o -> !resource.equals(o.getResource())).collect(Collectors.toList());
        }
        old.addAll(degradeRules);
        degradeRuleMap.put(resource, degradeRules);
        DegradeRuleManager.loadRules(old);
    }

    /**
     * 删除对应资源名称的熔断规则
     * @param resource  -- 资源名称
     */
    public static void removeDegradeRule(String resource) {
        List<DegradeRule> degradeRules = DegradeRuleManager.getRules();
        if (CollectionUtils.isEmpty(degradeRules)) {
            return;
        }
        List<DegradeRule> update = degradeRules.stream().filter(o -> !resource.equals(o.getResource())).collect(Collectors.toList());
        if (degradeRules.size() == update.size()) {
            return;//之前就没有该资源的
        }
        DegradeRuleManager.loadRules(update);
        degradeRuleMap.remove(resource);
    }

    /**
     * 熔断降级规则配置
     * @param name         -- 资源名称
     * @param count        -- 数量
     * @param grade        -- 等级
     * @param timeWindow   -- 时间窗口
     */
    public static void degradeRule(String name, double count, int timeWindow, int grade) {
        //降级规则，可以多个degradeRule rule
        //DegradeRuleManager.getRules()可以获取到已经设置的降级规则
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule();
        //设置资源名称，sentinel降级都是以资源为单位进行
        rule.setResource(name);
        //使用异常统计降级,分钟统计,滑动时间窗口
        rule.setGrade(grade);
        //异常数达到的数量阈值
        rule.setCount(count);
        //秒级时间窗口,该值必须有且必须大于零，否则降级将无法生效
        rule.setTimeWindow(timeWindow);
        rules.add(rule);
        //重新加载限流规则，此处将覆盖原有的限流，所以如果想要不覆盖
        //请使用DegradeRuleManager.getRules()获取到的加入到rules中
        DegradeRuleManager.loadRules(rules);
    }

    /**
     * 生成熔断规则
     * 平均响应时间(ms)
     * @see com.alibaba.csp.sentinel.slots.block.RuleConstant#DEGRADE_GRADE_RT
     * 异常率
     * @see com.alibaba.csp.sentinel.slots.block.RuleConstant#DEGRADE_GRADE_EXCEPTION_RATIO
     * 异常数量
     * @see com.alibaba.csp.sentinel.slots.block.RuleConstant#DEGRADE_GRADE_EXCEPTION_COUNT
     * @param name        -- 资源名称
     * @param count       -- 数量
     * @param timeWindow  -- 时间窗口(s),如:10，表示降级将在10秒的窗口内
     * @param grade       -- 等级
     * @return
     */
    public static DegradeRule createDegradeRule(String name, double count, int timeWindow, int grade) {
        DegradeRule rule = new DegradeRule();
        //设置资源名称，sentinel降级都是以资源为单位进行
        rule.setResource(name);
        //使用异常统计降级,分钟统计,滑动时间窗口
        rule.setGrade(grade);
        //异常数达到的数量阈值
        rule.setCount(count);
        //以秒为单位的时间窗口,该值必须有且必须大于零，否则降级将无法生效
        rule.setTimeWindow(timeWindow);
        return rule;
    }

    private static <T> List<T> newArrayList(T ...args) {
        return new ArrayList<>(Arrays.asList(args));
    }
}
