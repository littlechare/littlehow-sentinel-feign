package com.littlehow.tool.sentinel.feign;

import com.alibaba.csp.sentinel.SphU;
import com.littlehow.tool.sentinel.base.GlobalDegradeFallback;
import com.littlehow.tool.sentinel.base.GlobalFlowFallback;
import com.littlehow.tool.sentinel.exception.SentinelException;
import feign.Feign;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ComponentScan(basePackages = "com.littlehow.tool.sentinel")
@ConditionalOnClass({ SphU.class, Feign.class })
public class SentinelFeignAutoConfiguration {

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "feign.sentinel.enabled")
    public Feign.Builder feignSentinelBuilder() {
        return SentinelFeign.builder();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalDegradeFallback degradeFallback() {
        return () -> { throw new SentinelException("degrade exception", "666001"); };
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalFlowFallback flowFallback() {
        return () -> { throw new SentinelException("too many requests", "666002"); };
    }
}