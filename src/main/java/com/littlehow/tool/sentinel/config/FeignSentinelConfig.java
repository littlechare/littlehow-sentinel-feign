package com.littlehow.tool.sentinel.config;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.littlehow.tool.sentinel.base.RuleManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class FeignSentinelConfig {

    private static List<DegradeRule> degradeRules = new ArrayList<>();

    private static boolean globalFallback = false;

    /**
     * 设置默认规则
     * @param rules  -- 规则 grade,timeWindow,count#grade,time...
     */
    @Value("${feign.sentinel.degrade:1,10,0.4#2,10,20}")
    private void setDegradeRules(String rules) {
        degradeRules.clear();
        if (StringUtils.isEmpty(rules)) {
            return;
        }
        String[] info = rules.split("#");
        Stream.of(info).forEach(o -> {
            String[] rule = o.split(",");
            degradeRules.add(RuleManager.createDegradeRule(null,
                    Double.parseDouble(rule[2]), Integer.parseInt(rule[1]), Integer.parseInt(rule[0])));
        });
    }

    /**
     * 是否所有追踪异常都执行fallback
     * @param fallback
     */
    @Value("${feign.sentinel.fallback.all:false}")
    private void setGlobalFallback(boolean fallback) {
        globalFallback = fallback;
    }

    /**
     * 获取默认降级配置
     * @param resource  -- 资源名称
     * @return
     */
    public static List<DegradeRule> getDefaultDegrade(String resource) {
        if (CollectionUtils.isEmpty(degradeRules)) return new ArrayList<>();
        return degradeRules.stream().map(o -> RuleManager
                .createDegradeRule(resource, o.getCount(), o.getTimeWindow(), o.getGrade()))
                .collect(Collectors.toList());
    }

    public static boolean getGlobalFallback() {
        return globalFallback;
    }
}
