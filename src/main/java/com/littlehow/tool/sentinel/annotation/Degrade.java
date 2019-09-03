package com.littlehow.tool.sentinel.annotation;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Degrade {
    int grade() default RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT;
    int timeWindow() default 10;
    double count() default 20;
}
